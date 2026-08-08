package io.github.brookite.verseplus.features.loyalpets.mixin;

import io.github.brookite.verseplus.VersePlusChances;
import io.github.brookite.verseplus.features.loyalpets.ai.FullMoonPlayerTargetGoal;
import io.github.brookite.verseplus.features.loyalpets.ai.MultiOwnerFollowGoal;
import net.minecraft.util.SpawnUtil;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Wolf.class)
public abstract class WolfMixin extends TamableAnimal {
    protected WolfMixin(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
    }

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void useMultiOwnerFollowGoal(CallbackInfo ci) {
        goalSelector.removeAllGoals(goal -> goal instanceof FollowOwnerGoal);
        goalSelector.addGoal(6, new MultiOwnerFollowGoal(this, 1.0, 10.0F, 2.0F));
        targetSelector.addGoal(4, new FullMoonPlayerTargetGoal((Wolf) (Object) this));
    }

    @Inject(method = "finalizeSpawn", at = @At("RETURN"))
    private void spawnExtraWolfDuringFullMoon(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            EntitySpawnReason spawnReason,
            SpawnGroupData spawnGroupData,
            CallbackInfoReturnable<SpawnGroupData> cir
    ) {
        Wolf wolf = (Wolf) (Object) this;
        if (spawnReason != EntitySpawnReason.NATURAL
                || !level.getLevel().isDarkOutside()
                || level.getLevel().getMoonBrightness(wolf.blockPosition()) < 1.0F
                || wolf.getRandom().nextFloat() >= VersePlusChances.FULL_MOON_EXTRA_WOLF_CHANCE) {
            return;
        }

        SpawnUtil.trySpawnMob(
                EntityTypes.WOLF,
                EntitySpawnReason.EVENT,
                level.getLevel(),
                wolf.blockPosition(),
                4,
                4,
                2,
                SpawnUtil.Strategy.ON_TOP_OF_COLLIDER_NO_LEAVES,
                true
        ).ifPresent(extraWolf -> extraWolf.finalizeSpawn(
                level,
                level.getCurrentDifficultyAt(extraWolf.blockPosition()),
                EntitySpawnReason.EVENT,
                cir.getReturnValue()
        ));
    }
}
