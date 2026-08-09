package io.github.brookite.verseplus.features.containerlocks.mixin;

import io.github.brookite.verseplus.features.containerlocks.ButtonLockSavedData;
import io.github.brookite.verseplus.features.containerlocks.LockData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ButtonBlock.class)
public class ButtonBlockMixin {
    @Inject(method = "press", at = @At("HEAD"), cancellable = true)
    private void requireMatchingButtonKey(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            CallbackInfo ci
    ) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        LockData lock = ButtonLockSavedData.get(serverLevel).getLock(pos);
        if (lock != null && lock.closed()) {
            ci.cancel();
        }
    }
}
