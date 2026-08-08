package io.github.brookite.verseplus.features.wardenstaff;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class WardenStaffItem extends Item {
    private static final int USE_DURATION = 72_000;
    private static final int CHARGE_TICKS = 34;
    private static final double RANGE = 15.0;
    private static final float DAMAGE = 10.0F;
    private static final double KNOCKBACK_VERTICAL = 0.5;
    private static final double KNOCKBACK_HORIZONTAL = 2.5;

    public WardenStaffItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }

        player.startUsingItem(hand);
        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.WARDEN_SONIC_CHARGE,
                SoundSource.PLAYERS,
                3.0F,
                1.0F
        );
        return InteractionResult.CONSUME;
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (USE_DURATION - remainingUseDuration >= CHARGE_TICKS) {
            livingEntity.releaseUsingItem();
        }
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int remainingUseDuration) {
        int elapsedTicks = getUseDuration(stack, livingEntity) - remainingUseDuration;
        if (elapsedTicks < CHARGE_TICKS || !(livingEntity instanceof Player player)) {
            return false;
        }

        if (level instanceof ServerLevel serverLevel) {
            fire(serverLevel, player);
            player.awardStat(Stats.ITEM_USED.get(this));
            if (!player.hasInfiniteMaterials()) {
                stack.hurtAndBreak(1, player, player.getUsedItemHand());
            }
        }
        return true;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity livingEntity) {
        return USE_DURATION;
    }

    private static void fire(ServerLevel level, Player player) {
        Vec3 origin = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F).normalize();
        Vec3 maximumEnd = origin.add(look.scale(RANGE));
        AABB searchBox = player.getBoundingBox().expandTowards(look.scale(RANGE)).inflate(1.0);
        EntityHitResult hitResult = ProjectileUtil.getEntityHitResult(
                player,
                origin,
                maximumEnd,
                searchBox,
                entity -> isValidTarget(player, entity),
                RANGE * RANGE
        );

        LivingEntity target = hitResult != null && hitResult.getEntity() instanceof LivingEntity livingTarget
                ? livingTarget
                : null;
        Vec3 beamEnd = target != null ? target.getEyePosition() : maximumEnd;
        Vec3 beam = beamEnd.subtract(origin);
        Vec3 direction = beam.normalize();

        int particleCount = Math.max(1, (int) Math.floor(beam.length()) + 1);
        for (int step = 1; step <= particleCount; step++) {
            Vec3 particlePosition = origin.add(direction.scale(Math.min(step, beam.length())));
            level.sendParticles(
                    ParticleTypes.SONIC_BOOM,
                    particlePosition.x(),
                    particlePosition.y(),
                    particlePosition.z(),
                    1,
                    0.0,
                    0.0,
                    0.0,
                    0.0
            );
        }

        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.WARDEN_SONIC_BOOM,
                SoundSource.PLAYERS,
                3.0F,
                1.0F
        );

        if (target != null && target.hurtServer(level, level.damageSources().sonicBoom(player), DAMAGE)) {
            double resistance = 1.0 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
            target.push(
                    direction.x() * KNOCKBACK_HORIZONTAL * resistance,
                    direction.y() * KNOCKBACK_VERTICAL * resistance,
                    direction.z() * KNOCKBACK_HORIZONTAL * resistance
            );
        }
    }

    private static boolean isValidTarget(Player player, Entity entity) {
        return entity != player
                && entity instanceof LivingEntity
                && entity.isAlive()
                && entity.isPickable();
    }
}
