package appeng.client.render.tesr;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3f;
import appeng.client.render.FacingToRotation;
import appeng.client.render.model.DriveBakedModel;
import appeng.tile.storage.DriveBlockEntity;

/**
 * Renders the drive cell status indicators.
 */
@Environment(EnvType.CLIENT)
public class DriveLedTileEntityRenderer extends BlockEntityRenderer<DriveBlockEntity> {

    public DriveLedTileEntityRenderer(BlockEntityRenderDispatcher renderDispatcher) {
        super(renderDispatcher);
    }

    @Override
    public void render(DriveBlockEntity drive, float partialTicks, MatrixStack ms, VertexConsumerProvider buffers,
            int combinedLightIn, int combinedOverlayIn) {

        if (drive.getCellCount() != 10) {
            throw new IllegalStateException("Expected drive to have 10 slots");
        }

        ms.push();
        ms.translate(0.5, 0.5, 0.5);
        FacingToRotation.get(drive.getForward(), drive.getUp()).push(ms);
        ms.translate(-0.5, -0.5, -0.5);

        VertexConsumer buffer = buffers.getBuffer(CellLedRenderer.RENDER_LAYER);

        Vec3f slotTranslation = new Vec3f();
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 2; col++) {
                ms.push();

                DriveBakedModel.getSlotOrigin(row, col, slotTranslation);
                ms.translate(slotTranslation.getX(), slotTranslation.getY(), slotTranslation.getZ());

                int slot = row * 2 + col;
                CellLedRenderer.renderLed(drive, slot, buffer, ms, partialTicks);

                ms.pop();
            }
        }

        ms.pop();
    }

}
