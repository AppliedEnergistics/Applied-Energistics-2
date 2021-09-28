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

import java.util.Map;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.storage.data.IAEStack;
import appeng.core.AELog;
import appeng.helpers.iface.DualityPatternProvider;
import appeng.helpers.iface.GenericStackInv;
import appeng.helpers.iface.GenericStackSyncHelper;
import appeng.helpers.iface.IPatternProviderHost;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantic;
import appeng.menu.guisync.GuiSync;
import appeng.menu.slot.RestrictedInputSlot;

public class PatternProviderMenu extends AEBaseMenu implements IGenericSyncMenu {

    public static final MenuType<PatternProviderMenu> TYPE = MenuTypeBuilder
            .create(PatternProviderMenu::new, IPatternProviderHost.class)
            .requirePermission(SecurityPermissions.BUILD)
            .build("pattern_provider");

    private final DualityPatternProvider duality;
    private final GenericStackSyncHelper syncHelper;

    @GuiSync(3)
    public YesNo blockingMode = YesNo.NO;
    @GuiSync(4)
    public YesNo showInInterfaceTerminal = YesNo.YES;

    public PatternProviderMenu(int id, Inventory playerInventory, IPatternProviderHost host) {
        super(TYPE, id, playerInventory, host);

        this.createPlayerInventorySlots(playerInventory);

        this.duality = host.getDuality();
        this.syncHelper = new GenericStackSyncHelper(getReturnInv(), 0);

        for (int x = 0; x < DualityPatternProvider.NUMBER_OF_PATTERN_SLOTS; x++) {
            this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.ENCODED_PATTERN,
                    duality.getPatternInv(), x),
                    SlotSemantic.ENCODED_PATTERN);
        }
    }

    @Override
    public void broadcastChanges() {
        this.verifyPermissions(SecurityPermissions.BUILD, false);

        if (isServer()) {
            blockingMode = duality.getConfigManager().getSetting(Settings.BLOCKING_MODE);
            showInInterfaceTerminal = duality.getConfigManager().getSetting(Settings.INTERFACE_TERMINAL);

            this.syncHelper.sendDiff(getPlayer());
        }

        super.broadcastChanges();
    }

    @Override
    public void sendAllDataToRemote() {
        super.sendAllDataToRemote();
        this.syncHelper.sendFull(getPlayer());
    }

    @Override
    public void receiveGenericStacks(Map<Integer, IAEStack> stacks) {
        if (isClient()) {
            for (var entry : stacks.entrySet()) {
                getReturnInv().setStack(entry.getKey(), entry.getValue());
            }
        } else {
            AELog.warn("Client tried to override pattern provider return stacks!");
        }
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
