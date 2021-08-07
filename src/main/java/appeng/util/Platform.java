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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.WeakHashMap;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag.Default;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import net.minecraftforge.registries.ForgeRegistries;

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
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.security.ISecurityService;
import appeng.api.networking.storage.IStorageService;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalBlockPos;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.Api;
import appeng.core.definitions.AEItems;
import appeng.core.stats.AeStats;
import appeng.hooks.ticking.TickHandler;
import appeng.integration.abstraction.JEIFacade;
import appeng.items.tools.quartz.QuartzToolType;
import appeng.me.GridNode;
import appeng.util.helpers.ItemComparisonHelper;
import appeng.util.helpers.P2PHelper;
import appeng.util.item.AEItemStack;
import appeng.util.prioritylist.IPartitionList;

/**
 * @author AlgorithmX2
 * @author thatsIch
 * @version rv2
 * @since rv0
 */
public class Platform {

    public static final int DEF_OFFSET = 16;

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
        final PowerUnits displayUnits = AEConfig.instance().getSelectedPowerUnit();
        p = PowerUnits.AE.convertTo(displayUnits, p);

        final String[] preFixes = { "k", "M", "G", "T", "P", "T", "P", "E", "Z", "Y" };
        String unitName = displayUnits.name();

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

    public static Direction crossProduct(final Direction forward, final Direction up) {
        final int west_x = forward.getStepY() * up.getStepZ() - forward.getStepZ() * up.getStepY();
        final int west_y = forward.getStepZ() * up.getStepX() - forward.getStepX() * up.getStepZ();
        final int west_z = forward.getStepX() * up.getStepY() - forward.getStepY() * up.getStepX();

        switch (west_x + west_y * 2 + west_z * 3) {
            case 1:
                return Direction.EAST;
            case -1:
                return Direction.WEST;

            case 2:
                return Direction.UP;
            case -2:
                return Direction.DOWN;

            case 3:
                return Direction.SOUTH;
            case -3:
                return Direction.NORTH;
        }

        // something is better then nothing?
        return Direction.NORTH;
    }

    /**
     * @return True if client-side classes (such as Renderers) are available.
     */
    public static boolean hasClientClasses() {
        return FMLEnvironment.dist.isClient();
    }

    /*
     * returns true if the code is on the client.
     */
    public static boolean isClient() {
        return Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER;
    }

    public static boolean hasPermissions(final DimensionalBlockPos dc, final Player player) {
        if (!dc.isInWorld(player.level)) {
            return false;
        }
        return player.level.mayInteract(player, dc.getPos());
    }

    public static boolean checkPermissions(final Player player, final Object accessInterface,
            SecurityPermissions requiredPermission, boolean notifyPlayer) {
        // FIXME: Check permissions...
        if (requiredPermission != null && accessInterface instanceof IActionHost) {
            final IGridNode gn = ((IActionHost) accessInterface).getActionableNode();
            if (gn != null) {
                final IGrid g = gn.getGrid();
                if (g != null) {
                    final boolean requirePower = false;
                    if (requirePower) {
                        final IEnergyService eg = g.getService(IEnergyService.class);
                        if (!eg.isNetworkPowered()) {
                            // FIXME trace logging?
                            return false;
                        }
                    }

                    final ISecurityService sg = g.getService(ISecurityService.class);
                    if (!sg.hasPermission(player, requiredPermission)) {
                        player.sendMessage(new TranslatableComponent("appliedenergistics2.permission_denied")
                                .withStyle(ChatFormatting.RED), Util.NIL_UUID);
                        // FIXME trace logging?
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public static ItemStack[] getBlockDrops(final Level w, final BlockPos pos) {
        // FIXME: Check assumption here and if this could only EVER be called with a
        // server level
        if (!(w instanceof ServerLevel serverLevel)) {
            return new ItemStack[0];
        }

        final BlockState state = w.getBlockState(pos);
        final BlockEntity blockEntity = w.getBlockEntity(pos);

        List<ItemStack> out = Block.getDrops(state, serverLevel, pos, blockEntity);

        return out.toArray(new ItemStack[0]);
    }

    /*
     * Generates Item entities in the level similar to how items are generally dropped.
     */
    public static void spawnDrops(final Level w, final BlockPos pos, final List<ItemStack> drops) {
        if (!w.isClientSide()) {
            for (final ItemStack i : drops) {
                if (!i.isEmpty() && i.getCount() > 0) {
                    final double offset_x = (getRandomInt() % 32 - 16) / 82;
                    final double offset_y = (getRandomInt() % 32 - 16) / 82;
                    final double offset_z = (getRandomInt() % 32 - 16) / 82;
                    final ItemEntity ei = new ItemEntity(w, 0.5 + offset_x + pos.getX(),
                            0.5 + offset_y + pos.getY(), 0.2 + offset_z + pos.getZ(), i.copy());
                    w.addFreshEntity(ei);
                }
            }
        }
    }

    /*
     * returns true if the code is on the server.
     */
    public static boolean isServer() {
        return Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER;
    }

    /**
     * Throws an exception if the current thread is not one of the server threads.
     */
    public static void assertServerThread() {
        if (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) {
            throw new UnsupportedOperationException(
                    "This code can only be called server-side and this is most likely a bug.");
        }
    }

    public static int getRandomInt() {
        return Math.abs(RANDOM_GENERATOR.nextInt());
    }

    @OnlyIn(Dist.CLIENT)
    public static List<Component> getTooltip(final Object o) {
        if (o == null) {
            return Collections.emptyList();
        }

        ItemStack itemStack = ItemStack.EMPTY;
        if (o instanceof AEItemStack) {
            final AEItemStack ais = (AEItemStack) o;
            return ais.getToolTip();
        } else if (o instanceof ItemStack) {
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

    public static String getModId(final IAEItemStack is) {
        if (is == null) {
            return "** Null";
        }

        final String n = ((AEItemStack) is).getModID();
        return n == null ? "** Null" : n;
    }

    public static String getModId(final IAEFluidStack fs) {
        if (fs == null || fs.getFluidStack().isEmpty()) {
            return "** Null";
        }

        final ResourceLocation n = ForgeRegistries.FLUIDS.getKey(fs.getFluidStack().getFluid());
        return n == null ? "** Null" : n.getNamespace();
    }

    public static String getModName(String modId) {
        return "" + ChatFormatting.BLUE + ChatFormatting.ITALIC
                + ModList.get().getModContainerById(modId).map(mc -> mc.getModInfo().getDisplayName()).orElse(null);
    }

    public static Component getItemDisplayName(final Object o) {
        if (o == null) {
            return new TextComponent("** Null");
        }

        ItemStack itemStack = ItemStack.EMPTY;
        if (o instanceof AEItemStack) {
            final Component n = ((AEItemStack) o).getDisplayName();
            return n == null ? new TextComponent("** Null") : n;
        } else if (o instanceof ItemStack) {
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

    public static Component getFluidDisplayName(IAEFluidStack o) {
        if (o == null) {
            return new TextComponent("** Null");
        }
        FluidStack fluidStack = o.getFluidStack();
        return fluidStack.getDisplayName();
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

    public static Player getPlayer(final ServerLevel w) {
        Objects.requireNonNull(w);

        final Player wrp = FAKE_PLAYERS.get(w);
        if (wrp != null) {
            return wrp;
        }

        final Player p = FakePlayerFactory.getMinecraft(w);
        FAKE_PLAYERS.put(w, p);
        return p;
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

    public static AEPartLocation rotateAround(final AEPartLocation forward, final AEPartLocation axis) {
        if (axis == AEPartLocation.INTERNAL || forward == AEPartLocation.INTERNAL) {
            return forward;
        }

        switch (forward) {
            case DOWN:
                switch (axis) {
                    case DOWN:
                        return forward;
                    case UP:
                        return forward;
                    case NORTH:
                        return AEPartLocation.EAST;
                    case SOUTH:
                        return AEPartLocation.WEST;
                    case EAST:
                        return AEPartLocation.NORTH;
                    case WEST:
                        return AEPartLocation.SOUTH;
                    default:
                        break;
                }
                break;
            case UP:
                switch (axis) {
                    case NORTH:
                        return AEPartLocation.WEST;
                    case SOUTH:
                        return AEPartLocation.EAST;
                    case EAST:
                        return AEPartLocation.SOUTH;
                    case WEST:
                        return AEPartLocation.NORTH;
                    default:
                        break;
                }
                break;
            case NORTH:
                switch (axis) {
                    case UP:
                        return AEPartLocation.WEST;
                    case DOWN:
                        return AEPartLocation.EAST;
                    case EAST:
                        return AEPartLocation.UP;
                    case WEST:
                        return AEPartLocation.DOWN;
                    default:
                        break;
                }
                break;
            case SOUTH:
                switch (axis) {
                    case UP:
                        return AEPartLocation.EAST;
                    case DOWN:
                        return AEPartLocation.WEST;
                    case EAST:
                        return AEPartLocation.DOWN;
                    case WEST:
                        return AEPartLocation.UP;
                    default:
                        break;
                }
                break;
            case EAST:
                switch (axis) {
                    case UP:
                        return AEPartLocation.NORTH;
                    case DOWN:
                        return AEPartLocation.SOUTH;
                    case NORTH:
                        return AEPartLocation.UP;
                    case SOUTH:
                        return AEPartLocation.DOWN;
                    default:
                        break;
                }
            case WEST:
                switch (axis) {
                    case UP:
                        return AEPartLocation.SOUTH;
                    case DOWN:
                        return AEPartLocation.NORTH;
                    case NORTH:
                        return AEPartLocation.DOWN;
                    case SOUTH:
                        return AEPartLocation.UP;
                    default:
                        break;
                }
            default:
                break;
        }
        return forward;
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

    public static <T extends IAEStack<T>> T poweredExtraction(final IEnergySource energy, final IMEInventory<T> cell,
            final T request, final IActionSource src) {
        return poweredExtraction(energy, cell, request, src, Actionable.MODULATE);
    }

    public static <T extends IAEStack<T>> T poweredExtraction(final IEnergySource energy, final IMEInventory<T> cell,
            final T request, final IActionSource src, final Actionable mode) {
        Preconditions.checkNotNull(energy);
        Preconditions.checkNotNull(cell);
        Preconditions.checkNotNull(request);
        Preconditions.checkNotNull(src);
        Preconditions.checkNotNull(mode);

        final T possible = cell.extractItems(request.copy(), Actionable.SIMULATE, src);

        long retrieved = 0;
        if (possible != null) {
            retrieved = possible.getStackSize();
        }

        final double energyFactor = Math.max(1.0, cell.getChannel().transferFactor());
        final double availablePower = energy.extractAEPower(retrieved / energyFactor, Actionable.SIMULATE,
                PowerMultiplier.CONFIG);
        final long itemToExtract = Math.min((long) (availablePower * energyFactor + 0.9), retrieved);

        if (itemToExtract > 0) {
            if (mode == Actionable.MODULATE) {
                energy.extractAEPower(retrieved / energyFactor, Actionable.MODULATE, PowerMultiplier.CONFIG);
                possible.setStackSize(itemToExtract);
                final T ret = cell.extractItems(possible, Actionable.MODULATE, src);

                if (ret != null) {
                    src.player()
                            .ifPresent(player -> AeStats.ItemsExtracted.addToPlayer(player, (int) ret.getStackSize()));
                }
                return ret;
            } else {
                return possible.setStackSize(itemToExtract);
            }
        }

        return null;
    }

    public static <T extends IAEStack<T>> T poweredInsert(final IEnergySource energy, final IMEInventory<T> cell,
            final T input, final IActionSource src) {
        return poweredInsert(energy, cell, input, src, Actionable.MODULATE);
    }

    public static <T extends IAEStack<T>> T poweredInsert(final IEnergySource energy, final IMEInventory<T> cell,
            final T input, final IActionSource src, final Actionable mode) {
        Preconditions.checkNotNull(energy);
        Preconditions.checkNotNull(cell);
        Preconditions.checkNotNull(input);
        Preconditions.checkNotNull(src);
        Preconditions.checkNotNull(mode);

        final T overflow = cell.injectItems(input.copy(), Actionable.SIMULATE, src);

        long transferAmount = input.getStackSize();
        if (overflow != null) {
            transferAmount -= overflow.getStackSize();
        }

        final double energyFactor = Math.max(1.0, cell.getChannel().transferFactor());
        final double availablePower = energy.extractAEPower(transferAmount / energyFactor, Actionable.SIMULATE,
                PowerMultiplier.CONFIG);
        final long itemToAdd = Math.min((long) (availablePower * energyFactor + 0.9), transferAmount);

        if (itemToAdd > 0) {
            if (mode == Actionable.MODULATE) {
                energy.extractAEPower(transferAmount / energyFactor, Actionable.MODULATE, PowerMultiplier.CONFIG);
                if (itemToAdd < input.getStackSize()) {
                    final long original = input.getStackSize();
                    final T leftover = input.copy();
                    final T split = input.copy();

                    leftover.decStackSize(itemToAdd);
                    split.setStackSize(itemToAdd);
                    leftover.add(cell.injectItems(split, Actionable.MODULATE, src));

                    src.player().ifPresent(player -> {
                        final long diff = original - leftover.getStackSize();
                        AeStats.ItemsInserted.addToPlayer(player, (int) diff);
                    });

                    return leftover;
                }

                final T ret = cell.injectItems(input, Actionable.MODULATE, src);

                src.player().ifPresent(player -> {
                    final long diff = ret == null ? input.getStackSize() : input.getStackSize() - ret.getStackSize();
                    AeStats.ItemsInserted.addToPlayer(player, (int) diff);
                });

                return ret;
            } else {
                final T ret = input.copy().setStackSize(input.getStackSize() - itemToAdd);
                return ret != null && ret.getStackSize() > 0 ? ret : null;
            }
        }

        return input;
    }

    /**
     * Post inventory changes from a whole cell being added or removed from the grid.
     */
    public static void postWholeCellChanges(IStorageService service,
            ItemStack removedCell,
            ItemStack addedCell,
            IActionSource src) {
        for (var channel : Api.instance().storage().storageChannels()) {
            postWholeCellChanges(service, channel, removedCell, addedCell, src);
        }
    }

    private static <T extends IAEStack<T>> void postWholeCellChanges(IStorageService service,
            IStorageChannel<T> channel,
            ItemStack removedCell,
            ItemStack addedCell,
            IActionSource src) {
        var myChanges = channel.createList();

        if (!removedCell.isEmpty()) {
            var myInv = Api.instance().registries().cell().getCellInventory(removedCell, null, channel);
            if (myInv != null) {
                myInv.getAvailableItems(myChanges);
                for (var is : myChanges) {
                    is.setStackSize(-is.getStackSize());
                }
            }
        }
        if (!addedCell.isEmpty()) {
            var myInv = Api.instance().registries().cell().getCellInventory(addedCell, null, channel);
            if (myInv != null) {
                myInv.getAvailableItems(myChanges);
            }

        }
        service.postAlterationOfStoredItems(channel, myChanges, src);
    }

    public static <T extends IAEStack<T>> void postListChanges(final IItemList<T> before, final IItemList<T> after,
            final IMEMonitorHandlerReceiver<T> meMonitorPassthrough, final IActionSource source) {
        final List<T> changes = new ArrayList<>();

        for (final T is : before) {
            is.setStackSize(-is.getStackSize());
        }

        for (final T is : after) {
            before.add(is);
        }

        for (final T is : before) {
            if (is.getStackSize() != 0) {
                changes.add(is);
            }
        }

        if (!changes.isEmpty()) {
            meMonitorPassthrough.postChange(null, changes, source);
        }
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

        final ISecurityService gs = grid.getService(ISecurityService.class);

        if (gs == null) {
            return true;
        }

        if (!gs.isAvailable()) {
            return true;
        }

        return gs.hasPermission(playerID, SecurityPermissions.BUILD);
    }

    public static void configurePlayer(final Player player, final AEPartLocation side, final BlockEntity blockEntity) {
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
            case INTERNAL:
                break;
            case UP:
                pitch = 90.0f;
                break;
            case WEST:
                yaw = 90.0f;
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

    public static ItemStack extractItemsByRecipe(final IEnergySource energySrc, final IActionSource mySrc,
            final IMEMonitor<IAEItemStack> src, final Level w, final Recipe<CraftingContainer> r,
            final ItemStack output, final CraftingContainer ci, final ItemStack providedTemplate, final int slot,
            final IItemList<IAEItemStack> items, final Actionable realForFake,
            final IPartitionList<IAEItemStack> filter) {
        if (energySrc.extractAEPower(1, Actionable.SIMULATE, PowerMultiplier.CONFIG) > 0.9) {
            if (providedTemplate == null) {
                return ItemStack.EMPTY;
            }

            final AEItemStack ae_req = AEItemStack.fromItemStack(providedTemplate);
            ae_req.setStackSize(1);

            if (filter == null || filter.isListed(ae_req)) {
                final IAEItemStack ae_ext = src.extractItems(ae_req, realForFake, mySrc);
                if (ae_ext != null) {
                    final ItemStack extracted = ae_ext.createItemStack();
                    if (!extracted.isEmpty()) {
                        energySrc.extractAEPower(1, realForFake, PowerMultiplier.CONFIG);
                        return extracted;
                    }
                }
            }

            final boolean checkFuzzy = /*
                                        * FIXME ae_req.getOre().isPresent() || FIXME providedTemplate.getDamage() ==
                                        * OreDictionary.WILDCARD_VALUE ||
                                        */ providedTemplate.hasTag() || providedTemplate.isDamageableItem();

            if (items != null && checkFuzzy) {
                for (final IAEItemStack x : items) {
                    final ItemStack sh = x.getDefinition();
                    if (Platform.itemComparisons().isEqualItemType(providedTemplate, sh)
                            && !ItemStack.isSame(sh, output)) {
                        final ItemStack cp = sh.copy();
                        cp.setCount(1);
                        ci.setItem(slot, cp);
                        if (r.matches(ci, w) && ItemStack.isSame(r.assemble(ci), output)) {
                            final IAEItemStack ax = x.copy();
                            ax.setStackSize(1);
                            if (filter == null || filter.isListed(ax)) {
                                final IAEItemStack ex = src.extractItems(ax, realForFake, mySrc);
                                if (ex != null) {
                                    energySrc.extractAEPower(1, realForFake, PowerMultiplier.CONFIG);
                                    return ex.createItemStack();
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

    /**
     * Gets the container item for the given item or EMPTY. A container item is what remains when the item is used for
     * crafting, i.E. the empty bucket for a bucket of water.
     */
    public static ItemStack getContainerItem(final ItemStack stackInSlot) {
        if (stackInSlot == null) {
            return ItemStack.EMPTY;
        }

        final Item i = stackInSlot.getItem();
        if (i == null || !i.hasContainerItem(stackInSlot)) {
            if (stackInSlot.getCount() > 1) {
                stackInSlot.setCount(stackInSlot.getCount() - 1);
                return stackInSlot;
            }
            return ItemStack.EMPTY;
        }

        ItemStack ci = i.getContainerItem(stackInSlot.copy());
        if (!ci.isEmpty() && ci.isDamageableItem() && ci.getDamageValue() == ci.getMaxDamage()) {
            ci = ItemStack.EMPTY;
        }

        return ci;
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

        if (!serverLevel.isPositionTickingWithEntitiesLoaded(pos)) {
            return null;
        }

        return serverLevel.getBlockEntity(pos);
    }

    /**
     * Checks that the chunk at the given position in the given level is in a state where block entities would tick.
     * 
     * @see {@link net.minecraft.world.level.chunk.LevelChunk#isTicking(BlockPos)} (which is package-visible)
     */
    public static boolean areBlockEntitiesTicking(@Nullable Level level, BlockPos pos) {
        return level instanceof ServerLevel serverLevel && serverLevel.isPositionTickingWithEntitiesLoaded(pos);
    }

}
