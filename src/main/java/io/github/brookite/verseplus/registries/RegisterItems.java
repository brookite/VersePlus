package io.github.brookite.verseplus.registries;

import io.github.brookite.verseplus.VersePlus;
import io.github.brookite.verseplus.items.ChargedRareEnderPearlItem;
import io.github.brookite.verseplus.items.FireEnderPearlItem;
import io.github.brookite.verseplus.items.ThrowableFireballItem;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Function;

public class RegisterItems {
    private static Item register(String name, Function<Item.Properties, Item> itemFactory, Item.Properties settings) {
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(VersePlus.MOD_ID, name));
        Item item = itemFactory.apply(settings.setId(itemKey));
        Registry.register(BuiltInRegistries.ITEM, itemKey, item);
        return item;
    }

    public static final Item THROWABLE_FIREBALL_ITEM = register("throwable_fireball_item",
            ThrowableFireballItem::new, new Item.Properties().useCooldown(3)
    );
    public static final Item FIRE_ENDER_PEARL_ITEM = register("fire_ender_pearl_item",
            FireEnderPearlItem::new,
            new Item.Properties().useCooldown(1).stacksTo(16).rarity(Rarity.RARE));
    public static final Item RARE_ENDER_PEARL_ITEM = register("rare_ender_pearl_item",
            EnderpearlItem::new,
            new Item.Properties().useCooldown(1).stacksTo(16).rarity(Rarity.EPIC));
    public static final Item CHARGED_RARE_ENDER_PEARL_ITEM = register("charged_rare_ender_pearl_item",
            ChargedRareEnderPearlItem::new,
            new Item.Properties().useCooldown(15).stacksTo(16).rarity(Rarity.EPIC));
    public static final Item FROSTED_ICE_ITEM = register("frosted_ice",
            properties -> new BlockItem(Blocks.FROSTED_ICE, properties),
            new Item.Properties());

    public static void initialize() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.NATURAL_BLOCKS).register(entries ->
                entries.insertAfter(Items.ICE, RegisterItems.FROSTED_ICE_ITEM));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(entries ->
                entries.insertAfter(Items.FIRE_CHARGE, RegisterItems.THROWABLE_FIREBALL_ITEM));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(entries ->
                entries.insertAfter(Items.ENDER_PEARL, RegisterItems.FIRE_ENDER_PEARL_ITEM));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(entries ->
                entries.insertAfter(RegisterItems.FIRE_ENDER_PEARL_ITEM, RegisterItems.RARE_ENDER_PEARL_ITEM));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(entries ->
                entries.insertAfter(RegisterItems.RARE_ENDER_PEARL_ITEM, RegisterItems.CHARGED_RARE_ENDER_PEARL_ITEM));
    }
}
