package uristqwerty.CraftGuide.client.ui;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import org.lwjgl.input.Keyboard;

import uristqwerty.gui_craftguide.components.GuiElement;
import uristqwerty.gui_craftguide.minecraft.Text;

public class GuiTextInput extends GuiElement implements IButtonListener
{
	private List<ITextInputListener> listeners = new LinkedList<ITextInputListener>();
	private String before = "";
	private String after = "";
	public static GuiTextInput inFocus = null;
	private Text textDisplayBefore = new Text(1, 2, "", 0xff000000);
	private Text textDisplayAfter = new Text(1, 2, "", 0xff000000);
	private int yText;
	private int xText;

	public GuiTextInput(int x, int y, int width, int height)
	{
		this(x, y, width, height, 0, 0);
	}

	public GuiTextInput(int x, int y, int width, int height, int xTextOffset, int yTextOffset)
	{
		super(x, y, width, height);

		xText = xTextOffset;
		yText = yTextOffset;
	}

	public GuiTextInput addListener(ITextInputListener listener)
	{
		listeners.add(listener);
		return this;
	}

	public String getText()
	{
		return before + after;
	}

	@Override
	public void mousePressed(int x, int y)
	{
		if(!containsPoint(x, y))
		{
			setFocus(false);
		}

		super.mousePressed(x, y);
	}

	@Override
	public void elementClicked(int x, int y)
	{
		setFocus(true);
		moveCursor(xToCharIndex(x - xText));

		super.elementClicked(x, y);
	}

	private int xToCharIndex(int x)
	{
		if(x < Text.textWidth(before))
		{
			return xToCharIndex(before, x);
		}
		else if(inFocus())
		{
			if(x < Text.textWidth(before) + Text.textWidth("_"))
			{
				return before.length();
			}
			else
			{
				return xToCharIndex(after, x - Text.textWidth(before) - Text.textWidth("_")) + before.length() + 1;
			}
		}
		else
		{
			return xToCharIndex(after, x - Text.textWidth(before)) + before.length() + 1;
		}
	}

	private boolean inFocus()
	{
		return inFocus == this;
	}

	private int xToCharIndex(String text, int x)
	{
		FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
		int i = 0;

		while(i < text.length() && fr.getStringWidth(text.substring(0, i + 1)) < x)
		{
			i++;
		}

		return i;
	}

	private void moveCursor(int index)
	{
		String text = before + after;

		if(index <= 0)
		{
			before = "";
			after = text;
		}
		else if(index >= text.length())
		{
			before = text;
			after = "";
		}
		else
		{
			before = text.substring(0, index);
			after = text.substring(index);
		}
	}

	@Override
	public void onKeyTyped(char eventChar, int eventKey)
	{
		if(inFocus == this)
		{
			if(eventKey == Keyboard.KEY_BACK)
			{
				if(before.length() > 0)
				{
					before = before.substring(0, before.length() - 1);
				}
			}
			else if(eventKey == Keyboard.KEY_DELETE)
			{
				if(after.length() > 0)
				{
					after = after.substring(1);
				}
			}
			else if(eventKey == Keyboard.KEY_LEFT)
			{
				if(before.length() > 0)
				{
					after = before.substring(before.length() - 1) + after;
					before = before.substring(0, before.length() - 1);
				}
			}
			else if(eventKey == Keyboard.KEY_RIGHT)
			{
				if(after.length() > 0)
				{
					before = before + after.substring(0, 1);
					after = after.substring(1);
				}

			}
			else if(eventKey == Keyboard.KEY_RETURN)
			{
				setFocus(false);

				for(ITextInputListener listener: listeners)
				{
					listener.onSubmit(this);
				}

				return;
			}
			else if(eventChar != 0 && eventKey != Keyboard.KEY_ESCAPE)
			{
				before = before + eventChar;
			}

			for(ITextInputListener listener: listeners)
			{
				listener.onTextChanged(this);
			}
		}
		else
		{
			super.onKeyTyped(eventChar, eventKey);
		}
	}

	@Override
	public void draw()
	{
		int afterOffset = 0;

		if(inFocus == this)
		{
			textDisplayBefore.setText(before);

			if((System.currentTimeMillis() % 1000) > 500)
			{
				textDisplayAfter.setText("_" + after);
			}
			else
			{
				textDisplayAfter.setText(after);
				afterOffset = Text.textWidth("_");
			}
		}
		else
		{
			textDisplayBefore.setText(before);
			textDisplayAfter.setText(after);
		}

		render(textDisplayBefore, xText, yText);
		render(textDisplayAfter, xText + textDisplayBefore.textWidth() + afterOffset, yText);
		super.draw();
	}

	public void setFocus(boolean focused)
	{
		if(focused)
		{
			inFocus = this;
		}
		else if(inFocus == this)
		{
			inFocus = null;
		}
	}

	@Override
	public void onButtonEvent(GuiButton button, Event eventType)
	{
		setFocus(true);
	}

	public void setText(String string)
	{
		before = string;
		after = "";

		for(ITextInputListener listener: listeners)
		{
			listener.onTextChanged(this);
		}
	}

/*
	@Override
	public void onGuiClosed()
	{
		setFocus(false);

		super.onGuiClosed();
	}
*/
}
