package appeng.client.guidebook.document.block.table;

import appeng.client.guidebook.document.block.LytVBox;

/**
 * A cell in a {@link LytTable}s {@link LytTableRow}.
 */
public class LytTableCell extends LytVBox {
    final LytTable table;
    final LytTableRow row;
    final LytTableColumn column;

    public LytTableCell(LytTable table, LytTableRow row, LytTableColumn column) {
        this.table = table;
        this.row = row;
        this.column = column;
        this.parent = row;

        paddingLeft = 1;
        paddingTop = 1;
        paddingRight = 1;
        paddingBottom = 1;
    }
}
