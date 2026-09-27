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

import appeng.api.util.KeyTypeSelection;
import appeng.api.util.KeyTypeSelectionHost;
import appeng.core.definitions.AEItems;
import appeng.menu.guisync.GuiSync;
import appeng.menu.interfaces.KeyTypeSelectionMenu;
import appeng.parts.automation.ExportBusPart;
import appeng.parts.automation.IOBusPart;
import appeng.parts.automation.ImportBusPart;

/**
 * Used for {@link ImportBusPart}, {@link ExportBusPart}.
 *
 * @see appeng.client.gui.implementations.IOBusScreen
 */
public class IOBusMenu extends UpgradeableMenu<IOBusPart> implements KeyTypeSelectionMenu {

    public static final MenuType<IOBusMenu> EXPORT_TYPE = MenuTypeBuilder
            .create(IOBusMenu::new, ExportBusPart.class)
            .build("export_bus");

    public static final MenuType<IOBusMenu> IMPORT_TYPE = MenuTypeBuilder
            .create(IOBusMenu::new, ImportBusPart.class)
            .build("import_bus");

    @GuiSync(20)
    public SyncedKeyTypes importKeyTypes = new SyncedKeyTypes();

    public IOBusMenu(MenuType<?> menuType, int id, Inventory ip, IOBusPart host) {
        super(menuType, id, ip, host);
    }

    @Override
    protected void setupConfig() {
        addExpandableConfigSlots(getHost().getConfig(), 2, 9, 5);
    }

    @Override
    public boolean isSlotEnabled(int idx) {
        final int upgrades = getUpgrades().getInstalledUpgrades(AEItems.CAPACITY_CARD);

        return upgrades > idx;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        if (isServerSide()) {
            if (getHost() instanceof KeyTypeSelectionHost selectionHost) {
                importKeyTypes = new SyncedKeyTypes(selectionHost.getKeyTypeSelection().enabled());
            }
        }
    }

    @Override
    public KeyTypeSelection getServerKeyTypeSelection() {
        return ((KeyTypeSelectionHost) getHost()).getKeyTypeSelection();
    }

    @Override
    public SyncedKeyTypes getClientKeyTypeSelection() {
        return importKeyTypes;
    }
}
