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
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.SecurityPermissions;
import appeng.client.gui.implementations.ItemIOBusScreen;
import appeng.menu.SlotSemantic;
import appeng.menu.slot.FakeTypeOnlySlot;
import appeng.menu.slot.OptionalTypeOnlyFakeSlot;
import appeng.parts.automation.ExportBusPart;
import appeng.parts.automation.ImportBusPart;
import appeng.parts.automation.SharedItemBusPart;

/**
 * Used for {@link ImportBusPart} and {@link ExportBusPart}
 *
 * @see ItemIOBusScreen
 */
public class ItemIOBusContainer extends UpgradeableContainer {

    public static final MenuType<ItemIOBusContainer> EXPORT_TYPE = ContainerTypeBuilder
            .create(ItemIOBusContainer::new, ExportBusPart.class)
            .requirePermission(SecurityPermissions.BUILD)
            .build("export_bus");

    public static final MenuType<ItemIOBusContainer> IMPORT_TYPE = ContainerTypeBuilder
            .create(ItemIOBusContainer::new, ImportBusPart.class)
            .requirePermission(SecurityPermissions.BUILD)
            .build("import_bus");

    public ItemIOBusContainer(MenuType<?> containerType, int id, Inventory ip, SharedItemBusPart te) {
        super(containerType, id, ip, te);
    }

    @Override
    protected void setupConfig() {
        this.setupUpgrades();

        final IItemHandler inv = this.getUpgradeable().getInventoryByName("config");
        final SlotSemantic s = SlotSemantic.CONFIG;

        this.addSlot(new FakeTypeOnlySlot(inv, 0), s);

        // Slots that become available with 1 capacity card
        this.addSlot(new OptionalTypeOnlyFakeSlot(inv, this, 1, 1), s);
        this.addSlot(new OptionalTypeOnlyFakeSlot(inv, this, 2, 1), s);
        this.addSlot(new OptionalTypeOnlyFakeSlot(inv, this, 3, 1), s);
        this.addSlot(new OptionalTypeOnlyFakeSlot(inv, this, 4, 1), s);

        // Slots that become available with 2 capacity cards
        this.addSlot(new OptionalTypeOnlyFakeSlot(inv, this, 5, 2), s);
        this.addSlot(new OptionalTypeOnlyFakeSlot(inv, this, 6, 2), s);
        this.addSlot(new OptionalTypeOnlyFakeSlot(inv, this, 7, 2), s);
        this.addSlot(new OptionalTypeOnlyFakeSlot(inv, this, 8, 2), s);
    }

}
