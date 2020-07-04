package appeng.client.render.tesr;

import java.util.EnumMap;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import appeng.block.storage.DriveSlotState;
import appeng.client.render.FacingToRotation;
import appeng.tile.storage.DriveBlockEntity;

/**
 * Renders the drive cell status indicators.
 */
@Environment(EnvType.CLIENT)
public class DriveLedTileEntityRenderer extends BlockEntityRenderer<DriveBlockEntity> {

    private static final EnumMap<DriveSlotState, Vector3f> STATE_COLORS;

    // Color used for the cell indicator for blinking during recent activity
    private static final Vector3f BLINK_COLOR = new Vector3f(1, 0.5f, 0.5f);

    static {
        STATE_COLORS = new EnumMap<>(DriveSlotState.class);
        STATE_COLORS.put(DriveSlotState.OFFLINE, new Vector3f(0, 0, 0));
        STATE_COLORS.put(DriveSlotState.ONLINE, new Vector3f(0, 1, 0));
        STATE_COLORS.put(DriveSlotState.NOT_EMPTY, new Vector3f(0f, 0.667f, 1));
        STATE_COLORS.put(DriveSlotState.TYPES_FULL, new Vector3f(1, 0.667f, 0));
        STATE_COLORS.put(DriveSlotState.FULL, new Vector3f(1, 0, 0));
    }

    private static final float L = 14 / 16.f; // left (x-axis)
    private static final float R = 13 / 16.f; // right (x-axis)
    private static final float T = 14 / 16.f; // top (x-axis)
    private static final float B = 12.999f / 16.f; // bottom (x-axis)
    private static final float FR = 0.999f / 16.f; // front (z-axis)
    private static final float BA = 1.499f / 16.f; // back (z-axis)

    // Vertex data for the LED cuboid (has no back)
    // Directions are when looking from the front onto the LED
    private static final float[] LED_QUADS = {
            // Front Face
            R, T, FR, L, T, FR, L, B, FR, R, B, FR,
            // Left Face
            L, T, FR, L, T, BA, L, B, BA, L, B, FR,
            // Right Face
            R, T, BA, R, T, FR, R, B, FR, R, B, BA,
            // Top Face
            R, T, BA, L, T, BA, L, T, FR, R, T, FR,
            // Bottom Face
            R, B, FR, L, B, FR, L, B, BA, R, B, BA, };

    private static final RenderLayer STATE = RenderLayer.of("ae_drive_leds", VertexFormats.POSITION_COLOR, 7,
            32565, false, true, RenderLayer.MultiPhaseParameters.builder().build(false));

    public DriveLedTileEntityRenderer(BlockEntityRenderDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
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

        VertexConsumer buffer = buffers.getBuffer(STATE);

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 2; col++) {
                int slot = row * 2 + col;
                Vector3f color = getColorForSlot(drive, slot, partialTicks);
                if (color == null) {
                    continue;
                }

                ms.push();

                // Position this drive model copy at the correct slot. The transform is based on
                // the
                // cell-model being in slot 0,0 at the top left of the drive.
                float xOffset = -col * 8 / 16.0f;
                float yOffset = -row * 3 / 16.0f;
                ms.translate(xOffset, yOffset, 0);

                for (int i = 0; i < LED_QUADS.length; i += 3) {
                    float x = LED_QUADS[i];
                    float y = LED_QUADS[i + 1];
                    float z = LED_QUADS[i + 2];
                    buffer.vertex(ms.peek().getModel(), x, y, z).color(color.getX(), color.getY(), color.getZ(), 1.f)
                            .next();
                }

                ms.pop();
            }
        }

        ms.pop();
    }

    private Vector3f getColorForSlot(DriveBlockEntity drive, int slot, float partialTicks) {
        DriveSlotState state = DriveSlotState.fromCellStatus(drive.getCellStatus(slot));
        if (state == DriveSlotState.EMPTY) {
            return null;
        }

        if (!drive.isPowered()) {
            return STATE_COLORS.get(DriveSlotState.OFFLINE);
        }

        Vector3f col = STATE_COLORS.get(state);
        if (drive.isCellBlinking(slot)) {
            // 200 ms interval (100ms to get to red, then 100ms back)
            long t = System.currentTimeMillis() % 200;
            float f = (t - 100) / 200.0f + 0.5f;
            f = easeInOutCubic(f);
            col = col.copy();
            col.lerp(BLINK_COLOR, f);
        }

        return col;
    }

    private static float easeInOutCubic(float x) {
        return x < 0.5f ? 4 * x * x * x : 1 - (float) Math.pow(-2 * x + 2, 3) / 2;
    }

}
