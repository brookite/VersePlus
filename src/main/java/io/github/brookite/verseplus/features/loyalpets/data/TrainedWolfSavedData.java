package io.github.brookite.verseplus.features.loyalpets.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.brookite.verseplus.VersePlus;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class TrainedWolfSavedData extends SavedData {
    private static final Codec<TrainedWolfSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(TrainedWolfRecord.CODEC).optionalFieldOf("wolves", List.of())
                    .forGetter(data -> List.copyOf(data.wolves.values()))
    ).apply(instance, TrainedWolfSavedData::new));

    private static final SavedDataType<TrainedWolfSavedData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(VersePlus.MOD_ID, "trained_wolves"),
            TrainedWolfSavedData::new,
            CODEC,
            DataFixTypes.SAVED_DATA_MAP_DATA
    );

    private final Map<UUID, TrainedWolfRecord> wolves = new LinkedHashMap<>();

    public TrainedWolfSavedData() {
    }

    private TrainedWolfSavedData(List<TrainedWolfRecord> records) {
        records.forEach(record -> wolves.put(record.wolfId(), record));
    }

    public static TrainedWolfSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public void put(TrainedWolfRecord record) {
        if (!record.equals(wolves.put(record.wolfId(), record))) {
            setDirty();
        }
    }

    public void remove(UUID wolfId) {
        if (wolves.remove(wolfId) != null) {
            setDirty();
        }
    }

    public Collection<TrainedWolfRecord> records() {
        return List.copyOf(wolves.values());
    }
}
