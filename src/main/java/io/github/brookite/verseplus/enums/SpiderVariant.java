package io.github.brookite.verseplus.enums;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

import java.util.function.IntFunction;

public enum SpiderVariant implements StringRepresentable {
    TINY("tiny", (byte) 0, 0.3F),
    SMALL("small", (byte) 1, 0.5F),
    MEDIUM("medium", (byte) 2, 0.7F),
    LARGE("large", (byte) 3, 1.0F);

    public static final SpiderVariant DEFAULT = LARGE;
    public static final StringRepresentable.EnumCodec<SpiderVariant> CODEC = StringRepresentable.fromEnum(SpiderVariant::values);
    static final IntFunction<SpiderVariant> FROM_INDEX = ByIdMap.continuous(
            SpiderVariant::getIndex, values(), ByIdMap.OutOfBoundsStrategy.CLAMP
    );
    public static final StreamCodec<ByteBuf, SpiderVariant> PACKET_CODEC = ByteBufCodecs.idMapper(FROM_INDEX, SpiderVariant::getIndex);
    private final String id;
    final byte index;
    final float scale;

    SpiderVariant(final String id, final byte index, final float scale) {
        this.id = id;
        this.index = index;
        this.scale = scale;
    }

    public float getScale() {
        return this.scale;
    }

    @Override
    public String getSerializedName() {
        return this.id;
    }

    public byte getIndexAsByte() {
        return this.index;
    }

    public int getIndex() {
        return this.index;
    }
}