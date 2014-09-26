package uristqwerty.CraftGuide.client.ui;

import java.util.LinkedList;
import java.util.List;

import uristqwerty.gui_craftguide.components.GuiElement;
import uristqwerty.gui_craftguide.texture.Texture;


public class GuiSlider extends GuiButton
{
	private int minX, minY, maxX, maxY;
	private int dx, dy;

	private List<ISliderListener> sliderListeners = new LinkedList<ISliderListener>();
	
	@Override
	public void mousePressed(int x, int y)
	{
		super.mousePressed(x, y);
		dx = x - bounds.x();
		dy = y - bounds.y();
	}

	@Override
	public void mouseMoved(int x, int y)
	{
		super.mouseMoved(x, y);
		
		if(isHeld())
		{
			updatePosition(x - dx, y - dy);
			sendSliderEvent();
		}
	}

	@Override
	public GuiElement setSize(int width, int height)
	{
		float x = getPosX();
		float y = getPosY();
		
		maxX += width - bounds.width();
		maxY += height - bounds.height();
		
		setValue(x, y);
		
		return super.setSize(bounds.width(), bounds.height());
	}

	private void sendSliderEvent()
	{
		for(ISliderListener listener: sliderListeners)
		{
			listener.onSliderMoved(this);
		}
	}
	
	public void addSliderListener(ISliderListener listener)
	{
		sliderListeners.add(listener);
	}

	private void updatePosition(int x, int y)
	{
		x = clampValue(x, minX, maxX);
		y = clampValue(y, minY, maxY);
		setPosition(x, y);
	}
	
	public void setValue(float x, float y)
	{
		updatePosition((int)(x * (maxX - minX)) + minX, (int)(y * (maxY - minY)) + minY);
	}

	private int clampValue(int value, int min, int max)
	{
		if(value < min)
		{
			value = min;
		}
		
		if(value > max)
		{
			value = max;
		}
		
		return value;
	}

	public GuiSlider(int x, int y, int width, int height, int buttonWidth, int buttonHeight,
		Texture texture, int u, int v)
	{
		super(x, y, buttonWidth, buttonHeight, texture, u, v);
		
		minX = x;
		minY = y;
		maxX = minX + width  - buttonWidth;
		maxY = minY + height - buttonHeight;
	}
	
	public float getPosX()
	{
		return (bounds.x() - minX)/(float)(maxX - minX);
	}
	
	public float getPosY()
	{
		return (bounds.y() - minY)/(float)(maxY - minY);
	}
}
