package io.github.brookite.verseplus.mixin;

import io.github.brookite.verseplus.ItemDropLogHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class PlayerDropLoggerMixin {
    @Shadow
    private World world;

    @Inject(at = @At("HEAD"), method = "discard()V")
    public void discard(CallbackInfo ci) {
        if (((Object)this) instanceof ItemEntity itemEntity
                && (itemEntity.getOwner() instanceof PlayerEntity ||
                    itemEntity.getFireTicks() > 0 ||
                    itemEntity.health < 0)
                && world instanceof ServerWorld serverWorld) {
            var logger = ItemDropLogHandler.get(serverWorld);
            ItemStack stack = itemEntity.getStack();
            if (stack != null && !stack.isEmpty()) {
                logger.addItem(stack);
            }
        }
    }
}
