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

import javax.annotation.Nonnull;

import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import appeng.api.parts.CableRenderMode;
import appeng.block.AEBaseBlock;
import appeng.client.ActionKey;
import appeng.client.EffectType;
import appeng.core.sync.BasePacket;

public abstract class CommonHelper {

    public abstract void bindTileEntitySpecialRenderer(Class<? extends TileEntity> tile, AEBaseBlock blk);

    public abstract List<? extends PlayerEntity> getPlayers();

    public abstract void sendToAllNearExcept(PlayerEntity p, double x, double y, double z, double dist, World w,
            BasePacket packet);

    public abstract void spawnEffect(EffectType effect, World world, double posX, double posY, double posZ,
            Object extra);

    public abstract boolean shouldAddParticles(Random r);

    public abstract RayTraceResult getRTR();

    public abstract void postInit();

    public abstract CableRenderMode getRenderMode();

    public abstract void updateRenderMode(PlayerEntity player);

    public abstract boolean isActionKey(@Nonnull final ActionKey key, InputMappings.Input input);

}
