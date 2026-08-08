package io.github.brookite.verseplus.features.wardenstaff;

import io.github.brookite.verseplus.VersePlus;
import io.github.brookite.verseplus.registries.RegisterItems;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public final class WardenStaffFeature {
    private static final Identifier WARDEN_LOOT_TABLE = Identifier.withDefaultNamespace("entities/warden");

    private WardenStaffFeature() {
    }

    public static void initialize() {
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder) -> {
            if (resourceManager.identifier().equals(WARDEN_LOOT_TABLE) && id.isBuiltin()) {
                lootManager.pool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(RegisterItems.WARDEN_ROD))
                        .when(LootItemKilledByPlayerCondition.killedByPlayer())
                        .build());
            }
        });
    }
}
