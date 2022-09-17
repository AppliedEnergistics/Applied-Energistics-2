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

package appeng.items.tools;


import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.implementations.items.IAEWrench;
import appeng.api.networking.IGridHost;
import appeng.api.parts.IPartHost;
import appeng.api.parts.SelectedPart;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.INetworkToolAgent;
import appeng.container.AEBaseContainer;
import appeng.core.AppEng;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketClick;
import appeng.items.AEBaseItem;
import appeng.items.contents.NetworkToolViewer;
import appeng.util.Platform;
import cofh.api.item.IToolHammer;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional.Interface;


// TODO BC Integration
//@Interface( iface = "buildcraft.api.tools.IToolWrench", iname = IntegrationType.BuildCraftCore )
@Interface(iface = "cofh.api.item.IToolHammer", modid = "cofhcore")
public class ToolNetworkTool extends AEBaseItem implements IGuiItem, IAEWrench, IToolHammer /* , IToolWrench */ {

    public ToolNetworkTool() {
        this.setMaxStackSize(1);
        this.setHarvestLevel("wrench", 0);
    }

    @Override
    public IGuiItemObject getGuiObject(final ItemStack is, final World world, final BlockPos pos) {
        final TileEntity te = world.getTileEntity(pos);
        return new NetworkToolViewer(is, (IGridHost) (te instanceof IGridHost ? te : null));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(final World w, final EntityPlayer p, final EnumHand hand) {
        if (Platform.isClient()) {
            final RayTraceResult mop = AppEng.proxy.getRTR();

            if (mop == null || mop.typeOfHit == RayTraceResult.Type.MISS) {
                NetworkHandler.instance().sendToServer(new PacketClick(BlockPos.ORIGIN, null, 0, 0, 0, hand));
            }
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, p.getHeldItem(hand));
    }

    @Override
    public EnumActionResult onItemUseFirst(final EntityPlayer player, final World world, final BlockPos pos, final EnumFacing side, final float hitX, final float hitY, final float hitZ, final EnumHand hand) {
        final RayTraceResult mop = new RayTraceResult(new Vec3d(hitX, hitY, hitZ), side, pos);
        final TileEntity te = world.getTileEntity(pos);

        if (te instanceof IPartHost) {
            final SelectedPart part = ((IPartHost) te).selectPart(mop.hitVec);

            if (part.part != null || part.facade != null) {
                if (part.part instanceof INetworkToolAgent && !((INetworkToolAgent) part.part).showNetworkInfo(mop)) {
                    return EnumActionResult.FAIL;
                } else if (player.isSneaking()) {
                    return EnumActionResult.PASS;
                }
            }
        } else if (te instanceof INetworkToolAgent && !((INetworkToolAgent) te).showNetworkInfo(mop)) {
            return EnumActionResult.FAIL;
        }

        if (Platform.isClient()) {
            NetworkHandler.instance().sendToServer(new PacketClick(pos, side, hitX, hitY, hitZ, hand));
        }

        return EnumActionResult.SUCCESS;
    }

    @Override
    public boolean doesSneakBypassUse(final ItemStack itemstack, final IBlockAccess world, final BlockPos pos, final EntityPlayer player) {
        return true;
    }

    public boolean serverSideToolLogic(final ItemStack is, final EntityPlayer p, final EnumHand hand, final World w, final BlockPos pos, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        if (side != null) {
            if (!Platform.hasPermissions(new DimensionalCoord(w, pos), p)) {
                return false;
            }

            final Block b = w.getBlockState(pos).getBlock();
            if (!p.isSneaking()) {
                final TileEntity te = w.getTileEntity(pos);
                if (!(te instanceof IGridHost)) {
                    if (b.rotateBlock(w, pos, side)) {
                        b.neighborChanged(Platform.AIR_BLOCK.getDefaultState(), w, pos, Platform.AIR_BLOCK, null);
                        p.swingArm(hand);
                        return !w.isRemote;
                    }
                }
            }

            if (!p.isSneaking()) {
                if (p.openContainer instanceof AEBaseContainer) {
                    return true;
                }

                final TileEntity te = w.getTileEntity(pos);

                if (te instanceof IGridHost) {
                    Platform.openGUI(p, te, AEPartLocation.fromFacing(side), GuiBridge.GUI_NETWORK_STATUS);
                } else {
                    Platform.openGUI(p, null, AEPartLocation.INTERNAL, GuiBridge.GUI_NETWORK_TOOL);
                }

                return true;
            } else {
                b.onBlockActivated(w, pos, w.getBlockState(pos), p, hand, side, hitX, hitY, hitZ);
            }
        } else {
            Platform.openGUI(p, null, AEPartLocation.INTERNAL, GuiBridge.GUI_NETWORK_TOOL);
        }

        return false;
    }

    @Override
    public boolean canWrench(final ItemStack wrench, final EntityPlayer player, final BlockPos pos) {
        return true;
    }

    // IToolHammer - start
    @Override
    public boolean isUsable(ItemStack item, EntityLivingBase user, BlockPos pos) {
        return true;
    }

    @Override
    public boolean isUsable(ItemStack item, EntityLivingBase user, Entity entity) {
        return true;
    }

    @Override
    public void toolUsed(ItemStack item, EntityLivingBase user, BlockPos pos) {
    }

    @Override
    public void toolUsed(ItemStack item, EntityLivingBase user, Entity entity) {
    }
    // IToolHammer - end

    // TODO: BC WRENCH INTEGRATION

}
