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

package appeng.init.internal;

import appeng.api.features.IChargerRegistry;
import appeng.core.Api;
import appeng.core.api.definitions.ApiBlocks;
import appeng.core.api.definitions.ApiItems;

public class InitChargerRates {

    public static void init() {
        // Charge Rates
        IChargerRegistry charger = Api.instance().registries().charger();
        charger.addChargeRate(ApiItems.CHARGED_STAFF, 320d);
        charger.addChargeRate(ApiItems.PORTABLE_CELL1K, 800d);
        charger.addChargeRate(ApiItems.PORTABLE_CELL4k, 800d);
        charger.addChargeRate(ApiItems.PORTABLE_CELL16K, 800d);
        charger.addChargeRate(ApiItems.PORTABLE_CELL64K, 800d);
        charger.addChargeRate(ApiItems.COLOR_APPLICATOR, 800d);
        charger.addChargeRate(ApiItems.WIRELESS_TERMINAL, 8000d);
        charger.addChargeRate(ApiItems.ENTROPY_MANIPULATOR, 8000d);
        charger.addChargeRate(ApiItems.MASS_CANNON, 8000d);
        charger.addChargeRate(ApiBlocks.ENERGY_CELL, 8000d);
        charger.addChargeRate(ApiBlocks.DENSE_ENERGY_CELL, 16000d);
    }

}
