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

package appeng.fluids.container;


import appeng.api.config.SecurityPermissions;
import appeng.api.config.Upgrades;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.IConfigManager;
import appeng.container.guisync.GuiSync;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketTargetFluidStack;
import appeng.fluids.helper.DualityFluidInterface;
import appeng.fluids.helper.FluidSyncHelper;
import appeng.fluids.helper.IFluidInterfaceHost;
import appeng.fluids.util.AEFluidInventory;
import appeng.fluids.util.AEFluidStack;
import appeng.fluids.util.IAEFluidInventory;
import appeng.fluids.util.IAEFluidTank;
import appeng.helpers.InventoryAction;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;


public class ContainerFluidInterface extends ContainerFluidConfigurable implements IConfigManagerHost {
    private final DualityFluidInterface myDuality;
    private final FluidSyncHelper tankSync;
    private IConfigManagerHost gui;
    // Holds the fluid the client wishes to extract, or null for insert
    private IAEFluidStack clientRequestedTargetFluid = null;

    @GuiSync(7)
    public int capacityUpgrades = 0;

    public ContainerFluidInterface(final InventoryPlayer ip, final IFluidInterfaceHost te) {
        super(ip, te.getDualityFluidInterface().getHost());

        this.myDuality = te.getDualityFluidInterface();
        this.tankSync = new FluidSyncHelper(this.myDuality.getTanks(), DualityFluidInterface.NUMBER_OF_TANKS);
    }

    @Override
    protected int getHeight() {
        return 231;
    }

    @Override
    public IAEFluidTank getFluidConfigInventory() {
        return this.myDuality.getConfig();
    }

    @Override
    public void updateSetting(IConfigManager manager, Enum settingName, Enum newValue) {
        if (this.getGui() != null) {
            this.getGui().updateSetting(manager, settingName, newValue);
        }
    }

    @Override
    public void detectAndSendChanges() {
        this.verifyPermissions(SecurityPermissions.BUILD, false);

        if (Platform.isServer()) {
            this.tankSync.sendDiff(this.listeners);
            if (capacityUpgrades != getUpgradeable().getInstalledUpgrades(Upgrades.CAPACITY)) {
                capacityUpgrades = getUpgradeable().getInstalledUpgrades(Upgrades.CAPACITY);
            }
        }

        super.detectAndSendChanges();
    }

    @Override
    public void onUpdate(final String field, final Object oldValue, final Object newValue) {
        super.onUpdate(field, oldValue, newValue);
        if (Platform.isClient() && field.equals("capacityUpgrades")) {
            this.capacityUpgrades = (int) newValue;
            ((AEFluidInventory) this.myDuality.getTanks()).setCapacity((int) (Math.pow(4, this.capacityUpgrades + 1) * Fluid.BUCKET_VOLUME));
        }
    }

    @Override
    protected void setupConfig() {
        this.setupUpgrades();
    }

    @Override
    protected void loadSettingsFromHost(final IConfigManager cm) {
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        this.tankSync.sendFull(Collections.singleton(listener));
    }

    @Override
    public void receiveFluidSlots(Map<Integer, IAEFluidStack> fluids) {
        super.receiveFluidSlots(fluids);
        this.tankSync.readPacket(fluids);
    }

    private IConfigManagerHost getGui() {
        return this.gui;
    }

    public void setGui(@Nonnull final IConfigManagerHost gui) {
        this.gui = gui;
    }

    public void doAction(EntityPlayerMP player, InventoryAction action, int slot, long id) {
        if (action != InventoryAction.FILL_ITEM && action != InventoryAction.EMPTY_ITEM) {
            super.doAction(player, action, slot, id);
            return;
        }

        final ItemStack held = player.inventory.getItemStack();
        ItemStack heldCopy = held.copy();
        heldCopy.setCount(1);
        IFluidHandlerItem fh = FluidUtil.getFluidHandler(heldCopy);
        if (fh == null) {
            // only fluid handlers items
            return;
        }

        if (action == InventoryAction.FILL_ITEM && this.clientRequestedTargetFluid != null) {
            final IAEFluidStack stack = this.clientRequestedTargetFluid.copy();

            // Check how much we can store in the item
            stack.setStackSize(Integer.MAX_VALUE);
            int amountAllowed = fh.fill(stack.getFluidStack(), false);
            int heldAmount = held.getCount();
            for (int i = 0; i < heldAmount; i++) {
                ItemStack copiedFluidContainer = held.copy();
                copiedFluidContainer.setCount(1);
                fh = FluidUtil.getFluidHandler(copiedFluidContainer);

                FluidStack extractableFluid = this.myDuality.getTanks().drain(stack.setStackSize(amountAllowed).getFluidStack(), false);
                if (extractableFluid == null || extractableFluid.amount == 0) {
                    break;
                }

                int fillableAmount = fh.fill(extractableFluid, false);
                if (fillableAmount > 0) {
                    FluidStack extractedFluid = this.myDuality.getTanks().drain(extractableFluid, true);
                    fh.fill(extractedFluid, true);
                }

                if (held.getCount() == 1) {
                    player.inventory.setItemStack(fh.getContainer());
                } else {
                    player.inventory.getItemStack().shrink(1);
                    if (!player.inventory.addItemStackToInventory(fh.getContainer())) {
                        player.dropItem(fh.getContainer(), false);
                    }
                }
            }
        } else if (action == InventoryAction.EMPTY_ITEM) {
            int heldAmount = held.getCount();
            for (int i = 0; i < heldAmount; i++) {
                ItemStack copiedFluidContainer = held.copy();
                copiedFluidContainer.setCount(1);
                fh = FluidUtil.getFluidHandler(copiedFluidContainer);

                FluidStack drainable = fh.drain(this.myDuality.getTanks().getTankProperties()[slot].getCapacity(), false);
                if (drainable != null) {
                    fh.drain(drainable, true);
                    this.myDuality.getTanks().fill(drainable, true);
                }

                if (held.getCount() == 1) {
                    player.inventory.setItemStack(fh.getContainer());
                } else {
                    player.inventory.getItemStack().shrink(1);
                    if (!player.inventory.addItemStackToInventory(fh.getContainer())) {
                        player.dropItem(fh.getContainer(), false);
                    }
                }
            }
        }
        this.updateHeld(player);
    }

    public void setTargetStack(final IAEFluidStack stack) {
        if (Platform.isClient()) {
            if (stack == null && this.clientRequestedTargetFluid == null) {
                return;
            }
            if (stack != null && this.clientRequestedTargetFluid != null && stack.getFluidStack().isFluidEqual(this.clientRequestedTargetFluid.getFluidStack())) {
                return;
            }
            NetworkHandler.instance().sendToServer(new PacketTargetFluidStack((AEFluidStack) stack));
        }

        this.clientRequestedTargetFluid = stack == null ? null : stack.copy();
    }

    @Override
    protected boolean supportCapacity() {
        return false;
    }

    @Override
    public int availableUpgrades() {
        return 2;
    }
}
