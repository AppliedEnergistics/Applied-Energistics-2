package uristqwerty.gui_craftguide.components;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import uristqwerty.CraftGuide.CraftGuideLog;
import uristqwerty.gui_craftguide.MutableRect;
import uristqwerty.gui_craftguide.Rect;
import uristqwerty.gui_craftguide.editor.GuiElementMeta.GuiElementProperty;
import uristqwerty.gui_craftguide.rendering.Renderable;
import uristqwerty.gui_craftguide.texture.Texture;

public class GuiElement
{
	protected MutableRect bounds;

	@GuiElementProperty(name = "background")
	public Texture background = null;

	private GuiElement parent = null;
	private List<GuiElement> children = new LinkedList<GuiElement>();

	public enum AnchorPoint
	{
		TOP_LEFT,
		TOP_RIGHT,
		BOTTOM_LEFT,
		BOTTOM_RIGHT,
	}

	private AnchorPoint anchorTL = AnchorPoint.TOP_LEFT;
	private AnchorPoint anchorBR = AnchorPoint.TOP_LEFT;

	private boolean clickable = true;

	public GuiElement(int x, int y, int width, int height)
	{
		bounds = new MutableRect(x, y, width, height);
	}

	public GuiElement(Rect bounds)
	{
		this.bounds = new MutableRect(bounds);
	}

	public GuiElement(Rect bounds, String template)
	{
		this.bounds = new MutableRect(bounds);
		applyTemplate(template);
	}

	public GuiElement getLayer(Window.Layer layer)
	{
		if(parent != null)
		{
			return parent.getLayer(layer);
		}
		else
		{
			return null;
		}
	}

	public GuiElement getElementAtPoint(int x, int y)
	{
		if(!containsPoint(x, y))
		{
			return null;
		}

		GuiElement clicked = clickable? this : null;

		for(GuiElement child: children)
		{
			GuiElement element = child.getElementAtPoint(x - bounds.x(), y - bounds.y());

			if(element != null)
			{
				clicked = element;
			}
		}

		return clicked;
	}

	private void applyTemplate(String template)
	{
		for(Field field: this.getClass().getFields())
		{
			GuiElementProperty property = field.getAnnotation(GuiElementProperty.class);
			if(property != null)
			{
				try
				{
					CraftGuideLog.log("Annotated property '" + property.name() + "', field '" + field.getName() + "', currentValue '" + field.get(this) + "'", true);
				}
				catch(IllegalArgumentException e)
				{
					e.printStackTrace();
				}
				catch(IllegalAccessException e)
				{
					e.printStackTrace();
				}
			}
		}
		Map<String, Object> values = getTemplate(template);

		if(values != null)
		{
			/*
			//GuiElementMeta meta = this.getClass().getAnnotation(GuiElementMeta.class);

			for(Field field: this.getClass().getFields())
			{
				GuiElementProperty property = field.getAnnotation(GuiElementProperty.class);

				if(property != null)
				{
					if(values.containsKey(property.name()))
					{

					}
					else
					{
						if()
						{

						}
					}
				}
			}*/
		}
	}

	private Map<String, Object> getTemplate(String template)
	{
		if(template.equalsIgnoreCase(""))
		{

		}

		return null;
	}

	public GuiElement addElement(GuiElement element)
	{
		element.parent = this;
		children.add(element);

		return this;
	}

	public void removeElement(GuiElement element)
	{
		element.parent = null;
		children.remove(element);
	}

	public void update()
	{
		for(GuiElement element: children)
		{
			element.update();
		}
	}

	public void draw()
	{
		drawBackground();
		drawChildren();
	}

	public void drawChildren()
	{
		for(GuiElement element: children)
		{
			element.draw();
		}
	}

	public void drawBackground()
	{
		if(background != null)
		{
			render(background, 0, 0, bounds.width(), bounds.height());
		}
	}

	public void mouseMoved(int x, int y)
	{
		for(GuiElement element: children)
		{
			element.mouseMoved(x - bounds.x(), y - bounds.y());
		}
	}

	public void mousePressed(int x, int y)
	{
		for(GuiElement element: children)
		{
			element.mousePressed(x - bounds.x(), y - bounds.y());
		}
	}

	public void mouseReleased(int x, int y)
	{
		for(GuiElement element: children)
		{
			element.mouseReleased(x - bounds.x(), y - bounds.y());
		}
	}

	public void render(Renderable renderable)
	{
		render(renderable, 0, 0);
	}

	public void render(Texture texture, int x, int y, int width, int height)
	{
		if(parent != null && texture != null && width > 0 && height > 0)
		{
			parent.render(texture, x + bounds.x(), y + bounds.y(), width, height);
		}
	}

	public void render(Renderable renderable, int xOffset, int yOffset)
	{
		if(parent != null && renderable != null)
		{
			parent.render(renderable, xOffset + bounds.x(), yOffset + bounds.y());
		}
	}

	public boolean containsPoint(int x, int y)
	{
		return x >= bounds.x()
			&& x <  bounds.x() + bounds.width()
			&& y >= bounds.y()
			&& y <  bounds.y() + bounds.height();
	}

	public GuiElement setSize(int width, int height)
	{
		if(width != bounds.width() || height != bounds.height())
		{
			int oldWidth = bounds.width();
			int oldHeight = bounds.height();

			bounds.setSize(width, height);
			onResize(oldWidth, oldHeight);

			for(GuiElement element: children)
			{
				element.onParentResize(oldWidth, oldHeight, width, height);
			}
		}

		return this;
	}

	public void onParentResize(int oldWidth, int oldHeight, int newWidth, int newHeight)
	{
		int x1 = bounds.x();
		int y1 = bounds.y();
		int x2 = bounds.x() + bounds.width();
		int y2 = bounds.y() + bounds.height();

		if(anchorTL == AnchorPoint.TOP_RIGHT || anchorTL == AnchorPoint.BOTTOM_RIGHT)
		{
			x1 += newWidth - oldWidth;
		}

		if(anchorTL == AnchorPoint.BOTTOM_LEFT || anchorTL == AnchorPoint.BOTTOM_RIGHT)
		{
			y1 += newHeight - oldHeight;
		}

		if(anchorBR == AnchorPoint.TOP_RIGHT || anchorBR == AnchorPoint.BOTTOM_RIGHT)
		{
			x2 += newWidth - oldWidth;
		}

		if(anchorBR == AnchorPoint.BOTTOM_LEFT || anchorBR == AnchorPoint.BOTTOM_RIGHT)
		{
			y2 += newHeight - oldHeight;
		}

		if(x1 != bounds.x() || y1 != bounds.y())
		{
			setPosition(x1, y1);
		}

		if(x2 - x1 != bounds.width() || y2 - y1 != bounds.height())
		{
			setSize(x2 - x1, y2 - y1);
		}
	}

	public GuiElement setPosition(int x, int y)
	{
		if(x == bounds.x() && y == bounds.y())
		{
			return this;
		}

		bounds.setPosition(x, y);

		onMove();

		for(GuiElement element: children)
		{
			element.onParentMove();
		}

		return this;
	}

	public GuiElement setPositionAbsolute(int x, int y)
	{
		return setPosition(x - absoluteX() + bounds.x(), y - absoluteY() + bounds.y());
	}

	public GuiElement anchor(AnchorPoint topLeft, AnchorPoint bottomRight)
	{
		anchorTL = topLeft;
		anchorBR = bottomRight;

		return this;
	}

	public GuiElement anchor(AnchorPoint point)
	{
		return anchor(point, point);
	}

	public int absoluteX()
	{
		if(parent == null)
		{
			return bounds.x();
		}
		else
		{
			return parent.absoluteX() + bounds.x();
		}
	}

	public int absoluteY()
	{
		if(parent == null)
		{
			return bounds.y();
		}
		else
		{
			return parent.absoluteY() + bounds.y();
		}
	}

	public void onKeyTyped(char eventChar, int eventKey)
	{
		for(GuiElement element: children)
		{
			element.onKeyTyped(eventChar, eventKey);
		}
	}

	public void scrollWheelTurned(int change)
	{
		for(GuiElement element: children)
		{
			element.scrollWheelTurned(change);
		}
	}

	public void onGuiClosed()
	{
		for(GuiElement element: children)
		{
			element.onGuiClosed();
		}
	}

	public GuiElement setClickable(boolean clickable)
	{
		this.clickable = clickable;
		return this;
	}

	public GuiElement setBackground(Texture background)
	{
		this.background = background;
		return this;
	}

	public int width()
	{
		return bounds.width();
	}

	public int height()
	{
		return bounds.height();
	}

	public void onResize(int oldWidth, int oldHeight)
	{
		/** Default implementation: Do nothing */
	}

	public void onParentMove()
	{
		/** Default implementation: Do nothing */
	}

	public void onMove()
	{
		/** Default implementation: Do nothing */
	}

	public void elementClicked(int x, int y)
	{
		/** Default implementation: Do nothing */
	}
}
