package uristqwerty.CraftGuide.client.ui;

import uristqwerty.CraftGuide.CraftGuide;
import uristqwerty.CraftGuide.client.ui.GuiScrollBar.ScrollBarAlignmentCallback;
import uristqwerty.CraftGuide.client.ui.Rendering.GridRect;
import uristqwerty.gui_craftguide.components.GuiElement;

public abstract class GuiScrollableGrid extends GuiElement implements ScrollBarAlignmentCallback
{
	protected GuiScrollBar scrollBar;
	protected int rowHeight;

	private GridRect display;
	private int rows = 0, columns = 1, cells = 0;
	private int columnWidth = 1;
	private int reducedWidth = 0;

	public int borderSize = 1;
	public boolean flexibleSize = false;

	private int lastMouseX, lastMouseY;
	private float lastScroll;

	public GuiScrollableGrid(int x, int y, int width, int height, GuiScrollBar scrollBar, int rowHeight, int columnWidth)
	{
		super(x, y, width, height);
		this.display = new GridRect(0, 0, width, height, this);
		this.rowHeight = rowHeight;
		this.scrollBar = scrollBar;
		this.columnWidth = columnWidth;

		scrollBar.setAlignmentCallback(this);
		scrollBar.setPageSize((height / rowHeight) * rowHeight);

		recalculateColumns();
	}

	@Override
	public void draw()
	{
		if(lastScroll != scrollBar.getValue())
		{
			lastScroll = scrollBar.getValue();
			mouseMoved(lastMouseX, lastMouseY);
		}

		if(flexibleSize)
		{
			int width = columnWidth * ((bounds.width() + reducedWidth) / columnWidth);

			if(width != bounds.width())
			{
				int xOff = (reducedWidth / 2) &~1;
				reducedWidth = bounds.width() + reducedWidth - width;

				setPosition(bounds.x() - xOff + ((reducedWidth / 2) &~1), bounds.y());
				setSize(width, bounds.height());
			}
		}

		drawBackground();
		render(display);
		drawChildren();
	}

	@Override
	public void drawBackground()
	{
		if(background != null)
		{
			render(background, -borderSize, -borderSize, bounds.width() + borderSize * 2, bounds.height() + borderSize * 2);
		}
	}

	@Override
	public void mouseMoved(int x, int y)
	{
		int gridY = y - bounds.y();
		int scrollY = pixelsScrolledForY(gridY);
		int row = rowAtY(gridY);

		lastMouseX = x;
		lastMouseY = y;
		lastScroll = scrollBar.getValue();

		mouseMovedRow(row, x - bounds.x(), scrollY - rowStartPixels(row), containsPoint(x, y));

		super.mouseMoved(x, y);
	}

	@Override
	public void mousePressed(int x, int y)
	{
		int gridY = y - bounds.y();
		int row = rowAtY(gridY);
		int rowPos = pixelsScrolledForY(gridY) - rowStartPixels(row);

		rowClicked(row, x - bounds.x(), rowPos, containsPoint(x, y));
		super.mousePressed(x, y);
	}

	/**
	 * @return Number of UI pixels between the top of the first
	 *  row, and the start of the requested row.
	 */
	protected int rowStartPixels(int row)
	{
		return row * rowHeight;
	}

	/**
	 * @return Number of UI pixels between the top of the first
	 *  row, and .
	 */
	protected int pixelsScrolledForScreenY(int y)
	{
		return ((int)scrollBar.getValue()) + y - absoluteY();
	}

	protected int pixelsScrolledForY(int y)
	{
		return ((int)scrollBar.getValue()) + y;
	}

	protected int rowAtScreenY(int y)
	{
		return pixelsScrolledForScreenY(y) / rowHeight;
	}

	protected int rowAtGridY(int y)
	{
		return y / rowHeight;
	}

	protected int rowAtY(int y)
	{
		return pixelsScrolledForY(y) / rowHeight;
	}

	protected int getHeightForRow(int row)
	{
		return rowHeight;
	}

	@Override
	public void onResize(int oldWidth, int oldHeight)
	{
		scrollBar.setPageSize((bounds.height() / rowHeight) * rowHeight);
		display.setSize(bounds.width(), bounds.height());


		if(flexibleSize)
		{
			int width = columnWidth * ((bounds.width() + reducedWidth) / columnWidth);

			if(width != bounds.width())
			{
				int xOff = reducedWidth / 2;
				reducedWidth = bounds.width() + reducedWidth - width;

				setPosition(bounds.x() - xOff + reducedWidth / 2, bounds.y());
				setSize(width, bounds.height());
			}
		}

		recalculateColumns();
		scrollBar.setPageSize(bounds.height());
	}

	public void recalculateColumns()
	{
		setColumns(Math.max(bounds.width() / columnWidth, 1));
	}

	public void setRows(int rowCount)
	{
		rows = rowCount;

		recalculateRowHeight();
		updateScrollbarScale();
	}

	public void updateScrollbarScale()
	{
		float end = rowStartPixels(rows - 1) + getHeightForRow(rows - 1) - bounds.height();

		if(end < 0)
		{
			end = 0;
		}

		scrollBar.setRowSize(rowHeight);
		scrollBar.setScale(0, end);
	}

	protected void recalculateRowHeight()
	{
		int maxHeight = 1;
		for(int i = 0; i < getCells(); i++)
		{
			maxHeight = Math.max(maxHeight, getMinCellHeight(i));
		}

		setRowHeight(maxHeight);
	}

	public void setColumns()
	{
		setColumns(bounds.width() / columnWidth);
	}

	public void setColumnWidth(int newWidth)
	{
		columnWidth = newWidth;
		recalculateColumns();
	}

	public void setRowHeight(int newHeight)
	{
		rowHeight = newHeight;
		updateScrollbarScale();
	}

	public void setColumns(int columns)
	{
		this.columns = columns;
		setRows((getCells() + columns - 1) / columns);
	}

	protected int getColumns()
	{
		return columns;
	}

	public void setCells(int cells)
	{
		this.cells = cells;
		setRows((cells + getColumns() - 1) / getColumns());
	}

	protected int getCells()
	{
		return cells;
	}

	public void renderGridRows(GuiRenderer renderer, int xOffset, int yOffset)
	{
		int yMin = pixelsScrolledForScreenY(yOffset);
		int row = rowAtScreenY(yOffset + 1);

		while(row < rowCount())
		{
			int y = rowStartPixels(row) - yMin;

			if(y >= bounds.height())
			{
				break;
			}

			renderGridRow(renderer, xOffset, y + yOffset, row);
			row++;
		}
	}

	public void renderGridRow(GuiRenderer renderer, int xOffset, int yOffset, int row)
	{
		for(int i = 0; i < getColumns(); i++)
		{
			int columnX = columnOffset(i);

			renderGridCell(renderer, xOffset + columnX, yOffset, row * getColumns() + i);
		}
	}

	public int columnOffset(int column)
	{
		if(CraftGuide.gridPacking)
		{
			return column * columnWidth;
		}
		else
		{
			return getColumns() < 2? 0 : (int)((bounds.width() - columnWidth) * column / (float)(getColumns() - 1));
		}
	}

	protected int columnAtX(int x)
	{
		if(CraftGuide.gridPacking)
		{
			return Math.min(x / columnWidth, getColumns() - 1);
		}
		else
		{
			return (x * getColumns()) / bounds.width();
		}
	}

	public void rowClicked(int row, int x, int y, boolean inBounds)
	{
		int column = columnAtX(x);
		int columnX = columnOffset(column);

		if(inBounds && x - columnX < columnWidth && row * getColumns() + column < getCells())
		{
			cellClicked(row * getColumns() + column, x - columnX, y);
		}
	}

	public void mouseMovedRow(int row, int x, int y, boolean inBounds)
	{
		int column = columnAtX(x);

		if(column >= 0 && row * getColumns() + column < getCells())
		{
			int columnX = columnOffset(column);

			if(x >= columnX && x - columnX < columnWidth)
			{
				mouseMovedCell(row * getColumns() + column, x - columnX, y, inBounds);
			}
		}
	}

	public int cellAtCoords(int x, int y)
	{
		int row = rowAtY(y);
		return columnAtX(x) + row * getColumns();
	}

	public int rowCount()
	{
		return rows;
	}

	public int firstVisibleRow()
	{
		return rowAtScreenY(absoluteY());
	}

	public int lastVisibleRow()
	{
		return rowAtScreenY(absoluteY() + bounds.height());
	}

	@Override
	public float alignScrollBar(GuiScrollBar guiScrollBar, float oldValue, float newValue)
	{
		float alignedNewValue = rowStartPixels(rowAtGridY((int)newValue));

		if(newValue > oldValue && alignedNewValue <= oldValue)
		{
			alignedNewValue = rowStartPixels(rowAtGridY((int)newValue) + 1);
		}
		else if(newValue < oldValue && alignedNewValue >= oldValue)
		{
			alignedNewValue = rowStartPixels(rowAtGridY((int)newValue) - 1);
		}

		return alignedNewValue;
	}

	public void mouseMovedCell(int cell, int x, int y, boolean inBounds)
	{
		/** Default implementation: Do nothing */
	}

	public void cellClicked(int cell, int x, int y)
	{
		/** Default implementation: Do nothing */
	}

	/**
	 * Called by the default implementation of {@link renderGridRow} one
	 * time for each column in the row. Override this in order to render
	 * each cell individually.
	 * <br><br>
	 * (x, y) is the absolute screen position of this cell's top left corner.
	 * @param renderer
	 * @param x
	 * @param y
	 * @param cell [previous cells in this row] + ([number of previous rows]
	 * * [cells per row])
	 */
	public void renderGridCell(GuiRenderer renderer, int x, int y, int cell)
	{
		/** Default implementation: Do nothing */
	}

	abstract protected int getMinCellHeight(int i);
}
