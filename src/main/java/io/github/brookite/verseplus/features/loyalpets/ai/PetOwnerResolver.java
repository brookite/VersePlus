package io.github.brookite.verseplus.features.loyalpets.ai;

import io.github.brookite.verseplus.features.loyalpets.data.LoyalPetAttachments;
import io.github.brookite.verseplus.features.loyalpets.data.PetOwnershipData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import org.jspecify.annotations.Nullable;

public final class PetOwnerResolver {
    private PetOwnerResolver() {
    }

    public static @Nullable LivingEntity resolveFollowTarget(TamableAnimal pet) {
        if (!(pet.level() instanceof ServerLevel level)) {
            return pet.getOwner();
        }

        LivingEntity primaryOwner = pet.getOwner();
        if (isAvailableInLevel(primaryOwner, level)) {
            return primaryOwner;
        }

        PetOwnershipData ownership = pet.getAttached(LoyalPetAttachments.PET_OWNERSHIP);
        if (ownership == null) {
            return null;
        }

        ServerPlayer nearest = null;
        double nearestDistance = Double.MAX_VALUE;
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
        return nearest;
    }

    private static boolean isAvailableInLevel(@Nullable LivingEntity owner, ServerLevel level) {
        return owner != null
                && owner.level() == level
                && owner.isAlive()
                && !owner.isSpectator();
    }
}
