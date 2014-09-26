package uristqwerty.gui_craftguide.theme.reader;

import org.xml.sax.Attributes;

import uristqwerty.gui_craftguide.Rect;
import uristqwerty.gui_craftguide.theme.Theme;

public class RectTemplate implements ValueTemplate
{
	public int x, y, width, height;

	@Override
	public void startElement(Theme theme, String name, Attributes attributes)
	{
		for(int i = 0; i < attributes.getLength(); i++)
		{
			if(attributes.getLocalName(i).equalsIgnoreCase("x"))
			{
				x = num(attributes.getValue(i));
			}
			else if(attributes.getLocalName(i).equalsIgnoreCase("y"))
			{
				y = num(attributes.getValue(i));
			}
			else if(attributes.getLocalName(i).equalsIgnoreCase("width"))
			{
				width = num(attributes.getValue(i));
			}
			else if(attributes.getLocalName(i).equalsIgnoreCase("height"))
			{
				height = num(attributes.getValue(i));
			}
		}
	}

	@Override
	public void characters(Theme theme, char[] chars, int start, int length)
	{
	}

	@Override
	public ElementHandler getSubElement(String name, Attributes attributes)
	{
		return NullElement.instance;
	}

	@Override
	public void endElement(Theme theme, String name)
	{
	}

	@Override
	public void endSubElement(Theme theme, ElementHandler handler, String name)
	{
	}

	private int num(String value)
	{
		try
		{
			return Integer.parseInt(value);
		}
		catch(NumberFormatException e)
		{
			return 0;
		}
	}

	@Override
	public Class valueType()
	{
		return Rect.class;
	}

	@Override
	public Object getValue()
	{
		return new Rect(x, y, width, height);
	}
}
