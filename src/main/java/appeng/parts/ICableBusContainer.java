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

import java.util.Random;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import appeng.api.parts.SelectedPart;
import appeng.api.util.AEColor;
import appeng.client.render.cablebus.CableBusRenderState;

public interface ICableBusContainer {

    int isProvidingStrongPower(Direction opposite);

    int isProvidingWeakPower(Direction opposite);

    boolean canConnectRedstone(Direction opposite);

    void onEntityCollision(Entity e);

    boolean activate(Player player, InteractionHand hand, Vec3 vecFromPool);

    void onNeighborChanged(BlockGetter level, BlockPos pos, BlockPos neighbor);

    boolean isEmpty();

    SelectedPart selectPartLocal(Vec3 v3);

    boolean recolourBlock(Direction side, AEColor colour, Player who);

    boolean isLadder(LivingEntity entity);

    @Environment(EnvType.CLIENT)
    void animateTick(Level level, BlockPos pos, Random r);

    int getLightValue();

    CableBusRenderState getRenderState();

}
