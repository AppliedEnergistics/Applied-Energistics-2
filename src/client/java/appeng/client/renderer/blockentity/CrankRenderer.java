package appeng.client.renderer.blockentity;

import java.util.List;
import java.util.Objects;

import com.mojang.blaze3d.vertex.PoseStack;

import org.joml.Quaternionf;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.RenderTypeHelper;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;

import appeng.api.orientation.BlockOrientation;
import appeng.blockentity.misc.CrankBlockEntity;
import appeng.core.AppEng;

public class CrankRenderer implements BlockEntityRenderer<CrankBlockEntity> {

    public static final ResourceLocation HANDLE_MODEL_ID = AppEng.makeId("block/crank_handle");
    public static final StandaloneModelKey<SimpleModelWrapper> HANDLE_MODEL = new StandaloneModelKey<>(
            HANDLE_MODEL_ID::toString);

    private final BlockRenderDispatcher blockRenderer;

    private final ModelManager modelManager;

    public CrankRenderer(BlockEntityRendererProvider.Context context) {
        this.modelManager = context.getBlockRenderDispatcher().getBlockModelShaper().getModelManager();
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(CrankBlockEntity crank, float partialTick, PoseStack stack, MultiBufferSource buffers,
            int packedLight, int packedOverlay, Vec3 cameraPosition) {

        var handleModel = Objects.requireNonNull(modelManager.getStandaloneModel(HANDLE_MODEL));

        var blockState = crank.getBlockState();
        var pos = crank.getBlockPos();

        stack.pushPose();
        var rotation = new Quaternionf(BlockOrientation.get(crank).getQuaternion());
        // The base model points "up" towards positive Y by default, although the unrotated state would be facing north
        rotation.rotateX(Mth.DEG_TO_RAD * 270);
        rotation.rotateY(-Mth.DEG_TO_RAD * crank.getVisibleRotation());
        stack.rotateAround(rotation, 0.5f, 0.5f, 0.5f);

        blockRenderer.getModelRenderer().tesselateBlock(
                crank.getLevel(),
                List.of(handleModel),
                blockState,
                pos,
                stack,
                layer -> buffers.getBuffer(RenderTypeHelper.getEntityRenderType(layer)),
                false,
                packedOverlay);
        stack.popPose();
    }

}
