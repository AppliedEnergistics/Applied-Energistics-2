package appeng.client.guidebook.document.block.table;

import appeng.client.guidebook.document.block.LytTextAlignment;

public class LytTableColumn {
    private final int index;
    int x;
    int width;
    private LytTextAlignment alignment = LytTextAlignment.LEFT;

    public LytTableColumn(int index) {
        this.index = index;
    }

    public LytTextAlignment getAlignment() {
        return alignment;
    }

    public void setAlignment(LytTextAlignment alignment) {
        this.alignment = alignment;
    }
}
