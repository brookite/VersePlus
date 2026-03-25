package io.github.brookite.verseplus.datagen;

import io.github.brookite.verseplus.VersePlusChances;
import io.github.brookite.verseplus.registries.RegisterItems;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableSubProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class ModChestLootTables extends SimpleFabricLootTableSubProvider {
    public ModChestLootTables(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(output, registryLookup, LootContextParamSets.CHEST);
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> consumer) {
        ResourceKey<LootTable> fortressChest = ResourceKey.create(Registries.LOOT_TABLE, Identifier.withDefaultNamespace("chests/stronghold"));
        LootPool.Builder fortPool = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(RegisterItems.RARE_ENDER_PEARL_ITEM))
                .when(LootItemRandomChanceCondition.randomChance(VersePlusChances.RARE_ENDER_PEARL_STRONGHOLD_CHEST_LOOT));
        consumer.accept(fortressChest, LootTable.lootTable().withPool(fortPool));
    }
}
