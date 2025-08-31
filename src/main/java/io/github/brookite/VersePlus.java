package io.github.brookite;

import io.github.brookite.registries.RegisterItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ProjectileDispenserBehavior;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VersePlus implements ModInitializer {
	public static final String MOD_ID = "verseplus";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private void changeForestTrees() {
        BiomeModifications.create(Identifier.of(MOD_ID, "changed_forest_trees")).add(
                ModificationPhase.REPLACEMENTS, BiomeSelectors.foundInOverworld()
                        .and((context) -> context
                                .getBiomeKey()
                                .getValue()
                                .getPath()
                                .equals("forest")
                        ),
                (selection, context) -> {
                    context.getGenerationSettings().removeFeature(RegistryKey.of(RegistryKeys.PLACED_FEATURE,
                            Identifier.ofVanilla("trees_birch_and_oak_leaf_litter")));

                    context.getGenerationSettings().addFeature(GenerationStep.Feature.VEGETAL_DECORATION,
                            RegistryKey.of(RegistryKeys.PLACED_FEATURE,
                            Identifier.of(MOD_ID,"trees_birch_and_oak")));
                }
        );
    }

    @Override
	public void onInitialize() {
        changeForestTrees();
        RegisterItems.initialize();
        DispenserBlock.registerBehavior(RegisterItems.THROWABLE_FIREBALL_ITEM,
                new ProjectileDispenserBehavior(RegisterItems.THROWABLE_FIREBALL_ITEM));
	}
}