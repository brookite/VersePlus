package io.github.brookite.verseplus.features.obsidianboat.mixin;

import io.github.brookite.verseplus.features.obsidianboat.ObsidianBoatEntity;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractBoat.class)
public abstract class AbstractBoatLavaMixin {
    private static final float OBSIDIAN_BOAT_SPEED_MULTIPLIER = 0.6F;
    private static final float LOW_RESOURCE_SPEED_MULTIPLIER = 0.75F;

    @Redirect(
            method = {"getWaterLevelAbove", "checkInWater", "isUnderwater", "checkFallDamage"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"
            )
    )
    private boolean verseplus$obsidianBoatsTreatLavaAsWater(FluidState state, TagKey<Fluid> tag) {
        if ((Object)this instanceof ObsidianBoatEntity boat && boat.hasTravelResource() && tag == FluidTags.WATER) {
            return state.is(FluidTags.LAVA);
        }
        return state.is(tag);
    }

    @Redirect(
            method = "canAddPassenger",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/vehicle/boat/AbstractBoat;isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z"
            )
    )
    private boolean verseplus$checkObsidianBoatLavaEye(AbstractBoat boat, TagKey<Fluid> tag) {
        if (boat instanceof ObsidianBoatEntity obsidianBoat && obsidianBoat.hasTravelResource() && tag == FluidTags.WATER) {
            return boat.isEyeInFluid(FluidTags.LAVA);
        }
        return boat.isEyeInFluid(tag);
    }

    @ModifyConstant(method = "floatBoat", constant = @Constant(doubleValue = 0.101))
    private double verseplus$raiseObsidianBoatOnLava(double surfaceOffset) {
        return (Object)this instanceof ObsidianBoatEntity boat && boat.hasTravelResource() ? 0.55 : surfaceOffset;
    }

    @ModifyConstant(method = "controlBoat", constant = @Constant(floatValue = 0.04F))
    private float verseplus$slowObsidianBoat(float acceleration) {
        if ((Object)this instanceof ObsidianBoatEntity boat) {
            float resourceMultiplier = boat.hasLowTravelResource() ? LOW_RESOURCE_SPEED_MULTIPLIER : 1.0F;
            return acceleration * OBSIDIAN_BOAT_SPEED_MULTIPLIER * resourceMultiplier;
        }
        return acceleration;
    }
}
