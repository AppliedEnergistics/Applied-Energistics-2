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

package appeng.block.misc;


import appeng.api.AEApi;
import appeng.api.util.AEAxisAlignedBB;
import appeng.block.AEBaseTileBlock;
import appeng.client.render.effects.LightningFX;
import appeng.client.render.renderable.ItemRenderable;
import appeng.client.render.tesr.ModularTESR;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.helpers.ICustomCollision;
import appeng.tile.AEBaseTile;
import appeng.tile.misc.TileCharger;
import appeng.util.Platform;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;


public class BlockCharger extends AEBaseTileBlock implements ICustomCollision {

    public BlockCharger() {
        super(Material.IRON);

        this.setLightOpacity(2);
        this.setFullSize(this.setOpaque(false));
    }

    @Override
    public boolean onActivated(final World w, final BlockPos pos, final EntityPlayer player, final EnumHand hand, final @Nullable ItemStack heldItem, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        if (player.isSneaking()) {
            return false;
        }

        if (Platform.isServer()) {
            final TileCharger tc = this.getTileEntity(w, pos);
            if (tc != null) {
                tc.activate(player);
            }
        }

        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(final IBlockState state, final World w, final BlockPos pos, final Random r) {
        if (!AEConfig.instance().isEnableEffects()) {
            return;
        }

        if (r.nextFloat() < 0.98) {
            return;
        }

        final AEBaseTile tile = this.getTileEntity(w, pos);
        if (tile instanceof TileCharger) {
            final TileCharger tc = (TileCharger) tile;

            if (AEApi.instance().definitions().materials().certusQuartzCrystalCharged().isSameAs(tc.getInternalInventory().getStackInSlot(0))) {
                final double xOff = 0.0;
                final double yOff = 0.0;
                final double zOff = 0.0;

                for (int bolts = 0; bolts < 3; bolts++) {
                    if (AppEng.proxy.shouldAddParticles(r)) {
                        final LightningFX fx = new LightningFX(w, xOff + 0.5 + pos.getX(), yOff + 0.5 + pos.getY(), zOff + 0.5 + pos
                                .getZ(), 0.0D, 0.0D, 0.0D);
                        Minecraft.getMinecraft().effectRenderer.addEffect(fx);
                    }
                }
            }
        }
    }

    @Override
    public Iterable<AxisAlignedBB> getSelectedBoundingBoxesFromPool(final World w, final BlockPos pos, final Entity thePlayer, final boolean b) {
        final TileCharger tile = this.getTileEntity(w, pos);
        if (tile != null) {
            final double twoPixels = 2.0 / 16.0;
            final EnumFacing up = tile.getUp();
            final EnumFacing forward = tile.getForward();
            final AEAxisAlignedBB bb = new AEAxisAlignedBB(twoPixels, twoPixels, twoPixels, 1.0 - twoPixels, 1.0 - twoPixels, 1.0 - twoPixels);

            if (up.getFrontOffsetX() != 0) {
                bb.minX = 0;
                bb.maxX = 1;
            }
            if (up.getFrontOffsetY() != 0) {
                bb.minY = 0;
                bb.maxY = 1;
            }
            if (up.getFrontOffsetZ() != 0) {
                bb.minZ = 0;
                bb.maxZ = 1;
            }

            switch (forward) {
                case DOWN:
                    bb.maxY = 1;
                    break;
                case UP:
                    bb.minY = 0;
                    break;
                case NORTH:
                    bb.maxZ = 1;
                    break;
                case SOUTH:
                    bb.minZ = 0;
                    break;
                case EAST:
                    bb.minX = 0;
                    break;
                case WEST:
                    bb.maxX = 1;
                    break;
                default:
                    break;
            }

            return Collections.singletonList(bb.getBoundingBox());
        }
        return Collections.singletonList(new AxisAlignedBB(0.0, 0, 0.0, 1.0, 1.0, 1.0));
    }

    @Override
    public void addCollidingBlockToList(final World w, final BlockPos pos, final AxisAlignedBB bb, final List<AxisAlignedBB> out, final Entity e) {
        out.add(new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0));
    }

    @SideOnly(Side.CLIENT)
    public static TileEntitySpecialRenderer<TileCharger> createTesr() {
        return new ModularTESR<>(new ItemRenderable<>(BlockCharger::getRenderedItem));
    }

    @SideOnly(Side.CLIENT)
    private static Pair<ItemStack, Matrix4f> getRenderedItem(TileCharger tile) {
        Matrix4f transform = new Matrix4f();
        transform.translate(new Vector3f(0.5f, 0.4f, 0.5f));
        return new ImmutablePair<>(tile.getInternalInventory().getStackInSlot(0), transform);
    }

}
