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

package appeng.util;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Iterables;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.Fluid;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnits;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.SortOrder;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.api.util.DimensionalBlockPos;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.hooks.ticking.TickHandler;
import appeng.me.GridNode;
import appeng.util.helpers.P2PHelper;
import appeng.util.prioritylist.IPartitionList;

public class Platform {

    private static final FabricLoader FABRIC = FabricLoader.getInstance();

    /*
     * random source, use it for item drop locations...
     */
    private static final Random RANDOM_GENERATOR = new Random();

    private static final P2PHelper P2P_HELPER = new P2PHelper();

    public static final Direction[] DIRECTIONS_WITH_NULL = new Direction[] { Direction.DOWN, Direction.UP,
            Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, null };

    public static P2PHelper p2p() {
        return P2P_HELPER;
    }

    public static Random getRandom() {
        return RANDOM_GENERATOR;
    }

    public static float getRandomFloat() {
        return RANDOM_GENERATOR.nextFloat();
    }

    /**
     * This displays the value for encoded longs ( double *100 )
     *
     * @param n      to be formatted long value
     * @param isRate if true it adds a /t to the formatted string
     * @return formatted long value
     */
    public static String formatPowerLong(long n, boolean isRate) {
        return formatPower((double) n / 100, isRate);
    }

    public static String formatPower(double p, boolean isRate) {
        var displayUnits = AEConfig.instance().getSelectedPowerUnit();
        p = PowerUnits.AE.convertTo(displayUnits, p);

        final String[] preFixes = { "k", "M", "G", "T", "P", "T", "P", "E", "Z", "Y" };
        var unitName = displayUnits.getSymbolName();

        String level = "";
        int offset = 0;
        while (p > 1000 && offset < preFixes.length) {
            p /= 1000;
            level = preFixes[offset];
            offset++;
        }

        final DecimalFormat df = new DecimalFormat("#.##");
        return df.format(p) + ' ' + level + unitName + (isRate ? "/t" : "");
    }

    public static String formatTimeMeasurement(long nanos) {
        if (nanos <= 0) {
            return "0 ns";
        } else if (nanos < 1000) {
            return "<1 µs";
        } else if (nanos <= 1000 * 1000) {
            final long ms = TimeUnit.MICROSECONDS.convert(nanos, TimeUnit.NANOSECONDS);
            return ms + "µs";
        }

        final long ms = TimeUnit.MILLISECONDS.convert(nanos, TimeUnit.NANOSECONDS);
        return ms + "ms";
    }

    public static Direction crossProduct(Direction forward, Direction up) {
        final int west_x = forward.getStepY() * up.getStepZ() - forward.getStepZ() * up.getStepY();
        final int west_y = forward.getStepZ() * up.getStepX() - forward.getStepX() * up.getStepZ();
        final int west_z = forward.getStepX() * up.getStepY() - forward.getStepY() * up.getStepX();

        return switch (west_x + west_y * 2 + west_z * 3) {
            case 1 -> Direction.EAST;
            case -1 -> Direction.WEST;
            case 2 -> Direction.UP;
            case -2 -> Direction.DOWN;
            case 3 -> Direction.SOUTH;
            case -3 -> Direction.NORTH;
            default ->

                // something is better then nothing?
                Direction.NORTH;
        };

    }

    /*
     * returns true if the code is on the client.
     */
    public static boolean isClient() {
        var currentServer = AppEng.instance().getCurrentServer();
        return currentServer == null || Thread.currentThread() != currentServer.getRunningThread();
    }

    public static boolean hasPermissions(DimensionalBlockPos dc, Player player) {
        if (!dc.isInWorld(player.level)) {
            return false;
        }
        return player.level.mayInteract(player, dc.getPos());
    }

    public static boolean checkPermissions(Player player,
            IActionHost actionHost,
            SecurityPermissions requiredPermission,
            boolean requirePower,
            boolean notifyPlayer) {
        var gn = actionHost.getActionableNode();
        if (gn != null) {
            var g = gn.getGrid();
            if (requirePower) {
                var eg = g.getEnergyService();
                if (!eg.isNetworkPowered()) {
                    return false;
                }
            }

            var sg = g.getSecurityService();
            if (!sg.hasPermission(player, requiredPermission)) {
                if (notifyPlayer) {
                    player.sendMessage(new TranslatableComponent("ae2.permission_denied")
                            .withStyle(ChatFormatting.RED), Util.NIL_UUID);
                }
                return false;
            }
        }

        return true;
    }

    public static ItemStack[] getBlockDrops(Level level, BlockPos pos) {
        // FIXME: Check assumption here and if this could only EVER be called with a
        // server level
        if (!(level instanceof ServerLevel serverLevel)) {
            return new ItemStack[0];
        }

        final BlockState state = level.getBlockState(pos);
        final BlockEntity blockEntity = level.getBlockEntity(pos);

        List<ItemStack> out = Block.getDrops(state, serverLevel, pos, blockEntity);

        return out.toArray(new ItemStack[0]);
    }

    /*
     * Generates Item entities in the level similar to how items are generally dropped.
     */
    public static void spawnDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        if (!level.isClientSide()) {
            for (ItemStack i : drops) {
                if (!i.isEmpty() && i.getCount() > 0) {
                    final double offset_x = (getRandomInt() % 32 - 16) / 82D;
                    final double offset_y = (getRandomInt() % 32 - 16) / 82D;
                    final double offset_z = (getRandomInt() % 32 - 16) / 82D;
                    final ItemEntity ei = new ItemEntity(level, 0.5 + offset_x + pos.getX(),
                            0.5 + offset_y + pos.getY(), 0.2 + offset_z + pos.getZ(), i.copy());
                    level.addFreshEntity(ei);
                }
            }
        }
    }

    /*
     * returns true if the code is on the server.
     */
    public static boolean isServer() {
        try {
            var currentServer = AppEng.instance().getCurrentServer();
            return currentServer != null && Thread.currentThread() == currentServer.getRunningThread();
        } catch (NullPointerException npe) {
            // FIXME TEST HACKS
            // Running from tests: AppEng.instance() is null... :(
            return false;
        }
    }

    /**
     * Throws an exception if the current thread is not one of the server threads.
     */
    public static void assertServerThread() {
        if (!isServer()) {
            throw new UnsupportedOperationException(
                    "This code can only be called server-side and this is most likely a bug.");
        }
    }

    public static int getRandomInt() {
        return Math.abs(RANDOM_GENERATOR.nextInt());
    }

    public static String formatModName(String modId) {
        return "" + ChatFormatting.BLUE + ChatFormatting.ITALIC
                + FABRIC.getModContainer(modId).map(mc -> mc.getMetadata().getName()).orElse(null);
    }

    public static String getDescriptionId(Fluid fluid) {
        return fluid.defaultFluidState().createLegacyBlock().getBlock().getDescriptionId();
    }

    public static String getDescriptionId(FluidVariant fluid) {
        return getDescriptionId(fluid.getFluid());
    }

    public static Component getFluidDisplayName(Fluid fluid, @Nullable CompoundTag tag) {
        // no usage of the tag, but we keep it for compatibility
        return new TranslatableComponent(getDescriptionId(fluid));
    }

    // tag copy is not necessary, as the tag is not modified.
    // and this itemStack is not reachable
    public static Component getItemDisplayName(Item item, @Nullable CompoundTag tag) {
        var itemStack = new ItemStack(item);
        itemStack.setTag(tag);
        return itemStack.getHoverName();
    }

    public static boolean isChargeable(ItemStack i) {
        if (i.isEmpty()) {
            return false;
        }
        if (i.getItem() instanceof IAEItemPowerStorage powerStorage) {
            return powerStorage.getAEMaxPower(i) > 0 &&
                    powerStorage.getPowerFlow(i) != AccessRestriction.READ;
        }
        return false;
    }

    public static Player getPlayer(ServerLevel level) {
        Objects.requireNonNull(level);

        return FakePlayer.getOrCreate(level);
    }

    /**
     * Returns a random element from the given collection.
     *
     * @return null if the collection is empty
     */
    @Nullable
    public static <T> T pickRandom(Collection<T> outs) {
        if (outs.isEmpty()) {
            return null;
        }

        int index = RANDOM_GENERATOR.nextInt(outs.size());
        return Iterables.get(outs, index, null);
    }

    public static Direction rotateAround(Direction forward, Direction axis) {
        if (forward.getAxis() == axis.getAxis()) {
            return forward;
        }
        var newForward = forward.getNormal().cross(axis.getNormal());
        return Objects.requireNonNull(Direction.fromNormal(new BlockPos(newForward)));
    }

    public static boolean securityCheck(GridNode a, GridNode b) {
        if (a.getLastSecurityKey() == -1 && b.getLastSecurityKey() == -1
                || a.getLastSecurityKey() == b.getLastSecurityKey()) {
            return true;
        }

        // If the node has no grid, it counts as unpowered
        final boolean a_isSecure = a.isPowered() && a.getLastSecurityKey() != -1;
        final boolean b_isSecure = b.isPowered() && b.getLastSecurityKey() != -1;

        if (AEConfig.instance().isSecurityAuditLogEnabled()) {
            AELog.info(
                    "Audit: Node A [isSecure=%b, key=%d, playerID=%d, %s] vs Node B[isSecure=%b, key=%d, playerID=%d, %s]",
                    a_isSecure, a.getLastSecurityKey(), a.getOwningPlayerId(), a, b_isSecure, b.getLastSecurityKey(),
                    b.getOwningPlayerId(), b);
        }

        // can't do that son...
        if (a_isSecure && b_isSecure) {
            return false;
        }

        if (!a_isSecure && b_isSecure) {
            // NOTE: b cannot be powered/secure if b has no grid, so b.getGrid() should succeed
            return checkPlayerPermissions(b.getGrid(), a.getOwningPlayerId());
        }

        if (a_isSecure && !b_isSecure) {
            // NOTE: a cannot be powered/secure if a has no grid, so a.getGrid() should succeed
            return checkPlayerPermissions(a.getGrid(), b.getOwningPlayerId());
        }

        return true;
    }

    private static boolean checkPlayerPermissions(IGrid grid, int playerID) {
        if (grid == null) {
            return true;
        }

        var gs = grid.getSecurityService();
        return !gs.isAvailable() || gs.hasPermission(playerID, SecurityPermissions.BUILD);
    }

    public static void configurePlayer(Player player, Direction side, BlockEntity blockEntity) {
        float pitch = 0.0f;
        float yaw = 0.0f;
        switch (side) {
            case DOWN, UP -> pitch = 90.0f;
            case EAST -> yaw = -90.0f;
            case NORTH -> yaw = 180.0f;
            case SOUTH -> yaw = 0.0f;
            case WEST -> yaw = 90.0f;
            default -> {
            }
        }

        player.moveTo(blockEntity.getBlockPos().getX() + 0.5, blockEntity.getBlockPos().getY() + 0.5,
                blockEntity.getBlockPos().getZ() + 0.5,
                yaw, pitch);
    }

    public static boolean canAccess(IManagedGridNode gridProxy, IActionSource src) {
        var grid = gridProxy.getGrid();
        if (grid == null) {
            return false;
        }

        if (src.player().isPresent()) {
            return grid.getSecurityService().hasPermission(src.player().get(), SecurityPermissions.BUILD);
        } else if (src.machine().isPresent()) {
            final IActionHost te = src.machine().get();
            final IGridNode n = te.getActionableNode();
            if (n == null) {
                return false;
            }

            final int playerID = n.getOwningPlayerId();
            return grid.getSecurityService().hasPermission(playerID, SecurityPermissions.BUILD);
        } else {
            return grid.getSecurityService().hasPermission(-1, SecurityPermissions.BUILD);
        }
    }

    public static ItemStack extractItemsByRecipe(IEnergySource energySrc,
            IActionSource mySrc,
            MEStorage src,
            Level level,
            Recipe<CraftingContainer> r,
            ItemStack output,
            CraftingContainer ci,
            ItemStack providedTemplate,
            int slot,
            KeyCounter items,
            Actionable realForFake,
            IPartitionList filter) {
        if (energySrc.extractAEPower(1, Actionable.SIMULATE, PowerMultiplier.CONFIG) > 0.9) {
            if (providedTemplate == null) {
                return ItemStack.EMPTY;
            }

            var ae_req = AEItemKey.of(providedTemplate);

            if (filter == null || filter.isListed(ae_req)) {
                var extracted = src.extract(ae_req, 1, realForFake, mySrc);
                if (extracted > 0) {
                    energySrc.extractAEPower(1, realForFake, PowerMultiplier.CONFIG);
                    return ae_req.toStack();
                }
            }

            var checkFuzzy = providedTemplate.hasTag() || providedTemplate.isDamageableItem();

            if (items != null && checkFuzzy) {
                for (var x : items) {
                    if (x.getKey() instanceof AEItemKey itemKey) {
                        if (providedTemplate.getItem() == itemKey.getItem() && !itemKey.matches(output)) {
                            ci.setItem(slot, itemKey.toStack());
                            if (r.matches(ci, level) && ItemStack.isSame(r.assemble(ci), output)) {
                                if (filter == null || filter.isListed(itemKey)) {
                                    var ex = src.extract(itemKey, 1, realForFake, mySrc);
                                    if (ex > 0) {
                                        energySrc.extractAEPower(1, realForFake, PowerMultiplier.CONFIG);
                                        return itemKey.toStack();
                                    }
                                }
                            }
                            ci.setItem(slot, providedTemplate);
                        }
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public static void notifyBlocksOfNeighbors(Level level, BlockPos pos) {
        if (level != null && !level.isClientSide) {
            TickHandler.instance().addCallable(level, new BlockUpdate(pos));
        }
    }

    public static boolean isSortOrderAvailable(SortOrder order) {
        return true;
    }

    /**
     * Retrieves a BlockEntity from a given position, but only if that particular BlockEntity would be in a state where
     * it would be ticked by the chunk.
     * <p/>
     * This method also doesn't return a block entity on the client-side.
     */
    @Nullable
    public static BlockEntity getTickingBlockEntity(@Nullable Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }

        if (!serverLevel.shouldTickBlocksAt(ChunkPos.asLong(pos))) {
            return null;
        }

        return serverLevel.getBlockEntity(pos);
    }

    /**
     * Checks that the chunk at the given position in the given level is in a state where block entities would tick.
     * Vanilla does this check in {@link Level#tickBlockEntities}
     */
    public static boolean areBlockEntitiesTicking(@Nullable Level level, BlockPos pos) {
        return level instanceof ServerLevel serverLevel && serverLevel.shouldTickBlocksAt(ChunkPos.asLong(pos));
    }

    public static Transaction openOrJoinTx() {
        return Transaction.openNested(Transaction.getCurrentUnsafe());
    }

    public static boolean canItemStacksStack(ItemStack a, ItemStack b) {
        if (a.isEmpty() || !a.sameItem(b) || a.hasTag() != b.hasTag())
            return false;

        return (!a.hasTag() || a.getTag().equals(b.getTag()));
    }

    /**
     * Create a full packet of the chunks data with lighting.
     */
    public static Packet<?> getFullChunkPacket(LevelChunk c) {
        return new ClientboundLevelChunkWithLightPacket(c, c.getLevel().getLightEngine(), null, null, true);
    }

    public static ItemStack getInsertionRemainder(ItemStack original, long inserted) {
        if (inserted >= original.getCount()) {
            return ItemStack.EMPTY;
        } else {
            return copyStackWithSize(original, (int) (original.getCount() - inserted));
        }
    }

    public static ItemStack copyStackWithSize(ItemStack itemStack, int size) {
        if (size <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack copy = itemStack.copy();
        copy.setCount(size);
        return copy;
    }

    /**
     * Send an update packet for the block entity at the given position to the player immediately, if they're a server
     * player.
     */
    public static void sendImmediateBlockEntityUpdate(Player player, BlockPos pos) {
        if (player instanceof ServerPlayer serverPlayer) {
            var be = player.getLevel().getBlockEntity(pos);
            if (be != null) {
                var packet = be.getUpdatePacket();
                if (packet != null) {
                    serverPlayer.connection.send(packet);
                }
            }
        }
    }
}
