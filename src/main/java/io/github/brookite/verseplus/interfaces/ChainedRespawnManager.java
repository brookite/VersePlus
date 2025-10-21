package io.github.brookite.verseplus.interfaces;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayDeque;

public interface ChainedRespawnManager {
    ArrayDeque<ServerPlayerEntity.Respawn> getRespawns();
}
