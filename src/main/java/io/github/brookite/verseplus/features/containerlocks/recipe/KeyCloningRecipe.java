package io.github.brookite.verseplus.features.containerlocks.recipe;

import com.mojang.serialization.MapCodec;
import io.github.brookite.verseplus.features.containerlocks.ContainerLockComponents;
import io.github.brookite.verseplus.features.containerlocks.KeyItem;
import io.github.brookite.verseplus.features.containerlocks.LockData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public final class KeyCloningRecipe extends CustomRecipe {
    public static final KeyCloningRecipe INSTANCE = new KeyCloningRecipe();
    public static final MapCodec<KeyCloningRecipe> MAP_CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, KeyCloningRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);
    public static final RecipeSerializer<KeyCloningRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private static ItemStack findKey(CraftingInput input) {
        ItemStack found = ItemStack.EMPTY;
        for (ItemStack stack : input.items()) {
            if (stack.getItem() instanceof KeyItem) {
                if (!found.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                found = stack;
            }
        }
        return found;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (input.ingredientCount() != 2) {
            return false;
        }

        ItemStack keyStack = findKey(input);
        if (!(keyStack.getItem() instanceof KeyItem keyItem)) {
            return false;
        }
        LockData data = keyStack.get(ContainerLockComponents.LOCK_DATA);
        if (data == null || data.material() != keyItem.material()) {
            return false;
        }

        for (ItemStack stack : input.items()) {
            if (!stack.isEmpty() && stack != keyStack) {
                return stack.is(keyItem.material().ingot());
            }
        }
        return false;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack keyStack = findKey(input);
        return keyStack.isEmpty() ? ItemStack.EMPTY : keyStack.copyWithCount(2);
    }

    @Override
    public RecipeSerializer<KeyCloningRecipe> getSerializer() {
        return SERIALIZER;
    }
}
