package io.github.brookite.verseplus.features.containerlocks.recipe;

import io.github.brookite.verseplus.VersePlus;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public final class ContainerLockRecipes {
    private ContainerLockRecipes() {
    }

    public static void initialize() {
        Registry.register(
                BuiltInRegistries.RECIPE_SERIALIZER,
                Identifier.fromNamespaceAndPath(VersePlus.MOD_ID, "key_cloning"),
                KeyCloningRecipe.SERIALIZER
        );
        Registry.register(
                BuiltInRegistries.RECIPE_SERIALIZER,
                Identifier.fromNamespaceAndPath(VersePlus.MOD_ID, "shulker_locking"),
                ShulkerLockRecipe.SERIALIZER
        );
    }
}
