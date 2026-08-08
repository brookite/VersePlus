package io.github.brookite.verseplus.features.snifferegg.mixin;

import io.github.brookite.verseplus.features.snifferegg.SnifferEggFertility;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(method = "inventoryTick", at = @At("HEAD"))
    private void initializeSnifferEggFertility(Level level, Entity entity, EquipmentSlot slot, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        if (level.isClientSide() || !(entity instanceof Player) || !stack.is(Items.SNIFFER_EGG)) {
            return;
        }

        SnifferEggFertility.initialize(stack, level.getRandom());
    }
}
