package io.github.brookite.verseplus.mixin;

import io.github.brookite.verseplus.interfaces.SizeableSpider;
import io.github.brookite.verseplus.registries.CustomEntityModelLayers;
import io.github.brookite.verseplus.renderers.state.SizedSpiderEntityRenderState;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.SpiderEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.SpiderEntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(SpiderEntityRenderer.class)
public abstract class SpiderRendererMixin<T extends SpiderEntity> extends MobEntityRenderer<T, LivingEntityRenderState, SpiderEntityModel> {
    private SpiderEntityModel smallModel;
    private SpiderEntityModel tinyModel;
    private SpiderEntityModel mediumModel;
    private SpiderEntityModel largeModel;

    public SpiderRendererMixin(EntityRendererFactory.Context context, SpiderEntityModel entityModel, float f) {
        super(context, entityModel, f);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(EntityRendererFactory.Context context, CallbackInfo ci) {
        if (this.getClass().equals(SpiderEntityRenderer.class)) {
            this.smallModel = new SpiderEntityModel(context.getPart(CustomEntityModelLayers.SPIDER_SMALL_LAYER));
            this.mediumModel = new SpiderEntityModel(context.getPart(CustomEntityModelLayers.SPIDER_MEDIUM_LAYER));
            this.tinyModel = new SpiderEntityModel(context.getPart(CustomEntityModelLayers.SPIDER_TINY_LAYER));
            this.largeModel = new SpiderEntityModel(context.getPart(EntityModelLayers.SPIDER));
            this.model = this.largeModel;
        }
    }

    @Inject(method= "updateRenderState(Lnet/minecraft/entity/mob/SpiderEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V", at=@At("TAIL"))
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

    @Inject(method = "createRenderState()Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;", at = @At("HEAD"), cancellable = true)
    public void createRenderState(CallbackInfoReturnable<LivingEntityRenderState> cir) {
        cir.setReturnValue(new SizedSpiderEntityRenderState());
    }

    @Override
    public void render(LivingEntityRenderState renderState, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        SizedSpiderEntityRenderState entityRenderState = (SizedSpiderEntityRenderState)renderState;
        if (this.getClass().equals(SpiderEntityRenderer.class)) {
            this.model = switch (entityRenderState.variant) {
                case SMALL -> this.smallModel;
                case TINY -> this.tinyModel;
                case MEDIUM -> this.mediumModel;
                case LARGE -> this.largeModel;
            };
        }
        super.render(renderState, matrices, queue, cameraState);
    }

    @Shadow
    public abstract Identifier getTexture(LivingEntityRenderState state);
}
