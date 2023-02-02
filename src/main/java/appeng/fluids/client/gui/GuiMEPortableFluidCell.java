/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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


import appeng.api.config.Settings;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.client.gui.AEBaseMEGui;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiScrollbar;
import appeng.client.gui.widgets.ISortSource;
import appeng.client.gui.widgets.MEGuiTextField;
import appeng.client.me.FluidRepo;
import appeng.client.me.InternalFluidSlotME;
import appeng.client.me.SlotFluidME;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.fluids.container.ContainerMEPortableFluidCell;
import appeng.fluids.container.ContainerWirelessFluidTerminal;
import appeng.fluids.container.slots.IMEFluidSlot;
import appeng.helpers.InventoryAction;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.Loader;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;


public class GuiMEPortableFluidCell extends AEBaseMEGui implements ISortSource, IConfigManagerHost {
    private final List<SlotFluidME> meFluidSlots = new LinkedList<>();
    private final FluidRepo repo;
    private final IConfigManager configSrc;
    private final ContainerMEPortableFluidCell container;
    private final int offsetX = 9;
    private final int rows = 6;
    private final int perRow = 9;

    protected ITerminalHost terminal;

    private MEGuiTextField searchField;
    private GuiImgButton sortByBox;
    private GuiImgButton sortDirBox;

    public GuiMEPortableFluidCell(InventoryPlayer inventoryPlayer, final WirelessTerminalGuiObject te, final ContainerWirelessFluidTerminal c) {
        super(c);
        this.terminal = te;
        this.xSize = 185;
        this.ySize = 222;
        final GuiScrollbar scrollbar = new GuiScrollbar();
        this.setScrollBar(scrollbar);
        this.repo = new FluidRepo(scrollbar, this);
        this.configSrc = ((IConfigurableObject) this.inventorySlots).getConfigManager();
        (this.container = (ContainerMEPortableFluidCell) this.inventorySlots).setGui(this);
    }

    @Override
    public void initGui() {
        this.mc.player.openContainer = this.inventorySlots;
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        this.searchField = new MEGuiTextField(this.fontRenderer, this.guiLeft + Math.max(80, this.offsetX), this.guiTop + 4, 90, 12);
        this.searchField.setEnableBackgroundDrawing(false);
        this.searchField.setMaxStringLength(25);
        this.searchField.setTextColor(0xFFFFFF);
        this.searchField.setSelectionColor(0xFF99FF99);
        this.searchField.setVisible(true);

        int offset = this.guiTop;

        this.buttonList.add(this.sortByBox = new GuiImgButton(this.guiLeft - 18, offset, Settings.SORT_BY, this.configSrc.getSetting(Settings.SORT_BY)));
        offset += 20;

        this.buttonList.add(this.sortDirBox = new GuiImgButton(this.guiLeft - 18, offset, Settings.SORT_DIRECTION, this.configSrc
                .getSetting(Settings.SORT_DIRECTION)));

        for (int y = 0; y < this.rows; y++) {
            for (int x = 0; x < this.perRow; x++) {
                SlotFluidME slot = new SlotFluidME(new InternalFluidSlotME(this.repo, x + y * this.perRow, this.offsetX + x * 18, 18 + y * 18));
                this.getMeFluidSlots().add(slot);
                this.inventorySlots.inventorySlots.add(slot);
            }
        }
        this.setScrollBar();
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.fontRenderer.drawString(this.getGuiDisplayName(GuiText.WirelessTerminal.getLocal()), 8, 6, 4210752);
        this.fontRenderer.drawString(GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752);
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.bindTexture(this.getBackground());
        final int x_width = 197;
        this.drawTexturedModalRect(offsetX, offsetY, 0, 0, x_width, 18);

        for (int x = 0; x < 6; x++) {
            this.drawTexturedModalRect(offsetX, offsetY + 18 + x * 18, 0, 18, x_width, 18);
        }

        this.drawTexturedModalRect(offsetX, offsetY + 16 + 6 * 18, 0, 106 - 18 - 18, x_width, 99 + 77);

        if (this.searchField != null) {
            this.searchField.drawTextBox();
        }
    }

    @Override
    public void updateScreen() {
        this.repo.setPower(this.container.isPowered());
        super.updateScreen();
    }

    @Override
    protected void renderHoveredToolTip(int mouseX, int mouseY) {
        final Slot slot = this.getSlot(mouseX, mouseY);

        if (slot != null && slot instanceof IMEFluidSlot && slot.isEnabled()) {
            final IMEFluidSlot fluidSlot = (IMEFluidSlot) slot;

            if (fluidSlot.getAEFluidStack() != null && fluidSlot.shouldRenderAsFluid()) {
                final IAEFluidStack fluidStack = fluidSlot.getAEFluidStack();
                final String formattedAmount = NumberFormat.getNumberInstance(Locale.US).format(fluidStack.getStackSize() / 1000.0) + " B";

                final String modName = "" + TextFormatting.BLUE + TextFormatting.ITALIC + Loader.instance()
                        .getIndexedModList()
                        .get(Platform.getModId(fluidStack))
                        .getName();

                final List<String> list = new ArrayList<>();

                list.add(fluidStack.getFluidStack().getLocalizedName());
                list.add(formattedAmount);
                list.add(modName);

                this.drawHoveringText(list, mouseX, mouseY);

                return;
            }
        }
        super.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void actionPerformed(GuiButton btn) throws IOException {
        if (btn instanceof GuiImgButton) {
            final boolean backwards = Mouse.isButtonDown(1);
            final GuiImgButton iBtn = (GuiImgButton) btn;

            if (iBtn.getSetting() != Settings.ACTIONS) {
                final Enum cv = iBtn.getCurrentValue();
                final Enum next = Platform.rotateEnum(cv, backwards, iBtn.getSetting().getPossibleValues());

                try {
                    NetworkHandler.instance().sendToServer(new PacketValueConfig(iBtn.getSetting().name(), next.name()));
                } catch (final IOException e) {
                    AELog.debug(e);
                }

                iBtn.set(next);
            }
        }
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
                    NetworkHandler.instance().sendToServer(new PacketInventoryAction(InventoryAction.FILL_ITEM, slot.slotNumber, 0));
                } else {
                    this.container.setTargetStack(meSlot.getAEFluidStack());
                    if (meSlot.getAEFluidStack() != null) {
                        AELog.debug("mouse1 GUI STACK SIZE %s", meSlot.getAEFluidStack().getStackSize());
                    }
                    NetworkHandler.instance().sendToServer(new PacketInventoryAction(InventoryAction.EMPTY_ITEM, slot.slotNumber, 0));
                }
            }
            return;
        }
        super.handleMouseClick(slot, slotIdx, mouseButton, clickType);
    }

    @Override
    protected void keyTyped(final char character, final int key) throws IOException {
        if (!this.checkHotbarKeys(key)) {
            if (character == ' ' && this.searchField.getText().isEmpty()) {
                return;
            }

            if (this.searchField.textboxKeyTyped(character, key)) {
                this.repo.setSearchString(this.searchField.getText());
                this.repo.updateView();
                this.setScrollBar();
            } else {
                super.keyTyped(character, key);
            }
        }
    }

    @Override
    protected void mouseClicked(final int xCoord, final int yCoord, final int btn) throws IOException {
        this.searchField.mouseClicked(xCoord, yCoord, btn);

        if (btn == 1 && this.searchField.isMouseIn(xCoord, yCoord)) {
            this.searchField.setText("");
            this.repo.setSearchString("");
            this.repo.updateView();
            this.setScrollBar();
        }

        super.mouseClicked(xCoord, yCoord, btn);
    }

    public void postUpdate(final List<IAEFluidStack> list) {
        for (final IAEFluidStack is : list) {
            this.repo.postUpdate(is);
        }

        this.repo.updateView();
        this.setScrollBar();
    }

    private void setScrollBar() {
        this.getScrollBar().setTop(18).setLeft(175).setHeight(this.rows * 18 - 2);
        this.getScrollBar().setRange(0, (this.repo.size() + this.perRow - 1) / this.perRow - this.rows, Math.max(1, this.rows / 6));
    }

    @Override
    public Enum getSortBy() {
        return this.configSrc.getSetting(Settings.SORT_BY);
    }

    @Override
    public Enum getSortDir() {
        return this.configSrc.getSetting(Settings.SORT_DIRECTION);
    }

    @Override
    public Enum getSortDisplay() {
        return this.configSrc.getSetting(Settings.VIEW_MODE);
    }

    @Override
    public void updateSetting(IConfigManager manager, Enum settingName, Enum newValue) {
        if (this.sortByBox != null) {
            this.sortByBox.set(this.configSrc.getSetting(Settings.SORT_BY));
        }

        if (this.sortDirBox != null) {
            this.sortDirBox.set(this.configSrc.getSetting(Settings.SORT_DIRECTION));
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
