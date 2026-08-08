package io.github.brookite.verseplus.features.loyalpets.interaction;

import io.github.brookite.verseplus.features.loyalpets.data.LoyalPetAttachments;
import io.github.brookite.verseplus.features.loyalpets.data.PetOwnershipData;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.item.ItemStack;

public final class PetBondingHandler {
    private static final double PRIMARY_OWNER_RADIUS_SQUARED = 10.0 * 10.0;
    private static final byte TAMING_SUCCESS_EVENT = 7;
    private static final byte TAMING_FAILURE_EVENT = 6;

    private PetBondingHandler() {
    }

    public static void initialize() {
        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            if (!(entity instanceof TamableAnimal pet)
                    || (!(pet instanceof Wolf) && !(pet instanceof Cat))
                    || !pet.isTame()) {
                return InteractionResult.PASS;
            }

            ItemStack food = player.getItemInHand(hand);
            if (!pet.isFood(food) || pet.isOwnedBy(player)) {
                return InteractionResult.PASS;
            }

            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }

            if (!(level instanceof ServerLevel serverLevel)) {
                return InteractionResult.PASS;
            }

            LivingEntity primaryOwner = pet.getOwner();
            if (primaryOwner == null
                    || primaryOwner.level() != level
                    || !primaryOwner.isAlive()
                    || primaryOwner.isSpectator()
                    || pet.distanceToSqr(primaryOwner) > PRIMARY_OWNER_RADIUS_SQUARED) {
                serverLevel.broadcastEntityEvent(pet, TAMING_FAILURE_EVENT);
                return InteractionResult.FAIL;
            }

            PetOwnershipData ownership = LoyalPetAttachments.getOrCreateOwnership(pet);
            if (!ownership.hasOwnerCapacity() || !ownership.canAdvanceBonding(player.getUUID())) {
                serverLevel.broadcastEntityEvent(pet, TAMING_FAILURE_EVENT);
                return InteractionResult.FAIL;
            }

            food.consume(1, player);
            PetOwnershipData.BondingAdvance advance = ownership.advanceBonding(player.getUUID());
            pet.setAttached(LoyalPetAttachments.PET_OWNERSHIP, advance.data());
            serverLevel.broadcastEntityEvent(pet, TAMING_SUCCESS_EVENT);
            return InteractionResult.SUCCESS_SERVER;
        });
    }
}
