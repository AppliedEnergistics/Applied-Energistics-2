package appeng.client.gui.implementations;

import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.MEGuiTextField;
import appeng.container.implementations.ContainerRenamer;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.helpers.ICustomNameObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import java.io.IOException;

public class GuiRenamer extends AEBaseGui {
    private MEGuiTextField textField;
    private GuiButton confirmButton;

    public GuiRenamer(InventoryPlayer ip, ICustomNameObject obj) {
        super(new ContainerRenamer(ip, obj));
        this.xSize = 256;

        this.textField = new MEGuiTextField(Minecraft.getMinecraft().fontRenderer, 0, 0, 230, 12) {
            @Override
            public void onTextChange(final String oldText) {
                final String text = getText();

                if (!text.equals(oldText)) {
                    ((ContainerRenamer) inventorySlots).setCustomName(text);
                }
            }
        };
        this.textField.setEnableBackgroundDrawing(false);
        this.textField.setMaxStringLength(32);
    }

    @Override
    public void initGui() {
        super.initGui();

        this.textField.x = this.guiLeft + 12;
        this.textField.y = this.guiTop + 35;
        this.textField.setFocused(true);

        this.buttonList.add(this.confirmButton = new GuiButton(0, this.guiLeft + 238, this.guiTop + 33, 12, 12, "↵"));

        ((ContainerRenamer) this.inventorySlots).setTextField(this.textField);
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.fontRenderer.drawString(this.getGuiDisplayName(GuiText.Renamer.getLocal()), 12, 8, 4210752);
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.bindTexture("guis/renamer.png");
        this.drawTexturedModalRect(offsetX, offsetY, 0, 0, this.xSize, this.ySize);
        this.textField.drawTextBox();
    }

    @Override
    protected void mouseClicked(final int xCoord, final int yCoord, final int btn) throws IOException{
        this.textField.mouseClicked(xCoord, yCoord, btn);
        super.mouseClicked(xCoord, yCoord, btn);
    }

    @Override
    protected void keyTyped(final char character, final int key) throws IOException {
        if (key == 28) { // Enter
            try {
                NetworkHandler.instance().sendToServer(
                        new PacketValueConfig("QuartzKnife.ReName", this.textField.getText()));
            } catch (IOException e) {
                AELog.debug(e);
            }
            this.mc.player.closeScreen();
        } else if (!this.textField.textboxKeyTyped(character, key)) {
            super.keyTyped(character, key);
        }
    }

    @Override
    protected void actionPerformed(final GuiButton btn) throws IOException {
        super.actionPerformed(btn);

        if(btn == this.confirmButton) {
            try {
                NetworkHandler.instance().sendToServer(
                        new PacketValueConfig("QuartzKnife.ReName", this.textField.getText()));
                this.mc.player.closeScreen();
            } catch (IOException e) {
                AELog.debug(e);
            }
        }
    }

    public boolean isOverTextField(final int mousex, final int mousey) {
        return textField.isMouseIn(mousex, mousey);
    }

    public void setTextFieldValue(final String displayName, final int mousex, final int mousey, final ItemStack stack) {
        textField.setText(displayName);
    }
}
