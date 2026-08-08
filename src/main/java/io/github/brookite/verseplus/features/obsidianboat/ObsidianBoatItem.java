package io.github.brookite.verseplus.features.obsidianboat;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class ObsidianBoatItem extends BoatItem {
    private final EntityType<ObsidianBoatEntity> entityType;

    public ObsidianBoatItem(EntityType<ObsidianBoatEntity> entityType, Properties properties) {
        super(entityType, properties);
        this.entityType = entityType;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        HitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
        if (hitResult.getType() == HitResult.Type.MISS) {
            return InteractionResult.PASS;
        }

        Vec3 viewVector = player.getViewVector(1.0F);
        List<Entity> entities = level.getEntities(
                player,
                player.getBoundingBox().expandTowards(viewVector.scale(5.0)).inflate(1.0),
                EntitySelector.CAN_BE_PICKED
        );
        Vec3 eyePosition = player.getEyePosition();
        for (Entity entity : entities) {
            AABB box = entity.getBoundingBox().inflate(entity.getPickRadius());
            if (box.contains(eyePosition)) {
                return InteractionResult.PASS;
            }
        }

        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return InteractionResult.PASS;
        }

        ObsidianBoatEntity boat = createBoat(level, hitResult, stack, player);
        if (boat == null) {
            return InteractionResult.FAIL;
        }

        boat.setYRot(player.getYRot());
        if (!level.noCollision(boat, boat.getBoundingBox())) {
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide()) {
            level.addFreshEntity(boat);
            level.gameEvent(player, GameEvent.ENTITY_PLACE, hitResult.getLocation());
            stack.consume(1, player);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.SUCCESS;
    }

    private @Nullable ObsidianBoatEntity createBoat(Level level, HitResult hitResult, ItemStack stack, Player player) {
        ObsidianBoatEntity boat = entityType.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        if (boat != null) {
            Vec3 location = hitResult.getLocation();
            boat.setInitialPos(location.x, location.y, location.z);
            boat.setTravelDistance(stack.getDamageValue());
            if (level instanceof ServerLevel serverLevel) {
                EntityType.<ObsidianBoatEntity>createDefaultStackConfig(serverLevel, stack, player).apply(boat);
            }
        }
        return boat;
    }
}
