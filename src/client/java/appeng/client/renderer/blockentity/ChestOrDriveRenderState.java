package appeng.client.renderer.blockentity;

import java.util.EnumMap;

import org.joml.Vector3f;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

import appeng.api.implementations.blockentities.IChestOrDrive;
import appeng.api.orientation.BlockOrientation;
import appeng.api.storage.cells.CellState;

public class ChestOrDriveRenderState extends BlockEntityRenderState {
    private static final EnumMap<CellState, Vector3f> STATE_COLORS;

    // Color to use if the cell is present but unpowered
    private static final Vector3f UNPOWERED_COLOR = new Vector3f(0, 0, 0);

    // Color used for the cell indicator for blinking during recent activity
    private static final Vector3f BLINK_COLOR = new Vector3f(1, 0.5f, 0.5f);

    static {
        STATE_COLORS = new EnumMap<>(CellState.class);
        for (var cellState : CellState.values()) {
            var color = cellState.getStateColor();
            var colorVector = new Vector3f(
                    ((color >> 16) & 0xFF) / 255.0f,
                    ((color >> 8) & 0xFF) / 255.0f,
                    (color & 0xFF) / 255.0f);
            STATE_COLORS.put(cellState, colorVector);
        }
    }

    public BlockOrientation blockOrientation;
    public Vector3f[] cellColors;

    public void extract(BlockOrientation blockOrientation, IChestOrDrive drive, float partialTicks) {
        this.blockOrientation = blockOrientation;

        if (cellColors == null || cellColors.length != drive.getCellCount()) {
            cellColors = new Vector3f[drive.getCellCount()];
        }

        for (int i = 0; i < drive.getCellCount(); i++) {
            cellColors[i] = getColorForSlot(drive, i, partialTicks);
        }
    }

    private static Vector3f getColorForSlot(IChestOrDrive drive, int slot, float partialTicks) {
        var state = drive.getCellStatus(slot);
        if (state == CellState.ABSENT) {
            return null;
        }

        if (!drive.isPowered()) {
            return UNPOWERED_COLOR;
        }

        Vector3f col = STATE_COLORS.get(state);
        if (drive.isCellBlinking(slot)) {
            // 200 ms interval (100ms to get to red, then 100ms back)
            long t = System.currentTimeMillis() % 200;
            float f = (t - 100) / 200.0f + 0.5f;
            f = easeInOutCubic(f);
            col = new Vector3f(col);
            col.lerp(BLINK_COLOR, f);
        }

        return col;
    }

    private static float easeInOutCubic(float x) {
        return x < 0.5f ? 4 * x * x * x : 1 - (float) Math.pow(-2 * x + 2, 3) / 2;
    }
}
