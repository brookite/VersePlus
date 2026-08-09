package io.github.brookite.verseplus.features.containerlocks;

import io.github.brookite.verseplus.features.containerlocks.recipe.ContainerLockRecipes;

public final class ContainerLocksFeature {
    private ContainerLocksFeature() {
    }

    public static void initialize() {
        ContainerLockRecipes.initialize();
        ContainerLockInteraction.initialize();
        ContainerLockLoot.initialize();
    }
}
