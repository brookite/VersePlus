package io.github.brookite.verseplus;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ItemDropLogHandler extends SavedData {
    private static final int LOG_SLICE_TIME = 5 * 60;
    private static final Codec<UUID> UUID_CODEC = Codec.STRING.xmap(UUID::fromString, UUID::toString);

    private static final class DropEntry {
        static final Codec<DropEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.LONG.fieldOf("timestamp").forGetter(DropEntry::timestamp),
                Codec.list(ItemStack.CODEC).fieldOf("stacks").forGetter(DropEntry::stacks)
        ).apply(instance, DropEntry::new));

        private final long timestamp;
        private final List<ItemStack> stacks;

        private DropEntry(long timestamp, List<ItemStack> stacks) {
            this.timestamp = timestamp;
            this.stacks = new ArrayList<>(stacks);
        }

        private long timestamp() {
            return timestamp;
        }

        private List<ItemStack> stacks() {
            return stacks;
        }
    }

    private static final class PlayerGrave {
        static final Codec<PlayerGrave> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                UUID_CODEC.fieldOf("id").forGetter(PlayerGrave::id),
                UUID_CODEC.fieldOf("player").forGetter(PlayerGrave::playerId),
                Codec.LONG.fieldOf("created_at").forGetter(PlayerGrave::createdAt),
                Codec.BOOL.optionalFieldOf("claimed", false).forGetter(PlayerGrave::claimed),
                Codec.list(ItemStack.CODEC).optionalFieldOf("stacks", List.of()).forGetter(PlayerGrave::stacks)
        ).apply(instance, PlayerGrave::new));

        private final UUID id;
        private final UUID playerId;
        private final long createdAt;
        private boolean claimed;
        private final List<ItemStack> stacks;

        private PlayerGrave(UUID id, UUID playerId, long createdAt, boolean claimed, List<ItemStack> stacks) {
            this.id = id;
            this.playerId = playerId;
            this.createdAt = createdAt;
            this.claimed = claimed;
            this.stacks = new ArrayList<>(stacks);
        }

        private UUID id() {
            return id;
        }

        private UUID playerId() {
            return playerId;
        }

        private long createdAt() {
            return createdAt;
        }

        private boolean claimed() {
            return claimed;
        }

        private List<ItemStack> stacks() {
            return stacks;
        }

        private int itemCount() {
            return stacks.stream().mapToInt(ItemStack::getCount).sum();
        }
    }

    public record GraveSummary(UUID id, long createdAt, int itemCount) {
    }

    public record GraveClaim(ClaimStatus status, List<ItemStack> stacks, int itemCount) {
        public GraveClaim {
            stacks = List.copyOf(stacks);
        }

        public static GraveClaim noGrave() {
            return new GraveClaim(ClaimStatus.NO_GRAVE, List.of(), 0);
        }

        public static GraveClaim tooLarge(int itemCount) {
            return new GraveClaim(ClaimStatus.TOO_LARGE, List.of(), itemCount);
        }
    }

    public enum ClaimStatus {
        CLAIMED,
        NO_GRAVE,
        TOO_LARGE
    }

    public static final Codec<ItemDropLogHandler> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(DropEntry.CODEC).optionalFieldOf("drops", List.of()).forGetter(ItemDropLogHandler::legacyDrops),
            Codec.list(PlayerGrave.CODEC).optionalFieldOf("player_graves", List.of()).forGetter(ItemDropLogHandler::playerGraves)
    ).apply(instance, ItemDropLogHandler::new));

    private static final SavedDataType<ItemDropLogHandler> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(VersePlus.MOD_ID, "drop_graveyard"),
            ItemDropLogHandler::new,
            CODEC,
            null
    );

    private final List<DropEntry> drops;
    private final List<PlayerGrave> graves;

    public ItemDropLogHandler() {
        this(List.of(), List.of());
    }

    private ItemDropLogHandler(List<DropEntry> drops, List<PlayerGrave> graves) {
        this.drops = new ArrayList<>(drops);
        this.graves = new ArrayList<>(graves);
    }

    public static ItemDropLogHandler get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public void addItem(ItemStack stack) {
        long seconds = System.currentTimeMillis() / 1000;
        Map.Entry<DropEntry, Boolean> entry = findOrCreateEntry(seconds);
        entry.getKey().stacks.add(stack.copy());
        if (!entry.getValue()) {
            drops.add(entry.getKey());
        }
        setDirty();
    }

    public void addGraveItem(UUID graveId, UUID playerId, long createdAt, ItemStack stack) {
        PlayerGrave grave = graves.stream()
                .filter(candidate -> candidate.id.equals(graveId))
                .findFirst()
                .orElseGet(() -> {
                    PlayerGrave created = new PlayerGrave(graveId, playerId, createdAt, false, List.of());
                    graves.add(created);
                    return created;
                });
        if (!grave.claimed && grave.playerId.equals(playerId) && !stack.isEmpty()) {
            grave.stacks.add(stack.copy());
            setDirty();
        }
    }

    public Optional<GraveSummary> latestRecoverableGrave(UUID playerId) {
        return latestGrave(playerId).map(grave -> new GraveSummary(grave.id, grave.createdAt, grave.itemCount()));
    }

    public GraveClaim claimLatestGrave(UUID playerId, int availableItemBudget) {
        Optional<PlayerGrave> candidate = latestGrave(playerId);
        if (candidate.isEmpty()) {
            return GraveClaim.noGrave();
        }

        PlayerGrave grave = candidate.get();
        int itemCount = grave.itemCount();
        if (itemCount > availableItemBudget) {
            return GraveClaim.tooLarge(itemCount);
        }

        grave.claimed = true;
        setDirty();
        return new GraveClaim(
                ClaimStatus.CLAIMED,
                grave.stacks.stream().map(ItemStack::copy).toList(),
                itemCount
        );
    }

    private Optional<PlayerGrave> latestGrave(UUID playerId) {
        return graves.stream()
                .filter(grave -> grave.playerId.equals(playerId) && !grave.claimed && !grave.stacks.isEmpty())
                .max(Comparator.comparingLong(PlayerGrave::createdAt));
    }

    private Map.Entry<DropEntry, Boolean> findOrCreateEntry(long seconds) {
        long interval = seconds - seconds % LOG_SLICE_TIME;
        for (DropEntry entry : drops) {
            if (entry.timestamp == interval) {
                return new AbstractMap.SimpleImmutableEntry<>(entry, true);
            }
        }
        return new AbstractMap.SimpleImmutableEntry<>(new DropEntry(interval, new ArrayList<>()), false);
    }

    private List<DropEntry> legacyDrops() {
        return Collections.unmodifiableList(drops);
    }

    private List<PlayerGrave> playerGraves() {
        return Collections.unmodifiableList(graves);
    }
}
