package io.github.brookite.mixin;

import net.minecraft.entity.ItemEntity;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class PlayerDropLifetimeIncreaserMixin {
    @Shadow
    private int itemAge;

    @Inject(at = @At("TAIL"), method = "setCovetedItem()V")
	private void init(CallbackInfo info) {
		this.itemAge = -24000; // 25 minutes (from -24K to 0 and to 6K);
	}
}