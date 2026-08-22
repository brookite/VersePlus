package io.github.brookite.verseplus.features.loyalpets.ai;

import io.github.brookite.verseplus.features.loyalpets.data.LoyalPetAttachments;
import io.github.brookite.verseplus.features.loyalpets.data.PetOwnershipData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import org.jspecify.annotations.Nullable;

public final class PetOwnerResolver {
    private static final double PRIMARY_OWNER_PREFERENCE_RADIUS_SQUARED = 12.0 * 12.0;

    private PetOwnerResolver() {
    }

    public static @Nullable LivingEntity resolveFollowTarget(TamableAnimal pet) {
        if (!(pet.level() instanceof ServerLevel level)) {
            return pet.getOwner();
        }

        LivingEntity primaryOwner = pet.getOwner();
        boolean isPrimaryOwnerAvailable = isAvailableInLevel(primaryOwner, level);
        if (isPrimaryOwnerAvailable
                && pet.distanceToSqr(primaryOwner) <= PRIMARY_OWNER_PREFERENCE_RADIUS_SQUARED) {
            return primaryOwner;
        }

        PetOwnershipData ownership = pet.getAttached(LoyalPetAttachments.PET_OWNERSHIP);
        LivingEntity nearest = isPrimaryOwnerAvailable ? primaryOwner : null;
        double nearestDistance = nearest == null ? Double.MAX_VALUE : pet.distanceToSqr(nearest);
        if (ownership != null) {
            for (var ownerId : ownership.additionalOwners()) {
                ServerPlayer candidate = level.getServer().getPlayerList().getPlayer(ownerId);
                if (!isAvailableInLevel(candidate, level)) {
                    continue;
                }

                double distance = pet.distanceToSqr(candidate);
                if (distance < nearestDistance) {
                    nearest = candidate;
                    nearestDistance = distance;
                }
            }
        }
        return nearest;
    }

    private static boolean isAvailableInLevel(@Nullable LivingEntity owner, ServerLevel level) {
        return owner != null
                && owner.level() == level
                && owner.isAlive()
                && !owner.isSpectator();
    }
}
