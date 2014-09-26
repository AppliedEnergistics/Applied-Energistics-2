package uristqwerty.gui_craftguide.components;

import java.util.EnumMap;

import uristqwerty.gui_craftguide.Rect;
import uristqwerty.gui_craftguide.editor.GuiElementMeta;
import uristqwerty.gui_craftguide.editor.GuiElementMeta.EnumMapProperty;
import uristqwerty.gui_craftguide.editor.GuiElementMeta.GuiElementProperty;
import uristqwerty.gui_craftguide.texture.Texture;

@GuiElementMeta(name = "button")
public class Button extends GuiElement
{
	enum ButtonState
	{
		UP,
		DOWN,
		MOUSEOVER,
		DOWN_MOUSEOUT,
	}

	@GuiElementProperty(name = "states")
	@EnumMapProperty(keyType = ButtonState.class, valueType = Texture.class)
	public EnumMap<ButtonState, Texture> stateBackgrounds = new EnumMap<ButtonState, Texture>(ButtonState.class);

	private ButtonState currentState;

	public Button(int x, int y, int width, int height)
	{
		this(new Rect(x, y, width, height));
	}

	public Button(int x, int y, int width, int height, String template)
	{
		this(new Rect(x, y, width, height), template);
	}

	public Button(Rect rect)
	{
		this(rect, "button-default");
	}

	public Button(Rect rect, String template)
	{
		super(rect, template);
	}

	@Override
	public void mouseMoved(int x, int y)
	{
		if(containsPoint(x, y))
		{
			if(currentState == ButtonState.UP)
			{
				currentState = ButtonState.MOUSEOVER;
			}
			else if(currentState == ButtonState.DOWN_MOUSEOUT)
			{
				currentState = ButtonState.DOWN;
			}
		}
		else
		{
			if(currentState == ButtonState.MOUSEOVER)
			{
				currentState = ButtonState.UP;
			}
			else if(currentState == ButtonState.DOWN)
			{
				currentState = ButtonState.DOWN_MOUSEOUT;
			}
		}

		super.mouseMoved(x, y);
	}

	@Override
	public void mouseReleased(int x, int y)
	{
		currentState = ButtonState.UP;
		super.mouseReleased(x, y);
	}

	@Override
	public void drawBackground()
	{
		super.drawBackground();

		Texture texture = stateBackgrounds.get(currentState);
		if(texture != null)
		{
			render(texture, 0, 0, bounds.width(), bounds.height());
		}
	}
}
