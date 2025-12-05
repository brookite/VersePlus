package io.github.brookite.verseplus;

import io.github.brookite.verseplus.registries.RegisterEntities;
import io.github.brookite.verseplus.registries.RegisterItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ProjectileDispenserBehavior;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradedItem;
import net.minecraft.world.gen.GenerationStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

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

    private void extendVanillaLootTables(Map<Identifier, LootPool> pools) {
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder) -> {
            var lootTable = resourceManager.getValue();
            for (var pool : pools.entrySet()) {
                if (lootTable.equals(pool.getKey()) && id.isBuiltin()) {
                    lootManager.pool(pool.getValue());
                }
            }
        });
    }

    private Map<Identifier, LootPool> getNewLoots() {
        var rarePearlStrongholdLoot = LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(1))
                .with(ItemEntry.builder(RegisterItems.RARE_ENDER_PEARL_ITEM))
                .conditionally(RandomChanceLootCondition.builder(VersePlusChances.RARE_ENDER_PEARL_STRONGHOLD_CHEST_LOOT)).build();

        return Map.of(
                LootTables.STRONGHOLD_CROSSING_CHEST.getValue(), rarePearlStrongholdLoot,
                LootTables.STRONGHOLD_CORRIDOR_CHEST.getValue(), rarePearlStrongholdLoot
        );
    }

    private void extendWanderingTraderOffers() {
        TradeOfferHelper.registerWanderingTraderOffers(builder -> {
            builder.addOffersToPool(
                    TradeOfferHelper.WanderingTraderOffersBuilder.SELL_SPECIAL_ITEMS_POOL,
                    (world, entity, random) -> {
                        if (random.nextDouble() < VersePlusChances.RARE_ENDER_PEARL_TRADE_CHANCE) {
                            return new TradeOffer(
                                    new TradedItem(Items.EMERALD, 15),
                                    new ItemStack(RegisterItems.RARE_ENDER_PEARL_ITEM),
                                    5, // maxUses
                                    8,  // xp
                                    0.2f // price multiplier
                            );
                        }
                        return null;
                    }
            );
        });
    }

    @Override
	public void onInitialize() {
        changeForestTrees();
        extendVanillaLootTables(getNewLoots());
        extendWanderingTraderOffers();
        RegisterItems.initialize();
        RegisterEntities.initialize();
        DispenserBlock.registerBehavior(RegisterItems.THROWABLE_FIREBALL_ITEM,
                new ProjectileDispenserBehavior(RegisterItems.THROWABLE_FIREBALL_ITEM));
	}
}