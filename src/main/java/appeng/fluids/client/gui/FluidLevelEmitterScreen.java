
package appeng.fluids.client.gui;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

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
            Text title) {
        super(container, playerInventory, title);
    }

    @Override
    public void init() {
        super.init();

        this.level = new NumberBox(this.textRenderer, this.x + 24, this.y + 43, 79, this.textRenderer.fontHeight,
                Long.class);
        this.level.setHasBorder(false);
        this.level.setMaxLength(16);
        this.level.setEditableColor(0xFFFFFF);
        this.level.setVisible(true);
        this.level.changeFocus(true);
        handler.setTextField(this.level);

        final int y = 40;
        final int x = 80 + 44;
        this.guiSlots.add(new FluidSlotWidget(this.handler.getFluidConfigInventory(), 0, 0, x, y));
    }

    @Override
    protected void addButtons() {
        this.redstoneMode = new ServerSettingToggleButton<>(this.x - 18, this.y + 28,
                Settings.REDSTONE_EMITTER, RedstoneMode.LOW_SIGNAL);

        final int a = AEConfig.instance().levelByMillyBuckets(0);
        final int b = AEConfig.instance().levelByMillyBuckets(1);
        final int c = AEConfig.instance().levelByMillyBuckets(2);
        final int d = AEConfig.instance().levelByMillyBuckets(3);

        this.addButton(new ButtonWidget(this.x + 20, this.y + 17, 22, 20, new LiteralText("+" + a), btn -> addQty(a)));
        this.addButton(new ButtonWidget(this.x + 48, this.y + 17, 28, 20, new LiteralText("+" + b), btn -> addQty(b)));
        this.addButton(new ButtonWidget(this.x + 82, this.y + 17, 32, 20, new LiteralText("+" + c), btn -> addQty(c)));
        this.addButton(new ButtonWidget(this.x + 120, this.y + 17, 38, 20, new LiteralText("+" + d), btn -> addQty(d)));

        this.addButton(new ButtonWidget(this.x + 20, this.y + 59, 22, 20, new LiteralText("-" + a), btn -> addQty(-a)));
        this.addButton(new ButtonWidget(this.x + 48, this.y + 59, 28, 20, new LiteralText("-" + b), btn -> addQty(-b)));
        this.addButton(new ButtonWidget(this.x + 82, this.y + 59, 32, 20, new LiteralText("-" + c), btn -> addQty(-c)));
        this.addButton(new ButtonWidget(this.x + 120, this.y + 59, 38, 20, new LiteralText("-" + d), btn -> addQty(-d)));

        this.addButton(this.redstoneMode);
    }

    @Override
    public void drawBG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX, final int mouseY, float partialTicks) {
        super.drawBG(matrices, offsetX, offsetY, mouseX, mouseY, partialTicks);
        this.level.render(matrices, mouseX, mouseY, partialTicks);
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
        if (!this.checkHotbarKeys(keyCode, scanCode)) {
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
