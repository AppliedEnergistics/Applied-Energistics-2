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

package appeng.parts.misc;

import java.util.EnumSet;
import java.util.List;

import appeng.api.networking.IGridNodeListener;
import appeng.me.helpers.ManagedGridNode;
import appeng.parts.AEBasePart;
import com.google.common.collect.ImmutableSet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.Actionable;
import appeng.api.config.Upgrades;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigManager;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.container.implementations.InterfaceContainer;
import appeng.core.AppEng;
import appeng.core.definitions.AEParts;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.helpers.IPriorityHost;
import appeng.items.parts.PartModels;
import appeng.parts.BasicStatePart;
import appeng.parts.PartModel;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.IInventoryDestination;
import appeng.util.inv.InvOperation;

public class InterfacePart extends BasicStatePart implements IStorageMonitorable, IInventoryDestination,
        IInterfaceHost, IAEAppEngInventory, IPriorityHost {

    public static final ResourceLocation MODEL_BASE = new ResourceLocation(AppEng.MOD_ID, "part/item_interface_base");

    private static final IGridNodeListener<InterfacePart> NODE_LISTENER = new AEBasePart.NodeListener<>() {
        @Override
        public void onGridChanged(InterfacePart nodeOwner, IGridNode node) {
            super.onGridChanged(nodeOwner, node);
            nodeOwner.getInterfaceDuality().gridChanged();
        }
    };

    @PartModels
    public static final PartModel MODELS_OFF = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/item_interface_off"));

    @PartModels
    public static final PartModel MODELS_ON = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/item_interface_on"));

    @PartModels
    public static final PartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/item_interface_has_channel"));

    private final DualityInterface duality;

    public InterfacePart(final ItemStack is) {
        super(is);
        this.duality = new DualityInterface(this.getMainNode(), this, is);
    }

    @Override
    public void saveChanges() {
        getHost().markForSave();
    }

    @Override
    protected ManagedGridNode createMainNode() {
        return new ManagedGridNode(this, NODE_LISTENER);
    }

    @Override
    protected void onMainNodeStateChanged(IGridNodeListener.ActiveChangeReason reason) {
        super.onMainNodeStateChanged(reason);
        this.duality.notifyNeighbors();
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        bch.addBox(2, 2, 14, 14, 14, 16);
        bch.addBox(5, 5, 12, 11, 11, 14);
    }

    @Override
    public int getInstalledUpgrades(final Upgrades u) {
        return this.duality.getInstalledUpgrades(u);
    }

    @Override
    public void readFromNBT(final CompoundNBT data) {
        super.readFromNBT(data);
        this.duality.readFromNBT(data);
    }

    @Override
    public void writeToNBT(final CompoundNBT data) {
        super.writeToNBT(data);
        this.duality.writeToNBT(data);
    }

    @Override
    public void addToWorld() {
        super.addToWorld();
        this.duality.initialize();
    }

    @Override
    public void getDrops(final List<ItemStack> drops, final boolean wrenched) {
        this.duality.addDrops(drops);
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 4;
    }

    @Override
    public IConfigManager getConfigManager() {
        return this.duality.getConfigManager();
    }

    @Override
    public IItemHandler getInventoryByName(final String name) {
        return this.duality.getInventoryByName(name);
    }

    @Override
    public boolean onPartActivate(final PlayerEntity p, final Hand hand, final Vector3d pos) {
        if (!p.getEntityWorld().isRemote()) {
            ContainerOpener.openContainer(InterfaceContainer.TYPE, p, ContainerLocator.forPart(this));
        }
        return true;
    }

    @Override
    public boolean canInsert(final ItemStack stack) {
        return this.duality.canInsert(stack);
    }

    @Override
    public <T extends IAEStack<T>> IMEMonitor<T> getInventory(IStorageChannel<T> channel) {
        return this.duality.getInventory(channel);
    }

    @Override
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc,
            final ItemStack removedStack, final ItemStack newStack) {
        this.duality.onChangeInventory(inv, slot, mc, removedStack, newStack);
    }

    @Override
    public DualityInterface getInterfaceDuality() {
        return this.duality;
    }

    @Override
    public EnumSet<Direction> getTargets() {
        return EnumSet.of(this.getSide().getDirection());
    }

    @Override
    public TileEntity getTileEntity() {
        return super.getHost().getTile();
    }

    @Override
    public boolean pushPattern(final ICraftingPatternDetails patternDetails, final CraftingInventory table) {
        return this.duality.pushPattern(patternDetails, table);
    }

    @Override
    public boolean isBusy() {
        return this.duality.isBusy();
    }

    @Override
    public void provideCrafting(final ICraftingProviderHelper craftingTracker) {
        this.duality.provideCrafting(craftingTracker);
    }

    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        return this.duality.getRequestedJobs();
    }

    @Override
    public IAEItemStack injectCraftedItems(final ICraftingLink link, final IAEItemStack items, final Actionable mode) {
        return this.duality.injectCraftedItems(link, items, mode);
    }

    @Override
    public void jobStateChange(final ICraftingLink link) {
        this.duality.jobStateChange(link);
    }

    @Override
    public int getPriority() {
        return this.duality.getPriority();
    }

    @Override
    public void setPriority(final int newValue) {
        this.duality.setPriority(newValue);
    }

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
    public <T> LazyOptional<T> getCapability(Capability<T> capabilityClass) {
        return this.duality.getCapability(capabilityClass, this.getSide().getDirection());
    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return AEParts.INTERFACE.stack();
    }

    @Override
    public ContainerType<?> getContainerType() {
        return InterfaceContainer.TYPE;
    }
}
