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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;

import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.api.parts.SelectedPart;
import appeng.api.util.DimensionalBlockPos;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.BlockDefinition;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PartPlacementPacket;
import appeng.facade.IFacadeItem;
import appeng.util.InteractionUtil;
import appeng.util.LookDirection;
import appeng.util.Platform;

public class PartPlacement {

    private static float eyeHeight = 0.0f;

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

        BlockEntity blockEntity = level.getBlockEntity(pos);
        IPartHost host = null;

        if (blockEntity instanceof IPartHost) {
            host = (IPartHost) blockEntity;
        }

        if (!held.isEmpty()) {
            final IFacadePart fp = createFacade(held, side);
            if (fp != null) {
                if (host != null) {
                    if (!level.isClientSide) {
                        if (host.getPart(null) == null) {
                            return InteractionResult.FAIL;
                        }

                        if (host.canAddPart(held, side)
                                && host.getFacadeContainer().addFacade(fp)) {
                            host.markForSave();
                            host.markForUpdate();
                            if (!player.isCreative()) {
                                held.grow(-1);

                                if (held.getCount() == 0) {
                                    player.getInventory().items.set(player.getInventory().selected,
                                            ItemStack.EMPTY);
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

        if (held.isEmpty() || !(held.getItem() instanceof IPartItem<?>partItem)) {
            return InteractionResult.PASS;
        }

        BlockPos te_pos = pos;

        final BlockDefinition<?> multiPart = AEBlocks.CABLE_BUS;
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
            BlockItem multiPartBlockItem = multiPart.asItem();

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
            } else if (host != null && !host.canAddPart(held, side)) {
                return InteractionResult.FAIL;
            }
        }

        if (host == null) {
            return InteractionResult.PASS;
        }

        if (!host.canAddPart(held, side)) {
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
                    return InteractionResult.CONSUME;
                }
            }

            final DimensionalBlockPos dc = host.getLocation();
            if (!Platform.hasPermissions(dc, player)) {
                return InteractionResult.FAIL;
            }

            if (host.addPart(partItem, side, player) != null) {
                BlockState blockState = level.getBlockState(pos);
                var ss = multiPart.block().getSoundType(blockState);

                level.playSound(null, pos, ss.getPlaceSound(), SoundSource.BLOCKS, (ss.getVolume() + 1.0F) / 2.0F,
                        ss.getPitch() * 0.8F);

                if (!player.isCreative()) {
                    held.grow(-1);
                    if (held.getCount() == 0) {
                        player.setItemInHand(hand, ItemStack.EMPTY);
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
            return host.selectPartLocal(pos);
        } finally {
            AppEng.instance().setPartInteractionPlayer(null);
        }
    }

    public static IFacadePart createFacade(final ItemStack held, final Direction side) {
        if (held.getItem() instanceof IFacadeItem) {
            return ((IFacadeItem) held.getItem()).createPartFromItemStack(held, side);
        }

        return null;
    }

    public static InteractionResult onPlayerUseBlock(Player player, Level level, InteractionHand hand,
            BlockHitResult hitResult) {
        if (level.isClientSide() || player.isSpectator()) {
            return InteractionResult.PASS;
        }

        // Only handle the main hand event
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        final ItemStack held = player.getItemInHand(hand);
        if (place(held, hitResult.getBlockPos(), hitResult.getDirection(), player, hand,
                level, PlaceType.INTERACT_FIRST_PASS, 0) == InteractionResult.sidedSuccess(level.isClientSide())) {
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
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
