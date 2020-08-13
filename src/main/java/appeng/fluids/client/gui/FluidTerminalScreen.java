/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

package appeng.fluids.client.gui;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import com.mojang.blaze3d.matrix.MatrixStack;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.IConfigManager;
import appeng.client.ActionKey;
import appeng.client.gui.AEBaseMEScreen;
import appeng.client.gui.widgets.AETextField;
import appeng.client.gui.widgets.ISortSource;
import appeng.client.gui.widgets.Scrollbar;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.client.me.FluidRepo;
import appeng.client.me.InternalFluidSlotME;
import appeng.client.me.SlotFluidME;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;
import appeng.core.sync.packets.InventoryActionPacket;
import appeng.fluids.container.FluidTerminalContainer;
import appeng.fluids.container.slots.IMEFluidSlot;
import appeng.helpers.InventoryAction;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;

/**
 * @author BrockWS
 * @version rv6 - 12/05/2018
 * @since rv6 12/05/2018
 */
public class FluidTerminalScreen extends AEBaseMEScreen<FluidTerminalContainer>
        implements ISortSource, IConfigManagerHost {
    private final List<SlotFluidME> meFluidSlots = new LinkedList<>();
    private final FluidRepo repo;
    private final IConfigManager configSrc;

    private static final int GRID_OFFSET_X = 9;
    private static final int GRID_OFFSET_Y = 18;
    private static final int ROWS = 6;
    private static final int COLS = 9;

    private AETextField searchField;
    private SettingToggleButton<SortOrder> sortByBox;
    private SettingToggleButton<SortDir> sortDirBox;

    public FluidTerminalScreen(FluidTerminalContainer container, PlayerInventory playerInventory,
            ITextComponent title) {
        super(container, playerInventory, title);
        this.xSize = 185;
        this.ySize = 222;
        final Scrollbar scrollbar = new Scrollbar();
        this.setScrollBar(scrollbar);
        this.repo = new FluidRepo(scrollbar, this);
        this.configSrc = container.getConfigManager();
        this.container.setGui(this);
    }

    @Override
    public void init() {
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        this.searchField = new AETextField(this.font, this.guiLeft + 80, this.guiTop + 4, 90, 12);
        this.searchField.setEnableBackgroundDrawing(false);
        this.searchField.setMaxStringLength(25);
        this.searchField.setTextColor(0xFFFFFF);
        this.searchField.setSelectionColor(0xFF99FF99);
        this.searchField.setVisible(true);

        int offset = this.guiTop;

        this.sortByBox = this.addButton(new SettingToggleButton<>(this.guiLeft - 18, offset, Settings.SORT_BY,
                getSortBy(), Platform::isSortOrderAvailable, this::toggleServerSetting));
        offset += 20;

        this.sortDirBox = this.addButton(new SettingToggleButton<>(this.guiLeft - 18, offset, Settings.SORT_DIRECTION,
                getSortDir(), this::toggleServerSetting));

        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {
                SlotFluidME slot = new SlotFluidME(new InternalFluidSlotME(this.repo, x + y * COLS,
                        GRID_OFFSET_X + x * 18, GRID_OFFSET_Y + y * 18));
                this.getMeFluidSlots().add(slot);
                this.container.inventorySlots.add(slot);
            }
        }
        this.setScrollBar();
    }

    @Override
    public void drawFG(MatrixStack matrixStack, int offsetX, int offsetY, int mouseX, int mouseY) {
        this.font.drawString(matrixStack, "Fluid Terminal", 8, 6, 4210752);
        this.font.drawString(matrixStack, GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752);
    }

    @Override
    public void drawBG(MatrixStack matrixStack, int offsetX, int offsetY, int mouseX, int mouseY, float partialTicks) {
        this.bindTexture(this.getBackground());
        final int x_width = 197;
        GuiUtils.drawTexturedModalRect(offsetX, offsetY, 0, 0, x_width, 18, getBlitOffset());

        for (int x = 0; x < 6; x++) {
            GuiUtils.drawTexturedModalRect(offsetX, offsetY + 18 + x * 18, 0, 18, x_width, 18, getBlitOffset());
        }

        GuiUtils.drawTexturedModalRect(offsetX, offsetY + 16 + 6 * 18, 0, 106 - 18 - 18, x_width, 99 + 77,
                getBlitOffset());

        if (this.searchField != null) {
            this.searchField.render(matrixStack, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public void tick() {
        this.repo.setPower(this.container.isPowered());
        super.tick();
    }

    @Override
    protected void func_230459_a_(MatrixStack matrixStack, int mouseX, int mouseY) {
        final Slot slot = this.getSlot(mouseX, mouseY);

        if (slot instanceof IMEFluidSlot && slot.isEnabled()) {
            final IMEFluidSlot fluidSlot = (IMEFluidSlot) slot;

            if (fluidSlot.getAEFluidStack() != null && fluidSlot.shouldRenderAsFluid()) {
                final IAEFluidStack fluidStack = fluidSlot.getAEFluidStack();
                final String formattedAmount = NumberFormat.getNumberInstance(Locale.US)
                        .format(fluidStack.getStackSize() / 1000.0) + " B";

                final String modName = Platform.getModName(Platform.getModId(fluidStack));

                final List<ITextComponent> list = new ArrayList<>();
                list.add(fluidStack.getFluidStack().getDisplayName());
                list.add(new StringTextComponent(formattedAmount));
                list.add(new StringTextComponent(modName));

                this.func_243308_b(matrixStack, list, mouseX, mouseY);

                return;
            }
        }
        super.func_230459_a_(matrixStack, mouseX, mouseY);
    }

    private <S extends Enum<S>> void toggleServerSetting(SettingToggleButton<S> btn, boolean backwards) {
        S next = btn.getNextValue(backwards);
        NetworkHandler.instance().sendToServer(new ConfigValuePacket(btn.getSetting().name(), next.name()));
        btn.set(next);
    }

    @Override
    protected void handleMouseClick(Slot slot, int slotIdx, int mouseButton, ClickType clickType) {
        if (slot instanceof SlotFluidME) {
            final SlotFluidME meSlot = (SlotFluidME) slot;

            if (clickType == ClickType.PICKUP) {
                // TODO: Allow more options
                if (mouseButton == 0 && meSlot.getHasStack()) {
                    this.container.setTargetStack(meSlot.getAEFluidStack());
                    AELog.debug("mouse0 GUI STACK SIZE %s", meSlot.getAEFluidStack().getStackSize());
                    NetworkHandler.instance()
                            .sendToServer(new InventoryActionPacket(InventoryAction.FILL_ITEM, slot.slotNumber, 0));
                } else {
                    this.container.setTargetStack(meSlot.getAEFluidStack());
                    if (meSlot.getAEFluidStack() != null) {
                        AELog.debug("mouse1 GUI STACK SIZE %s", meSlot.getAEFluidStack().getStackSize());
                    }
                    NetworkHandler.instance()
                            .sendToServer(new InventoryActionPacket(InventoryAction.EMPTY_ITEM, slot.slotNumber, 0));
                }
            }
            return;
        }
        super.handleMouseClick(slot, slotIdx, mouseButton, clickType);
    }

    @Override
    public boolean charTyped(char character, int p_charTyped_2_) {
        if (character == ' ' && this.searchField.getText().isEmpty()) {
            return true;
        }

        if (this.searchField.isFocused() && this.searchField.charTyped(character, p_charTyped_2_)) {
            this.repo.setSearchString(this.searchField.getText());
            this.repo.updateView();
            this.setScrollBar();
            return true;
        }

        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int p_keyPressed_3_) {

        InputMappings.Input input = InputMappings.getInputByCode(keyCode, scanCode);

        if (keyCode != GLFW.GLFW_KEY_ESCAPE && !this.checkHotbarKeys(input)) {
            if (AppEng.proxy.isActionKey(ActionKey.TOGGLE_FOCUS, input)) {
                this.searchField.setFocused2(!this.searchField.isFocused());
                return true;
            }

            if (this.searchField.isFocused()) {
                if (keyCode == GLFW.GLFW_KEY_ENTER) {
                    this.searchField.setFocused2(false);
                    return true;
                }

                if (this.searchField.keyPressed(keyCode, scanCode, p_keyPressed_3_)) {
                    this.repo.setSearchString(this.searchField.getText());
                    this.repo.updateView();
                    this.setScrollBar();
                }

                // We need to swallow key presses if the field is focused because typing 'e'
                // would otherwise close
                // the screen
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, p_keyPressed_3_);
    }

    @Override
    public boolean mouseClicked(final double xCoord, final double yCoord, final int btn) {
        if (this.searchField.mouseClicked(xCoord, yCoord, btn)) {
            return true;
        }

        // Right-clicking on the search field should clear it
        if (this.searchField.isMouseOver(xCoord, yCoord) && btn == 1) {
            this.searchField.setText("");
            this.repo.setSearchString("");
            this.repo.updateView();
            this.setScrollBar();
            return true;
        }

        return super.mouseClicked(xCoord, yCoord, btn);
    }

    public void postUpdate(final List<IAEFluidStack> list) {
        for (final IAEFluidStack is : list) {
            this.repo.postUpdate(is);
        }

        this.repo.updateView();
        this.setScrollBar();
    }

    private void setScrollBar() {
        this.getScrollBar().setTop(18).setLeft(175).setHeight(ROWS * 18 - 2);
        this.getScrollBar().setRange(0, (this.repo.size() + COLS - 1) / COLS - ROWS, ROWS / 6);
    }

    @Override
    public SortOrder getSortBy() {
        return (SortOrder) this.configSrc.getSetting(Settings.SORT_BY);
    }

    @Override
    public SortDir getSortDir() {
        return (SortDir) this.configSrc.getSetting(Settings.SORT_DIRECTION);
    }

    @Override
    public ViewItems getSortDisplay() {
        return (ViewItems) this.configSrc.getSetting(Settings.VIEW_MODE);
    }

    @Override
    public void updateSetting(IConfigManager manager, Settings settingName, Enum<?> newValue) {
        if (this.sortByBox != null) {
            this.sortByBox.set(getSortBy());
        }

        if (this.sortDirBox != null) {
            this.sortDirBox.set(getSortDir());
        }

        this.repo.updateView();
    }

    protected List<SlotFluidME> getMeFluidSlots() {
        return this.meFluidSlots;
    }

    @Override
    protected boolean isPowered() {
        return this.repo.hasPower();
    }

    protected String getBackground() {
        return "guis/terminal.png";
    }

}
