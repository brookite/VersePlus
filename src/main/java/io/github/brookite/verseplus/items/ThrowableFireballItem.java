package io.github.brookite.verseplus.items;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ProjectileItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Optional;

public class ThrowableFireballItem extends Item implements ProjectileItem {
    private PlayerEntity owner;

    private static final double DEFAULT_POWER = 0.1;
    private static final int DEFAULT_EXPLOSION_POWER = 2;

    public ThrowableFireballItem(Item.Settings settings) {
        super(settings);
    }

    private Optional<NbtCompound> getCustomParameters(ItemStack stack) {
        var data = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (data != null) {
            NbtCompound value = data.copyNbt();
            return Optional.of(value);
        }
        return Optional.empty();
    }

    @Override
    public ProjectileEntity createEntity(World world, Position pos, ItemStack stack, Direction direction) {
        Optional<NbtCompound> params = getCustomParameters(stack);

        FireballEntity fireballEntity = new FireballEntity(EntityType.FIREBALL, world);
        fireballEntity.setPosition(pos.getX(), pos.getY(), pos.getZ());
        fireballEntity.setVelocity(direction.getDoubleVector());
        fireballEntity.setItem(stack);
        fireballEntity.accelerationPower = params.isPresent() ? params.get().getDouble("power", DEFAULT_POWER) : DEFAULT_POWER;
        fireballEntity.explosionPower = params.isPresent() ? params.get().getInt("explosionPower", DEFAULT_EXPLOSION_POWER) : DEFAULT_EXPLOSION_POWER;
        return fireballEntity;
    }

    @Override
    public ProjectileItem.Settings getProjectileSettings() {
        return ProjectileItem.Settings.builder().overrideDispenseEvent(1018).build();
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        Optional<NbtCompound> params = getCustomParameters(itemStack);
        double power = params.isPresent() ? params.get().getDouble("power", DEFAULT_POWER) : DEFAULT_POWER;
        int explosionPower =  params.isPresent() ? params.get().getInt("explosionPower", DEFAULT_EXPLOSION_POWER) : DEFAULT_EXPLOSION_POWER;

        Vec3d look = user.getRotationVec(1.0F);
        double spawnDistance = 1.5;
        double x = user.getX() + look.x * spawnDistance;
        double y = user.getEyeY() + look.y * spawnDistance;
        double z = user.getZ() + look.z * spawnDistance;

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
            FireballEntity entity = new FireballEntity(world, user, look, explosionPower);
            entity.setPosition(x, y, z);
            entity.accelerationPower = Math.max(power, 0.05);;
            ProjectileEntity.spawn(entity, serverWorld, itemStack);
        }
        user.incrementStat(Stats.USED.getOrCreateStat(this));
        itemStack.decrementUnlessCreative(1, user);
        return ActionResult.SUCCESS;
    }
}
