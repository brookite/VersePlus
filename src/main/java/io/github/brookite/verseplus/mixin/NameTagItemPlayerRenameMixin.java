package io.github.brookite.verseplus.mixin;

import io.github.brookite.verseplus.interfaces.RenamablePlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.NameTagItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NameTagItem.class)
public class NameTagItemPlayerRenameMixin {
    @Inject(method = "interactLivingEntity", at = @At("HEAD"), cancellable = true)
    private void renamePlayers(ItemStack stack, Player user, LivingEntity target, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        Component name = stack.get(DataComponents.CUSTOM_NAME);
        if (name == null || !(target instanceof ServerPlayer serverPlayer)) {
            return;
        }

        ((RenamablePlayer) serverPlayer).verseplus$setSavedName(name.getString());
        serverPlayer.setCustomName(Component.literal(name.getString()));
        stack.shrink(1);
        cir.setReturnValue(InteractionResult.SUCCESS);
    }
}
