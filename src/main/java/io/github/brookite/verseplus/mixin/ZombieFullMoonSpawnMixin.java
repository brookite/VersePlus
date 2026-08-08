package io.github.brookite.verseplus.mixin;

import io.github.brookite.verseplus.VersePlusChances;
import net.minecraft.util.SpawnUtil;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Zombie.class)
public abstract class ZombieFullMoonSpawnMixin {
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
        if (zombie.getType() != EntityTypes.ZOMBIE
                || spawnReason != EntitySpawnReason.NATURAL
                || level.getDifficulty() != Difficulty.HARD
                || !level.isDarkOutside()
                || level.getMoonBrightness(zombie.blockPosition()) < 1.0F
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
}
