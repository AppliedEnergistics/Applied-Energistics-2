package appeng.client.gui.widgets;

import appeng.api.config.PowerUnits;
import appeng.api.config.Settings;
import appeng.core.AEConfig;

public final class CommonButtons {

    private CommonButtons() {
    }

    public static SettingToggleButton<PowerUnits> togglePowerUnit(int x, int y) {
        return new SettingToggleButton<>(x, y, Settings.POWER_UNITS, AEConfig.instance().getSelectedPowerUnit(),
                CommonButtons::togglePowerUnit);
    }

    private static void togglePowerUnit(SettingToggleButton<PowerUnits> button, boolean backwards) {
        AEConfig.instance().nextPowerUnit(backwards);
        button.set(AEConfig.instance().getSelectedPowerUnit());
    }

}
