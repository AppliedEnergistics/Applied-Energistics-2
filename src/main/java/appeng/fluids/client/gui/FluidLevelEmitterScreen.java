
package appeng.fluids.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.client.gui.NumberEntryType;
import appeng.client.gui.implementations.NumberEntryWidget;
import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.core.localization.GuiText;
import appeng.fluids.client.gui.widgets.FluidSlotWidget;
import appeng.fluids.container.FluidLevelEmitterContainer;

public class FluidLevelEmitterScreen extends UpgradeableScreen<FluidLevelEmitterContainer> {

    private NumberEntryWidget level;

    public FluidLevelEmitterScreen(FluidLevelEmitterContainer container, PlayerInventory playerInventory,
            ITextComponent title) {
        super(container, playerInventory, title);
    }

    @Override
    public void init() {
        super.init();

        this.level = new NumberEntryWidget(this, 20, 17, 138, 62, NumberEntryType.LEVEL_FLUID_VOLUME);
        this.level.setTextFieldBounds(25, 44, 75);
        this.level.addButtons(children::add, this::addButton);
        this.level.setValue(menu.getReportingValue());
        this.level.setOnChange(this::saveReportingValue);
        this.level.setOnConfirm(this::onClose);

        this.changeFocus(true);

        final int y = 40;
        final int x = 80 + 57;
        this.guiSlots.add(new FluidSlotWidget(this.menu.getFluidConfigInventory(), 0, 0, x, y));
    }

    private void saveReportingValue() {
        this.level.getLongValue().ifPresent(menu::setReportingValue);
    }

    @Override
    protected void addButtons() {
        this.redstoneMode = new ServerSettingToggleButton<>(this.leftPos - 18, this.topPos + 28,
                Settings.REDSTONE_EMITTER, RedstoneMode.LOW_SIGNAL);
        this.addButton(this.redstoneMode);
    }

    @Override
    public void drawBG(MatrixStack matrixStack, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY, float partialTicks) {
        super.drawBG(matrixStack, offsetX, offsetY, mouseX, mouseY, partialTicks);
        this.level.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void drawFG(MatrixStack matrixStack, int offsetX, int offsetY, int mouseX, int mouseY) {
        super.drawFG(matrixStack, offsetX, offsetY, mouseX, mouseY);

        this.font.draw(matrixStack, GuiText.FluidLevelEmitterUnit.getLocal(), 110, 44, COLOR_DARK_GRAY);
    }

    @Override
    protected boolean drawUpgrades() {
        return false;
    }

    @Override
    protected String getBackground() {
        return "guis/lvlemitter.png";
    }

    @Override
    protected GuiText getName() {
        return GuiText.FluidLevelEmitter;
    }

    @Override
    protected void handleButtonVisibility() {
    }

}
