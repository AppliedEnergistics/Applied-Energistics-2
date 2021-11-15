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

package appeng.menu.implementations;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.helpers.iface.DualityPatternProvider;
import appeng.helpers.iface.GenericStackInv;
import appeng.helpers.iface.IPatternProviderHost;
import appeng.helpers.iface.PatternProviderReturnInventory;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantic;
import appeng.menu.guisync.GuiSync;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.slot.RestrictedInputSlot;

/**
 * @see appeng.client.gui.implementations.PatternProviderScreen
 */
public class PatternProviderMenu extends AEBaseMenu {

    public static final MenuType<PatternProviderMenu> TYPE = MenuTypeBuilder
            .create(PatternProviderMenu::new, IPatternProviderHost.class)
            .requirePermission(SecurityPermissions.BUILD)
            .build("pattern_provider");

    private final DualityPatternProvider duality;

    @GuiSync(3)
    public YesNo blockingMode = YesNo.NO;
    @GuiSync(4)
    public YesNo showInInterfaceTerminal = YesNo.YES;

    public PatternProviderMenu(int id, Inventory playerInventory, IPatternProviderHost host) {
        super(TYPE, id, playerInventory, host);

        this.createPlayerInventorySlots(playerInventory);

        this.duality = host.getDuality();

        for (int x = 0; x < DualityPatternProvider.NUMBER_OF_PATTERN_SLOTS; x++) {
            this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.ENCODED_PATTERN,
                    duality.getPatternInv(), x),
                    SlotSemantic.ENCODED_PATTERN);
        }

        // Show first few entries of the return inv
        var returnInv = duality.getReturnInv().createMenuWrapper();
        for (int i = 0; i < PatternProviderReturnInventory.NUMBER_OF_SLOTS; i++) {
            if (i < returnInv.size()) {
                this.addSlot(new AppEngSlot(returnInv, i), SlotSemantic.STORAGE);
            }
        }

    }

    @Override
    public void broadcastChanges() {
        this.verifyPermissions(SecurityPermissions.BUILD, false);

        if (isServer()) {
            blockingMode = duality.getConfigManager().getSetting(Settings.BLOCKING_MODE);
            showInInterfaceTerminal = duality.getConfigManager().getSetting(Settings.INTERFACE_TERMINAL);
        }

        super.broadcastChanges();
    }

    public GenericStackInv getReturnInv() {
        return duality.getReturnInv();
    }

    public YesNo getBlockingMode() {
        return blockingMode;
    }

    public YesNo getShowInInterfaceTerminal() {
        return showInInterfaceTerminal;
    }
}
