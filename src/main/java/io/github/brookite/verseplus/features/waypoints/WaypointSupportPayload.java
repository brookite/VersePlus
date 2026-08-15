package io.github.brookite.verseplus.features.waypoints;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record WaypointSupportPayload() implements CustomPacketPayload {
    public static final Type<WaypointSupportPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("verseplus", "waypoint_support"));
    public static final StreamCodec<RegistryFriendlyByteBuf, WaypointSupportPayload> CODEC = StreamCodec.unit(new WaypointSupportPayload());

    @Override
    public Type<WaypointSupportPayload> type() {
        return TYPE;
    }
}
