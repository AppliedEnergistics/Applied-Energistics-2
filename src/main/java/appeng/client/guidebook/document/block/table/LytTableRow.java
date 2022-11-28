package appeng.client.guidebook.document.block.table;

import java.util.ArrayList;
import java.util.List;

import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.document.block.LytNode;

/**
 * A row in {@link LytTable}. Contains {@link LytTableCell}.
 */
public class LytTableRow extends LytNode {
    private final LytTable table;
    private final List<LytTableCell> cells = new ArrayList<>();
    LytRect bounds = LytRect.empty();

    public LytTableRow(LytTable table) {
        this.table = table;
        this.parent = table;
    }

    @Override
    public LytRect getBounds() {
        return bounds;
    }

    public LytTableCell appendCell() {
        var cell = new LytTableCell(table, this, table.getOrCreateColumn(cells.size()));
        cells.add(cell);
        return cell;
    }

    @Override
    public List<LytTableCell> getChildren() {
        return cells;
    }
}
