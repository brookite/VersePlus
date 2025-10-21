package io.github.brookite.verseplus.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.screen.ingame.LoomScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.github.brookite.verseplus.VersePlusLimits.MAX_LOOM_PATTERNS;

@Mixin(LoomScreen.class)
public class LoomScreenPatternLimitIncreaserMixin {
    @Shadow
    private boolean hasTooManyPatterns;

    @Inject(
            method = "onInventoryChanged()V",
            at = @At(value = "FIELD",
                    target = "Lnet/minecraft/client/gui/screen/ingame/LoomScreen;hasTooManyPatterns:Z",
                    opcode = Opcodes.PUTFIELD,
                    shift = At.Shift.AFTER)
    )
    private void onInventoryChanged(CallbackInfo ci, @Local(ordinal = 1) ItemStack itemStack2) {
        BannerPatternsComponent bannerPatternsComponent = itemStack2.getOrDefault(DataComponentTypes.BANNER_PATTERNS, BannerPatternsComponent.DEFAULT);
        this.hasTooManyPatterns = bannerPatternsComponent.layers().size() > MAX_LOOM_PATTERNS;
    }
}
