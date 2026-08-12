package io.github.brookite.verseplus.features.loyalpets.recall;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Set;

public final class PetTeleportation {
    private static final int ATTEMPTS = 10;

    private PetTeleportation() {
    }

    public static @Nullable TamableAnimal teleportNear(TamableAnimal pet, LivingEntity target) {
        if (!(target.level() instanceof ServerLevel targetLevel)) {
            return null;
        }

        BlockPos targetPos = target.blockPosition();
        for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
            int xOffset = pet.getRandom().nextIntBetweenInclusive(-3, 3);
            int zOffset = pet.getRandom().nextIntBetweenInclusive(-3, 3);
            if (Math.abs(xOffset) < 2 && Math.abs(zOffset) < 2) {
                continue;
            }

            int yOffset = pet.getRandom().nextIntBetweenInclusive(-1, 1);
            BlockPos destination = targetPos.offset(xOffset, yOffset, zOffset);
            TamableAnimal teleported = teleportTo(pet, targetLevel, destination);
            if (teleported != null) {
                return teleported;
            }
        }
        return null;
    }

    private static @Nullable TamableAnimal teleportTo(
            TamableAnimal pet,
            ServerLevel targetLevel,
            BlockPos destination
    ) {
        PathfindingContext context = new PathfindingContext(targetLevel, pet);
        if (WalkNodeEvaluator.getPathTypeStatic(context, destination.mutable()) != PathType.WALKABLE) {
            return null;
        }

        if (targetLevel.getBlockState(destination.below()).getBlock() instanceof LeavesBlock) {
            return null;
        }

        double x = destination.getX() + 0.5;
        double y = destination.getY();
        double z = destination.getZ() + 0.5;
        if (!targetLevel.noCollision(
                pet,
                pet.getBoundingBox().move(x - pet.getX(), y - pet.getY(), z - pet.getZ())
        )) {
            return null;
        }

        var transition = new TeleportTransition(
                targetLevel,
                new Vec3(x, y, z),
                Vec3.ZERO,
                pet.getYRot(),
                pet.getXRot(),
                Set.<Relative>of(),
                TeleportTransition.DO_NOTHING
        );
        if (pet.teleport(transition) instanceof TamableAnimal teleported) {
            teleported.getNavigation().stop();
            return teleported;
        }
        return null;
    }
}
