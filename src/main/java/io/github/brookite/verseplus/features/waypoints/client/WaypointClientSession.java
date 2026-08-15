package io.github.brookite.verseplus.features.waypoints.client;

import io.github.brookite.verseplus.features.waypoints.WaypointIds;
import io.github.brookite.verseplus.features.waypoints.WaypointSupportPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.scores.TeamColor;
import net.minecraft.world.waypoints.TrackedWaypoint;
import net.minecraft.world.waypoints.Waypoint;
import net.minecraft.world.waypoints.WaypointStyleAssets;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** Named client-only waypoints kept until the current server connection ends. */
public final class WaypointClientSession {
    private static final int MAX_WAYPOINTS = 32;
    private static final Map<String, LocalWaypoint> WAYPOINTS = new LinkedHashMap<>();

    private record LocalWaypoint(BlockPos position, Waypoint.Icon icon) {
    }

    private WaypointClientSession() {
    }

    public static void initialize() {
        ClientPlayConnectionEvents.DISCONNECT.register((listener, client) -> WAYPOINTS.clear());
    }

    public static boolean handleUnsupportedServerCommand(String command) {
        String[] arguments = command.trim().split("\\s+");
        if (arguments.length == 0 || !arguments[0].equalsIgnoreCase("waypoint")) {
            return false;
        }

        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null || ClientPlayNetworking.canSend(WaypointSupportPayload.TYPE)) {
            return false;
        }
        if (arguments.length < 3) {
            sendUsage(player);
            return true;
        }

        String name = normalizeName(arguments[2]);
        if (name == null) {
            sendFailure(player, "commands.verseplus.waypoint.invalid_name");
            return true;
        }

        return switch (arguments[1].toLowerCase(Locale.ROOT)) {
            case "add" -> add(client, player, name, arguments);
            case "remove" -> remove(client, player, name, arguments);
            case "modify" -> modify(client, player, name, arguments);
            default -> {
                sendUsage(player);
                yield true;
            }
        };
    }

    private static boolean add(Minecraft client, LocalPlayer player, String name, String[] arguments) {
        if (!hasOnlySelfSelector(arguments, 3)) {
            sendUsage(player);
            return true;
        }
        if (!WAYPOINTS.containsKey(name) && WAYPOINTS.size() >= MAX_WAYPOINTS) {
            sendFailure(player, "commands.verseplus.waypoint.limit_reached");
            return true;
        }
        LocalWaypoint waypoint = new LocalWaypoint(player.blockPosition(), new Waypoint.Icon());
        WAYPOINTS.put(name, waypoint);
        track(client, player, name, waypoint);
        player.sendSystemMessage(Component.translatable("commands.verseplus.waypoint.local_saved"));
        return true;
    }

    private static boolean remove(Minecraft client, LocalPlayer player, String name, String[] arguments) {
        if (!hasOnlySelfSelector(arguments, 3)) {
            sendUsage(player);
            return true;
        }
        if (WAYPOINTS.remove(name) == null) {
            sendFailure(player, "commands.verseplus.waypoint.not_found", name);
            return true;
        }
        if (client.getConnection() != null) {
            client.getConnection().getWaypointManager().untrackWaypoint(TrackedWaypoint.empty(WaypointIds.personal(player.getUUID(), name)));
        }
        player.sendSystemMessage(Component.translatable("commands.verseplus.waypoint.local_removed"));
        return true;
    }

    private static boolean modify(Minecraft client, LocalPlayer player, String name, String[] arguments) {
        int actionIndex = arguments.length > 3 && arguments[3].equalsIgnoreCase("@s") ? 4 : 3;
        if (arguments.length < actionIndex + 2) {
            sendUsage(player);
            return true;
        }
        LocalWaypoint waypoint = WAYPOINTS.get(name);
        if (waypoint == null) {
            sendFailure(player, "commands.verseplus.waypoint.not_found", name);
            return true;
        }
        boolean changed = switch (arguments[actionIndex].toLowerCase(Locale.ROOT)) {
            case "color" -> updateColor(waypoint.icon(), arguments, actionIndex);
            case "style" -> updateStyle(waypoint.icon(), arguments, actionIndex);
            default -> false;
        };
        if (!changed || !hasOnlySelfSelector(arguments, actionIndex + colorOrStyleLength(arguments, actionIndex))) {
            sendUsage(player);
            return true;
        }
        track(client, player, name, waypoint);
        player.sendSystemMessage(Component.translatable("commands.verseplus.waypoint.local_saved"));
        return true;
    }

    private static int colorOrStyleLength(String[] arguments, int actionIndex) {
        return arguments.length > actionIndex + 1 && (arguments[actionIndex + 1].equalsIgnoreCase("hex") || arguments[actionIndex + 1].equalsIgnoreCase("set")) ? 3 : 2;
    }

    private static boolean updateColor(Waypoint.Icon icon, String[] arguments, int actionIndex) {
        if (arguments.length >= actionIndex + 2 && arguments[actionIndex + 1].equalsIgnoreCase("reset")) {
            icon.color = java.util.Optional.empty();
            return true;
        }
        if (arguments.length >= actionIndex + 2) {
            TeamColor color = TeamColor.byName(arguments[actionIndex + 1]);
            if (color != null) {
                icon.color = java.util.Optional.of(color.rgb());
                return true;
            }
        }
        if (arguments.length >= actionIndex + 3 && arguments[actionIndex + 1].equalsIgnoreCase("hex")) {
            String hex = arguments[actionIndex + 2].startsWith("#") ? arguments[actionIndex + 2].substring(1) : arguments[actionIndex + 2];
            if (hex.matches("[0-9a-fA-F]{6}")) {
                icon.color = java.util.Optional.of(Integer.parseInt(hex, 16));
                return true;
            }
        }
        return false;
    }

    private static boolean updateStyle(Waypoint.Icon icon, String[] arguments, int actionIndex) {
        if (arguments.length >= actionIndex + 2 && arguments[actionIndex + 1].equalsIgnoreCase("reset")) {
            icon.style = WaypointStyleAssets.DEFAULT;
            return true;
        }
        if (arguments.length >= actionIndex + 3 && arguments[actionIndex + 1].equalsIgnoreCase("set")) {
            Identifier identifier = Identifier.tryParse(arguments[actionIndex + 2]);
            if (identifier != null) {
                icon.style = ResourceKey.create(WaypointStyleAssets.ROOT_ID, identifier);
                return true;
            }
        }
        return false;
    }

    private static boolean hasOnlySelfSelector(String[] arguments, int index) {
        return arguments.length == index || arguments.length == index + 1 && arguments[index].equalsIgnoreCase("@s");
    }

    private static String normalizeName(String rawName) {
        String name = rawName.toLowerCase(Locale.ROOT);
        return name.matches("[a-z0-9_-]{1,32}") ? name : null;
    }

    private static void track(Minecraft client, LocalPlayer player, String name, LocalWaypoint waypoint) {
        if (client.getConnection() != null) {
            client.getConnection().getWaypointManager().trackWaypoint(
                    TrackedWaypoint.setPosition(WaypointIds.personal(player.getUUID(), name), waypoint.icon(), waypoint.position()));
        }
    }

    private static void sendUsage(LocalPlayer player) {
        player.sendSystemMessage(Component.translatable("commands.verseplus.waypoint.local_usage").withStyle(ChatFormatting.RED));
    }

    private static void sendFailure(LocalPlayer player, String key, Object... arguments) {
        player.sendSystemMessage(Component.translatable(key, arguments).withStyle(ChatFormatting.RED));
    }
}
