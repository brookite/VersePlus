package io.github.brookite.verseplus.features.obsidianboat.mixin;

import io.github.brookite.verseplus.features.obsidianboat.ObsidianBoatEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.InsideBlockEffectType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.LavaFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LavaFluid.class)
public abstract class LavaFluidPassengerProtectionMixin {
    @Inject(method = "entityInside", at = @At("HEAD"), cancellable = true)
    private void verseplus$protectObsidianBoatPassengers(
            Level level,
            BlockPos pos,
            Entity entity,
            InsideBlockEffectApplier effectApplier,
            CallbackInfo callback
    ) {
        if (entity.getVehicle() instanceof ObsidianBoatEntity) {
            effectApplier.apply(InsideBlockEffectType.CLEAR_FREEZE);
            callback.cancel();
        }
    }
}
