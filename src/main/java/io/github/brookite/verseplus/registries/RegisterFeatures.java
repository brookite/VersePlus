package io.github.brookite.verseplus.registries;

import io.github.brookite.verseplus.VersePlus;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.feature.BlockPileFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public class RegisterFeatures {
    public static final RegistryKey<ConfiguredFeature<?, ?>> WET_SPONGE_PILE_KEY =
            RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE,
                    Identifier.of(VersePlus.MOD_ID, "wet_sponge_pile"));

    public static void bootstrap(Registerable<ConfiguredFeature<?, ?>> context) {

        var config = new BlockPileFeatureConfig(
                BlockStateProvider.of(Blocks.WET_SPONGE)
        );

        context.register(WET_SPONGE_PILE_KEY,
                new ConfiguredFeature<>(Feature.BLOCK_PILE, config));
    }
}
