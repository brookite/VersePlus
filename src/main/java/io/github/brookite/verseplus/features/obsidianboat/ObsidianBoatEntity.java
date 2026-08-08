package io.github.brookite.verseplus.features.obsidianboat;

import io.github.brookite.verseplus.registries.RegisterItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ObsidianBoatEntity extends Boat {
    public static final float MAX_TRAVEL_DISTANCE = 2500.0F;
    public static final float MAX_SINK_DEPTH = 0.20F;
    private static final float PASSENGER_CLEARANCE = 0.25F;
    private static final String TRAVEL_DISTANCE_KEY = "ObsidianBoatTravelDistance";
    private static final EntityDataAccessor<Float> TRAVEL_DISTANCE = SynchedEntityData.defineId(
            ObsidianBoatEntity.class,
            EntityDataSerializers.FLOAT
    );

    private double lastTrackedX;
    private double lastTrackedZ;
    private boolean hasTrackedPosition;

    public ObsidianBoatEntity(EntityType<? extends ObsidianBoatEntity> entityType, Level level) {
        super(entityType, level, () -> RegisterItems.OBSIDIAN_BOAT_ITEM);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(TRAVEL_DISTANCE, 0.0F);
    }

    @Override
    public void tick() {
        if (!level().isClientSide()) {
            trackLavaTravel();
        }

        super.tick();

        if (!level().isClientSide() && getTravelDistance() >= MAX_TRAVEL_DISTANCE) {
            ejectPassengers();
            discard();
        }
    }

    private void trackLavaTravel() {
        if (hasTrackedPosition && getFluidHeight(FluidTags.LAVA) > 0.0) {
            double dx = getX() - lastTrackedX;
            double dz = getZ() - lastTrackedZ;
            float distance = (float)Math.sqrt(dx * dx + dz * dz);
            if (distance > 0.0F) {
                setTravelDistance(Math.min(MAX_TRAVEL_DISTANCE, getTravelDistance() + distance));
            }
        }

        lastTrackedX = getX();
        lastTrackedZ = getZ();
        hasTrackedPosition = true;
    }

    public float getTravelDistance() {
        return entityData.get(TRAVEL_DISTANCE);
    }

    public void setTravelDistance(float distance) {
        entityData.set(TRAVEL_DISTANCE, Math.clamp(distance, 0.0F, MAX_TRAVEL_DISTANCE));
    }

    public float getSinkDepth() {
        return MAX_SINK_DEPTH * getTravelDistance() / MAX_TRAVEL_DISTANCE;
    }

    @Override
    protected double rideHeight(EntityDimensions dimensions) {
        return super.rideHeight(dimensions) + PASSENGER_CLEARANCE - getSinkDepth();
    }

    @Override
    protected double getDefaultGravity() {
        return getFluidHeight(FluidTags.LAVA) > 0.0 ? 0.0 : super.getDefaultGravity();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putFloat(TRAVEL_DISTANCE_KEY, getTravelDistance());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setTravelDistance(input.getFloatOr(TRAVEL_DISTANCE_KEY, 0.0F));
    }

    @Override
    protected void destroy(ServerLevel level, DamageSource source) {
        kill(level);
        if (level.getGameRules().get(GameRules.ENTITY_DROPS)) {
            ItemStack stack = new ItemStack(RegisterItems.OBSIDIAN_BOAT_ITEM);
            stack.setDamageValue(Math.min((int)Math.ceil(getTravelDistance()), stack.getMaxDamage() - 1));
            stack.set(DataComponents.CUSTOM_NAME, getCustomName());
            spawnAtLocation(level, stack);
        }
    }
}
