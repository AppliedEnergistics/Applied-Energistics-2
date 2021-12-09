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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import appeng.api.client.AEStackRendering;
import appeng.api.client.AmountFormat;
import appeng.api.config.SearchBoxMode;
import appeng.api.config.Setting;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.implementations.blockentities.IMEChest;
import appeng.api.stacks.AEFluidKey;
import appeng.api.storage.AEKeyFilter;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.client.Point;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.TerminalStyle;
import appeng.client.gui.widgets.AETextField;
import appeng.client.gui.widgets.ISortSource;
import appeng.client.gui.widgets.Scrollbar;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.client.gui.widgets.TabButton;
import appeng.client.gui.widgets.UpgradesPanel;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.localization.ButtonToolTips;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;
import appeng.core.sync.packets.MEInteractionPacket;
import appeng.core.sync.packets.SwitchGuisPacket;
import appeng.helpers.InventoryAction;
import appeng.integration.abstraction.JEIFacade;
import appeng.items.storage.ViewCellItem;
import appeng.menu.SlotSemantic;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.me.common.MEMonitorableMenu;
import appeng.menu.me.crafting.CraftingStatusMenu;
import appeng.menu.me.interaction.StackInteractions;
import appeng.util.IConfigManagerListener;
import appeng.util.Platform;
import appeng.util.prioritylist.IPartitionList;

public class MEMonitorableScreen<C extends MEMonitorableMenu>
        extends AEBaseScreen<C> implements ISortSource, IConfigManagerListener {

    private static final int MIN_ROWS = 3;

    private static String memoryText = "";
    private final TerminalStyle style;
    protected final Repo repo;
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

        this.searchField = widgets.addTextField("search");

        this.scrollbar = widgets.addScrollBar("scrollbar");
        this.repo = new Repo(scrollbar, this);
        menu.setClientRepo(this.repo);
        this.repo.setUpdateViewListener(this::updateScrollbar);
        updateScrollbar();

        this.searchField.setResponder(text -> {
            repo.setSearchString(text);
            repo.updateView();
            updateScrollbar();
        });

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

    @Nullable
    protected IPartitionList createPartitionList(List<ItemStack> viewCells) {
        return ViewCellItem.createFilter(AEKeyFilter.none(), viewCells);
    }

    protected void handleGridInventoryEntryMouseClick(@Nullable GridInventoryEntry entry,
            int mouseButton,
            ClickType clickType) {
        if (entry != null) {
            AELog.debug("Clicked on grid inventory entry serial=%s, key=%s", entry.getSerial(), entry.getWhat());
        }

        // Is there an emptying action? If so, send it to the server
        if (mouseButton == 1 && clickType == ClickType.PICKUP && !menu.getCarried().isEmpty()) {
            var emptyingAction = StackInteractions.getEmptyingAction(menu.getCarried());
            if (emptyingAction != null && menu.isKeyVisible(emptyingAction.what())) {
                menu.handleInteraction(-1, InventoryAction.EMPTY_ITEM);
                return;
            }
        }

        if (entry == null) {
            // The only interaction allowed on an empty virtual slot is putting down the currently held item
            if (clickType == ClickType.PICKUP && !getMenu().getCarried().isEmpty()) {
                InventoryAction action = mouseButton == 1 ? InventoryAction.SPLIT_OR_PLACE_SINGLE
                        : InventoryAction.PICKUP_OR_SET_DOWN;
                menu.handleInteraction(-1, action);
            }
            return;
        }

        long serial = entry.getSerial();

        // Move as many items of a single type as possible
        if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_SPACE)) {
            menu.handleInteraction(serial, InventoryAction.MOVE_REGION);
        } else {
            InventoryAction action = null;

            switch (clickType) {
                case PICKUP: // pickup / set-down.
                    action = mouseButton == 1 ? InventoryAction.SPLIT_OR_PLACE_SINGLE
                            : InventoryAction.PICKUP_OR_SET_DOWN;

                    if (action == InventoryAction.PICKUP_OR_SET_DOWN
                            && shouldCraftOnClick(entry)
                            && getMenu().getCarried().isEmpty()) {
                        menu.handleInteraction(serial, InventoryAction.AUTO_CRAFT);
                        return;
                    }

                    break;
                case QUICK_MOVE:
                    action = mouseButton == 1 ? InventoryAction.PICKUP_SINGLE : InventoryAction.SHIFT_CLICK;
                    break;

                case CLONE: // creative dupe:
                    if (entry.isCraftable()) {
                        menu.handleInteraction(serial, InventoryAction.AUTO_CRAFT);
                        return;
                    } else if (getMenu().getPlayer().getAbilities().instabuild) {
                        action = InventoryAction.CREATIVE_DUPLICATE;
                    }
                    break;

                default:
                case THROW: // drop item:
            }

            if (action != null) {
                menu.handleInteraction(serial, action);
            }
        }
    }

    private boolean shouldCraftOnClick(GridInventoryEntry entry) {
        // Always auto-craft when viewing only craftable items
        if (isViewOnlyCraftable()) {
            return true;
        }

        // Otherwise only craft if there are no stored items
        return entry.getStoredAmount() == 0 && entry.isCraftable();
    }

    private void updateScrollbar() {
        scrollbar.setHeight(this.rows * style.getRow().getSrcHeight() - 2);
        int totalRows = (this.repo.size() + getSlotsPerRow() - 1) / getSlotsPerRow();
        scrollbar.setRange(0, totalRows - this.rows, Math.max(1, this.rows / 6));
    }

    private void showCraftingStatus() {
        NetworkHandler.instance().sendToServer(SwitchGuisPacket.openSubMenu(CraftingStatusMenu.TYPE));
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

                slots.add(new RepoSlot(this.repo, repoIndex++, pos.getX(), pos.getY()));
            }
        }

        super.init();

        SearchBoxMode searchMode = AEConfig.instance().getTerminalSearchMode();
        this.isAutoFocus = SearchBoxMode.AUTOSEARCH == searchMode || SearchBoxMode.JEI_AUTOSEARCH == searchMode
                || SearchBoxMode.AUTOSEARCH_KEEP == searchMode || SearchBoxMode.JEI_AUTOSEARCH_KEEP == searchMode;
        final boolean isKeepFilter = SearchBoxMode.AUTOSEARCH_KEEP == searchMode
                || SearchBoxMode.JEI_AUTOSEARCH_KEEP == searchMode || SearchBoxMode.MANUAL_SEARCH_KEEP == searchMode
                || SearchBoxMode.JEI_MANUAL_SEARCH_KEEP == searchMode;
        final boolean isJEIEnabled = SearchBoxMode.JEI_AUTOSEARCH == searchMode
                || SearchBoxMode.JEI_MANUAL_SEARCH == searchMode;

        if (this.isAutoFocus) {
            setInitialFocus(this.searchField);
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

            StackSizeRenderer.renderSizeLabel(font, x - this.leftPos, y - this.topPos,
                    String.valueOf(menu.activeCraftingJobs));
        }
    }

    @Override
    public boolean mouseClicked(final double xCoord, final double yCoord, final int btn) {
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
            if (this.getSlot((int) x, (int) y) instanceof RepoSlot repoSlot) {
                GridInventoryEntry entry = repoSlot.getEntry();
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
        if (slot instanceof RepoSlot repoSlot) {
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

    @Override
    public void renderSlot(PoseStack poseStack, Slot s) {
        if (s instanceof RepoSlot repoSlot) {
            if (!this.repo.hasPower()) {
                fill(poseStack, s.x, s.y, 16 + s.x, 16 + s.y, 0x66111111);
            } else {
                GridInventoryEntry entry = repoSlot.getEntry();
                if (entry != null) {
                    try {
                        AEStackRendering.drawRepresentation(
                                minecraft,
                                poseStack,
                                s.x,
                                s.y,
                                getBlitOffset(),
                                entry.getWhat());
                    } catch (final Exception err) {
                        AELog.warn("[AppEng] AE prevented crash while drawing slot: " + err);
                    }

                    // If a view mode is selected that only shows craftable items, display the "craftable" text
                    // regardless of stack size
                    long storedAmount = entry.getStoredAmount();
                    boolean craftable = entry.isCraftable();
                    var useLargeFonts = AEConfig.instance().isUseLargeFonts();
                    if (craftable && (isViewOnlyCraftable() || storedAmount <= 0)) {
                        var craftLabelText = useLargeFonts ? GuiText.LargeFontCraft.getLocal()
                                : GuiText.SmallFontCraft.getLocal();
                        StackSizeRenderer.renderSizeLabel(this.font, s.x, s.y, craftLabelText);
                    } else {
                        var text = AEStackRendering.formatAmount(entry.getWhat(), storedAmount,
                                useLargeFonts ? AmountFormat.PREVIEW_LARGE_FONT : AmountFormat.PREVIEW_REGULAR);
                        StackSizeRenderer.renderSizeLabel(this.font, s.x, s.y, text, useLargeFonts);
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
        if (this.hoveredSlot instanceof RepoSlot repoSlot) {
            var carried = menu.getCarried();
            if (!carried.isEmpty()) {
                var emptyingAction = StackInteractions.getEmptyingAction(carried);
                if (emptyingAction != null && menu.isKeyVisible(emptyingAction.what())) {
                    renderTooltip(poseStack, List.of(
                            new TextComponent("Left-Click: Store ").append(carried.getHoverName())
                                    .withStyle(ChatFormatting.GRAY).getVisualOrderText(),
                            new TextComponent("Right-Click: Store ").append(emptyingAction.description())
                                    .withStyle(ChatFormatting.GRAY).getVisualOrderText()),
                            x, y);
                    return;
                }

                // TODO: Fill action same way
            }

            // Vanilla doesn't show item tooltips when the player have something in their hand
            if (carried.isEmpty()) {
                GridInventoryEntry entry = repoSlot.getEntry();
                if (entry != null) {
                    renderGridInventoryEntryTooltip(poseStack, entry, x, y);
                }
            }
            return;
        }

        super.renderTooltip(poseStack, x, y);
    }

    protected void renderGridInventoryEntryTooltip(PoseStack poseStack, GridInventoryEntry entry, int x, int y) {
        if (entry.getWhat() instanceof AEFluidKey fluidKey) {
            var what = entry.getWhat();
            String formattedAmount = AEStackRendering.formatAmount(what, entry.getStoredAmount(), AmountFormat.FULL);

            String modName = Platform.formatModName(what.getModId());

            List<Component> list = new ArrayList<>();
            list.add(AEStackRendering.getDisplayName(fluidKey));
            list.add(new TextComponent(formattedAmount));
            list.add(new TextComponent(modName));

            this.renderComponentTooltip(poseStack, list, x, y);
            return;
        }

        final int bigNumber = AEConfig.instance().isUseLargeFonts() ? 999 : 9999;

        ItemStack stack = entry.getWhat().wrapForDisplayOrFilter();

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
    public boolean charTyped(char character, int modifiers) {
        if (character == ' ' && this.searchField.getValue().isEmpty()) {
            return true;
        }

        if (this.isAutoFocus && !this.searchField.isFocused() && isHovered()) {
            this.setInitialFocus(this.searchField);
        }

        return super.charTyped(character, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int p_keyPressed_3_) {
        Key input = InputConstants.getKey(keyCode, scanCode);
        if (this.checkHotbarKeys(input)) {
            return true;
        }

        if (this.searchField.isFocused() && keyCode == GLFW.GLFW_KEY_ENTER) {
            this.searchField.setFocus(false);
            this.setFocused(null);
            return true;
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
        return this.configSrc.getSetting(Settings.SORT_BY);
    }

    @Override
    public SortDir getSortDir() {
        return this.configSrc.getSetting(Settings.SORT_DIRECTION);
    }

    @Override
    public ViewItems getSortDisplay() {
        return this.configSrc.getSetting(Settings.VIEW_MODE);
    }

    @Override
    public void onSettingChanged(IConfigManager manager, Setting<?> setting) {
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
        NetworkHandler.instance().sendToServer(new ConfigValuePacket(btn.getSetting(), next));
        btn.set(next);
    }

    private void reinitalize() {
        new ArrayList<>(this.children()).forEach(this::removeWidget);
        this.init();
    }
}
