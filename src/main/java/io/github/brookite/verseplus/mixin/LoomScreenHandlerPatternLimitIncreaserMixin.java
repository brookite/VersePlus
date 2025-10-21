package io.github.brookite.verseplus.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.screen.LoomScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static io.github.brookite.verseplus.VersePlusLimits.MAX_LOOM_PATTERNS;

@Mixin(LoomScreenHandler.class)
public class LoomScreenHandlerPatternLimitIncreaserMixin {
    @ModifyVariable(method = "onContentChanged(Lnet/minecraft/inventory/Inventory;)V",
            at = @At(value="STORE"), ordinal = 1)
    protected boolean setBl2(boolean original, @Local BannerPatternsComponent bannerPatternsComponent) {
        return bannerPatternsComponent.layers().size() > MAX_LOOM_PATTERNS;
    }
}
