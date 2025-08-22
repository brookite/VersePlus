package io.github.brookite.mixin;

import net.minecraft.entity.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.github.brookite.VersePlusLimits.PLAYER_DROP_LIFETIME_MINUTES;

@Mixin(ItemEntity.class)
public class PlayerDropLifetimeIncreaserMixin {
    @Shadow
    private int itemAge;

    @Inject(at = @At("TAIL"), method = "setCovetedItem()V")
	private void init(CallbackInfo info) {
		this.itemAge = -(1200 * PLAYER_DROP_LIFETIME_MINUTES - 6000); // 25 minutes (from -24K to 0 and to 6K);
	}
}