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

import java.util.EnumSet;
import java.util.Random;


public class NullCableBusContainer implements ICableBusContainer {

    @Override
    public int isProvidingStrongPower(final EnumFacing opposite) {
        return 0;
    }

    @Override
    public int isProvidingWeakPower(final EnumFacing opposite) {
        return 0;
    }

    @Override
    public boolean canConnectRedstone(final EnumSet<EnumFacing> of) {
        return false;
    }

    @Override
    public void onEntityCollision(final Entity e) {

    }

    @Override
    public boolean activate(final EntityPlayer player, final EnumHand hand, final Vec3d vecFromPool) {
        return false;
    }

    @Override
    public void onNeighborChanged(IBlockAccess w, BlockPos pos, BlockPos neighbor) {

    }

    @Override
    public boolean isSolidOnSide(final EnumFacing side) {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public SelectedPart selectPart(final Vec3d v3) {
        return new SelectedPart();
    }

    @Override
    public boolean recolourBlock(final EnumFacing side, final AEColor colour, final EntityPlayer who) {
        return false;
    }

    @Override
    public boolean isLadder(final EntityLivingBase entity) {
        return false;
    }

    @Override
    public void randomDisplayTick(final World world, final BlockPos pos, final Random r) {

    }

    @Override
    public int getLightValue() {
        return 0;
    }

    @Override
    public CableBusRenderState getRenderState() {
        return new CableBusRenderState();
    }

    @Override
    public boolean clicked(EntityPlayer player, EnumHand hand, Vec3d hitVec) {
        return false;
    }

}
