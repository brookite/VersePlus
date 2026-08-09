package io.github.brookite.verseplus.features.containerlocks;

import io.github.brookite.verseplus.VersePlusChances;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.Set;

final class ContainerLockLoot {
    private static final Set<ResourceKey<LootTable>> STRUCTURE_LOOT = Set.of(
            BuiltInLootTables.SHIPWRECK_MAP,
            BuiltInLootTables.SHIPWRECK_SUPPLY,
            BuiltInLootTables.SHIPWRECK_TREASURE,
            BuiltInLootTables.VILLAGE_WEAPONSMITH,
            BuiltInLootTables.VILLAGE_TOOLSMITH,
            BuiltInLootTables.VILLAGE_ARMORER,
            BuiltInLootTables.VILLAGE_CARTOGRAPHER,
            BuiltInLootTables.VILLAGE_MASON,
            BuiltInLootTables.VILLAGE_SHEPHERD,
            BuiltInLootTables.VILLAGE_BUTCHER,
            BuiltInLootTables.VILLAGE_FLETCHER,
            BuiltInLootTables.VILLAGE_FISHER,
            BuiltInLootTables.VILLAGE_TANNERY,
            BuiltInLootTables.VILLAGE_TEMPLE,
            BuiltInLootTables.VILLAGE_DESERT_HOUSE,
            BuiltInLootTables.VILLAGE_PLAINS_HOUSE,
            BuiltInLootTables.VILLAGE_TAIGA_HOUSE,
            BuiltInLootTables.VILLAGE_SNOWY_HOUSE,
            BuiltInLootTables.VILLAGE_SAVANNA_HOUSE,
            BuiltInLootTables.ABANDONED_MINESHAFT,
            BuiltInLootTables.ANCIENT_CITY,
            BuiltInLootTables.ANCIENT_CITY_ICE_BOX
    );

    private ContainerLockLoot() {
    }

    static void initialize() {
        LootTableEvents.MODIFY_DROPS.register((holder, context, drops) -> holder.unwrapKey().ifPresent(key -> {
            if (STRUCTURE_LOOT.contains(key)
                    && context.getRandom().nextFloat() < VersePlusChances.CONTAINER_LOCK_STRUCTURE_CHEST_LOOT) {
                LockMaterial[] materials = LockMaterial.values();
                LockMaterial material = materials[context.getRandom().nextInt(materials.length)];
                drops.add(ContainerLocks.createGeneratedLock(material, context.getRandom()));
            }

            if (!key.identifier().getPath().startsWith("blocks/")) {
                return;
            }

            var origin = context.getOptionalParameter(LootContextParams.ORIGIN);
            var droppedState = context.getOptionalParameter(LootContextParams.BLOCK_STATE);
            if (origin != null && droppedState != null && droppedState.getBlock() instanceof ButtonBlock) {
                BlockPos pos = BlockPos.containing(origin);
                LockData buttonLock = ButtonLockSavedData.get(context.getLevel()).getLock(pos);
                if (buttonLock != null) {
                    for (ItemStack drop : drops) {
                        if (drop.getItem() instanceof BlockItem blockItem
                                && blockItem.getBlock() instanceof ButtonBlock) {
                            drop.set(ContainerLockComponents.LOCK_DATA, buttonLock.installed(buttonLock.closed()));
                            break;
                        }
                    }
                    ButtonLockSavedData.get(context.getLevel()).removeLock(pos);
                }
            }

            BaseContainerBlockEntity container = context.getOptionalParameter(LootContextParams.BLOCK_ENTITY)
                    instanceof BaseContainerBlockEntity baseContainer ? baseContainer : null;
            if (container == null) {
                return;
            }
            LockData data = container.components().get(ContainerLockComponents.LOCK_DATA);
            if (data == null) {
                return;
            }

            if (container instanceof ShulkerBoxBlockEntity) {
                for (ItemStack drop : drops) {
                    if (drop.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock) {
                        drop.set(ContainerLockComponents.LOCK_DATA, data);
                        break;
                    }
                }
            } else if (container instanceof ChestBlockEntity || container instanceof BarrelBlockEntity) {
                drops.add(ContainerLocks.createLock(data));
            }
        }));
    }
}
