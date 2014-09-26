package uristqwerty.gui_craftguide.theme;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import uristqwerty.CraftGuide.CraftGuideLog;
import uristqwerty.CraftGuide.client.CraftGuideClient;
import uristqwerty.gui_craftguide.editor.TextureMeta;
import uristqwerty.gui_craftguide.minecraft.Image;
import uristqwerty.gui_craftguide.texture.Texture;
import uristqwerty.gui_craftguide.theme.reader.ThemeReader;

public class ThemeManager
{
	private ThemeReader xmlReader = new ThemeReader();
	public Map<String, Theme> themeList;
	public static boolean debugOutput = false;

	public static ThemeManager instance = new ThemeManager();
	public static Theme currentTheme;
	public static Map<String, Class<? extends Texture>> textureTypes = new HashMap<String, Class<? extends Texture>>();

	public void reload()
	{
		File themeDir = CraftGuideClient.themeDirectory();
		Image.unloadFileTextures();

		if(themeDir == null || !themeDir.isDirectory())
		{
			return;
		}

		Map<String, Theme> themes = new HashMap<String, Theme>();

		CraftGuideLog.log("(re)loading themes...");

		for(File file: themeDir.listFiles())
		{
			Theme theme = null;

			if(file.isDirectory())
			{
				CraftGuideLog.log("  Trying to load directory: " + file.getName());
				theme = loadDirectory(file);
			}
			else if(file.getName().toLowerCase().endsWith(".txt"))
			{
				continue;
			}
			else
			{
				CraftGuideLog.log("  Trying to load file: " + file.getName());
				theme = loadFile(file);
			}

			if(theme != null)
			{
				CraftGuideLog.log("    Loaded " + file.getName());
				themes.put(theme.id, theme);
			}
			else
			{
				CraftGuideLog.log("    Failed to load " + file.getName());
			}
		}



		Map<String, Theme> validatedThemes = new HashMap<String, Theme>();
		List<String> processed = new ArrayList<String>();

		debug("Validating themes:");
		while(themes.size() > 0)
		{
			debug("  Looping over unvalidated themes:");
			for(String themeID: themes.keySet())
			{
				debug("    Theme '" + themeID + "':");
				Theme theme = themes.get(themeID);

				if(theme.loadError != null)
				{
					debug("      Already marked as having an error");
					processed.add(themeID);
					continue;
				}

				boolean hasDependencies = true;

				debug("      Checking dependencies...");
				for(String dependency: theme.dependencies)
				{
					debug("        Dependency '" + dependency + "'");
					if(themes.containsKey(dependency))
					{
						hasDependencies = false;
						break;
					}
					if(!validatedThemes.containsKey(dependency))
					{
						theme.loadError = "Dependency '" + dependency + "' missing or unloadable";
						break;
					}
					else if(validatedThemes.get(dependency).loadError != null)
					{
						theme.loadError = "Error in dependency '" + dependency + "'";
						break;
					}
				}

				if(theme.loadError != null)
				{
					debug("      Dependency error '" + theme.loadError + "'");
					processed.add(themeID);
					continue;
				}

				if(!hasDependencies)
				{
					continue;
				}

				debug("      All dependencies processed without error. Validating theme...");

				for(String imageID: theme.images.keySet())
				{
					debug("      Checking image sources for image '" + imageID + "'");
					boolean valid = false;
					for(Object[] imageSource: theme.images.get(imageID))
					{
						debug("        Source '" + imageSource + "'");

						if(validImage(theme, imageSource, validatedThemes))
						{
							valid = true;
							break;
						}
					}

					if(!valid)
					{
						theme.loadError = "Unable to locate valid source for image '" + imageID + "'";
						break;
					}
				}

				processed.add(themeID);
			}

			if(processed.size() > 0)
			{
				debug("  " + processed.size() + " themes validated");

				for(String themeID: processed)
				{
					validatedThemes.put(themeID, themes.get(themeID));
					themes.remove(themeID);
				}

				processed.clear();
			}
			else
			{
				debug("  No themes validated, marking remaining themes as having a cyclic dependency");

				for(String themeID: themes.keySet())
				{
					Theme theme = themes.get(themeID);
					theme.loadError = "Possible cyclic dependency";
					validatedThemes.put(themeID, theme);
				}

				break;
			}
		}


		debug("Result:");

		for(String themeID: validatedThemes.keySet())
		{
			if(validatedThemes.get(themeID).loadError == null)
			{
				debug("  '" + themeID + "' loaded successfully");
			}
			else
			{
				debug("  '" + themeID + "' failed to load with error \"" + validatedThemes.get(themeID).loadError + "\"");
			}
		}

		themeList = validatedThemes;
	}

	private boolean validImage(Theme theme, Object[] imageSource, Map<String, Theme> validatedThemes)
	{
		debug("          Checking if image exists and is valid...");
		String type = (String)imageSource[0];
		String source = (String)imageSource[1];
		if(type.equalsIgnoreCase("parent-theme"))
		{
			debug("            Searching for definition in parent theme(s)");
			for(String id: theme.dependencies)
			{
				debug("              Checking theme '" + id + "'");

				if(definesImage(validatedThemes.get(id), source, validatedThemes))
				{
					return true;
				}
			}

			debug("            No parent theme defines image.");
			return false;
		}
		else if(type.equalsIgnoreCase("file-jar") || type.equalsIgnoreCase("resource"))
		{
			debug("            Searching resource packs, Minecraft.jar, and mod jars/zips/dirs for '" + source + "'");
			
		}
		else if(type.equalsIgnoreCase("file"))
		{
			debug("            Checking theme location for '" + source + "'");

			if(theme.fileSourceType == Theme.SourceType.DIRECTORY)
			{
				return new File((File)imageSource[2], source).isFile();
			}
		}
		else if(type.equalsIgnoreCase("builtin:"))
		{
			String builtin = source;

			if(builtin.equalsIgnoreCase("error"))
			{
				return true;
			}
		}

		return false;
	}

	private boolean definesImage(Theme theme, String sourceID, Map<String, Theme> validatedThemes)
	{
		if(theme.images.containsKey(sourceID))
		{
			return true;
		}

		for(String id: theme.dependencies)
		{
			if(definesImage(validatedThemes.get(id), sourceID, validatedThemes))
			{
				return true;
			}
		}

		return false;
	}

	private Theme loadDirectory(File dir)
	{
		for(File file: dir.listFiles())
		{
			if(file.getName().startsWith("theme"))
			{
				CraftGuideLog.log("    Found " + file.getName());

				if(!file.isDirectory() && file.getName().endsWith(".xml"))
				{
				
				}

				CraftGuideLog.log("      Could not read " + file.getName());
			}
		}

		return null;
	}

	private Theme loadStream(InputStream stream)
	{
		Theme theme = xmlReader.read(stream, null);

		if(theme != null)
		{
			theme.fileSource = null;
			theme.fileSourceType = Theme.SourceType.STREAM;
		}

		return theme;
	}

	private Theme loadFile(File file)
	{
		return null;
	}

	public static void debug(String text)
	{
		if(debugOutput)
		{
			CraftGuideLog.log(text, true);
		}
	}

	public Theme buildTheme(String string)
	{
		debug("Building theme '" + string + "':");
		Theme theme = themeList.get(string);

		if(theme == null)
		{
			debug("  Could not find theme.");
			return null;
		}

		Theme combined = new Theme(null);
		debug("  Merging...");
		merge(combined, theme);
		combined.id = "[built-theme:" + string + "]";
		combined.fileSourceType = Theme.SourceType.GENERATED;
		combined.description = "Internal Theme that is a combination of the currently selected theme and all of its prerequisites.";

		debug("  Generating textures...");
		combined.generateTextures();

		return combined;
	}

	private void merge(Theme combined, Theme theme)
	{
		debug("    Handling dependencies:");
		for(String dependency: theme.dependencies)
		{
			debug("      Dependency '" + dependency + "'");
			if(!combined.dependencies.contains(dependency))
			{
				Theme dep = themeList.get(dependency);

				if(dep != null)
				{
					debug("        Not merged yet. Merging... [");
					merge(combined, dep);
					debug("        ] finished merging '" + dependency + "'");
				}
				else
				{
					debug("        Not merged yet. Could not find theme, though.");
				}
			}
			else
			{
				debug("        Already merged.");
			}
		}

		debug("    Merging:");
		if(combined.dependencies.contains(theme.id))
		{
			debug("      Already merged, skipping.");
			return;
		}

		debug("      Not already merged. adding images and textures.");
		combined.dependencies.add(theme.id);
		combined.images.putAll(theme.images);
		combined.textures.putAll(theme.textures);
	}

	public static void addTextureType(Class<? extends Texture> textureClass)
	{
		TextureMeta meta = textureClass.getAnnotation(TextureMeta.class);

		if(meta != null)
		{
			textureTypes.put(meta.name().toLowerCase(), textureClass);
		}
	}
}
