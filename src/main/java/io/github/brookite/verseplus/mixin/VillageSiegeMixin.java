package io.github.brookite.verseplus.mixin;

import io.github.brookite.verseplus.VersePlusChances;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.ai.village.VillageSiege;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillageSiege.class)
public abstract class VillageSiegeMixin {
    @Shadow
    private int zombiesToSpawn;

    @Shadow
    private int spawnX;

    @Shadow
    private int spawnY;

    @Shadow
    private int spawnZ;

    @Inject(method = "tryToSetupSiege", at = @At("RETURN"))
    private void enlargeSiegeDuringHardFullMoon(
            ServerLevel level,
            CallbackInfoReturnable<Boolean> cir
    ) {
        BlockPos spawnPos = new BlockPos(spawnX, spawnY, spawnZ);
        if (zombiesToSpawn > 0
                && level.getDifficulty() == Difficulty.HARD
                && level.isDarkOutside()
                && level.getMoonBrightness(spawnPos) >= 1.0F) {
            zombiesToSpawn = VersePlusChances.FULL_MOON_SIEGE_ZOMBIE_COUNT;
        }
    }
}
