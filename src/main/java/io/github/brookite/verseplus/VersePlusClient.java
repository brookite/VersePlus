package io.github.brookite.verseplus;

import io.github.brookite.verseplus.enums.SpiderVariant;
import io.github.brookite.verseplus.registries.CustomEntityModelLayers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.render.entity.model.ModelTransformer;
import net.minecraft.client.render.entity.model.SpiderEntityModel;

public class VersePlusClient  implements ClientModInitializer {
    private void registerSpiders() {
        EntityModelLayerRegistry.registerModelLayer(
                CustomEntityModelLayers.SPIDER_TINY_LAYER,
                () -> SpiderEntityModel.getTexturedModelData().transform(
                        ModelTransformer.scaling(SpiderVariant.TINY.getScale()))
        );
        EntityModelLayerRegistry.registerModelLayer(
                CustomEntityModelLayers.SPIDER_SMALL_LAYER,
                () -> SpiderEntityModel.getTexturedModelData().transform(
                        ModelTransformer.scaling(SpiderVariant.SMALL.getScale()))
        );
        EntityModelLayerRegistry.registerModelLayer(
                CustomEntityModelLayers.SPIDER_MEDIUM_LAYER,
                () -> SpiderEntityModel.getTexturedModelData().transform(
                        ModelTransformer.scaling(SpiderVariant.MEDIUM.getScale()))
        );
    }

    @Override
    public void onInitializeClient() {
       registerSpiders();
    }
}
