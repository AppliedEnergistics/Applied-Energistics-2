/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 AlgorithmX2
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package appeng.api.util;


import java.util.Arrays;
import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.text.translation.I18n;


/**
 * List of all colors supported by AE, their names, and various colors for display.
 *
 * Should be the same order as Dyes, excluding Transparent.
 */
public enum AEColor
{

	WHITE( "gui.appliedenergistics2.White", EnumDyeColor.WHITE, 0xBEBEBE, 0xDBDBDB, 0xFAFAFA ),

	ORANGE( "gui.appliedenergistics2.Orange", EnumDyeColor.ORANGE, 0xF99739, 0xFAAE44, 0xF4DEC3 ),

	MAGENTA( "gui.appliedenergistics2.Magenta", EnumDyeColor.MAGENTA, 0x821E82, 0xB82AB8, 0xC598C8 ),

	LIGHT_BLUE( "gui.appliedenergistics2.LightBlue", EnumDyeColor.LIGHT_BLUE, 0x628DCB, 0x82ACE7, 0xD8F6FF ),

	YELLOW( "gui.appliedenergistics2.Yellow", EnumDyeColor.YELLOW, 0xFFF7AA, 0xF8FF4A, 0xFFFFE8 ),

	LIME( "gui.appliedenergistics2.Lime", EnumDyeColor.LIME, 0x7CFF4A, 0xBBFF51, 0xE7F7D7 ),

	PINK( "gui.appliedenergistics2.Pink", EnumDyeColor.PINK, 0xDC8DB5, 0xF8B5D7, 0xF7DEEB ),

	GRAY( "gui.appliedenergistics2.Gray", EnumDyeColor.GRAY, 0x7C7C7C, 0xA0A0A0, 0xC9C9C9 ),

	LIGHT_GRAY( "gui.appliedenergistics2.LightGray", EnumDyeColor.SILVER, 0x9D9D9D, 0xCDCDCD, 0xEFEFEF ),

	CYAN( "gui.appliedenergistics2.Cyan", EnumDyeColor.CYAN, 0x2F9BA5, 0x51AAC6, 0xAEDDF4 ),

	PURPLE( "gui.appliedenergistics2.Purple", EnumDyeColor.PURPLE, 0x8230B2, 0xA453CE, 0xC7A3CC ),

	BLUE( "gui.appliedenergistics2.Blue", EnumDyeColor.BLUE, 0x2D29A0, 0x514AFF, 0xDDE6FF ),

	BROWN( "gui.appliedenergistics2.Brown", EnumDyeColor.BROWN, 0x724E35, 0xB7967F, 0xE0D2C8 ),

	GREEN( "gui.appliedenergistics2.Green", EnumDyeColor.GREEN, 0x45A021, 0x60E32E, 0xE3F2E3 ),

	RED( "gui.appliedenergistics2.Red", EnumDyeColor.RED, 0xA50029, 0xFF003C, 0xFFE6ED ),

	BLACK( "gui.appliedenergistics2.Black", EnumDyeColor.BLACK, 0x2B2B2B, 0x565656, 0x848484 ),

	TRANSPARENT( "gui.appliedenergistics2.Fluix", null, 0x1B2344, 0x895CA8, 0xD7BBEC );

	public static final List<AEColor> VALID_COLORS = Arrays.asList( WHITE, ORANGE, MAGENTA, LIGHT_BLUE, YELLOW, LIME, PINK, GRAY, LIGHT_GRAY, CYAN, PURPLE,
			BLUE, BROWN, GREEN, RED, BLACK );

	/**
	 * The {@link BakedQuad#getTintIndex() tint index} that can normally be used to get the {@link #blackVariant dark
	 * variant} of the apprioriate AE color.
	 */
	public static final int TINTINDEX_DARK = 1;

	/**
	 * The {@link BakedQuad#getTintIndex() tint index} that can normally be used to get the {@link #mediumVariant medium
	 * variant} of the apprioriate AE color.
	 */
	public static final int TINTINDEX_MEDIUM = 2;

	/**
	 * The {@link BakedQuad#getTintIndex() tint index} that can normally be used to get the {@link #whiteVariant bright
	 * variant} of the apprioriate AE color.
	 */
	public static final int TINTINDEX_BRIGHT = 3;

	/**
	 * The {@link BakedQuad#getTintIndex() tint index} that can normally be used to get a color between the
	 * {@link #mediumVariant medium}
	 * and {@link #whiteVariant bright variant} of the apprioriate AE color.
	 */
	public static final int TINTINDEX_MEDIUM_BRIGHT = 4;

	/**
	 * Unlocalized name for color.
	 */
	public final String unlocalizedName;

	/**
	 * Darkest Variant of the color, nearly black; as a RGB HEX Integer
	 */
	public final int blackVariant;

	/**
	 * The Variant of the color that is used to represent the color normally; as a RGB HEX Integer
	 */
	public final int mediumVariant;

	/**
	 * Lightest Variant of the color, nearly white; as a RGB HEX Integer
	 */
	public final int whiteVariant;

	/**
	 * Vanilla Dye Equivilient
	 */
	public final EnumDyeColor dye;

	AEColor( final String unlocalizedName, final EnumDyeColor dye, final int blackHex, final int medHex, final int whiteHex )
	{
		this.unlocalizedName = unlocalizedName;
		this.blackVariant = blackHex;
		this.mediumVariant = medHex;
		this.whiteVariant = whiteHex;
		this.dye = dye;
	}

	/**
	 * Will return a variant of this color based on the given tint index.
	 *
	 * @param tintIndex A tint index as it can be used for {@link BakedQuad#getTintIndex()}.
	 * @return The appropriate color variant, or -1.
	 */
	public int getVariantByTintIndex( int tintIndex )
	{
		switch( tintIndex )
		{
			// Please note that tintindex 0 is hardcoded for the block breaking particles. Returning anything other than
			// -1 for tintindex=0 here
			// will cause issues with those particles
			case 0:
				return -1;
			case TINTINDEX_DARK:
				return this.blackVariant;
			case TINTINDEX_MEDIUM:
				return this.mediumVariant;
			case TINTINDEX_BRIGHT:
				return this.whiteVariant;
			case TINTINDEX_MEDIUM_BRIGHT:
				final int light = this.whiteVariant;
				final int dark = this.mediumVariant;
				return ( ( ( ( ( light >> 16 ) & 0xff ) + ( ( dark >> 16 ) & 0xff ) ) / 2 ) << 16 ) | ( ( ( ( ( light >> 8 ) & 0xff ) + ( ( dark >> 8 ) & 0xff ) ) / 2 ) << 8 ) | ( ( ( ( light ) & 0xff ) + ( ( dark ) & 0xff ) ) / 2 );
			default:
				return -1;
		}
	}

	/**
	 * Logic to see which colors match each other.. special handle for Transparent
	 */
	public boolean matches( final AEColor color )
	{
		return this == TRANSPARENT || color == TRANSPARENT || this == color;
	}

	@Override
	public String toString()
	{
		return I18n.translateToLocal( this.unlocalizedName );
	}

}
