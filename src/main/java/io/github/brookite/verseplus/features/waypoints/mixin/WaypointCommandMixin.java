package io.github.brookite.verseplus.features.waypoints.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.brookite.verseplus.features.waypoints.PersonalWaypointSavedData;
import io.github.brookite.verseplus.features.waypoints.WaypointService;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.HexColorArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.TeamColorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.commands.WaypointCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.scores.TeamColor;
import net.minecraft.world.waypoints.Waypoint;
import net.minecraft.world.waypoints.WaypointStyleAsset;
import net.minecraft.world.waypoints.WaypointStyleAssets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;

@Mixin(WaypointCommand.class)
public class WaypointCommandMixin {
    /** Replaces vanilla entity waypoints with named, player-owned static waypoints. */
    @Overwrite
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        dispatcher.register(Commands.literal("waypoint")
                .then(addCommand())
                .then(removeCommand())
                .then(modifyCommand()));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> addCommand() {
        return Commands.literal("add")
                .then(Commands.argument("name", StringArgumentType.word())
                        .executes(context -> add(context, WaypointCommandMixin::self))
                        .then(Commands.literal("@s").executes(context -> add(context, WaypointCommandMixin::self)))
                        .then(Commands.argument("players", EntityArgument.players())
                                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                                .executes(context -> add(context, WaypointCommandMixin::selectedPlayers))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> removeCommand() {
        return Commands.literal("remove")
                .then(Commands.argument("name", StringArgumentType.word())
                        .executes(context -> remove(context, WaypointCommandMixin::self))
                        .then(Commands.literal("@s").executes(context -> remove(context, WaypointCommandMixin::self)))
                        .then(Commands.argument("players", EntityArgument.players())
                                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                                .executes(context -> remove(context, WaypointCommandMixin::selectedPlayers))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> modifyCommand() {
        return Commands.literal("modify")
                .then(Commands.argument("name", StringArgumentType.word())
                        .then(colorBranch(WaypointCommandMixin::self))
                        .then(styleBranch(WaypointCommandMixin::self))
                        .then(Commands.literal("@s")
                                .then(colorBranch(WaypointCommandMixin::self))
                                .then(styleBranch(WaypointCommandMixin::self)))
                        .then(Commands.argument("players", EntityArgument.players())
                                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                                .then(colorBranch(WaypointCommandMixin::selectedPlayers))
                                .then(styleBranch(WaypointCommandMixin::selectedPlayers))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> colorBranch(TargetResolver targets) {
        return Commands.literal("color")
                .then(Commands.argument("color", TeamColorArgument.teamColor())
                        .executes(context -> changeColor(context, targets, TeamColorArgument.getTeamColor(context, "color").rgb())))
                .then(Commands.literal("hex")
                        .then(Commands.argument("color", HexColorArgument.hexColor())
                                .executes(context -> changeColor(context, targets, HexColorArgument.getHexColor(context, "color")))))
                .then(Commands.literal("reset").executes(context -> changeColor(context, targets, null)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> styleBranch(TargetResolver targets) {
        return Commands.literal("style")
                .then(Commands.literal("set")
                        .then(Commands.argument("style", IdentifierArgument.id())
                                .executes(context -> changeStyle(context, targets,
                                        ResourceKey.create(WaypointStyleAssets.ROOT_ID, IdentifierArgument.getId(context, "style"))))))
                .then(Commands.literal("reset").executes(context -> changeStyle(context, targets, WaypointStyleAssets.DEFAULT)));
    }

    private static int add(CommandContext<CommandSourceStack> context, TargetResolver targets) throws CommandSyntaxException {
        String name = waypointName(context);
        if (name == null) {
            return 0;
        }
        CommandSourceStack source = context.getSource();
        BlockPos position = BlockPos.containing(source.getPosition());
        PersonalWaypointSavedData data = PersonalWaypointSavedData.get(source.getServer());
        int changed = 0;
        for (ServerPlayer player : targets.resolve(context)) {
            if (data.addOrMove(player.getUUID(), name, source.getLevel().dimension(), position)) {
                WaypointService.synchronize(player);
                changed++;
            }
        }
        sendResult(source, "commands.verseplus.waypoint.added", changed);
        return changed;
    }

    private static int remove(CommandContext<CommandSourceStack> context, TargetResolver targets) throws CommandSyntaxException {
        String name = waypointName(context);
        if (name == null) {
            return 0;
        }
        CommandSourceStack source = context.getSource();
        PersonalWaypointSavedData data = PersonalWaypointSavedData.get(source.getServer());
        int changed = 0;
        for (ServerPlayer player : targets.resolve(context)) {
            if (data.remove(player.getUUID(), name)) {
                WaypointService.untrack(player, name);
                WaypointService.synchronize(player);
                changed++;
            }
        }
        sendResult(source, "commands.verseplus.waypoint.removed", changed);
        return changed;
    }

    private static int changeColor(CommandContext<CommandSourceStack> context, TargetResolver targets, Integer color) throws CommandSyntaxException {
        return change(context, targets, icon -> icon.color = Optional.ofNullable(color));
    }

    private static int changeStyle(CommandContext<CommandSourceStack> context, TargetResolver targets, ResourceKey<WaypointStyleAsset> style) throws CommandSyntaxException {
        return change(context, targets, icon -> icon.style = style);
    }

    private static int change(CommandContext<CommandSourceStack> context, TargetResolver targets, Consumer<Waypoint.Icon> change) throws CommandSyntaxException {
        String name = waypointName(context);
        if (name == null) {
            return 0;
        }
        CommandSourceStack source = context.getSource();
        PersonalWaypointSavedData data = PersonalWaypointSavedData.get(source.getServer());
        int changed = 0;
        for (ServerPlayer player : targets.resolve(context)) {
            if (data.changeIcon(player.getUUID(), name, change)) {
                WaypointService.synchronize(player);
                changed++;
            }
        }
        sendResult(source, "commands.verseplus.waypoint.updated", changed);
        return changed;
    }

    private static String waypointName(CommandContext<CommandSourceStack> context) {
        String name = StringArgumentType.getString(context, "name").toLowerCase(Locale.ROOT);
        if (!name.matches("[a-z0-9_-]{1,32}")) {
            context.getSource().sendFailure(Component.translatable("commands.verseplus.waypoint.invalid_name"));
            return null;
        }
        return name;
    }

    private static void sendResult(CommandSourceStack source, String message, int changed) {
        source.sendSuccess(() -> Component.translatable(message, changed), false);
    }

    private static Collection<ServerPlayer> self(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return List.of(context.getSource().getPlayerOrException());
    }

    private static Collection<ServerPlayer> selectedPlayers(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return EntityArgument.getPlayers(context, "players");
    }

    @FunctionalInterface
    private interface TargetResolver {
        Collection<ServerPlayer> resolve(CommandContext<CommandSourceStack> context) throws CommandSyntaxException;
    }
}
