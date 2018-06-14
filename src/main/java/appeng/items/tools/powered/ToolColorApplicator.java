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
import com.google.common.base.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemSnowball;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;

import java.util.*;

import org.apache.commons.lang3.text.WordUtils;

public class ToolColorApplicator extends AEBasePoweredItem implements IStorageCell, IItemGroup, IBlockTool, IMouseWheelItem
{

	private static final Map<Integer, AEColor> ORE_TO_COLOR = new HashMap<Integer, AEColor>();

	static
	{

		for( final AEColor color : AEColor.VALID_COLORS )
		{
			final String dyeName = color.unlocalizedName;
			final String oreDictName = "dye" + WordUtils.capitalize( dyeName );
			final int oreDictId = OreDictionary.getOreID( oreDictName );

			ORE_TO_COLOR.put( oreDictId, color );
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
	public boolean onItemUse( final ItemStack is, final EntityPlayer p, final World w, final int x, final int y, final int z, final int side, final float hitX, final float hitY, final float hitZ )
	{
		final Block blk = w.getBlock( x, y, z );

		ItemStack paintBall = this.getColor( is );

		final IMEInventory<IAEItemStack> inv = AEApi.instance().registries().cell().getCellInventory( is, null, StorageChannel.ITEMS );
		if( inv != null )
		{
			final IAEItemStack option = inv.extractItems( AEItemStack.create( paintBall ), Actionable.SIMULATE, new BaseActionSource() );

			if( option != null )
			{
				paintBall = option.getItemStack();
				paintBall.stackSize = 1;
			}
			else
			{
				paintBall = null;
			}

			if( !Platform.hasPermissions( new DimensionalCoord( w, x, y, z ), p ) )
			{
				return false;
			}

			final double powerPerUse = 100;
			if( paintBall != null && paintBall.getItem() instanceof ItemSnowball )
			{
				final ForgeDirection orientation = ForgeDirection.getOrientation( side );
				final TileEntity te = w.getTileEntity( x, y, z );
				// clean cables.
				if( te instanceof IColorableTile )
				{
					if( this.getAECurrentPower( is ) > powerPerUse && ( (IColorableTile) te ).getColor() != AEColor.Transparent )
					{
						if( ( (IColorableTile) te ).recolourBlock( orientation, AEColor.Transparent, p ) )
						{
							inv.extractItems( AEItemStack.create( paintBall ), Actionable.MODULATE, new BaseActionSource() );
							this.extractAEPower( is, powerPerUse );
							return true;
						}
					}
				}

				// clean paint balls..
				final Block testBlk = w.getBlock( x + orientation.offsetX, y + orientation.offsetY, z + orientation.offsetZ );
				final TileEntity painted = w.getTileEntity( x + orientation.offsetX, y + orientation.offsetY, z + orientation.offsetZ );
				if( this.getAECurrentPower( is ) > powerPerUse && testBlk instanceof BlockPaint && painted instanceof TilePaint )
				{
					inv.extractItems( AEItemStack.create( paintBall ), Actionable.MODULATE, new BaseActionSource() );
					this.extractAEPower( is, powerPerUse );
					( (TilePaint) painted ).cleanSide( orientation.getOpposite() );
					return true;
				}
			}
			else if( paintBall != null )
			{
				final AEColor color = this.getColorFromItem( paintBall );

				if( color != null && this.getAECurrentPower( is ) > powerPerUse )
				{
					if( color != AEColor.Transparent && this.recolourBlock( blk, ForgeDirection.getOrientation( side ), w, x, y, z, ForgeDirection.getOrientation( side ), color, p ) )
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
	public String getItemStackDisplayName( final ItemStack par1ItemStack )
	{
		String extra = GuiText.Empty.getLocal();

		final AEColor selected = this.getActiveColor( par1ItemStack );

		if( selected != null && Platform.isClient() )
		{
			extra = Platform.gui_localize( selected.unlocalizedName );
		}

		return super.getItemStackDisplayName( par1ItemStack ) + " - " + extra;
	}

	public AEColor getActiveColor( final ItemStack tol )
	{
		return this.getColorFromItem( this.getColor( tol ) );
	}

	private AEColor getColorFromItem( final ItemStack paintBall )
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
			final ItemPaintBall ipb = (ItemPaintBall) paintBall.getItem();
			return ipb.getColor( paintBall );
		}
		else
		{
			final int[] id = OreDictionary.getOreIDs( paintBall );

			for( final int oreID : id )
			{
				if( ORE_TO_COLOR.containsKey( oreID ) )
				{
					return ORE_TO_COLOR.get( oreID );
				}
			}
		}

		return null;
	}

	public ItemStack getColor( final ItemStack is )
	{
		final NBTTagCompound c = is.getTagCompound();
		if( c != null && c.hasKey( "color" ) )
		{
			final NBTTagCompound color = c.getCompoundTag( "color" );
			final ItemStack oldColor = ItemStack.loadItemStackFromNBT( color );
			if( oldColor != null )
			{
				return oldColor;
			}
		}

		return this.findNextColor( is, null, 0 );
	}

	private ItemStack findNextColor( final ItemStack is, final ItemStack anchor, final int scrollOffset )
	{
		ItemStack newColor = null;

		final IMEInventory<IAEItemStack> inv = AEApi.instance().registries().cell().getCellInventory( is, null, StorageChannel.ITEMS );
		if( inv != null )
		{
			final IItemList<IAEItemStack> itemList = inv.getAvailableItems( AEApi.instance().storage().createItemList() );
			if( anchor == null )
			{
				final IAEItemStack firstItem = itemList.getFirstItem();
				if( firstItem != null )
				{
					newColor = firstItem.getItemStack();
				}
			}
			else
			{
				final LinkedList<IAEItemStack> list = new LinkedList<IAEItemStack>();

				for( final IAEItemStack i : itemList )
				{
					list.add( i );
				}

				Collections.sort( list, new Comparator<IAEItemStack>()
				{

					@Override
					public int compare( final IAEItemStack a, final IAEItemStack b )
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

	private void setColor( final ItemStack is, final ItemStack newColor )
	{
		final NBTTagCompound data = Platform.openNbtData( is );
		if( newColor == null )
		{
			data.removeTag( "color" );
		}
		else
		{
			final NBTTagCompound color = new NBTTagCompound();
			newColor.writeToNBT( color );
			data.setTag( "color", color );
		}
	}

	private boolean recolourBlock( final Block blk, final ForgeDirection side, final World w, final int x, final int y, final int z, final ForgeDirection orientation, final AEColor newColor, final EntityPlayer p )
	{
		if( blk == Blocks.carpet )
		{
			final int meta = w.getBlockMetadata( x, y, z );
			if( newColor.ordinal() == meta )
			{
				return false;
			}
			return w.setBlock( x, y, z, Blocks.carpet, newColor.ordinal(), 3 );
		}

		if( blk == Blocks.glass )
		{
			return w.setBlock( x, y, z, Blocks.stained_glass, newColor.ordinal(), 3 );
		}

		if( blk == Blocks.stained_glass )
		{
			final int meta = w.getBlockMetadata( x, y, z );
			if( newColor.ordinal() == meta )
			{
				return false;
			}
			return w.setBlock( x, y, z, Blocks.stained_glass, newColor.ordinal(), 3 );
		}

		if( blk == Blocks.glass_pane )
		{
			return w.setBlock( x, y, z, Blocks.stained_glass_pane, newColor.ordinal(), 3 );
		}

		if( blk == Blocks.stained_glass_pane )
		{
			final int meta = w.getBlockMetadata( x, y, z );
			if( newColor.ordinal() == meta )
			{
				return false;
			}
			return w.setBlock( x, y, z, Blocks.stained_glass_pane, newColor.ordinal(), 3 );
		}

		if( blk == Blocks.hardened_clay )
		{
			return w.setBlock( x, y, z, Blocks.stained_hardened_clay, newColor.ordinal(), 3 );
		}

		if( blk == Blocks.stained_hardened_clay )
		{
			final int meta = w.getBlockMetadata( x, y, z );
			if( newColor.ordinal() == meta )
			{
				return false;
			}
			return w.setBlock( x, y, z, Blocks.stained_hardened_clay, newColor.ordinal(), 3 );
		}

		if( blk instanceof BlockCableBus )
		{
			return ( (BlockCableBus) blk ).recolourBlock( w, x, y, z, side, newColor.ordinal(), p );
		}

		return blk.recolourBlock( w, x, y, z, side, newColor.ordinal() );
	}

	public void cycleColors( final ItemStack is, final ItemStack paintBall, final int i )
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
	public void addCheckedInformation( final ItemStack stack, final EntityPlayer player, final List<String> lines, final boolean displayMoreInfo )
	{
		super.addCheckedInformation( stack, player, lines, displayMoreInfo );

		final IMEInventory<IAEItemStack> cdi = AEApi.instance().registries().cell().getCellInventory( stack, null, StorageChannel.ITEMS );

		if( cdi instanceof CellInventoryHandler )
		{
			final ICellInventory cd = ( (ICellInventoryHandler) cdi ).getCellInv();
			if( cd != null )
			{
				lines.add( cd.getUsedBytes() + " " + GuiText.Of.getLocal() + ' ' + cd.getTotalBytes() + ' ' + GuiText.BytesUsed.getLocal() );
				lines.add( cd.getStoredItemTypes() + " " + GuiText.Of.getLocal() + ' ' + cd.getTotalItemTypes() + ' ' + GuiText.Types.getLocal() );
			}
		}
	}

	@Override
	public int getBytes( final ItemStack cellItem )
	{
		return 512;
	}

	@Override
	public int BytePerType( final ItemStack cell )
	{
		return 8;
	}

	@Override
	public int getBytesPerType( final ItemStack cellItem )
	{
		return 8;
	}

	@Override
	public int getTotalTypes( final ItemStack cellItem )
	{
		return 27;
	}

	@Override
	public boolean isBlackListed( final ItemStack cellItem, final IAEItemStack requestedAddition )
	{
		if( requestedAddition != null )
		{
			final int[] id = OreDictionary.getOreIDs( requestedAddition.getItemStack() );

			for( final int x : id )
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
	public boolean isStorageCell( final ItemStack i )
	{
		return true;
	}

	@Override
	public double getIdleDrain()
	{
		return 0.5;
	}

	@Override
	public String getUnlocalizedGroupName( final Set<ItemStack> others, final ItemStack is )
	{
		return GuiText.StorageCells.getUnlocalized();
	}

	@Override
	public boolean isEditable( final ItemStack is )
	{
		return true;
	}

	@Override
	public IInventory getUpgradesInventory( final ItemStack is )
	{
		return new CellUpgrades( is, 2 );
	}

	@Override
	public IInventory getConfigInventory( final ItemStack is )
	{
		return new CellConfig( is );
	}

	@Override
	public FuzzyMode getFuzzyMode( final ItemStack is )
	{
		final String fz = Platform.openNbtData( is ).getString( "FuzzyMode" );
		try
		{
			return FuzzyMode.valueOf( fz );
		}
		catch( final Throwable t )
		{
			return FuzzyMode.IGNORE_ALL;
		}
	}

	@Override
	public void setFuzzyMode( final ItemStack is, final FuzzyMode fzMode )
	{
		Platform.openNbtData( is ).setString( "FuzzyMode", fzMode.name() );
	}

	@Override
	public void onWheel( final ItemStack is, final boolean up )
	{
		this.cycleColors( is, this.getColor( is ), up ? 1 : -1 );
	}
}
