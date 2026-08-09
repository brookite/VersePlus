package io.github.brookite.verseplus.features.containerlocks;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.UUID;
import java.util.function.Consumer;

public class LockItem extends Item {
    private final LockMaterial material;

    public LockItem(LockMaterial material, Properties properties) {
        super(properties);
        this.material = material;
    }

    public LockMaterial material() {
        return material;
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        stack.set(ContainerLockComponents.LOCK_DATA, new LockData(material, UUID.randomUUID(), false, true));
        return stack;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> builder,
            TooltipFlag tooltipFlag
    ) {
        LockData data = stack.get(ContainerLockComponents.LOCK_DATA);
        if (data == null) {
            builder.accept(Component.translatable("tooltip.verseplus.lock.uninitialized").withStyle(ChatFormatting.RED));
            return;
        }

        String keyTooltip = data.containsKey()
                ? "tooltip.verseplus.lock.contains_key"
                : "tooltip.verseplus.lock.no_key";
        builder.accept(Component.translatable(keyTooltip).withStyle(ChatFormatting.GRAY));
        builder.accept(ContainerLocks.fingerprintTooltip(data));
    }
}
