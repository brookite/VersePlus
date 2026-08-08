package io.github.brookite.verseplus.features.loyalpets.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.UUID;

public record TrainedWolfRecord(
        UUID wolfId,
        ResourceKey<Level> dimension,
        ChunkPos chunk,
        UUID primaryOwner,
        List<UUID> additionalOwners,
        Identifier learnedSound,
        boolean sitting
) {
    public static final Codec<TrainedWolfRecord> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("wolf_id").forGetter(TrainedWolfRecord::wolfId),
            Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(TrainedWolfRecord::dimension),
            ChunkPos.CODEC.fieldOf("chunk").forGetter(TrainedWolfRecord::chunk),
            UUIDUtil.CODEC.fieldOf("primary_owner").forGetter(TrainedWolfRecord::primaryOwner),
            Codec.list(UUIDUtil.CODEC).optionalFieldOf("additional_owners", List.of())
                    .forGetter(TrainedWolfRecord::additionalOwners),
            Identifier.CODEC.fieldOf("learned_sound").forGetter(TrainedWolfRecord::learnedSound),
            Codec.BOOL.optionalFieldOf("sitting", false).forGetter(TrainedWolfRecord::sitting)
    ).apply(instance, TrainedWolfRecord::new));

    public TrainedWolfRecord {
        additionalOwners = List.copyOf(additionalOwners);
    }

    public boolean belongsTo(UUID playerId) {
        return primaryOwner.equals(playerId) || additionalOwners.contains(playerId);
    }
}
