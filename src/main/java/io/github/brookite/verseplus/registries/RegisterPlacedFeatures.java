package io.github.brookite.verseplus.registries;

import io.github.brookite.verseplus.VersePlus;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.placementmodifier.*;

import java.util.List;

public class RegisterPlacedFeatures {

    public static final RegistryKey<PlacedFeature> WET_SPONGE_PILE_PLACED_KEY =
            RegistryKey.of(RegistryKeys.PLACED_FEATURE,
                    Identifier.of(VersePlus.MOD_ID, "wet_sponge_pile_placed"));

    public static void bootstrap(Registerable<PlacedFeature> context) {

        var configured = context.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE)
                .getOrThrow(RegisterFeatures.WET_SPONGE_PILE_KEY);

        context.register(WET_SPONGE_PILE_PLACED_KEY,
                new PlacedFeature(configured,
                        List.of(
                                CountPlacementModifier.of(
                                        UniformIntProvider.create(3, 8)
                                ),
                                RarityFilterPlacementModifier.of(30),
                                SquarePlacementModifier.of(),
                                HeightmapPlacementModifier.of(Heightmap.Type.OCEAN_FLOOR_WG),
                                BiomePlacementModifier.of()
                        )
                ));
    }
}