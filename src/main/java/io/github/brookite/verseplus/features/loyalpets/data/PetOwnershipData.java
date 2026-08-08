package io.github.brookite.verseplus.features.loyalpets.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.RandomSource;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record PetOwnershipData(
        List<UUID> additionalOwners,
        int requiredFeedings,
        Optional<UUID> bondingCandidate,
        int bondingProgress
) {
    public static final int MAX_ADDITIONAL_OWNERS = 5;
    private static final int MIN_REQUIRED_FEEDINGS = 2;
    private static final int MAX_REQUIRED_FEEDINGS = 5;

    public static final Codec<PetOwnershipData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(UUIDUtil.CODEC).optionalFieldOf("additional_owners", List.of())
                    .forGetter(PetOwnershipData::additionalOwners),
            Codec.intRange(MIN_REQUIRED_FEEDINGS, MAX_REQUIRED_FEEDINGS)
                    .fieldOf("required_feedings").forGetter(PetOwnershipData::requiredFeedings),
            UUIDUtil.CODEC.optionalFieldOf("bonding_candidate").forGetter(PetOwnershipData::bondingCandidate),
            Codec.intRange(0, MAX_REQUIRED_FEEDINGS).optionalFieldOf("bonding_progress", 0)
                    .forGetter(PetOwnershipData::bondingProgress)
    ).apply(instance, PetOwnershipData::new));

    public PetOwnershipData {
        additionalOwners = List.copyOf(additionalOwners);
    }

    public static PetOwnershipData create(RandomSource random) {
        return new PetOwnershipData(
                List.of(),
                random.nextIntBetweenInclusive(MIN_REQUIRED_FEEDINGS, MAX_REQUIRED_FEEDINGS),
                Optional.empty(),
                0
        );
    }

    public boolean isAdditionalOwner(UUID playerId) {
        return additionalOwners.contains(playerId);
    }

    public boolean hasOwnerCapacity() {
        return additionalOwners.size() < MAX_ADDITIONAL_OWNERS;
    }

    public boolean canAdvanceBonding(UUID playerId) {
        return bondingCandidate.isEmpty() || bondingCandidate.get().equals(playerId);
    }

    public BondingAdvance advanceBonding(UUID playerId) {
        int nextProgress = bondingProgress + 1;
        if (nextProgress < requiredFeedings) {
            return new BondingAdvance(
                    new PetOwnershipData(additionalOwners, requiredFeedings, Optional.of(playerId), nextProgress),
                    false
            );
        }

        var owners = new java.util.ArrayList<>(additionalOwners);
        owners.add(playerId);
        return new BondingAdvance(
                new PetOwnershipData(owners, requiredFeedings, Optional.empty(), 0),
                true
        );
    }

    public record BondingAdvance(PetOwnershipData data, boolean completed) {
    }
}
