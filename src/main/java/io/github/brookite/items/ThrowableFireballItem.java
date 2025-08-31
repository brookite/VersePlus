package io.github.brookite.items;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ProjectileItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;

public class ThrowableFireballItem extends Item implements ProjectileItem {
    private PlayerEntity owner;
    private final float POWER = 1.8F;
    private final int EXPLOSION_POWER = 2;

    public ThrowableFireballItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public ProjectileEntity createEntity(World world, Position pos, ItemStack stack, Direction direction) {
        FireballEntity fireballEntity = new FireballEntity(EntityType.FIREBALL, world);
        fireballEntity.setPosition(pos.getX(), pos.getY(), pos.getZ());
        fireballEntity.setVelocity(direction.getDoubleVector());
        fireballEntity.setItem(stack);
        return fireballEntity;
    }

    @Override
    public ProjectileItem.Settings getProjectileSettings() {
        return ProjectileItem.Settings.builder().overrideDispenseEvent(1018).build();
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        world.playSound(
                null,
                user.getX(),
                user.getY(),
                user.getZ(),
                SoundEvents.ITEM_FIRECHARGE_USE,
                SoundCategory.NEUTRAL,
                0.5F,
                0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F)
        );
        if (world instanceof ServerWorld serverWorld) {
            FireballEntity entity = new FireballEntity(world, user, user.getRotationVector().multiply(POWER), EXPLOSION_POWER);
            ProjectileEntity.spawn(entity, serverWorld, itemStack);

        }
        user.incrementStat(Stats.USED.getOrCreateStat(this));
        itemStack.decrementUnlessCreative(1, user);
        return ActionResult.SUCCESS;
    }
}
