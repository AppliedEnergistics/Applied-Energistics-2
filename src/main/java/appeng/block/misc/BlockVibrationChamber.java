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

package appeng.block.misc;


import appeng.block.AEBaseTileBlock;
import appeng.client.texture.ExtraBlockTextures;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import appeng.core.sync.GuiBridge;
import appeng.tile.AEBaseTile;
import appeng.tile.misc.TileVibrationChamber;
import appeng.util.Platform;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.EnumSet;
import java.util.Random;


public final class BlockVibrationChamber extends AEBaseTileBlock
{

	public BlockVibrationChamber()
	{
		super( Material.iron );
		this.setTileEntity( TileVibrationChamber.class );
		this.setHardness( 4.2F );
		this.setFeature( EnumSet.of( AEFeature.PowerGen ) );
	}

	@Override
	public IIcon getIcon( final IBlockAccess w, final int x, final int y, final int z, final int s )
	{
		final IIcon ico = super.getIcon( w, x, y, z, s );
		final TileVibrationChamber tvc = this.getTileEntity( w, x, y, z );

		if( tvc != null && tvc.isOn && ico == this.getRendererInstance().getTexture( ForgeDirection.SOUTH ) )
		{
			return ExtraBlockTextures.BlockVibrationChamberFrontOn.getIcon();
		}

		return ico;
	}

	@Override
	public boolean onActivated( final World w, final int x, final int y, final int z, final EntityPlayer player, final int side, final float hitX, final float hitY, final float hitZ )
	{
		if( player.isSneaking() )
		{
			return false;
		}

		if( Platform.isServer() )
		{
			final TileVibrationChamber tc = this.getTileEntity( w, x, y, z );
			if( tc != null && !player.isSneaking() )
			{
				Platform.openGUI( player, tc, ForgeDirection.getOrientation( side ), GuiBridge.GUI_VIBRATION_CHAMBER );
				return true;
			}
		}

		return true;
	}

	@Override
	public void randomDisplayTick( final World w, final int x, final int y, final int z, final Random r )
	{
		if( !AEConfig.instance.enableEffects )
		{
			return;
		}

		final AEBaseTile tile = this.getTileEntity( w, x, y, z );
		if( tile instanceof TileVibrationChamber )
		{
			final TileVibrationChamber tc = (TileVibrationChamber) tile;
			if( tc.isOn )
			{
				float f1 = x + 0.5F;
				float f2 = y + 0.5F;
				float f3 = z + 0.5F;

				final ForgeDirection forward = tc.getForward();
				final ForgeDirection up = tc.getUp();

				final int west_x = forward.offsetY * up.offsetZ - forward.offsetZ * up.offsetY;
				final int west_y = forward.offsetZ * up.offsetX - forward.offsetX * up.offsetZ;
				final int west_z = forward.offsetX * up.offsetY - forward.offsetY * up.offsetX;

				f1 += forward.offsetX * 0.6;
				f2 += forward.offsetY * 0.6;
				f3 += forward.offsetZ * 0.6;

				final float ox = r.nextFloat();
				final float oy = r.nextFloat() * 0.2f;

				f1 += up.offsetX * ( -0.3 + oy );
				f2 += up.offsetY * ( -0.3 + oy );
				f3 += up.offsetZ * ( -0.3 + oy );

				f1 += west_x * ( 0.3 * ox - 0.15 );
				f2 += west_y * ( 0.3 * ox - 0.15 );
				f3 += west_z * ( 0.3 * ox - 0.15 );

				w.spawnParticle( "smoke", f1, f2, f3, 0.0D, 0.0D, 0.0D );
				w.spawnParticle( "flame", f1, f2, f3, 0.0D, 0.0D, 0.0D );
			}
		}
	}
}
