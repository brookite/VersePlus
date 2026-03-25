package io.github.brookite.verseplus.registries;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;

public class CustomEntityModelLayers {
    public static final ModelLayerLocation SPIDER_SMALL_LAYER =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath("verseplus", "spider_small"), "main");
    public static final ModelLayerLocation SPIDER_TINY_LAYER =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath("verseplus", "spider_tiny"), "main");
    public static final ModelLayerLocation SPIDER_MEDIUM_LAYER =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath("verseplus", "spider_medium"), "main");
}
