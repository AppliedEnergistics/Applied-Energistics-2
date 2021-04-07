/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.client.gui.me.items;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import appeng.core.sync.packets.RequestAutoCraftPacket;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import appeng.api.config.SearchBoxMode;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.TerminalStyle;
import appeng.api.config.ViewItems;
import appeng.api.implementations.guiobjects.IPortableCell;
import appeng.api.implementations.tiles.IMEChest;
import appeng.api.implementations.tiles.IViewCellStorage;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.client.ActionKey;
import appeng.client.gui.AEBaseMEScreen;
import appeng.client.gui.implementations.SecurityStationScreen;
import appeng.client.gui.widgets.AETextField;
import appeng.client.gui.widgets.ISortSource;
import appeng.client.gui.widgets.Scrollbar;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.client.gui.widgets.TabButton;
import appeng.client.render.StackSizeRenderer;
import appeng.container.me.crafting.CraftingStatusContainer;
import appeng.container.me.items.MEMonitorableContainer;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.CraftingMatrixSlot;
import appeng.container.slot.FakeCraftingMatrixSlot;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.localization.ButtonToolTips;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;
import appeng.core.sync.packets.InventoryActionPacket;
import appeng.core.sync.packets.SwitchGuisPacket;
import appeng.helpers.InventoryAction;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.integration.abstraction.JEIFacade;
import appeng.parts.reporting.AbstractTerminalPart;
import appeng.tile.misc.SecurityStationTileEntity;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;

public class MEMonitorableScreen<T extends MEMonitorableContainer> extends AEBaseMEScreen<T>
        implements ISortSource, IConfigManagerHost {

    private static int craftingGridOffsetX;
    private static int craftingGridOffsetY;

    private static String memoryText = "";
    private final ItemRepo repo;
    private final int offsetX = 9;
    private final int lowerTextureOffset = 0;
    private final IConfigManager configSrc;
    private final boolean viewCell;
    private final ItemStack[] myCurrentViewCells = new ItemStack[5];
    private final StackSizeRenderer stackSizeRenderer = new StackSizeRenderer();
    private TabButton craftingStatusBtn;
    private AETextField searchField;
    private GuiText myName;
    private int perRow = 9;
    private int reservedSpace = 0;
    private boolean customSortOrder = true;
    private int rows = 0;
    private int maxRows = Integer.MAX_VALUE;
    private int standardSize;
    private SettingToggleButton<ViewItems> viewModeToggle;
    private SettingToggleButton<SortOrder> sortByToggle;
    private SettingToggleButton<SortDir> sortDirToggle;
    private boolean isAutoFocus = false;
    private int currentMouseX = 0;
    private int currentMouseY = 0;

    public MEMonitorableScreen(T container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title, null);

        final Scrollbar scrollbar = new Scrollbar();
        this.setScrollBar(scrollbar);
        this.repo = new ItemRepo(scrollbar, this);
        setScrollBar();

        this.xSize = 185;
        this.ySize = 204;

        Object te = container.getTarget();
        if (te instanceof IViewCellStorage) {
            this.xSize += 33;
        }

        this.standardSize = this.xSize;

        this.configSrc = ((IConfigurableObject) this.container).getConfigManager();
        this.container.setGui(this);

        this.viewCell = te instanceof IViewCellStorage;

        if (te instanceof SecurityStationTileEntity) {
            this.myName = GuiText.Security;
        } else if (te instanceof WirelessTerminalGuiObject) {
            this.myName = GuiText.WirelessTerminal;
        } else if (te instanceof IPortableCell) {
            this.myName = GuiText.PortableCell;
        } else if (te instanceof IMEChest) {
            this.myName = GuiText.Chest;
        } else if (te instanceof AbstractTerminalPart) {
            this.myName = GuiText.Terminal;
        } else {
            throw new IllegalArgumentException("Invalid GUI target given: " + te);
        }
    }

    public void postUpdate(final List<IAEItemStack> list) {
        for (final IAEItemStack is : list) {
            this.repo.postUpdate(is);
        }

        this.repo.updateView();
        this.setScrollBar();
    }

    private void setScrollBar() {
        this.getScrollBar().setTop(18).setLeft(175).setHeight(this.rows * 18 - 2);
        this.getScrollBar().setRange(0, (this.repo.size() + this.perRow - 1) / this.perRow - this.rows,
                Math.max(1, this.rows / 6));
    }

    private void showCraftingStatus() {
        NetworkHandler.instance().sendToServer(new SwitchGuisPacket(CraftingStatusContainer.TYPE));
    }

    @Override
    public void init() {
        getMinecraft().keyboardListener.enableRepeatEvents(true);

        this.maxRows = this.getMaxRows();
        TerminalStyle terminalStyle = AEConfig.instance().getTerminalStyle();

        if (terminalStyle != TerminalStyle.FULL) {
            this.perRow = 9;
        } else {
            this.perRow = 9 + ((this.width - this.standardSize) / 18);
        }
        this.xSize = this.standardSize + ((this.perRow - 9) * 18);

        final int magicNumber = 114 + 1;
        final int extraSpace = this.height - magicNumber - this.reservedSpace;

        this.rows = extraSpace / 18;
        if (this.rows > this.maxRows) {
            this.rows = this.maxRows;
        }

        if (this.rows < 3) {
            this.rows = 3;
        }

        // Size the container according to the number of rows we decided to have
        this.ySize = magicNumber + this.rows * 18 + this.reservedSpace;

        // Re-create the ME slots since the number of rows could have changed
        List<Slot> slots = this.container.inventorySlots;
        slots.removeIf(slot -> slot instanceof VirtualItemSlot);

        for (int y = 0; y < this.rows; y++) {
            for (int x = 0; x < this.perRow; x++) {
                VirtualItemSlot slot = new VirtualItemSlot(this.repo, x + y * this.perRow, this.offsetX + x * 18,
                        18 + y * 18);
                slots.add(slot);
            }
        }

        super.init();
        // full size : 204
        // extra slots : 72
        // slot 18

        if (this.customSortOrder) {
            this.sortByToggle = this.addToLeftToolbar(new SettingToggleButton<>(0, 0, Settings.SORT_BY,
                    getSortBy(), Platform::isSortOrderAvailable, this::toggleServerSetting));
        }

        if (this.viewCell || this instanceof WirelessTermScreen) {
            this.viewModeToggle = this.addButton(new SettingToggleButton<>(0, 0,
                    Settings.VIEW_MODE, getSortDisplay(), this::toggleServerSetting));
        }

        this.addToLeftToolbar(this.sortDirToggle = new SettingToggleButton<>(0, 0,
                Settings.SORT_DIRECTION, getSortDir(), this::toggleServerSetting));

        SearchBoxMode searchMode = AEConfig.instance().getTerminalSearchMode();
        this.addToLeftToolbar(new SettingToggleButton<>(0, 0, Settings.SEARCH_MODE, searchMode,
                Platform::isSearchModeAvailable, this::toggleTerminalSearchMode));

        if (!(this instanceof MEPortableCellScreen) || this instanceof WirelessTermScreen) {
            this.addToLeftToolbar(new SettingToggleButton<>(0, 0, Settings.TERMINAL_STYLE, terminalStyle,
                    this::toggleTerminalStyle));
        }

        this.searchField = new AETextField(this.font, this.guiLeft + Math.max(80, this.offsetX), this.guiTop + 4, 90,
                12);
        this.searchField.setEnableBackgroundDrawing(false);
        this.searchField.setMaxStringLength(25);
        this.searchField.setTextColor(0xFFFFFF);
        this.searchField.setSelectionColor(0xFF008000);
        this.searchField.setVisible(true);

        if (this.viewCell || this instanceof WirelessTermScreen) {
            this.craftingStatusBtn = this.addButton(new TabButton(this.guiLeft + 170, this.guiTop - 4, 2 + 11 * 16,
                    GuiText.CraftingStatus.text(), this.itemRenderer, btn -> showCraftingStatus()));
            this.craftingStatusBtn.setHideEdge(true);
        }

        this.isAutoFocus = SearchBoxMode.AUTOSEARCH == searchMode || SearchBoxMode.JEI_AUTOSEARCH == searchMode
                || SearchBoxMode.AUTOSEARCH_KEEP == searchMode || SearchBoxMode.JEI_AUTOSEARCH_KEEP == searchMode;
        final boolean isKeepFilter = SearchBoxMode.AUTOSEARCH_KEEP == searchMode
                || SearchBoxMode.JEI_AUTOSEARCH_KEEP == searchMode || SearchBoxMode.MANUAL_SEARCH_KEEP == searchMode
                || SearchBoxMode.JEI_MANUAL_SEARCH_KEEP == searchMode;
        final boolean isJEIEnabled = SearchBoxMode.JEI_AUTOSEARCH == searchMode
                || SearchBoxMode.JEI_MANUAL_SEARCH == searchMode;

        this.searchField.setFocused2(this.isAutoFocus);

        if (isJEIEnabled) {
            memoryText = JEIFacade.instance().getSearchText();
        }

        if (isKeepFilter && memoryText != null && !memoryText.isEmpty()) {
            this.searchField.setText(memoryText);
            this.searchField.selectAll();
            this.repo.setSearchString(memoryText);
            this.repo.updateView();
            this.setScrollBar();
        }

        craftingGridOffsetX = Integer.MAX_VALUE;
        craftingGridOffsetY = Integer.MAX_VALUE;

        for (final Slot s : this.container.inventorySlots) {
            if (s instanceof AppEngSlot) {
                if (s.xPos < 197) {
                    this.repositionSlot((AppEngSlot) s);
                }
            }

            if (s instanceof CraftingMatrixSlot || s instanceof FakeCraftingMatrixSlot) {
                if (s.xPos > 0 && s.yPos > 0) {
                    craftingGridOffsetX = Math.min(craftingGridOffsetX, s.xPos);
                    craftingGridOffsetY = Math.min(craftingGridOffsetY, s.yPos);
                }
            }
        }

        craftingGridOffsetX -= 25;
        craftingGridOffsetY -= 6;

        this.setScrollBar();

    }

    @Override
    public void drawFG(MatrixStack matrixStack, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY) {
        this.font.drawString(matrixStack, this.getGuiDisplayName(this.myName.text()).getString(), 8, 6,
                COLOR_DARK_GRAY);
        this.font.drawString(matrixStack, GuiText.inventory.text().getString(), 8, this.ySize - 96 + 3,
                COLOR_DARK_GRAY);

        this.currentMouseX = mouseX;
        this.currentMouseY = mouseY;

        // Show the number of active crafting jobs
        if (this.craftingStatusBtn != null && container.activeCraftingJobs != -1) {
            // The stack size renderer expects a 16x16 slot, while the button is normally
            // bigger
            int x = this.craftingStatusBtn.x + (this.craftingStatusBtn.getWidth() - 16) / 2;
            int y = this.craftingStatusBtn.y + (this.craftingStatusBtn.getHeightRealms() - 16) / 2;
            StackSizeRenderer.renderSizeLabel(font, x - this.guiLeft, y - this.guiTop,
                    String.valueOf(container.activeCraftingJobs));
        }
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

    @Override
    public boolean mouseScrolled(double x, double y, double wheelDelta) {
        if (wheelDelta != 0 && hasShiftDown()) {
            final Slot slot = this.getSlot((int) x, (int) y);
            if (slot instanceof VirtualItemSlot) {
                final IAEItemStack item = ((VirtualItemSlot) slot).getAEStack();
                if (item != null) {
                    this.container.setTargetStack(item);
                    final InventoryAction direction = wheelDelta > 0 ? InventoryAction.ROLL_DOWN
                            : InventoryAction.ROLL_UP;
                    final int times = (int) Math.abs(wheelDelta);
                    final int inventorySize = this.container.inventorySlots.size();
                    for (int h = 0; h < times; h++) {
                        final InventoryActionPacket p = new InventoryActionPacket(direction, inventorySize, 0);
                        NetworkHandler.instance().sendToServer(p);
                    }

                    return true;
                }
            }
        }
        return super.mouseScrolled(x, y, wheelDelta);
    }

    @Override
    protected void handleMouseClick(Slot slot, int slotIdx, int mouseButton, ClickType clickType) {
        if (slot instanceof VirtualItemSlot) {
            VirtualItemSlot virtualItemSlot = (VirtualItemSlot) slot;

            IAEItemStack stack = virtualItemSlot.getAEStack();

            // Move as many items of a single type as possible
            if (InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), GLFW.GLFW_KEY_SPACE)) {
                this.container.setTargetStack(stack);
                final InventoryActionPacket p = new InventoryActionPacket(InventoryAction.MOVE_REGION, -1, 0);
                NetworkHandler.instance().sendToServer(p);
            } else {
                InventoryAction action = null;

                switch (clickType) {
                    case PICKUP: // pickup / set-down.
                        action = (mouseButton == 1) ? InventoryAction.SPLIT_OR_PLACE_SINGLE
                                : InventoryAction.PICKUP_OR_SET_DOWN;

                        if (stack != null && action == InventoryAction.PICKUP_OR_SET_DOWN && stack.getStackSize() == 0
                                && playerInventory.getItemStack().isEmpty()) {
                            NetworkHandler.instance().sendToServer(new RequestAutoCraftPacket(stack));
                            return;
                        }

                        break;
                    case QUICK_MOVE:
                        action = (mouseButton == 1) ? InventoryAction.PICKUP_SINGLE : InventoryAction.SHIFT_CLICK;
                        break;

                    case CLONE: // creative dupe:
                        if (stack != null && stack.isCraftable()) {
                            NetworkHandler.instance().sendToServer(new RequestAutoCraftPacket(stack));
                            return;
                        } else if (playerInventory.player.abilities.isCreativeMode) {
                            final IAEItemStack slotItem = ((VirtualItemSlot) slot).getAEStack();
                            if (slotItem != null) {
                                action = InventoryAction.CREATIVE_DUPLICATE;
                            }
                        }
                        break;

                    default:
                    case THROW: // drop item:
                }

                if (action != null) {
                    this.container.setTargetStack(stack);
                    final InventoryActionPacket p = new InventoryActionPacket(action, -1, 0);
                    NetworkHandler.instance().sendToServer(p);
                }
            }
            return;
        }

        super.handleMouseClick(slot, slotIdx, mouseButton, clickType);
    }

    @Override
    public void onClose() {
        super.onClose();
        getMinecraft().keyboardListener.enableRepeatEvents(false);
        memoryText = this.searchField.getText();
    }

    @Override
    public void drawBG(MatrixStack matrixStack, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY, float partialTicks) {

        this.bindTexture(this.getBackground());
        final int x_width = 197;
        blit(matrixStack, offsetX, offsetY, 0, 0, x_width, 18);

        if (this.viewCell || (this instanceof SecurityStationScreen)) {
            blit(matrixStack, offsetX + x_width, offsetY, x_width, 0, 46, 128);
        }

        for (int x = 0; x < this.rows; x++) {
            blit(matrixStack, offsetX, offsetY + 18 + x * 18, 0, 18, x_width, 18);
        }

        blit(matrixStack, offsetX, offsetY + 16 + this.rows * 18 + this.lowerTextureOffset, 0, 106 - 18 - 18, x_width,
                99 + this.reservedSpace - this.lowerTextureOffset);

        if (this.viewCell) {
            boolean update = false;

            for (int i = 0; i < 5; i++) {
                if (this.myCurrentViewCells[i] != this.container.getCellViewSlot(i).getStack()) {
                    update = true;
                    this.myCurrentViewCells[i] = this.container.getCellViewSlot(i).getStack();
                }
            }

            if (update) {
                this.repo.setViewCell(this.myCurrentViewCells);
            }
        }

        if (this.searchField != null) {
            this.searchField.render(matrixStack, mouseX, mouseY, partialTicks);
        }

    }

    // TODO This is incorrectly named in MCP, it's renderSlot, essentially
    @Override
    protected void moveItems(MatrixStack matrices, Slot s) {
        if (s instanceof VirtualItemSlot) {

            try {
                if (!this.repo.hasPower()) {
                    fill(matrices, s.xPos, s.yPos, 16 + s.xPos, 16 + s.yPos, 0x66111111);
                }

                // Annoying but easier than trying to splice into render item
                super.moveItems(matrices, new Size1Slot(s));

                this.stackSizeRenderer.renderStackSize(this.font, ((VirtualItemSlot) s).getAEStack(), s.xPos, s.yPos);

            } catch (final Exception err) {
                AELog.warn("[AppEng] AE prevented crash while drawing slot: " + err.toString());
            }

            return;
        }

        super.moveItems(matrices, s);
    }

    @Override
    protected void renderTooltip(MatrixStack matrixStack, final ItemStack stack, final int x, final int y) {
        final Slot s = this.getSlot(x, y);

        if (s instanceof VirtualItemSlot && !stack.isEmpty()) {
            final int bigNumber = AEConfig.instance().isUseLargeFonts() ? 999 : 9999;

            IAEItemStack myStack = null;
            final List<ITextComponent> currentToolTip = this.getTooltipFromItem(stack);

            try {
                final VirtualItemSlot theSlotField = (VirtualItemSlot) s;
                myStack = theSlotField.getAEStack();
            } catch (final Throwable ignore) {
            }

            if (myStack != null) {
                if (myStack.getStackSize() > bigNumber || (myStack.getStackSize() > 1 && stack.isDamaged())) {
                    final String formattedAmount = NumberFormat.getNumberInstance(Locale.US)
                            .format(myStack.getStackSize());
                    currentToolTip
                            .add(ButtonToolTips.ItemsStored.text(formattedAmount).mergeStyle(TextFormatting.GRAY));
                }

                if (myStack.getCountRequestable() > 0) {
                    final String formattedAmount = NumberFormat.getNumberInstance(Locale.US)
                            .format(myStack.getCountRequestable());
                    currentToolTip.add(ButtonToolTips.ItemsRequestable.text(formattedAmount));
                }

                this.renderToolTip(matrixStack, Lists.transform(currentToolTip, ITextComponent::func_241878_f), x, y,
                        this.font);

                return;
            } else if (stack.getCount() > bigNumber) {
                final String formattedAmount = NumberFormat.getNumberInstance(Locale.US).format(stack.getCount());
                currentToolTip.add(ButtonToolTips.ItemsStored.text(formattedAmount).mergeStyle(TextFormatting.GRAY));

                this.renderToolTip(matrixStack, Lists.transform(currentToolTip, ITextComponent::func_241878_f), x, y,
                        this.font);

                return;
            }
        }

        super.renderTooltip(matrixStack, stack, x, y);
    }

    protected String getBackground() {
        return "guis/terminal.png";
    }

    protected int getMaxRows() {
        return AEConfig.instance().getTerminalStyle() == TerminalStyle.SMALL ? 6 : Integer.MAX_VALUE;
    }

    protected void repositionSlot(final AppEngSlot s) {
        s.yPos = s.getY() + this.ySize - 78 - 5;
    }

    @Override
    public boolean charTyped(char character, int p_charTyped_2_) {
        if (character == ' ' && this.searchField.getText().isEmpty()) {
            return true;
        }

        if (this.isAutoFocus && !this.searchField.isFocused() && isHovered()) {
            this.searchField.setFocused2(true);
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
            if (!this.searchField.isFocused() && this.isAutoFocus && isHovered()) {
                this.searchField.setFocused2(true);
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

    private boolean isHovered() {
        return isPointInRegion(0, 0, this.xSize, this.ySize, currentMouseX, currentMouseY);
    }

    @Override
    public void tick() {
        this.repo.setPower(this.container.isPowered());
        super.tick();
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
    public void updateSetting(final IConfigManager manager, final Settings settingName, final Enum<?> newValue) {
        if (this.sortByToggle != null) {
            this.sortByToggle.set(getSortBy());
        }

        if (this.sortDirToggle != null) {
            this.sortDirToggle.set(getSortDir());
        }

        if (this.viewModeToggle != null) {
            this.viewModeToggle.set(getSortDisplay());
        }

        this.repo.updateView();
    }

    protected int getReservedSpace() {
        return this.reservedSpace;
    }

    protected void setReservedSpace(final int reservedSpace) {
        this.reservedSpace = reservedSpace;
    }

    public boolean isCustomSortOrder() {
        return this.customSortOrder;
    }

    protected void setCustomSortOrder(final boolean customSortOrder) {
        this.customSortOrder = customSortOrder;
    }

    public int getStandardSize() {
        return this.standardSize;
    }

    protected void setStandardSize(final int standardSize) {
        this.standardSize = standardSize;
    }

    private void toggleTerminalSearchMode(SettingToggleButton<SearchBoxMode> btn, boolean backwards) {
        SearchBoxMode next = btn.getNextValue(backwards);
        AEConfig.instance().setTerminalSearchMode(next);
        btn.set(next);
        this.reinitalize();
    }

    private void toggleTerminalStyle(SettingToggleButton<TerminalStyle> btn, boolean backwards) {
        TerminalStyle next = btn.getNextValue(backwards);
        AEConfig.instance().setTerminalStyle(next);
        btn.set(next);
        this.reinitalize();
    }

    private <S extends Enum<S>> void toggleServerSetting(SettingToggleButton<S> btn, boolean backwards) {
        S next = btn.getNextValue(backwards);
        NetworkHandler.instance().sendToServer(new ConfigValuePacket(btn.getSetting().name(), next.name()));
        btn.set(next);
    }

    private void reinitalize() {
        this.children.removeAll(this.buttons);
        this.buttons.clear();
        this.init();
    }

}
