package io.github.brookite.verseplus.registries;

import io.github.brookite.verseplus.VersePlus;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.registry.FabricPotionBrewingBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

public class RegisterPotions {
    public static final Holder<Potion> TRUE_INVISIBILITY = registerPotion("true_invisibility",
            new Potion("invisibility",
                new MobEffectInstance(
                        MobEffects.INVISIBILITY,
                        19200,
                        1,
                        false,
                        true,
                        true
                )
            )
    );

    private static Holder<Potion> registerPotion(String name, Potion potion) {
        return Registry.registerForHolder(
                BuiltInRegistries.POTION,
                Identifier.fromNamespaceAndPath(VersePlus.MOD_ID, name),
                potion
        );
    }

    public static void initialize() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(entries -> {
            entries.insertAfter(
                    PotionContents.createItemStack(Items.POTION, Potions.LONG_INVISIBILITY),
                    PotionContents.createItemStack(Items.POTION, TRUE_INVISIBILITY)
            );
            entries.insertAfter(
                    PotionContents.createItemStack(Items.SPLASH_POTION, Potions.LONG_INVISIBILITY),
                    PotionContents.createItemStack(Items.SPLASH_POTION, TRUE_INVISIBILITY)
            );
            entries.insertAfter(
                    PotionContents.createItemStack(Items.LINGERING_POTION, Potions.LONG_INVISIBILITY),
                    PotionContents.createItemStack(Items.LINGERING_POTION, TRUE_INVISIBILITY)
            );
        });

        FabricPotionBrewingBuilder.BUILD.register(builder -> {
            builder.addMix(Potions.LONG_INVISIBILITY, Items.GLOWSTONE_DUST, TRUE_INVISIBILITY);
        });
    }

}
