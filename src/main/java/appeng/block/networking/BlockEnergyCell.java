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

package appeng.block.networking;


import java.util.EnumSet;
import java.util.List;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.block.AEBaseItemBlock;
import appeng.block.AEBaseItemBlockChargeable;
import appeng.block.AEBaseTileBlock;
import appeng.core.features.AEFeature;
import appeng.helpers.AEGlassMaterial;
import appeng.tile.networking.TileEnergyCell;
import appeng.util.Platform;


public class BlockEnergyCell extends AEBaseTileBlock
{

	public static final PropertyInteger ENERGY_STORAGE = PropertyInteger.create( "fullness", 0, 8 );

	@Override
	public int getMetaFromState( final IBlockState state )
	{
		return (int) state.getValue( ENERGY_STORAGE );
	}

	@Override
	public IBlockState getStateFromMeta( final int meta )
	{
		return this.getDefaultState().withProperty( ENERGY_STORAGE, Math.min( 7, Math.max( 0, meta ) ) );
	}

	public BlockEnergyCell()
	{
		super( AEGlassMaterial.INSTANCE );

		this.setTileEntity( TileEnergyCell.class );
		this.setFeature( EnumSet.of( AEFeature.Core ) );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void getCheckedSubBlocks( final Item item, final CreativeTabs tabs, final List<ItemStack> itemStacks )
	{
		super.getCheckedSubBlocks( item, tabs, itemStacks );

		final ItemStack charged = new ItemStack( this, 1 );
		final NBTTagCompound tag = Platform.openNbtData( charged );
		tag.setDouble( "internalCurrentPower", this.getMaxPower() );
		tag.setDouble( "internalMaxPower", this.getMaxPower() );

		itemStacks.add( charged );
	}

	public double getMaxPower()
	{
		return 200000.0;
	}

	@Override
	protected IProperty[] getAEStates()
	{
		return new IProperty[] { AE_BLOCK_FORWARD, AE_BLOCK_UP, ENERGY_STORAGE };
	}

	@Override
	public Class<? extends AEBaseItemBlock> getItemBlockClass()
	{
		return AEBaseItemBlockChargeable.class;
	}

	/**
	 * Helper method that returns the energy fill factor (between 0 and 1) of a given item stack.
	 * Returns 0 if the item stack has no fill factor.
	 */
	private static double getFillFactor( ItemStack is ) {
		if( !( is.getItem() instanceof IAEItemPowerStorage ) )
		{
			return 0;
		}

		AEBaseItemBlockChargeable itemChargeable = (AEBaseItemBlockChargeable) is.getItem();
		double curPower = itemChargeable.getAECurrentPower( is );
		double maxPower = itemChargeable.getAEMaxPower( is );

		return curPower / maxPower;
	}

	/**
	 * Determines which version of the energy cell model should be used depending on the fill factor
	 * of the item stack.
	 */
	@SideOnly( Side.CLIENT )
	@Override
	public ItemMeshDefinition getItemMeshDefinition()
	{
		return is -> {
			double fillFactor = getFillFactor( is );

			int storageLevel = TileEnergyCell.getStorageLevelFromFillFactor(fillFactor);

			return new ModelResourceLocation( "appliedenergistics2:tile.BlockEnergyCell", "fullness=" + storageLevel );

		};
	}

}
