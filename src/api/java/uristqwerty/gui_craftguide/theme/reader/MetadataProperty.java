package uristqwerty.gui_craftguide.theme.reader;

import org.xml.sax.Attributes;

import uristqwerty.gui_craftguide.theme.Theme;

public class MetadataProperty implements ElementHandler
{
	private String propertyName;
	
	public MetadataProperty(String property)
	{
		propertyName = property;
	}

	@Override
	public void characters(Theme theme, char[] chars, int start, int length)
	{
		String value = String.valueOf(chars, start, length).trim();
		theme.setMetadata(propertyName, value);
	}

	@Override
	public ElementHandler getSubElement(String name, Attributes attributes)
	{
		return NullElement.instance;
	}
	
	@Override
	public void startElement(Theme theme, String name, Attributes attributes)
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
