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

package appeng.block.storage;


import appeng.api.util.AEPartLocation;
import appeng.block.AEBaseTileBlock;
import appeng.core.sync.GuiBridge;
import appeng.helpers.ICustomCollision;
import appeng.tile.storage.TileSkyChest;
import appeng.util.Platform;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;


public class BlockSkyChest extends AEBaseTileBlock implements ICustomCollision {

    private static final double AABB_OFFSET_BOTTOM = 0.00;
    private static final double AABB_OFFSET_SIDES = 0.06;
    private static final double AABB_OFFSET_TOP = 0.125;

    public enum SkyChestType {
        STONE, BLOCK
    }

    public final SkyChestType type;

    public BlockSkyChest(final SkyChestType type) {
        super(Material.ROCK);
        this.setOpaque(this.setFullSize(false));
        this.lightOpacity = 0;
        this.setHardness(50);
        this.blockResistance = 150.0f;
        this.type = type;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public boolean onActivated(final World w, final BlockPos pos, final EntityPlayer player, final EnumHand hand, final @Nullable ItemStack heldItem, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        if (Platform.isServer()) {
            Platform.openGUI(player, this.getTileEntity(w, pos), AEPartLocation.fromFacing(side), GuiBridge.GUI_SKYCHEST);
        }

        return true;
    }

    @Override
    public Iterable<AxisAlignedBB> getSelectedBoundingBoxesFromPool(final World w, final BlockPos pos, final Entity thePlayer, final boolean b) {
        final AxisAlignedBB aabb = this.computeAABB(w, pos);

        return Collections.singletonList(aabb);
    }

    @Override
    public void addCollidingBlockToList(final World w, final BlockPos pos, final AxisAlignedBB bb, final List<AxisAlignedBB> out, final Entity e) {
        final AxisAlignedBB aabb = this.computeAABB(w, pos);

        out.add(aabb);
    }

    private AxisAlignedBB computeAABB(final World w, final BlockPos pos) {
        final TileSkyChest sk = this.getTileEntity(w, pos);
        EnumFacing o = EnumFacing.UP;

        if (sk != null) {
            o = sk.getUp();
        }

        final double offsetX = o.getFrontOffsetX() == 0 ? AABB_OFFSET_SIDES : 0.0;
        final double offsetY = o.getFrontOffsetY() == 0 ? AABB_OFFSET_SIDES : 0.0;
        final double offsetZ = o.getFrontOffsetZ() == 0 ? AABB_OFFSET_SIDES : 0.0;

        // for x/z top and bottom is swapped
        final double minX = Math.max(0.0, offsetX + (o.getFrontOffsetX() < 0 ? AABB_OFFSET_BOTTOM : (o.getFrontOffsetX() * AABB_OFFSET_TOP)));
        final double minY = Math.max(0.0, offsetY + (o.getFrontOffsetY() < 0 ? AABB_OFFSET_TOP : (o.getFrontOffsetY() * AABB_OFFSET_BOTTOM)));
        final double minZ = Math.max(0.0, offsetZ + (o.getFrontOffsetZ() < 0 ? AABB_OFFSET_BOTTOM : (o.getFrontOffsetZ() * AABB_OFFSET_TOP)));

        final double maxX = Math.min(1.0, 1.0 - offsetX - (o.getFrontOffsetX() < 0 ? AABB_OFFSET_TOP : (o.getFrontOffsetX() * AABB_OFFSET_BOTTOM)));
        final double maxY = Math.min(1.0, 1.0 - offsetY - (o.getFrontOffsetY() < 0 ? AABB_OFFSET_BOTTOM : (o.getFrontOffsetY() * AABB_OFFSET_TOP)));
        final double maxZ = Math.min(1.0, 1.0 - offsetZ - (o.getFrontOffsetZ() < 0 ? AABB_OFFSET_TOP : (o.getFrontOffsetZ() * AABB_OFFSET_BOTTOM)));

        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
