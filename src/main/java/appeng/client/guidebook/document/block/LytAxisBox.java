package appeng.client.guidebook.document.block;

/**
 * A box that just aligns its content along the vertical or horizontal axis.
 */
public abstract class LytAxisBox extends LytBox {
    private int gap;

    private AlignItems alignItems = AlignItems.START;

    public AlignItems getAlignItems() {
        return alignItems;
    }

    public void setAlignItems(AlignItems alignItems) {
        this.alignItems = alignItems;
    }

    public int getGap() {
        return gap;
    }

    public void setGap(int gap) {
        this.gap = gap;
    }
}
