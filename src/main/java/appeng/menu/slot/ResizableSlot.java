package appeng.menu.slot;

import appeng.api.inventories.InternalInventory;

public class ResizableSlot extends AppEngSlot {
    /**
     * Id of the widget in the style-sheet that will determine this slots position and size instead of the normal slot
     * positioning.
     */
    private final String styleId;
    private int width = 16;
    private int height = 16;

    public ResizableSlot(InternalInventory inv, int invSlot, String styleId) {
        super(inv, invSlot);
        this.styleId = styleId;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getStyleId() {
        return styleId;
    }
}
