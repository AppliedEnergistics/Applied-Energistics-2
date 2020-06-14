package appeng.client.gui.widgets;

import appeng.api.config.FuzzyMode;
import appeng.api.config.PowerUnits;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.core.AEConfig;

public final class CommonButtons {

    private CommonButtons() {
    }

    public static GuiSettingToggleButton<PowerUnits> togglePowerUnit(int x, int y) {
        return new GuiSettingToggleButton<>( x, y, Settings.POWER_UNITS, AEConfig.instance().selectedPowerUnit(), CommonButtons::togglePowerUnit );
    }

    private static void togglePowerUnit(GuiSettingToggleButton<PowerUnits> button, boolean backwards) {
        AEConfig.instance().nextPowerUnit( backwards );
        button.set( AEConfig.instance().selectedPowerUnit() );
    }

}
