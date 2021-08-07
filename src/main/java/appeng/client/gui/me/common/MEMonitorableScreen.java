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
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.blaze3d.vertex.PoseStack;

import org.lwjgl.glfw.GLFW;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.SearchBoxMode;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.implementations.blockentities.IMEChest;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.client.ActionKey;
import appeng.client.Point;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.me.items.RepoSlot;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.TerminalStyle;
import appeng.client.gui.widgets.AETextField;
import appeng.client.gui.widgets.IScrollSource;
import appeng.client.gui.widgets.ISortSource;
import appeng.client.gui.widgets.Scrollbar;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.client.gui.widgets.TabButton;
import appeng.client.gui.widgets.UpgradesPanel;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.AppEngClient;
import appeng.core.localization.ButtonToolTips;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;
import appeng.core.sync.packets.MEInteractionPacket;
import appeng.core.sync.packets.SwitchGuisPacket;
import appeng.helpers.InventoryAction;
import appeng.integration.abstraction.JEIFacade;
import appeng.menu.SlotSemantic;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.me.common.MEMonitorableMenu;
import appeng.menu.me.crafting.CraftingStatusMenu;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;
import appeng.util.prioritylist.IPartitionList;

public abstract class MEMonitorableScreen<T extends IAEStack<T>, C extends MEMonitorableMenu<T>>
        extends AEBaseScreen<C> implements ISortSource, IConfigManagerHost {

    private static final int MIN_ROWS = 3;

    private static String memoryText = "";
    private final TerminalStyle style;
    protected final Repo<T> repo;
    private final List<ItemStack> currentViewCells = new ArrayList<>();
    private final IConfigManager configSrc;
    private final boolean supportsViewCells;
    private TabButton craftingStatusBtn;
    private AETextField searchField;
    private int rows = 0;
    private SettingToggleButton<ViewItems> viewModeToggle;
    private SettingToggleButton<SortOrder> sortByToggle;
    private final SettingToggleButton<SortDir> sortDirToggle;
    private boolean isAutoFocus = false;
    private int currentMouseX = 0;
    private int currentMouseY = 0;
    private final Scrollbar scrollbar;

    public MEMonitorableScreen(C menu, Inventory playerInventory,
            Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);

        this.style = style.getTerminalStyle();
        if (this.style == null) {
            throw new IllegalStateException(
                    "Cannot construct screen " + getClass() + " without a terminalStyles setting");
        }

        this.scrollbar = widgets.addScrollBar("scrollbar");
        this.repo = createRepo(scrollbar);
        menu.setClientRepo(this.repo);
        this.repo.setUpdateViewListener(this::updateScrollbar);
        updateScrollbar();

        this.imageWidth = this.style.getScreenWidth();
        this.imageHeight = this.style.getScreenHeight(0);

        this.configSrc = ((IConfigurableObject) this.menu).getConfigManager();
        this.menu.setGui(this);

        List<Slot> viewCellSlots = menu.getSlots(SlotSemantic.VIEW_CELL);
        this.supportsViewCells = !viewCellSlots.isEmpty();
        if (this.supportsViewCells) {
            List<Component> tooltip = Collections.singletonList(GuiText.TerminalViewCellsTooltip.text());
            this.widgets.add("viewCells", new UpgradesPanel(viewCellSlots, () -> tooltip));
        }

        if (this.style.isSupportsAutoCrafting()) {
            this.craftingStatusBtn = new TabButton(Icon.PERMISSION_CRAFT,
                    GuiText.CraftingStatus.text(), this.itemRenderer, btn -> showCraftingStatus());
            this.craftingStatusBtn.setHideEdge(true);
            this.widgets.add("craftingStatus", this.craftingStatusBtn);
        }

        if (this.style.isSortable()) {
            this.sortByToggle = this.addToLeftToolbar(new SettingToggleButton<>(Settings.SORT_BY,
                    getSortBy(), Platform::isSortOrderAvailable, this::toggleServerSetting));
        }

        // Toggling between craftable/stored items only makes sense if the terminal supports auto-crafting
        if (this.style.isSupportsAutoCrafting()) {
            this.viewModeToggle = this.addToLeftToolbar(new SettingToggleButton<>(
                    Settings.VIEW_MODE, getSortDisplay(), this::toggleServerSetting));
        }

        this.addToLeftToolbar(this.sortDirToggle = new SettingToggleButton<>(
                Settings.SORT_DIRECTION, getSortDir(), this::toggleServerSetting));

        SearchBoxMode searchMode = AEConfig.instance().getTerminalSearchMode();
        this.addToLeftToolbar(new SettingToggleButton<>(Settings.SEARCH_MODE, searchMode,
                Platform::isSearchModeAvailable, this::toggleTerminalSearchMode));

        // Show a button to toggle the terminal style if the style doesn't enforce a max number of rows
        if (this.style.getMaxRows() == null) {
            appeng.api.config.TerminalStyle terminalStyle = AEConfig.instance().getTerminalStyle();
            this.addToLeftToolbar(new SettingToggleButton<>(Settings.TERMINAL_STYLE, terminalStyle,
                    this::toggleTerminalStyle));
        }
    }

    protected abstract Repo<T> createRepo(IScrollSource scrollSource);

    @Nullable
    protected abstract IPartitionList<T> createPartitionList(List<ItemStack> viewCells);

    protected abstract void renderGridInventoryEntry(PoseStack poseStack, int x, int y, GridInventoryEntry<T> entry);

    protected abstract void handleGridInventoryEntryMouseClick(@Nullable GridInventoryEntry<T> entry, int mouseButton,
            ClickType clickType);

    private void updateScrollbar() {
        scrollbar.setHeight(this.rows * style.getRow().getSrcHeight() - 2);
        int totalRows = (this.repo.size() + getSlotsPerRow() - 1) / getSlotsPerRow();
        scrollbar.setRange(0, totalRows - this.rows, Math.max(1, this.rows / 6));
    }

    private void showCraftingStatus() {
        NetworkHandler.instance().sendToServer(new SwitchGuisPacket(CraftingStatusMenu.TYPE));
    }

    private int getSlotsPerRow() {
        return style.getSlotsPerRow();
    }

    @Override
    public void init() {
        getMinecraft().keyboardHandler.setSendRepeatsToGui(true);

        this.rows = Mth.clamp(style.getPossibleRows(height), MIN_ROWS, getMaxRows());

        // Size the menu according to the number of rows we decided to have
        this.imageHeight = style.getScreenHeight(rows);

        // Re-create the ME slots since the number of rows could have changed
        List<Slot> slots = this.menu.slots;
        slots.removeIf(slot -> slot instanceof RepoSlot);

        int repoIndex = 0;
        for (int row = 0; row < this.rows; row++) {
            for (int col = 0; col < style.getSlotsPerRow(); col++) {
                Point pos = style.getSlotPos(row, col);

                slots.add(new RepoSlot<>(this.repo, repoIndex++, pos.getX(), pos.getY()));
            }
        }

        super.init();

        Rect2i searchFieldRect = style.getSearchFieldRect();
        this.searchField = new AETextField(this.font,
                this.leftPos + searchFieldRect.getX(),
                this.topPos + searchFieldRect.getY(),
                searchFieldRect.getWidth(),
                searchFieldRect.getHeight());
        this.searchField.setBordered(false);
        this.searchField.setMaxLength(25);
        this.searchField.setTextColor(0xFFFFFF);
        this.searchField.setSelectionColor(0xFF008000);
        this.searchField.setVisible(true);

        SearchBoxMode searchMode = AEConfig.instance().getTerminalSearchMode();
        this.isAutoFocus = SearchBoxMode.AUTOSEARCH == searchMode || SearchBoxMode.JEI_AUTOSEARCH == searchMode
                || SearchBoxMode.AUTOSEARCH_KEEP == searchMode || SearchBoxMode.JEI_AUTOSEARCH_KEEP == searchMode;
        final boolean isKeepFilter = SearchBoxMode.AUTOSEARCH_KEEP == searchMode
                || SearchBoxMode.JEI_AUTOSEARCH_KEEP == searchMode || SearchBoxMode.MANUAL_SEARCH_KEEP == searchMode
                || SearchBoxMode.JEI_MANUAL_SEARCH_KEEP == searchMode;
        final boolean isJEIEnabled = SearchBoxMode.JEI_AUTOSEARCH == searchMode
                || SearchBoxMode.JEI_MANUAL_SEARCH == searchMode;

        this.searchField.setFocus(this.isAutoFocus);
        if (this.searchField.isFocused()) {
            this.setFocused(this.searchField);
        }

        if (isJEIEnabled) {
            memoryText = JEIFacade.instance().getSearchText();
        }

        if (isKeepFilter && memoryText != null && !memoryText.isEmpty()) {
            this.searchField.setValue(memoryText);
            this.searchField.selectAll();
            this.repo.setSearchString(memoryText);
            this.repo.updateView();
        }

        this.updateScrollbar();
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        // Override the dialog title found in the screen JSON with the user-supplied name
        if (!this.title.getString().isEmpty()) {
            setTextContent(TEXT_ID_DIALOG_TITLE, this.title);
        } else if (this.menu.getTarget() instanceof IMEChest) {
            // ME Chest uses Item Terminals, but overrides the title
            setTextContent(TEXT_ID_DIALOG_TITLE, GuiText.Chest.text());
        }
    }

    @Override
    public void drawFG(PoseStack poseStack, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY) {
        this.currentMouseX = mouseX;
        this.currentMouseY = mouseY;

        // Show the number of active crafting jobs
        if (this.craftingStatusBtn != null && menu.activeCraftingJobs != -1) {
            // The stack size renderer expects a 16x16 slot, while the button is normally
            // bigger
            int x = this.craftingStatusBtn.x + (this.craftingStatusBtn.getWidth() - 16) / 2;
            int y = this.craftingStatusBtn.y + (this.craftingStatusBtn.getHeight() - 16) / 2;

            style.getStackSizeRenderer().renderSizeLabel(font, x - this.leftPos, y - this.topPos,
                    String.valueOf(menu.activeCraftingJobs));
        }
    }

    @Override
    public boolean mouseClicked(final double xCoord, final double yCoord, final int btn) {
        if (this.searchField.mouseClicked(xCoord, yCoord, btn)) {
            return true;
        }

        // Right-clicking on the search field should clear it
        if (this.searchField.isMouseOver(xCoord, yCoord) && btn == 1) {
            this.searchField.setValue("");
            this.repo.setSearchString("");
            this.repo.updateView();
            this.updateScrollbar();
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
                    final MEInteractionPacket p = new MEInteractionPacket(this.menu.containerId, serial, direction);
                    NetworkHandler.instance().sendToServer(p);
                }

                return true;
            }
        }
        return super.mouseScrolled(x, y, wheelDelta);
    }

    @Override
    protected void slotClicked(Slot slot, int slotIdx, int mouseButton, ClickType clickType) {
        RepoSlot<T> repoSlot = RepoSlot.tryCast(repo, slot);
        if (repoSlot != null) {
            handleGridInventoryEntryMouseClick(repoSlot.getEntry(), mouseButton, clickType);
            return;
        }

        super.slotClicked(slot, slotIdx, mouseButton, clickType);
    }

    @Override
    public void removed() {
        super.removed();
        getMinecraft().keyboardHandler.setSendRepeatsToGui(false);
        memoryText = this.searchField.getValue();
    }

    @Override
    public void drawBG(PoseStack poseStack, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY, float partialTicks) {

        style.getHeader()
                .dest(offsetX, offsetY)
                .blit(poseStack, getBlitOffset());

        int y = offsetY;
        style.getHeader().dest(offsetX, y).blit(poseStack, getBlitOffset());
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
            row.dest(offsetX, y).blit(poseStack, getBlitOffset());
            y += style.getRow().getSrcHeight();
        }

        style.getBottom().dest(offsetX, y).blit(poseStack, getBlitOffset());

        if (this.searchField != null) {
            this.searchField.render(poseStack, mouseX, mouseY, partialTicks);
        }

    }

    // TODO This is incorrectly named in MCP, it's renderSlot, essentially
    @Override
    protected void renderSlot(PoseStack poseStack, Slot s) {
        RepoSlot<T> repoSlot = RepoSlot.tryCast(repo, s);
        if (repoSlot != null) {
            if (!this.repo.hasPower()) {
                fill(poseStack, s.x, s.y, 16 + s.x, 16 + s.y, 0x66111111);
            } else {
                GridInventoryEntry<T> entry = repoSlot.getEntry();
                if (entry != null) {
                    try {
                        renderGridInventoryEntry(poseStack, s.x, s.y, entry);
                    } catch (final Exception err) {
                        AELog.warn("[AppEng] AE prevented crash while drawing slot: " + err);
                    }

                    // If a view mode is selected that only shows craftable items, display the "craftable" text
                    // regardless of stack size
                    long storedAmount = entry.getStoredAmount();
                    boolean craftable = entry.isCraftable();
                    if (isViewOnlyCraftable() && craftable) {
                        style.getStackSizeRenderer().renderStackSize(this.font, 0, true, s.x, s.y);
                    } else {
                        style.getStackSizeRenderer().renderStackSize(this.font, storedAmount, craftable, s.x,
                                s.y);
                    }
                }
            }

            return;
        }

        super.renderSlot(poseStack, s);
    }

    /**
     * @return True if the terminal should only show craftable items.
     */
    protected final boolean isViewOnlyCraftable() {
        return viewModeToggle != null && viewModeToggle.getCurrentValue() == ViewItems.CRAFTABLE;
    }

    @Override
    protected void renderTooltip(PoseStack poseStack, int x, int y) {
        // Vanilla doesn't show item tooltips when the player have something in their hand
        if (style.isShowTooltipsWithItemInHand() || getMenu().getCarried().isEmpty()) {
            RepoSlot<T> repoSlot = RepoSlot.tryCast(repo, this.hoveredSlot);

            if (repoSlot != null) {
                GridInventoryEntry<T> entry = repoSlot.getEntry();
                if (entry != null) {
                    this.renderGridInventoryEntryTooltip(poseStack, entry, x, y);
                }
                return;
            }
        }

        super.renderTooltip(poseStack, x, y);
    }

    protected void renderGridInventoryEntryTooltip(PoseStack poseStack, GridInventoryEntry<T> entry, int x, int y) {
        final int bigNumber = AEConfig.instance().isUseLargeFonts() ? 999 : 9999;

        ItemStack stack = entry.getStack().asItemStackRepresentation();

        final List<Component> currentToolTip = this.getTooltipFromItem(stack);

        long storedAmount = entry.getStoredAmount();
        if (storedAmount > bigNumber || storedAmount > 1 && stack.isDamaged()) {
            final String formattedAmount = NumberFormat.getNumberInstance(Locale.US)
                    .format(storedAmount);
            currentToolTip
                    .add(ButtonToolTips.ItemsStored.text(formattedAmount).withStyle(ChatFormatting.GRAY));
        }

        long requestableAmount = entry.getRequestableAmount();
        if (requestableAmount > 0) {
            final String formattedAmount = NumberFormat.getNumberInstance(Locale.US)
                    .format(requestableAmount);
            currentToolTip.add(ButtonToolTips.ItemsRequestable.text(formattedAmount));
        }

        // TODO: Should also list craftable status
        if (Minecraft.getInstance().options.advancedItemTooltips) {
            currentToolTip
                    .add(new TextComponent("Serial: " + entry.getSerial()).withStyle(ChatFormatting.DARK_GRAY));
        }

        this.renderComponentTooltip(poseStack, currentToolTip, x, y);
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
        if (character == ' ' && this.searchField.getValue().isEmpty()) {
            return true;
        }

        if (this.isAutoFocus && !this.searchField.isFocused() && isHovered()) {
            this.setInitialFocus(this.searchField);
        }

        if (this.searchField.isFocused() && this.searchField.charTyped(character, p_charTyped_2_)) {
            this.repo.setSearchString(this.searchField.getValue());
            this.repo.updateView();
            this.updateScrollbar();
            return true;
        }

        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int p_keyPressed_3_) {

        Key input = InputConstants.getKey(keyCode, scanCode);

        if (keyCode != GLFW.GLFW_KEY_ESCAPE && !this.checkHotbarKeys(input)) {
            if (AppEngClient.instance().isActionKey(ActionKey.TOGGLE_FOCUS, input)) {
                this.searchField.setFocus(!this.searchField.isFocused());
                if (this.searchField.isFocused()) {
                    this.setFocused(this.searchField);
                }
                return true;
            }

            if (this.searchField.isFocused()) {
                if (keyCode == GLFW.GLFW_KEY_ENTER) {
                    this.searchField.setFocus(false);
                    this.setFocused(null);
                    return true;
                }

                if (this.searchField.keyPressed(keyCode, scanCode, p_keyPressed_3_)) {
                    this.repo.setSearchString(this.searchField.getValue());
                    this.repo.updateView();
                    this.updateScrollbar();
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
        return isHovering(0, 0, this.imageWidth, this.imageHeight, currentMouseX, currentMouseY);
    }

    @Override
    public void containerTick() {
        this.repo.setPower(this.menu.isPowered());

        if (this.supportsViewCells) {
            List<ItemStack> viewCells = this.menu.getViewCells();
            if (!this.currentViewCells.equals(viewCells)) {
                this.currentViewCells.clear();
                this.currentViewCells.addAll(viewCells);
                this.repo.setPartitionList(createPartitionList(viewCells));
            }
        }

        super.containerTick();
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

    private void reinitalize() {
        new ArrayList<>(this.children()).forEach(this::removeWidget);
        this.init();
    }
}
