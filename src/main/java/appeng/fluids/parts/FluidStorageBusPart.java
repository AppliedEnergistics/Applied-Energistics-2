/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

package appeng.fluids.parts;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import appeng.parts.misc.InterfacePart;
import appeng.tile.misc.InterfaceTileEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import appeng.api.config.AccessRestriction;
import appeng.api.config.FuzzyMode;
import appeng.api.config.IncludeExclude;
import appeng.api.config.Settings;
import appeng.api.config.StorageFilter;
import appeng.api.config.Upgrades;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.GridCellArrayUpdate;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.IStorageMonitorableAccessor;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AEPartLocation;
import appeng.capabilities.Capabilities;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.core.Api;
import appeng.core.AppEng;
import appeng.core.definitions.AEParts;
import appeng.core.settings.TickRates;
import appeng.fluids.container.FluidStorageBusContainer;
import appeng.fluids.helper.IConfigurableFluidInventory;
import appeng.fluids.util.AEFluidInventory;
import appeng.fluids.util.IAEFluidInventory;
import appeng.fluids.util.IAEFluidTank;
import appeng.helpers.IInterfaceHost;
import appeng.items.parts.PartModels;
import appeng.me.helpers.MachineSource;
import appeng.me.storage.ITickingMonitor;
import appeng.me.storage.MEInventoryHandler;
import appeng.parts.PartModel;
import appeng.parts.misc.SharedStorageBusPart;
import appeng.util.Platform;
import appeng.util.prioritylist.FuzzyPriorityList;
import appeng.util.prioritylist.PrecisePriorityList;

/**
 * @author BrockWS
 * @version rv6 - 22/05/2018
 * @since rv6 22/05/2018
 */
public class FluidStorageBusPart extends SharedStorageBusPart
        implements IMEMonitorHandlerReceiver<IAEFluidStack>, IAEFluidInventory, IConfigurableFluidInventory {
    public static final ResourceLocation MODEL_BASE = new ResourceLocation(AppEng.MOD_ID,
            "part/fluid_storage_bus_base");
    @PartModels
    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/fluid_storage_bus_off"));
    @PartModels
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/fluid_storage_bus_on"));
    @PartModels
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/fluid_storage_bus_has_channel"));

    private final IActionSource source;
    private final AEFluidInventory config = new AEFluidInventory(this, 63);
    private boolean cached = false;
    private ITickingMonitor monitor = null;
    private MEInventoryHandler<IAEFluidStack> handler = null;
    private int handlerHash = 0;
    private byte resetCacheLogic = 0;

    public FluidStorageBusPart(ItemStack is) {
        super(is);
        this.getConfigManager().registerSetting(Settings.ACCESS, AccessRestriction.READ_WRITE);
        this.getConfigManager().registerSetting(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL);
        this.getConfigManager().registerSetting(Settings.STORAGE_FILTER, StorageFilter.EXTRACTABLE_ONLY);
        this.source = new MachineSource(this);
    }

    private IMEInventory<IAEFluidStack> getInventoryWrapper(BlockEntity target) {
        Direction targetSide = this.getSide().getDirection().getOpposite();
        // Prioritize a handler to directly link to another ME network
        IStorageMonitorableAccessor accessor = target
                .getCapability(Capabilities.STORAGE_MONITORABLE_ACCESSOR, targetSide).orElse(null);
        if (accessor != null) {
            IStorageMonitorable inventory = accessor.getInventory(this.source);
            if (inventory != null) {
                return inventory.getInventory(Api.instance().storage().getStorageChannel(IFluidStorageChannel.class));
            }

            // So this could / can be a design decision. If the tile does support our custom
            // capability,
            // but it does not return an inventory for the action source, we do NOT fall
            // back to using
            // IItemHandler's, as that might circumvent the security setings, and might also
            // cause
            // performance issues.
            return null;
        }

        // Check via cap for IItemHandler
        IFluidHandler handlerExt = target.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, targetSide)
                .orElse(null);
        if (handlerExt != null) {
            return new FluidHandlerAdapter(handlerExt, () -> {
                getMainNode().ifPresent((grid, node) -> {
                    grid.getTickManager().alertDevice(node);
                });
            });
        }

        return null;
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (this.resetCacheLogic != 0) {
            this.resetCache();
        }

        if (this.monitor != null) {
            return this.monitor.onTick();
        }

        return TickRateModulation.SLEEP;
    }

    @Override
    protected void resetCache() {
        final boolean fullReset = this.resetCacheLogic == 2;
        this.resetCacheLogic = 0;

        final IMEInventory<IAEFluidStack> in = this.getInternalHandler();
        IItemList<IAEFluidStack> before = Api.instance().storage().getStorageChannel(IFluidStorageChannel.class)
                .createList();
        if (in != null) {
            before = in.getAvailableItems(before);
        }

        this.cached = false;
        if (fullReset) {
            this.handlerHash = 0;
        }

        final IMEInventory<IAEFluidStack> out = this.getInternalHandler();

        if (in != out) {
            IItemList<IAEFluidStack> after = Api.instance().storage().getStorageChannel(IFluidStorageChannel.class)
                    .createList();
            if (out != null) {
                after = out.getAvailableItems(after);
            }
            Platform.postListChanges(before, after, this, this.source);
        }
    }

    @Override
    protected void resetCache(final boolean fullReset) {
        if (isRemote()) {
            return;
        }

        if (fullReset) {
            this.resetCacheLogic = 2;
        } else {
            this.resetCacheLogic = 1;
        }

        getMainNode().ifPresent((grid, node) -> {
            grid.getTickManager().alertDevice(node);
        });
    }

    @Override
    public boolean onPartActivate(final Player player, final InteractionHand hand, final Vec3 pos) {
        if (!isRemote()) {
            ContainerOpener.openContainer(FluidStorageBusContainer.TYPE, player, ContainerLocator.forPart(this));
        }
        return true;
    }

    @Override
    public void onFluidInventoryChanged(IAEFluidTank inv, int slot) {
        if (inv == this.config) {
            this.resetCache(true);
        }
    }

    @Override
    public void readFromNBT(final CompoundTag data) {
        super.readFromNBT(data);
        this.config.readFromNBT(data, "config");
    }

    @Override
    public void writeToNBT(final CompoundTag data) {
        super.writeToNBT(data);
        this.config.writeToNBT(data, "config");
    }

    @Override
    public boolean isValid(final Object verificationToken) {
        return this.handler == verificationToken;
    }

    @Override
    public void postChange(final IBaseMonitor<IAEFluidStack> monitor, final Iterable<IAEFluidStack> change,
            final IActionSource source) {
        if (this.getMainNode().isActive()) {
            getMainNode().ifPresent(grid -> {
                grid.getStorageService().postAlterationOfStoredItems(
                        Api.instance().storage().getStorageChannel(IFluidStorageChannel.class), change, this.source);
            });
        }
    }

    public MEInventoryHandler<IAEFluidStack> getInternalHandler() {
        if (this.cached) {
            return this.handler;
        }

        final boolean wasSleeping = this.monitor == null;

        this.cached = true;
        final BlockEntity self = this.getHost().getTile();
        final BlockEntity target = self.getLevel().getBlockEntity(self.getBlockPos().relative(this.getSide().getDirection()));
        final int newHandlerHash = this.createHandlerHash(target);

        if (newHandlerHash != 0 && newHandlerHash == this.handlerHash) {
            return this.handler;
        }

        this.handlerHash = newHandlerHash;
        this.handler = null;
        this.monitor = null;
        if (target != null) {
            IMEInventory<IAEFluidStack> inv = this.getInventoryWrapper(target);
            if (inv instanceof ITickingMonitor) {
                this.monitor = (ITickingMonitor) inv;
                this.monitor.setActionSource(new MachineSource(this));
            }

            if (inv != null) {
                this.checkInterfaceVsStorageBus(target, this.getSide().getOpposite());

                this.handler = new MEInventoryHandler<>(inv,
                        Api.instance().storage().getStorageChannel(IFluidStorageChannel.class));

                this.handler.setBaseAccess((AccessRestriction) this.getConfigManager().getSetting(Settings.ACCESS));
                this.handler.setWhitelist(this.getInstalledUpgrades(Upgrades.INVERTER) > 0 ? IncludeExclude.BLACKLIST
                        : IncludeExclude.WHITELIST);
                this.handler.setPriority(this.getPriority());

                final IItemList<IAEFluidStack> priorityList = Api.instance().storage()
                        .getStorageChannel(IFluidStorageChannel.class).createList();

                final int slotsToUse = 18 + this.getInstalledUpgrades(Upgrades.CAPACITY) * 9;
                for (int x = 0; x < this.config.getSlots() && x < slotsToUse; x++) {
                    final IAEFluidStack is = this.config.getFluidInSlot(x);
                    if (is != null) {
                        priorityList.add(is);
                    }
                }

                if (this.getInstalledUpgrades(Upgrades.FUZZY) > 0) {
                    this.handler.setPartitionList(new FuzzyPriorityList<IAEFluidStack>(priorityList,
                            (FuzzyMode) this.getConfigManager().getSetting(Settings.FUZZY_MODE)));
                } else {
                    this.handler.setPartitionList(new PrecisePriorityList<IAEFluidStack>(priorityList));
                }

                if (inv instanceof IBaseMonitor) {
                    ((IBaseMonitor<IAEFluidStack>) inv).addListener(this, this.handler);
                }
            }
        }

        // update sleep state...
        if (wasSleeping != (this.monitor == null)) {
            getMainNode().ifPresent((grid, node) -> {
                var tm = grid.getTickManager();
                if (this.monitor == null) {
                    tm.sleepDevice(node);
                } else {
                    tm.wakeDevice(node);
                }
            });
        }

        // force grid to update handlers...
        getMainNode().ifPresent(grid -> grid.postEvent(new GridCellArrayUpdate()));

        return this.handler;
    }

    private void checkInterfaceVsStorageBus(final BlockEntity target, final AEPartLocation side) {
        IGridNode targetNode = null;

        if (target instanceof InterfaceTileEntity interfaceTileEntity) {
            targetNode = interfaceTileEntity.getMainNode().getNode();
        } else if (target instanceof IPartHost) {
            final Object part = ((IPartHost) target).getPart(side);
            if (part instanceof InterfacePart interfacePart) {
                targetNode = interfacePart.getMainNode().getNode();
            }
        }

        if (targetNode != null) {
            // Platform.addStat( achievement.getActionableNode().getPlayerID(),
            // Achievements.Recursive.getAchievement()
            // );
            // Platform.addStat( getActionableNode().getPlayerID(),
            // Achievements.Recursive.getAchievement() );
        }
    }

    @Override
    public List<IMEInventoryHandler> getCellArray(final IStorageChannel channel) {
        if (channel == this.getStorageChannel()) {
            final IMEInventoryHandler<IAEFluidStack> out = this.getMainNode().isActive() ? this.getInternalHandler()
                    : null;
            if (out != null) {
                return Collections.singletonList(out);
            }
        }
        return Collections.emptyList();
    }

    private int createHandlerHash(BlockEntity target) {
        if (target == null) {
            return 0;
        }

        final Direction targetSide = this.getSide().getDirection().getOpposite();

        LazyOptional<IStorageMonitorableAccessor> accessorOpt = target
                .getCapability(Capabilities.STORAGE_MONITORABLE_ACCESSOR, targetSide);
        if (accessorOpt.isPresent()) {
            return Objects.hash(target, accessorOpt.orElse(null));
        }

        final IFluidHandler fluidHandler = target
                .getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, targetSide).orElse(null);

        if (fluidHandler != null) {
            return Objects.hash(target, fluidHandler, fluidHandler.getTanks());
        }

        return 0;
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(TickRates.FluidStorageBus.getMin(), TickRates.FluidStorageBus.getMax(),
                this.isSleeping(), true);
    }

    @Override
    public void onListUpdate() {
        // not used here.
    }

    @Override
    public IStorageChannel getStorageChannel() {
        return Api.instance().storage().getStorageChannel(IFluidStorageChannel.class);
    }

    public IAEFluidTank getConfig() {
        return this.config;
    }

    @Override
    public IFluidHandler getFluidInventoryByName(final String name) {
        if (name.equals("config")) {
            return this.config;
        }
        return null;
    }

    @Nonnull
    @Override
    public IPartModel getStaticModels() {
        if (this.isActive() && this.isPowered()) {
            return MODELS_HAS_CHANNEL;
        } else if (this.isPowered()) {
            return MODELS_ON;
        } else {
            return MODELS_OFF;
        }
    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return AEParts.FLUID_STORAGE_BUS.stack();
    }

    @Override
    public MenuType<?> getContainerType() {
        return FluidStorageBusContainer.TYPE;
    }
}
