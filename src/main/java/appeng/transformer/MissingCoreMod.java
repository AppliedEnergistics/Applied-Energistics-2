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

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiErrorScreen;
import cpw.mods.fml.client.CustomModLoadingErrorDisplayException;

public class MissingCoreMod extends CustomModLoadingErrorDisplayException
{

	private static final long serialVersionUID = -966774766922821652L;
	private boolean deobf = false;

	@Override
	public void initGui(GuiErrorScreen errorScreen, FontRenderer fontRenderer)
	{
		Class clz = errorScreen.getClass();
		try
		{
			clz.getField( "mc" );
			this.deobf = true;
		}
		catch (Throwable ignored)
		{

		}
	}

	@Override
	public void drawScreen(GuiErrorScreen errorScreen, FontRenderer fontRenderer, int mouseRelX, int mouseRelY, float tickTime)
	{
		int offset = 10;
		this.drawCenteredString( fontRenderer, "Sorry, couldn't load AE2 Properly.", errorScreen.width / 2, offset += 15, 0xffffff );
		this.drawCenteredString( fontRenderer, "Please make sure that AE2 is installed into your mods folder.", errorScreen.width / 2, offset += 15, 0xeeeeee );

		offset += 15;

		if ( this.deobf )
		{
			offset += 15;
			this.drawCenteredString( fontRenderer, "In a developer environment add the following too your args,", errorScreen.width / 2, offset += 15, 0xffffff );
			this.drawCenteredString( fontRenderer, "-Dfml.coreMods.load=appeng.transformer.AppEngCore", errorScreen.width / 2, offset += 15, 0xeeeeee );
		}
		else
		{
			this.drawCenteredString( fontRenderer, "You're launcher may refer to this by different names,", errorScreen.width / 2, offset += 15, 0xffffff );

			offset += 5;

			this.drawCenteredString( fontRenderer, "MultiMC calls this tab \"Loader Mods\"", errorScreen.width / 2, offset += 15, 0xeeeeee );
			this.drawCenteredString( fontRenderer, "Magic Launcher calls this tab \"External Mods\"", errorScreen.width / 2, offset += 15, 0xeeeeee );
			this.drawCenteredString( fontRenderer, "Most other launchers refer to this tab as just \"Mods\"", errorScreen.width / 2, offset += 15, 0xeeeeee );

			offset += 15;

			this.drawCenteredString( fontRenderer, "Also make sure that the AE2 file is a .jar, and not a .zip", errorScreen.width / 2, offset += 15, 0xffffff );
		}
	}

	public void drawCenteredString(FontRenderer fontRenderer, String string, int x, int y, int colour)
	{
		fontRenderer.drawStringWithShadow( string, x - fontRenderer.getStringWidth( string.replaceAll( "\\P{InBasic_Latin}", "" ) ) / 2, y, colour );
	}
}