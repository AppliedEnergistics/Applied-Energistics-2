package mekanism.api;

import net.minecraft.util.StatCollector;

/**
 * Simple color enum for adding colors to in-game GUI strings of text.
 * @author AidanBrady
 *
 */
public enum EnumColor
{
	BLACK("\u00a70", "black", "Black", new int[] {0, 0, 0}, 0),
	DARK_BLUE("\u00a71", "darkBlue", "Blue", new int[] {0, 0, 170}, 4),
	DARK_GREEN("\u00a72", "darkGreen", "Green", new int[] {0, 170, 0}, 2),
	DARK_AQUA("\u00a73", "darkAqua", "Cyan", new int[] {0, 170, 170}, 6),
	DARK_RED("\u00a74", "darkRed", null, new int[] {170, 0, 0}, -1),
	PURPLE("\u00a75", "purple", "Purple", new int[] {170, 0, 170}, 5),
	ORANGE("\u00a76", "orange", "Orange", new int[] {255, 170, 0}, 14),
	GREY("\u00a77", "grey", "LightGray", new int[] {170, 170, 170}, 7),
	DARK_GREY("\u00a78", "darkGrey", "Gray", new int[] {85, 85, 85}, 8),
	INDIGO("\u00a79", "indigo", "LightBlue", new int[] {85, 85, 255}, 12),
	BRIGHT_GREEN("\u00a7a", "brightGreen", "Lime", new int[] {85, 255, 85}, 10),
	AQUA("\u00a7b", "aqua", null, new int[] {85, 255, 255}, -1),
	RED("\u00a7c", "red", "Red", new int[] {255, 0, 0}, 1),
	PINK("\u00a7d", "pink", "Magenta", new int[] {255, 85, 255}, 13),
	YELLOW("\u00a7e", "yellow", "Yellow", new int[] {255, 255, 85}, 11),
	WHITE("\u00a7f", "white", "White", new int[] {255, 255, 255}, 15),
	//Extras for dye-completeness
	BROWN("\u00a76", "brown", "Brown", new int[] {150, 75, 0}, 3),
	BRIGHT_PINK("\u00a7d", "brightPink", "Pink", new int[] {255, 192, 203}, 9);

	public static EnumColor[] DYES = new EnumColor[] {BLACK, RED, DARK_GREEN, BROWN, DARK_BLUE, PURPLE, DARK_AQUA, GREY, DARK_GREY, BRIGHT_PINK, BRIGHT_GREEN, YELLOW, INDIGO, PINK, ORANGE, WHITE};

	/** The color code that will be displayed */
	public final String code;

	public final int[] rgbCode;

	public final int mcMeta;

	/** A friendly name of the color. */
	public String unlocalizedName;
	
	public String dyeName;

	private EnumColor(String s, String n, String dye, int[] rgb, int meta)
	{
		code = s;
		unlocalizedName = n;
		dyeName = dye;
		rgbCode = rgb;
		mcMeta = meta;
	}

	/**
	 * Gets the localized name of this color by translating the unlocalized name.
	 * @return localized name
	 */
	public String getLocalizedName()
	{
		return StatCollector.translateToLocal("color." + unlocalizedName);
	}

	public String getDyeName()
	{
		return StatCollector.translateToLocal("dye." + unlocalizedName);
	}
	
	public String getOreDictName()
	{
		return dyeName;
	}

	/**
	 * Gets the name of this color with it's color prefix code.
	 * @return the color's name and color prefix
	 */
	public String getName()
	{
		return code + getLocalizedName();
	}

	public String getDyedName()
	{
		return code + getDyeName();
	}

	/**
	 * Gets the 0-1 of this color's RGB value by dividing by 255 (used for OpenGL coloring).
	 * @param index - R:0, G:1, B:2
	 * @return the color value
	 */
	public float getColor(int index)
	{
		return (float)rgbCode[index]/255F;
	}

	/**
	 * Gets the value of this color mapped to MC in-game item colors present in dyes and wool.
	 * @return mc meta value
	 */
	public int getMetaValue()
	{
		return mcMeta;
	}

	@Override
	public String toString()
	{
		return code;
	}
}
