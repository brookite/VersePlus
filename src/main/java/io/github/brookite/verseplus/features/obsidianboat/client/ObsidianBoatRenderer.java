package io.github.brookite.verseplus.features.obsidianboat.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.brookite.verseplus.VersePlus;
import io.github.brookite.verseplus.features.obsidianboat.ObsidianBoatEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.AbstractBoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;

public class ObsidianBoatRenderer extends AbstractBoatRenderer {
    private final Model.Simple lavaPatchModel;
    private final EntityModel<BoatRenderState> model;

    public ObsidianBoatRenderer(EntityRendererProvider.Context context) {
        super(context, Identifier.fromNamespaceAndPath(VersePlus.MOD_ID, "textures/entity/boat/obsidian.png"));
        lavaPatchModel = new Model.Simple(context.bakeLayer(ModelLayers.BOAT_WATER_PATCH), ignored -> RenderTypes.waterMask());
        model = new BoatModel(context.bakeLayer(ModelLayers.OAK_BOAT));
    }

    @Override
    protected EntityModel<BoatRenderState> model() {
        return model;
    }

    @Override
    public BoatRenderState createRenderState() {
        return new ObsidianBoatRenderState();
    }

    @Override
    public void extractRenderState(AbstractBoat entity, BoatRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        if (entity instanceof ObsidianBoatEntity boat && state instanceof ObsidianBoatRenderState obsidianState) {
            obsidianState.sinkDepth = boat.getSinkDepth();
        }
    }

    @Override
    public void submit(BoatRenderState state, PoseStack poseStack, SubmitNodeCollector nodes, CameraRenderState camera) {
        poseStack.pushPose();
        if (state instanceof ObsidianBoatRenderState obsidianState) {
            poseStack.translate(0.0F, -obsidianState.sinkDepth, 0.0F);
        }
        super.submit(state, poseStack, nodes, camera);
        poseStack.popPose();
    }

    @Override
    protected void submitTypeAdditions(BoatRenderState state, PoseStack poseStack, SubmitNodeCollector nodes, int light) {
        if (!state.isUnderWater) {
            nodes.submitModel(lavaPatchModel, Unit.INSTANCE, poseStack, texture, light,
                    OverlayTexture.NO_OVERLAY, state.outlineColor, null);
        }
    }

    private static final class ObsidianBoatRenderState extends BoatRenderState {
        private float sinkDepth;
    }
}
