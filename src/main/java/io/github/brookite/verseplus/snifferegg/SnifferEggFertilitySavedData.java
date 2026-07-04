package io.github.brookite.verseplus.snifferegg;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import io.github.brookite.verseplus.VersePlus;

import java.util.Arrays;

public class SnifferEggFertilitySavedData extends SavedData {
    private static final String ID = "infertile_sniffer_eggs";

    private static final Codec<SnifferEggFertilitySavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG_STREAM.fieldOf("positions").forGetter(data -> Arrays.stream(data.infertileEggPositions.toLongArray()))
    ).apply(instance, stream -> {
        LongOpenHashSet positions = new LongOpenHashSet();
        stream.forEach(positions::add);
        return new SnifferEggFertilitySavedData(positions);
    }));

    private static final SavedDataType<SnifferEggFertilitySavedData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(VersePlus.MOD_ID, ID),
            SnifferEggFertilitySavedData::new,
            CODEC,
            DataFixTypes.SAVED_DATA_MAP_DATA
    );

    private final LongSet infertileEggPositions;

    public SnifferEggFertilitySavedData() {
        this(new LongOpenHashSet());
    }

    private SnifferEggFertilitySavedData(LongSet infertileEggPositions) {
        this.infertileEggPositions = infertileEggPositions;
    }

    public static SnifferEggFertilitySavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean isInfertile(BlockPos pos) {
        return infertileEggPositions.contains(pos.asLong());
    }

    public void setInfertile(BlockPos pos, boolean infertile) {
        boolean changed;
        if (infertile) {
            changed = infertileEggPositions.add(pos.asLong());
        } else {
            changed = infertileEggPositions.remove(pos.asLong());
        }

        if (changed) {
            setDirty();
        }
    }
}
