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

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.MaterialColor;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

import appeng.api.util.IOrientable;
import appeng.api.util.IOrientableBlock;
import appeng.helpers.AEMaterials;
import appeng.util.Platform;

public abstract class AEBaseBlock extends Block {

    private boolean isInventory = false;

    protected AEBaseBlock(final Settings props) {
        super(props);
    }

    /**
     * Utility function to create block properties with some sensible defaults for AE blocks.
     */
    public static FabricBlockSettings defaultProps(Material material) {
        return defaultProps(material, material.getColor());
    }

    /**
     * Utility function to create block properties with some sensible defaults for AE blocks.
     */
    public static FabricBlockSettings defaultProps(Material material, MaterialColor color) {
        return FabricBlockSettings.of(material, color)
                // These values previousls were encoded in AEBaseBlock
                .strength(2.2f, 11.f).breakByTool(FabricToolTags.PICKAXES, 0)
                .sounds(getDefaultSoundByMaterial(material));
    }

    private static BlockSoundGroup getDefaultSoundByMaterial(Material mat) {
        if (mat == AEMaterials.GLASS || mat == Material.GLASS) {
            return BlockSoundGroup.GLASS;
        } else if (mat == Material.STONE) {
            return BlockSoundGroup.STONE;
        } else if (mat == Material.WOOD) {
            return BlockSoundGroup.WOOD;
        } else {
            return BlockSoundGroup.METAL;
        }
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return this.isInventory();
    }

    @Override
    public int getComparatorOutput(BlockState state, final World worldIn, final BlockPos pos) {
        return 0;
    }

    /**
     * Rotates around the given Axis (usually the current up axis).
     */
    public boolean rotateAroundFaceAxis(WorldAccess w, BlockPos pos, Direction face) {
        final IOrientable rotatable = this.getOrientable(w, pos);

        if (rotatable != null && rotatable.canBeRotated()) {
            if (this.hasCustomRotation()) {
                this.customRotateBlock(rotatable, face);
                return true;
            } else {
                Direction forward = rotatable.getForward();
                Direction up = rotatable.getUp();

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

        final Direction forward = ori.getForward();
        final Direction up = ori.getUp();

        if (forward == null || up == null) {
            return dir;
        }

        final int west_x = forward.getOffsetY() * up.getOffsetZ() - forward.getOffsetZ() * up.getOffsetY();
        final int west_y = forward.getOffsetZ() * up.getOffsetX() - forward.getOffsetX() * up.getOffsetZ();
        final int west_z = forward.getOffsetX() * up.getOffsetY() - forward.getOffsetY() * up.getOffsetX();

        Direction west = null;
        for (final Direction dx : Direction.values()) {
            if (dx.getOffsetX() == west_x && dx.getOffsetY() == west_y && dx.getOffsetZ() == west_z) {
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
            return Direction.DOWN;
        }

        if (dir == west) {
            return Direction.WEST;
        }
        if (dir == west.getOpposite()) {
            return Direction.EAST;
        }

        return null;
    }

    @Override
    public String toString() {
        Identifier id = Registry.BLOCK.getId(this);
        String regName = id == Registry.BLOCK.getDefaultId() ? "unregistered" : id.getPath();
        return this.getClass().getSimpleName() + "[" + regName + "]";
    }

    protected String getUnlocalizedName(final ItemStack is) {
        return this.getTranslationKey();
    }

    protected boolean hasCustomRotation() {
        return false;
    }

    protected void customRotateBlock(final IOrientable rotatable, final Direction axis) {

    }

    protected IOrientable getOrientable(final BlockView w, final BlockPos pos) {
        if (this instanceof IOrientableBlock) {
            IOrientableBlock orientable = (IOrientableBlock) this;
            return orientable.getOrientable(w, pos);
        }
        return null;
    }

    protected boolean isValidOrientation(final WorldAccess w, final BlockPos pos, final Direction forward,
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
