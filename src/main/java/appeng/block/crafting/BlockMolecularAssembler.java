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

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RenderBlockAssembler;
import appeng.core.features.AEFeature;
import appeng.core.sync.GuiBridge;
import appeng.tile.crafting.TileMolecularAssembler;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockMolecularAssembler extends AEBaseBlock
{

	public BlockMolecularAssembler() {
		super( BlockMolecularAssembler.class, Material.iron );
		this.setFeature( EnumSet.of( AEFeature.MolecularAssembler ) );
		this.setTileEntity( TileMolecularAssembler.class );
		this.isOpaque = false;
		this.lightOpacity = 1;
	}

	public static boolean booleanAlphaPass = false;

	@Override
	public boolean canRenderInPass(int pass)
	{
		booleanAlphaPass = pass == 1;
		return pass == 0 || pass == 1;
	}

	@Override
	public int getRenderBlockPass()
	{
		return 1;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Class<? extends BaseBlockRender> getRenderer()
	{
		return RenderBlockAssembler.class;
	}

	@Override
	public boolean onActivated(World w, int x, int y, int z, EntityPlayer p, int side, float hitX, float hitY, float hitZ)
	{
		TileMolecularAssembler tg = this.getTileEntity( w, x, y, z );
		if ( tg != null && !p.isSneaking() )
		{
			Platform.openGUI( p, tg, ForgeDirection.getOrientation( side ), GuiBridge.GUI_MAC );
			return true;
		}
		return false;
	}
}
