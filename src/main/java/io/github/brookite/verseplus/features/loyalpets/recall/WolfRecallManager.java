package io.github.brookite.verseplus.features.loyalpets.recall;

import io.github.brookite.verseplus.features.loyalpets.data.LoyalPetAttachments;
import io.github.brookite.verseplus.features.loyalpets.data.PetOwnershipData;
import io.github.brookite.verseplus.features.loyalpets.data.TrainedWolfRecord;
import io.github.brookite.verseplus.features.loyalpets.data.TrainedWolfSavedData;
import io.github.brookite.verseplus.features.loyalpets.data.WolfHornMemory;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class WolfRecallManager {
    private static final int INDEX_UPDATE_INTERVAL = 20;
    private static final int RECALL_TIMEOUT_TICKS = 100;
    private static final double ALREADY_NEAR_DISTANCE_SQUARED = 12.0 * 12.0;
    private static final Map<MinecraftServer, State> STATES = new IdentityHashMap<>();

    private WolfRecallManager() {
    }

    public static void initialize() {
        LoyalPetTicketTypes.initialize();

        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof Wolf wolf) {
                state(level.getServer()).onLoad(wolf);
            }
        });
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) -> {
            if (entity instanceof Wolf wolf) {
                state(level.getServer()).onUnload(wolf);
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> state(server).tick());
        ServerLifecycleEvents.SERVER_STOPPED.register(STATES::remove);
    }

    public static void track(Wolf wolf) {
        if (wolf.level() instanceof ServerLevel level) {
            state(level.getServer()).track(wolf);
        }
    }

    public static void recall(ServerPlayer caller, Identifier learnedSound) {
        state(caller.level().getServer()).recall(caller, learnedSound);
    }

    private static State state(MinecraftServer server) {
        return STATES.computeIfAbsent(server, State::new);
    }

    private static final class State {
        private final MinecraftServer server;
        private final Map<UUID, Wolf> loadedWolves = new HashMap<>();
        private final Map<UUID, PendingRecall> pending = new HashMap<>();
        private final Map<SourceChunk, Set<UUID>> pendingByChunk = new HashMap<>();

        private State(MinecraftServer server) {
            this.server = server;
        }

        private void onLoad(Wolf wolf) {
            track(wolf);
            if (pending.containsKey(wolf.getUUID())) {
                resolveLoaded(wolf.getUUID());
            }
        }

        private void onUnload(Wolf wolf) {
            loadedWolves.remove(wolf.getUUID());
            RemovalReason reason = wolf.getRemovalReason();
            if (reason != null && reason.shouldDestroy()) {
                TrainedWolfSavedData.get(server).remove(wolf.getUUID());
            } else {
                updateIndex(wolf);
            }
        }

        private void track(Wolf wolf) {
            WolfHornMemory memory = wolf.getAttached(LoyalPetAttachments.WOLF_HORN_MEMORY);
            if (memory == null || memory.learnedSound().isEmpty() || !wolf.isTame()) {
                loadedWolves.remove(wolf.getUUID());
                TrainedWolfSavedData.get(server).remove(wolf.getUUID());
                return;
            }

            loadedWolves.put(wolf.getUUID(), wolf);
            updateIndex(wolf);
        }

        private void updateIndex(Wolf wolf) {
            WolfHornMemory memory = wolf.getAttached(LoyalPetAttachments.WOLF_HORN_MEMORY);
            if (memory == null || memory.learnedSound().isEmpty() || wolf.getOwnerReference() == null) {
                TrainedWolfSavedData.get(server).remove(wolf.getUUID());
                return;
            }

            PetOwnershipData ownership = wolf.getAttached(LoyalPetAttachments.PET_OWNERSHIP);
            TrainedWolfSavedData.get(server).put(new TrainedWolfRecord(
                    wolf.getUUID(),
                    wolf.level().dimension(),
                    wolf.chunkPosition(),
                    wolf.getOwnerReference().getUUID(),
                    ownership == null ? java.util.List.of() : ownership.additionalOwners(),
                    memory.learnedSound().orElseThrow(),
                    wolf.isOrderedToSit()
            ));
        }

        private void recall(ServerPlayer caller, Identifier sound) {
            UUID callerId = caller.getUUID();
            var candidates = TrainedWolfSavedData.get(server).records().stream()
                    .filter(record -> record.learnedSound().equals(sound) && record.belongsTo(callerId))
                    .toList();

            for (TrainedWolfRecord record : candidates) {
                Wolf loaded = loadedWolves.get(record.wolfId());
                if (loaded == null) {
                    ServerLevel recordedLevel = server.getLevel(record.dimension());
                    Entity entity = recordedLevel == null ? null : recordedLevel.getEntity(record.wolfId());
                    if (entity instanceof Wolf wolf) {
                        loaded = wolf;
                        track(wolf);
                    }
                }

                if (loaded != null) {
                    RecallResult result = recallLoaded(loaded, caller, sound);
                    if (result == RecallResult.AUDIBLE_FAILURE) {
                        playFailureSound(caller);
                    }
                } else if (!record.sitting()) {
                    queue(record, caller, sound);
                }
            }
        }

        private void queue(TrainedWolfRecord record, ServerPlayer caller, Identifier sound) {
            if (pending.containsKey(record.wolfId())) {
                return;
            }

            ServerLevel sourceLevel = server.getLevel(record.dimension());
            if (sourceLevel == null) {
                playFailureSound(caller);
                return;
            }

            SourceChunk source = new SourceChunk(record.dimension(), record.chunk());
            pending.put(record.wolfId(), new PendingRecall(
                    record.wolfId(), caller.getUUID(), sound, source, server.getTickCount() + RECALL_TIMEOUT_TICKS
            ));

            Set<UUID> chunkRequests = pendingByChunk.computeIfAbsent(source, ignored -> new HashSet<>());
            boolean firstRequest = chunkRequests.isEmpty();
            chunkRequests.add(record.wolfId());
            if (firstRequest) {
                sourceLevel.getChunkSource()
                        .addTicketAndLoadWithRadius(LoyalPetTicketTypes.PET_RECALL, record.chunk(), 0)
                        .whenComplete((ignored, error) -> server.execute(() -> resolveChunk(source, error != null)));
            }
        }

        private void resolveChunk(SourceChunk source, boolean loadFailed) {
            Set<UUID> wolfIds = pendingByChunk.get(source);
            if (wolfIds == null) {
                return;
            }

            for (UUID wolfId : new ArrayList<>(wolfIds)) {
                if (loadFailed) {
                    fail(wolfId);
                } else {
                    resolveLoaded(wolfId);
                }
            }
        }

        private void resolveLoaded(UUID wolfId) {
            PendingRecall request = pending.get(wolfId);
            if (request == null) {
                return;
            }

            Wolf wolf = loadedWolves.get(wolfId);
            if (wolf == null) {
                ServerLevel sourceLevel = server.getLevel(request.source().dimension());
                Entity entity = sourceLevel == null ? null : sourceLevel.getEntity(wolfId);
                if (entity instanceof Wolf loadedWolf) {
                    wolf = loadedWolf;
                    track(wolf);
                }
            }

            if (wolf == null) {
                return;
            }

            ServerPlayer caller = server.getPlayerList().getPlayer(request.callerId());
            if (caller != null) {
                RecallResult result = recallLoaded(wolf, caller, request.sound());
                if (result == RecallResult.AUDIBLE_FAILURE) {
                    playFailureSound(caller);
                }
            }
            finish(wolfId);
        }

        private RecallResult recallLoaded(Wolf wolf, ServerPlayer caller, Identifier sound) {
            WolfHornMemory memory = wolf.getAttached(LoyalPetAttachments.WOLF_HORN_MEMORY);
            if (!wolf.isTame()
                    || wolf.isOrderedToSit()
                    || wolf.isPassenger()
                    || wolf.mayBeLeashed()
                    || memory == null
                    || memory.learnedSound().filter(sound::equals).isEmpty()
                    || !isOwner(wolf, caller.getUUID())) {
                return RecallResult.SILENT_FAILURE;
            }

            ServerPlayer destination = resolveDestination(wolf, caller);
            if (destination == null) {
                return RecallResult.SILENT_FAILURE;
            }

            if (wolf.level() == destination.level()
                    && wolf.distanceToSqr(destination) < ALREADY_NEAR_DISTANCE_SQUARED) {
                updateIndex(wolf);
                return RecallResult.SUCCESS;
            }

            TamableAnimal teleported = PetTeleportation.teleportNear(wolf, destination);
            if (teleported instanceof Wolf recalledWolf) {
                track(recalledWolf);
            }
            return teleported == null ? RecallResult.AUDIBLE_FAILURE : RecallResult.SUCCESS;
        }

        private boolean isOwner(Wolf wolf, UUID playerId) {
            if (wolf.getOwnerReference() != null && wolf.getOwnerReference().getUUID().equals(playerId)) {
                return true;
            }
            PetOwnershipData ownership = wolf.getAttached(LoyalPetAttachments.PET_OWNERSHIP);
            return ownership != null && ownership.isAdditionalOwner(playerId);
        }

        private ServerPlayer resolveDestination(Wolf wolf, ServerPlayer caller) {
            if (wolf.getOwnerReference() != null) {
                ServerPlayer primary = server.getPlayerList().getPlayer(wolf.getOwnerReference().getUUID());
                if (isAvailable(primary)) {
                    return primary;
                }
            }
            return isOwner(wolf, caller.getUUID()) && isAvailable(caller) ? caller : null;
        }

        private boolean isAvailable(ServerPlayer player) {
            return player != null && player.isAlive() && !player.isSpectator();
        }

        private void tick() {
            if (server.getTickCount() % INDEX_UPDATE_INTERVAL == 0) {
                Iterator<Wolf> wolves = loadedWolves.values().iterator();
                while (wolves.hasNext()) {
                    Wolf wolf = wolves.next();
                    if (wolf.isRemoved()) {
                        wolves.remove();
                    } else {
                        updateIndex(wolf);
                    }
                }
            }

            for (PendingRecall request : new ArrayList<>(pending.values())) {
                if (server.getTickCount() >= request.expiresAt()) {
                    TrainedWolfSavedData.get(server).remove(request.wolfId());
                    fail(request.wolfId());
                } else if (loadedWolves.containsKey(request.wolfId())) {
                    resolveLoaded(request.wolfId());
                }
            }
        }

        private void fail(UUID wolfId) {
            PendingRecall request = pending.get(wolfId);
            if (request != null) {
                ServerPlayer caller = server.getPlayerList().getPlayer(request.callerId());
                if (caller != null) {
                    playFailureSound(caller);
                }
            }
            finish(wolfId);
        }

        private void finish(UUID wolfId) {
            PendingRecall request = pending.remove(wolfId);
            if (request == null) {
                return;
            }

            Set<UUID> chunkRequests = pendingByChunk.get(request.source());
            if (chunkRequests == null) {
                return;
            }

            chunkRequests.remove(wolfId);
            if (chunkRequests.isEmpty()) {
                pendingByChunk.remove(request.source());
                ServerLevel sourceLevel = server.getLevel(request.source().dimension());
                if (sourceLevel != null) {
                    sourceLevel.getChunkSource().removeTicketWithRadius(
                            LoyalPetTicketTypes.PET_RECALL,
                            request.source().chunk(),
                            0
                    );
                }
            }
        }

        private void playFailureSound(ServerPlayer player) {
            player.playSound(SoundEvents.FIRE_EXTINGUISH, 0.35F, 1.4F);
        }
    }

    private record PendingRecall(
            UUID wolfId,
            UUID callerId,
            Identifier sound,
            SourceChunk source,
            int expiresAt
    ) {
    }

    private record SourceChunk(ResourceKey<Level> dimension, ChunkPos chunk) {
    }

    private enum RecallResult {
        SUCCESS,
        SILENT_FAILURE,
        AUDIBLE_FAILURE
    }
}
