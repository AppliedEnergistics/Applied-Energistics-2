package uristqwerty.gui_craftguide.theme.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.LinkedList;

import uristqwerty.gui_craftguide.Rect;
import uristqwerty.gui_craftguide.texture.DynamicTexture;
import uristqwerty.gui_craftguide.texture.TextureClip;
import uristqwerty.gui_craftguide.theme.Theme;

public class XMLThemeWriter implements ThemeWriter
{
	private class ElementException extends Exception
	{
		private static final long serialVersionUID = -1106134229546685224L;
	}
	
	private class ElementCloseMismatchException extends ElementException
	{
		private static final long serialVersionUID = 3826879376987706602L;
	}
	
	private class ElementAttributeCountException extends ElementException
	{
		private static final long serialVersionUID = 5363934302886278365L;
	}
	
	private LinkedList<String> elementStack = new LinkedList<String>();
	
	private OutputStreamWriter output;
	private int indentation = 0;
	
	@Override
	public boolean write(Theme theme, OutputStream outputStream)
	{
		try
		{
			output = new OutputStreamWriter(outputStream);
			
			openElement("theme");
				openElement("metadata");
					writeElement("id", theme.id);
					writeElement("name", theme.name);
					writeElement("description", theme.description);
					
					for(String dependency: theme.dependencies)
					{
						writeElement("dependency", dependency);
					}
					
				closeElement("metadata");
				
				for(String image: theme.images.keySet())
				{
					openElement("image", "id", image);
					
					for(Object[] source: theme.images.get(image))
					{
						writeElement("source", (String)source[0], "type", (String)source[1]);
					}
					
					closeElement("image");
				}
				
				for(String texture: theme.textures.keySet())
				{
					if(theme.textures.get(texture) instanceof TextureClip)
					{
						TextureClip clip = (TextureClip)theme.textures.get(texture);
						openElement("texture", "id", texture, "type", "clip");
						
						if(clip.source instanceof DynamicTexture)
						{
							writeElement("source", ((DynamicTexture)clip.source).id);
						}
						else
						{
							//TODO
						}
						
						writeRect(clip.rect);
						
						closeElement("texture");
					}
				}
				
			closeElement("theme");
			
			output.flush();
			
			return true;
		}
		catch(ElementException e)
		{
			e.printStackTrace();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		return false;
	}

	private void writeRect(Rect rect) throws ElementAttributeCountException, IOException
	{
		writeElement("rect", "",
				"x", Integer.toString(rect.x),
				"y", Integer.toString(rect.y),
				"width", Integer.toString(rect.width),
				"height", Integer.toString(rect.height));
	}

	private void writeElement(String elementName, String contents, String... attributes) throws IOException, ElementAttributeCountException
	{
		indent();
		output.write("<" + elementName + attributes(attributes) +  ">" + escape(contents) + "</" + elementName + ">\n");
	}

	private String attributes(String[] attributes) throws ElementAttributeCountException
	{
		if((attributes.length & 0x1) != 0)
		{
			throw new ElementAttributeCountException();
		}
		
		String result = "";
		
		for(int i = 0; i < attributes.length; i += 2)
		{
			result = result + " " + attributes[i] + "=\"" + escape(attributes[i + 1]) + "\"";
		}
		
		return result;
	}

	private void openElement(String elementName, String... attributes) throws IOException, ElementAttributeCountException
	{
		elementStack.push(elementName);
		indent();
		output.write("<" + elementName + attributes(attributes) + ">\n");

		indentation++;
	}

	private void closeElement(String elementName) throws ElementCloseMismatchException, IOException
	{
		if(!elementName.equals(elementStack.pop()))
		{
			throw new ElementCloseMismatchException();
		}
		
		indentation--;
		indent();
		output.write("</" + elementName + ">\n");
	}

	private void indent() throws IOException
	{
		for(int i = 0; i < indentation; i++)
		{
			output.write("\t");
		}
	}
	
	private String escape(String string)
	{
		if(string == null)
		{
			return "";
		}
		else
		{
			return string
				.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("'", "&apos;")
				.replace("\"", "&quot;");
		}
	}
}
