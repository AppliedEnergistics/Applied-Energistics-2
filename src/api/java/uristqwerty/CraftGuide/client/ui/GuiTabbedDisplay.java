package uristqwerty.CraftGuide.client.ui;

import java.util.HashMap;
import java.util.Map;

import uristqwerty.gui_craftguide.components.GuiElement;

public class GuiTabbedDisplay extends GuiElement implements IButtonListener
{
	private GuiElement currentTab = null;
	private GuiElement changeTab = null;
	private Map<Object, GuiElement> tabMap = new HashMap<Object, GuiElement>();
	
	public GuiTabbedDisplay(int x, int y, int width, int height)
	{
		super(x, y, width, height);
	}
	
	public GuiTabbedDisplay(int x, int y, int width, int height, Object[][] tabs)
	{
		this(x, y, width, height);
		
		for(Object[] tab: tabs)
		{
			if(tab[0] instanceof GuiElement && tab[1] instanceof GuiButton)
			{
				addTab((GuiElement)tab[0], (GuiButton)tab[1]);
			}
		}
	}
	
	public GuiTabbedDisplay(int x, int y, int width, int height, Object[] tabs)
	{
		this(x, y, width, height);
		
		for(int i = 0; i < (tabs.length & ~1); i += 2)
		{
			if(tabs[i] instanceof GuiElement && tabs[i + 1] instanceof GuiButton)
			{
				addTab((GuiElement)tabs[i], (GuiButton)tabs[i + 1]);
			}
		}
	}

	public GuiTabbedDisplay addTab(GuiElement tab, GuiButton button)
	{
		return addTab(tab, button, true);
	}

	public GuiTabbedDisplay addTab(GuiElement tab, GuiButton button, boolean addAsChild)
	{
		button.addButtonListener(this);
		return addTab(tab, (GuiElement)button, addAsChild);
	}
	
	public GuiTabbedDisplay addTab(GuiElement tab, GuiElement key)
	{
		return addTab(tab, key, true);
	}
	
	public GuiTabbedDisplay addTab(GuiElement tab, GuiElement key, boolean addAsChild)
	{
		if(addAsChild)
		{
			addElement(key);
		}
		
		return addTab(tab, (Object)key);
	}
	
	public GuiTabbedDisplay addTab(GuiElement tab, Object key)
	{
		tabMap.put(key, tab);
		
		if(currentTab == null)
		{
			setTab(tab);
		}
		
		return this;
	}
	
	@Override
	public void mousePressed(int x, int y)
	{
		super.mousePressed(x, y);
		
		if(changeTab != null)
		{
			setTab(changeTab);
		}
	}

	@Override
	public void onButtonEvent(GuiButton button, Event eventType)
	{
		if(eventType == Event.PRESS)
		{
			changeTab = tabMap.get(button);
		}
	}
	
	public void openTab(Object key)
	{
		setTab(tabMap.get(key));
	}

	private void setTab(GuiElement tab)
	{
		if(currentTab != null)
		{
			currentTab.mouseReleased(0, 0);
			removeElement(currentTab);
		}
		
		if(tab != null)
		{
			addElement(tab);
		}
		
		currentTab = tab;
		changeTab = null;
	}

	@Override
	public void onResize(int oldWidth, int oldHeight)
	{
		for(GuiElement element: tabMap.values())
		{
			if(element != currentTab)
			{
				element.onParentResize(oldWidth, oldHeight, bounds.width(), bounds.height());
			}
		}
		
		super.onResize(oldWidth, oldHeight);
	}
}
