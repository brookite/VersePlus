package io.github.brookite.verseplus.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemEntity.class)
public class ItemEntityGraveMetadataCleanupMixin {
    private static final String GRAVE_ID_KEY = "VersePlusGraveId";
    private static final String GRAVE_OWNER_KEY = "VersePlusGraveOwner";
    private static final String GRAVE_CREATED_AT_KEY = "VersePlusGraveCreatedAt";

    @Redirect(
            method = "playerTouch",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z"
            )
    )
    private boolean versePlus$addCleanGraveDrop(Inventory inventory, ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        if (!tag.contains(GRAVE_ID_KEY)
                && !tag.contains(GRAVE_OWNER_KEY)
                && !tag.contains(GRAVE_CREATED_AT_KEY)) {
            return inventory.add(stack);
        }

        tag.remove(GRAVE_ID_KEY);
        tag.remove(GRAVE_OWNER_KEY);
        tag.remove(GRAVE_CREATED_AT_KEY);
        ItemStack pickupStack = stack.copy();
        if (tag.isEmpty()) {
            pickupStack.remove(DataComponents.CUSTOM_DATA);
        } else {
            pickupStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }

        int originalCount = stack.getCount();
        boolean added = inventory.add(pickupStack);
        stack.shrink(originalCount - pickupStack.getCount());
        return added;
    }
}
