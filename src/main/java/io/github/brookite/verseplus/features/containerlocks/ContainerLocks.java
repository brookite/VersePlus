package io.github.brookite.verseplus.features.containerlocks;

import io.github.brookite.verseplus.registries.RegisterItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;
import java.util.UUID;

public final class ContainerLocks {
    private ContainerLocks() {
    }

    public static ItemStack createGeneratedLock(LockMaterial material, RandomSource random) {
        UUID fingerprint = new UUID(random.nextLong(), random.nextLong());
        ItemStack stack = new ItemStack(lockItem(material));
        stack.set(ContainerLockComponents.LOCK_DATA, new LockData(material, fingerprint, false, true));
        return stack;
    }

    public static ItemStack createLock(LockData data) {
        ItemStack stack = new ItemStack(lockItem(data.material()));
        stack.set(ContainerLockComponents.LOCK_DATA, data.installed(false));
        return stack;
    }

    public static ItemStack createKey(LockData data) {
        ItemStack stack = new ItemStack(keyItem(data.material()));
        stack.set(ContainerLockComponents.LOCK_DATA, data.installed(false));
        return stack;
    }

    public static boolean matches(ItemStack keyStack, LockData lockData) {
        if (!(keyStack.getItem() instanceof KeyItem keyItem) || keyItem.material() != lockData.material()) {
            return false;
        }

        LockData keyData = keyStack.get(ContainerLockComponents.LOCK_DATA);
        return keyData != null
                && keyData.material() == lockData.material()
                && keyData.fingerprint().equals(lockData.fingerprint());
    }

    public static Component fingerprintTooltip(LockData data) {
        String fingerprint = data.fingerprint().toString().replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);
        return Component.translatable("tooltip.verseplus.lock.fingerprint", fingerprint).withStyle(ChatFormatting.DARK_GRAY);
    }

    public static Item lockItem(LockMaterial material) {
        return switch (material) {
            case COPPER -> RegisterItems.COPPER_LOCK;
            case IRON -> RegisterItems.IRON_LOCK;
            case GOLD -> RegisterItems.GOLDEN_LOCK;
        };
    }

    public static Item keyItem(LockMaterial material) {
        return switch (material) {
            case COPPER -> RegisterItems.COPPER_KEY;
            case IRON -> RegisterItems.IRON_KEY;
            case GOLD -> RegisterItems.GOLDEN_KEY;
        };
    }
}
