package io.github.brookite.verseplus.features.obsidianboat.client;

import io.github.brookite.verseplus.VersePlus;
import io.github.brookite.verseplus.features.obsidianboat.ObsidianBoatEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.renderer.entity.AbstractBoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;

import java.util.List;

public class ObsidianBoatRenderer extends AbstractBoatRenderer {
    private final EntityModel<BoatRenderState> model;

    public ObsidianBoatRenderer(EntityRendererProvider.Context context) {
        super(context, Identifier.fromNamespaceAndPath(VersePlus.MOD_ID, "textures/entity/boat/obsidian.png"));
        model = new ObsidianBoatModel(context.bakeLayer(ModelLayers.OAK_BOAT));
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

    private static final class ObsidianBoatModel extends BoatModel {
        private static final float MODEL_UNITS_PER_BLOCK = 16.0F;
        private final List<ModelPart> sinkingParts;

        private ObsidianBoatModel(ModelPart root) {
            super(root);
            sinkingParts = List.of(
                    root.getChild("back"),
                    root.getChild("front"),
                    root.getChild("right"),
                    root.getChild("left"),
                    root.getChild("left_paddle"),
                    root.getChild("right_paddle")
            );
        }

        @Override
        public void setupAnim(BoatRenderState state) {
            super.setupAnim(state);
            if (state instanceof ObsidianBoatRenderState obsidianState) {
                float modelSinkDepth = obsidianState.sinkDepth * MODEL_UNITS_PER_BLOCK;
                for (ModelPart part : sinkingParts) {
                    part.y += modelSinkDepth;
                }
            }
        }
    }

    private static final class ObsidianBoatRenderState extends BoatRenderState {
        private float sinkDepth;
    }
}
