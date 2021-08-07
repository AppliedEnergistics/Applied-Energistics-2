/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.parts.automation;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.capability.IFluidHandler;

import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.storage.IStackWatcher;
import appeng.api.networking.storage.IStackWatcherHost;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.IConfigManager;
import appeng.core.Api;
import appeng.core.AppEng;
import appeng.helpers.IConfigurableFluidInventory;
import appeng.items.parts.PartModels;
import appeng.menu.MenuLocator;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.FluidLevelEmitterMenu;
import appeng.parts.PartModel;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;
import appeng.util.fluid.AEFluidInventory;
import appeng.util.fluid.IAEFluidTank;
import appeng.util.inv.IAEFluidInventory;

public class FluidLevelEmitterPart extends UpgradeablePart
        implements IStackWatcherHost, IConfigManagerHost, IAEFluidInventory, IMEMonitorHandlerReceiver<IAEFluidStack>,
        IConfigurableFluidInventory {
    @PartModels
    public static final ResourceLocation MODEL_BASE_OFF = new ResourceLocation(AppEng.MOD_ID,
            "part/item_level_emitter_base_off");
    @PartModels
    public static final ResourceLocation MODEL_BASE_ON = new ResourceLocation(AppEng.MOD_ID,
            "part/item_level_emitter_base_on");
    @PartModels
    public static final ResourceLocation MODEL_STATUS_OFF = new ResourceLocation(AppEng.MOD_ID,
            "part/item_level_emitter_status_off");
    @PartModels
    public static final ResourceLocation MODEL_STATUS_ON = new ResourceLocation(AppEng.MOD_ID,
            "part/item_level_emitter_status_on");
    @PartModels
    public static final ResourceLocation MODEL_STATUS_HAS_CHANNEL = new ResourceLocation(AppEng.MOD_ID,
            "part/item_level_emitter_status_has_channel");

    public static final PartModel MODEL_OFF_OFF = new PartModel(MODEL_BASE_OFF, MODEL_STATUS_OFF);
    public static final PartModel MODEL_OFF_ON = new PartModel(MODEL_BASE_OFF, MODEL_STATUS_ON);
    public static final PartModel MODEL_OFF_HAS_CHANNEL = new PartModel(MODEL_BASE_OFF, MODEL_STATUS_HAS_CHANNEL);
    public static final PartModel MODEL_ON_OFF = new PartModel(MODEL_BASE_ON, MODEL_STATUS_OFF);
    public static final PartModel MODEL_ON_ON = new PartModel(MODEL_BASE_ON, MODEL_STATUS_ON);
    public static final PartModel MODEL_ON_HAS_CHANNEL = new PartModel(MODEL_BASE_ON, MODEL_STATUS_HAS_CHANNEL);

    private static final int FLAG_ON = 4;

    private boolean prevState = false;
    private long lastReportedValue = 0;
    private long reportingValue = 0;
    private IStackWatcher stackWatcher = null;
    private final AEFluidInventory config = new AEFluidInventory(this, 1);

    public FluidLevelEmitterPart(ItemStack is) {
        super(is);

        getMainNode().addService(IStackWatcherHost.class, this);

        this.getConfigManager().registerSetting(Settings.REDSTONE_EMITTER, RedstoneMode.HIGH_SIGNAL);
    }

    public long getReportingValue() {
        return this.reportingValue;
    }

    public void setReportingValue(final long v) {
        this.reportingValue = v;
        this.updateState();
    }

    @Override
    public void updateSetting(IConfigManager manager, Settings settingName, Enum<?> newValue) {
        this.configureWatchers();
    }

    @Override
    public void updateWatcher(IStackWatcher newWatcher) {
        this.stackWatcher = newWatcher;
        this.configureWatchers();
    }

    @Override
    public void onStackChange(IItemList<?> o, IAEStack<?> fullStack, IAEStack<?> diffStack, IActionSource src,
            IStorageChannel<?> chan) {
        if (chan == Api.instance().storage().getStorageChannel(IFluidStorageChannel.class)
                && fullStack.equals(this.config.getFluidInSlot(0))) {
            this.lastReportedValue = fullStack.getStackSize();
            this.updateState();
        }
    }

    @Override
    public void onFluidInventoryChanged(IAEFluidTank inv, int slot) {
        this.configureWatchers();
    }

    @Override
    protected void onMainNodeStateChanged(IGridNodeListener.State reason) {
        updateState();
    }

    @Override
    public int isProvidingStrongPower() {
        return this.prevState ? 15 : 0;
    }

    @Override
    public int isProvidingWeakPower() {
        return this.prevState ? 15 : 0;
    }

    @Override
    protected int calculateClientFlags() {
        return super.calculateClientFlags() | (this.prevState ? FLAG_ON : 0);
    }

    @Override
    public boolean isValid(Object effectiveGrid) {
        var grid = this.getMainNode().getGrid();
        return grid != null && grid == effectiveGrid;
    }

    @Override
    public void postChange(final IBaseMonitor<IAEFluidStack> monitor, final Iterable<IAEFluidStack> change,
            final IActionSource actionSource) {
        this.updateReportingValue((IMEMonitor<IAEFluidStack>) monitor);
    }

    @Override
    public void onListUpdate() {
        getMainNode().ifPresent(grid -> {
            var channel = Api.instance().storage().getStorageChannel(IFluidStorageChannel.class);
            this.updateReportingValue(grid.getStorageService().getInventory(channel));
        });
    }

    private void updateState() {
        final boolean isOn = this.isLevelEmitterOn();
        if (this.prevState != isOn) {
            this.getHost().markForUpdate();
            final BlockEntity te = this.getHost().getBlockEntity();
            this.prevState = isOn;
            Platform.notifyBlocksOfNeighbors(te.getLevel(), te.getBlockPos());
            Platform.notifyBlocksOfNeighbors(te.getLevel(), te.getBlockPos().relative(this.getSide().getDirection()));
        }
    }

    private void configureWatchers() {
        final IFluidStorageChannel channel = Api.instance().storage().getStorageChannel(IFluidStorageChannel.class);

        if (this.stackWatcher != null) {
            this.stackWatcher.reset();

            final IAEFluidStack myStack = this.config.getFluidInSlot(0);

            getMainNode().ifPresent(grid -> {
                if (myStack != null) {
                    grid.getStorageService().getInventory(channel).removeListener(this);
                    this.stackWatcher.add(myStack);
                } else {
                    grid.getStorageService().getInventory(channel).addListener(this, grid);
                }

                this.updateReportingValue(grid.getStorageService().getInventory(channel));
            });
        }
    }

    private void updateReportingValue(final IMEMonitor<IAEFluidStack> monitor) {
        final IAEFluidStack myStack = this.config.getFluidInSlot(0);

        if (myStack == null) {
            this.lastReportedValue = 0;
            for (final IAEFluidStack st : monitor.getStorageList()) {
                this.lastReportedValue += st.getStackSize();
            }
        } else {
            final IAEFluidStack r = monitor.getStorageList().findPrecise(myStack);
            if (r == null) {
                this.lastReportedValue = 0;
            } else {
                this.lastReportedValue = r.getStackSize();
            }
        }
        this.updateState();
    }

    private boolean isLevelEmitterOn() {
        if (isRemote()) {
            return (this.getClientFlags() & FLAG_ON) == FLAG_ON;
        }

        if (!this.getMainNode().isActive()) {
            return false;
        }

        final boolean flipState = this.getConfigManager()
                .getSetting(Settings.REDSTONE_EMITTER) == RedstoneMode.LOW_SIGNAL;
        return flipState ? this.reportingValue > this.lastReportedValue : this.reportingValue <= this.lastReportedValue;
    }

    @Override
    public AECableType getExternalCableConnectionType() {
        return AECableType.SMART;
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 16;
    }

    @Override
    public boolean canConnectRedstone() {
        return true;
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        bch.addBox(7, 7, 11, 9, 9, 16);
    }

    @Override
    public void animateTick(final Level level, final BlockPos pos, final Random r) {
        if (this.isLevelEmitterOn()) {
            final AEPartLocation d = this.getSide();

            final double d0 = d.xOffset * 0.45F + (r.nextFloat() - 0.5F) * 0.2D;
            final double d1 = d.yOffset * 0.45F + (r.nextFloat() - 0.5F) * 0.2D;
            final double d2 = d.zOffset * 0.45F + (r.nextFloat() - 0.5F) * 0.2D;

            level.addParticle(DustParticleOptions.REDSTONE, 0.5 + pos.getX() + d0, 0.5 + pos.getY() + d1,
                    0.5 + pos.getZ() + d2, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public boolean onPartActivate(final Player player, final InteractionHand hand, final Vec3 pos) {
        if (!isRemote()) {
            MenuOpener.open(FluidLevelEmitterMenu.TYPE, player, MenuLocator.forPart(this));
        }
        return true;
    }

    @Override
    public IPartModel getStaticModels() {
        if (this.isActive() && this.isPowered()) {
            return this.isLevelEmitterOn() ? MODEL_ON_HAS_CHANNEL : MODEL_OFF_HAS_CHANNEL;
        } else if (this.isPowered()) {
            return this.isLevelEmitterOn() ? MODEL_ON_ON : MODEL_OFF_ON;
        } else {
            return this.isLevelEmitterOn() ? MODEL_ON_OFF : MODEL_OFF_OFF;
        }
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

    @Override
    public void readFromNBT(final CompoundTag data) {
        super.readFromNBT(data);
        this.lastReportedValue = data.getLong("lastReportedValue");
        this.reportingValue = data.getLong("reportingValue");
        this.prevState = data.getBoolean("prevState");
        this.config.readFromNBT(data, "config");
    }

    @Override
    public void writeToNBT(final CompoundTag data) {
        super.writeToNBT(data);
        data.putLong("lastReportedValue", this.lastReportedValue);
        data.putLong("reportingValue", this.reportingValue);
        data.putBoolean("prevState", this.prevState);
        this.config.writeToNBT(data, "config");
    }

}
