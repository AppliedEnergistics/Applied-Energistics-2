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

package appeng.parts;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.DirectionalPlaceContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import appeng.api.definitions.IBlockDefinition;
import appeng.api.definitions.IItems;
import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.api.parts.PartItemStack;
import appeng.api.parts.SelectedPart;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.core.Api;
import appeng.core.AppEng;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ClickPacket;
import appeng.core.sync.packets.PartPlacementPacket;
import appeng.facade.IFacadeItem;
import appeng.util.LookDirection;
import appeng.util.Platform;

public class PartPlacement {

    private static float eyeHeight = 0.0f;
    private final ThreadLocal<Object> placing = new ThreadLocal<>();
    private boolean wasCanceled = false;

    public static ActionResultType place(final ItemStack held, final BlockPos pos, Direction side,
            final PlayerEntity player, final Hand hand, final World world, PlaceType pass, final int depth) {
        if (depth > 3) {
            return ActionResultType.FAIL;
        }

        // FIXME: This was changed alot.
        final LookDirection dir = Platform.getPlayerRay(player);
        RayTraceContext rtc = new RayTraceContext(dir.getA(), dir.getB(), RayTraceContext.BlockMode.OUTLINE,
                RayTraceContext.FluidMode.NONE, player);
        final BlockRayTraceResult mop = world.rayTraceBlocks(rtc);
        BlockItemUseContext useContext = new BlockItemUseContext(new ItemUseContext(player, hand, mop));

        if (!held.isEmpty() && Platform.isWrench(player, held, pos) && player.isCrouching()) {
            if (!Platform.hasPermissions(new DimensionalCoord(world, pos), player)) {
                return ActionResultType.FAIL;
            }

            final TileEntity tile = world.getTileEntity(pos);
            IPartHost host = null;

            if (tile instanceof IPartHost) {
                host = (IPartHost) tile;
            }

            if (host != null) {
                if (!world.isRemote) {
                    if (mop.getType() == RayTraceResult.Type.BLOCK) {
                        final List<ItemStack> is = new ArrayList<>();
                        final SelectedPart sp = selectPart(player, host,
                                mop.getHitVec().add(-mop.getPos().getX(), -mop.getPos().getY(), -mop.getPos().getZ()));

                        if (sp.part != null) {
                            is.add(sp.part.getItemStack(PartItemStack.WRENCH));
                            sp.part.getDrops(is, true);
                            host.removePart(sp.side, false);
                        }

                        if (sp.facade != null) {
                            is.add(sp.facade.getItemStack());
                            host.getFacadeContainer().removeFacade(host, sp.side);
                            Platform.notifyBlocksOfNeighbors(world, pos);
                        }

                        if (host.isEmpty()) {
                            host.cleanup();
                        }

                        if (!is.isEmpty()) {
                            Platform.spawnDrops(world, pos, is);
                        }
                    }
                } else {
                    player.swingArm(hand);
                    NetworkHandler.instance()
                            .sendToServer(new PartPlacementPacket(pos, side, getEyeOffset(player), hand));
                }
                return ActionResultType.SUCCESS;
            }

            return ActionResultType.FAIL;
        }

        TileEntity tile = world.getTileEntity(pos);
        IPartHost host = null;

        if (tile instanceof IPartHost) {
            host = (IPartHost) tile;
        }

        if (!held.isEmpty()) {
            final IFacadePart fp = isFacade(held, AEPartLocation.fromFacing(side));
            if (fp != null) {
                if (host != null) {
                    if (!world.isRemote) {
                        if (host.getPart(AEPartLocation.INTERNAL) == null) {
                            return ActionResultType.FAIL;
                        }

                        if (host.canAddPart(held, AEPartLocation.fromFacing(side))) {
                            if (host.getFacadeContainer().addFacade(fp)) {
                                host.markForSave();
                                host.markForUpdate();
                                if (!player.isCreative()) {
                                    held.grow(-1);
                                    ;
                                    if (held.getCount() == 0) {
                                        player.inventory.mainInventory.set(player.inventory.currentItem,
                                                ItemStack.EMPTY);
                                        MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(player, held, hand));
                                    }
                                }
                                return ActionResultType.CONSUME;
                            }
                        }
                    } else {
                        player.swingArm(hand);
                        NetworkHandler.instance()
                                .sendToServer(new PartPlacementPacket(pos, side, getEyeOffset(player), hand));
                        return ActionResultType.SUCCESS;
                    }
                }
                return ActionResultType.FAIL;
            }
        }

        if (held.isEmpty()) {
            if (host != null && player.isCrouching() && world.isAirBlock(pos)) {
                if (mop.getType() == RayTraceResult.Type.BLOCK) {
                    Vector3d hitVec = mop.getHitVec().add(-mop.getPos().getX(), -mop.getPos().getY(),
                            -mop.getPos().getZ());
                    final SelectedPart sPart = selectPart(player, host, hitVec);
                    if (sPart != null && sPart.part != null) {
                        if (sPart.part.onShiftActivate(player, hand, hitVec)) {
                            if (world.isRemote) {
                                NetworkHandler.instance()
                                        .sendToServer(new PartPlacementPacket(pos, side, getEyeOffset(player), hand));
                            }
                            return ActionResultType.SUCCESS;
                        }
                    }
                }
            }
        }

        if (held.isEmpty() || !(held.getItem() instanceof IPartItem)) {
            return ActionResultType.PASS;
        }

        BlockPos te_pos = pos;

        final IBlockDefinition multiPart = Api.instance().definitions().blocks().multiPart();
        if (host == null && pass == PlaceType.PLACE_ITEM) {
            Direction offset = null;

            BlockState blockState = world.getBlockState(pos);
            // FIXME isReplacable on the block state allows for more control, but requires
            // an item use context
            if (!blockState.isAir(world, pos) && !blockState.isReplaceable(useContext)) {
                offset = side;
                if (Platform.isServer()) {
                    side = side.getOpposite();
                }
            }

            te_pos = offset == null ? pos : pos.offset(offset);

            tile = world.getTileEntity(te_pos);
            if (tile instanceof IPartHost) {
                host = (IPartHost) tile;
            }

            ItemStack multiPartStack = multiPart.stack(1);
            Block multiPartBlock = multiPart.block();
            BlockItem multiPartBlockItem = multiPart.blockItem();

            boolean hostIsNotPresent = host == null;
            BlockState multiPartBlockState = multiPartBlock.getDefaultState();
            boolean canMultiPartBePlaced = multiPartBlockState.isValidPosition(world, te_pos);

            // We cannot override the item stack of normal use context, so we use this hack
            BlockItemUseContext mpUseCtx = new BlockItemUseContext(
                    new DirectionalPlaceContext(world, te_pos, side, multiPartStack, side));

            // FIXME: This is super-fishy and all needs to be re-checked. what does this
            // even do???
            if (hostIsNotPresent && canMultiPartBePlaced
                    && multiPartBlockItem.tryPlace(mpUseCtx).isSuccessOrConsume()) {
                if (!world.isRemote) {
                    tile = world.getTileEntity(te_pos);

                    if (tile instanceof IPartHost) {
                        host = (IPartHost) tile;
                    }

                    pass = PlaceType.INTERACT_SECOND_PASS;
                } else {
                    player.swingArm(hand);
                    NetworkHandler.instance()
                            .sendToServer(new PartPlacementPacket(pos, side, getEyeOffset(player), hand));
                    return ActionResultType.SUCCESS;
                }
            } else if (host != null && !host.canAddPart(held, AEPartLocation.fromFacing(side))) {
                return ActionResultType.FAIL;
            }
        }

        if (host == null) {
            return ActionResultType.PASS;
        }

        if (!host.canAddPart(held, AEPartLocation.fromFacing(side))) {
            if (pass == PlaceType.INTERACT_FIRST_PASS || pass == PlaceType.PLACE_ITEM) {
                te_pos = pos.offset(side);

                final BlockState blkState = world.getBlockState(te_pos);

                // FIXME: this is always true (host was de-referenced above)
                if (blkState.isAir(world, te_pos) || blkState.isReplaceable(useContext) || host != null) {
                    return place(held, te_pos, side.getOpposite(), player, hand, world,
                            pass == PlaceType.INTERACT_FIRST_PASS ? PlaceType.INTERACT_SECOND_PASS
                                    : PlaceType.PLACE_ITEM,
                            depth + 1);
                }
            }
            return ActionResultType.PASS;
        }

        if (!world.isRemote) {
            if (mop.getType() != RayTraceResult.Type.MISS) {
                final SelectedPart sp = selectPart(player, host,
                        mop.getHitVec().add(-mop.getPos().getX(), -mop.getPos().getY(), -mop.getPos().getZ()));

                if (sp.part != null) {
                    if (!player.isCrouching() && sp.part.onActivate(player, hand, mop.getHitVec())) {
                        return ActionResultType.FAIL;
                    }
                }
            }

            final DimensionalCoord dc = host.getLocation();
            if (!Platform.hasPermissions(dc, player)) {
                return ActionResultType.FAIL;
            }

            final AEPartLocation mySide = host.addPart(held, AEPartLocation.fromFacing(side), player, hand);
            if (mySide != null) {
                multiPart.maybeBlock().ifPresent(multiPartBlock -> {
                    BlockState blockState = world.getBlockState(pos);
                    final SoundType ss = multiPartBlock.getSoundType(blockState, world, pos, player);

                    world.playSound(null, pos, ss.getPlaceSound(), SoundCategory.BLOCKS, (ss.getVolume() + 1.0F) / 2.0F,
                            ss.getPitch() * 0.8F);
                });

                if (!player.isCreative()) {
                    held.grow(-1);
                    if (held.getCount() == 0) {
                        player.setHeldItem(hand, ItemStack.EMPTY);
                        MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(player, held, hand));
                    }
                }
            }
        } else {
            player.swingArm(hand);
        }
        return ActionResultType.SUCCESS;
    }

    private static float getEyeOffset(final PlayerEntity p) {
        if (p.world.isRemote) {
            return Platform.getEyeOffset(p);
        }

        return getEyeHeight();
    }

    private static SelectedPart selectPart(final PlayerEntity player, final IPartHost host, final Vector3d pos) {
        AppEng.proxy.setPartInteractionPlayer(player);
        try {
            return host.selectPart(pos);
        } finally {
            AppEng.proxy.setPartInteractionPlayer(null);
        }
    }

    public static IFacadePart isFacade(final ItemStack held, final AEPartLocation side) {
        if (held.getItem() instanceof IFacadeItem) {
            return ((IFacadeItem) held.getItem()).createPartFromItemStack(held, side);
        }

        return null;
    }

    @SubscribeEvent
    public void playerInteract(final TickEvent.ClientTickEvent event) {
        this.wasCanceled = false;
    }

    @SubscribeEvent
    public void playerInteract(final PlayerInteractEvent event) {
        // Only handle the main hand event
        if (event.getHand() != Hand.MAIN_HAND) {
            return;
        }

        if (event instanceof PlayerInteractEvent.RightClickEmpty && event.getPlayer().world.isRemote) {
            // re-check to see if this event was already channeled, cause these two events
            // are really stupid...
            final RayTraceResult mop = Platform.rayTrace(event.getPlayer(), true, false);
            final Minecraft mc = Minecraft.getInstance();

            final float f = 1.0F;
            final double d0 = mc.playerController.getBlockReachDistance();
            final Vector3d vec3 = mc.getRenderViewEntity().getEyePosition(f);

            if (mop instanceof BlockRayTraceResult && mop.getHitVec().distanceTo(vec3) < d0) {
                BlockRayTraceResult brtr = (BlockRayTraceResult) mop;

                final World w = event.getEntity().world;
                final TileEntity te = w.getTileEntity(brtr.getPos());
                if (te instanceof IPartHost && this.wasCanceled) {
                    event.setCanceled(true);
                }
            } else {
                final ItemStack held = event.getPlayer().getHeldItem(event.getHand());
                final IItems items = Api.instance().definitions().items();

                boolean supportedItem = items.memoryCard().isSameAs(held);
                supportedItem |= items.colorApplicator().isSameAs(held);

                if (event.getPlayer().isCrouching() && !held.isEmpty() && supportedItem) {
                    NetworkHandler.instance().sendToServer(new ClickPacket(event.getHand()));
                }
            }
        } else if (event instanceof PlayerInteractEvent.RightClickBlock && !event.getPlayer().world.isRemote) {
            if (this.placing.get() != null) {
                return;
            }

            this.placing.set(event);

            final ItemStack held = event.getPlayer().getHeldItem(event.getHand());
            if (place(held, event.getPos(), event.getFace(), event.getPlayer(), event.getHand(),
                    event.getPlayer().world, PlaceType.INTERACT_FIRST_PASS, 0) == ActionResultType.SUCCESS) {
                event.setCanceled(true);
                this.wasCanceled = true;
            }

            this.placing.set(null);
        }
    }

    private static float getEyeHeight() {
        return eyeHeight;
    }

    public static void setEyeHeight(final float eyeHeight) {
        PartPlacement.eyeHeight = eyeHeight;
    }

    public enum PlaceType {
        PLACE_ITEM, INTERACT_FIRST_PASS, INTERACT_SECOND_PASS
    }
}
