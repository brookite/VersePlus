package io.github.brookite.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.screen.LoomScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LoomScreenHandler.class)
public class LoomPatternLimitIncreaserMixin {
    @ModifyVariable(method = "onContentChanged(Lnet/minecraft/inventory/Inventory;)V", at = @At("STORE"), ordinal = 1)
    protected boolean setBl1(boolean original, @Local(ordinal = 0) BannerPatternsComponent bannerPatternsComponent) {
        return bannerPatternsComponent.layers().size() > 16;
    }
}
