/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.transformer;


import cpw.mods.fml.client.CustomModLoadingErrorDisplayException;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiErrorScreen;


@SideOnly( Side.CLIENT )
public final class MissingCoreMod extends CustomModLoadingErrorDisplayException
{
	private static final int SHADOW_WHITE = 0xeeeeee;
	private static final int COLOR_WHITE = 0xffffff;
	private static final long serialVersionUID = -966774766922821652L;
	private static final int SCREEN_OFFSET = 15;

	private boolean deobf = false;

	@Override
	public void initGui( final GuiErrorScreen errorScreen, final FontRenderer fontRenderer )
	{
		final Class<?> clz = errorScreen.getClass();
		try
		{
			clz.getField( "mc" );
			this.deobf = true;
		}
		catch( final Throwable ignored )
		{

		}
	}

	@Override
	public void drawScreen( final GuiErrorScreen errorScreen, final FontRenderer fontRenderer, final int mouseRelX, final int mouseRelY, final float tickTime )
	{
		int offset = 10;
		this.drawCenteredString( fontRenderer, "Sorry, couldn't load AE2 properly.", errorScreen.width / 2, offset, COLOR_WHITE );

		offset += SCREEN_OFFSET;
		this.drawCenteredString( fontRenderer, "Please make sure that AE2 is installed into your mods folder.", errorScreen.width / 2, offset, SHADOW_WHITE );

		offset += 2 * SCREEN_OFFSET;

		if( this.deobf )
		{
			offset += SCREEN_OFFSET;
			this.drawCenteredString( fontRenderer, "In a developer environment add the following too your args,", errorScreen.width / 2, offset, COLOR_WHITE );

			offset += SCREEN_OFFSET;
			this.drawCenteredString( fontRenderer, "-Dfml.coreMods.load=appeng.transformer.AppEngCore", errorScreen.width / 2, offset, SHADOW_WHITE );
		}
		else
		{
			this.drawCenteredString( fontRenderer, "You're launcher may refer to this by different names,", errorScreen.width / 2, offset, COLOR_WHITE );

			offset += SCREEN_OFFSET + 5;

			this.drawCenteredString( fontRenderer, "MultiMC calls this tab \"Loader Mods\"", errorScreen.width / 2, offset, SHADOW_WHITE );

			offset += SCREEN_OFFSET;
			this.drawCenteredString( fontRenderer, "Magic Launcher calls this tab \"External Mods\"", errorScreen.width / 2, offset, SHADOW_WHITE );

			offset += SCREEN_OFFSET;
			this.drawCenteredString( fontRenderer, "Most other launchers refer to this tab as just \"Mods\"", errorScreen.width / 2, offset, SHADOW_WHITE );

			offset += 2 * SCREEN_OFFSET;
			this.drawCenteredString( fontRenderer, "Also make sure that the AE2 file is a .jar, and not a .zip", errorScreen.width / 2, offset, COLOR_WHITE );
		}
	}

	private void drawCenteredString( final FontRenderer fontRenderer, final String string, final int x, final int y, final int colour )
	{
		final String reEncoded = string.replaceAll( "\\P{InBasic_Latin}", "" );
		final int reEncodedWidth = fontRenderer.getStringWidth( reEncoded );
		final int centeredX = x - reEncodedWidth / 2;

		fontRenderer.drawStringWithShadow( string, centeredX, y, colour );
	}
}