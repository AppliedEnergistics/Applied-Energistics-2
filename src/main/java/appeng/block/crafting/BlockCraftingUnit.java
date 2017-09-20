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

package appeng.block.crafting;


import appeng.block.AEBaseTileBlock;
import appeng.client.render.blocks.RenderBlockCraftingCPU;
import appeng.client.texture.ExtraBlockTextures;
import appeng.core.features.AEFeature;
import appeng.core.sync.GuiBridge;
import appeng.tile.crafting.TileCraftingTile;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.EnumSet;
import java.util.List;


public class BlockCraftingUnit extends AEBaseTileBlock
{
	static final int FLAG_FORMED = 8;

	public BlockCraftingUnit()
	{
		super( Material.iron );

		this.hasSubtypes = true;
		this.setTileEntity( TileCraftingTile.class );
		this.setFeature( EnumSet.of( AEFeature.CraftingCPU ) );
	}

	@Override
	@SideOnly( Side.CLIENT )
	protected RenderBlockCraftingCPU<? extends BlockCraftingUnit, ? extends TileCraftingTile> getRenderer()
	{
		return new RenderBlockCraftingCPU<BlockCraftingUnit, TileCraftingTile>();
	}

	@Override
	public IIcon getIcon( final int direction, final int metadata )
	{
		switch( metadata )
		{
			default:
			case 0:
				return super.getIcon( 0, 0 );
			case 1:
				return ExtraBlockTextures.BlockCraftingAccelerator.getIcon();
			case FLAG_FORMED:
				return ExtraBlockTextures.BlockCraftingUnitFit.getIcon();
			case 1 | FLAG_FORMED:
				return ExtraBlockTextures.BlockCraftingAcceleratorFit.getIcon();
		}
	}

	@Override
	public boolean onBlockActivated( final World w, final int x, final int y, final int z, final EntityPlayer p, final int side, final float hitX, final float hitY, final float hitZ )
	{
		final TileCraftingTile tg = this.getTileEntity( w, x, y, z );
		if( tg != null && !p.isSneaking() && tg.isFormed() && tg.isActive() )
		{
			if( Platform.isClient() )
			{
				return true;
			}

			Platform.openGUI( p, tg, ForgeDirection.getOrientation( side ), GuiBridge.GUI_CRAFTING_CPU );
			return true;
		}

		return super.onBlockActivated( w, x, y, z, p, side, hitX, hitY, hitZ );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void getCheckedSubBlocks( final Item item, final CreativeTabs tabs, final List<ItemStack> itemStacks )
	{
		itemStacks.add( new ItemStack( this, 1, 0 ) );
		itemStacks.add( new ItemStack( this, 1, 1 ) );
	}

	@Override
	public void setRenderStateByMeta( final int itemDamage )
	{
		final IIcon front = this.getIcon( ForgeDirection.SOUTH.ordinal(), itemDamage );
		final IIcon other = this.getIcon( ForgeDirection.NORTH.ordinal(), itemDamage );
		this.getRendererInstance().setTemporaryRenderIcons( other, other, front, other, other, other );
	}

	@Override
	public void breakBlock( final World w, final int x, final int y, final int z, final Block a, final int b )
	{
		final TileCraftingTile cp = this.getTileEntity( w, x, y, z );
		if( cp != null )
		{
			cp.breakCluster();
		}

		super.breakBlock( w, x, y, z, a, b );
	}

	@Override
	public String getUnlocalizedName( final ItemStack is )
	{
		if( is.getItemDamage() == 1 )
		{
			return "tile.appliedenergistics2.BlockCraftingAccelerator";
		}

		return this.getItemUnlocalizedName( is );
	}

	protected String getItemUnlocalizedName( final ItemStack is )
	{
		return super.getUnlocalizedName( is );
	}

	@Override
	public void onNeighborBlockChange( final World w, final int x, final int y, final int z, final Block junk )
	{
		final TileCraftingTile cp = this.getTileEntity( w, x, y, z );
		if( cp != null )
		{
			cp.updateMultiBlock();
		}
	}

	@Override
	public int damageDropped( final int meta )
	{
		return meta & 3;
	}

	@Override
	public int getDamageValue( final World w, final int x, final int y, final int z )
	{
		final int meta = w.getBlockMetadata( x, y, z );
		return this.damageDropped( meta );
	}
}
