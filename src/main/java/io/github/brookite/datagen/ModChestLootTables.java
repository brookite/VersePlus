package io.github.brookite.datagen;

import io.github.brookite.VersePlusChances;
import io.github.brookite.registries.RegisterItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableProvider;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class ModChestLootTables extends SimpleFabricLootTableProvider {
    public ModChestLootTables(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(output, registryLookup, LootContextTypes.CHEST);
    }

    @Override
    public void accept(BiConsumer<RegistryKey<LootTable>, LootTable.Builder> consumer) {
        RegistryKey<LootTable> fortressChest = RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.ofVanilla("chests/stronghold"));
        LootPool.Builder fortPool = LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(1))
                .with(ItemEntry.builder(RegisterItems.RARE_ENDER_PEARL_ITEM))
                .conditionally(RandomChanceLootCondition.builder(VersePlusChances.RARE_ENDER_PEARL_STRONGHOLD_CHEST_LOOT));
        consumer.accept(fortressChest, LootTable.builder().pool(fortPool));
    }
}
