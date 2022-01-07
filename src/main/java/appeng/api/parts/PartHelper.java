/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 AlgorithmX2
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package appeng.api.parts;

import java.util.Objects;

import javax.annotation.Nullable;

import alexiil.mc.lib.multipart.api.MultipartContainer;
import appeng.integration.modules.lmp.CableBusPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlockEntities;
import appeng.core.definitions.AEBlocks;
import appeng.parts.PartPlacement;

public final class PartHelper {
    private PartHelper() {
    }

    /**
     * When implementing a custom part in an addon, you can use this method in
     * {@link net.minecraft.world.item.Item#useOn} of your parts item (if you're not using AE2s internal PartItem class)
     * to implement part placement.
     *
     * @return The result of placement suitable for returning from
     *         {@link net.minecraft.world.item.Item#useOn(UseOnContext)}.
     */
    public static InteractionResult usePartItem(UseOnContext context) {
        return PartPlacement.place(context);
    }

    /**
     * Tries to retrieve a part placed from a given part item from the world, and returns it.
     *
     * @param side Null will retrieve the part at the center (the cable).
     */
    @Nullable
    public static <T extends IPart> T getPart(IPartItem<T> partItem, BlockGetter level, BlockPos pos,
            @Nullable Direction side) {
        var part = getPart(level, pos, side);
        if (part != null) {
            var partClass = partItem.getPartClass();
            if (partClass.isInstance(part)) {
                return partClass.cast(part);
            }
        }
        return null;
    }

    /**
     * Tries to retrieve a part from the world, and returns it.
     *
     * @param side Null will retrieve the part at the center (the cable).
     */
    @Nullable
    public static IPart getPart(BlockGetter level, BlockPos pos, @Nullable Direction side) {
        var host = getPartHost((Level) level, pos); // TODO: bad cast
        if (host != null) {
            return host.getPart(side);
        }
        return null;
    }

    /**
     * Place or replace a part at the given position and side. Use `null` as the side to place a cable in the center of
     * the bus. An existing cable bus at the location will be reused, otherwise the existing block will be replaced with
     * a cable bus if its material is replaceable.
     *
     * @param player The player is only used to set the ownership of the created grid node.
     */
    @Nullable
    public static <T extends IPart> T setPart(ServerLevel level, BlockPos pos, @Nullable Direction side,
            @Nullable Player player, IPartItem<T> partItem) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(pos, "pos");

        var host = getOrPlacePartHost(level, pos, true, null);
        if (host == null) {
            return null;
        }

        var part = host.replacePart(partItem, side, player, null);
        if (host.isEmpty()) {
            host.cleanup();
        }

        return part;
    }

    /**
     * Gets or places a part host at the given position. The caller needs to handle empty part hosts. They should be
     * cleaned up if they contain no parts, otherwise they may impact gameplay.
     * <p/>
     * Use {@link IPartHost#isEmpty()} and {@link IPartHost#cleanup()}.
     *
     * @param force  If true, an existing non-cable-bus block will be unconditionally replaced.
     * @param player The player trying to place the cable bus. Will be used to check if the player can actually place it
     *               if force is not true.
     */
    @Nullable
    public static IPartHost getOrPlacePartHost(Level level, BlockPos pos, boolean force, @Nullable Player player) {
        // Get or place part host
        var host = getPartHost(level, pos);
        if (host != null) {
            return host;
        } else {
            if (!force && !canPlacePartHost(player, level, pos)) {
                return null;
            }

            var state = AEBlocks.CABLE_BUS.block().getStateForPlacement(level, pos);
            level.setBlockAndUpdate(pos, state);
            return level.getBlockEntity(pos, AEBlockEntities.CABLE_BUS).orElse(null);
        }
    }

    /**
     * Tries placing a new part host at the given location as a player.
     *
     * @return null if placing a new bus fails (even if a bus already is at that location)
     */
    @Nullable
    public static IPartHost placePartHost(@Nullable Player player, Level level, BlockPos pos) {
        // Get or place part host
        if (!canPlacePartHost(player, level, pos)) {
            return null;
        }

        var state = AEBlocks.CABLE_BUS.block().getStateForPlacement(level, pos);
        level.setBlockAndUpdate(pos, state);
        return level.getBlockEntity(pos, AEBlockEntities.CABLE_BUS).orElse(null);
    }

    public static boolean canPlacePartHost(@Nullable Player player, Level level, BlockPos pos) {
        if (player != null && !level.mayInteract(player, pos)) {
            return false;
        }

        return level.isEmptyBlock(pos) || level.getBlockState(pos).getMaterial().isReplaceable();
    }

    /**
     * Gets a part host at the given position.
     */
    @Nullable
    public static IPartHost getPartHost(Level level, BlockPos pos) {
        var blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof IPartHost partHost) {
            return partHost;
        }

        // TODO no hard dep
        var maybeContainer = MultipartContainer.ATTRIBUTE.getFirstOrNull(level, pos);
        if (maybeContainer != null) {
            var cableBus = maybeContainer.getFirstPart(CableBusPart.class);
            if (cableBus != null) {
                return cableBus;
            }
        }

        return null;
    }

    /**
     * @return the render mode
     */
    public static CableRenderMode getCableRenderMode() {
        return AppEng.instance().getCableRenderMode();
    }

}
