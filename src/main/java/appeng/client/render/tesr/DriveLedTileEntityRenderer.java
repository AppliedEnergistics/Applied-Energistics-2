package appeng.client.render.tesr;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.vector.Vector3f;
import appeng.client.render.FacingToRotation;
import appeng.client.render.model.DriveBakedModel;
import appeng.tile.storage.DriveTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

/**
 * Renders the drive cell status indicators.
 */
@Environment(EnvType.CLIENT)
public class DriveLedTileEntityRenderer extends TileEntityRenderer<DriveTileEntity> {

    public DriveLedTileEntityRenderer(TileEntityRendererDispatcher renderDispatcher) {
        super(renderDispatcher);
    }

    @Override
    public void render(DriveTileEntity drive, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffers,
                       int combinedLightIn, int combinedOverlayIn) {

        if (drive.getCellCount() != 10) {
            throw new IllegalStateException("Expected drive to have 10 slots");
        }

        ms.push();
        ms.translate(0.5, 0.5, 0.5);
        FacingToRotation.get(drive.getForward(), drive.getUp()).push(ms);
        ms.translate(-0.5, -0.5, -0.5);

        IVertexBuilder buffer = buffers.getBuffer(CellLedRenderer.RENDER_LAYER);

        Vector3f slotTranslation = new Vector3f();
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
