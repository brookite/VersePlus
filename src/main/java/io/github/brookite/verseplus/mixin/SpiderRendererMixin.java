package io.github.brookite.verseplus.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.brookite.verseplus.interfaces.SizeableSpider;
import io.github.brookite.verseplus.registries.CustomEntityModelLayers;
import io.github.brookite.verseplus.renderers.state.SizedSpiderEntityRenderState;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.spider.SpiderModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.spider.Spider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(SpiderRenderer.class)
public abstract class SpiderRendererMixin<T extends Spider> extends MobRenderer<T, LivingEntityRenderState, SpiderModel> {
    private SpiderModel smallModel;
    private SpiderModel tinyModel;
    private SpiderModel mediumModel;
    private SpiderModel largeModel;

    public SpiderRendererMixin(EntityRendererProvider.Context context, SpiderModel entityModel, float f) {
        super(context, entityModel, f);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(EntityRendererProvider.Context context, CallbackInfo ci) {
        if (this.getClass().equals(SpiderRenderer.class)) {
            this.smallModel = new SpiderModel(context.bakeLayer(CustomEntityModelLayers.SPIDER_SMALL_LAYER));
            this.mediumModel = new SpiderModel(context.bakeLayer(CustomEntityModelLayers.SPIDER_MEDIUM_LAYER));
            this.tinyModel = new SpiderModel(context.bakeLayer(CustomEntityModelLayers.SPIDER_TINY_LAYER));
            this.largeModel = new SpiderModel(context.bakeLayer(ModelLayers.SPIDER));
            this.model = this.largeModel;
        }
    }

    @Inject(method= "extractRenderState(Lnet/minecraft/world/entity/monster/spider/Spider;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at=@At("TAIL"))
    public void updateRenderState(T spiderEntity, LivingEntityRenderState livingEntityRenderState, float f, CallbackInfo ci) {
        SizedSpiderEntityRenderState entityRenderState = (SizedSpiderEntityRenderState)livingEntityRenderState;
        SizeableSpider spider = (SizeableSpider)spiderEntity;
        entityRenderState.variant = spider.getVariant();
        switch(entityRenderState.variant) {
            case LARGE -> this.shadowRadius = 0.5F;
            case SMALL -> this.shadowRadius = 0.3F;
            case MEDIUM -> this.shadowRadius = 0.4F;
            case TINY -> this.shadowRadius = 0.2F;
        }
    }

    @Inject(method = "createRenderState()Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;", at = @At("HEAD"), cancellable = true)
    public void createRenderState(CallbackInfoReturnable<LivingEntityRenderState> cir) {
        cir.setReturnValue(new SizedSpiderEntityRenderState());
    }

    @Override
    public void submit(LivingEntityRenderState renderState, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        SizedSpiderEntityRenderState entityRenderState = (SizedSpiderEntityRenderState)renderState;
        if (this.getClass().equals(SpiderRenderer.class)) {
            this.model = switch (entityRenderState.variant) {
                case SMALL -> this.smallModel;
                case TINY -> this.tinyModel;
                case MEDIUM -> this.mediumModel;
                case LARGE -> this.largeModel;
            };
        }
        super.submit(renderState, matrices, queue, cameraState);
    }

    @Shadow
    public abstract Identifier getTextureLocation(LivingEntityRenderState state);
}
