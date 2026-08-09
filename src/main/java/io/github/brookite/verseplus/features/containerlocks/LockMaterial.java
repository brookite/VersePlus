package io.github.brookite.verseplus.features.containerlocks;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public enum LockMaterial implements StringRepresentable {
    COPPER("copper"),
    IRON("iron"),
    GOLD("gold");

    public static final Codec<LockMaterial> CODEC = StringRepresentable.fromEnum(LockMaterial::values);

    private final String serializedName;

    LockMaterial(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public Item ingot() {
        return switch (this) {
            case COPPER -> Items.COPPER_INGOT;
            case IRON -> Items.IRON_INGOT;
            case GOLD -> Items.GOLD_INGOT;
        };
    }
}
