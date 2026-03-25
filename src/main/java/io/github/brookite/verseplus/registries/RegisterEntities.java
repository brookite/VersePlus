package io.github.brookite.verseplus.registries;

import io.github.brookite.verseplus.VersePlus;
import io.github.brookite.verseplus.entities.FireEnderPearlEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class RegisterEntities {
    // Объявляем тип сущности
    public static final EntityType<FireEnderPearlEntity> FIRE_ENDER_PEARL_ENTITY = register("fire_ender_pearl_entity",
            EntityType.Builder.<FireEnderPearlEntity>of(FireEnderPearlEntity::new, MobCategory.CREATURE)
            .sized(0.25f, 0.25f)
    );

    private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> type) {
        ResourceKey<EntityType<?>> regKey =
                ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(VersePlus.MOD_ID, name));
        return Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(VersePlus.MOD_ID, name),
                type.build(regKey)
        );
    }

    // Метод инициализации
    public static void initialize() {

    }
}
