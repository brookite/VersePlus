package io.github.brookite.registries;

import io.github.brookite.VersePlus;
import io.github.brookite.entities.FireEnderPearlEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class RegisterEntities {
    private static final RegistryKey<EntityType<?>> FIRE_ENDER_PEARL_KEY =
            RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(VersePlus.MOD_ID, "fire_ender_pearl_entity"));

    // Объявляем тип сущности
    public static final EntityType<FireEnderPearlEntity> FIRE_ENDER_PEARL_ENTITY = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(VersePlus.MOD_ID, "fire_ender_pearl_entity"),
            EntityType.Builder.<FireEnderPearlEntity>create(FireEnderPearlEntity::new, SpawnGroup.CREATURE)
                    .dimensions(0.25f, 0.25f).build(FIRE_ENDER_PEARL_KEY)
    );

    // Метод инициализации
    public static void initialize() {

    }
}
