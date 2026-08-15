package io.github.brookite.verseplus.features.waypoints;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.brookite.verseplus.VersePlus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.waypoints.Waypoint;
import net.minecraft.world.waypoints.WaypointStyleAsset;
import net.minecraft.world.waypoints.WaypointStyleAssets;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public final class PersonalWaypointSavedData extends SavedData {
    public static final int MAX_WAYPOINTS_PER_PLAYER = 32;

    public record Entry(UUID owner, String name, ResourceKey<Level> dimension, BlockPos position, Identifier style, Optional<Integer> color) {
        private static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                UUIDUtil.CODEC.fieldOf("owner").forGetter(Entry::owner),
                Codec.STRING.fieldOf("name").forGetter(Entry::name),
                Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(Entry::dimension),
                BlockPos.CODEC.fieldOf("position").forGetter(Entry::position),
                Identifier.CODEC.fieldOf("style").forGetter(Entry::style),
                Codec.INT.optionalFieldOf("color").forGetter(Entry::color)
        ).apply(instance, Entry::new));

        public static Entry create(UUID owner, String name, ResourceKey<Level> dimension, BlockPos position) {
            return new Entry(owner, name, dimension, position, WaypointStyleAssets.DEFAULT.identifier(), Optional.empty());
        }

        public Waypoint.Icon icon() {
            Waypoint.Icon icon = new Waypoint.Icon();
            icon.style = ResourceKey.create(WaypointStyleAssets.ROOT_ID, style);
            icon.color = color;
            return icon;
        }

        public Entry movedTo(ResourceKey<Level> newDimension, BlockPos newPosition) {
            return new Entry(owner, name, newDimension, newPosition, style, color);
        }

        public Entry withIcon(Consumer<Waypoint.Icon> change) {
            Waypoint.Icon icon = icon();
            change.accept(icon);
            return new Entry(owner, name, dimension, position, icon.style.identifier(), icon.color);
        }
    }

    private static final Codec<PersonalWaypointSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Entry.CODEC.listOf().optionalFieldOf("waypoints", List.of()).forGetter(PersonalWaypointSavedData::entries)
    ).apply(instance, PersonalWaypointSavedData::new));

    private static final SavedDataType<PersonalWaypointSavedData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(VersePlus.MOD_ID, "personal_waypoints"),
            PersonalWaypointSavedData::new,
            CODEC,
            DataFixTypes.SAVED_DATA_MAP_DATA
    );

    private final Map<UUID, LinkedHashMap<String, Entry>> byOwner = new LinkedHashMap<>();

    public PersonalWaypointSavedData() {
    }

    private PersonalWaypointSavedData(List<Entry> entries) {
        entries.forEach(entry -> byOwner.computeIfAbsent(entry.owner(), ignored -> new LinkedHashMap<>()).put(entry.name(), entry));
    }

    public static PersonalWaypointSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean addOrMove(UUID owner, String name, ResourceKey<Level> dimension, BlockPos position) {
        LinkedHashMap<String, Entry> waypoints = byOwner.computeIfAbsent(owner, ignored -> new LinkedHashMap<>());
        Entry existing = waypoints.get(name);
        if (existing == null && waypoints.size() >= MAX_WAYPOINTS_PER_PLAYER) {
            return false;
        }
        Entry updated = existing == null ? Entry.create(owner, name, dimension, position) : existing.movedTo(dimension, position);
        if (!updated.equals(waypoints.put(name, updated))) {
            setDirty();
        }
        return true;
    }

    public boolean remove(UUID owner, String name) {
        Map<String, Entry> waypoints = byOwner.get(owner);
        if (waypoints == null || waypoints.remove(name) == null) {
            return false;
        }
        if (waypoints.isEmpty()) {
            byOwner.remove(owner);
        }
        setDirty();
        return true;
    }

    public boolean changeIcon(UUID owner, String name, Consumer<Waypoint.Icon> change) {
        Map<String, Entry> waypoints = byOwner.get(owner);
        if (waypoints == null) {
            return false;
        }
        Entry existing = waypoints.get(name);
        if (existing == null) {
            return false;
        }
        Entry updated = existing.withIcon(change);
        if (!updated.equals(waypoints.put(name, updated))) {
            setDirty();
        }
        return true;
    }

    public List<Entry> waypoints(UUID owner) {
        Map<String, Entry> waypoints = byOwner.get(owner);
        return waypoints == null ? List.of() : List.copyOf(waypoints.values());
    }

    private List<Entry> entries() {
        return byOwner.values().stream().flatMap(entries -> entries.values().stream()).toList();
    }
}
