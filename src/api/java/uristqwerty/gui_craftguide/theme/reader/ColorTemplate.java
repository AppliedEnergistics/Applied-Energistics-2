package uristqwerty.gui_craftguide.theme.reader;

import org.xml.sax.Attributes;

import uristqwerty.gui_craftguide.Color;
import uristqwerty.gui_craftguide.theme.Theme;

public class ColorTemplate implements ValueTemplate
{
	public int red = 255, green = 255, blue = 255, alpha = 255;

	@Override
	public void startElement(Theme theme, String name, Attributes attributes)
	{
		for(int i = 0; i < attributes.getLength(); i++)
		{
			if(attributes.getLocalName(i).equalsIgnoreCase("red"))
			{
				red = num(attributes.getValue(i));
			}
			else if(attributes.getLocalName(i).equalsIgnoreCase("green"))
			{
				green = num(attributes.getValue(i));
			}
			else if(attributes.getLocalName(i).equalsIgnoreCase("blue"))
			{
				blue = num(attributes.getValue(i));
			}
			else if(attributes.getLocalName(i).equalsIgnoreCase("alpha"))
			{
				alpha = num(attributes.getValue(i));
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
		return Color.class;
	}

	@Override
	public Object getValue()
	{
		return new Color(red, green, blue, alpha);
	}
}
