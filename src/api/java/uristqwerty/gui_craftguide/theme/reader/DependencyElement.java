package uristqwerty.gui_craftguide.theme.reader;

import org.xml.sax.Attributes;

import uristqwerty.gui_craftguide.theme.Theme;

public class DependencyElement implements ElementHandler
{

	@Override
	public void startElement(Theme theme, String name, Attributes attributes)
	{
	}

	@Override
	public void characters(Theme theme, char[] chars, int start, int length)
	{
		theme.addDependency(String.valueOf(chars, start, length).trim());
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
}
