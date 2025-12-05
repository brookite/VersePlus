package io.github.brookite.verseplus.renderers.state;

import io.github.brookite.verseplus.enums.SpiderVariant;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;

@Environment(EnvType.CLIENT)
public class SizedSpiderEntityRenderState extends LivingEntityRenderState {
    public SpiderVariant variant = SpiderVariant.LARGE;
}
