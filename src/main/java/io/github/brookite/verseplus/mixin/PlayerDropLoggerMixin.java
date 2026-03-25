package io.github.brookite.verseplus.mixin;

import io.github.brookite.verseplus.ItemDropLogHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class PlayerDropLoggerMixin {
    @Shadow
    private Level level;

    @Inject(at = @At("HEAD"), method = "discard()V")
    public void discard(CallbackInfo ci) {
        if (((Object)this) instanceof ItemEntity itemEntity
                && (itemEntity.getOwner() instanceof Player ||
                    itemEntity.getRemainingFireTicks() > 0 ||
                    itemEntity.health < 0)
                && level instanceof ServerLevel serverWorld) {
            var logger = ItemDropLogHandler.get(serverWorld);
            ItemStack stack = itemEntity.getItem();
            if (stack != null && !stack.isEmpty()) {
                logger.addItem(stack);
            }
        }
    }
}
