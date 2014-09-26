package uristqwerty.gui_craftguide.theme.reader;

import org.xml.sax.Attributes;

import uristqwerty.gui_craftguide.theme.Theme;

public class ImageSourceElement implements ElementHandler
{
	private String type = ""; /*file (relative to theme), jar, mcdir, image (other image already loaded)*/
	private String path = "";
	
	public String getSource()
	{
		return type + ":" +  path;
	}
	
	@Override
	public ElementHandler getSubElement(String name, Attributes attributes)
	{
		return NullElement.instance;
	}

	@Override
	public void startElement(Theme theme, String name, Attributes attributes)
	{
		for(int i = 0; i < attributes.getLength(); i++)
		{
			if(attributes.getLocalName(i).equalsIgnoreCase("type"))
			{
				type = attributes.getValue(i);
			}
		}
	}

	@Override
	public void characters(Theme theme, char[] chars, int start, int length)
	{
		path = String.valueOf(chars, start, length).trim();
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
