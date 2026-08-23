package io.github.brookite.verseplus.mixin;

import io.github.brookite.verseplus.VersePlus;
import io.github.brookite.verseplus.VersePlusChances;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.SpawnUtil;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Zombie.class)
public abstract class ZombieFullMoonSpawnMixin {
    @Unique
    private static final Identifier FULL_MOON_FOLLOW_RANGE_BONUS_ID =
            Identifier.fromNamespaceAndPath(VersePlus.MOD_ID, "full_moon_follow_range_bonus");

    @Unique
    private static final AttributeModifier FULL_MOON_FOLLOW_RANGE_BONUS = new AttributeModifier(
            FULL_MOON_FOLLOW_RANGE_BONUS_ID,
            0.25,
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
    );

    @Inject(method = "tick", at = @At("HEAD"))
    private void updateFullMoonFollowRange(CallbackInfo ci) {
        Zombie zombie = (Zombie) (Object) this;
        if (!(zombie.level() instanceof ServerLevel level)) {
            return;
        }

        var followRange = zombie.getAttribute(Attributes.FOLLOW_RANGE);
        boolean shouldApplyBonus = isOrdinaryZombieDuringHardFullMoon(zombie, level);

        if (shouldApplyBonus) {
            if (!followRange.hasModifier(FULL_MOON_FOLLOW_RANGE_BONUS_ID)) {
                followRange.addTransientModifier(FULL_MOON_FOLLOW_RANGE_BONUS);
            }
        } else {
            followRange.removeModifier(FULL_MOON_FOLLOW_RANGE_BONUS_ID);
        }
    }

    @Inject(method = "finalizeSpawn", at = @At("RETURN"))
    private void enlargeNaturalGroupsDuringHardFullMoon(
            ServerLevelAccessor levelAccessor,
            DifficultyInstance difficulty,
            EntitySpawnReason spawnReason,
            SpawnGroupData spawnGroupData,
            CallbackInfoReturnable<SpawnGroupData> cir
    ) {
        Zombie zombie = (Zombie) (Object) this;
        var level = levelAccessor.getLevel();
        if (spawnReason != EntitySpawnReason.NATURAL
                || !isOrdinaryZombieDuringHardFullMoon(zombie, level)
                || zombie.getRandom().nextFloat() >= VersePlusChances.FULL_MOON_EXTRA_ZOMBIE_CHANCE) {
            return;
        }

        SpawnUtil.trySpawnMob(
                EntityTypes.ZOMBIE,
                EntitySpawnReason.EVENT,
                level,
                zombie.blockPosition(),
                4,
                4,
                2,
                SpawnUtil.Strategy.ON_TOP_OF_COLLIDER_NO_LEAVES,
                true
        ).ifPresent(extraZombie -> extraZombie.finalizeSpawn(
                levelAccessor,
                level.getCurrentDifficultyAt(extraZombie.blockPosition()),
                EntitySpawnReason.EVENT,
                cir.getReturnValue()
        ));
    }

    @Unique
    private static boolean isOrdinaryZombieDuringHardFullMoon(Zombie zombie, ServerLevel level) {
        return zombie.getType() == EntityTypes.ZOMBIE
                && level.getDifficulty() == Difficulty.HARD
                && level.isDarkOutside()
                && level.getMoonBrightness(zombie.blockPosition()) >= 1.0F;
    }
}
