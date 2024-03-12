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

package appeng.parts.automation;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.MenuType;

import appeng.api.behaviors.StackImportStrategy;
import appeng.api.networking.IGrid;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.util.KeyTypeSelection;
import appeng.api.util.KeyTypeSelectionHost;
import appeng.core.definitions.AEItems;
import appeng.core.settings.TickRates;
import appeng.menu.implementations.IOBusMenu;

public class ImportBusPart extends IOBusPart implements KeyTypeSelectionHost {
    @Nullable
    private StackImportStrategy importStrategy;
    private final KeyTypeSelection keyTypeSelection;

    public ImportBusPart(IPartItem<?> partItem) {
        super(TickRates.ImportBus, StackWorldBehaviors.withImportStrategy(), partItem);

        this.keyTypeSelection = new KeyTypeSelection(() -> {
            getHost().markForSave();
            // Reset strategies
            importStrategy = null;
            // We can potentially wake up now
            getMainNode().ifPresent((grid, node) -> grid.getTickManager().alertDevice(node));
        }, StackWorldBehaviors.hasImportStrategyTypeFilter());
    }

    @Override
    protected boolean doBusWork(IGrid grid) {
        if (importStrategy == null) {
            var self = this.getHost().getBlockEntity();
            var fromPos = self.getBlockPos().relative(this.getSide());
            var fromSide = getSide().getOpposite();
            importStrategy = StackWorldBehaviors.createImportFacade((ServerLevel) getLevel(), fromPos, fromSide,
                    keyTypeSelection.enabledPredicate());
        }

        var context = new StackTransferContextImpl(
                grid.getStorageService(),
                grid.getEnergyService(),
                this.source,
                getOperationsPerTick(),
                getFilter());

        context.setInverted(this.isUpgradedWith(AEItems.INVERTER_CARD));
        importStrategy.transfer(context);

        return context.hasDoneWork();
    }

    @Override
    protected MenuType<?> getMenuType() {
        return IOBusMenu.IMPORT_TYPE;
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        bch.addBox(6, 6, 11, 10, 10, 13);
        bch.addBox(5, 5, 13, 11, 11, 14);
        bch.addBox(4, 4, 14, 12, 12, 16);
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
    public void readFromNBT(CompoundTag extra) {
        super.readFromNBT(extra);
        keyTypeSelection.readFromNBT(extra);
    }

    @Override
    public void writeToNBT(CompoundTag extra) {
        super.writeToNBT(extra);
        keyTypeSelection.writeToNBT(extra);
    }

    @Override
    public KeyTypeSelection getKeyTypeSelection() {
        return keyTypeSelection;
    }
}
