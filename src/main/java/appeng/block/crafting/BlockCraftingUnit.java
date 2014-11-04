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

import java.util.EnumSet;
import java.util.List;

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
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RenderBlockCraftingCPU;
import appeng.client.texture.ExtraBlockTextures;
import appeng.core.features.AEFeature;
import appeng.core.sync.GuiBridge;
import appeng.tile.crafting.TileCraftingTile;
import appeng.util.Platform;

public class BlockCraftingUnit extends AEBaseBlock
{
	public static final int FLAG_FORMED = 8;

	public BlockCraftingUnit(Class<? extends BlockCraftingUnit> childClass)
	{
		super( childClass, Material.iron );

		hasSubtypes = true;
		setFeature( EnumSet.of( AEFeature.CraftingCPU ) );
	}

	public BlockCraftingUnit()
	{
		this( BlockCraftingUnit.class );

		setTileEntity( TileCraftingTile.class );
	}

	@Override
	public boolean onActivated(World w, int x, int y, int z, EntityPlayer p, int side, float hitX, float hitY, float hitZ)
	{
		TileCraftingTile tg = getTileEntity( w, x, y, z );
		if ( tg != null && !p.isSneaking() && tg.isFormed() && tg.isActive() )
		{
			if ( Platform.isClient() )
				return true;

			Platform.openGUI( p, tg, ForgeDirection.getOrientation( side ), GuiBridge.GUI_CRAFTING_CPU );
			return true;
		}

		return false;
	}

	@Override
	public int getDamageValue(World w, int x, int y, int z)
	{
		int meta = w.getBlockMetadata( x, y, z );
		return damageDropped( meta );
	}

	@Override
	public int damageDropped(int meta)
	{
		return meta & 3;
	}

	@Override
	public String getUnlocalizedName(ItemStack is)
	{
		if ( is.getItemDamage() == 1 )
			return "tile.appliedenergistics2.BlockCraftingAccelerator";

		return getItemUnlocalizedName( is );
	}

	protected String getItemUnlocalizedName(ItemStack is)
	{
		return super.getUnlocalizedName( is );
	}

	@Override
	public void setRenderStateByMeta(int itemDamage)
	{
		IIcon front = getIcon( ForgeDirection.SOUTH.ordinal(), itemDamage );
		IIcon other = getIcon( ForgeDirection.NORTH.ordinal(), itemDamage );
		getRendererInstance().setTemporaryRenderIcons( other, other, front, other, other, other );
	}

	@Override
	public IIcon getIcon(int direction, int metadata)
	{
		switch (metadata)
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
	protected Class<? extends BaseBlockRender> getRenderer()
	{
		return RenderBlockCraftingCPU.class;
	}

	@Override
	public void breakBlock(World w, int x, int y, int z, Block a, int b)
	{
		TileCraftingTile cp = getTileEntity( w, x, y, z );
		if ( cp != null )
			cp.breakCluster();

		super.breakBlock( w, x, y, z, a, b );
	}

	@Override
	public void onNeighborBlockChange(World w, int x, int y, int z, Block junk)
	{
		TileCraftingTile cp = getTileEntity( w, x, y, z );
		if ( cp != null )
			cp.updateMultiBlock();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getCheckedSubBlocks(Item item, CreativeTabs tabs, List<ItemStack> itemStacks)
	{
		itemStacks.add( new ItemStack( this, 1, 0 ) );
		itemStacks.add( new ItemStack( this, 1, 1 ) );
	}
}
