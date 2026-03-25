package io.github.brookite.verseplus.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.screens.inventory.LoomScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
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
    private boolean hasMaxPatterns;

    @Inject(
            method = "containerChanged()V",
            at = @At(value = "FIELD",
                    target = "Lnet/minecraft/client/gui/screens/inventory/LoomScreen;hasMaxPatterns:Z",
                    opcode = Opcodes.PUTFIELD,
                    shift = At.Shift.AFTER)
    )
    private void onInventoryChanged(CallbackInfo ci, @Local(ordinal = 1) ItemStack itemStack2) {
        BannerPatternLayers bannerPatternsComponent = itemStack2.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
        this.hasMaxPatterns = bannerPatternsComponent.layers().size() > MAX_LOOM_PATTERNS;
    }
}
