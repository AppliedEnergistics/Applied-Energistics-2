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
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.cells.ICellInventoryHandler;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import appeng.block.networking.BlockCableBus;
import appeng.block.paint.BlockPaint;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.core.localization.GuiText;
import appeng.helpers.IMouseWheelItem;
import appeng.hooks.IBlockTool;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.items.misc.ItemPaintBall;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.me.helpers.BaseActionSource;
import appeng.tile.misc.TilePaint;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SnowballItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.text.WordUtils;

import java.util.*;


public class ToolColorApplicator extends AEBasePoweredItem implements IStorageCell<IAEItemStack>, IItemGroup, IBlockTool, IMouseWheelItem
{

	private static final Map<Integer, AEColor> ORE_TO_COLOR = new HashMap<>();

	private static final String TAG_COLOR = "color";

	static
	{
		for( final AEColor color : AEColor.VALID_COLORS )
		{
			final String dyeName = color.dye.getTranslationKey();
			final String oreDictName = "dye" + WordUtils.capitalize( dyeName );
			// FIXME final int oreDictId = OreDictionary.getOreID( oreDictName );

			// FIXME ORE_TO_COLOR.put( oreDictId, color );
		}
	}

	public ToolColorApplicator(Item.Properties props)
	{
		super( AEConfig.instance().getColorApplicatorBattery(), props );
		addPropertyOverride(
				new ResourceLocation(AppEng.MOD_ID, "colored"),
				(itemStack, world, entity) -> {
					// If the stack has no color, don't use the colored model since the impact of calling getColor
					// for every quad is extremely high, if the stack tries to re-search its inventory for a new
					// paintball everytime
					AEColor col = getActiveColor( itemStack );
					return ( col != null ) ? 1 : 0;
				}
		);
	}

	@Override
	public ActionResultType onItemUse( PlayerEntity p, World w, BlockPos pos, Hand hand, Direction side, float hitX, float hitY, float hitZ )
	{
		return this.onItemUse( p.getHeldItem( hand ), p, w, pos, hand, side, hitX, hitY, hitZ );
	}

	@Override
	public ActionResultType onItemUse( ItemStack is, PlayerEntity p, World w, BlockPos pos, Hand hand, Direction side, float hitX, float hitY, float hitZ )
	{
		final Block blk = w.getBlockState( pos ).getBlock();

		ItemStack paintBall = this.getColor( is );

		final IMEInventory<IAEItemStack> inv = AEApi.instance()
				.registries()
				.cell()
				.getCellInventory( is, null,
						AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ) );
		if( inv != null )
		{
			final IAEItemStack option = inv.extractItems( AEItemStack.fromItemStack( paintBall ), Actionable.SIMULATE, new BaseActionSource() );

			if( option != null )
			{
				paintBall = option.createItemStack();
				paintBall.setCount( 1 );
			}
			else
			{
				paintBall = ItemStack.EMPTY;
			}

			if( !Platform.hasPermissions( new DimensionalCoord( w, pos ), p ) )
			{
				return ActionResultType.FAIL;
			}

			final double powerPerUse = 100;
			if( !paintBall.isEmpty() && paintBall.getItem() instanceof SnowballItem )
			{
				final TileEntity te = w.getTileEntity( pos );
				// clean cables.
				if( te instanceof IColorableTile )
				{
					if( this.getAECurrentPower( is ) > powerPerUse && ( (IColorableTile) te ).getColor() != AEColor.TRANSPARENT )
					{
						if( ( (IColorableTile) te ).recolourBlock( side, AEColor.TRANSPARENT, p ) )
						{
							inv.extractItems( AEItemStack.fromItemStack( paintBall ), Actionable.MODULATE, new BaseActionSource() );
							this.extractAEPower( is, powerPerUse, Actionable.MODULATE );
							return ActionResultType.SUCCESS;
						}
					}
				}

				// clean paint balls..
				final Block testBlk = w.getBlockState( pos.offset( side ) ).getBlock();
				final TileEntity painted = w.getTileEntity( pos.offset( side ) );
				if( this.getAECurrentPower( is ) > powerPerUse && testBlk instanceof BlockPaint && painted instanceof TilePaint )
				{
					inv.extractItems( AEItemStack.fromItemStack( paintBall ), Actionable.MODULATE, new BaseActionSource() );
					this.extractAEPower( is, powerPerUse, Actionable.MODULATE );
					( (TilePaint) painted ).cleanSide( side.getOpposite() );
					return ActionResultType.SUCCESS;
				}
			}
			else if( !paintBall.isEmpty() )
			{
				final AEColor color = this.getColorFromItem( paintBall );

				if( color != null && this.getAECurrentPower( is ) > powerPerUse )
				{
					if( color != AEColor.TRANSPARENT && this.recolourBlock( blk, side, w, pos, side, color, p ) )
					{
						inv.extractItems( AEItemStack.fromItemStack( paintBall ), Actionable.MODULATE, new BaseActionSource() );
						this.extractAEPower( is, powerPerUse, Actionable.MODULATE );
						return ActionResultType.SUCCESS;
					}
				}
			}
		}

		if( p.isCrouching() )
		{
			this.cycleColors( is, paintBall, 1 );
		}

		return ActionResultType.FAIL;
	}

	@Override
	public ITextComponent getDisplayName( final ItemStack is )
	{
		ITextComponent extra = GuiText.Empty.textComponent();

		final AEColor selected = this.getActiveColor( is );

		if( selected != null && Platform.isClient() )
		{
			extra = new TranslationTextComponent(selected.translationKey);
		}

		return super.getDisplayName( is ).appendText(" - ").appendSibling( extra );
	}

	public AEColor getActiveColor( final ItemStack tol )
	{
		return this.getColorFromItem( this.getColor( tol ) );
	}

	private AEColor getColorFromItem( final ItemStack paintBall )
	{
		if( paintBall.isEmpty() )
		{
			return null;
		}

		if( paintBall.getItem() instanceof SnowballItem )
		{
			return AEColor.TRANSPARENT;
		}

		if( paintBall.getItem() instanceof ItemPaintBall )
		{
			final ItemPaintBall ipb = (ItemPaintBall) paintBall.getItem();
			return ipb.getColor();
		}
		else
		{
			// FIXME final int[] id = OreDictionary.getOreIDs( paintBall );
// FIXME
			// FIXME for( final int oreID : id )
			// FIXME {
			// FIXME 	if( ORE_TO_COLOR.containsKey( oreID ) )
			// FIXME 	{
			// FIXME 		return ORE_TO_COLOR.get( oreID );
			// FIXME 	}
			// FIXME }
		}

		return null;
	}

	public ItemStack getColor( final ItemStack is )
	{
		final CompoundNBT c = is.getTag();
		if( c != null && c.contains(TAG_COLOR) )
		{
			final CompoundNBT color = c.getCompound( TAG_COLOR );
			final ItemStack oldColor = ItemStack.read(color);
			if( !oldColor.isEmpty() )
			{
				return oldColor;
			}
		}

		return this.findNextColor( is, ItemStack.EMPTY, 0 );
	}

	private ItemStack findNextColor( final ItemStack is, final ItemStack anchor, final int scrollOffset )
	{
		ItemStack newColor = ItemStack.EMPTY;

		final IMEInventory<IAEItemStack> inv = AEApi.instance()
				.registries()
				.cell()
				.getCellInventory( is, null,
						AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ) );
		if( inv != null )
		{
			final IItemList<IAEItemStack> itemList = inv
					.getAvailableItems( AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ).createList() );
			if( anchor.isEmpty() )
			{
				final IAEItemStack firstItem = itemList.getFirstItem();
				if( firstItem != null )
				{
					newColor = firstItem.asItemStackRepresentation();
				}
			}
			else
			{
				final LinkedList<IAEItemStack> list = new LinkedList<>();

				for( final IAEItemStack i : itemList )
				{
					list.add( i );
				}

				Collections.sort( list, ( a, b ) -> Integer.compare( a.getItemDamage(), b.getItemDamage() ) );

				if( list.size() <= 0 )
				{
					return ItemStack.EMPTY;
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

				return list.get( 0 ).asItemStackRepresentation();
			}
		}

		if( !newColor.isEmpty() )
		{
			this.setColor( is, newColor );
		}

		return newColor;
	}

	private void setColor( final ItemStack is, final ItemStack newColor )
	{
        final CompoundNBT data = is.getOrCreateTag();
		if( newColor.isEmpty() )
		{
			data.remove( TAG_COLOR );
		}
		else
		{
			final CompoundNBT color = new CompoundNBT();
			newColor.write(color);
			data.put( TAG_COLOR, color );
		}
	}

	private boolean recolourBlock( final Block blk, final Direction side, final World w, final BlockPos pos, final Direction orientation, final AEColor newColor, final PlayerEntity p )
	{
		final BlockState state = w.getBlockState( pos );

// FIXME		if( blk instanceof BlockColored )
// FIXME		{
// FIXME			final DyeColor color = state.get( BlockColored.COLOR );
// FIXME
// FIXME			if( newColor.dye == color )
// FIXME			{
// FIXME				return false;
// FIXME			}
// FIXME
// FIXME			return w.setBlockState( pos, state.with( BlockColored.COLOR, newColor.dye ) );
// FIXME		}

//		if( blk == Blocks.GLASS )
//		{
//			return w.setBlockState( pos, Blocks.STAINED_GLASS.getDefaultState().with( BlockStainedGlass.COLOR, newColor.dye ) );
//		}
//
//		if( blk == Blocks.STAINED_GLASS )
//		{
//			final DyeColor color = state.get( BlockStainedGlass.COLOR );
//
//			if( newColor.dye == color )
//			{
//				return false;
//			}
//
//			return w.setBlockState( pos, state.with( BlockStainedGlass.COLOR, newColor.dye ) );
//		}

//		if( blk == Blocks.GLASS_PANE )
//		{
//			return w.setBlockState( pos, Blocks.STAINED_GLASS_PANE.getDefaultState().with( BlockStainedGlassPane.COLOR, newColor.dye ) );
//		}
//
//		if( blk == Blocks.STAINED_GLASS_PANE )
//		{
//			final DyeColor color = state.get( BlockStainedGlassPane.COLOR );
//
//			if( newColor.dye == color )
//			{
//				return false;
//			}
//
//			return w.setBlockState( pos, state.with( BlockStainedGlassPane.COLOR, newColor.dye ) );
//		}
//
//		if( blk == Blocks.HARDENED_CLAY )
//		{
//			return w.setBlockState( pos, Blocks.STAINED_HARDENED_CLAY.getDefaultState().with( BlockColored.COLOR, newColor.dye ) );
//		}

		if( blk instanceof BlockCableBus )
		{
			return ( (BlockCableBus) blk ).recolorBlock( w, pos, side, newColor.dye, p );
		}

		return blk.recolorBlock( state, w, pos, side, newColor.dye );
	}

	public void cycleColors( final ItemStack is, final ItemStack paintBall, final int i )
	{
		if( paintBall.isEmpty() )
		{
			this.setColor( is, this.getColor( is ) );
		}
		else
		{
			this.setColor( is, this.findNextColor( is, paintBall, i ) );
		}
	}

	@Override
	@OnlyIn( Dist.CLIENT )
	public void addInformation(final ItemStack stack, final World world, final List<ITextComponent> lines, final ITooltipFlag advancedTooltips )
	{
		super.addInformation( stack, world, lines, advancedTooltips );

		final ICellInventoryHandler<IAEItemStack> cdi = AEApi.instance()
				.registries()
				.cell()
				.getCellInventory( stack, null,
						AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ) );

		AEApi.instance().client().addCellInformation( cdi, lines );
	}

	@Override
	public int getBytes( final ItemStack cellItem )
	{
		return 512;
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
			// FIXME final int[] id = OreDictionary.getOreIDs( requestedAddition.getDefinition() );

			// FIXME for( final int x : id )
			// FIXME {
			// FIXME 	if( ORE_TO_COLOR.containsKey( x ) )
			// FIXME 	{
			// FIXME 		return false;
			// FIXME 	}
			// FIXME }

			if( requestedAddition.getItem() instanceof SnowballItem )
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
	public IStorageChannel<IAEItemStack> getChannel()
	{
		return AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class );
	}

	@Override
	public String getUnlocalizedGroupName( final Set<ItemStack> others, final ItemStack is )
	{
		return GuiText.StorageCells.getTranslationKey();
	}

	@Override
	public boolean isEditable( final ItemStack is )
	{
		return true;
	}

	@Override
	public IItemHandler getUpgradesInventory( final ItemStack is )
	{
		return new CellUpgrades( is, 2 );
	}

	@Override
	public IItemHandler getConfigInventory( final ItemStack is )
	{
		return new CellConfig( is );
	}

	@Override
	public FuzzyMode getFuzzyMode( final ItemStack is )
	{
        final String fz = is.getOrCreateTag().getString( "FuzzyMode" );
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
        is.getOrCreateTag().putString("FuzzyMode", fzMode.name());
	}

	@Override
	public void onWheel( final ItemStack is, final boolean up )
	{
		this.cycleColors( is, this.getColor( is ), up ? 1 : -1 );
	}

}
