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

import java.util.EnumSet;
import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.parts.SelectedPart;
import appeng.api.util.AEColor;
import appeng.client.render.cablebus.CableBusRenderState;

public interface ICableBusContainer {

    int isProvidingStrongPower(Direction opposite);

    int isProvidingWeakPower(Direction opposite);

    boolean canConnectRedstone(EnumSet<Direction> of);

    void onEntityCollision(Entity e);

    boolean activate(PlayerEntity player, Hand hand, Vec3d vecFromPool);

    boolean clicked(PlayerEntity player, Hand hand, Vec3d hitVec);

    void onNeighborChanged(IBlockReader w, BlockPos pos, BlockPos neighbor);

    boolean isSolidOnSide(Direction side);

    boolean isEmpty();

    SelectedPart selectPart(Vec3d v3);

    boolean recolourBlock(Direction side, AEColor colour, PlayerEntity who);

    boolean isLadder(LivingEntity entity);

    @OnlyIn(Dist.CLIENT)
    void animateTick(World world, BlockPos pos, Random r);

    int getLightValue();

    CableBusRenderState getRenderState();

}
