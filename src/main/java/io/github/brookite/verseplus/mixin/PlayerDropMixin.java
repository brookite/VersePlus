package io.github.brookite.verseplus.mixin;

import io.github.brookite.verseplus.VersePlus;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(Player.class)
public abstract class PlayerDropMixin extends LivingEntity {
    @Unique private static final String GRAVE_ID_KEY = "VersePlusGraveId";
    @Unique private static final String GRAVE_OWNER_KEY = "VersePlusGraveOwner";
    @Unique private static final String GRAVE_CREATED_AT_KEY = "VersePlusGraveCreatedAt";
    @Unique private UUID versePlus$activeGraveId;
    @Unique private long versePlus$activeGraveCreatedAt;

    protected PlayerDropMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    /**
     * @author brookit
     * @reason increase player drop lifetime
     */
    @Override
    public ItemEntity drop(ItemStack stack, boolean dropAtSelf, boolean retainOwnership) {
        if (this.versePlus$activeGraveId != null) {
            versePlus$markGraveStack(stack);
        }
        ItemEntity result = super.drop(stack, dropAtSelf, retainOwnership);
        if (result == null) {
            return null;
        }
        if (BuiltInRegistries.ITEM.getKey(result.getItem().getItem()).equals(Identifier.fromNamespaceAndPath(VersePlus.MOD_ID, "charged_rare_ender_pearl_item"))) {
            result.setPickUpDelay(-1);
        } else {
            result.setExtendedLifetime();
        }
        return result;
    }

    @Inject(method = "dropEquipment(Lnet/minecraft/server/level/ServerLevel;)V", at = @At("HEAD"))
    private void versePlus$beginDeathGrave(ServerLevel level, CallbackInfo ci) {
        this.versePlus$activeGraveId = UUID.randomUUID();
        this.versePlus$activeGraveCreatedAt = System.currentTimeMillis();
    }

    @Inject(method = "dropEquipment(Lnet/minecraft/server/level/ServerLevel;)V", at = @At("TAIL"))
    private void versePlus$endDeathGrave(ServerLevel level, CallbackInfo ci) {
        this.versePlus$activeGraveId = null;
        this.versePlus$activeGraveCreatedAt = 0L;
    }

    @Unique
    private void versePlus$markGraveStack(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        tag.putString(GRAVE_ID_KEY, this.versePlus$activeGraveId.toString());
        tag.putString(GRAVE_OWNER_KEY, this.getUUID().toString());
        tag.putLong(GRAVE_CREATED_AT_KEY, this.versePlus$activeGraveCreatedAt);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

}
