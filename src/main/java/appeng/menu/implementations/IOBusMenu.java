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
import appeng.client.gui.implementations.IOBusScreen;
import appeng.menu.SlotSemantics;
import appeng.menu.slot.FakeSlot;
import appeng.menu.slot.OptionalFakeSlot;
import appeng.parts.automation.ExportBusPart;
import appeng.parts.automation.IOBusPart;
import appeng.parts.automation.ImportBusPart;

/**
 * Used for {@link ImportBusPart}, {@link appeng.parts.automation.ExportBusPart}.
 *
 * @see IOBusScreen
 */
public class IOBusMenu extends UpgradeableMenu<IOBusPart> {

    public static final MenuType<IOBusMenu> EXPORT_TYPE = MenuTypeBuilder
            .create(IOBusMenu::new, ExportBusPart.class)
            .requirePermission(SecurityPermissions.BUILD)
            .build("export_bus");

    public static final MenuType<IOBusMenu> IMPORT_TYPE = MenuTypeBuilder
            .create(IOBusMenu::new, ImportBusPart.class)
            .requirePermission(SecurityPermissions.BUILD)
            .build("import_bus");

    public IOBusMenu(MenuType<?> menuType, int id, Inventory ip, IOBusPart host) {
        super(menuType, id, ip, host);
    }

    @Override
    protected void setupConfig() {
        this.setupUpgrades();

        var inv = this.getHost().getConfig().createMenuWrapper();
        var s = SlotSemantics.CONFIG;
        this.addSlot(new FakeSlot(inv, 0), s);

        // Slots that become available with 1 capacity card
        this.addSlot(new OptionalFakeSlot(inv, this, 1, 1), s);
        this.addSlot(new OptionalFakeSlot(inv, this, 2, 1), s);
        this.addSlot(new OptionalFakeSlot(inv, this, 3, 1), s);
        this.addSlot(new OptionalFakeSlot(inv, this, 4, 1), s);

        // Slots that become available with 2 capacity cards
        this.addSlot(new OptionalFakeSlot(inv, this, 5, 2), s);
        this.addSlot(new OptionalFakeSlot(inv, this, 6, 2), s);
        this.addSlot(new OptionalFakeSlot(inv, this, 7, 2), s);
        this.addSlot(new OptionalFakeSlot(inv, this, 8, 2), s);
    }

}
