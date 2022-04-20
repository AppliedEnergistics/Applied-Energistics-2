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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.behaviors.StackImportStrategy;
import appeng.api.networking.IGrid;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.core.definitions.AEItems;
import appeng.core.settings.TickRates;
import appeng.menu.implementations.IOBusMenu;

public class ImportBusPart extends IOBusPart {
    private StackImportStrategy importStrategy;

    private BlockPos fromPos;
    private Direction fromSide;

    private boolean inverted = false;

    public ImportBusPart(IPartItem<?> partItem) {
        super(TickRates.ImportBus, partItem);
    }

    @Override
    public void upgradesChanged() {
        super.upgradesChanged();

        boolean wasInverted = inverted;
        inverted = isUpgradedWith(AEItems.INVERTER_CARD);
        if (wasInverted != inverted) {
            importStrategy = StackWorldBehaviors.createImportFacade((ServerLevel) getLevel(), fromPos, fromSide,
                    this.inverted);
        }
    }

    @Override
    protected boolean doBusWork(IGrid grid) {
        if (importStrategy == null) {
            BlockEntity self = this.getHost().getBlockEntity();
            fromPos = self.getBlockPos().relative(this.getSide());
            fromSide = getSide().getOpposite();
            importStrategy = StackWorldBehaviors.createImportFacade((ServerLevel) getLevel(), fromPos, fromSide,
                    this.inverted);
        }

        var context = new StackTransferContextImpl(
                grid.getStorageService(),
                grid.getEnergyService(),
                this.source,
                getOperationsPerTick(),
                getFilter());

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
}
