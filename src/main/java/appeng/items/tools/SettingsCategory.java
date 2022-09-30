package appeng.items.tools;

import appeng.core.localization.GuiText;
import net.minecraft.network.chat.Component;

/**
 * Settings that the memory card can copy generically from machine to machine.
 */
public enum SettingsCategory {
    UPGRADES(GuiText.RestoredGenericSettingUpgrades.text()),
    SETTINGS(GuiText.RestoredGenericSettingSettings.text()),
    CONFIG_INV(GuiText.RestoredGenericSettingConfigInv.text()),
    PRIORITY(GuiText.RestoredGenericSettingPriority.text()),
    ;

    private final Component label;

    SettingsCategory(Component label) {
        this.label = label;
    }

    public Component getLabel() {
        return label;
    }
}
