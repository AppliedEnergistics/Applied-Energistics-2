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

package appeng.client.gui.me.common;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

import appeng.client.gui.style.TerminalStyle;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import appeng.api.config.SearchBoxMode;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.implementations.tiles.IMEChest;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.client.ActionKey;
import appeng.client.Point;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.me.items.RepoSlot;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AETextField;
import appeng.client.gui.widgets.IScrollSource;
import appeng.client.gui.widgets.ISortSource;
import appeng.client.gui.widgets.Scrollbar;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.client.gui.widgets.TabButton;
import appeng.client.gui.widgets.UpgradesPanel;
import appeng.container.SlotSemantic;
import appeng.container.me.common.GridInventoryEntry;
import appeng.container.me.common.MEMonitorableContainer;
import appeng.container.me.crafting.CraftingStatusContainer;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.localization.ButtonToolTips;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;
import appeng.core.sync.packets.MEInteractionPacket;
import appeng.core.sync.packets.SwitchGuisPacket;
import appeng.helpers.InventoryAction;
import appeng.integration.abstraction.JEIFacade;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;
import appeng.util.prioritylist.IPartitionList;

public abstract class MEMonitorableScreen<T extends IAEStack<T>, C extends MEMonitorableContainer<T>>
        extends AEBaseScreen<C> implements ISortSource, IConfigManagerHost {

    private static final int MIN_ROWS = 3;

    private static String memoryText = "";
    private final TerminalStyle style;
    private final Repo<T> repo;
    private final List<ItemStack> currentViewCells = new ArrayList<>();
    private final IConfigManager configSrc;
    private final boolean supportsViewCells;
    // The box on the right of the terminal shown behind the view cell slots
    @Nullable
    private final UpgradesPanel viewCellBg;
    private TabButton craftingStatusBtn;
    private AETextField searchField;
    private int rows = 0;
    private SettingToggleButton<ViewItems> viewModeToggle;
    private SettingToggleButton<SortOrder> sortByToggle;
    private SettingToggleButton<SortDir> sortDirToggle;
    private boolean isAutoFocus = false;
    private int currentMouseX = 0;
    private int currentMouseY = 0;

    public MEMonitorableScreen(TerminalStyle style, C container, PlayerInventory playerInventory,
            ITextComponent title, ScreenStyle style1) {
        super(container, playerInventory, title, style1);

        this.style = style;

        final Scrollbar scrollbar = new Scrollbar();
        this.setScrollBar(scrollbar);
        this.repo = createRepo(scrollbar);
        setScrollBar();

        this.xSize = style.getScreenWidth();
        this.ySize = style.getScreenHeight(0);

        this.configSrc = ((IConfigurableObject) this.container).getConfigManager();
        this.container.setGui(this);

        List<Slot> viewCellSlots = container.getSlots(SlotSemantic.VIEW_CELL);
        this.supportsViewCells = !viewCellSlots.isEmpty();
        if (this.supportsViewCells) {
            viewCellBg = new UpgradesPanel(xSize + 2, 0, viewCellSlots);
        } else {
            viewCellBg = null;
        }
    }

    protected abstract Repo<T> createRepo(IScrollSource scrollSource);

    @Nullable
    protected abstract IPartitionList<T> createPartitionList(List<ItemStack> viewCells);

    protected abstract void renderGridInventoryEntry(MatrixStack matrices, int x, int y, GridInventoryEntry<T> entry);

    protected abstract void handleGridInventoryEntryMouseClick(@Nullable GridInventoryEntry<T> entry, int mouseButton,
            ClickType clickType);

    public void postUpdate(boolean fullUpdate, final List<GridInventoryEntry<T>> list) {
        if (fullUpdate) {
            this.repo.clear();
        }

        for (GridInventoryEntry<T> entry : list) {
            this.repo.postUpdate(entry);
        }

        this.repo.updateView();
        this.setScrollBar();
    }

    private void setScrollBar() {
        this.getScrollBar().setTop(18).setLeft(175).setHeight(this.rows * 18 - 2);
        int totalRows = (this.repo.size() + getSlotsPerRow() - 1) / getSlotsPerRow();
        this.getScrollBar().setRange(0, totalRows - this.rows, Math.max(1, this.rows / 6));
    }

    private void showCraftingStatus() {
        NetworkHandler.instance().sendToServer(new SwitchGuisPacket(CraftingStatusContainer.TYPE));
    }

    private int getSlotsPerRow() {
        return style.getSlotsPerRow();
    }

    @Override
    public void init() {
        getMinecraft().keyboardListener.enableRepeatEvents(true);

        this.rows = MathHelper.clamp(style.getPossibleRows(height), MIN_ROWS, getMaxRows());

        // Size the container according to the number of rows we decided to have
        this.ySize = style.getScreenHeight(rows);

        // Re-create the ME slots since the number of rows could have changed
        List<Slot> slots = this.container.inventorySlots;
        slots.removeIf(slot -> slot instanceof RepoSlot);

        int repoIndex = 0;
        for (int row = 0; row < this.rows; row++) {
            for (int col = 0; col < style.getSlotsPerRow(); col++) {
                Point pos = style.getSlotPos(row, col);

                slots.add(new RepoSlot<>(this.repo, repoIndex++, pos.getX(), pos.getY()));
            }
        }

        super.init();

        if (style.hasSortByButton()) {
            this.sortByToggle = this.addToLeftToolbar(new SettingToggleButton<>(0, 0, Settings.SORT_BY,
                    getSortBy(), Platform::isSortOrderAvailable, this::toggleServerSetting));
        }

        // Toggling between craftable/stored items only makes sense if the terminal supports auto-crafting
        if (style.isSupportsAutoCrafting()) {
            this.viewModeToggle = this.addToLeftToolbar(new SettingToggleButton<>(0, 0,
                    Settings.VIEW_MODE, getSortDisplay(), this::toggleServerSetting));
        }

        this.addToLeftToolbar(this.sortDirToggle = new SettingToggleButton<>(0, 0,
                Settings.SORT_DIRECTION, getSortDir(), this::toggleServerSetting));

        SearchBoxMode searchMode = AEConfig.instance().getTerminalSearchMode();
        this.addToLeftToolbar(new SettingToggleButton<>(0, 0, Settings.SEARCH_MODE, searchMode,
                Platform::isSearchModeAvailable, this::toggleTerminalSearchMode));

        // Show a button to toggle the terminal style if the style doesn't enforce a max number of rows
        if (style.getMaxRows() == null) {
            appeng.api.config.TerminalStyle terminalStyle = AEConfig.instance().getTerminalStyle();
            this.addToLeftToolbar(new SettingToggleButton<>(0, 0, Settings.TERMINAL_STYLE, terminalStyle,
                    this::toggleTerminalStyle));
        }

        Rectangle2d searchFieldRect = style.getSearchFieldRect();
        this.searchField = new AETextField(this.font,
                this.guiLeft + searchFieldRect.getX(),
                this.guiTop + searchFieldRect.getY(),
                searchFieldRect.getWidth(),
                searchFieldRect.getHeight());
        this.searchField.setEnableBackgroundDrawing(false);
        this.searchField.setMaxStringLength(25);
        this.searchField.setTextColor(0xFFFFFF);
        this.searchField.setSelectionColor(0xFF008000);
        this.searchField.setVisible(true);

        if (style.isSupportsAutoCrafting()) {
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

        this.setScrollBar();

    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        // Override the dialog title found in the screen JSON with the user-supplied name
        if (!this.title.getString().isEmpty()) {
            setTextContent(TEXT_ID_DIALOG_TITLE, this.title);
        } else if (this.container.getTarget() instanceof IMEChest) {
            // ME Chest uses Item Terminals, but overrides the title
            setTextContent(TEXT_ID_DIALOG_TITLE, GuiText.Chest.text());
        }
    }

    @Override
    public void drawFG(MatrixStack matrixStack, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY) {
        this.currentMouseX = mouseX;
        this.currentMouseY = mouseY;

        // Show the number of active crafting jobs
        if (this.craftingStatusBtn != null && container.activeCraftingJobs != -1) {
            // The stack size renderer expects a 16x16 slot, while the button is normally
            // bigger
            int x = this.craftingStatusBtn.x + (this.craftingStatusBtn.getWidth() - 16) / 2;
            int y = this.craftingStatusBtn.y + (this.craftingStatusBtn.getHeightRealms() - 16) / 2;
            style.getStackSizeRenderer().renderSizeLabel(font, x - this.guiLeft, y - this.guiTop,
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
            RepoSlot<T> repoSlot = RepoSlot.tryCast(repo, slot);
            if (repoSlot != null) {
                GridInventoryEntry<T> entry = repoSlot.getEntry();
                long serial = entry != null ? entry.getSerial() : -1;
                final InventoryAction direction = wheelDelta > 0 ? InventoryAction.ROLL_DOWN
                        : InventoryAction.ROLL_UP;
                int times = (int) Math.abs(wheelDelta);
                for (int h = 0; h < times; h++) {
                    final MEInteractionPacket p = new MEInteractionPacket(this.container.windowId, serial, direction);
                    NetworkHandler.instance().sendToServer(p);
                }

                return true;
            }
        }
        return super.mouseScrolled(x, y, wheelDelta);
    }

    @Override
    protected void handleMouseClick(Slot slot, int slotIdx, int mouseButton, ClickType clickType) {
        RepoSlot<T> repoSlot = RepoSlot.tryCast(repo, slot);
        if (repoSlot != null) {
            handleGridInventoryEntryMouseClick(repoSlot.getEntry(), mouseButton, clickType);
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

        style.getHeader()
                .dest(offsetX, offsetY)
                .blit(matrixStack, getBlitOffset());

        int y = offsetY;
        style.getHeader().dest(offsetX, y).blit(matrixStack, getBlitOffset());
        y += style.getHeader().getSrcHeight();

        // To draw the first/last row, we need to at least draw 2
        int rowsToDraw = Math.max(2, this.rows);
        for (int x = 0; x < rowsToDraw; x++) {
            Blitter row = style.getRow();
            if (x == 0) {
                row = style.getFirstRow();
            } else if (x + 1 == rowsToDraw) {
                row = style.getLastRow();
            }
            row.dest(offsetX, y).blit(matrixStack, getBlitOffset());
            y += style.getRow().getSrcHeight();
        }

        style.getBottom().dest(offsetX, y).blit(matrixStack, getBlitOffset());

        if (viewCellBg != null) {
            viewCellBg.draw(matrixStack, getBlitOffset(), offsetX, offsetY);
        }

        if (this.searchField != null) {
            this.searchField.render(matrixStack, mouseX, mouseY, partialTicks);
        }

    }

    // TODO This is incorrectly named in MCP, it's renderSlot, essentially
    @Override
    protected void moveItems(MatrixStack matrices, Slot s) {
        RepoSlot<T> repoSlot = RepoSlot.tryCast(repo, s);
        if (repoSlot != null) {
            if (!this.repo.hasPower()) {
                fill(matrices, s.xPos, s.yPos, 16 + s.xPos, 16 + s.yPos, 0x66111111);
            } else {
                GridInventoryEntry<T> entry = repoSlot.getEntry();
                if (entry != null) {
                    try {
                        renderGridInventoryEntry(matrices, s.xPos, s.yPos, entry);
                    } catch (final Exception err) {
                        AELog.warn("[AppEng] AE prevented crash while drawing slot: " + err.toString());
                    }

                    // If a view mode is selected that only shows craftable items, display the "craftable" text
                    // regardless of stack size
                    long storedAmount = entry.getStoredAmount();
                    boolean craftable = entry.isCraftable();
                    if (isViewOnlyCraftable() && craftable) {
                        style.getStackSizeRenderer().renderStackSize(this.font, 0, true, s.xPos, s.yPos);
                    } else {
                        style.getStackSizeRenderer().renderStackSize(this.font, storedAmount, craftable, s.xPos,
                                s.yPos);
                    }
                }
            }

            return;
        }

        super.moveItems(matrices, s);
    }

    /**
     * @return True if the terminal should only show craftable items.
     */
    protected final boolean isViewOnlyCraftable() {
        return viewModeToggle != null && viewModeToggle.getCurrentValue() == ViewItems.CRAFTABLE;
    }

    protected void renderHoveredTooltip(MatrixStack matrixStack, int x, int y) {
        // Vanilla doesn't show item tooltips when the player have something in their hand
        if (style.isShowTooltipsWithItemInHand() || getPlayer().inventory.getItemStack().isEmpty()) {
            RepoSlot<T> repoSlot = RepoSlot.tryCast(repo, this.hoveredSlot);

            if (repoSlot != null) {
                GridInventoryEntry<T> entry = repoSlot.getEntry();
                if (entry != null) {
                    this.renderGridInventoryEntryTooltip(matrixStack, entry, x, y);
                }
                return;
            }
        }

        super.renderHoveredTooltip(matrixStack, x, y);
    }

    protected void renderGridInventoryEntryTooltip(MatrixStack matrices, GridInventoryEntry<T> entry, int x, int y) {
        final int bigNumber = AEConfig.instance().isUseLargeFonts() ? 999 : 9999;

        ItemStack stack = entry.getStack().asItemStackRepresentation();

        final List<ITextComponent> currentToolTip = this.getTooltipFromItem(stack);

        long storedAmount = entry.getStoredAmount();
        if (storedAmount > bigNumber || (storedAmount > 1 && stack.isDamaged())) {
            final String formattedAmount = NumberFormat.getNumberInstance(Locale.US)
                    .format(storedAmount);
            currentToolTip
                    .add(ButtonToolTips.ItemsStored.text(formattedAmount).mergeStyle(TextFormatting.GRAY));
        }

        long requestableAmount = entry.getRequestableAmount();
        if (requestableAmount > 0) {
            final String formattedAmount = NumberFormat.getNumberInstance(Locale.US)
                    .format(requestableAmount);
            currentToolTip.add(ButtonToolTips.ItemsRequestable.text(formattedAmount));
        }

        // TODO: Should also list craftable status
        if (Minecraft.getInstance().gameSettings.advancedItemTooltips) {
            currentToolTip
                    .add(new StringTextComponent("Serial: " + entry.getSerial()).mergeStyle(TextFormatting.DARK_GRAY));
        }

        this.renderToolTip(matrices, Lists.transform(currentToolTip, ITextComponent::func_241878_f), x, y, this.font);
    }

    private int getMaxRows() {
        Integer maxRows = style.getMaxRows();
        if (maxRows != null) {
            return maxRows;
        }
        return AEConfig.instance().getTerminalStyle() == appeng.api.config.TerminalStyle.SMALL ? 6 : Integer.MAX_VALUE;
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

        if (this.supportsViewCells) {
            List<ItemStack> viewCells = this.container.getViewCells();
            if (!this.currentViewCells.equals(viewCells)) {
                this.currentViewCells.clear();
                this.currentViewCells.addAll(viewCells);
                this.repo.setPartitionList(createPartitionList(viewCells));
            }
        }

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

    private void toggleTerminalSearchMode(SettingToggleButton<SearchBoxMode> btn, boolean backwards) {
        SearchBoxMode next = btn.getNextValue(backwards);
        AEConfig.instance().setTerminalSearchMode(next);
        btn.set(next);
        this.reinitalize();
    }

    private void toggleTerminalStyle(SettingToggleButton<appeng.api.config.TerminalStyle> btn, boolean backwards) {
        appeng.api.config.TerminalStyle next = btn.getNextValue(backwards);
        AEConfig.instance().setTerminalStyle(next);
        btn.set(next);
        this.reinitalize();
    }

    private <SE extends Enum<SE>> void toggleServerSetting(SettingToggleButton<SE> btn, boolean backwards) {
        SE next = btn.getNextValue(backwards);
        NetworkHandler.instance().sendToServer(new ConfigValuePacket(btn.getSetting().name(), next.name()));
        btn.set(next);
    }

    @Override
    public List<Rectangle2d> getExclusionZones() {
        List<Rectangle2d> result = super.getExclusionZones();
        if (viewCellBg != null) {
            viewCellBg.addExclusionZones(guiLeft, guiTop, result);
        }
        return result;
    }

    private void reinitalize() {
        this.children.removeAll(this.buttons);
        this.buttons.clear();
        this.init();
    }
}
