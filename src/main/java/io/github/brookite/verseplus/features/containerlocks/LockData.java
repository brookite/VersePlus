package io.github.brookite.verseplus.features.containerlocks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;

import java.util.UUID;

public record LockData(LockMaterial material, UUID fingerprint, boolean closed, boolean containsKey) {
    public static final Codec<LockData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            LockMaterial.CODEC.fieldOf("material").forGetter(LockData::material),
            UUIDUtil.CODEC.fieldOf("fingerprint").forGetter(LockData::fingerprint),
            Codec.BOOL.optionalFieldOf("closed", false).forGetter(LockData::closed),
            Codec.BOOL.optionalFieldOf("contains_key", false).forGetter(LockData::containsKey)
    ).apply(instance, LockData::new));

    public LockData installed(boolean isClosed) {
        return new LockData(material, fingerprint, isClosed, false);
    }

    public LockData toggled() {
        return installed(!closed);
    }
}
