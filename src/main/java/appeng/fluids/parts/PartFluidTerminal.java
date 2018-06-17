/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

package appeng.fluids.parts;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import appeng.api.parts.IPartModel;
import appeng.core.AppEng;
import appeng.core.sync.GuiBridge;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;
import appeng.parts.reporting.AbstractPartTerminal;


/**
 * @author BrockWS
 * @version rv6 - 12/05/2018
 * @since rv6 12/05/2018
 */
public class PartFluidTerminal extends AbstractPartTerminal
{

	@PartModels
	public static final ResourceLocation MODEL_OFF = new ResourceLocation( AppEng.MOD_ID, "part/fluid_terminal_off" );
	@PartModels
	public static final ResourceLocation MODEL_ON = new ResourceLocation( AppEng.MOD_ID, "part/fluid_terminal_on" );

	public static final IPartModel MODELS_OFF = new PartModel( MODEL_BASE, MODEL_OFF, MODEL_STATUS_OFF );
	public static final IPartModel MODELS_ON = new PartModel( MODEL_BASE, MODEL_ON, MODEL_STATUS_ON );
	public static final IPartModel MODELS_HAS_CHANNEL = new PartModel( MODEL_BASE, MODEL_ON, MODEL_STATUS_HAS_CHANNEL );

	public PartFluidTerminal( ItemStack is )
	{
		super( is );
	}

	@Override
	public GuiBridge getGui( EntityPlayer player )
	{
		return GuiBridge.GUI_FLUID_TERMINAL;
	}

	@Override
	public IPartModel getStaticModels()
	{
		return this.selectModel( MODELS_OFF, MODELS_ON, MODELS_HAS_CHANNEL );
	}
}
