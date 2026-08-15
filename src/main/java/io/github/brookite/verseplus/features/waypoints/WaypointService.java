package io.github.brookite.verseplus.features.waypoints;

import net.minecraft.network.protocol.game.ClientboundTrackedWaypointPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class WaypointService {
    private static final Map<UUID, net.minecraft.resources.ResourceKey<Level>> LAST_DIMENSIONS = new HashMap<>();

    private WaypointService() {
    }

    public static void synchronize(ServerPlayer player) {
        PersonalWaypointSavedData data = PersonalWaypointSavedData.get(player.level().getServer());
        for (PersonalWaypointSavedData.Entry waypoint : data.waypoints(player.getUUID())) {
            UUID id = WaypointIds.personal(player.getUUID(), waypoint.name());
            player.connection.send(ClientboundTrackedWaypointPacket.removeWaypoint(id));
            if (waypoint.dimension().equals(player.level().dimension())) {
                player.connection.send(ClientboundTrackedWaypointPacket.addWaypointPosition(id, waypoint.icon(), waypoint.position()));
            }
        }
        LAST_DIMENSIONS.put(player.getUUID(), player.level().dimension());
    }

    public static void forget(ServerPlayer player) {
        LAST_DIMENSIONS.remove(player.getUUID());
    }

    public static void untrack(ServerPlayer player, String name) {
        player.connection.send(ClientboundTrackedWaypointPacket.removeWaypoint(WaypointIds.personal(player.getUUID(), name)));
    }

    public static void synchronizeChangedDimensions(Iterable<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            if (!player.level().dimension().equals(LAST_DIMENSIONS.get(player.getUUID()))) {
                synchronize(player);
            }
        }
    }
}
