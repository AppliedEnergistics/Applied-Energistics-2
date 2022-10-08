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

package appeng.client.gui.me.interfaceterminal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import com.google.common.collect.HashMultimap;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Settings;
import appeng.api.config.ShowPatternProviders;
import appeng.api.config.TerminalStyle;
import appeng.api.stacks.AEItemKey;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.PaletteColor;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AETextField;
import appeng.client.gui.widgets.Scrollbar;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.InventoryActionPacket;
import appeng.helpers.InventoryAction;
import appeng.helpers.iface.PatternProviderLogic;
import appeng.menu.implementations.InterfaceTerminalMenu;

public class InterfaceTerminalScreen<C extends InterfaceTerminalMenu> extends AEBaseScreen<C> {

    private static final int GUI_WIDTH = 195;

    private static final int GUI_PADDING_X = 8;
    private static final int GUI_PADDING_Y = 6;

    private static final int GUI_HEADER_HEIGHT = 17;
    private static final int GUI_FOOTER_HEIGHT = 97;

    /**
     * Additional margin in pixel for a text row inside the scrolling box.
     */
    private static final int INTERFACE_NAME_MARGIN_X = 2;

    /**
     * The maximum length for the string of a text row in pixel.
     */
    private static final int TEXT_MAX_WIDTH = 155;

    /**
     * Height of a table-row in pixels.
     */
    private static final int ROW_HEIGHT = 18;

    /**
     * Number of rows for a normal terminal (not tall)
     */
    private static final int DEFAULT_ROW_COUNT = 6;
    /**
     * Minimum rows for a tall terminal. Should prevent some strange aspect ratios from not displaying any rows.
     */
    private static final int MIN_ROW_COUNT = 3;

    /**
     * Size of a slot in both x and y dimensions in pixel, most likely always the same as ROW_HEIGHT.
     */
    private static final int SLOT_SIZE = ROW_HEIGHT;

    // Bounding boxes of key areas in the UI texture.
    // The upper part of the UI, anything above the scrollable area (incl. its top border)
    private static final Rect2i HEADER_BBOX = new Rect2i(0, 0, GUI_WIDTH, GUI_HEADER_HEIGHT);
    // Background for a text row in the scroll-box.
    // Spans across the whole texture including the right and left borders including the scrollbar.
    // Covers separate textures for the top, middle and bottoms rows for more customization.
    private static final Rect2i ROW_TEXT_TOP_BBOX = new Rect2i(0, 17, GUI_WIDTH, ROW_HEIGHT);
    private static final Rect2i ROW_TEXT_MIDDLE_BBOX = new Rect2i(0, 53, GUI_WIDTH, ROW_HEIGHT);
    private static final Rect2i ROW_TEXT_BOTTOM_BBOX = new Rect2i(0, 89, GUI_WIDTH, ROW_HEIGHT);
    // Background for a inventory row in the scroll-box.
    // Spans across the whole texture including the right and left borders including the scrollbar.
    // Covers separate textures for the top, middle and bottoms rows for more customization.
    private static final Rect2i ROW_INVENTORY_TOP_BBOX = new Rect2i(0, 35, GUI_WIDTH, ROW_HEIGHT);
    private static final Rect2i ROW_INVENTORY_MIDDLE_BBOX = new Rect2i(0, 71, GUI_WIDTH, ROW_HEIGHT);
    private static final Rect2i ROW_INVENTORY_BOTTOM_BBOX = new Rect2i(0, 107, GUI_WIDTH, ROW_HEIGHT);
    // This is the lower part of the UI, anything below the scrollable area (incl. its bottom border)
    private static final Rect2i FOOTER_BBOX = new Rect2i(0, 125, GUI_WIDTH, GUI_FOOTER_HEIGHT);

    private final HashMap<Long, InterfaceRecord> byId = new HashMap<>();
    // Used to show multiple interfaces with the same name under a single header
    private final HashMultimap<String, InterfaceRecord> byName = HashMultimap.create();
    private final ArrayList<String> names = new ArrayList<>();
    private final ArrayList<Object> lines = new ArrayList<>();

    private final Map<String, Set<Object>> cachedSearches = new WeakHashMap<>();
    private final Scrollbar scrollbar;
    private final AETextField searchField;

    private boolean refreshList = false;
    private int numLines = 0;

    private final ServerSettingToggleButton showPatternProviders;

    public InterfaceTerminalScreen(C menu, Inventory playerInventory,
            Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.scrollbar = widgets.addScrollBar("scrollbar");
        this.imageWidth = GUI_WIDTH;

        // Add a terminalstyle button
        TerminalStyle terminalStyle = AEConfig.instance().getTerminalStyle();
        this.addToLeftToolbar(
                new SettingToggleButton<>(Settings.TERMINAL_STYLE, terminalStyle, this::toggleTerminalStyle));

        showPatternProviders = new ServerSettingToggleButton(Settings.TERMINAL_SHOW_PATTERN_PROVIDERS,
                ShowPatternProviders.VISIBLE);

        this.addToLeftToolbar(
                showPatternProviders);

        this.searchField = widgets.addTextField("search");
        this.searchField.setResponder(str -> this.refreshList());
        this.searchField.setPlaceholder(GuiText.SearchPlaceholder.text());
    }

    @Override
    public void init() {
        // Decide on number of rows.
        TerminalStyle terminalStyle = AEConfig.instance().getTerminalStyle();
        int maxLines = terminalStyle == TerminalStyle.SMALL ? DEFAULT_ROW_COUNT : Integer.MAX_VALUE;
        this.numLines = (this.height - GUI_HEADER_HEIGHT - GUI_FOOTER_HEIGHT) / ROW_HEIGHT;
        this.numLines = Mth.clamp(this.numLines, MIN_ROW_COUNT, maxLines);
        // Render inventory in correct place.
        this.imageHeight = GUI_HEADER_HEIGHT + GUI_FOOTER_HEIGHT + this.numLines * ROW_HEIGHT;

        super.init();

        // Auto focus search field
        this.setInitialFocus(this.searchField);

        // numLines may have changed, recalculate scroll bar.
        this.resetScrollbar();
    }

    @Override
    public void drawFG(PoseStack poseStack, int offsetX, int offsetY, int mouseX,
            int mouseY) {

        this.menu.slots.removeIf(slot -> slot instanceof InterfaceSlot);

        int textColor = style.getColor(PaletteColor.DEFAULT_TEXT_COLOR).toARGB();

        final int scrollLevel = scrollbar.getCurrentScroll();
        int i = 0;
        for (; i < this.numLines; ++i) {
            if (scrollLevel + i < this.lines.size()) {
                final Object lineObj = this.lines.get(scrollLevel + i);
                if (lineObj instanceof InterfaceRecord inv) {
                    // Note: We have to shift everything after the header up by 1 to avoid black line duplication.
                    for (int z = 0; z < inv.getInventory().size(); z++) {
                        this.menu.slots
                                .add(new InterfaceSlot(inv, z, z * SLOT_SIZE + GUI_PADDING_X, (i + 1) * SLOT_SIZE));
                    }
                } else if (lineObj instanceof String name) {
                    final int rows = this.byName.get(name).size();
                    if (rows > 1) {
                        name = name + " (" + rows + ')';
                    }

                    name = this.font.plainSubstrByWidth(name, TEXT_MAX_WIDTH, true);

                    this.font.draw(poseStack, name, GUI_PADDING_X + INTERFACE_NAME_MARGIN_X,
                            GUI_PADDING_Y + GUI_HEADER_HEIGHT + i * ROW_HEIGHT, textColor);
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double xCoord, double yCoord, int btn) {
        if (btn == 1 && this.searchField.isMouseOver(xCoord, yCoord)) {
            this.searchField.setValue("");
            // Don't return immediately to also grab focus.
        }

        return super.mouseClicked(xCoord, yCoord, btn);
    }

    @Override
    protected void slotClicked(Slot slot, int slotIdx, int mouseButton, ClickType clickType) {
        if (slot instanceof InterfaceSlot) {
            InventoryAction action = null;

            switch (clickType) {
                case PICKUP: // pickup / set-down.
                    action = mouseButton == 1 ? InventoryAction.SPLIT_OR_PLACE_SINGLE
                            : InventoryAction.PICKUP_OR_SET_DOWN;
                    break;
                case QUICK_MOVE:
                    action = mouseButton == 1 ? InventoryAction.PICKUP_SINGLE : InventoryAction.SHIFT_CLICK;
                    break;

                case CLONE: // creative dupe:
                    if (getPlayer().getAbilities().instabuild) {
                        action = InventoryAction.CREATIVE_DUPLICATE;
                    }

                    break;

                default:
                case THROW: // drop item:
            }

            if (action != null) {
                InterfaceSlot machineSlot = (InterfaceSlot) slot;
                final InventoryActionPacket p = new InventoryActionPacket(action, machineSlot.getSlotIndex(),
                        machineSlot.getMachineInv().getServerId());
                NetworkHandler.instance().sendToServer(p);
            }

            return;
        }

        super.slotClicked(slot, slotIdx, mouseButton, clickType);
    }

    @Override
    public void drawBG(PoseStack poseStack, int offsetX, int offsetY, int mouseX,
            int mouseY, float partialTicks) {
        this.bindTexture("guis/interfaceterminal.png");

        // Draw the top of the dialog
        blit(poseStack, offsetX, offsetY, HEADER_BBOX);

        final int scrollLevel = scrollbar.getCurrentScroll();
        boolean isInvLine;

        int currentY = offsetY + GUI_HEADER_HEIGHT;

        // Draw the footer now so slots will draw on top of it
        blit(poseStack, offsetX, currentY + this.numLines * ROW_HEIGHT, FOOTER_BBOX);

        for (int i = 0; i < this.numLines; ++i) {
            // Draw the dialog background for this row
            // Skip 1 pixel for the first row in order to not over-draw on the top scrollbox border,
            // and do the same but for the bottom border on the last row
            boolean firstLine = i == 0;
            boolean lastLine = i == this.numLines - 1;

            // Draw the background for the slots in an inventory row
            isInvLine = false;
            if (scrollLevel + i < this.lines.size()) {
                final Object lineObj = this.lines.get(scrollLevel + i);
                isInvLine = lineObj instanceof InterfaceRecord;
            }

            Rect2i bbox = selectRowBackgroundBox(isInvLine, firstLine, lastLine);
            blit(poseStack, offsetX, currentY, bbox);

            currentY += ROW_HEIGHT;
        }
    }

    private Rect2i selectRowBackgroundBox(boolean isInvLine, boolean firstLine, boolean lastLine) {
        if (isInvLine) {
            if (firstLine) {
                return ROW_INVENTORY_TOP_BBOX;
            } else if (lastLine) {
                return ROW_INVENTORY_BOTTOM_BBOX;
            } else {
                return ROW_INVENTORY_MIDDLE_BBOX;
            }
        } else if (firstLine) {
            return ROW_TEXT_TOP_BBOX;
        } else if (lastLine) {
            return ROW_TEXT_BOTTOM_BBOX;
        } else {
            return ROW_TEXT_MIDDLE_BBOX;
        }
    }

    @Override
    public boolean charTyped(char character, int key) {
        if (character == ' ' && this.searchField.getValue().isEmpty()) {
            return true;
        }
        return super.charTyped(character, key);
    }

    public void postInventoryUpdate(boolean clearExistingData, long inventoryId, CompoundTag invData) {
        if (clearExistingData) {
            this.byId.clear();
            this.refreshList = true;
        } else {
            var un = Serializer.fromJson(invData.getString("un"));
            var current = this.getById(inventoryId, invData.getLong("sortBy"), un);

            for (int x = 0; x < current.getInventory().size(); x++) {
                final String which = Integer.toString(x);
                if (invData.contains(which)) {
                    current.getInventory().setItemDirect(x, ItemStack.of(invData.getCompound(which)));
                }
            }
        }

        if (this.refreshList) {
            this.refreshList = false;
            // invalid caches on refresh
            this.cachedSearches.clear();
            this.refreshList();
        }
    }

    @Override
    public void updateBeforeRender() {
        this.showPatternProviders.set(this.menu.getShownProviders());
    }

    /**
     * Rebuilds the list of interfaces.
     * <p>
     * Respects a search term if present (ignores case) and adding only matching patterns.
     */
    private void refreshList() {
        this.byName.clear();

        final String searchFilterLowerCase = this.searchField.getValue().toLowerCase();

        final Set<Object> cachedSearch = this.getCacheForSearchTerm(searchFilterLowerCase);
        final boolean rebuild = cachedSearch.isEmpty();

        for (InterfaceRecord entry : this.byId.values()) {
            // ignore inventory if not doing a full rebuild or cache already marks it as miss.
            if (!rebuild && !cachedSearch.contains(entry)) {
                continue;
            }

            // Shortcut to skip any filter if search term is ""/empty
            boolean found = searchFilterLowerCase.isEmpty();

            // Search if the current inventory holds a pattern containing the search term.
            if (!found) {
                for (ItemStack itemStack : entry.getInventory()) {
                    found = this.itemStackMatchesSearchTerm(itemStack, searchFilterLowerCase);
                    if (found) {
                        break;
                    }
                }
            }

            // if found, filter skipped or machine name matching the search term, add it
            if (found || entry.getSearchName().contains(searchFilterLowerCase)) {
                this.byName.put(entry.getDisplayName(), entry);
                cachedSearch.add(entry);
            } else {
                cachedSearch.remove(entry);
            }
        }

        this.names.clear();
        this.names.addAll(this.byName.keySet());

        Collections.sort(this.names);

        this.lines.clear();
        this.lines.ensureCapacity(this.getMaxRows());

        for (String n : this.names) {
            this.lines.add(n);

            List<InterfaceRecord> clientInventories = new ArrayList<>(this.byName.get(n));

            Collections.sort(clientInventories);
            this.lines.addAll(clientInventories);
        }

        // lines may have changed - recalculate scroll bar.
        this.resetScrollbar();
    }

    /**
     * Should be called whenever this.lines.size() or this.numLines changes.
     */
    private void resetScrollbar() {
        // Needs to take the border into account, so offset for 1 px on the top and bottom.
        scrollbar.setHeight(this.numLines * ROW_HEIGHT - 2);
        scrollbar.setRange(0, this.lines.size() - this.numLines, 2);
    }

    private boolean itemStackMatchesSearchTerm(ItemStack itemStack, String searchTerm) {
        if (itemStack.isEmpty()) {
            return false;
        }

        final CompoundTag encodedValue = itemStack.getTag();

        if (encodedValue == null) {
            return false;
        }

        // Potential later use to filter by input
        // ListNBT inTag = encodedValue.getTagList( "in", 10 );
        final ListTag outTag = encodedValue.getList("out", 10);

        for (int i = 0; i < outTag.size(); i++) {

            var parsedItemStack = ItemStack.of(outTag.getCompound(i));
            var itemKey = AEItemKey.of(parsedItemStack);
            if (itemKey != null) {
                var displayName = itemKey.getDisplayName().getString().toLowerCase();
                if (displayName.contains(searchTerm)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Tries to retrieve a cache for a with search term as keyword.
     * <p>
     * If this cache should be empty, it will populate it with an earlier cache if available or at least the cache for
     * the empty string.
     *
     * @param searchTerm the corresponding search
     * @return a Set matching a superset of the search term
     */
    private Set<Object> getCacheForSearchTerm(String searchTerm) {
        if (!this.cachedSearches.containsKey(searchTerm)) {
            this.cachedSearches.put(searchTerm, new HashSet<>());
        }

        final Set<Object> cache = this.cachedSearches.get(searchTerm);

        if (cache.isEmpty() && searchTerm.length() > 1) {
            cache.addAll(this.getCacheForSearchTerm(searchTerm.substring(0, searchTerm.length() - 1)));
        }

        return cache;
    }

    private void reinitialize() {
        this.children().removeAll(this.renderables);
        this.renderables.clear();
        this.init();
    }

    private void toggleTerminalStyle(SettingToggleButton<TerminalStyle> btn, boolean backwards) {
        TerminalStyle next = btn.getNextValue(backwards);
        AEConfig.instance().setTerminalStyle(next);
        btn.set(next);
        this.reinitialize();
    }

    /**
     * The max amount of unique names and each inv row. Not affected by the filtering.
     *
     * @return max amount of unique names and each inv row
     */
    private int getMaxRows() {
        return this.names.size() + this.byId.size();
    }

    private InterfaceRecord getById(long id, long sortBy, Component name) {
        InterfaceRecord o = this.byId.get(id);

        if (o == null) {
            this.byId.put(id,
                    o = new InterfaceRecord(id, PatternProviderLogic.NUMBER_OF_PATTERN_SLOTS, sortBy, name));
            this.refreshList = true;
        }

        return o;
    }

    /**
     * A version of blit that lets us pass a source rectangle
     *
     * @see GuiComponent#blit(PoseStack, int, int, int, int, int, int)
     */
    private void blit(PoseStack poseStack, int offsetX, int offsetY, Rect2i srcRect) {
        blit(poseStack, offsetX, offsetY, srcRect.getX(), srcRect.getY(), srcRect.getWidth(), srcRect.getHeight());
    }

}
