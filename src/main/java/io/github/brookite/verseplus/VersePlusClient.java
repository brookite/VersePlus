package io.github.brookite.verseplus;

import io.github.brookite.verseplus.enums.SpiderVariant;
import io.github.brookite.verseplus.registries.CustomEntityModelLayers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.monster.spider.SpiderModel;

public class VersePlusClient  implements ClientModInitializer {
    private void registerSpiders() {
        ModelLayerRegistry.registerModelLayer(
                CustomEntityModelLayers.SPIDER_TINY_LAYER,
                () -> SpiderModel.createSpiderBodyLayer().apply(
                        MeshTransformer.scaling(SpiderVariant.TINY.getScale()))
        );
        ModelLayerRegistry.registerModelLayer(
                CustomEntityModelLayers.SPIDER_SMALL_LAYER,
                () -> SpiderModel.createSpiderBodyLayer().apply(
                        MeshTransformer.scaling(SpiderVariant.SMALL.getScale()))
        );
        ModelLayerRegistry.registerModelLayer(
                CustomEntityModelLayers.SPIDER_MEDIUM_LAYER,
                () -> SpiderModel.createSpiderBodyLayer().apply(
                        MeshTransformer.scaling(SpiderVariant.MEDIUM.getScale()))
        );
    }

    @Override
    public void onInitializeClient() {
       registerSpiders();
    }
}
