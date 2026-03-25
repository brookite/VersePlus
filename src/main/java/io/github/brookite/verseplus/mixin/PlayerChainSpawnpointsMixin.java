package io.github.brookite.verseplus.mixin;

import com.mojang.serialization.Codec;
import io.github.brookite.verseplus.interfaces.ChainedRespawnManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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

@Mixin(ServerPlayer.class)
public abstract class PlayerChainSpawnpointsMixin implements ChainedRespawnManager {
    @Unique public ArrayDeque<ServerPlayer.RespawnConfig> respawns = new ArrayDeque<>();
    @Shadow private ServerPlayer.RespawnConfig respawnConfig;
    @Shadow private MinecraftServer server;

    @Invoker("findRespawnAndUseSpawnBlock")
    static Optional<ServerPlayer.RespawnPosAngle> findRespawnPosition(ServerLevel world, ServerPlayer.RespawnConfig respawn, boolean bl) {
        throw new AssertionError(); // unreachable
    }

    /**
     * @author brookit
     * @reason add chained respawns
     */
    @Overwrite
    public @Nullable ServerPlayer.RespawnConfig getRespawnConfig() {
        var toRemoveRespawns = this.respawns.stream()
                .filter(r -> !r.forced()
                        && findRespawnPosition(this.server.getLevel(ServerPlayer.RespawnConfig.getDimensionOrDefault(r)), r, false)
                        .isEmpty()).toList();

        for (var invalidRespawn : toRemoveRespawns) {
            this.respawns.remove(invalidRespawn);
        }

        if (this.respawnConfig != null
                && findRespawnPosition(this.server.getLevel(ServerPlayer.RespawnConfig.getDimensionOrDefault(this.respawnConfig)), this.respawnConfig, false).isEmpty()
                && this.respawns.size() > 0) {
            this.respawnConfig = this.respawns.removeFirst();
        }

        return this.respawnConfig;
    }

    @Inject(at = @At("TAIL"), method = "restoreFrom(Lnet/minecraft/server/level/ServerPlayer;Z)V")
    public void copyFrom(ServerPlayer oldPlayer, boolean alive, CallbackInfo ci) {
        this.respawns = new ArrayDeque<>(((ChainedRespawnManager) oldPlayer).getRespawns());
    }

    @Override
    @Unique
    public ArrayDeque<ServerPlayer.RespawnConfig> getRespawns() {
        return this.respawns;
    }

    @Inject(at = @At("HEAD"), method="setRespawnPosition(Lnet/minecraft/server/level/ServerPlayer$RespawnConfig;Z)V")
    public void setSpawnPoint(ServerPlayer.RespawnConfig respawn, boolean sendMessage, CallbackInfo ci) {
        List<ServerPlayer.RespawnConfig> duplicates = respawns.stream()
                .filter(e -> e != null && e.isSamePosition(respawn))
                .toList();

        for (ServerPlayer.RespawnConfig duplicate : duplicates) {
            respawns.remove(duplicate);
        }

        if (this.respawnConfig != null && respawn != null && !respawn.isSamePosition(this.respawnConfig)) {
            respawns.addFirst(this.respawnConfig);
            if (respawns.size() > MAX_RESPAWNS) {
                respawns.removeLast();
            }
        }
    }

    @Inject(at = @At("TAIL"), method="addAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueOutput;)V")
    protected void writeCustomData(ValueOutput view, CallbackInfo ci) {
        view.store("respawnChain", Codec.list(ServerPlayer.RespawnConfig.CODEC), this.respawns.stream().toList());
    }

    @Inject(at = @At("TAIL"), method="readAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueInput;)V")
    protected void readCustomData(ValueInput view, CallbackInfo ci) {
        this.respawns = new ArrayDeque<>(view.read("respawnChain", Codec.list(ServerPlayer.RespawnConfig.CODEC)).orElse(List.of()));
    }
}
