package appeng.client.gui.style;

import appeng.client.gui.layout.SlotGridLayout;

import javax.annotation.Nullable;

/**
 * Describes positioning for a slot.
 */
public class SlotPosition extends Position {

    @Nullable
    private SlotGridLayout grid;

    @Nullable
    public SlotGridLayout getGrid() {
        return grid;
    }

    public void setGrid(@Nullable SlotGridLayout grid) {
        this.grid = grid;
    }

    @Override
    public String toString() {
        String result = super.toString();
        return grid != null ? (result + "grid=" + grid) : result;
    }
}
