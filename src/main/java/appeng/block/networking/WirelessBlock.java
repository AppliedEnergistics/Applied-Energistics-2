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

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import appeng.block.AEBaseTileBlock;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.container.implementations.WirelessContainer;
import appeng.helpers.AEMaterials;
import appeng.tile.networking.WirelessBlockEntity;
import appeng.util.Platform;

public class WirelessBlock extends AEBaseTileBlock<WirelessBlockEntity> {

    enum State implements StringIdentifiable {
        OFF, ON, HAS_CHANNEL;

        @Override
        public String asString() {
            return this.name().toLowerCase();
        }
    }

    public static final EnumProperty<State> STATE = EnumProperty.of("state", State.class);

    public WirelessBlock() {
        super(defaultProps(AEMaterials.GLASS).nonOpaque());
        this.setDefaultState(this.getDefaultState().with(STATE, State.OFF));
    }

    @Override
    protected BlockState updateBlockStateFromTileEntity(BlockState currentState, WirelessBlockEntity te) {
        State teState = State.OFF;

        if (te.isActive()) {
            teState = State.HAS_CHANNEL;
        } else if (te.isPowered()) {
            teState = State.ON;
        }

        return currentState.with(STATE, teState);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(STATE);
    }

    @Override
    public ActionResult onUse(BlockState state, World w, BlockPos pos, PlayerEntity player, Hand hand,
            BlockHitResult hit) {
        final WirelessBlockEntity tg = this.getBlockEntity(w, pos);

        if (tg != null && !player.isInSneakingPose()) {
            if (Platform.isServer()) {
                ContainerOpener.openContainer(WirelessContainer.TYPE, player,
                        ContainerLocator.forTileEntitySide(tg, hit.getSide()));
            }
            return ActionResult.SUCCESS;
        }

        return super.onUse(state, w, pos, player, hand, hit);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView w, BlockPos pos, ShapeContext context) {
        final WirelessBlockEntity tile = this.getBlockEntity(w, pos);
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

            return VoxelShapes.cuboid(new Box(minX, minY, minZ, maxX, maxY, maxZ));
        }
        return VoxelShapes.empty();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView w, BlockPos pos, ShapeContext context) {

        final WirelessBlockEntity tile = this.getBlockEntity(w, pos);
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

            return VoxelShapes.cuboid(new Box(minX, minY, minZ, maxX, maxY, maxZ));
        } else {
            return VoxelShapes.empty();
        }
    }

    @Override
    public boolean isTranslucent(BlockState state, BlockView reader, BlockPos pos) {
        return true;
    }

}
