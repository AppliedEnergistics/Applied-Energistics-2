package uristqwerty.gui_craftguide.theme;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import uristqwerty.gui_craftguide.minecraft.Image;
import uristqwerty.gui_craftguide.texture.DynamicTexture;
import uristqwerty.gui_craftguide.texture.SolidColorTexture;
import uristqwerty.gui_craftguide.texture.Texture;

public class Theme
{
	public enum SourceType
	{
		DIRECTORY,
		GENERATED,
		STREAM,
	}

	private static Texture errorTexture = new SolidColorTexture(255, 0, 255, 255);

	public String id;
	public String name;
	public String description;
	public File fileSource;
	public SourceType fileSourceType;
	public Map<String, List<Object[]>> images = new HashMap<String, List<Object[]>>();
	public String loadError = null;
	public List<String> dependencies = new ArrayList<String>();
	public Map<String, Texture> textures = new HashMap<String, Texture>();

	private static Object[] errorImage = {"builtin", "error", null};

	public Theme(File location)
	{
		fileSource = location;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public void setID(String id)
	{
		this.id = id.toLowerCase().replaceAll("[^a-z0-9_-]", "");
	}

	public void setMetadata(String propertyName, String value)
	{
		if(propertyName.equalsIgnoreCase("id"))
		{
			setID(value);
		}
		else if(propertyName.equalsIgnoreCase("name"))
		{
			setName(value);
		}
		else if(propertyName.equalsIgnoreCase("description"))
		{
			setDescription(value);
		}
	}

	public void addImage(String id, List<String> sources)
	{
		List<Object[]> converted = new ArrayList<Object[]>(sources.size());

		for(String source: sources)
		{
			Object[] o = new Object[]{source.substring(0, source.indexOf(':')), source.substring(source.indexOf(':') + 1), fileSource};
			converted.add(o);
		}

		images.put(id, converted);
	}

	public void addDependency(String dependency)
	{
		dependencies.add(dependency);
	}

	public void generateTextures()
	{
		for(String imageID: images.keySet())
		{
			ThemeManager.debug("    Loading image '" + imageID + "'");
			Texture texture = null;

			for(Object[] imageFormat: images.get(imageID))
			{
				texture = loadImage(imageFormat);

				if(texture != null)
				{
					break;
				}
			}

			if(texture == null)
			{
				texture = loadImage(errorImage);
			}

			textures.put(imageID, texture);
		}

		for(String id: textures.keySet())
		{
			ThemeManager.debug("    Adding texture '" + id + "'. Maps to '" + textures.get(id) + "'");
			DynamicTexture.instance(id, textures.get(id));
		}
	}

	private Texture loadImage(Object[] imageFormat)
	{
		String sourceType = (String)imageFormat[0];
		String source = (String)imageFormat[1];

		ThemeManager.debug("      Loading " + sourceType + " image '" + source + "'");

		if(sourceType.equalsIgnoreCase("builtin"))
		{
			if(source.equalsIgnoreCase("error"))
			{
				return errorTexture;
			}
		}
		else if(sourceType.equalsIgnoreCase("file-jar") || sourceType.equalsIgnoreCase("resource"))
		{
			
		}
		else if(sourceType.equalsIgnoreCase("file") && imageFormat[2] != null)
		{
			return Image.fromFile((File)imageFormat[2], source);
		}

		ThemeManager.debug("        Not found.");
		return null;
	}

	public void addTexture(String id, Texture texture)
	{
		textures.put(id, texture);
	}
}
