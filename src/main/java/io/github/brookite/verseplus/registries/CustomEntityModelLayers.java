package io.github.brookite.verseplus.registries;

import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class CustomEntityModelLayers {
    public static final EntityModelLayer SPIDER_SMALL_LAYER =
            new EntityModelLayer(Identifier.of("verseplus", "spider_small"), "main");
    public static final EntityModelLayer SPIDER_TINY_LAYER =
            new EntityModelLayer(Identifier.of("verseplus", "spider_tiny"), "main");
    public static final EntityModelLayer SPIDER_MEDIUM_LAYER =
            new EntityModelLayer(Identifier.of("verseplus", "spider_medium"), "main");
}
