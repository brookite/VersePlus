package io.github.brookite.verseplus.features.containerlocks.mixin;

import io.github.brookite.verseplus.features.containerlocks.ContainerLockComponents;
import io.github.brookite.verseplus.features.containerlocks.LockData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackTooltipMixin {
    @Redirect(
            method = "addDetailsToTooltip",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;addToTooltip(Lnet/minecraft/core/component/DataComponentType;Lnet/minecraft/world/item/Item$TooltipContext;Lnet/minecraft/world/item/component/TooltipDisplay;Ljava/util/function/Consumer;Lnet/minecraft/world/item/TooltipFlag;)V",
                    ordinal = 5
            )
    )
    private <T extends TooltipProvider> void hideClosedShulkerContents(
            ItemStack stack,
            DataComponentType<T> type,
            Item.TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> builder,
            TooltipFlag flag
    ) {
        boolean isShulker = stack.getItem() instanceof BlockItem blockItem
                && blockItem.getBlock() instanceof ShulkerBoxBlock;
        LockData data = stack.get(ContainerLockComponents.LOCK_DATA);
        if (!isShulker || data == null) {
            stack.addToTooltip(type, context, display, builder, flag);
            return;
        }

        builder.accept(Component.translatable(
                data.closed() ? "tooltip.verseplus.lock.closed" : "tooltip.verseplus.lock.open"
        ).withStyle(data.closed() ? ChatFormatting.RED : ChatFormatting.GREEN));
        if (!data.closed()) {
            stack.addToTooltip(type, context, display, builder, flag);
        }
    }
}
