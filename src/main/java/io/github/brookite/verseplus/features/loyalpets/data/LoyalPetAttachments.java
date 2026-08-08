package io.github.brookite.verseplus.features.loyalpets.data;

import io.github.brookite.verseplus.VersePlus;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.TamableAnimal;

import java.util.UUID;

public final class LoyalPetAttachments {
    public static final AttachmentType<PetOwnershipData> PET_OWNERSHIP = AttachmentRegistry.create(
            id("pet_ownership"),
            builder -> builder
                    .persistent(PetOwnershipData.CODEC)
                    .syncWith(ByteBufCodecs.fromCodecWithRegistries(PetOwnershipData.CODEC), AttachmentSyncPredicate.all())
    );

    public static final AttachmentType<WolfHornMemory> WOLF_HORN_MEMORY = AttachmentRegistry.createPersistent(
            id("wolf_horn_memory"),
            WolfHornMemory.CODEC
    );

    private LoyalPetAttachments() {
    }

    public static void initialize() {
    }

    public static PetOwnershipData getOrCreateOwnership(TamableAnimal pet) {
        return pet.getAttachedOrCreate(PET_OWNERSHIP, () -> PetOwnershipData.create(pet.getRandom()));
    }

    public static WolfHornMemory getOrCreateHornMemory(TamableAnimal pet) {
        return pet.getAttachedOrCreate(WOLF_HORN_MEMORY, () -> WolfHornMemory.create(pet.getRandom()));
    }

    public static boolean isAdditionalOwner(TamableAnimal pet, UUID playerId) {
        PetOwnershipData data = pet.getAttached(PET_OWNERSHIP);
        return data != null && data.isAdditionalOwner(playerId);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(VersePlus.MOD_ID, path);
    }
}
