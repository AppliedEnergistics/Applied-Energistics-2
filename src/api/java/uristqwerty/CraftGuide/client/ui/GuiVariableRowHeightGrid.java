package uristqwerty.CraftGuide.client.ui;

import java.util.Arrays;


public abstract class GuiVariableRowHeightGrid extends GuiScrollableGrid
{
	private int[] rowHeights;
	private int[] rowStartY;

	public GuiVariableRowHeightGrid(int x, int y, int width, int height, GuiScrollBar scrollBar, int rowHeight, int columnWidth)
	{
		super(x, y, width, height, scrollBar, rowHeight, columnWidth);
	}

	@Override
	protected void recalculateRowHeight()
	{
		int rowCount = rowCount();
		int columns = getColumns();
		rowHeights = new int[rowCount];
		rowStartY = new int[rowCount];

		if(rowCount < 1)
		{
			return;
		}

		rowStartY[0] = 0;

		for(int row = 0; row < rowCount; row++)
		{
			int height = 1;
			for(int column = 0; column < columns; column++)
			{
				int cell = row * columns + column;

				if(cell < getCells())
				{
					height = Math.max(height, getMinCellHeight(cell));
				}
			}

			rowHeights[row] = height;

			if(row < rowCount - 1)
			{
				rowStartY[row + 1] = rowStartY[row] + height;
			}
		}
	}

	@Override
	protected int getHeightForRow(int row)
	{
		if(row >= 0 && row < rowHeights.length)
		{
			return rowHeights[row];
		}
		else
		{
			return 0;
		}
	}

	@Override
	protected int rowAtScreenY(int y)
	{
		return rowAtGridY(pixelsScrolledForScreenY(y));
	}

	@Override
	protected int rowAtY(int y)
	{
		return rowAtGridY(pixelsScrolledForY(y));
	}

	@Override
	protected int rowAtGridY(int y)
	{
		int row = Arrays.binarySearch(rowStartY, y);

		if(row < 0)
		{
			row = Math.max(0,(-row) - 2);
		}

		return row;
	}

	@Override
	protected int rowStartPixels(int row)
	{
		if(row >= 0)
		{
			if(row < rowStartY.length)
			{
				return rowStartY[row];
			}
			else if(rowStartY.length > 0)
			{
				return rowStartY[rowStartY.length - 1];
			}
		}

		return 0;
	}
}
