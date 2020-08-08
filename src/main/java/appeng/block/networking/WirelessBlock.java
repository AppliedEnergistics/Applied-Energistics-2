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

package appeng.block.networking;

import java.util.Locale;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import appeng.block.AEBaseTileBlock;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.container.implementations.WirelessContainer;
import appeng.helpers.AEMaterials;
import appeng.tile.networking.WirelessTileEntity;
import appeng.util.Platform;

public class WirelessBlock extends AEBaseTileBlock<WirelessTileEntity> {

    enum State implements IStringSerializable {
        OFF, ON, HAS_CHANNEL;

        @Override
        public String getString() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    public static final EnumProperty<State> STATE = EnumProperty.create("state", State.class);

    public WirelessBlock() {
        super(defaultProps(AEMaterials.GLASS).notSolid());
        this.setDefaultState(this.getDefaultState().with(STATE, State.OFF));
    }

    @Override
    protected BlockState updateBlockStateFromTileEntity(BlockState currentState, WirelessTileEntity te) {
        State teState = State.OFF;

        if (te.isActive()) {
            teState = State.HAS_CHANNEL;
        } else if (te.isPowered()) {
            teState = State.ON;
        }

        return currentState.with(STATE, teState);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(STATE);
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World w, BlockPos pos, PlayerEntity player, Hand hand,
            BlockRayTraceResult hit) {
        final WirelessTileEntity tg = this.getTileEntity(w, pos);

        if (tg != null && !player.isCrouching()) {
            if (Platform.isServer()) {
                ContainerOpener.openContainer(WirelessContainer.TYPE, player,
                        ContainerLocator.forTileEntitySide(tg, hit.getFace()));
            }
            return ActionResultType.SUCCESS;
        }

        return super.onBlockActivated(state, w, pos, player, hand, hit);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader w, BlockPos pos, ISelectionContext context) {
        final WirelessTileEntity tile = this.getTileEntity(w, pos);
        if (tile != null) {
            final Direction forward = tile.getForward();

            double minX = 0;
            double minY = 0;
            double minZ = 0;
            double maxX = 1;
            double maxY = 1;
            double maxZ = 1;

            switch (forward) {
                case DOWN:
                    minZ = minX = 3.0 / 16.0;
                    maxZ = maxX = 13.0 / 16.0;
                    maxY = 1.0;
                    minY = 5.0 / 16.0;
                    break;
                case EAST:
                    minZ = minY = 3.0 / 16.0;
                    maxZ = maxY = 13.0 / 16.0;
                    maxX = 11.0 / 16.0;
                    minX = 0.0;
                    break;
                case NORTH:
                    minY = minX = 3.0 / 16.0;
                    maxY = maxX = 13.0 / 16.0;
                    maxZ = 1.0;
                    minZ = 5.0 / 16.0;
                    break;
                case SOUTH:
                    minY = minX = 3.0 / 16.0;
                    maxY = maxX = 13.0 / 16.0;
                    maxZ = 11.0 / 16.0;
                    minZ = 0.0;
                    break;
                case UP:
                    minZ = minX = 3.0 / 16.0;
                    maxZ = maxX = 13.0 / 16.0;
                    maxY = 11.0 / 16.0;
                    minY = 0.0;
                    break;
                case WEST:
                    minZ = minY = 3.0 / 16.0;
                    maxZ = maxY = 13.0 / 16.0;
                    maxX = 1.0;
                    minX = 5.0 / 16.0;
                    break;
                default:
                    break;
            }

            return VoxelShapes.create(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ));
        }
        return VoxelShapes.empty();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader w, BlockPos pos, ISelectionContext context) {

        final WirelessTileEntity tile = this.getTileEntity(w, pos);
        if (tile != null) {
            final Direction forward = tile.getForward();

            double minX = 0;
            double minY = 0;
            double minZ = 0;
            double maxX = 1;
            double maxY = 1;
            double maxZ = 1;

            switch (forward) {
                case DOWN:
                    minZ = minX = 3.0 / 16.0;
                    maxZ = maxX = 13.0 / 16.0;
                    maxY = 1.0;
                    minY = 5.0 / 16.0;
                    break;
                case EAST:
                    minZ = minY = 3.0 / 16.0;
                    maxZ = maxY = 13.0 / 16.0;
                    maxX = 11.0 / 16.0;
                    minX = 0.0;
                    break;
                case NORTH:
                    minY = minX = 3.0 / 16.0;
                    maxY = maxX = 13.0 / 16.0;
                    maxZ = 1.0;
                    minZ = 5.0 / 16.0;
                    break;
                case SOUTH:
                    minY = minX = 3.0 / 16.0;
                    maxY = maxX = 13.0 / 16.0;
                    maxZ = 11.0 / 16.0;
                    minZ = 0.0;
                    break;
                case UP:
                    minZ = minX = 3.0 / 16.0;
                    maxZ = maxX = 13.0 / 16.0;
                    maxY = 11.0 / 16.0;
                    minY = 0.0;
                    break;
                case WEST:
                    minZ = minY = 3.0 / 16.0;
                    maxZ = maxY = 13.0 / 16.0;
                    maxX = 1.0;
                    minX = 5.0 / 16.0;
                    break;
                default:
                    break;
            }

            return VoxelShapes.create(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ));
        } else {
            return VoxelShapes.empty();
        }
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
        return true;
    }

}
