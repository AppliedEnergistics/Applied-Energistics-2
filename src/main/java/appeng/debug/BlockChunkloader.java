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

package appeng.debug;


import java.util.EnumSet;
import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.LoadingCallback;
import net.minecraftforge.common.ForgeChunkManager.Ticket;

import appeng.block.AEBaseBlock;
import appeng.core.AppEng;
import appeng.core.features.AEFeature;


public class BlockChunkloader extends AEBaseBlock implements LoadingCallback
{

	public BlockChunkloader()
	{
		super( BlockChunkloader.class, Material.iron );
		this.setFeature( EnumSet.of( AEFeature.UnsupportedDeveloperTools, AEFeature.Creative ) );
		this.setTileEntity( TileChunkLoader.class );
		ForgeChunkManager.setForcedChunkLoadingCallback( AppEng.instance, this );
	}

	@Override
	public void ticketsLoaded( List<Ticket> tickets, World world )
	{

	}

	@Override
	public void registerBlockIcons( IIconRegister iconRegistry )
	{
		this.registerNoIcons();
	}
}
