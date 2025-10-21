package io.github.brookite.verseplus;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemDropLogHandler extends PersistentState {
    private static final int LOG_SLICE_TIME = 5 * 60;

    private static class DropEntry {
        private final long timestamp;
        private final List<ItemStack> stacks;

        DropEntry(long timestamp, List<ItemStack> stacks) {
            this.timestamp = timestamp;
            this.stacks = new ArrayList<>(stacks);
        }

        public List<ItemStack> getStacks() {
            return stacks;
        }

        public long getTimestamp() {
            return timestamp;
        }

        static final Codec<DropEntry> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.LONG.fieldOf("timestamp").forGetter(DropEntry::getTimestamp),
                        Codec.list(ItemStack.CODEC).fieldOf("stacks").forGetter(DropEntry::getStacks)
                ).apply(instance, DropEntry::new)
        );
    }

    List<DropEntry> drops;

    public void addEntry(DropEntry entry) {
        drops.add(entry);
    }

    private void addItem(DropEntry entry, ItemStack stack) {
        entry.stacks.add(stack);
        markDirty();
    }

    public void addItem(ItemStack stack) {
        long seconds = System.currentTimeMillis() / 1000;
        Pair<DropEntry, Boolean> entry = findOrCreateEntry(seconds);
        addItem(entry.getLeft(), stack);
        if (!entry.getRight()) {
            addEntry(entry.getLeft());
        }
    }

    public Pair<DropEntry, Boolean> findOrCreateEntry(long seconds) {
        for (DropEntry entry : drops) {
            if (entry.timestamp == (seconds - seconds % LOG_SLICE_TIME)) {
                return new Pair<>(entry, true);
            }
        }
        long timestamp = System.currentTimeMillis() / 1000;
        long interval = timestamp - timestamp % LOG_SLICE_TIME;
        return new Pair<>(new DropEntry(interval, new ArrayList<>()), false);
    }

    public List<DropEntry> getDrops() {
        return Collections.unmodifiableList(drops);
    }

    public ItemDropLogHandler(List<DropEntry> drops) {
        if (drops == null) {
            this.drops = new ArrayList<>();
        } else {
            this.drops = new ArrayList<>(drops);
        }
    }

    public ItemDropLogHandler() {
        this.drops = new ArrayList<>();
    }

    public static ItemDropLogHandler get(ServerWorld serverWorld) {
        ItemDropLogHandler state = serverWorld.getPersistentStateManager().getOrCreate(TYPE);
        return state;
    }

    public static final Codec<ItemDropLogHandler> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.list(DropEntry.CODEC).fieldOf("drops").forGetter(ItemDropLogHandler::getDrops)
            ).apply(instance, ItemDropLogHandler::new)
    );

    private static final PersistentStateType<ItemDropLogHandler> TYPE =
            new PersistentStateType<>("drop_graveyard", ItemDropLogHandler::new, CODEC, null);


}
