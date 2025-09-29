package appeng.client.renderer.blockentity;

import java.util.Objects;

import com.mojang.blaze3d.vertex.PoseStack;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;

import appeng.blockentity.misc.CrankBlockEntity;
import appeng.core.AppEng;

public class CrankRenderer implements BlockEntityRenderer<CrankBlockEntity, CrankRenderState> {

    public static final ResourceLocation HANDLE_MODEL_ID = AppEng.makeId("block/crank_handle");
    public static final StandaloneModelKey<SimpleModelWrapper> HANDLE_MODEL = new StandaloneModelKey<>(
            HANDLE_MODEL_ID::toString);

    private final BlockRenderDispatcher blockRenderer;

    private final ModelManager modelManager;

    public CrankRenderer(BlockEntityRendererProvider.Context context) {
        this.modelManager = context.blockRenderDispatcher().getBlockModelShaper().getModelManager();
        this.blockRenderer = context.blockRenderDispatcher();
    }

    @Override
    public CrankRenderState createRenderState() {
        return new CrankRenderState();
    }

    @Override
    public void extractRenderState(CrankBlockEntity be, CrankRenderState state, float partialTicks, Vec3 cameraPos,
            @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPos, crumblingOverlay);
    }

    @Override
    public void submit(CrankRenderState state, PoseStack poseStack, SubmitNodeCollector nodes,
            CameraRenderState cameraRenderState) {
        var handleModel = Objects.requireNonNull(modelManager.getStandaloneModel(HANDLE_MODEL));

        // TODO 1.21.9 var blockState = crank.getBlockState();
        // TODO 1.21.9 var pos = crank.getBlockPos();
// TODO 1.21.9
        // TODO 1.21.9 stack.pushPose();
        // TODO 1.21.9 var rotation = new Quaternionf(BlockOrientation.get(crank).getQuaternion());
        // TODO 1.21.9 // The base model points "up" towards positive Y by default, although the unrotated state would
        // be facing north
        // TODO 1.21.9 rotation.rotateX(Mth.DEG_TO_RAD * 270);
        // TODO 1.21.9 rotation.rotateY(-Mth.DEG_TO_RAD * crank.getVisibleRotation());
        // TODO 1.21.9 stack.rotateAround(rotation, 0.5f, 0.5f, 0.5f);
// TODO 1.21.9
        // TODO 1.21.9 blockRenderer.getModelRenderer().tesselateBlock(
        // TODO 1.21.9 crank.getLevel(),
        // TODO 1.21.9 List.of(handleModel),
        // TODO 1.21.9 blockState,
        // TODO 1.21.9 pos,
        // TODO 1.21.9 stack,
        // TODO 1.21.9 layer -> buffers.getBuffer(RenderTypeHelper.getEntityRenderType(layer)),
        // TODO 1.21.9 false,
        // TODO 1.21.9 packedOverlay);
        // TODO 1.21.9 stack.popPose();
    }

}
