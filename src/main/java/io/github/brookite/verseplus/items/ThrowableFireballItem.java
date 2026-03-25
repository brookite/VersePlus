package io.github.brookite.verseplus.items;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class ThrowableFireballItem extends Item implements ProjectileItem {
    private Player owner;

    private static final double DEFAULT_POWER = 0.1;
    private static final int DEFAULT_EXPLOSION_POWER = 2;

    public ThrowableFireballItem(Item.Properties settings) {
        super(settings);
    }

    private Optional<CompoundTag> getCustomParameters(ItemStack stack) {
        var data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null) {
            CompoundTag value = data.copyTag();
            return Optional.of(value);
        }
        return Optional.empty();
    }

    @Override
    public Projectile asProjectile(Level world, Position pos, ItemStack stack, Direction direction) {
        Optional<CompoundTag> params = getCustomParameters(stack);

        LargeFireball fireballEntity = new LargeFireball(EntityType.FIREBALL, world);
        fireballEntity.setPos(pos.x(), pos.y(), pos.z());
        fireballEntity.setDeltaMovement(direction.getUnitVec3());
        fireballEntity.setItem(stack);
        fireballEntity.accelerationPower = params.isPresent() ? params.get().getDoubleOr("power", DEFAULT_POWER) : DEFAULT_POWER;
        fireballEntity.explosionPower = params.isPresent() ? params.get().getIntOr("explosionPower", DEFAULT_EXPLOSION_POWER) : DEFAULT_EXPLOSION_POWER;
        return fireballEntity;
    }

    @Override
    public ProjectileItem.DispenseConfig createDispenseConfig() {
        return ProjectileItem.DispenseConfig.builder().overrideDispenseEvent(1018).build();
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        Optional<CompoundTag> params = getCustomParameters(itemStack);
        double power = params.isPresent() ? params.get().getDoubleOr("power", DEFAULT_POWER) : DEFAULT_POWER;
        int explosionPower =  params.isPresent() ? params.get().getIntOr("explosionPower", DEFAULT_EXPLOSION_POWER) : DEFAULT_EXPLOSION_POWER;

        Vec3 look = user.getViewVector(1.0F);
        double spawnDistance = 1.5;
        double x = user.getX() + look.x * spawnDistance;
        double y = user.getEyeY() + look.y * spawnDistance;
        double z = user.getZ() + look.z * spawnDistance;

        world.playSound(
                null,
                user.getX(),
                user.getY(),
                user.getZ(),
                SoundEvents.FIRECHARGE_USE,
                SoundSource.NEUTRAL,
                0.5F,
                0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F)
        );
        if (world instanceof ServerLevel serverWorld) {
            LargeFireball entity = new LargeFireball(world, user, look, explosionPower);
            entity.setPos(x, y, z);
            entity.accelerationPower = Math.max(power, 0.05);;
            Projectile.spawnProjectile(entity, serverWorld, itemStack);
        }
        user.awardStat(Stats.ITEM_USED.get(this));
        itemStack.consume(1, user);
        return InteractionResult.SUCCESS;
    }
}
