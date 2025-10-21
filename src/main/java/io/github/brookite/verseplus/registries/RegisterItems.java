package io.github.brookite.verseplus.registries;

import io.github.brookite.verseplus.VersePlus;
import io.github.brookite.verseplus.items.ChargedRareEnderPearlItem;
import io.github.brookite.verseplus.items.FireEnderPearlItem;
import io.github.brookite.verseplus.items.ThrowableFireballItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

import java.util.function.Function;

public class RegisterItems {
    private static Item register(String name, Function<Item.Settings, Item> itemFactory, Item.Settings settings) {
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(VersePlus.MOD_ID, name));
        Item item = itemFactory.apply(settings.registryKey(itemKey));
        Registry.register(Registries.ITEM, itemKey, item);
        return item;
    }

    public static final Item THROWABLE_FIREBALL_ITEM = register("throwable_fireball_item",
            ThrowableFireballItem::new, new Item.Settings().useCooldown(3)
    );
    public static final Item FIRE_ENDER_PEARL_ITEM = register("fire_ender_pearl_item",
            FireEnderPearlItem::new,
            new Item.Settings().useCooldown(1).maxCount(16).rarity(Rarity.RARE));
    public static final Item RARE_ENDER_PEARL_ITEM = register("rare_ender_pearl_item",
            EnderPearlItem::new,
            new Item.Settings().useCooldown(1).maxCount(16).rarity(Rarity.EPIC));
    public static final Item CHARGED_RARE_ENDER_PEARL_ITEM = register("charged_rare_ender_pearl_item",
            ChargedRareEnderPearlItem::new,
            new Item.Settings().useCooldown(15).maxCount(16).rarity(Rarity.EPIC));

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries ->
                entries.addAfter(Items.FIRE_CHARGE, RegisterItems.THROWABLE_FIREBALL_ITEM));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries ->
                entries.addAfter(Items.ENDER_PEARL, RegisterItems.FIRE_ENDER_PEARL_ITEM));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries ->
                entries.addAfter(RegisterItems.FIRE_ENDER_PEARL_ITEM, RegisterItems.RARE_ENDER_PEARL_ITEM));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries ->
                entries.addAfter(RegisterItems.RARE_ENDER_PEARL_ITEM, RegisterItems.CHARGED_RARE_ENDER_PEARL_ITEM));
    }
}
