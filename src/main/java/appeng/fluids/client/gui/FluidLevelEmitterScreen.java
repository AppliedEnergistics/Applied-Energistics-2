
package appeng.fluids.client.gui;

import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.widgets.NumberBox;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;
import appeng.fluids.client.gui.widgets.FluidSlotWidget;
import appeng.fluids.container.FluidLevelEmitterContainer;

public class FluidLevelEmitterScreen extends UpgradeableScreen<FluidLevelEmitterContainer> {
    private NumberBox level;

    public FluidLevelEmitterScreen(FluidLevelEmitterContainer container, PlayerInventory playerInventory,
            ITextComponent title) {
        super(container, playerInventory, title);
    }

    @Override
    public void init() {
        super.init();

        this.level = new NumberBox(this.font, this.guiLeft + 24, this.guiTop + 43, 79, this.font.FONT_HEIGHT,
                Long.class, value -> NetworkHandler.instance().sendToServer(new ConfigValuePacket("FluidLevelEmitter.Value", String.valueOf(value))));
        this.level.setEnableBackgroundDrawing(false);
        this.level.setMaxStringLength(16);
        this.level.setTextColor(0xFFFFFF);
        this.level.setVisible(true);
        this.level.changeFocus(true);
        container.setTextField(this.level);

        final int y = 40;
        final int x = 80 + 44;
        this.guiSlots.add(new FluidSlotWidget(this.container.getFluidConfigInventory(), 0, 0, x, y));
    }

    @Override
    protected void addButtons() {
        this.redstoneMode = new ServerSettingToggleButton<>(this.guiLeft - 18, this.guiTop + 28,
                Settings.REDSTONE_EMITTER, RedstoneMode.LOW_SIGNAL);

        final int a = AEConfig.instance().levelByMillyBuckets(0);
        final int b = AEConfig.instance().levelByMillyBuckets(1);
        final int c = AEConfig.instance().levelByMillyBuckets(2);
        final int d = AEConfig.instance().levelByMillyBuckets(3);

        this.addButton(new Button(this.guiLeft + 20, this.guiTop + 17, 22, 20, "+" + a, btn -> addQty(a)));
        this.addButton(new Button(this.guiLeft + 48, this.guiTop + 17, 28, 20, "+" + b, btn -> addQty(b)));
        this.addButton(new Button(this.guiLeft + 82, this.guiTop + 17, 32, 20, "+" + c, btn -> addQty(c)));
        this.addButton(new Button(this.guiLeft + 120, this.guiTop + 17, 38, 20, "+" + d, btn -> addQty(d)));

        this.addButton(new Button(this.guiLeft + 20, this.guiTop + 59, 22, 20, "-" + a, btn -> addQty(-a)));
        this.addButton(new Button(this.guiLeft + 48, this.guiTop + 59, 28, 20, "-" + b, btn -> addQty(-b)));
        this.addButton(new Button(this.guiLeft + 82, this.guiTop + 59, 32, 20, "-" + c, btn -> addQty(-c)));
        this.addButton(new Button(this.guiLeft + 120, this.guiTop + 59, 38, 20, "-" + d, btn -> addQty(-d)));

        this.addButton(this.redstoneMode);
    }

    @Override
    public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY, float partialTicks) {
        super.drawBG(offsetX, offsetY, mouseX, mouseY, partialTicks);
        this.level.render(mouseX, mouseY, partialTicks);
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

    private void addQty(final long i) {
        try {
            String Out = this.level.getText();

            boolean Fixed = false;
            while (Out.startsWith("0") && Out.length() > 1) {
                Out = Out.substring(1);
                Fixed = true;
            }

            if (Fixed) {
                this.level.setText(Out);
            }

            if (Out.isEmpty()) {
                Out = "0";
            }

            long result = Long.parseLong(Out);
            result += i;
            if (result < 0) {
                result = 0;
            }

            this.level.setText(Out = Long.toString(result));

            NetworkHandler.instance().sendToServer(new ConfigValuePacket("FluidLevelEmitter.Value", Out));
        } catch (final NumberFormatException e) {
            // nope..
            this.level.setText("0");
        }
    }

    @Override
    public boolean charTyped(char character, int keyCode) {
        if (level.charTyped(character, keyCode)) {
            String Out = this.level.getText();

            boolean Fixed = false;
            while (Out.startsWith("0") && Out.length() > 1) {
                Out = Out.substring(1);
                Fixed = true;
            }

            if (Fixed) {
                this.level.setText(Out);
            }

            if (Out.isEmpty()) {
                Out = "0";
            }

            NetworkHandler.instance().sendToServer(new ConfigValuePacket("FluidLevelEmitter.Value", Out));
            return true;
        }
        return super.charTyped(character, keyCode);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int p_keyPressed_3_) {
        if (!this.checkHotbarKeys(InputMappings.getInputByCode(keyCode, scanCode))) {
            if ((keyCode == 211 || keyCode == 205 || keyCode == 203 || keyCode == 14)
                    && this.level.keyPressed(keyCode, scanCode, p_keyPressed_3_)) {
                String Out = this.level.getText();

                boolean Fixed = false;
                while (Out.startsWith("0") && Out.length() > 1) {
                    Out = Out.substring(1);
                    Fixed = true;
                }

                if (Fixed) {
                    this.level.setText(Out);
                }

                if (Out.isEmpty()) {
                    Out = "0";
                }

                NetworkHandler.instance().sendToServer(new ConfigValuePacket("FluidLevelEmitter.Value", Out));
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, p_keyPressed_3_);
    }
}
