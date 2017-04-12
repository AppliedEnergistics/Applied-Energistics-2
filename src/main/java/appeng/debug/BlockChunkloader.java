/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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


import appeng.block.AEBaseTileBlock;
import appeng.core.AppEng;
import appeng.core.features.AEFeature;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.LoadingCallback;
import net.minecraftforge.common.ForgeChunkManager.Ticket;

import java.util.EnumSet;
import java.util.List;


public class BlockChunkloader extends AEBaseTileBlock implements LoadingCallback
{

	public BlockChunkloader()
	{
		super( Material.iron );
		this.setTileEntity( TileChunkLoader.class );
		ForgeChunkManager.setForcedChunkLoadingCallback( AppEng.instance(), this );
		this.setFeature( EnumSet.of( AEFeature.UnsupportedDeveloperTools, AEFeature.Creative ) );
	}

	@Override
	public void ticketsLoaded( final List<Ticket> tickets, final World world )
	{

	}

	@Override
	public void registerBlockIcons( final IIconRegister iconRegistry )
	{
		this.registerNoIcons();
	}
}
