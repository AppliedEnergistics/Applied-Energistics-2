package uristqwerty.CraftGuide.client.ui;

import java.util.LinkedList;
import java.util.List;

import uristqwerty.CraftGuide.client.ui.Rendering.FloatingItemText;
import uristqwerty.CraftGuide.client.ui.Rendering.Overlay;
import uristqwerty.gui_craftguide.components.GuiElement;
import uristqwerty.gui_craftguide.texture.Texture;
import uristqwerty.gui_craftguide.texture.TextureClip;


public class GuiButton extends GuiElement
{
	public enum ButtonState
	{
		UP,
		UP_OVER,
		DOWN,
		DOWN_OVER,
	}

	private List<IButtonListener> buttonListeners = new LinkedList<IButtonListener>();
	private ButtonTemplate template;

	protected ButtonState currentState = ButtonState.UP;

	private static FloatingItemText toolTip = new FloatingItemText("");
	private static Overlay toolTipRender = new Overlay(toolTip);
	private String toolTipText = "";

	public GuiButton(int x, int y, int width, int height, Texture texture, int u, int v)
	{
		this(x, y, width, height, texture, u, v, width, 0);
	}

	public GuiButton(int x, int y, int width, int height, Texture texture, int u, int v, int dx, int dy)
	{
		super(x, y, width, height);

		template = new ButtonTemplate();

		int yOffset = 0;
		int xOffset = 0;
		for(ButtonState state: ButtonState.values())
		{
			template.setStateImage(state, new TextureClip(texture, u + xOffset, v + yOffset, width, height));
			xOffset += dx;
			yOffset += dy;
		}
	}

	protected GuiButton(int x, int y, int width, int height)
	{
		super(x, y, width, height);
	}

	public GuiButton(int x, int y, int width, int height, ButtonTemplate template)
	{
		super(x, y, width, height);

		this.template = template;
	}

	public GuiButton(int x, int y, int width, int height, ButtonTemplate template, String text)
	{
		this(x, y, width, height, template);

		addElement(
			new GuiCentredText(0, 0, width, height, text)
				.anchor(AnchorPoint.TOP_LEFT, AnchorPoint.BOTTOM_RIGHT));
	}

	@Override
	public void draw()
	{
		if(isOver() && !toolTipText.isEmpty())
		{
			toolTip.setText(toolTipText);
			render(toolTipRender);
		}

		setBackground(template.getStateImage(currentState));
		super.draw();
	}

	@Override
	public void mouseMoved(int x, int y)
	{
		updateState(containsPoint(x, y), isHeld());
	}

	@Override
	public void mousePressed(int x, int y)
	{
		if(containsPoint(x, y))
		{
			if(!isHeld())
			{
				sendButtonEvent(IButtonListener.Event.PRESS);
			}

			updateState(true, true);
		}
	}

	@Override
	public void mouseReleased(int x, int y)
	{
		if(isHeld())
		{
			sendButtonEvent(IButtonListener.Event.RELEASE);
		}

		updateState(containsPoint(x, y), false);
	}

	protected void updateState(boolean over, boolean held)
	{
		currentState =
			held?
				over?
					ButtonState.DOWN_OVER
				:
					ButtonState.DOWN
			:
				over?
					ButtonState.UP_OVER
				:
					ButtonState.UP;
	}

	protected boolean isHeld()
	{
		return currentState == ButtonState.DOWN || currentState == ButtonState.DOWN_OVER;
	}

	protected boolean isOver()
	{
		return currentState == ButtonState.UP_OVER || currentState == ButtonState.DOWN_OVER;
	}

	public GuiButton addButtonListener(IButtonListener listener)
	{
		buttonListeners.add(listener);

		return this;
	}

	protected void sendButtonEvent(IButtonListener.Event eventType)
	{
		for(IButtonListener listener: buttonListeners)
		{
			listener.onButtonEvent(this, eventType);
		}
	}

	public GuiButton setToolTip(String text)
	{
		toolTipText = text;

		return this;
	}
}
