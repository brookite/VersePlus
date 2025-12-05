package io.github.brookite.verseplus.enums;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;

import java.util.function.IntFunction;

public enum SpiderVariant implements StringIdentifiable {
    TINY("tiny", (byte) 0, 0.3F),
    SMALL("small", (byte) 1, 0.5F),
    MEDIUM("medium", (byte) 2, 0.7F),
    LARGE("large", (byte) 3, 1.0F);

    public static final SpiderVariant DEFAULT = LARGE;
    public static final StringIdentifiable.EnumCodec<SpiderVariant> CODEC = StringIdentifiable.createCodec(SpiderVariant::values);
    static final IntFunction<SpiderVariant> FROM_INDEX = ValueLists.createIndexToValueFunction(
            SpiderVariant::getIndex, values(), ValueLists.OutOfBoundsHandling.CLAMP
    );
    public static final PacketCodec<ByteBuf, SpiderVariant> PACKET_CODEC = PacketCodecs.indexed(FROM_INDEX, SpiderVariant::getIndex);
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
    public String asString() {
        return this.id;
    }

    public byte getIndexAsByte() {
        return this.index;
    }

    public int getIndex() {
        return this.index;
    }
}