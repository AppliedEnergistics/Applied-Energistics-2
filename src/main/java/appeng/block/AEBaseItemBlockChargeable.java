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

package appeng.block;


import appeng.api.config.AccessRestriction;
import appeng.api.config.PowerUnits;
import appeng.api.definitions.IBlockDefinition;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.core.Api;
import appeng.core.localization.GuiText;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.text.MessageFormat;
import java.util.List;


public class AEBaseItemBlockChargeable extends AEBaseItemBlock implements IAEItemPowerStorage
{

	public AEBaseItemBlockChargeable( final Block id )
	{
		super( id );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void addCheckedInformation( final ItemStack itemStack, final EntityPlayer player, final List<String> toolTip, final boolean advancedTooltips )
	{
		final NBTTagCompound tag = itemStack.getTagCompound();
		double internalCurrentPower = 0;
		final double internalMaxPower = this.getMaxEnergyCapacity();

		if( tag != null )
		{
			internalCurrentPower = tag.getDouble( "internalCurrentPower" );
		}

		final double percent = internalCurrentPower / internalMaxPower;

		toolTip.add( GuiText.StoredEnergy.getLocal() + ':' + MessageFormat.format( " {0,number,#} ", internalCurrentPower ) + Platform.gui_localize( PowerUnits.AE.unlocalizedName ) + " - " + MessageFormat.format( " {0,number,#.##%} ", percent ) );
	}

	private double getMaxEnergyCapacity()
	{
		final Block blockID = Block.getBlockFromItem( this );
		final IBlockDefinition energyCell = Api.INSTANCE.definitions().blocks().energyCell();
		for( final Block block : energyCell.maybeBlock().asSet() )
		{
			if( blockID == block )
			{
				return 200000;
			}
			else
			{
				return 8 * 200000;
			}
		}

		return 0;
	}

	@Override
	public double injectAEPower( final ItemStack is, double amt )
	{
		double internalCurrentPower = this.getInternal( is );
		final double internalMaxPower = this.getMaxEnergyCapacity();
		internalCurrentPower += amt;
		if( internalCurrentPower > internalMaxPower )
		{
			amt = internalCurrentPower - internalMaxPower;
			internalCurrentPower = internalMaxPower;
			this.setInternal( is, internalCurrentPower );
			return amt;
		}

		this.setInternal( is, internalCurrentPower );
		return 0;
	}

	private double getInternal( final ItemStack is )
	{
		final NBTTagCompound nbt = Platform.openNbtData( is );
		return nbt.getDouble( "internalCurrentPower" );
	}

	private void setInternal( final ItemStack is, final double amt )
	{
		final NBTTagCompound nbt = Platform.openNbtData( is );
		nbt.setDouble( "internalCurrentPower", amt );
	}

	@Override
	public double extractAEPower( final ItemStack is, double amt )
	{
		double internalCurrentPower = this.getInternal( is );
		if( internalCurrentPower > amt )
		{
			internalCurrentPower -= amt;
			this.setInternal( is, internalCurrentPower );
			return amt;
		}

		amt = internalCurrentPower;
		this.setInternal( is, 0 );
		return amt;
	}

	@Override
	public double getAEMaxPower( final ItemStack is )
	{
		return this.getMaxEnergyCapacity();
	}

	@Override
	public double getAECurrentPower( final ItemStack is )
	{
		return this.getInternal( is );
	}

	@Override
	public AccessRestriction getPowerFlow( final ItemStack is )
	{
		return AccessRestriction.WRITE;
	}
}
