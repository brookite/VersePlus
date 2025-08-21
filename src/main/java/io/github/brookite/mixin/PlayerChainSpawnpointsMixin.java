package io.github.brookite.mixin;

import com.mojang.serialization.Codec;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Stream;

@Mixin(ServerPlayerEntity.class)
public abstract class PlayerChainSpawnpointsMixin {
    private static final int MAX_RESPAWNS = 32;

    private Stack<ServerPlayerEntity.Respawn> respawns = new Stack<>();
    @Shadow private ServerPlayerEntity.Respawn respawn;

    @Shadow
    public abstract ServerWorld getWorld();

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
        var allRespawn = Stream.concat(Stream.of(this.respawn), this.respawns.stream());
        var respawn = allRespawn.dropWhile(r -> !findRespawnPosition(getWorld(), r, true).isPresent()).findFirst().orElse(null);

        var toRemoveRespawns = allRespawn
                .takeWhile(r -> !findRespawnPosition(getWorld(), r, false)
                        .isPresent() && this.respawns.contains(respawn)).toList();

        for (var invalidRespawn : toRemoveRespawns) {
            respawns.remove(invalidRespawn);
        }
        return respawn;
    }

    @Inject(at = @At("HEAD"), method="setSpawnPoint(Lnet/minecraft/server/network/ServerPlayerEntity$Respawn;Z)V")
    public void setSpawnPoint(ServerPlayerEntity.Respawn respawn, boolean sendMessage, CallbackInfo ci) {
        List<ServerPlayerEntity.Respawn> duplicates = respawns.stream()
                .filter(e -> e.posEquals(respawn))
                .toList();

        for (ServerPlayerEntity.Respawn duplicate : duplicates) {
            respawns.remove(duplicate);
        }

        if (!respawn.posEquals(this.respawn)) {
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
        this.respawns = new Stack<>();
        this.respawns.addAll(view.read("respawnChain", Codec.list(ServerPlayerEntity.Respawn.CODEC)).orElse(List.of()));
    }
}
