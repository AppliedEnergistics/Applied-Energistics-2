package appeng.api.util;

import net.minecraft.util.StatCollector;

/**
 * List of all colors supported by AE, their names, and various colors for display.
 * 
 * Should be the same order as Dyes, excluding Transparent.
 */
public enum AEColor
{

	White("gui.appliedenergistics2.White", 0xBEBEBE, 0xDBDBDB, 0xFAFAFA),

	Orange("gui.appliedenergistics2.Orange", 0xF99739, 0xFAAE44, 0xF4DEC3),

	Magenta("gui.appliedenergistics2.Magenta", 0x821E82, 0xB82AB8, 0xC598C8),

	LightBlue("gui.appliedenergistics2.LightBlue", 0x628DCB, 0x82ACE7, 0xD8F6FF),

	Yellow("gui.appliedenergistics2.Yellow", 0xFFF7AA, 0xF8FF4A, 0xFFFFE8),

	Lime("gui.appliedenergistics2.Lime", 0x7CFF4A, 0xBBFF51, 0xE7F7D7),

	Pink("gui.appliedenergistics2.Pink", 0xDC8DB5, 0xF8B5D7, 0xF7DEEB),

	Gray("gui.appliedenergistics2.Gray", 0x7C7C7C, 0xA0A0A0, 0xC9C9C9),

	LightGray("gui.appliedenergistics2.LightGray", 0x9D9D9D, 0xCDCDCD, 0xEFEFEF),

	Cyan("gui.appliedenergistics2.Cyan", 0x2F9BA5, 0x51AAC6, 0xAEDDF4),

	Purple("gui.appliedenergistics2.Purple", 0x8230B2, 0xA453CE, 0xC7A3CC),

	Blue("gui.appliedenergistics2.Blue", 0x2D29A0, 0x514AFF, 0xDDE6FF),

	Brown("gui.appliedenergistics2.Brown", 0x724E35, 0xB7967F, 0xE0D2C8),

	Green("gui.appliedenergistics2.Green", 0x45A021, 0x60E32E, 0xE3F2E3),

	Red("gui.appliedenergistics2.Red", 0xA50029, 0xFF003C, 0xFFE6ED),

	Black("gui.appliedenergistics2.Black", 0x2B2B2B, 0x565656, 0x848484),

	Transparent("gui.appliedenergistics2.Fluix", 0x1B2344, 0x895CA8, 0xD7BBEC);

	/**
	 * Unlocalized name for color.
	 */
	final public String unlocalizedName;

	/**
	 * Darkest Variant of the color, nearly black; as a RGB HEX Integer
	 */
	final public int blackVariant;

	/**
	 * The Variant of the color that is used to represent the color normally; as a RGB HEX Integer
	 */
	final public int mediumVariant;

	/**
	 * Lightest Variant of the color, nearly white; as a RGB HEX Integer
	 */
	final public int whiteVariant;

	AEColor(String unlocalizedName, int blackHex, int medHex, int whiteHex) {
		this.unlocalizedName = unlocalizedName;
		blackVariant = blackHex;
		mediumVariant = medHex;
		whiteVariant = whiteHex;
	}

	/**
	 * Logic to see which colors match each other.. special handle for Transparent
	 */
	public boolean matches(AEColor color)
	{
		return equals(Transparent) || color.equals(Transparent) || equals(color);
	}

	@Override
	public String toString()
	{
		return StatCollector.translateToLocal( unlocalizedName );
	}

}
