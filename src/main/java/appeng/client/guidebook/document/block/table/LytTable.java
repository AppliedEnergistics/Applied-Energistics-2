package appeng.client.guidebook.document.block.table;

import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.document.block.LytBlock;
import appeng.client.guidebook.layout.LayoutContext;
import appeng.client.guidebook.render.RenderContext;
import appeng.client.guidebook.render.SymbolicColor;
import net.minecraft.client.renderer.MultiBufferSource;

import java.util.ArrayList;
import java.util.List;

public class LytTable extends LytBlock {
    /**
     * Width of border around cells.
     */
    private static final int CELL_BORDER = 1;
    private final List<LytTableRow> rows = new ArrayList<>();

    private final List<LytTableColumn> columns = new ArrayList<>();

    @Override
    protected LytRect computeLayout(LayoutContext context, int x, int y, int availableWidth) {
        if (columns.isEmpty()) {
            return LytRect.empty();
        }

        // Distribute available width evenly between columns
        var cellWidth = (availableWidth - (columns.size() + 1) * CELL_BORDER) / columns.size();
        var colX = x + CELL_BORDER;
        for (var column : columns) {
            column.x = colX;
            column.width = cellWidth;
            colX += column.width + CELL_BORDER;
        }

        // Ensure the last column fills the entire width (fixes rounding off-by-one issues)
        var lastCol = columns.get(columns.size() - 1);
        lastCol.width = (x + availableWidth) - lastCol.x - CELL_BORDER;

        // Layout each row
        var currentY = y + CELL_BORDER;
        for (var row : rows) {
            var rowTop = currentY;
            var rowBottom = currentY;
            for (var cell : row.getChildren()) {
                var column = cell.column;
                var cellBounds = cell.layout(context, column.x, currentY, column.width);
                rowBottom = Math.max(rowBottom, cellBounds.bottom());
            }
            row.bounds = new LytRect(x, rowTop, availableWidth, rowBottom - rowTop);
            currentY = rowBottom + CELL_BORDER;
        }

        return new LytRect(
                x, y,
                availableWidth,
                currentY - y
        );
    }

    @Override
    public void renderBatch(RenderContext context, MultiBufferSource buffers) {
        for (var row : getChildren()) {
            for (var cell : row.getChildren()) {
                cell.renderBatch(context, buffers);
            }
        }
    }

    @Override
    public void render(RenderContext context) {
        // Render the table cell borders
        var bounds = getBounds();
        for (int i = 0; i < columns.size() - 1; i++) {
            var column = columns.get(i);
            if (i == 0) {
                //context.fillRect(column.x - 1, bounds.y(), 1, bounds.height(), SymbolicColor.TABLE_BORDER.ref());
            }
            var colRight = column.x + column.width;
            context.fillRect(colRight, bounds.y(), 1, bounds.height(), SymbolicColor.TABLE_BORDER.ref());
        }

        for (int i = 0; i < rows.size() - 1; i++) {
            var row = rows.get(i);

            if (i == 0) {
                //context.fillRect(bounds.x(), row.bounds.y() - 1, bounds.width(), 1, SymbolicColor.TABLE_BORDER.ref());
            }
            context.fillRect(bounds.x(), row.bounds.bottom(), bounds.width(), 1, SymbolicColor.TABLE_BORDER.ref());
        }

        for (var row : rows) {
            for (var cell : row.getChildren()) {
                cell.render(context);
            }
        }
    }

    public LytTableRow appendRow() {
        var row = new LytTableRow(this);
        rows.add(row);
        return row;
    }

    public List<LytTableColumn> getColumns() {
        return columns;
    }

    public LytTableColumn getOrCreateColumn(int index) {
        while (index >= columns.size()) {
            columns.add(new LytTableColumn());
        }
        return columns.get(index);
    }

    @Override
    public List<LytTableRow> getChildren() {
        return rows;
    }
}

