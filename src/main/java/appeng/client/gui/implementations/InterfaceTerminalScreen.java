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

package appeng.client.gui.implementations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import com.google.common.collect.HashMultimap;
import com.mojang.blaze3d.matrix.MatrixStack;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

import appeng.api.config.Settings;
import appeng.api.config.TerminalStyle;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.client.ActionKey;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.widgets.AETextField;
import appeng.client.gui.widgets.Scrollbar;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.client.me.ClientDCInternalInv;
import appeng.client.me.SlotDisconnected;
import appeng.container.implementations.InterfaceTerminalContainer;
import appeng.container.slot.AppEngSlot;
import appeng.core.AEConfig;
import appeng.core.Api;
import appeng.core.AppEng;
import appeng.core.localization.GuiText;
import appeng.util.Platform;

public class InterfaceTerminalScreen extends AEBaseScreen<InterfaceTerminalContainer> {

    private final HashMap<Long, ClientDCInternalInv> byId = new HashMap<>();
    private final HashMultimap<String, ClientDCInternalInv> byName = HashMultimap.create();
    private final ArrayList<String> names = new ArrayList<>();
    private final ArrayList<Object> lines = new ArrayList<>();

    private final Map<String, Set<Object>> cachedSearches = new WeakHashMap<>();

    private boolean refreshList = false;
    private AETextField searchField;

    /**
     * Height of a table-row in pixels.
     */
    private static final int ROW_HEIGHT = 18;

    // Bounding boxes of key areas in the UI texture.
    // The upper part of the UI, anything above the scrollable area (incl. its top border)
    private final Rectangle2d HEADER_BBOX = new Rectangle2d(0, 0, 195, 18);
    // Background for a row in the scroll-box, including the left and right area of the dialog, plus scrollbar
    private final Rectangle2d ROW_BG_BBOX = new Rectangle2d(0, 18, 195, ROW_HEIGHT);
    // Covers one row of slots representing an interface inventory
    private final Rectangle2d ROW_SLOT_BBOX = new Rectangle2d(7, 87, 162, ROW_HEIGHT);
    // This is the lower part of the UI, anything below the scrollable area (incl. its bottom border)
    private final Rectangle2d FOOTER_BBOX = new Rectangle2d(0, 72, 195, 98);
    private int numLines = 0;

    public InterfaceTerminalScreen(InterfaceTerminalContainer container, PlayerInventory playerInventory,
            ITextComponent title) {
        super(container, playerInventory, title);
        final Scrollbar scrollbar = new Scrollbar();
        this.setScrollBar(scrollbar);
        this.xSize = 195;
    }

    @Override
    public void init() {
        // Decide on number of rows.
        TerminalStyle terminalStyle = AEConfig.instance().getTerminalStyle();
        int maxLines = terminalStyle == TerminalStyle.SMALL ? 6 : Integer.MAX_VALUE;
        this.numLines = (this.height - 115) / ROW_HEIGHT; // 97 (footer size) + 17 (header size) + ...1?
        this.numLines = MathHelper.clamp(this.numLines, 3, maxLines);
        // Render inventory in correct place.
        this.ySize = 115 + this.numLines * ROW_HEIGHT; // 17 (header size) + 97 (footer size) + all the lines

        super.init();
        this.searchField = new AETextField(this.font, this.guiLeft + 104, this.guiTop + 4, 65, 12);
        this.searchField.setEnableBackgroundDrawing(false);
        this.searchField.setMaxStringLength(25);
        this.searchField.setTextColor(0xFFFFFF);
        this.searchField.setVisible(true);
        this.searchField.setResponder(str -> this.refreshList());
        this.addListener(this.searchField);
        this.changeFocus(true);

        // Add a terminalstyle button
        int offset = this.guiTop + 8;
        this.addButton(new SettingToggleButton<>(this.guiLeft - 18, offset, Settings.TERMINAL_STYLE, terminalStyle,
                this::toggleTerminalStyle));

        // Reposition player inventory slots.
        for (final Slot s : this.container.inventorySlots) {
            if (s instanceof AppEngSlot) {
                // The first slot must be positioned 83 pixels from the bottom of the dialog (see the dialog PNG)
                s.yPos = ((AppEngSlot) s).getY() + this.ySize - 83;
            }
        }

        // numLines may have changed, recalculate scroll bar.
        this.resetScrollbar();
    }

    @Override
    public void drawFG(MatrixStack matrixStack, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY) {
        final int TEXT_X_OFFSET = 10;
        final int INV_TEXT_Y_OFFSET = 3;
        final int MAIN_TEXT_Y_OFFSET = 6;
        this.font.drawString(matrixStack, this.getGuiDisplayName(GuiText.InterfaceTerminal.text()).getString(), 8, 6,
                COLOR_DARK_GRAY);

        this.container.inventorySlots.removeIf(slot -> slot instanceof SlotDisconnected);

        final int scrollLevel = this.getScrollBar().getCurrentScroll();
        int i = 0;
        for (; i < this.numLines; ++i) {
            if (scrollLevel + i < this.lines.size()) {
                final Object lineObj = this.lines.get(scrollLevel + i);
                if (lineObj instanceof ClientDCInternalInv) {
                    // Note: We have to shift everything after the header up by 1 to avoid black line duplication.
                    final ClientDCInternalInv inv = (ClientDCInternalInv) lineObj;
                    for (int z = 0; z < inv.getInventory().getSlots(); z++) {
                        this.container.inventorySlots
                                .add(new SlotDisconnected(inv, z, z * 18 + 8, (i + 1) * ROW_HEIGHT));
                    }
                } else if (lineObj instanceof String) {
                    String name = (String) lineObj;
                    final int rows = this.byName.get(name).size();
                    if (rows > 1) {
                        name = name + " (" + rows + ')';
                    }

                    while (name.length() > 2 && this.font.getStringWidth(name) > 155) {
                        name = name.substring(0, name.length() - 1);
                    }

                    this.font.drawString(matrixStack, name, TEXT_X_OFFSET, MAIN_TEXT_Y_OFFSET + 17 + i * 18,
                            COLOR_DARK_GRAY);
                }
            }
        }

        this.font.drawString(matrixStack, GuiText.inventory.text().getString(), TEXT_X_OFFSET,
                INV_TEXT_Y_OFFSET + 17 + i * ROW_HEIGHT, COLOR_DARK_GRAY);
    }

    @Override
    public boolean mouseClicked(final double xCoord, final double yCoord, final int btn) {
        if (this.searchField.mouseClicked(xCoord, yCoord, btn)) {
            return true;
        }

        if (btn == 1 && this.searchField.isMouseOver(xCoord, yCoord)) {
            this.searchField.setText("");
            return true;
        }

        return super.mouseClicked(xCoord, yCoord, btn);
    }

    @Override
    public void drawBG(MatrixStack matrixStack, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY, float partialTicks) {
        this.bindTexture("guis/interfaceterminal.png");

        // Draw the top of the dialog
        blit(matrixStack, offsetX, offsetY, HEADER_BBOX);

        final int scrollLevel = this.getScrollBar().getCurrentScroll();
        boolean isInvLine;

        int currentY = offsetY + 17;

        // Draw the footer now so slots will draw on top of it
        blit(matrixStack, offsetX, currentY + this.numLines * ROW_HEIGHT - 1, FOOTER_BBOX);

        for (int i = 0; i < this.numLines; ++i) {
            // Draw the dialog background for this row
            // Skip 1 pixel for the first row in order to not over-draw on the top scrollbox border,
            // and do the same but for the bottom border on the last row
            boolean firstLine = i == 0;
            boolean lastLine = i == this.numLines - 1;
            int firstAdj = firstLine ? 1 : 0;
            int lastAdj = lastLine ? 1 : 0;
            blit(matrixStack, offsetX, currentY + firstAdj,
                    ROW_BG_BBOX.getX(), ROW_BG_BBOX.getY(),
                    ROW_BG_BBOX.getWidth(), ROW_BG_BBOX.getHeight() - firstAdj - lastAdj);

            // Draw the background for the slots in an inventory row
            isInvLine = false;
            if (scrollLevel + i < this.lines.size()) {
                final Object lineObj = this.lines.get(scrollLevel + i);
                isInvLine = lineObj instanceof ClientDCInternalInv;
            }
            if (isInvLine) {
                blit(matrixStack, offsetX + 7, currentY, ROW_SLOT_BBOX);
            }

            currentY += ROW_HEIGHT;
        }

        // Draw search field.
        if (this.searchField != null) {
            this.searchField.render(matrixStack, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public boolean charTyped(char character, int key) {
        if (character == ' ' && this.searchField.getText().isEmpty()) {
            return true;
        }
        return super.charTyped(character, key);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int p_keyPressed_3_) {

        InputMappings.Input input = InputMappings.getInputByCode(keyCode, scanCode);

        if (keyCode != GLFW.GLFW_KEY_ESCAPE) {
            if (AppEng.proxy.isActionKey(ActionKey.TOGGLE_FOCUS, input)) {
                this.searchField.setFocused2(!this.searchField.isFocused());
                return true;
            }

            // Forward keypresses to the search field
            if (this.searchField.isFocused()) {
                if (keyCode == GLFW.GLFW_KEY_ENTER) {
                    this.searchField.setFocused2(false);
                    return true;
                }

                this.searchField.keyPressed(keyCode, scanCode, p_keyPressed_3_);

                // We need to swallow key presses if the field is focused because typing 'e'
                // would otherwise close the screen
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, p_keyPressed_3_);
    }

    public void postUpdate(final CompoundNBT in) {
        if (in.getBoolean("clear")) {
            this.byId.clear();
            this.refreshList = true;
        }

        for (final Object oKey : in.keySet()) {
            final String key = (String) oKey;
            if (key.startsWith("=")) {
                try {
                    final long id = Long.parseLong(key.substring(1), Character.MAX_RADIX);
                    final CompoundNBT invData = in.getCompound(key);
                    ITextComponent un = ITextComponent.Serializer.getComponentFromJson(invData.getString("un"));
                    final ClientDCInternalInv current = this.getById(id, invData.getLong("sortBy"), un);

                    for (int x = 0; x < current.getInventory().getSlots(); x++) {
                        final String which = Integer.toString(x);
                        if (invData.contains(which)) {
                            current.getInventory().setStackInSlot(x, ItemStack.read(invData.getCompound(which)));
                        }
                    }
                } catch (final NumberFormatException ignored) {
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

    /**
     * Rebuilds the list of interfaces.
     * <p>
     * Respects a search term if present (ignores case) and adding only matching patterns.
     */
    private void refreshList() {
        this.byName.clear();

        final String searchFilterLowerCase = this.searchField.getText().toLowerCase();

        final Set<Object> cachedSearch = this.getCacheForSearchTerm(searchFilterLowerCase);
        final boolean rebuild = cachedSearch.isEmpty();

        for (final ClientDCInternalInv entry : this.byId.values()) {
            // ignore inventory if not doing a full rebuild or cache already marks it as
            // miss.
            if (!rebuild && !cachedSearch.contains(entry)) {
                continue;
            }

            // Shortcut to skip any filter if search term is ""/empty
            boolean found = searchFilterLowerCase.isEmpty();

            // Search if the current inventory holds a pattern containing the search term.
            if (!found) {
                for (final ItemStack itemStack : entry.getInventory()) {
                    found = this.itemStackMatchesSearchTerm(itemStack, searchFilterLowerCase);
                    if (found) {
                        break;
                    }
                }
            }

            // if found, filter skipped or machine name matching the search term, add it
            if (found || entry.getSearchName().contains(searchFilterLowerCase)) {
                this.byName.put(entry.getFormattedName(), entry);
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

        for (final String n : this.names) {
            this.lines.add(n);

            List<ClientDCInternalInv> clientInventories = new ArrayList<>(this.byName.get(n));

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
        Scrollbar bar = this.getScrollBar();
        bar.setLeft(175).setTop(18).setHeight(this.numLines * 18 - 2);
        bar.setRange(0, this.lines.size() - this.numLines, 2);
    }

    private boolean itemStackMatchesSearchTerm(final ItemStack itemStack, final String searchTerm) {
        if (itemStack.isEmpty()) {
            return false;
        }

        final CompoundNBT encodedValue = itemStack.getTag();

        if (encodedValue == null) {
            return false;
        }

        // Potential later use to filter by input
        // ListNBT inTag = encodedValue.getTagList( "in", 10 );
        final ListNBT outTag = encodedValue.getList("out", 10);

        for (int i = 0; i < outTag.size(); i++) {

            final ItemStack parsedItemStack = ItemStack.read(outTag.getCompound(i));
            if (!parsedItemStack.isEmpty()) {
                final String displayName = Platform.getItemDisplayName(Api.instance().storage()
                        .getStorageChannel(IItemStorageChannel.class).createStack(parsedItemStack)).getString()
                        .toLowerCase();
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
    private Set<Object> getCacheForSearchTerm(final String searchTerm) {
        if (!this.cachedSearches.containsKey(searchTerm)) {
            this.cachedSearches.put(searchTerm, new HashSet<>());
        }

        final Set<Object> cache = this.cachedSearches.get(searchTerm);

        if (cache.isEmpty() && searchTerm.length() > 1) {
            cache.addAll(this.getCacheForSearchTerm(searchTerm.substring(0, searchTerm.length() - 1)));
            return cache;
        }

        return cache;
    }

    private void reinitialize() {
        this.children.removeAll(this.buttons);
        this.buttons.clear();
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

    private ClientDCInternalInv getById(final long id, final long sortBy, final ITextComponent name) {
        ClientDCInternalInv o = this.byId.get(id);

        if (o == null) {
            this.byId.put(id, o = new ClientDCInternalInv(9, id, sortBy, name));
            this.refreshList = true;
        }

        return o;
    }

    // A version of blit that lets us pass a source rectangle
    private void blit(MatrixStack matrixStack, int offsetX, int offsetY, Rectangle2d srcRect) {
        blit(matrixStack, offsetX, offsetY, srcRect.getX(), srcRect.getY(), srcRect.getWidth(), srcRect.getHeight());
    }

}
