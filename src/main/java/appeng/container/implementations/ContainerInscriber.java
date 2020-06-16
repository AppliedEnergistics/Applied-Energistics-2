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

package appeng.container.implementations;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.IItemHandler;

import appeng.api.AEApi;
import appeng.api.definitions.IItemDefinition;
import appeng.container.ContainerLocator;
import appeng.container.guisync.GuiSync;
import appeng.container.implementations.ContainerHelper;
import appeng.container.interfaces.IProgressProvider;
import appeng.container.slot.SlotOutput;
import appeng.container.slot.SlotRestrictedInput;
import appeng.tile.misc.InscriberRecipes;
import appeng.tile.misc.TileInscriber;
import appeng.util.Platform;

/**
 * @author AlgorithmX2
 * @author thatsIch
 * @version rv2
 * @since rv0
 */
public class ContainerInscriber extends ContainerUpgradeable implements IProgressProvider {

    public static ContainerType<ContainerInscriber> TYPE;

    private static final ContainerHelper<ContainerInscriber, TileInscriber> helper = new ContainerHelper<>(
            ContainerInscriber::new, TileInscriber.class);

    public static ContainerInscriber fromNetwork(int windowId, PlayerInventory inv, PacketBuffer buf) {
        return helper.fromNetwork(windowId, inv, buf);
    }

    public static boolean open(PlayerEntity player, ContainerLocator locator) {
        return helper.open(player, locator);
    }

    private final TileInscriber ti;

    private final Slot top;
    private final Slot middle;
    private final Slot bottom;

    @GuiSync(2)
    public int maxProcessingTime = -1;

    @GuiSync(3)
    public int processingTime = -1;

    public ContainerInscriber(int id, final PlayerInventory ip, final TileInscriber te) {
        super(TYPE, id, ip, te);
        this.ti = te;

        IItemHandler inv = te.getInternalInventory();

        SlotRestrictedInput top = new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.INSCRIBER_PLATE, inv, 0,
                45, 16, this.getPlayerInventory());
        top.setStackLimit(1);
        this.top = this.addSlot(top);
        SlotRestrictedInput bottom = new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.INSCRIBER_PLATE, inv,
                1, 45, 62, this.getPlayerInventory());
        bottom.setStackLimit(1);
        this.bottom = this.addSlot(bottom);
        SlotRestrictedInput middle = new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.INSCRIBER_INPUT, inv,
                2, 63, 39, this.getPlayerInventory());
        middle.setStackLimit(1);
        this.middle = this.addSlot(middle);

        this.addSlot(new SlotOutput(inv, 3, 113, 40, -1));
    }

    @Override
    protected int getHeight() {
        return 176;
    }

    @Override
    /**
     * Overridden super.setupConfig to prevent setting up the fake slots
     */
    protected void setupConfig() {
        this.setupUpgrades();
    }

    @Override
    protected boolean supportCapacity() {
        return false;
    }

    @Override
    public int availableUpgrades() {
        return 3;
    }

    @Override
    public void detectAndSendChanges() {
        this.standardDetectAndSendChanges();

        if (Platform.isServer()) {
            this.maxProcessingTime = this.ti.getMaxProcessingTime();
            this.processingTime = this.ti.getProcessingTime();
        }
    }

    @Override
    public boolean isValidForSlot(final Slot s, final ItemStack is) {
        final ItemStack top = this.ti.getInternalInventory().getStackInSlot(0);
        final ItemStack bot = this.ti.getInternalInventory().getStackInSlot(1);

        if (s == this.middle) {
            IItemDefinition press = AEApi.instance().definitions().materials().namePress();
            if (press.isSameAs(top) || press.isSameAs(bot)) {
                return !press.isSameAs(is);
            }

            return InscriberRecipes.findRecipe(ti.getWorld(), is, top, bot, false) != null;
        } else if ((s == this.top && !bot.isEmpty()) || (s == this.bottom && !top.isEmpty())) {
            ItemStack otherSlot;
            if (s == this.top) {
                otherSlot = this.bottom.getStack();
            } else {
                otherSlot = this.top.getStack();
            }

            // name presses
            final IItemDefinition namePress = AEApi.instance().definitions().materials().namePress();
            if (namePress.isSameAs(otherSlot)) {
                return namePress.isSameAs(is);
            }

            // everything else
            // test for a partial recipe match (ignoring the middle slot)
            return InscriberRecipes.isValidOptionalIngredientCombination(ti.getWorld(), is, otherSlot);
        }
        return true;
    }

    @Override
    public int getCurrentProgress() {
        return this.processingTime;
    }

    @Override
    public int getMaxProgress() {
        return this.maxProcessingTime;
    }
}
