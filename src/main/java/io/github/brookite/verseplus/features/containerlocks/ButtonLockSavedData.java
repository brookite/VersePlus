package io.github.brookite.verseplus.features.containerlocks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.brookite.verseplus.VersePlus;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ButtonLockSavedData extends SavedData {
    private static final String ID = "button_locks";

    private record Entry(long position, LockData lock) {
        private static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.LONG.fieldOf("position").forGetter(Entry::position),
                LockData.CODEC.fieldOf("lock").forGetter(Entry::lock)
        ).apply(instance, Entry::new));
    }

    private static final Codec<ButtonLockSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Entry.CODEC.listOf().fieldOf("locks").forGetter(ButtonLockSavedData::entries)
    ).apply(instance, ButtonLockSavedData::new));

    private static final SavedDataType<ButtonLockSavedData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(VersePlus.MOD_ID, ID),
            ButtonLockSavedData::new,
            CODEC,
            DataFixTypes.SAVED_DATA_MAP_DATA
    );

    private final Map<Long, LockData> locks;

    public ButtonLockSavedData() {
        this.locks = new HashMap<>();
    }

    private ButtonLockSavedData(List<Entry> entries) {
        this();
        entries.forEach(entry -> locks.put(entry.position(), entry.lock().installed(entry.lock().closed())));
    }

    public static ButtonLockSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public LockData getLock(BlockPos pos) {
        return locks.get(pos.asLong());
    }

    public void setLock(BlockPos pos, LockData data) {
        LockData installed = data.installed(data.closed());
        if (!installed.equals(locks.put(pos.asLong(), installed))) {
            setDirty();
        }
    }

    public void removeLock(BlockPos pos) {
        if (locks.remove(pos.asLong()) != null) {
            setDirty();
        }
    }

    private List<Entry> entries() {
        return locks.entrySet().stream()
                .map(entry -> new Entry(entry.getKey(), entry.getValue()))
                .toList();
    }
}
