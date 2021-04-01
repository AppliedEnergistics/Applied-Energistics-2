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
import alexiil.mc.lib.attributes.item.FixedItemInv;

import appeng.api.definitions.IItemDefinition;
import appeng.container.ContainerLocator;
import appeng.container.guisync.GuiSync;
import appeng.container.interfaces.IProgressProvider;
import appeng.container.slot.OutputSlot;
import appeng.container.slot.RestrictedInputSlot;
import appeng.core.Api;
import appeng.tile.misc.InscriberBlockEntity;
import appeng.tile.misc.InscriberRecipes;

/**
 * @author AlgorithmX2
 * @author thatsIch
 * @version rv2
 * @since rv0
 */
public class InscriberContainer extends UpgradeableContainer implements IProgressProvider {

    public static ContainerType<InscriberContainer> TYPE;

    private static final ContainerHelper<InscriberContainer, InscriberBlockEntity> helper = new ContainerHelper<>(
            InscriberContainer::new, InscriberBlockEntity.class);

    public static InscriberContainer fromNetwork(int windowId, PlayerInventory inv, PacketBuffer buf) {
        return helper.fromNetwork(windowId, inv, buf);
    }

    public static boolean open(PlayerEntity player, ContainerLocator locator) {
        return helper.open(player, locator);
    }

    private final InscriberBlockEntity ti;

    private final Slot top;
    private final Slot middle;
    private final Slot bottom;

    @GuiSync(2)
    public int maxProcessingTime = -1;

    @GuiSync(3)
    public int processingTime = -1;

    public InscriberContainer(int id, final PlayerInventory ip, final InscriberBlockEntity te) {
        super(TYPE, id, ip, te);
        this.ti = te;

        FixedItemInv inv = te.getInternalInventory();

        RestrictedInputSlot top = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.INSCRIBER_PLATE, inv, 0,
                45, 16, this.getPlayerInventory());
        top.setStackLimit(1);
        this.top = this.addSlot(top);
        RestrictedInputSlot bottom = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.INSCRIBER_PLATE, inv,
                1, 45, 62, this.getPlayerInventory());
        bottom.setStackLimit(1);
        this.bottom = this.addSlot(bottom);
        RestrictedInputSlot middle = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.INSCRIBER_INPUT, inv,
                2, 63, 39, this.getPlayerInventory());
        middle.setStackLimit(1);
        this.middle = this.addSlot(middle);

        this.addSlot(new OutputSlot(inv, 3, 113, 40, -1));
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

        if (isServer()) {
            this.maxProcessingTime = this.ti.getMaxProcessingTime();
            this.processingTime = this.ti.getProcessingTime();
        }
    }

    @Override
    public boolean isValidForSlot(final Slot s, final ItemStack is) {
        final ItemStack top = this.ti.getInternalInventory().getInvStack(0);
        final ItemStack bot = this.ti.getInternalInventory().getInvStack(1);

        if (s == this.middle) {
            IItemDefinition press = Api.instance().definitions().materials().namePress();
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
            final IItemDefinition namePress = Api.instance().definitions().materials().namePress();
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
