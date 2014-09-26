package uristqwerty.gui_craftguide.minecraft;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import uristqwerty.gui_craftguide.rendering.RendererBase;
import uristqwerty.gui_craftguide.texture.Texture;

public class Image implements Texture
{
	private int texID;
	private static Map<String, Image> jarCache = new HashMap<String, Image>();
	private static Map<String, Image> fileCache = new HashMap<String, Image>();
	private static Image err = new Image(-1);
	private static boolean needsInit = true;

	public static void initJarTextures()
	{
		if(needsInit)
		{
			for(Entry<String, Image> entry: jarCache.entrySet())
			{
				Image image = entry.getValue();

				if(image.texID == -1)
				{
					ResourceLocation resourceLocation = new ResourceLocation(entry.getKey());
					ITextureObject texture = Minecraft.getMinecraft().getTextureManager().getTexture(resourceLocation);

					if(texture == null)
					{
						texture = new SimpleTexture(resourceLocation);
						Minecraft.getMinecraft().getTextureManager().loadTexture(resourceLocation, texture);
					}

					image.texID = texture.getGlTextureId();
				}
			}

			needsInit = false;
		}
	}

	public static Image fromJar(String filename)
	{
		if(filename == null || filename.trim().isEmpty())
		{
			return err;
		}

		Image image = jarCache.get(filename);

		if(image == null)
		{
			image = new Image(-1);
			jarCache.put(filename, image);
			needsInit = true;
		}

		return image;
	}

	public static Image fromFile(File directory, String filename)
	{
		String key;
		try
		{
			key = new File(directory, filename).getCanonicalPath();
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return null;
		}

		Image image = fileCache.get(key);

		if(image == null)
		{
			image = new Image(-1);
			fileCache.put(key, image);
		}

		if(image.texID == -1)
		{
			image.texID = LoadImageFile(directory, filename);
		}

		return image;
	}

	private static int LoadImageFile(File directory, String filename)
	{
		try
		{
			int width = 4;
			int height = 4;

			InputStream input = new FileInputStream(new File(directory, filename));
			BufferedImage image = ImageIO.read(input);

			while(width < image.getWidth())
			{
				width *= 2;
			}

			while(height < image.getHeight())
			{
				height *= 2;
			}

			ByteBuffer pixels = ByteBuffer.allocateDirect(width * height * 4);

			for(int y = 0; y < height; y++)
			{
				for(int x = 0; x < width; x++)
				{
					int pixel = (y * width + x) * 4;

					if(x < image.getWidth() && y < image.getHeight())
					{
						int rgb = image.getRGB(x, y);
						pixels.put(pixel + 0, (byte)((rgb >> 16) & 0xff));
						pixels.put(pixel + 1, (byte)((rgb >>  8) & 0xff));
						pixels.put(pixel + 2, (byte)((rgb >>  0) & 0xff));
						pixels.put(pixel + 3, (byte)((rgb >> 24) & 0xff));
					}
					else
					{
						pixels.put(pixel + 0, (byte)0x7f);
						pixels.put(pixel + 1, (byte)0x7f);
						pixels.put(pixel + 2, (byte)0x7f);
						pixels.put(pixel + 3, (byte)0x7f);
					}
				}
			}

			int texID = GL11.glGenTextures();
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, texID);
			GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);

			return texID;
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return -1;
		}
	}

	private Image(int textureID)
	{
		texID = textureID;
	}

	@Override
	public void renderRect(RendererBase renderer, int x, int y, int width, int height, int textureX, int textureY)
	{
		if(texID >= 0)
		{
			double u = (textureX % 256) / 256.0;
			double v = (textureY % 256) / 256.0;
			double u2 = ((textureX % 256) + width) / 256.0;
			double v2 = ((textureY % 256) + height) / 256.0;

			renderer.setTextureID(texID);
			renderer.drawTexturedRect(x, y, width, height, u, v, u2, v2);
		}
	}

	/**
	 * Unload the images associated with each Image loaded from a file.
	 * The Image objects are left behind, but given the texID of -1, so
	 * that they will be reloaded when next requested.
	 */
	public static void unloadFileTextures()
	{
		for(Image image: fileCache.values())
		{
			GL11.glDeleteTextures(image.texID);
			image.texID = -1;
		}
	}
}
