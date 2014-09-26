package uristqwerty.gui_craftguide.theme.reader;

import org.xml.sax.Attributes;

import uristqwerty.gui_craftguide.texture.DynamicTexture;
import uristqwerty.gui_craftguide.texture.Texture;
import uristqwerty.gui_craftguide.theme.Theme;

public class TextureSourceElement implements ElementHandler
{
	public boolean recursive = false;
	public Texture source;

	@Override
	public void startElement(Theme theme, String name, Attributes attributes)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void characters(Theme theme, char[] chars, int start, int length)
	{
		if(!recursive)
		{
			source = DynamicTexture.instance(String.valueOf(chars, start, length));
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
		// TODO Auto-generated method stub

	}

	@Override
	public void endSubElement(Theme theme, ElementHandler handler, String name)
	{
		// TODO Auto-generated method stub

	}

}
