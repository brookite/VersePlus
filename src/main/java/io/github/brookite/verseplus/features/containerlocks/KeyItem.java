package io.github.brookite.verseplus.features.containerlocks;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.UUID;
import java.util.function.Consumer;

public class KeyItem extends Item {
    private final LockMaterial material;

    public KeyItem(LockMaterial material, Properties properties) {
        super(properties);
        this.material = material;
    }

    public LockMaterial material() {
        return material;
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        stack.set(ContainerLockComponents.LOCK_DATA, new LockData(material, UUID.randomUUID(), false, false));
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
            builder.accept(Component.translatable("tooltip.verseplus.key.uninitialized").withStyle(ChatFormatting.RED));
            return;
        }

        builder.accept(ContainerLocks.fingerprintTooltip(data));
    }
}
