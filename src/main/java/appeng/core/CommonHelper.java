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

package appeng.core;


import java.util.List;
import java.util.Random;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import cpw.mods.fml.common.SidedProxy;

import appeng.api.parts.CableRenderMode;
import appeng.block.AEBaseBlock;
import appeng.client.EffectType;
import appeng.core.sync.AppEngPacket;


public abstract class CommonHelper
{

	@SidedProxy( clientSide = "appeng.client.ClientHelper", serverSide = "appeng.server.ServerHelper" )
	public static CommonHelper proxy;

	public abstract void init();

	public abstract World getWorld();

	public abstract void bindTileEntitySpecialRenderer( Class tile, AEBaseBlock blk );

	public abstract List<EntityPlayer> getPlayers();

	public abstract void sendToAllNearExcept( EntityPlayer p, double x, double y, double z, double dist, World w, AppEngPacket packet );

	public abstract void spawnEffect( EffectType effect, World worldObj, double posX, double posY, double posZ, Object extra );

	public abstract boolean shouldAddParticles( Random r );

	public abstract MovingObjectPosition getMOP();

	public abstract void doRenderItem( ItemStack itemstack, World w );

	public abstract void postInit();

	public abstract CableRenderMode getRenderMode();

	public abstract void triggerUpdates();

	public abstract void updateRenderMode( EntityPlayer player );

	public abstract void missingCoreMod();
}
