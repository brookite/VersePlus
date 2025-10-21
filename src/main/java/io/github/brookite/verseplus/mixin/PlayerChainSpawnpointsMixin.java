package io.github.brookite.verseplus.mixin;

import com.mojang.serialization.Codec;
import io.github.brookite.verseplus.interfaces.ChainedRespawnManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Optional;

import static io.github.brookite.verseplus.VersePlusLimits.MAX_RESPAWNS;

@Mixin(ServerPlayerEntity.class)
public abstract class PlayerChainSpawnpointsMixin implements ChainedRespawnManager {
    @Unique public ArrayDeque<ServerPlayerEntity.Respawn> respawns = new ArrayDeque<>();
    @Shadow private ServerPlayerEntity.Respawn respawn;
    @Shadow private MinecraftServer server;

    @Invoker("findRespawnPosition")
    static Optional<ServerPlayerEntity.RespawnPos> findRespawnPosition(ServerWorld world, ServerPlayerEntity.Respawn respawn, boolean bl) {
        throw new AssertionError(); // unreachable
    }

    /**
     * @author brookit
     * @reason add chained respawns
     */
    @Overwrite
    public @Nullable ServerPlayerEntity.Respawn getRespawn() {
        var toRemoveRespawns = this.respawns.stream()
                .filter(r -> !r.forced()
                        && findRespawnPosition(this.server.getWorld(ServerPlayerEntity.Respawn.getDimension(r)), r, false)
                        .isEmpty()).toList();

        for (var invalidRespawn : toRemoveRespawns) {
            this.respawns.remove(invalidRespawn);
        }

        if (this.respawn != null
                && findRespawnPosition(this.server.getWorld(ServerPlayerEntity.Respawn.getDimension(this.respawn)), this.respawn, false).isEmpty()
                && this.respawns.size() > 0) {
            this.respawn = this.respawns.removeFirst();
        }

        return this.respawn;
    }

    @Inject(at = @At("TAIL"), method = "copyFrom(Lnet/minecraft/server/network/ServerPlayerEntity;Z)V")
    public void copyFrom(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        this.respawns = new ArrayDeque<>(((ChainedRespawnManager) oldPlayer).getRespawns());
    }

    @Override
    @Unique
    public ArrayDeque<ServerPlayerEntity.Respawn> getRespawns() {
        return this.respawns;
    }

    @Inject(at = @At("HEAD"), method="setSpawnPoint(Lnet/minecraft/server/network/ServerPlayerEntity$Respawn;Z)V")
    public void setSpawnPoint(ServerPlayerEntity.Respawn respawn, boolean sendMessage, CallbackInfo ci) {
        List<ServerPlayerEntity.Respawn> duplicates = respawns.stream()
                .filter(e -> e != null && e.posEquals(respawn))
                .toList();

        for (ServerPlayerEntity.Respawn duplicate : duplicates) {
            respawns.remove(duplicate);
        }

        if (this.respawn != null && respawn != null && !respawn.posEquals(this.respawn)) {
            respawns.addFirst(this.respawn);
            if (respawns.size() > MAX_RESPAWNS) {
                respawns.removeLast();
            }
        }
    }

    @Inject(at = @At("TAIL"), method="writeCustomData(Lnet/minecraft/storage/WriteView;)V")
    protected void writeCustomData(WriteView view, CallbackInfo ci) {
        view.put("respawnChain", Codec.list(ServerPlayerEntity.Respawn.CODEC), this.respawns.stream().toList());
    }

    @Inject(at = @At("TAIL"), method="readCustomData(Lnet/minecraft/storage/ReadView;)V")
    protected void readCustomData(ReadView view, CallbackInfo ci) {
        this.respawns = new ArrayDeque<>(view.read("respawnChain", Codec.list(ServerPlayerEntity.Respawn.CODEC)).orElse(List.of()));
    }
}
