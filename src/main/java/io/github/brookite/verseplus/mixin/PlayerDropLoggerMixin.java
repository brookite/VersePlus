package io.github.brookite.verseplus.mixin;

import io.github.brookite.verseplus.ItemDropLogHandler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class PlayerDropLoggerMixin {
    private static final String GRAVE_ID_KEY = "VersePlusGraveId";
    private static final String GRAVE_OWNER_KEY = "VersePlusGraveOwner";
    private static final String GRAVE_CREATED_AT_KEY = "VersePlusGraveCreatedAt";
    @Shadow
    private Level level;

    @Inject(at = @At("HEAD"), method = "discard()V")
    public void discard(CallbackInfo ci) {
        if (((Object)this) instanceof ItemEntity itemEntity && level instanceof ServerLevel serverWorld) {
            ItemStack stack = itemEntity.getItem();
            boolean trackedGraveDrop = stack != null && stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                    .copyTag().contains(GRAVE_ID_KEY);
            if (!(trackedGraveDrop
                    || itemEntity.getOwner() instanceof Player
                    || itemEntity.getRemainingFireTicks() > 0
                    || itemEntity.health < 0)) {
                return;
            }

            var logger = ItemDropLogHandler.get(serverWorld.getServer());
            if (stack != null && !stack.isEmpty()) {
                ItemStack cleanStack = stack.copy();
                CustomData customData = cleanStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                CompoundTag tag = customData.copyTag();
                var graveId = tag.getString(GRAVE_ID_KEY);
                var graveOwner = tag.getString(GRAVE_OWNER_KEY);
                long graveCreatedAt = tag.getLongOr(GRAVE_CREATED_AT_KEY, System.currentTimeMillis());
                tag.remove(GRAVE_ID_KEY);
                tag.remove(GRAVE_OWNER_KEY);
                tag.remove(GRAVE_CREATED_AT_KEY);
                cleanStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

                if (graveId.isPresent() && graveOwner.isPresent()) {
                    try {
                        logger.addGraveItem(
                                java.util.UUID.fromString(graveId.get()),
                                java.util.UUID.fromString(graveOwner.get()),
                                graveCreatedAt,
                                cleanStack
                        );
                    } catch (IllegalArgumentException ignored) {
                    }
                }
                logger.addItem(cleanStack);
            }
        }
    }
}
