package uristqwerty.gui_craftguide.theme.reader;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;

import uristqwerty.gui_craftguide.theme.Theme;

public class ImageElement implements ElementHandler
{
	private String id;
	private List<String> sources = new ArrayList<String>();
	
	@Override
	public void startElement(Theme theme, String name, Attributes attributes)
	{
		for(int i = 0; i< attributes.getLength(); i++)	
		{
			if(attributes.getLocalName(i).equalsIgnoreCase("id"))
			{
				id = attributes.getValue(i).toLowerCase().replaceAll("[^a-z0-9_-]", "");
			}
		}
	}

	@Override
	public ElementHandler getSubElement(String name, Attributes attributes)
	{
		if(name.equalsIgnoreCase("source"))
		{
			return new ImageSourceElement();
		}
		
		return NullElement.instance;
	}

	@Override
	public void characters(Theme theme, char[] chars, int start, int length)
	{
	}

	@Override
	public void endElement(Theme theme, String name)
	{
		if(id == null || id.isEmpty())
		{
			return;
		}
		
		theme.addImage(id, sources);
	}

	@Override
	public void endSubElement(Theme theme, ElementHandler handler, String name)
	{
		if(handler instanceof ImageSourceElement)
		{
			sources.add(((ImageSourceElement)handler).getSource());
		}
	}
}
