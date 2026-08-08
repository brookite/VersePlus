package io.github.brookite.verseplus.features.chainedrespawn;

import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayDeque;

public interface ChainedRespawnManager {
    ArrayDeque<ServerPlayer.RespawnConfig> getRespawns();
}
