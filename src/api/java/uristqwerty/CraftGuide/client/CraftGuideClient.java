package uristqwerty.CraftGuide.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import uristqwerty.CraftGuide.CraftGuide;
import uristqwerty.CraftGuide.CraftGuideLog;
import uristqwerty.CraftGuide.CraftGuideSide;
import uristqwerty.CraftGuide.GuiCraftGuide;
import uristqwerty.CraftGuide.api.Util;
import uristqwerty.CraftGuide.client.ui.GuiRenderer;
import uristqwerty.gui_craftguide.rendering.RendererBase;
import uristqwerty.gui_craftguide.texture.BlankTexture;
import uristqwerty.gui_craftguide.texture.BorderedTexture;
import uristqwerty.gui_craftguide.texture.MultipleTextures;
import uristqwerty.gui_craftguide.texture.SolidColorTexture;
import uristqwerty.gui_craftguide.texture.SubTexture;
import uristqwerty.gui_craftguide.texture.TextureClip;
import uristqwerty.gui_craftguide.texture.TintedTexture;
import uristqwerty.gui_craftguide.theme.ThemeManager;

public abstract class CraftGuideClient implements CraftGuideSide
{
	@Override
	public void preInit()
	{
		RendererBase.instance = new GuiRenderer();
		Util.instance = new UtilImplementationClient();
		extractResources();

		ThemeManager.addTextureType(SolidColorTexture.class);
		ThemeManager.addTextureType(MultipleTextures.class);
		ThemeManager.addTextureType(BorderedTexture.class);
		ThemeManager.addTextureType(TintedTexture.class);
		ThemeManager.addTextureType(BlankTexture.class);
		ThemeManager.addTextureType(TextureClip.class);
		ThemeManager.addTextureType(SubTexture.class);


		ThemeManager.instance.reload();

		ThemeManager.currentTheme = ThemeManager.instance.buildTheme(readThemeChoice());

		if(ThemeManager.currentTheme == null)
		{
			ThemeManager.currentTheme = ThemeManager.instance.buildTheme("theme_base");
		}
	}

	private String readThemeChoice()
	{
		File dir = themeDirectory();

		if(dir == null)
		{
			return "base_texpack";
		}

		File file = new File(dir, "currentTheme.txt");

		if(!file.exists())
		{
			try
			{
				file.createNewFile();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}

			if(file.canWrite())
			{
				try
				{
					FileWriter writer = new FileWriter(file);
					writer.write("base_texpack");
					writer.close();
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		else if(file.canRead())
		{
			try
			{
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String line = reader.readLine();
				reader.close();
				return line;
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}

		return "base_texpack";
	}

	@Override
	public void reloadRecipes()
	{
		GuiCraftGuide.getInstance().reloadRecipes();
	}

	public static File themeDirectory()
	{
		File configDir = CraftGuide.configDirectory();

		if(configDir == null)
		{
			return null;
		}

		File dir = new File(configDir, "themes");

		if(!dir.exists() && !dir.mkdirs())
		{
			return null;
		}

		return dir;
	}

	private void extractResources()
	{
		File outputBase = themeDirectory();

		if(outputBase == null)
		{
			return;
		}

		try
		{
			InputStream stream = CraftGuide.class.getResourceAsStream("CraftGuideResources.zip");

			if(stream != null)
			{
				ZipInputStream resources = new ZipInputStream(stream);
				byte[] buffer = new byte[1024 * 16];
				ZipEntry entry;
				while((entry = resources.getNextEntry()) != null)
				{
					File destination = new File(outputBase, entry.getName());

					if(entry.isDirectory())
					{
						destination.mkdirs();
					}
					else
					{
						CraftGuideLog.log("CraftGuide: Extracting '" + entry.getName() + "' to '" + destination.getCanonicalPath() + "'", false);
						destination.getParentFile().mkdirs();
						destination.createNewFile();
						FileOutputStream output = new FileOutputStream(destination);
						int len;

						while((len = resources.read(buffer, 0, buffer.length)) != -1)
						{
							output.write(buffer, 0, len);
						}

						output.flush();
						output.close();
					}
				}
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void initNetworkChannels()
	{
		CraftGuide.loaderSide.initClientNetworkChannels();
	}
}
