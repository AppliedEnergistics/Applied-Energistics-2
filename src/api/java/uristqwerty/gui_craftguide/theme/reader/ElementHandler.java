package uristqwerty.gui_craftguide.theme.reader;

import org.xml.sax.Attributes;

import uristqwerty.gui_craftguide.theme.Theme;

public interface ElementHandler
{
	public void startElement(Theme theme, String name, Attributes attributes);
	public void characters(Theme theme, char[] chars, int start, int length);
	public ElementHandler getSubElement(String name, Attributes attributes);
	public void endElement(Theme theme, String name);
	public void endSubElement(Theme theme, ElementHandler handler, String name);
}
