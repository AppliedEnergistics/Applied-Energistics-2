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

package appeng.parts;


import appeng.api.parts.SelectedPart;
import appeng.api.util.AEColor;
import appeng.client.render.cablebus.CableBusRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.EnumSet;
import java.util.Random;


public interface ICableBusContainer {

    int isProvidingStrongPower(EnumFacing opposite);

    int isProvidingWeakPower(EnumFacing opposite);

    boolean canConnectRedstone(EnumSet<EnumFacing> of);

    void onEntityCollision(Entity e);

    boolean activate(EntityPlayer player, EnumHand hand, Vec3d vecFromPool);

    boolean clicked(EntityPlayer player, EnumHand hand, Vec3d hitVec);

    void onNeighborChanged(IBlockAccess w, BlockPos pos, BlockPos neighbor);

    boolean isSolidOnSide(EnumFacing side);

    boolean isEmpty();

    SelectedPart selectPart(Vec3d v3);

    boolean recolourBlock(EnumFacing side, AEColor colour, EntityPlayer who);

    boolean isLadder(EntityLivingBase entity);

    @SideOnly(Side.CLIENT)
    void randomDisplayTick(World world, BlockPos pos, Random r);

    int getLightValue();

    CableBusRenderState getRenderState();

}
