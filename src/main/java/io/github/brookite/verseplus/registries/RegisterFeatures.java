package io.github.brookite.verseplus.registries;

import io.github.brookite.verseplus.VersePlus;
import io.github.brookite.verseplus.worldgen.UnderwaterBlockPileFeature;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockPileConfiguration;

public class RegisterFeatures {
    public static final Feature<BlockPileConfiguration> UNDERWATER_BLOCK_PILE = Registry.register(
            BuiltInRegistries.FEATURE,
            Identifier.fromNamespaceAndPath(VersePlus.MOD_ID, "underwater_block_pile"),
            new UnderwaterBlockPileFeature(BlockPileConfiguration.CODEC)
    );

    public static void initialize() {
    }
}
