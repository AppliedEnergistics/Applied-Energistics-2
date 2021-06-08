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

package appeng.parts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import com.google.common.base.Preconditions;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import alexiil.mc.lib.attributes.fluid.FixedFluidInv;
import alexiil.mc.lib.attributes.item.FixedItemInv;

import appeng.api.config.Upgrades;
import appeng.api.definitions.IDefinitions;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.parts.BusSupport;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.PartItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IConfigManager;
import appeng.core.Api;
import appeng.fluids.helper.IConfigurableFluidInventory;
import appeng.fluids.parts.FluidLevelEmitterPart;
import appeng.fluids.util.AEFluidInventory;
import appeng.helpers.ICustomNameObject;
import appeng.helpers.IPriorityHost;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.parts.automation.LevelEmitterPart;
import appeng.parts.networking.CablePart;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.util.InteractionUtil;
import appeng.util.Platform;
import appeng.util.SettingsFrom;

public abstract class AEBasePart implements IPart, IGridProxyable, IActionHost, IUpgradeableHost, ICustomNameObject {

    private final AENetworkProxy proxy;
    private final ItemStack is;
    private TileEntity tile = null;
    private IPartHost host = null;
    private AEPartLocation side = null;

    public AEBasePart(final ItemStack is) {
        Preconditions.checkNotNull(is);

        this.is = is;
        this.proxy = new AENetworkProxy(this, "part", is, this instanceof CablePart);
        this.proxy.setValidSides(EnumSet.noneOf(Direction.class));
    }

    public final boolean isRemote() {
        return this.tile == null
                || this.tile.getWorld() == null
                || this.tile.getWorld().isRemote();
    }

    public IPartHost getHost() {
        return this.host;
    }

    @Override
    public IGridNode getGridNode(final AEPartLocation dir) {
        return this.proxy.getNode();
    }

    @Override
    public AECableType getCableConnectionType(final AEPartLocation dir) {
        return AECableType.GLASS;
    }

    @Override
    public void securityBreak() {
        if (this.getItemStack().getCount() > 0 && this.getGridNode() != null) {
            final List<ItemStack> items = new ArrayList<>();
            items.add(this.is.copy());
            this.host.removePart(this.side, false);
            Platform.spawnDrops(this.tile.getWorld(), this.tile.getPos(), items);
            this.is.setCount(0);
        }
    }

    protected AEColor getColor() {
        if (this.host == null) {
            return AEColor.TRANSPARENT;
        }
        return this.host.getColor();
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {

    }

    @Override
    public int getInstalledUpgrades(final Upgrades u) {
        return 0;
    }

    @Override
    public TileEntity getTile() {
        return this.tile;
    }

    @Override
    public AENetworkProxy getProxy() {
        return this.proxy;
    }

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this.tile);
    }

    @Override
    public void gridChanged() {

    }

    @Override
    public IGridNode getActionableNode() {
        return this.proxy.getNode();
    }

    @Override
    public void saveChanges() {
        this.host.markForSave();
    }

    @Override
    public ITextComponent getCustomInventoryName() {
        return this.getItemStack().getDisplayName();
    }

    @Override
    public boolean hasCustomInventoryName() {
        return this.getItemStack().hasDisplayName();
    }

    @Override
    public void addEntityCrashInfo(final CrashReportCategory crashreportcategory) {
        crashreportcategory.addDetail("Part Side", this.getSide());
    }

    @Override
    public ItemStack getItemStack(final PartItemStack type) {
        if (type == PartItemStack.NETWORK) {
            final ItemStack copy = this.is.copy();
            copy.setTag(null);
            return copy;
        }
        return this.is;
    }

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public void onNeighborChanged(IBlockReader w, BlockPos pos, BlockPos neighbor) {

    }

    @Override
    public boolean canConnectRedstone() {
        return false;
    }

    @Override
    public void readFromNBT(final CompoundNBT data) {
        this.proxy.readFromNBT(data);
    }

    @Override
    public void writeToNBT(final CompoundNBT data) {
        this.proxy.writeToNBT(data);
    }

    @Override
    public int isProvidingStrongPower() {
        return 0;
    }

    @Override
    public int isProvidingWeakPower() {
        return 0;
    }

    @Override
    public void writeToStream(final PacketBuffer data) throws IOException {

    }

    @Override
    public boolean readFromStream(final PacketBuffer data) throws IOException {
        return false;
    }

    @Override
    public IGridNode getGridNode() {
        return this.proxy.getNode();
    }

    @Override
    public void onEntityCollision(final Entity entity) {

    }

    @Override
    public void removeFromWorld() {
        this.proxy.remove();
    }

    @Override
    public void addToWorld() {
        this.proxy.onReady();
    }

    @Override
    public void setPartHostInfo(final AEPartLocation side, final IPartHost host, final TileEntity tile) {
        this.setSide(side);
        this.tile = tile;
        this.host = host;
    }

    @Override
    public IGridNode getExternalFacingNode() {
        return null;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void animateTick(final World world, final BlockPos pos, final Random r) {

    }

    @Override
    public int getLightLevel() {
        return 0;
    }

    @Override
    public void getDrops(final List<ItemStack> drops, final boolean wrenched) {

    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 3;
    }

    @Override
    public boolean isLadder(final LivingEntity entity) {
        return false;
    }

    @Override
    public IConfigManager getConfigManager() {
        return null;
    }

    @Override
    public FixedItemInv getInventoryByName(final String name) {
        return null;
    }

    /**
     * depending on the from, different settings will be accepted, don't call this with null
     *
     * @param from     source of settings
     * @param compound compound of source
     */
    private void uploadSettings(final SettingsFrom from, final CompoundNBT compound) {
        final IConfigManager cm = this.getConfigManager();
        if (cm != null) {
            cm.readFromNBT(compound);
        }

        if (this instanceof IPriorityHost) {
            final IPriorityHost pHost = (IPriorityHost) this;
            pHost.setPriority(compound.getInt("priority"));
        }

        final FixedItemInv inv = this.getInventoryByName("config");
        if (inv instanceof AppEngInternalAEInventory) {
            final AppEngInternalAEInventory target = (AppEngInternalAEInventory) inv;
            final AppEngInternalAEInventory tmp = new AppEngInternalAEInventory(null, target.getSlotCount());
            tmp.readFromNBT(compound, "config");
            for (int x = 0; x < tmp.getSlotCount(); x++) {
                target.forceSetInvStack(x, tmp.getInvStack(x));
            }
            if (this instanceof LevelEmitterPart) {
                final LevelEmitterPart partLevelEmitter = (LevelEmitterPart) this;
                partLevelEmitter.setReportingValue(compound.getLong("reportingValue"));
            }
        }

        if (this instanceof IConfigurableFluidInventory) {
            final FixedFluidInv tank = ((IConfigurableFluidInventory) this).getFluidInventoryByName("config");
            if (tank instanceof AEFluidInventory) {
                final AEFluidInventory target = (AEFluidInventory) tank;
                final AEFluidInventory tmp = new AEFluidInventory(null, target.getSlots());
                tmp.readFromNBT(compound, "config");
                for (int x = 0; x < tmp.getSlots(); x++) {
                    target.setFluidInSlot(x, tmp.getFluidInSlot(x));
                }
            }
            if (this instanceof FluidLevelEmitterPart) {
                final FluidLevelEmitterPart fluidLevelEmitterPart = (FluidLevelEmitterPart) this;
                fluidLevelEmitterPart.setReportingValue(compound.getLong("reportingValue"));
            }
        }
    }

    /**
     * null means nothing to store...
     *
     * @param from source of settings
     * @return compound of source
     */
    private CompoundNBT downloadSettings(final SettingsFrom from) {
        final CompoundNBT output = new CompoundNBT();

        final IConfigManager cm = this.getConfigManager();
        if (cm != null) {
            cm.writeToNBT(output);
        }

        if (this instanceof IPriorityHost) {
            final IPriorityHost pHost = (IPriorityHost) this;
            output.putInt("priority", pHost.getPriority());
        }

        final FixedItemInv inv = this.getInventoryByName("config");
        if (inv instanceof AppEngInternalAEInventory) {
            ((AppEngInternalAEInventory) inv).writeToNBT(output, "config");
            if (this instanceof LevelEmitterPart) {
                final LevelEmitterPart partLevelEmitter = (LevelEmitterPart) this;
                output.putLong("reportingValue", partLevelEmitter.getReportingValue());
            }
        }

        if (this instanceof IConfigurableFluidInventory) {
            final FixedFluidInv tank = ((IConfigurableFluidInventory) this).getFluidInventoryByName("config");
            ((AEFluidInventory) tank).writeToNBT(output, "config");
            if (this instanceof FluidLevelEmitterPart) {
                final FluidLevelEmitterPart fluidLevelEmitterPart = (FluidLevelEmitterPart) this;
                output.putLong("reportingValue", fluidLevelEmitterPart.getReportingValue());
            }
        }
        return output.isEmpty() ? null : output;
    }

    public boolean useStandardMemoryCard() {
        return true;
    }

    private boolean useMemoryCard(final PlayerEntity player) {
        final ItemStack memCardIS = player.inventory.getCurrentItem();

        if (!memCardIS.isEmpty() && this.useStandardMemoryCard() && memCardIS.getItem() instanceof IMemoryCard) {
            final IMemoryCard memoryCard = (IMemoryCard) memCardIS.getItem();

            ItemStack is = this.getItemStack(PartItemStack.NETWORK);

            // Blocks and parts share the same soul!
            final IDefinitions definitions = Api.instance().definitions();
            if (definitions.parts().iface().isSameAs(is)) {
                Optional<ItemStack> iface = definitions.blocks().iface().maybeStack(1);
                if (iface.isPresent()) {
                    is = iface.get();
                }
            }

            final String name = is.getTranslationKey();

            if (InteractionUtil.isInAlternateUseMode(player)) {
                final CompoundNBT data = this.downloadSettings(SettingsFrom.MEMORY_CARD);
                if (data != null) {
                    memoryCard.setMemoryCardContents(memCardIS, name, data);
                    memoryCard.notifyUser(player, MemoryCardMessages.SETTINGS_SAVED);
                }
            } else {
                final String storedName = memoryCard.getSettingsName(memCardIS);
                final CompoundNBT data = memoryCard.getData(memCardIS);
                if (name.equals(storedName)) {
                    this.uploadSettings(SettingsFrom.MEMORY_CARD, data);
                    memoryCard.notifyUser(player, MemoryCardMessages.SETTINGS_LOADED);
                } else {
                    memoryCard.notifyUser(player, MemoryCardMessages.INVALID_MACHINE);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public final boolean onActivate(final PlayerEntity player, final Hand hand, final Vector3d pos) {
        if (this.useMemoryCard(player)) {
            return true;
        }

        return this.onPartActivate(player, hand, pos);
    }

    @Override
    public final boolean onShiftActivate(final PlayerEntity player, final Hand hand, final Vector3d pos) {
        if (this.useMemoryCard(player)) {
            return true;
        }

        return this.onPartShiftActivate(player, hand, pos);
    }

    public boolean onPartActivate(final PlayerEntity player, final Hand hand, final Vector3d pos) {
        return false;
    }

    public boolean onPartShiftActivate(final PlayerEntity player, final Hand hand, final Vector3d pos) {
        return false;
    }

    @Override
    public void onPlacement(final PlayerEntity player, final Hand hand, final ItemStack held,
            final AEPartLocation side) {
        this.proxy.setOwner(player);
    }

    @Override
    public boolean canBePlacedOn(final BusSupport what) {
        return what == BusSupport.CABLE;
    }

    @Override
    public boolean requireDynamicRender() {
        return false;
    }

    public AEPartLocation getSide() {
        return this.side;
    }

    private void setSide(final AEPartLocation side) {
        this.side = side;
    }

    public ItemStack getItemStack() {
        return this.is;
    }
}
