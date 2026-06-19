package io.github.brookite.verseplus.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static io.github.brookite.verseplus.VersePlusChances.UNPICKABLE_GHAST_TEAR_CHANCE;

@Mixin(Entity.class)
public class GhastTearPickupDelayMixin {
    @Inject(
            method = "spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/item/ItemEntity;",
            at = @At("RETURN")
    )
    private void sometimesMakeGhastTearsUnpickable(ServerLevel level, ItemStack stack, CallbackInfoReturnable<ItemEntity> cir) {
        ItemEntity itemEntity = cir.getReturnValue();
        if ((Object) this instanceof Ghast && itemEntity != null && stack.is(Items.GHAST_TEAR)
                && level.getRandom().nextFloat() < UNPICKABLE_GHAST_TEAR_CHANCE) {
            itemEntity.setNeverPickUp();
        }
    }
}
