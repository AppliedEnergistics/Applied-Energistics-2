package uristqwerty.gui_craftguide.theme.reader;

import org.xml.sax.Attributes;

import uristqwerty.gui_craftguide.theme.Theme;

public class IntegerElement implements ValueTemplate
{
	Integer value = 0;

	@Override
	public void startElement(Theme theme, String name, Attributes attributes)
	{
	}

	@Override
	public void characters(Theme theme, char[] chars, int start, int length)
	{
		try
		{
			value = Integer.parseInt(String.valueOf(chars, start, length));
		}
		catch(NumberFormatException e)
		{
		}
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

	@Override
	public Class valueType()
	{
		return Integer.class;
	}

	@Override
	public Object getValue()
	{
		return value;
	}

}
