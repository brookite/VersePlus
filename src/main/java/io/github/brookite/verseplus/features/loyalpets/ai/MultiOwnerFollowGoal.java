package io.github.brookite.verseplus.features.loyalpets.ai;

import io.github.brookite.verseplus.features.loyalpets.recall.PetTeleportation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.PathType;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;

public final class MultiOwnerFollowGoal extends Goal {
    private final TamableAnimal pet;
    private final double speedModifier;
    private final PathNavigation navigation;
    private final float startDistance;
    private final float stopDistance;
    private @Nullable LivingEntity owner;
    private int timeToRecalculatePath;
    private float oldWaterCost;

    public MultiOwnerFollowGoal(
            TamableAnimal pet,
            double speedModifier,
            float startDistance,
            float stopDistance
    ) {
        this.pet = pet;
        this.speedModifier = speedModifier;
        this.navigation = pet.getNavigation();
        this.startDistance = startDistance;
        this.stopDistance = stopDistance;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));

        if (!(navigation instanceof GroundPathNavigation) && !(navigation instanceof FlyingPathNavigation)) {
            throw new IllegalArgumentException("Unsupported mob type for MultiOwnerFollowGoal");
        }
    }

    @Override
    public boolean canUse() {
        LivingEntity resolvedOwner = PetOwnerResolver.resolveFollowTarget(pet);
        if (resolvedOwner == null
                || unableToMove(resolvedOwner)
                || pet.distanceToSqr(resolvedOwner) < startDistance * startDistance) {
            return false;
        }

        owner = resolvedOwner;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return owner != null
                && owner == PetOwnerResolver.resolveFollowTarget(pet)
                && !navigation.isDone()
                && !unableToMove(owner)
                && pet.distanceToSqr(owner) > stopDistance * stopDistance;
    }

    @Override
    public void start() {
        timeToRecalculatePath = 0;
        oldWaterCost = pet.getPathfindingMalus(PathType.WATER);
        pet.setPathfindingMalus(PathType.WATER, 0.0F);
    }

    @Override
    public void stop() {
        owner = null;
        navigation.stop();
        pet.setPathfindingMalus(PathType.WATER, oldWaterCost);
    }

    @Override
    public void tick() {
        if (owner == null) {
            return;
        }

        boolean shouldTeleport = pet.distanceToSqr(owner) >= TamableAnimal.TELEPORT_WHEN_DISTANCE_IS_SQ;
        if (!shouldTeleport) {
            pet.getLookControl().setLookAt(owner, 10.0F, pet.getMaxHeadXRot());
        }

        if (--timeToRecalculatePath <= 0) {
            timeToRecalculatePath = adjustedTickDelay(10);
            if (shouldTeleport) {
                PetTeleportation.teleportNear(pet, owner);
            } else {
                navigation.moveTo(owner, speedModifier);
            }
        }
    }

    private boolean unableToMove(LivingEntity targetOwner) {
        return pet.isOrderedToSit()
                || pet.isPassenger()
                || pet.mayBeLeashed()
                || targetOwner.isSpectator();
    }
}
