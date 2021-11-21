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
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Iterables;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag.Default;
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
import appeng.api.config.SearchBoxMode;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.SortOrder;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.AEFluidKey;
import appeng.api.storage.data.AEItemKey;
import appeng.api.storage.data.KeyCounter;
import appeng.api.util.DimensionalBlockPos;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.hooks.ticking.TickHandler;
import appeng.integration.abstraction.JEIFacade;
import appeng.items.tools.quartz.QuartzToolType;
import appeng.me.GridNode;
import appeng.util.helpers.ItemComparisonHelper;
import appeng.util.helpers.P2PHelper;
import appeng.util.prioritylist.IPartitionList;

public class Platform {

    private static final FabricLoader FABRIC = FabricLoader.getInstance();

    /*
     * random source, use it for item drop locations...
     */
    private static final Random RANDOM_GENERATOR = new Random();
    private static final WeakHashMap<Level, Player> FAKE_PLAYERS = new WeakHashMap<>();

    private static final ItemComparisonHelper ITEM_COMPARISON_HELPER = new ItemComparisonHelper();

    public static ItemComparisonHelper itemComparisons() {
        return ITEM_COMPARISON_HELPER;
    }

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
    public static String formatPowerLong(final long n, final boolean isRate) {
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

    public static Direction crossProduct(final Direction forward, final Direction up) {
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

    public static boolean hasPermissions(final DimensionalBlockPos dc, final Player player) {
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
            if (g != null) {
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
        }

        return true;
    }

    public static ItemStack[] getBlockDrops(final Level level, final BlockPos pos) {
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
    public static void spawnDrops(final Level level, final BlockPos pos, final List<ItemStack> drops) {
        if (!level.isClientSide()) {
            for (final ItemStack i : drops) {
                if (!i.isEmpty() && i.getCount() > 0) {
                    final double offset_x = (getRandomInt() % 32 - 16) / 82;
                    final double offset_y = (getRandomInt() % 32 - 16) / 82;
                    final double offset_z = (getRandomInt() % 32 - 16) / 82;
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

    @Environment(EnvType.CLIENT)
    public static List<Component> getTooltip(AEItemKey item) {
        return getTooltip(item.toStack());
    }

    @Environment(EnvType.CLIENT)
    public static List<Component> getTooltip(final Object o) {
        if (o == null) {
            return Collections.emptyList();
        }

        ItemStack itemStack = ItemStack.EMPTY;
        if (o instanceof ItemStack) {
            itemStack = (ItemStack) o;
        } else {
            return Collections.emptyList();
        }

        try {
            Default tooltipFlag = Minecraft.getInstance().options.advancedItemTooltips
                    ? Default.ADVANCED
                    : Default.NORMAL;
            return itemStack.getTooltipLines(Minecraft.getInstance().player, tooltipFlag);
        } catch (final Exception errB) {
            return Collections.emptyList();
        }
    }

    public static String formatModName(String modId) {
        return "" + ChatFormatting.BLUE + ChatFormatting.ITALIC
                + FABRIC.getModContainer(modId).map(mc -> mc.getMetadata().getName()).orElse(null);
    }

    public static Component getItemDisplayName(AEItemKey what) {
        return getItemDisplayName(what.toStack());
    }

    public static Component getItemDisplayName(final Object o) {
        if (o == null) {
            return new TextComponent("** Null");
        }

        ItemStack itemStack = ItemStack.EMPTY;
        if (o instanceof ItemStack) {
            itemStack = (ItemStack) o;
        } else {
            return new TextComponent("**Invalid Object");
        }

        try {
            return itemStack.getHoverName();
        } catch (final Exception errA) {
            try {
                return new TranslatableComponent(itemStack.getDescriptionId());
            } catch (final Exception errB) {
                return new TextComponent("** Exception");
            }
        }
    }

    public static String getDescriptionId(Fluid fluid) {
        return fluid.defaultFluidState().createLegacyBlock().getBlock().getDescriptionId();
    }

    public static String getDescriptionId(FluidVariant fluid) {
        return getDescriptionId(fluid.getFluid());
    }

    public static Component getFluidDisplayName(AEFluidKey o) {
        return new TranslatableComponent(getDescriptionId(o.toVariant()));
    }

    public static boolean isChargeable(final ItemStack i) {
        if (i.isEmpty()) {
            return false;
        }
        final Item it = i.getItem();
        if (it instanceof IAEItemPowerStorage) {
            return ((IAEItemPowerStorage) it).getPowerFlow(i) != AccessRestriction.READ;
        }
        return false;
    }

    public static Player getPlayer(final ServerLevel level) {
        Objects.requireNonNull(level);

        return FakePlayer.getOrCreate(level);
    }

    public static boolean isFakePlayer(Player player) {
        return FakePlayer.isFakePlayer(player);
    }

    /**
     * Returns a random element from the given collection.
     *
     * @return null if the collection is empty
     */
    @Nullable
    public static <T> T pickRandom(final Collection<T> outs) {
        if (outs.isEmpty()) {
            return null;
        }

        int index = RANDOM_GENERATOR.nextInt(outs.size());
        return Iterables.get(outs, index, null);
    }

    public static Direction rotateAround(final Direction forward, final Direction axis) {
        switch (forward) {
            case DOWN:
                switch (axis) {
                    case DOWN:
                        return forward;
                    case UP:
                        return forward;
                    case NORTH:
                        return Direction.EAST;
                    case SOUTH:
                        return Direction.WEST;
                    case EAST:
                        return Direction.NORTH;
                    case WEST:
                        return Direction.SOUTH;
                    default:
                        break;
                }
                break;
            case UP:
                switch (axis) {
                    case NORTH:
                        return Direction.WEST;
                    case SOUTH:
                        return Direction.EAST;
                    case EAST:
                        return Direction.SOUTH;
                    case WEST:
                        return Direction.NORTH;
                    default:
                        break;
                }
                break;
            case NORTH:
                switch (axis) {
                    case UP:
                        return Direction.WEST;
                    case DOWN:
                        return Direction.EAST;
                    case EAST:
                        return Direction.UP;
                    case WEST:
                        return Direction.DOWN;
                    default:
                        break;
                }
                break;
            case SOUTH:
                switch (axis) {
                    case UP:
                        return Direction.EAST;
                    case DOWN:
                        return Direction.WEST;
                    case EAST:
                        return Direction.DOWN;
                    case WEST:
                        return Direction.UP;
                    default:
                        break;
                }
                break;
            case EAST:
                switch (axis) {
                    case UP:
                        return Direction.NORTH;
                    case DOWN:
                        return Direction.SOUTH;
                    case NORTH:
                        return Direction.UP;
                    case SOUTH:
                        return Direction.DOWN;
                    default:
                        break;
                }
            case WEST:
                switch (axis) {
                    case UP:
                        return Direction.SOUTH;
                    case DOWN:
                        return Direction.NORTH;
                    case NORTH:
                        return Direction.DOWN;
                    case SOUTH:
                        return Direction.UP;
                    default:
                        break;
                }
            default:
                break;
        }
        return forward;
    }

    public static boolean securityCheck(final GridNode a, final GridNode b) {
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
            // NOTE: a cannot be powered/secure if a has no grid, so b.getGrid() should succeed
            return checkPlayerPermissions(a.getGrid(), b.getOwningPlayerId());
        }

        return true;
    }

    private static boolean checkPlayerPermissions(final IGrid grid, final int playerID) {
        if (grid == null) {
            return true;
        }

        var gs = grid.getSecurityService();
        return !gs.isAvailable() || gs.hasPermission(playerID, SecurityPermissions.BUILD);
    }

    public static void configurePlayer(final Player player, final Direction side, final BlockEntity blockEntity) {
        float pitch = 0.0f;
        float yaw = 0.0f;
        // player.yOffset = 1.8f;

        switch (side) {
            case DOWN:
                pitch = 90.0f;
                // player.getYOffset() = -1.8f;
                break;
            case EAST:
                yaw = -90.0f;
                break;
            case NORTH:
                yaw = 180.0f;
                break;
            case SOUTH:
                yaw = 0.0f;
                break;
            case UP:
                pitch = 90.0f;
                break;
            case WEST:
                yaw = 90.0f;
                break;
            default:
                break;
        }

        player.moveTo(blockEntity.getBlockPos().getX() + 0.5, blockEntity.getBlockPos().getY() + 0.5,
                blockEntity.getBlockPos().getZ() + 0.5,
                yaw, pitch);
    }

    public static boolean canAccess(final IManagedGridNode gridProxy, final IActionSource src) {
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
            return false;
        }
    }

    public static ItemStack extractItemsByRecipe(IEnergySource energySrc,
            IActionSource mySrc,
            IMEMonitor<AEItemKey> src,
            Level level,
            Recipe<CraftingContainer> r,
            ItemStack output,
            CraftingContainer ci,
            ItemStack providedTemplate,
            int slot,
            KeyCounter<AEItemKey> items,
            Actionable realForFake,
            IPartitionList<AEItemKey> filter) {
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
                    var availableKey = x.getKey();
                    if (providedTemplate.getItem() == availableKey.getItem() && !availableKey.matches(output)) {
                        ci.setItem(slot, availableKey.toStack());
                        if (r.matches(ci, level) && ItemStack.isSame(r.assemble(ci), output)) {
                            if (filter == null || filter.isListed(availableKey)) {
                                var ex = src.extract(availableKey, 1, realForFake, mySrc);
                                if (ex > 0) {
                                    energySrc.extractAEPower(1, realForFake, PowerMultiplier.CONFIG);
                                    return availableKey.toStack();
                                }
                            }
                        }
                        ci.setItem(slot, providedTemplate);
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public static void notifyBlocksOfNeighbors(final Level level, final BlockPos pos) {
        if (!level.isClientSide) {
            TickHandler.instance().addCallable(level, new BlockUpdate(pos));
        }
    }

    public static boolean canRepair(final QuartzToolType type, final ItemStack a, final ItemStack b) {
        if (b.isEmpty() || a.isEmpty()) {
            return false;
        }

        if (type == QuartzToolType.CERTUS) {
            return AEItems.CERTUS_QUARTZ_CRYSTAL.isSameAs(b);
        }

        if (type == QuartzToolType.NETHER) {
            return Items.QUARTZ == b.getItem();
        }

        return false;
    }

    public static boolean isSortOrderAvailable(SortOrder order) {
        return true;
    }

    public static boolean isSearchModeAvailable(SearchBoxMode mode) {
        if (mode.isRequiresJei()) {
            return JEIFacade.instance().isEnabled();
        }
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

    public static String formatFluidAmount(long amount) {
        return NumberFormat.getNumberInstance(Locale.US).format(amount / (double) AEFluidKey.AMOUNT_BUCKET) + " B";
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

    public static boolean canItemStacksStack(@Nonnull ItemStack a, @Nonnull ItemStack b) {
        if (a.isEmpty() || !a.sameItem(b) || a.hasTag() != b.hasTag())
            return false;

        return (!a.hasTag() || a.getTag().equals(b.getTag()));
    }

    @Nonnull
    public static ItemStack copyStackWithSize(@Nonnull ItemStack itemStack, int size) {
        if (size == 0)
            return ItemStack.EMPTY;
        ItemStack copy = itemStack.copy();
        copy.setCount(size);
        return copy;
    }

    /**
     * Create a full packet of the chunks data with lighting.
     */
    public static Packet<?> getFullChunkPacket(LevelChunk c) {
        return new ClientboundLevelChunkWithLightPacket(c, c.getLevel().getLightEngine(), null, null, true);
    }
}
