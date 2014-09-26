package uristqwerty.CraftGuide.client.ui;

import uristqwerty.CraftGuide.CraftGuide;
import uristqwerty.gui_craftguide.components.GuiElement;
import uristqwerty.gui_craftguide.components.Window;


public class GuiResizeHandle extends GuiElement
{
	private AnchorPoint corner;
	private boolean dragging = false;
	private int targetX, targetY, targetOffsetX, targetOffsetY;

	private int minWidth, minHeight;
	private GuiElement target;

	public GuiResizeHandle(int x, int y, int width, int height, GuiElement element)
	{
		this(x, y, width, height, element, AnchorPoint.BOTTOM_RIGHT);
	}

	public GuiResizeHandle(int x, int y, int width, int height, GuiElement element, AnchorPoint corner)
	{
		this(x, y, width, height, element, corner, element.width(), element.height());
	}

	public GuiResizeHandle(int x, int y, int width, int height,
		GuiElement element, int minimumWidth, int minimumHeight)
	{
		this(x, y, width, height, element, AnchorPoint.BOTTOM_RIGHT, minimumWidth, minimumHeight);
	}

	public GuiResizeHandle(int x, int y, int width, int height,
		GuiElement element, AnchorPoint corner, int minimumWidth, int minimumHeight)
	{
		super(x, y, width, height);

		this.corner = corner;
		target = element;
		minWidth = minimumWidth;
		minHeight = minimumHeight;

		anchor(corner, corner);
	}

	@Override
	public void mouseMoved(int x, int y)
	{
		super.mouseMoved(x, y);

		if(dragging)
		{
			targetX = absoluteX() + x - bounds.x() - targetOffsetX;
			targetY = absoluteY() + y - bounds.y() - targetOffsetY;
		}
	}

	@Override
	public void mousePressed(int x, int y)
	{
		if(containsPoint(x, y))
		{
			dragging = true;
			targetX = absoluteX();
			targetY = absoluteY();
			targetOffsetX = x - bounds.x();
			targetOffsetY = y - bounds.y();
		}

		super.mousePressed(x, y);
	}

	@Override
	public void elementClicked(int x, int y)
	{
		super.elementClicked(x, y);
	}

	@Override
	public void mouseReleased(int x, int y)
	{
		super.mouseReleased(x, y);

		dragging = false;
	}

	@Override
	public void update()
	{
		if(dragging)
		{
			int xDif = targetX - absoluteX();
			int yDif = targetY - absoluteY();

			if(CraftGuide.resizeRate > 0)
			{
				int xDir = (int)Math.signum(xDif);
				int yDir = (int)Math.signum(yDif);

				xDif = Math.min(Math.abs(xDif), CraftGuide.resizeRate) * xDir;
				yDif = Math.min(Math.abs(yDif), CraftGuide.resizeRate) * yDir;
			}

			if(xDif != 0 || yDif != 0)
			{
				if(corner == AnchorPoint.TOP_LEFT || corner == AnchorPoint.TOP_RIGHT)
				{
					yDif = -yDif;
				}

				if(corner == AnchorPoint.TOP_LEFT || corner == AnchorPoint.BOTTOM_LEFT)
				{
					xDif = -xDif;
				}

				if(target instanceof Window && ((Window)target).isCentred())
				{
					xDif *= 2;
					yDif *= 2;
				}

				target.setSize(
					Math.max(target.width()  + xDif, minWidth),
					Math.max(target.height() + yDif, minHeight));
			}
		}

		super.update();
	}
}
