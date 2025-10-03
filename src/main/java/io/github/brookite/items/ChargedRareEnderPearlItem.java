package io.github.brookite.items;

import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

import java.util.List;

public class ChargedRareEnderPearlItem extends EnderPearlItem {
    public ChargedRareEnderPearlItem(Settings settings) {
        super(settings);
    }

    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        if (user instanceof ServerPlayerEntity entity) {
            var vec3d = Vec3d.ofBottomCenter(entity.getBlockPos());
            List<HostileEntity> list = entity.getEntityWorld().getEntitiesByClass(HostileEntity.class,
                    new Box(vec3d.getX() - (double)8.0F, vec3d.getY() - (double)5.0F,
                            vec3d.getZ() - (double)8.0F,
                            vec3d.getX() + (double)8.0F, vec3d.getY() + (double)5.0F,
                            vec3d.getZ() + (double)8.0F),
                    (e) -> e.isAngryAt(entity.getEntityWorld(), entity));

            if (!list.isEmpty() && !entity.isCreative()) {
                entity.sendMessage(Text.translatable("verseplus.rarepearl.transition_failed"));
                return ActionResult.FAIL;
            }

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
