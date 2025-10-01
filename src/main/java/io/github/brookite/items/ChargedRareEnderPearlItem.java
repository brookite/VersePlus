package io.github.brookite.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

public class ChargedRareEnderPearlItem extends EnderPearlItem {
    public ChargedRareEnderPearlItem(Settings settings) {
        super(settings);
    }

    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        if (user instanceof ServerPlayerEntity entity) {
            TeleportTarget.PostDimensionTransition transition = (e) -> {
                entity.sendMessage(Text.translatable("verseplus.rarepearl.transition"), false);
            };
            TeleportTarget target = entity.getRespawnTarget(false, transition);
            entity.teleportTo(target);
        }
        itemStack.decrementUnlessCreative(1, user);
        return ActionResult.SUCCESS;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }
}
