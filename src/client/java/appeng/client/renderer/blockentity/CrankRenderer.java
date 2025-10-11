package appeng.client.renderer.blockentity;

import java.util.Objects;

import com.mojang.blaze3d.vertex.PoseStack;

import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.block.model.SingleVariant;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.RenderTypeHelper;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;

import appeng.api.orientation.BlockOrientation;
import appeng.blockentity.misc.CrankBlockEntity;
import appeng.core.AppEng;

public class CrankRenderer implements BlockEntityRenderer<CrankBlockEntity, CrankRenderState> {

    public static final ResourceLocation HANDLE_MODEL_ID = AppEng.makeId("block/crank_handle");
    public static final StandaloneModelKey<SimpleModelWrapper> HANDLE_MODEL = new StandaloneModelKey<>(
            HANDLE_MODEL_ID::toString);

    private final BlockStateModel handleModel;

    public CrankRenderer(BlockEntityRendererProvider.Context context) {
        var modelManager = context.blockRenderDispatcher().getBlockModelShaper().getModelManager();
        handleModel = new SingleVariant(Objects.requireNonNull(modelManager.getStandaloneModel(HANDLE_MODEL)));
    }

    @Override
    public CrankRenderState createRenderState() {
        return new CrankRenderState();
    }

    @Override
    public void extractRenderState(CrankBlockEntity be, CrankRenderState state, float partialTicks, Vec3 cameraPos,
            @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPos, crumblingOverlay);

        state.orientation = BlockOrientation.get(be);
        state.visibleRotation = be.getVisibleRotation();
    }

    @Override
    public void submit(CrankRenderState state, PoseStack poseStack, SubmitNodeCollector nodes,
            CameraRenderState cameraRenderState) {

        poseStack.pushPose();
        var rotation = new Quaternionf(state.orientation.getQuaternion());
        // The base model points "up" towards positive Y by default, although the unrotated state would
        // be facing north
        rotation.rotateX(Mth.DEG_TO_RAD * 270);
        rotation.rotateY(-Mth.DEG_TO_RAD * state.visibleRotation);
        poseStack.rotateAround(rotation, 0.5f, 0.5f, 0.5f);

        nodes.submitBlockModel(
                poseStack,
                RenderTypeHelper.getEntityRenderType(ChunkSectionLayer.SOLID),
                handleModel,
                1, 1, 1,
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                0);

        poseStack.popPose();
    }

}
