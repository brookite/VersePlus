package io.github.brookite.verseplus.features.waypoints;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public final class WaypointFeature {
    private WaypointFeature() {
    }

    public static void initialize() {
        PayloadTypeRegistry.serverboundPlay().register(WaypointSupportPayload.TYPE, WaypointSupportPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(WaypointSupportPayload.TYPE, (payload, context) -> {
            // Registering this channel lets clients distinguish a VersePlus server from vanilla.
        });
        ServerPlayConnectionEvents.JOIN.register((listener, sender, server) -> WaypointService.synchronize(listener.getPlayer()));
        ServerPlayConnectionEvents.DISCONNECT.register((listener, server) -> WaypointService.forget(listener.getPlayer()));
        ServerTickEvents.END_SERVER_TICK.register(server -> WaypointService.synchronizeChangedDimensions(server.getPlayerList().getPlayers()));
    }
}
