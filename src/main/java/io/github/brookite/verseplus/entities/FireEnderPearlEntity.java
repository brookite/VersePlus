package io.github.brookite.verseplus.entities;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class FireEnderPearlEntity extends ThrownEnderpearl {
    private boolean canBeDiscarded = false;
    private static final int EXPLODE_TIMEOUT = 20 * 5;
    private int explodeTimer = -1;
    private Vec3 lastCollisionPosition = null;

    public FireEnderPearlEntity(EntityType<? extends ThrownEnderpearl> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    protected void onHit(HitResult hitResult) {
        if (explodeTimer < 0) {
            super.onHit(hitResult);
            lastCollisionPosition = hitResult.getLocation();
        }
    }

    @Override
    public void discard() {
        if (canBeDiscarded) super.discard();
    }

    @Override
    public void tick() {
        super.tick();
        Level var2 = this.level();
        if (var2 instanceof ServerLevel serverWorld) {
            if (--explodeTimer == 0 && lastCollisionPosition != null) {
                this.level().explode(this, lastCollisionPosition.x(),
                        lastCollisionPosition.y(), lastCollisionPosition.z(),
                        (float)4, true, Level.ExplosionInteraction.MOB);
                canBeDiscarded = true;
                this.discard();
            }
        }
    }

    @Override
    public void playSound(Level world, Vec3 pos) {
        super.playSound(world, pos);
        explodeTimer = EXPLODE_TIMEOUT;
    }

    public FireEnderPearlEntity(Level world, LivingEntity owner, ItemStack stack) {
        super(world, owner, stack);
    }
}
