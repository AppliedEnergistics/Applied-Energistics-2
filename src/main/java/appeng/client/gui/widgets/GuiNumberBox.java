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

package appeng.client.gui.widgets;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;


public class GuiNumberBox extends GuiTextField
{
	
	final Class type;
	
	public GuiNumberBox(FontRenderer p_i1032_1_, int p_i1032_2_, int p_i1032_3_, int p_i1032_4_, int p_i1032_5_,Class type) {
		super( p_i1032_1_, p_i1032_2_, p_i1032_3_, p_i1032_4_, p_i1032_5_ );
		this.type = type;
	}

	@Override
	public void writeText(String p_146191_1_)
	{
		String original = getText();
		super.writeText( p_146191_1_ );
		
		try
		{
			if ( type == int.class || type == Integer.class )
				Integer.parseInt( getText() );
			else if ( type == long.class || type == Long.class )
				Long.parseLong( getText() );
			else if ( type == double.class || type == Double.class )
				Double.parseDouble( getText() );
		}
		catch(NumberFormatException e )
		{
			setText( original );
		}
	}
	

}
