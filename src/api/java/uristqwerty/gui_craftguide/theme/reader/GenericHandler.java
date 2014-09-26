package uristqwerty.gui_craftguide.theme.reader;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;

import uristqwerty.gui_craftguide.theme.Theme;

public class GenericHandler implements ElementHandler
{
	private Map<String, Object> handlers = new HashMap<String, Object>();
	
	public GenericHandler(Object... elements)//May throw IndexOutOfBoundsException or ClassCastException on bad data
	{
		for(int i = 0; i < elements.length; i += 2)
		{
			handlers.put(((String)elements[i]).toLowerCase(), elements[i + 1]);
		}
	}

	@Override
	public ElementHandler getSubElement(String name, Attributes attributes)
	{
		Object handler = handlers.get(name.toLowerCase());
		
		if(handler instanceof ElementHandler)
		{
			return (ElementHandler)handler;
		}
		else if(handler instanceof Class && ElementHandler.class.isAssignableFrom((Class)handler))
		{
			try
			{
				return ((Class<? extends ElementHandler>)handler).newInstance();
			}
			catch(InstantiationException e)
			{
				e.printStackTrace();
			}
			catch(IllegalAccessException e)
			{
				e.printStackTrace();
			}
		}
		
		return NullElement.instance;
	}

	@Override
	public void startElement(Theme theme, String name, Attributes attributes)
	{
	}

	@Override
	public void characters(Theme theme, char[] chars, int start, int length)
	{
	}

	@Override
	public void endElement(Theme theme, String name)
	{
	}

	@Override
	public void endSubElement(Theme theme, ElementHandler handler, String name)
	{
	}
}
