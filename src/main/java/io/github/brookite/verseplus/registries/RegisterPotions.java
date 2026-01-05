package io.github.brookite.verseplus.registries;

import io.github.brookite.verseplus.VersePlus;
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class RegisterPotions {
    public static final RegistryEntry<Potion> TRUE_INVISIBILITY = registerPotion("true_invisibility",
            new Potion("invisibility",
                new StatusEffectInstance(
                        StatusEffects.INVISIBILITY,
                        19200,
                        1,
                        false,
                        false,
                        true
                )
            )
    );

    private static RegistryEntry<Potion> registerPotion(String name, Potion potion) {
        return Registry.registerReference(
                Registries.POTION,
                Identifier.of(VersePlus.MOD_ID, name),
                potion
        );
    }

    public static void initialize() {
        FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> {
            builder.registerPotionRecipe(Potions.LONG_INVISIBILITY, Items.GLOWSTONE_DUST, TRUE_INVISIBILITY);
        });
    }

}
