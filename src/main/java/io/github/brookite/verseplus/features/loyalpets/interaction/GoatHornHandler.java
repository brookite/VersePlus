package io.github.brookite.verseplus.features.loyalpets.interaction;

import io.github.brookite.verseplus.features.loyalpets.data.LoyalPetAttachments;
import io.github.brookite.verseplus.features.loyalpets.data.WolfHornMemory;
import io.github.brookite.verseplus.features.loyalpets.recall.WolfRecallManager;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.InstrumentComponent;
import net.minecraft.world.phys.AABB;

public final class GoatHornHandler {
    private static final double TRAINING_RADIUS = 64.0;
    private static final double TRAINING_RADIUS_SQUARED = TRAINING_RADIUS * TRAINING_RADIUS;
    private static final byte TAMING_SUCCESS_EVENT = 7;

    private GoatHornHandler() {
    }

    public static void initialize() {
        UseItemCallback.EVENT.register((player, level, hand) -> {
            var stack = player.getItemInHand(hand);
            if (level.isClientSide()
                    || !(player instanceof ServerPlayer serverPlayer)
                    || !stack.is(Items.GOAT_HORN)
                    || player.getCooldowns().isOnCooldown(stack)) {
                return InteractionResult.PASS;
            }

            InstrumentComponent component = stack.get(DataComponents.INSTRUMENT);
            if (component == null) {
                return InteractionResult.PASS;
            }

            Identifier sound = component.instrument().unwrapKey()
                    .map(key -> key.identifier())
                    .orElse(null);
            if (sound == null || !(level instanceof ServerLevel serverLevel)) {
                return InteractionResult.PASS;
            }

            WolfRecallManager.recall(serverPlayer, sound);
            trainNearbyWolves(serverLevel, serverPlayer, sound);
            return InteractionResult.PASS;
        });
    }

    private static void trainNearbyWolves(ServerLevel level, ServerPlayer player, Identifier sound) {
        AABB area = AABB.ofSize(
                player.position(),
                TRAINING_RADIUS * 2.0,
                TRAINING_RADIUS * 2.0,
                TRAINING_RADIUS * 2.0
        );
        for (Wolf wolf : level.getEntitiesOfClass(
                Wolf.class,
                area,
                wolf -> wolf.isTame()
                        && wolf.isOwnedBy(player)
                        && wolf.distanceToSqr(player) <= TRAINING_RADIUS_SQUARED
        )) {
            WolfHornMemory memory = LoyalPetAttachments.getOrCreateHornMemory(wolf);
            WolfHornMemory.LearningAdvance advance = memory.hear(sound);
            if (advance.memory().equals(memory)) {
                continue;
            }

            wolf.setAttached(LoyalPetAttachments.WOLF_HORN_MEMORY, advance.memory());
            level.broadcastEntityEvent(wolf, TAMING_SUCCESS_EVENT);
            if (advance.completed()) {
                WolfRecallManager.track(wolf);
            }
        }
    }
}
