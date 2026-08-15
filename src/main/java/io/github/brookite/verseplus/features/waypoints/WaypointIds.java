package io.github.brookite.verseplus.features.waypoints;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class WaypointIds {
    private WaypointIds() {
    }

    public static UUID personal(UUID owner, String name) {
        return UUID.nameUUIDFromBytes(("verseplus:waypoint:" + owner + ':' + name).getBytes(StandardCharsets.UTF_8));
    }
}
