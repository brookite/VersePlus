package io.github.brookite.verseplus.features.containerlocks.mixin;

import io.github.brookite.verseplus.features.containerlocks.ContainerLockComponents;
import io.github.brookite.verseplus.features.containerlocks.ContainerLocks;
import io.github.brookite.verseplus.features.containerlocks.LockData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BaseContainerBlockEntity.class)
public abstract class BaseContainerBlockEntityMixin {
    @Inject(method = "canOpen", at = @At("RETURN"), cancellable = true)
    private void requireMatchingContainerKey(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() || player.isSpectator()) {
            return;
        }

        BaseContainerBlockEntity container = (BaseContainerBlockEntity) (Object) this;
        LockData lockData = container.components().get(ContainerLockComponents.LOCK_DATA);
        if (lockData != null && lockData.closed() && !ContainerLocks.matches(player.getMainHandItem(), lockData)) {
            cir.setReturnValue(false);
        }
    }
}
