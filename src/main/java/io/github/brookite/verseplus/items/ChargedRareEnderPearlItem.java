package io.github.brookite.verseplus.items;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnderpearlItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ChargedRareEnderPearlItem extends EnderpearlItem {
    public ChargedRareEnderPearlItem(Properties settings) {
        super(settings);
    }

    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        if (user instanceof ServerPlayer entity) {
            var vec3d = Vec3.atBottomCenterOf(entity.blockPosition());
            List<Monster> list = entity.level().getEntitiesOfClass(Monster.class,
                    new AABB(vec3d.x() - (double)8.0F, vec3d.y() - (double)5.0F,
                            vec3d.z() - (double)8.0F,
                            vec3d.x() + (double)8.0F, vec3d.y() + (double)5.0F,
                            vec3d.z() + (double)8.0F),
                    (e) -> e.isPreventingPlayerRest(entity.level(), entity));

            if (!list.isEmpty() && !entity.isCreative()) {
                entity.sendSystemMessage(Component.translatable("verseplus.rarepearl.transition_failed"));
                return InteractionResult.FAIL;
            }

            TeleportTransition.PostTeleportTransition transition = (e) -> {
                entity.sendOverlayMessage(Component.translatable("verseplus.rarepearl.transition"));
            };
            TeleportTransition target = entity.findRespawnPositionAndUseSpawnBlock(false, transition);
            entity.teleport(target);
        }
        itemStack.consume(1, user);
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
