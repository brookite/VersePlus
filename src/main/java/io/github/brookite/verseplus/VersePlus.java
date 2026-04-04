package io.github.brookite.verseplus;

import io.github.brookite.verseplus.registries.RegisterEntities;
import io.github.brookite.verseplus.registries.RegisterItems;
import io.github.brookite.verseplus.registries.RegisterPotions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.dispenser.ProjectileDispenseBehavior;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class VersePlus implements ModInitializer {
	public static final String MOD_ID = "verseplus";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private void changeForestTrees() {
        BiomeModifications.create(Identifier.fromNamespaceAndPath(MOD_ID, "changed_forest_trees")).add(
                ModificationPhase.REPLACEMENTS, BiomeSelectors.foundInOverworld()
                        .and((context) -> context
                                .getBiomeKey()
                                .identifier()
                                .getPath()
                                .equals("forest")
                        ),
                (selection, context) -> {
                    context.getGenerationSettings().removeFeature(ResourceKey.create(Registries.PLACED_FEATURE,
                            Identifier.withDefaultNamespace("trees_birch_and_oak_leaf_litter")));

                    context.getGenerationSettings().addFeature(GenerationStep.Decoration.VEGETAL_DECORATION,
                            ResourceKey.create(Registries.PLACED_FEATURE,
                            Identifier.fromNamespaceAndPath(MOD_ID,"trees_birch_and_oak")));
                }
        );
    }

    private void extendVanillaLootTables(Map<Identifier, LootPool> pools) {
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder) -> {
            var lootTable = resourceManager.identifier();
            for (var pool : pools.entrySet()) {
                if (lootTable.equals(pool.getKey()) && id.isBuiltin()) {
                    lootManager.pool(pool.getValue());
                }
            }
        });
    }

    private Map<Identifier, LootPool> getNewLoots() {
        var rarePearlStrongholdLoot = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(RegisterItems.RARE_ENDER_PEARL_ITEM))
                .when(LootItemRandomChanceCondition.randomChance(VersePlusChances.RARE_ENDER_PEARL_STRONGHOLD_CHEST_LOOT)).build();

        return Map.of(
                BuiltInLootTables.STRONGHOLD_CROSSING.identifier(), rarePearlStrongholdLoot,
                BuiltInLootTables.STRONGHOLD_CORRIDOR.identifier(), rarePearlStrongholdLoot
        );
    }


    @Override
	public void onInitialize() {
        RegisterItems.initialize();
        RegisterPotions.initialize();
        RegisterEntities.initialize();
        DispenserBlock.registerBehavior(RegisterItems.THROWABLE_FIREBALL_ITEM,
                new ProjectileDispenseBehavior(RegisterItems.THROWABLE_FIREBALL_ITEM));

        extendVanillaLootTables(getNewLoots());

        changeForestTrees();
    }
}