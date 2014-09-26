package uristqwerty.gui_craftguide.theme.reader;

import org.xml.sax.Attributes;

import uristqwerty.gui_craftguide.theme.Theme;

public class NullElement implements ElementHandler
{
	public static NullElement instance = new NullElement();
	
	@Override
	public ElementHandler getSubElement(String name, Attributes attributes)
	{
		return this;
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
