package io.github.brookite.verseplus.features.containerlocks.recipe;

import com.mojang.serialization.MapCodec;
import io.github.brookite.verseplus.features.containerlocks.ContainerLockComponents;
import io.github.brookite.verseplus.features.containerlocks.ContainerLocks;
import io.github.brookite.verseplus.features.containerlocks.LockData;
import io.github.brookite.verseplus.features.containerlocks.LockItem;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ShulkerBoxBlock;

public final class ShulkerLockRecipe extends CustomRecipe {
    public static final ShulkerLockRecipe INSTANCE = new ShulkerLockRecipe();
    public static final MapCodec<ShulkerLockRecipe> MAP_CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, ShulkerLockRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);
    public static final RecipeSerializer<ShulkerLockRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private record Inputs(ItemStack shulker, ItemStack lock, LockData data) {
    }

    private static Inputs findInputs(CraftingInput input) {
        if (input.ingredientCount() != 2) {
            return null;
        }

        ItemStack shulker = ItemStack.EMPTY;
        ItemStack lock = ItemStack.EMPTY;
        for (ItemStack stack : input.items()) {
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock) {
                if (!shulker.isEmpty()) {
                    return null;
                }
                shulker = stack;
            } else if (stack.getItem() instanceof LockItem) {
                if (!lock.isEmpty()) {
                    return null;
                }
                lock = stack;
            } else {
                return null;
            }
        }

        LockData data = lock.get(ContainerLockComponents.LOCK_DATA);
        if (shulker.isEmpty()
                || !(lock.getItem() instanceof LockItem lockItem)
                || data == null
                || data.material() != lockItem.material()
                || shulker.has(ContainerLockComponents.LOCK_DATA)) {
            return null;
        }
        return new Inputs(shulker, lock, data);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return findInputs(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        Inputs inputs = findInputs(input);
        if (inputs == null) {
            return ItemStack.EMPTY;
        }

        ItemStack result = inputs.shulker().copyWithCount(1);
        result.set(ContainerLockComponents.LOCK_DATA, inputs.data().installed(false));
        return result;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> result = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        Inputs inputs = findInputs(input);
        if (inputs == null || !inputs.data().containsKey()) {
            return result;
        }

        for (int slot = 0; slot < input.size(); slot++) {
            if (input.getItem(slot) == inputs.lock()) {
                result.set(slot, ContainerLocks.createKey(inputs.data()));
                break;
            }
        }
        return result;
    }

    @Override
    public RecipeSerializer<ShulkerLockRecipe> getSerializer() {
        return SERIALIZER;
    }
}
