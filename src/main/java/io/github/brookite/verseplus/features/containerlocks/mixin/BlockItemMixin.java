package io.github.brookite.verseplus.features.containerlocks.mixin;

import io.github.brookite.verseplus.features.containerlocks.ButtonLockSavedData;
import io.github.brookite.verseplus.features.containerlocks.ContainerLockComponents;
import io.github.brookite.verseplus.features.containerlocks.LockData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {
    @Inject(method = "placeBlock", at = @At("RETURN"))
    private void transferButtonLock(
            BlockPlaceContext context,
            BlockState state,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!cir.getReturnValue()
                || !(state.getBlock() instanceof ButtonBlock)
                || !(context.getLevel() instanceof ServerLevel level)) {
            return;
        }

        ButtonLockSavedData savedData = ButtonLockSavedData.get(level);
        LockData lock = context.getItemInHand().get(ContainerLockComponents.LOCK_DATA);
        if (lock == null) {
            savedData.removeLock(context.getClickedPos());
        } else {
            savedData.setLock(context.getClickedPos(), lock);
        }
    }
}
