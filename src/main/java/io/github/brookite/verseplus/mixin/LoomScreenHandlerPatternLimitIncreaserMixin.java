package io.github.brookite.verseplus.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.inventory.LoomMenu;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static io.github.brookite.verseplus.VersePlusLimits.MAX_LOOM_PATTERNS;

@Mixin(LoomMenu.class)
public class LoomScreenHandlerPatternLimitIncreaserMixin {
    @ModifyVariable(method = "slotsChanged(Lnet/minecraft/world/Container;)V",
            at = @At(value="STORE"), ordinal = 1)
    protected boolean setBl2(boolean original, @Local BannerPatternLayers bannerPatternsComponent) {
        return bannerPatternsComponent.layers().size() > MAX_LOOM_PATTERNS;
    }
}
