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

package appeng.client.gui.widgets;

import appeng.api.config.PowerUnits;
import appeng.api.config.Settings;
import appeng.core.AEConfig;

public final class CommonButtons {

    private CommonButtons() {
    }

    public static SettingToggleButton<PowerUnits> togglePowerUnit() {
        return new SettingToggleButton<>(Settings.POWER_UNITS, AEConfig.instance().getSelectedPowerUnit(),
                CommonButtons::togglePowerUnit);
    }

    private static void togglePowerUnit(SettingToggleButton<PowerUnits> button, boolean backwards) {
        AEConfig.instance().nextPowerUnit(backwards);
        button.set(AEConfig.instance().getSelectedPowerUnit());
    }

}
