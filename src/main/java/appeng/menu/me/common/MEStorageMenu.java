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

package appeng.menu.me.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.Setting;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.TypeFilter;
import appeng.api.config.ViewItems;
import appeng.api.implementations.blockentities.IMEChest;
import appeng.api.implementations.blockentities.IViewCellStorage;
import appeng.api.implementations.menuobjects.IPortableTerminal;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionHost;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import appeng.api.storage.cells.IBasicCellItem;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.core.AELog;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;
import appeng.core.sync.packets.MEInteractionPacket;
import appeng.core.sync.packets.MEInventoryUpdatePacket;
import appeng.helpers.InventoryAction;
import appeng.me.helpers.ChannelPowerSrc;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.ToolboxMenu;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.me.crafting.CraftAmountMenu;
import appeng.menu.me.interaction.StackInteractions;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.slot.RestrictedInputSlot;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerListener;
import appeng.util.Platform;

/**
 * @see MEStorageScreen
 */
public class MEStorageMenu extends AEBaseMenu
        implements IConfigManagerListener, IConfigurableObject, IMEInteractionHandler {

    public static final MenuType<MEStorageMenu> TYPE = MenuTypeBuilder
            .<MEStorageMenu, ITerminalHost>create(MEStorageMenu::new, ITerminalHost.class)
            .build("item_terminal");

    public static final MenuType<MEStorageMenu> PORTABLE_ITEM_CELL_TYPE = MenuTypeBuilder
            .<MEStorageMenu, IPortableTerminal>create(MEStorageMenu::new, IPortableTerminal.class)
            .build("portable_item_cell");
    public static final MenuType<MEStorageMenu> PORTABLE_FLUID_CELL_TYPE = MenuTypeBuilder
            .<MEStorageMenu, IPortableTerminal>create(MEStorageMenu::new, IPortableTerminal.class)
            .build("portable_fluid_cell");

    public static final MenuType<MEStorageMenu> WIRELESS_TYPE = MenuTypeBuilder
            .<MEStorageMenu, IPortableTerminal>create(MEStorageMenu::new, IPortableTerminal.class)
            .build("wirelessterm");

    private final List<RestrictedInputSlot> viewCellSlots;
    private final IConfigManager clientCM;
    private final ToolboxMenu toolboxMenu;
    private final ITerminalHost host;
    @GuiSync(99)
    public boolean canEditViewCells;
    @GuiSync(98)
    public boolean hasPower = false;
    /**
     * The number of active crafting jobs in the network. -1 means unknown and will hide the label on the screen.
     */
    @GuiSync(100)
    public int activeCraftingJobs = -1;

    private IConfigManagerListener gui;
    private IConfigManager serverCM;

    // This is null on the client-side and can be null on the server too
    @Nullable
    protected final MEStorage storage;

    @Nullable
    protected final IEnergySource powerSource;

    private final IncrementalUpdateHelper updateHelper = new IncrementalUpdateHelper();

    /**
     * A grid connection is optional for a screen showing the content of a {@link MEStorage}, because inventories like
     * portable cells are not grid connected.
     */
    @Nullable
    private IGridNode networkNode;

    /**
     * The repository of entries currently known on the client-side. This is maintained by the screen associated with
     * this menu and will only be non-null on the client-side.
     */
    @Nullable
    private IClientRepo clientRepo;

    /**
     * The last set of craftables sent to the client.
     */
    private Set<AEKey> previousCraftables = Collections.emptySet();
    private KeyCounter previousAvailableStacks = new KeyCounter();

    public MEStorageMenu(MenuType<?> menuType, int id, Inventory ip, ITerminalHost host) {
        this(menuType, id, ip, host, true);
    }

    protected MEStorageMenu(MenuType<?> menuType, int id, Inventory ip, ITerminalHost host, boolean bindInventory) {
        super(menuType, id, ip, host);

        this.host = host;
        this.clientCM = new ConfigManager(this);

        this.clientCM.registerSetting(Settings.SORT_BY, SortOrder.NAME);
        this.clientCM.registerSetting(Settings.VIEW_MODE, ViewItems.ALL);
        this.clientCM.registerSetting(Settings.TYPE_FILTER, TypeFilter.ALL);
        this.clientCM.registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING);

        IEnergySource powerSource = null;
        if (isServerSide()) {
            this.serverCM = host.getConfigManager();

            this.storage = host.getInventory();
            if (this.storage != null) {

                if (host instanceof IPortableTerminal || host instanceof IMEChest) {
                    powerSource = (IEnergySource) host;
                } else if (host instanceof IActionHost actionHost) {
                    var node = actionHost.getActionableNode();
                    if (node != null) {
                        this.networkNode = node;
                        var g = node.getGrid();
                        powerSource = new ChannelPowerSrc(this.networkNode, g.getEnergyService());
                    }
                }
            } else {
                this.setValidMenu(false);
            }
        } else {
            this.storage = null;
        }
        this.powerSource = powerSource;

        // Create slots for the view cells, in case the terminal host supports those
        if (!hideViewCells() && host instanceof IViewCellStorage) {
            var viewCellStorage = ((IViewCellStorage) host).getViewCellStorage();
            this.viewCellSlots = new ArrayList<>(viewCellStorage.size());
            for (int i = 0; i < viewCellStorage.size(); i++) {
                var slot = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.VIEW_CELL,
                        viewCellStorage, i);
                this.addSlot(slot, SlotSemantics.VIEW_CELL);
                this.viewCellSlots.add(slot);
            }
        } else {
            this.viewCellSlots = Collections.emptyList();
        }
        updateViewCellPermission();

        this.toolboxMenu = new ToolboxMenu(this);

        setupUpgrades(host.getUpgrades());

        if (bindInventory) {
            this.createPlayerInventorySlots(ip);
        }
    }

    public ToolboxMenu getToolbox() {
        return toolboxMenu;
    }

    protected boolean hideViewCells() {
        return false;
    }

    @Nullable
    public IGridNode getNetworkNode() {
        return this.networkNode;
    }

    public boolean isKeyVisible(AEKey key) {
        // If the host is a basic item cell with a limited key space, account for this
        if (host instanceof ItemMenuHost itemMenuHost) {
            if (itemMenuHost.getItemStack().getItem() instanceof IBasicCellItem basicCellItem) {
                return basicCellItem.getKeyType().contains(key);
            }
        }

        return true;
    }

    @Override
    public void broadcastChanges() {
        toolboxMenu.tick();

        if (isServerSide()) {
            // Close the screen if the backing network inventory has changed
            if (this.storage != this.host.getInventory()) {
                this.setValidMenu(false);
                return;
            }

            this.updateActiveCraftingJobs();

            for (var set : this.serverCM.getSettings()) {
                var sideLocal = this.serverCM.getSetting(set);
                var sideRemote = this.clientCM.getSetting(set);

                if (sideLocal != sideRemote) {
                    set.copy(serverCM, clientCM);
                    sendPacketToClient(new ConfigValuePacket(set, serverCM));
                }
            }

            var craftables = getCraftablesFromGrid();
            var availableStacks = storage == null ? new KeyCounter() : storage.getAvailableStacks();

            // This is currently not supported/backed by any network service
            var requestables = new KeyCounter();

            try {
                // Craftables
                // Newly craftable
                Sets.difference(previousCraftables, craftables).forEach(updateHelper::addChange);
                // No longer craftable
                Sets.difference(craftables, previousCraftables).forEach(updateHelper::addChange);

                // Available changes
                previousAvailableStacks.removeAll(availableStacks);
                previousAvailableStacks.removeZeros();
                previousAvailableStacks.keySet().forEach(updateHelper::addChange);

                if (updateHelper.hasChanges()) {
                    var builder = MEInventoryUpdatePacket
                            .builder(containerId, updateHelper.isFullUpdate());
                    builder.setFilter(this::isKeyVisible);
                    builder.addChanges(updateHelper, availableStacks, craftables, requestables);
                    builder.buildAndSend(this::sendPacketToClient);
                    updateHelper.commitChanges();
                }

            } catch (Exception e) {
                AELog.warn(e, "Failed to send incremental inventory update to client");
            }

            previousCraftables = ImmutableSet.copyOf(craftables);
            previousAvailableStacks = availableStacks;

            this.updatePowerStatus();

            updateViewCellPermission();

            super.broadcastChanges();
        }

    }

    protected boolean showsCraftables() {
        return true;
    }

    private Set<AEKey> getCraftablesFromGrid() {
        IGridNode hostNode = networkNode;
        // Wireless terminals do not directly expose the target grid (even though they have one)
        if (hostNode == null && host instanceof IActionHost actionHost) {
            hostNode = actionHost.getActionableNode();
        }
        if (!showsCraftables()) {
            return Collections.emptySet();
        }

        if (hostNode != null && hostNode.isActive()) {
            return hostNode.getGrid().getCraftingService().getCraftables(this::isKeyVisible);
        }
        return Collections.emptySet();
    }

    /**
     * The player's permission w.r.t. editing the terminal can change while it is open. Update the view cell permissions
     * accordingly.
     */
    private void updateViewCellPermission() {
        final boolean oldAccessible = this.canEditViewCells;
        this.canEditViewCells = this.hasAccess(SecurityPermissions.BUILD, false);
        if (this.canEditViewCells != oldAccessible) {
            for (RestrictedInputSlot slot : viewCellSlots) {
                slot.setAllowEdit(this.canEditViewCells);
            }
        }
    }

    protected void updatePowerStatus() {
        if (this.networkNode != null) {
            this.hasPower = this.networkNode.isActive();
        } else if (this.powerSource instanceof IEnergyService energyService) {
            this.hasPower = energyService.isNetworkPowered();
        } else if (this.powerSource != null) {
            this.hasPower = this.powerSource.extractAEPower(1, Actionable.SIMULATE, PowerMultiplier.CONFIG) > 0.8;
        } else {
            this.hasPower = false;
        }
    }

    @Override
    public void onServerDataSync() {
        super.onServerDataSync();

        // When the canEditViewCells field changes on the client-side after a data sync from the server,
        // update the associated slots accordingly
        for (RestrictedInputSlot slot : viewCellSlots) {
            slot.setAllowEdit(this.canEditViewCells);
        }
    }

    private void updateActiveCraftingJobs() {
        IGridNode hostNode = networkNode;
        // Wireless terminals do not directly expose the target grid (even though they
        // have one)
        if (hostNode == null && host instanceof IActionHost) {
            hostNode = ((IActionHost) host).getActionableNode();
        }
        IGrid grid = null;
        if (hostNode != null) {
            grid = hostNode.getGrid();
        }

        if (grid == null) {
            // No grid to query crafting jobs from
            this.activeCraftingJobs = -1;
            return;
        }

        int activeJobs = 0;
        for (var cpus : grid.getCraftingService().getCpus()) {
            if (cpus.isBusy()) {
                activeJobs++;
            }
        }
        this.activeCraftingJobs = activeJobs;
    }

    @Override
    public void onSettingChanged(IConfigManager manager, Setting<?> setting) {
        if (this.getGui() != null) {
            this.getGui().onSettingChanged(manager, setting);
        }
    }

    @Override
    public IConfigManager getConfigManager() {
        if (isServerSide()) {
            return this.serverCM;
        }
        return this.clientCM;
    }

    public List<ItemStack> getViewCells() {
        return this.viewCellSlots.stream()
                .map(AppEngSlot::getItem)
                .collect(Collectors.toList());
    }

    /**
     * Checks that the inventory monitor is connected, a power source exists and that it is powered.
     */
    protected final boolean canInteractWithGrid() {
        return this.storage != null && this.powerSource != null && this.isPowered();
    }

    @Override
    public final void handleInteraction(long serial, InventoryAction action) {
        if (isClientSide()) {
            NetworkHandler.instance().sendToServer(new MEInteractionPacket(containerId, serial, action));
            return;
        }

        // Do not allow interactions if there's no monitor or no power
        if (!canInteractWithGrid()) {
            return;
        }

        ServerPlayer player = (ServerPlayer) this.getPlayerInventory().player;

        // Serial -1 is used to target empty virtual slots, which only allows the player to put
        // items under their cursor into the network inventory
        if (serial == -1) {
            handleNetworkInteraction(player, null, action);
            return;
        }

        AEKey stack = getStackBySerial(serial);
        if (stack == null) {
            // This can happen if the client sent the request after we removed the item, but before
            // the client knows about it (-> network delay).
            return;
        }

        handleNetworkInteraction(player, stack, action);
    }

    protected void handleNetworkInteraction(ServerPlayer player, @Nullable AEKey clickedKey, InventoryAction action) {

        // Interacting with the network is not possible if there's no network.
        if (this.storage == null) {
            return;
        }

        if (action == InventoryAction.PICKUP_OR_SET_DOWN && StackInteractions.isKeySupported(clickedKey)) {
            action = InventoryAction.FILL_ITEM;
        }

        if (action == InventoryAction.SPLIT_OR_PLACE_SINGLE) {
            if (StackInteractions.getContainedStack(getCarried()) != null) {
                action = InventoryAction.EMPTY_ITEM;
            }
        }

        if (action == InventoryAction.FILL_ITEM) {
            handleFillingHeldItem(
                    (amount, mode) -> StorageHelper.poweredExtraction(powerSource, storage, clickedKey, amount,
                            getActionSource(), mode),
                    clickedKey);
        } else if (action == InventoryAction.EMPTY_ITEM) {
            handleEmptyHeldItem((what, amount, mode) -> StorageHelper.poweredInsert(powerSource, storage, what, amount,
                    getActionSource(), mode));
        } else if (action == InventoryAction.AUTO_CRAFT) {
            var locator = getLocator();
            if (locator != null && clickedKey != null) {
                CraftAmountMenu.open(player, locator, clickedKey, clickedKey.getAmountPerUnit());
            }
            return;
        }

        // Handle interactions where the player wants to put something into the network
        if (clickedKey == null) {
            if (action == InventoryAction.SPLIT_OR_PLACE_SINGLE || action == InventoryAction.ROLL_DOWN) {
                putCarriedItemIntoNetwork(true);
            } else if (action == InventoryAction.PICKUP_OR_SET_DOWN) {
                putCarriedItemIntoNetwork(false);
            }
            return;
        }

        if (!(clickedKey instanceof AEItemKey clickedItem)) {
            return;
        }

        switch (action) {
            case SHIFT_CLICK:
                moveOneStackToPlayer(clickedItem);
                break;

            case ROLL_DOWN: {
                // Insert 1 of the carried stack into the network (or at least try to), regardless of what we're
                // hovering in the network inventory.
                var carried = getCarried();
                if (!carried.isEmpty()) {
                    var what = AEItemKey.of(carried);
                    var inserted = StorageHelper.poweredInsert(powerSource, storage, what, 1, this.getActionSource());
                    if (inserted > 0) {
                        getCarried().shrink(1);
                    }
                }
            }
                break;
            case ROLL_UP:
            case PICKUP_SINGLE: {
                // Extract 1 of the hovered stack from the network (or at least try to), and add it to the carried item
                var item = getCarried();

                if (!item.isEmpty()) {
                    if (item.getCount() >= item.getMaxStackSize()) {
                        return; // Max stack size reached
                    }
                    if (!clickedItem.matches(item)) {
                        return; // Not stackable
                    }
                }

                var extracted = StorageHelper.poweredExtraction(powerSource, storage, clickedItem, 1,
                        this.getActionSource());
                if (extracted > 0) {
                    if (item.isEmpty()) {
                        setCarried(clickedItem.toStack());
                    } else {
                        // we checked beforehand that max stack size was not reached
                        item.grow(1);
                    }
                }
            }
                break;
            case PICKUP_OR_SET_DOWN: {
                if (!getCarried().isEmpty()) {
                    putCarriedItemIntoNetwork(false);
                } else {
                    var extracted = StorageHelper.poweredExtraction(
                            powerSource,
                            storage,
                            clickedItem,
                            clickedItem.getItem().getMaxStackSize(),
                            this.getActionSource());
                    if (extracted > 0) {
                        setCarried(clickedItem.toStack((int) extracted));
                    } else {
                        setCarried(ItemStack.EMPTY);
                    }
                }
            }
                break;
            case SPLIT_OR_PLACE_SINGLE:
                if (!getCarried().isEmpty()) {
                    putCarriedItemIntoNetwork(true);
                } else {
                    var extracted = storage.extract(
                            clickedItem,
                            clickedItem.getItem().getMaxStackSize(),
                            Actionable.SIMULATE,
                            this.getActionSource());

                    if (extracted > 0) {
                        // Half
                        extracted = extracted + 1 >> 1;
                        extracted = StorageHelper.poweredExtraction(powerSource, storage, clickedItem, extracted,
                                this.getActionSource());
                    }

                    if (extracted > 0) {
                        setCarried(clickedItem.toStack((int) extracted));
                    } else {
                        setCarried(ItemStack.EMPTY);
                    }
                }

                break;
            case CREATIVE_DUPLICATE:
                if (player.getAbilities().instabuild) {
                    var is = clickedItem.toStack();
                    is.setCount(is.getMaxStackSize());
                    setCarried(is);
                }
                break;
            case MOVE_REGION:
                final int playerInv = player.getInventory().items.size();
                for (int slotNum = 0; slotNum < playerInv; slotNum++) {
                    if (!moveOneStackToPlayer(clickedItem)) {
                        break;
                    }
                }
                break;
            default:
                AELog.warn("Received unhandled inventory action %s from client in %s", action, getClass());
                break;
        }
    }

    protected void putCarriedItemIntoNetwork(boolean singleItem) {
        var heldStack = getCarried();

        var what = AEItemKey.of(heldStack);
        if (what == null) {
            return;
        }

        var amount = heldStack.getCount();
        if (singleItem) {
            amount = 1;
        }

        var inserted = StorageHelper.poweredInsert(powerSource, storage, what, amount,
                this.getActionSource());
        setCarried(Platform.getInsertionRemainder(heldStack, inserted));
    }

    private boolean moveOneStackToPlayer(AEItemKey stack) {
        ItemStack myItem = stack.toStack();

        var playerInv = getPlayerInventory();
        var slot = playerInv.getSlotWithRemainingSpace(myItem);
        int toExtract;
        if (slot != -1) {
            // Try to fill up existing slot with item
            toExtract = myItem.getMaxStackSize() - playerInv.getItem(slot).getCount();
        } else {
            slot = playerInv.getFreeSlot();
            if (slot == -1) {
                return false; // No more free space
            }
            toExtract = myItem.getMaxStackSize();
        }
        if (toExtract <= 0) {
            return false;
        }

        var extracted = StorageHelper.poweredExtraction(powerSource, storage, stack, toExtract, getActionSource());
        if (extracted == 0) {
            return false; // No items available
        }

        var itemInSlot = playerInv.getItem(slot);
        if (itemInSlot.isEmpty()) {
            playerInv.setItem(slot, stack.toStack((int) extracted));
        } else {
            itemInSlot.grow((int) extracted);
        }
        return true;
    }

    @Nullable
    protected final AEKey getStackBySerial(long serial) {
        return updateHelper.getBySerial(serial);
    }

    public boolean isPowered() {
        return this.hasPower;
    }

    private IConfigManagerListener getGui() {
        return this.gui;
    }

    public void setGui(IConfigManagerListener gui) {
        this.gui = gui;
    }

    @Nullable
    public IClientRepo getClientRepo() {
        return clientRepo;
    }

    public void setClientRepo(@Nullable IClientRepo clientRepo) {
        this.clientRepo = clientRepo;
    }

    /**
     * Try to transfer an item stack into the grid.
     */
    @Override
    protected ItemStack transferStackToMenu(ItemStack input) {
        if (!canInteractWithGrid()) {
            // Allow non-grid slots to be use
            return super.transferStackToMenu(input);
        }

        var key = AEItemKey.of(input);
        if (key == null || !isKeyVisible(key)) {
            return input;
        }

        var inserted = StorageHelper.poweredInsert(powerSource, storage,
                key, input.getCount(),
                this.getActionSource());
        return Platform.getInsertionRemainder(input, inserted);
    }

    /**
     * Checks if the terminal has a given amount of the requested item. Used to determine for REI/JEI if a recipe is
     * potentially craftable based on the available items.
     * <p/>
     * This method is <strong>slow</strong>, but it is client-only and thus doesn't scale with the player count.
     */
    public boolean hasIngredient(Predicate<ItemStack> ingredient, int amount) {
        var clientRepo = getClientRepo();

        if (clientRepo != null) {
            for (var stack : clientRepo.getAllEntries()) {
                if (stack.getWhat() instanceof AEItemKey itemKey && ingredient.test(itemKey.toStack())) {
                    if (stack.getStoredAmount() >= amount) {
                        return true;
                    }
                    amount -= stack.getStoredAmount();
                }
            }
        }

        return false;
    }

    /**
     * @return The stacks available in the storage as determined the last time this menu was ticked.
     */
    protected final KeyCounter getPreviousAvailableStacks() {
        Preconditions.checkState(isServerSide());
        return previousAvailableStacks;
    }

    public boolean canConfigureTypeFilter() {
        return this.host.getConfigManager().hasSetting(Settings.TYPE_FILTER);
    }

    public ITerminalHost getHost() {
        return host;
    }
}
