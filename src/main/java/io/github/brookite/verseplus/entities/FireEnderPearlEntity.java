package io.github.brookite.verseplus.entities;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class FireEnderPearlEntity extends EnderPearlEntity {
    private boolean canBeDiscarded = false;
    private static final int EXPLODE_TIMEOUT = 20 * 5;
    private int explodeTimer = -1;
    private Vec3d lastCollisionPosition = null;

    public FireEnderPearlEntity(EntityType<? extends EnderPearlEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        if (explodeTimer < 0) {
            super.onCollision(hitResult);
            lastCollisionPosition = hitResult.getPos();
        }
    }

    @Override
    public void discard() {
        if (canBeDiscarded) super.discard();
    }

    @Override
    public void tick() {
        super.tick();
        World var2 = this.getEntityWorld();
        if (var2 instanceof ServerWorld serverWorld) {
            if (--explodeTimer == 0 && lastCollisionPosition != null) {
                this.getEntityWorld().createExplosion(this, lastCollisionPosition.getX(),
                        lastCollisionPosition.getY(), lastCollisionPosition.getZ(),
                        (float)4, true, World.ExplosionSourceType.MOB);
                canBeDiscarded = true;
                this.discard();
            }
        }
    }

    @Override
    public void playTeleportSound(World world, Vec3d pos) {
        super.playTeleportSound(world, pos);
        explodeTimer = EXPLODE_TIMEOUT;
    }

    public FireEnderPearlEntity(World world, LivingEntity owner, ItemStack stack) {
        super(world, owner, stack);
    }
}
