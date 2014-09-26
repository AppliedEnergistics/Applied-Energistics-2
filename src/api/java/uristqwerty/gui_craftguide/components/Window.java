package uristqwerty.gui_craftguide.components;

import java.util.EnumMap;
import java.util.Map;

import uristqwerty.CraftGuide.client.ui.GuiRenderer;
import uristqwerty.gui_craftguide.Rect;
import uristqwerty.gui_craftguide.rendering.Renderable;
import uristqwerty.gui_craftguide.texture.Texture;

public class Window extends GuiElement
{
	public enum Layer
	{
		MAIN,
		POPUP,
		TOOLTIP,
	}

	private GuiRenderer renderer;
	private boolean mousePressed;
	private Map<Layer, GuiElement> layers = new EnumMap<Layer, GuiElement>(Layer.class);

	private int lastMouseX, lastMouseY;
	private int centerX, centerY;
	private int maxWidth, maxHeight;
	private boolean centred = false;

	public Window(int x, int y, int width, int height, GuiRenderer renderer)
	{
		super(x, y, width, height);

		this.renderer = renderer;
	}

	public void centerOn(int centerX, int centerY)
	{
		centred = true;
		this.centerX = centerX;
		this.centerY = centerY;
		setPosition(centerX - (bounds.width() / 2), centerY - (bounds.height() / 2));
	}

	@Override
	public void render(Renderable renderable, int xOffset, int yOffset)
	{
		renderer.render(renderable, xOffset + bounds.x(), yOffset + bounds.y());
	}

	@Override
	public void render(Texture texture, int x, int y, int width, int height)
	{
		texture.renderRect(renderer, x + bounds.x(), y + bounds.y(), width, height, 0, 0);
	}

	public void updateMouse(int x, int y)
	{
		if(x != lastMouseX || y != lastMouseY)
		{
			mouseMoved(x, y);
			lastMouseX = x;
			lastMouseY = y;
		}
	}

	public void updateMouseState(int x, int y, boolean buttonState)
	{
		if(mousePressed != buttonState)
		{
			mousePressed = buttonState;

			if(buttonState)
			{
				mousePressed(x, y);
				GuiElement element = getElementAtPoint(x, y);

				if(element != null)
				{
					element.elementClicked(x - element.absoluteX(), y - element.absoluteY());
				}
			}
			else
			{
				mouseReleased(x, y);
			}
		}
	}

	@Override
	public void mousePressed(int x, int y)
	{
		getLayer(Layer.MAIN).mousePressed(x - bounds.x(), y - bounds.y());
		getLayer(Layer.POPUP).mousePressed(x - bounds.x(), y - bounds.y());
	}

	@Override
	public void draw()
	{
		drawBackground();
		getLayer(Layer.MAIN).draw();
		getLayer(Layer.POPUP).draw();
		getLayer(Layer.TOOLTIP).draw();
	}

	@Override
	public GuiElement getElementAtPoint(int x, int y)
	{
		GuiElement element = null;

		element = getLayer(Layer.POPUP).getElementAtPoint(x - bounds.x(), y - bounds.y());

		if(element == null)
		{
			element = getLayer(Layer.MAIN).getElementAtPoint(x - bounds.x(), y - bounds.y());
		}

		return element;
	}

	public void setMaxSize(int width, int height)
	{
		maxWidth = width;
		maxHeight = height;

		if(bounds.width() > width || bounds.height() > height)
		{
			setSize(Math.min(bounds.width(), width), Math.min(bounds.height(), height));
		}
	}

	@Override
	public GuiElement getLayer(Layer layer)
	{
		GuiElement element = layers.get(layer);

		if(element == null)
		{
			element = new GuiElement(new Rect(0, 0, bounds.width(), bounds.height()))
					.anchor(AnchorPoint.TOP_LEFT, AnchorPoint.BOTTOM_RIGHT)
					.setClickable(false);
			layers.put(layer, element);
			super.addElement(element);
		}

		return element;
	}

	@Override
	public GuiElement addElement(GuiElement element)
	{
		addElement(element, Layer.MAIN);
		return this;
	}

	@Override
	public GuiElement setSize(int width, int height)
	{
		super.setSize(Math.min(width, maxWidth), Math.min(height, maxHeight));

		if(isCentred())
		{
			setPosition(centerX - bounds.width() / 2, centerY - bounds.height() / 2);
		}

		return this;
	}

	private void addElement(GuiElement element, Layer layer)
	{
		getLayer(layer).addElement(element);
	}

	public boolean isCentred()
	{
		return centred;
	}
}
