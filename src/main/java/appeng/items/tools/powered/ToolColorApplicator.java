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

package appeng.items.tools.powered;


import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;

import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.BlockStainedGlassPane;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemSnowball;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.oredict.OreDictionary;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.implementations.items.IItemGroup;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.implementations.tiles.IColorableTile;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import appeng.block.misc.BlockPaint;
import appeng.block.networking.BlockCableBus;
import appeng.client.render.items.ToolColorApplicatorRender;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.helpers.IMouseWheelItem;
import appeng.hooks.DispenserBlockTool;
import appeng.hooks.IBlockTool;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.items.misc.ItemPaintBall;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.me.storage.CellInventoryHandler;
import appeng.tile.misc.TilePaint;
import appeng.util.ItemSorters;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;


public class ToolColorApplicator extends AEBasePoweredItem implements IStorageCell, IItemGroup, IBlockTool, IMouseWheelItem
{

	static final Map<Integer, AEColor> ORE_TO_COLOR = new HashMap<Integer, AEColor>();

	static
	{

		for( AEColor col : AEColor.values() )
		{
			if( col == AEColor.Transparent )
			{
				continue;
			}

			ORE_TO_COLOR.put( OreDictionary.getOreID( "dye" + col.name() ), col );
		}
	}

	public ToolColorApplicator()
	{
		super( AEConfig.instance.colorApplicatorBattery, Optional.<String>absent() );
		this.setFeature( EnumSet.of( AEFeature.ColorApplicator, AEFeature.PoweredTools ) );
		if( Platform.isClient() )
		{
			MinecraftForgeClient.registerItemRenderer( this, new ToolColorApplicatorRender() );
		}
	}

	@Override
	public void postInit()
	{
		super.postInit();
		BlockDispenser.dispenseBehaviorRegistry.putObject( this, new DispenserBlockTool() );
	}

	@Override
	public boolean onItemUse(
			ItemStack is,
			EntityPlayer p,
			World w,
			BlockPos pos,
			EnumFacing side,
			float hitX,
			float hitY,
			float hitZ )
	{
		Block blk = w.getBlockState( pos ).getBlock();
		double powerPerUse = 100;

		ItemStack paintBall = this.getColor( is );

		IMEInventory<IAEItemStack> inv = AEApi.instance().registries().cell().getCellInventory( is, null, StorageChannel.ITEMS );
		if( inv != null )
		{
			IAEItemStack option = inv.extractItems( AEItemStack.create( paintBall ), Actionable.SIMULATE, new BaseActionSource() );

			if( option != null )
			{
				paintBall = option.getItemStack();
				paintBall.stackSize = 1;
			}
			else
			{
				paintBall = null;
			}

			if( !Platform.hasPermissions( new DimensionalCoord( w, pos ), p ) )
			{
				return false;
			}

			if( paintBall != null && paintBall.getItem() instanceof ItemSnowball )
			{
				TileEntity te = w.getTileEntity( pos );
				// clean cables.
				if( te instanceof IColorableTile )
				{
					if( this.getAECurrentPower( is ) > powerPerUse && ( (IColorableTile) te ).getColor() != AEColor.Transparent )
					{
						if( ( (IColorableTile) te ).recolourBlock( side, AEColor.Transparent, p ) )
						{
							inv.extractItems( AEItemStack.create( paintBall ), Actionable.MODULATE, new BaseActionSource() );
							this.extractAEPower( is, powerPerUse );
							return true;
						}
					}
				}

				// clean paint balls..
				Block testBlk = w.getBlockState(  pos.offset( side ) ).getBlock();
				TileEntity painted = w.getTileEntity( pos.offset( side ) );
				if( this.getAECurrentPower( is ) > powerPerUse && testBlk instanceof BlockPaint && painted instanceof TilePaint )
				{
					inv.extractItems( AEItemStack.create( paintBall ), Actionable.MODULATE, new BaseActionSource() );
					this.extractAEPower( is, powerPerUse );
					( (TilePaint) painted ).cleanSide( side.getOpposite() );
					return true;
				}
			}
			else if( paintBall != null )
			{
				AEColor color = this.getColorFromItem( paintBall );

				if( color != null && this.getAECurrentPower( is ) > powerPerUse )
				{
					if( color != AEColor.Transparent && this.recolourBlock( blk, side, w, pos, side, color, p ) )
					{
						inv.extractItems( AEItemStack.create( paintBall ), Actionable.MODULATE, new BaseActionSource() );
						this.extractAEPower( is, powerPerUse );
						return true;
					}
				}
			}
		}

		if( p.isSneaking() )
		{
			this.cycleColors( is, paintBall, 1 );
		}

		return false;
	}

	@Override
	public String getItemStackDisplayName( ItemStack par1ItemStack )
	{
		String extra = GuiText.Empty.getLocal();

		AEColor selected = this.getActiveColor( par1ItemStack );

		if( selected != null && Platform.isClient() )
		{
			extra = Platform.gui_localize( selected.unlocalizedName );
		}

		return super.getItemStackDisplayName( par1ItemStack ) + " - " + extra;
	}

	public AEColor getActiveColor( ItemStack tol )
	{
		return this.getColorFromItem( this.getColor( tol ) );
	}

	public AEColor getColorFromItem( ItemStack paintBall )
	{
		if( paintBall == null )
		{
			return null;
		}

		if( paintBall.getItem() instanceof ItemSnowball )
		{
			return AEColor.Transparent;
		}

		if( paintBall.getItem() instanceof ItemPaintBall )
		{
			ItemPaintBall ipb = (ItemPaintBall) paintBall.getItem();
			return ipb.getColor( paintBall );
		}
		else
		{
			int[] id = OreDictionary.getOreIDs( paintBall );

			for( int oreID : id )
			{
				if( ORE_TO_COLOR.containsKey( oreID ) )
				{
					return ORE_TO_COLOR.get( oreID );
				}
			}
		}

		return null;
	}

	public ItemStack getColor( ItemStack is )
	{
		NBTTagCompound c = is.getTagCompound();
		if( c != null && c.hasKey( "color" ) )
		{
			NBTTagCompound color = c.getCompoundTag( "color" );
			ItemStack oldColor = ItemStack.loadItemStackFromNBT( color );
			if( oldColor != null )
			{
				return oldColor;
			}
		}

		return this.findNextColor( is, null, 0 );
	}

	private ItemStack findNextColor( ItemStack is, ItemStack anchor, int scrollOffset )
	{
		ItemStack newColor = null;

		IMEInventory<IAEItemStack> inv = AEApi.instance().registries().cell().getCellInventory( is, null, StorageChannel.ITEMS );
		if( inv != null )
		{
			IItemList<IAEItemStack> itemList = inv.getAvailableItems( AEApi.instance().storage().createItemList() );
			if( anchor == null )
			{
				IAEItemStack firstItem = itemList.getFirstItem();
				if( firstItem != null )
				{
					newColor = firstItem.getItemStack();
				}
			}
			else
			{
				LinkedList<IAEItemStack> list = new LinkedList<IAEItemStack>();

				for( IAEItemStack i : itemList )
				{
					list.add( i );
				}

				Collections.sort( list, new Comparator<IAEItemStack>()
				{

					@Override
					public int compare( IAEItemStack a, IAEItemStack b )
					{
						return ItemSorters.compareInt( a.getItemDamage(), b.getItemDamage() );
					}
				} );

				if( list.size() <= 0 )
				{
					return null;
				}

				IAEItemStack where = list.getFirst();
				int cycles = 1 + list.size();

				while( cycles > 0 && !where.equals( anchor ) )
				{
					list.addLast( list.removeFirst() );
					cycles--;
					where = list.getFirst();
				}

				if( scrollOffset > 0 )
				{
					list.addLast( list.removeFirst() );
				}

				if( scrollOffset < 0 )
				{
					list.addFirst( list.removeLast() );
				}

				return list.get( 0 ).getItemStack();
			}
		}

		if( newColor != null )
		{
			this.setColor( is, newColor );
		}

		return newColor;
	}

	public void setColor( ItemStack is, ItemStack newColor )
	{
		NBTTagCompound data = Platform.openNbtData( is );
		if( newColor == null )
		{
			data.removeTag( "color" );
		}
		else
		{
			NBTTagCompound color = new NBTTagCompound();
			newColor.writeToNBT( color );
			data.setTag( "color", color );
		}
	}

	private boolean recolourBlock( Block blk, EnumFacing side, World w, BlockPos pos, EnumFacing orientation, AEColor newColor, EntityPlayer p )
	{
		IBlockState state = w.getBlockState( pos );
		
		if( blk instanceof BlockColored )
		{
			EnumDyeColor color = ( EnumDyeColor ) state.getValue( BlockColored.COLOR );
			
			if( newColor.dye == color )
			{
				return false;
			}
			
			return w.setBlockState( pos, state.withProperty( BlockColored.COLOR, newColor.dye ) );
		}

		if( blk == Blocks.glass )
		{
			return w.setBlockState( pos, Blocks.stained_glass.getDefaultState().withProperty(  BlockStainedGlass.COLOR, newColor.dye)  );
		}

		if( blk == Blocks.stained_glass )
		{
			EnumDyeColor color = ( EnumDyeColor ) state.getValue( BlockStainedGlass.COLOR );
			
			if( newColor.dye == color )
			{
				return false;
			}
			
			return w.setBlockState( pos, state.withProperty( BlockStainedGlass.COLOR, newColor.dye ) );
		}

		if( blk == Blocks.glass_pane )
		{
			return w.setBlockState( pos, Blocks.stained_glass_pane.getDefaultState().withProperty(  BlockStainedGlassPane.COLOR, newColor.dye)  );
		}

		if( blk == Blocks.stained_glass_pane )
		{
			EnumDyeColor color = ( EnumDyeColor ) state.getValue( BlockStainedGlassPane.COLOR );
			
			if( newColor.dye == color )
			{
				return false;
			}
			
			return w.setBlockState( pos, state.withProperty( BlockStainedGlassPane.COLOR, newColor.dye ) );
		}

		if( blk == Blocks.hardened_clay )
		{
			return w.setBlockState( pos, Blocks.stained_hardened_clay.getDefaultState().withProperty( BlockColored.COLOR, newColor.dye ) );
		}

		if( blk instanceof BlockCableBus )
		{
			return ( (BlockCableBus) blk ).recolorBlock( w, pos, side, newColor.dye, p );
		}

		return blk.recolorBlock( w, pos, side, newColor.dye );
	}

	public void cycleColors( ItemStack is, ItemStack paintBall, int i )
	{
		if( paintBall == null )
		{
			this.setColor( is, this.getColor( is ) );
		}
		else
		{
			this.setColor( is, this.findNextColor( is, paintBall, i ) );
		}
	}

	@Override
	public void addCheckedInformation( ItemStack stack, EntityPlayer player, List<String> lines, boolean displayMoreInfo )
	{
		super.addCheckedInformation( stack, player, lines, displayMoreInfo );

		IMEInventory<IAEItemStack> cdi = AEApi.instance().registries().cell().getCellInventory( stack, null, StorageChannel.ITEMS );

		if( cdi instanceof CellInventoryHandler )
		{
			ICellInventory cd = ( (ICellInventoryHandler) cdi ).getCellInv();
			if( cd != null )
			{
				lines.add( cd.getUsedBytes() + " " + GuiText.Of.getLocal() + ' ' + cd.getTotalBytes() + ' ' + GuiText.BytesUsed.getLocal() );
				lines.add( cd.getStoredItemTypes() + " " + GuiText.Of.getLocal() + ' ' + cd.getTotalItemTypes() + ' ' + GuiText.Types.getLocal() );
			}
		}
	}

	@Override
	public int getBytes( ItemStack cellItem )
	{
		return 512;
	}

	@Override
	public int getBytesPerType( ItemStack cellItem )
	{
		return 8;
	}

	@Override
	public int getTotalTypes( ItemStack cellItem )
	{
		return 27;
	}

	@Override
	public boolean isBlackListed( ItemStack cellItem, IAEItemStack requestedAddition )
	{
		if( requestedAddition != null )
		{
			int[] id = OreDictionary.getOreIDs( requestedAddition.getItemStack() );

			for( int x : id )
			{
				if( ORE_TO_COLOR.containsKey( x ) )
				{
					return false;
				}
			}

			if( requestedAddition.getItem() instanceof ItemSnowball )
			{
				return false;
			}

			return !( requestedAddition.getItem() instanceof ItemPaintBall && requestedAddition.getItemDamage() < 20 );
		}
		return true;
	}

	@Override
	public boolean storableInStorageCell()
	{
		return true;
	}

	@Override
	public boolean isStorageCell( ItemStack i )
	{
		return true;
	}

	@Override
	public double getIdleDrain()
	{
		return 0.5;
	}

	@Override
	public String getUnlocalizedGroupName( Set<ItemStack> others, ItemStack is )
	{
		return GuiText.StorageCells.getUnlocalized();
	}

	@Override
	public boolean isEditable( ItemStack is )
	{
		return true;
	}

	@Override
	public IInventory getUpgradesInventory( ItemStack is )
	{
		return new CellUpgrades( is, 2 );
	}

	@Override
	public IInventory getConfigInventory( ItemStack is )
	{
		return new CellConfig( is );
	}

	@Override
	public FuzzyMode getFuzzyMode( ItemStack is )
	{
		String fz = Platform.openNbtData( is ).getString( "FuzzyMode" );
		try
		{
			return FuzzyMode.valueOf( fz );
		}
		catch( Throwable t )
		{
			return FuzzyMode.IGNORE_ALL;
		}
	}

	@Override
	public void setFuzzyMode( ItemStack is, FuzzyMode fzMode )
	{
		Platform.openNbtData( is ).setString( "FuzzyMode", fzMode.name() );
	}

	@Override
	public void onWheel( ItemStack is, boolean up )
	{
		this.cycleColors( is, this.getColor( is ), up ? 1 : -1 );
	}
}
