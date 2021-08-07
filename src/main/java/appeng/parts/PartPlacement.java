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

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.api.parts.PartItemStack;
import appeng.api.parts.SelectedPart;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalBlockPos;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.BlockDefinition;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ClickPacket;
import appeng.core.sync.packets.PartPlacementPacket;
import appeng.facade.IFacadeItem;
import appeng.util.InteractionUtil;
import appeng.util.LookDirection;
import appeng.util.Platform;

public class PartPlacement {

    private static float eyeHeight = 0.0f;
    private final ThreadLocal<Object> placing = new ThreadLocal<>();
    private boolean wasCanceled = false;

    public static InteractionResult place(final ItemStack held, final BlockPos pos, Direction side,
            final Player player, final InteractionHand hand, final Level level, PlaceType pass, final int depth) {
        if (depth > 3) {
            return InteractionResult.FAIL;
        }

        // FIXME: This was changed alot.
        final LookDirection dir = InteractionUtil.getPlayerRay(player);
        ClipContext rtc = new ClipContext(dir.getA(), dir.getB(), ClipContext.Block.OUTLINE,
                Fluid.NONE, player);
        final BlockHitResult mop = level.clip(rtc);
        BlockPlaceContext useContext = new BlockPlaceContext(new UseOnContext(player, hand, mop));

        if (!held.isEmpty() && InteractionUtil.isWrench(player, held, pos)
                && InteractionUtil.isInAlternateUseMode(player)) {
            if (!Platform.hasPermissions(new DimensionalBlockPos(level, pos), player)) {
                return InteractionResult.FAIL;
            }

            final BlockEntity blockEntity = level.getBlockEntity(pos);
            IPartHost host = null;

            if (blockEntity instanceof IPartHost) {
                host = (IPartHost) blockEntity;
            }

            if (host != null) {
                if (!level.isClientSide) {
                    if (mop.getType() == Type.BLOCK) {
                        final List<ItemStack> is = new ArrayList<>();
                        final SelectedPart sp = selectPart(player, host,
                                mop.getLocation().add(-mop.getBlockPos().getX(), -mop.getBlockPos().getY(),
                                        -mop.getBlockPos().getZ()));

                        // SelectedPart contains either a facade or a part. Never both.
                        if (sp.part != null) {
                            is.add(sp.part.getItemStack(PartItemStack.WRENCH));
                            sp.part.getDrops(is, true);
                            host.removePart(sp.side, false);
                        }

                        // A facade cannot exist without a cable part, no host cleanup needed.
                        if (sp.facade != null) {
                            is.add(sp.facade.getItemStack());
                            host.getFacadeContainer().removeFacade(host, sp.side);
                            Platform.notifyBlocksOfNeighbors(level, pos);
                        }

                        if (!is.isEmpty()) {
                            Platform.spawnDrops(level, pos, is);
                        }
                    }
                } else {
                    player.swing(hand);
                    NetworkHandler.instance()
                            .sendToServer(new PartPlacementPacket(pos, side, getEyeOffset(player), hand));
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }

            return InteractionResult.FAIL;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        IPartHost host = null;

        if (blockEntity instanceof IPartHost) {
            host = (IPartHost) blockEntity;
        }

        if (!held.isEmpty()) {
            final IFacadePart fp = isFacade(held, AEPartLocation.fromFacing(side));
            if (fp != null) {
                if (host != null) {
                    if (!level.isClientSide) {
                        if (host.getPart(AEPartLocation.INTERNAL) == null) {
                            return InteractionResult.FAIL;
                        }

                        if (host.canAddPart(held, AEPartLocation.fromFacing(side))
                                && host.getFacadeContainer().addFacade(fp)) {
                            host.markForSave();
                            host.markForUpdate();
                            if (!player.isCreative()) {
                                held.grow(-1);

                                if (held.getCount() == 0) {
                                    player.getInventory().items.set(player.getInventory().selected,
                                            ItemStack.EMPTY);
                                    MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(player, held, hand));
                                }
                            }
                            return InteractionResult.CONSUME;
                        }
                    } else {
                        player.swing(hand);
                        NetworkHandler.instance()
                                .sendToServer(new PartPlacementPacket(pos, side, getEyeOffset(player), hand));
                        return InteractionResult.sidedSuccess(level.isClientSide());
                    }
                }
                return InteractionResult.FAIL;
            }
        }

        if (held.isEmpty() && host != null && InteractionUtil.isInAlternateUseMode(player) && level.isEmptyBlock(pos)) {
            if (mop.getType() == Type.BLOCK) {
                Vec3 hitVec = mop.getLocation().add(-mop.getBlockPos().getX(), -mop.getBlockPos().getY(),
                        -mop.getBlockPos().getZ());
                final SelectedPart sPart = selectPart(player, host, hitVec);
                if (sPart != null && sPart.part != null && sPart.part.onShiftActivate(player, hand, hitVec)) {
                    if (level.isClientSide()) {
                        NetworkHandler.instance()
                                .sendToServer(new PartPlacementPacket(pos, side, getEyeOffset(player), hand));
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide());
                }
            }
        }

        if (held.isEmpty() || !(held.getItem() instanceof IPartItem)) {
            return InteractionResult.PASS;
        }

        BlockPos te_pos = pos;

        final BlockDefinition multiPart = AEBlocks.MULTI_PART;
        if (host == null && pass == PlaceType.PLACE_ITEM) {
            Direction offset = null;

            BlockState blockState = level.getBlockState(pos);
            // FIXME isReplacable on the block state allows for more control, but requires
            // an item use context
            if (!blockState.isAir() && !blockState.canBeReplaced(useContext)) {
                offset = side;
                if (!level.isClientSide()) {
                    side = side.getOpposite();
                }
            }

            te_pos = offset == null ? pos : pos.relative(offset);

            blockEntity = level.getBlockEntity(te_pos);
            if (blockEntity instanceof IPartHost) {
                host = (IPartHost) blockEntity;
            }

            ItemStack multiPartStack = multiPart.stack();
            Block multiPartBlock = multiPart.block();
            BlockItem multiPartBlockItem = (BlockItem) multiPart.asItem();

            boolean hostIsNotPresent = host == null;
            BlockState multiPartBlockState = multiPartBlock.defaultBlockState();
            boolean canMultiPartBePlaced = multiPartBlockState.canSurvive(level, te_pos);

            // We cannot override the item stack of normal use context, so we use this hack
            BlockPlaceContext mpUseCtx = new BlockPlaceContext(
                    new DirectionalPlaceContext(level, te_pos, side, multiPartStack, side));

            // FIXME: This is super-fishy and all needs to be re-checked. what does this
            // even do???
            if (hostIsNotPresent && canMultiPartBePlaced
                    && multiPartBlockItem.place(mpUseCtx).consumesAction()) {
                if (!level.isClientSide) {
                    blockEntity = level.getBlockEntity(te_pos);

                    if (blockEntity instanceof IPartHost) {
                        host = (IPartHost) blockEntity;
                    }

                    pass = PlaceType.INTERACT_SECOND_PASS;
                } else {
                    player.swing(hand);
                    NetworkHandler.instance()
                            .sendToServer(new PartPlacementPacket(pos, side, getEyeOffset(player), hand));
                    return InteractionResult.sidedSuccess(level.isClientSide());
                }
            } else if (host != null && !host.canAddPart(held, AEPartLocation.fromFacing(side))) {
                return InteractionResult.FAIL;
            }
        }

        if (host == null) {
            return InteractionResult.PASS;
        }

        if (!host.canAddPart(held, AEPartLocation.fromFacing(side))) {
            if (pass == PlaceType.INTERACT_FIRST_PASS || pass == PlaceType.PLACE_ITEM) {
                te_pos = pos.relative(side);

                final BlockState blkState = level.getBlockState(te_pos);

                // FIXME: this is always true (host was de-referenced above)
                if (blkState.isAir() || blkState.canBeReplaced(useContext) || host != null) {
                    return place(held, te_pos, side.getOpposite(), player, hand, level,
                            pass == PlaceType.INTERACT_FIRST_PASS ? PlaceType.INTERACT_SECOND_PASS
                                    : PlaceType.PLACE_ITEM,
                            depth + 1);
                }
            }
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            if (mop.getType() != Type.MISS) {
                final SelectedPart sp = selectPart(player, host,
                        mop.getLocation().add(-mop.getBlockPos().getX(), -mop.getBlockPos().getY(),
                                -mop.getBlockPos().getZ()));

                if (sp.part != null && !InteractionUtil.isInAlternateUseMode(player)
                        && sp.part.onActivate(player, hand, mop.getLocation())) {
                    return InteractionResult.FAIL;
                }
            }

            final DimensionalBlockPos dc = host.getLocation();
            if (!Platform.hasPermissions(dc, player)) {
                return InteractionResult.FAIL;
            }

            final AEPartLocation mySide = host.addPart(held, AEPartLocation.fromFacing(side), player, hand);
            if (mySide != null) {
                BlockState blockState = level.getBlockState(pos);
                final SoundType ss = multiPart.block().getSoundType(blockState, level, pos, player);

                level.playSound(null, pos, ss.getPlaceSound(), SoundSource.BLOCKS, (ss.getVolume() + 1.0F) / 2.0F,
                        ss.getPitch() * 0.8F);

                if (!player.isCreative()) {
                    held.grow(-1);
                    if (held.getCount() == 0) {
                        player.setItemInHand(hand, ItemStack.EMPTY);
                        MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(player, held, hand));
                    }
                }
            }
        } else {
            player.swing(hand);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private static float getEyeOffset(final Player p) {
        if (p.level.isClientSide) {
            return InteractionUtil.getEyeOffset(p);
        }

        return getEyeHeight();
    }

    private static SelectedPart selectPart(final Player player, final IPartHost host, final Vec3 pos) {
        AppEng.instance().setPartInteractionPlayer(player);
        try {
            return host.selectPart(pos);
        } finally {
            AppEng.instance().setPartInteractionPlayer(null);
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
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        if (event instanceof PlayerInteractEvent.RightClickEmpty && event.getPlayer().level.isClientSide) {
            // re-check to see if this event was already channeled, cause these two events
            // are really stupid...
            final HitResult mop = InteractionUtil.rayTrace(event.getPlayer(), true, false);
            final Minecraft mc = Minecraft.getInstance();

            final float f = 1.0F;
            final double d0 = mc.gameMode.getPickRange();
            final Vec3 vec3 = mc.getCameraEntity().getEyePosition(f);

            if (mop instanceof BlockHitResult && mop.getLocation().distanceTo(vec3) < d0) {
                BlockHitResult brtr = (BlockHitResult) mop;

                final Level level = event.getEntity().level;
                final BlockEntity te = level.getBlockEntity(brtr.getBlockPos());
                if (te instanceof IPartHost && this.wasCanceled) {
                    event.setCanceled(true);
                }
            } else {
                final ItemStack held = event.getPlayer().getItemInHand(event.getHand());

                boolean supportedItem = AEItems.MEMORY_CARD.isSameAs(held);
                supportedItem |= AEItems.COLOR_APPLICATOR.isSameAs(held);

                if (InteractionUtil.isInAlternateUseMode(event.getPlayer()) && !held.isEmpty() && supportedItem) {
                    NetworkHandler.instance().sendToServer(new ClickPacket(event.getHand()));
                }
            }
        } else if (event instanceof PlayerInteractEvent.RightClickBlock && !event.getPlayer().level.isClientSide) {
            if (this.placing.get() != null) {
                return;
            }

            this.placing.set(event);

            final ItemStack held = event.getPlayer().getItemInHand(event.getHand());
            Level level = event.getWorld();
            if (place(held, event.getPos(), event.getFace(), event.getPlayer(), event.getHand(),
                    level, PlaceType.INTERACT_FIRST_PASS, 0) == InteractionResult.sidedSuccess(level.isClientSide())) {
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
