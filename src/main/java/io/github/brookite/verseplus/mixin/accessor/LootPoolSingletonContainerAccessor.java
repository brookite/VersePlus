package io.github.brookite.verseplus.mixin.accessor;

import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LootPoolSingletonContainer.class)
public interface LootPoolSingletonContainerAccessor {
    @Accessor("quality")
    int verseplus$getQuality();
}
