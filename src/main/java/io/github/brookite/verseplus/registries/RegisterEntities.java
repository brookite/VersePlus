package io.github.brookite.verseplus.registries;

import io.github.brookite.verseplus.VersePlus;
import io.github.brookite.verseplus.entities.FireEnderPearlEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class RegisterEntities {
    // Объявляем тип сущности
    public static final EntityType<FireEnderPearlEntity> FIRE_ENDER_PEARL_ENTITY = register("fire_ender_pearl_entity",
            EntityType.Builder.<FireEnderPearlEntity>create(FireEnderPearlEntity::new, SpawnGroup.CREATURE)
            .dimensions(0.25f, 0.25f)
    );

    private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> type) {
        RegistryKey<EntityType<?>> regKey =
                RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(VersePlus.MOD_ID, name));
        return Registry.register(
                Registries.ENTITY_TYPE,
                Identifier.of(VersePlus.MOD_ID, name),
                type.build(regKey)
        );
    }

    // Метод инициализации
    public static void initialize() {

    }
}
