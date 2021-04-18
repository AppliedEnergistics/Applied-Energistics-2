package appeng.client.gui.implementations;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

import appeng.api.config.FuzzyMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.SchedulingMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.container.implementations.IOBusContainer;
import appeng.parts.automation.ExportBusPart;

public class IOBusScreen extends UpgradeableScreen<IOBusContainer> {

    private SettingToggleButton<RedstoneMode> redstoneMode;
    private SettingToggleButton<FuzzyMode> fuzzyMode;
    private SettingToggleButton<YesNo> craftMode;
    private SettingToggleButton<SchedulingMode> schedulingMode;

    public IOBusScreen(IOBusContainer container, PlayerInventory playerInventory, ITextComponent title,
            ScreenStyle style) {
        super(container, playerInventory, title, style);
    }

    @Override
    public void init() {
        super.init();

        this.redstoneMode = new ServerSettingToggleButton<>(0, 0, Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE);
        addToLeftToolbar(this.redstoneMode);
        this.fuzzyMode = new ServerSettingToggleButton<>(0, 0, Settings.FUZZY_MODE,
                FuzzyMode.IGNORE_ALL);
        addToLeftToolbar(this.fuzzyMode);

        // Craft & Scheduling mode is only supported by export bus
        if (container.getUpgradeable() instanceof ExportBusPart) {
            this.craftMode = new ServerSettingToggleButton<>(0, 0, Settings.CRAFT_ONLY,
                    YesNo.NO);
            addToLeftToolbar(this.craftMode);

            this.schedulingMode = new ServerSettingToggleButton<>(0, 0,
                    Settings.SCHEDULING_MODE, SchedulingMode.DEFAULT);
            addToLeftToolbar(this.schedulingMode);
        }
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        this.redstoneMode.set(container.getRedStoneMode());
        this.redstoneMode.setVisibility(container.hasUpgrade(Upgrades.REDSTONE));
        this.fuzzyMode.set(container.getFuzzyMode());
        this.fuzzyMode.setVisibility(container.hasUpgrade(Upgrades.FUZZY));
        if (this.craftMode != null) {
            this.craftMode.set(container.getCraftingMode());
            this.craftMode.setVisibility(container.hasUpgrade(Upgrades.CRAFTING));
        }
        if (this.schedulingMode != null) {
            this.schedulingMode.set(container.getSchedulingMode());
            this.schedulingMode.setVisibility(container.hasUpgrade(Upgrades.CAPACITY));
        }
    }

}
