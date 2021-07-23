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

package appeng.block;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.common.ToolType;

import appeng.api.util.IOrientable;
import appeng.api.util.IOrientableBlock;
import appeng.helpers.AEMaterials;
import appeng.util.Platform;

public abstract class AEBaseBlock extends Block {

    private boolean isInventory = false;

    protected AEBaseBlock(final net.minecraft.world.level.block.state.BlockBehaviour.Properties props) {
        super(props);
    }

    /**
     * Utility function to create block properties with some sensible defaults for AE blocks.
     */
    public static net.minecraft.world.level.block.state.BlockBehaviour.Properties defaultProps(net.minecraft.world.level.material.Material material) {
        return defaultProps(material, material.getColor());
    }

    /**
     * Utility function to create block properties with some sensible defaults for AE blocks.
     */
    public static net.minecraft.world.level.block.state.BlockBehaviour.Properties defaultProps(net.minecraft.world.level.material.Material material, MaterialColor color) {
        return net.minecraft.world.level.block.state.BlockBehaviour.Properties.of(material, color)
                // These values previousls were encoded in AEBaseBlock
                .strength(2.2f, 11.f).harvestTool(ToolType.PICKAXE).harvestLevel(0)
                .sound(getDefaultSoundByMaterial(material));
    }

    private static net.minecraft.world.level.block.SoundType getDefaultSoundByMaterial(net.minecraft.world.level.material.Material mat) {
        if (mat == AEMaterials.GLASS || mat == Material.GLASS) {
            return SoundType.GLASS;
        } else if (mat == net.minecraft.world.level.material.Material.STONE) {
            return net.minecraft.world.level.block.SoundType.STONE;
        } else if (mat == net.minecraft.world.level.material.Material.WOOD) {
            return net.minecraft.world.level.block.SoundType.WOOD;
        } else {
            return net.minecraft.world.level.block.SoundType.METAL;
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return this.isInventory();
    }

    @Override
    public int getAnalogOutputSignal(net.minecraft.world.level.block.state.BlockState state, final Level worldIn, final BlockPos pos) {
        return 0;
    }

    /**
     * Rotates around the given Axis (usually the current up axis).
     */
    public boolean rotateAroundFaceAxis(LevelAccessor w, net.minecraft.core.BlockPos pos, Direction face) {
        final IOrientable rotatable = this.getOrientable(w, pos);

        if (rotatable != null && rotatable.canBeRotated()) {
            if (this.hasCustomRotation()) {
                this.customRotateBlock(rotatable, face);
                return true;
            } else {
                Direction forward = rotatable.getForward();
                net.minecraft.core.Direction up = rotatable.getUp();

                for (int rs = 0; rs < 4; rs++) {
                    forward = Platform.rotateAround(forward, face);
                    up = Platform.rotateAround(up, face);

                    if (this.isValidOrientation(w, pos, forward, up)) {
                        rotatable.setOrientation(forward, up);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public final Direction mapRotation(final IOrientable ori, final Direction dir) {
        // case DOWN: return bottomIcon;
        // case UP: return blockIcon;
        // case NORTH: return northIcon;
        // case SOUTH: return southIcon;
        // case WEST: return sideIcon;
        // case EAST: return sideIcon;

        final net.minecraft.core.Direction forward = ori.getForward();
        final Direction up = ori.getUp();

        if (forward == null || up == null) {
            return dir;
        }

        final int west_x = forward.getStepY() * up.getStepZ() - forward.getStepZ() * up.getStepY();
        final int west_y = forward.getStepZ() * up.getStepX() - forward.getStepX() * up.getStepZ();
        final int west_z = forward.getStepX() * up.getStepY() - forward.getStepY() * up.getStepX();

        Direction west = null;
        for (final net.minecraft.core.Direction dx : Direction.values()) {
            if (dx.getStepX() == west_x && dx.getStepY() == west_y && dx.getStepZ() == west_z) {
                west = dx;
            }
        }

        if (west == null) {
            return dir;
        }

        if (dir == forward) {
            return Direction.SOUTH;
        }
        if (dir == forward.getOpposite()) {
            return Direction.NORTH;
        }

        if (dir == up) {
            return Direction.UP;
        }
        if (dir == up.getOpposite()) {
            return net.minecraft.core.Direction.DOWN;
        }

        if (dir == west) {
            return Direction.WEST;
        }
        if (dir == west.getOpposite()) {
            return net.minecraft.core.Direction.EAST;
        }

        return null;
    }

    @Override
    public String toString() {
        String regName = this.getRegistryName() != null ? this.getRegistryName().getPath() : "unregistered";
        return this.getClass().getSimpleName() + "[" + regName + "]";
    }

    protected String getUnlocalizedName(final ItemStack is) {
        return this.getDescriptionId();
    }

    protected boolean hasCustomRotation() {
        return false;
    }

    protected void customRotateBlock(final IOrientable rotatable, final net.minecraft.core.Direction axis) {

    }

    protected IOrientable getOrientable(final BlockGetter w, final BlockPos pos) {
        if (this instanceof IOrientableBlock) {
            IOrientableBlock orientable = (IOrientableBlock) this;
            return orientable.getOrientable(w, pos);
        }
        return null;
    }

    protected boolean isValidOrientation(final LevelAccessor w, final net.minecraft.core.BlockPos pos, final Direction forward,
                                         final Direction up) {
        return true;
    }

    protected boolean isInventory() {
        return this.isInventory;
    }

    protected void setInventory(final boolean isInventory) {
        this.isInventory = isInventory;
    }

}
