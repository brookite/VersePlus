package io.github.brookite.verseplus.features.snifferegg;

import io.github.brookite.verseplus.interfaces.ModFeature;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import static io.github.brookite.verseplus.VersePlusChances.EMPTY_SNIFFER_EGG_CHANCE;

public final class SnifferEggFertility implements ModFeature {
    private static final String INFERTILE_TAG = "verseplus:infertile_sniffer_egg";

    private SnifferEggFertility() {
    }

    public static void initialize(ItemStack stack, RandomSource random) {
        if (hasFertility(stack)) {
            return;
        }

        boolean infertile = random.nextFloat() < EMPTY_SNIFFER_EGG_CHANCE;
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putBoolean(INFERTILE_TAG, infertile));
    }

    public static boolean isInfertile(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        return customData != null && customData.copyTag().getBooleanOr(INFERTILE_TAG, false);
    }

    private static boolean hasFertility(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return false;
        }

        CompoundTag tag = customData.copyTag();
        return tag.contains(INFERTILE_TAG);
    }
}
