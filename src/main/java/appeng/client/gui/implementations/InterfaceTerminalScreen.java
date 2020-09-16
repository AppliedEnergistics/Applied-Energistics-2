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
import com.mojang.blaze3d.systems.RenderSystem;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.text.Text;

import alexiil.mc.lib.attributes.Simulation;

import appeng.api.storage.channels.IItemStorageChannel;
import appeng.client.ActionKey;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.widgets.AETextField;
import appeng.client.gui.widgets.Scrollbar;
import appeng.client.me.ClientDCInternalInv;
import appeng.client.me.SlotDisconnected;
import appeng.container.implementations.InterfaceTerminalContainer;
import appeng.core.Api;
import appeng.core.AppEng;
import appeng.core.localization.GuiText;
import appeng.util.Platform;

public class InterfaceTerminalScreen extends AEBaseScreen<InterfaceTerminalContainer> {

    private static final int LINES_ON_PAGE = 6;

    private final HashMap<Long, ClientDCInternalInv> byId = new HashMap<>();
    private final HashMultimap<String, ClientDCInternalInv> byName = HashMultimap.create();
    private final ArrayList<String> names = new ArrayList<>();
    private final ArrayList<Object> lines = new ArrayList<>();

    private final Map<String, Set<Object>> cachedSearches = new WeakHashMap<>();

    private boolean refreshList = false;
    private AETextField searchField;

    public InterfaceTerminalScreen(InterfaceTerminalContainer container, PlayerInventory playerInventory, Text title) {
        super(container, playerInventory, title);
        this.setScrollBar(new Scrollbar().setLeft(175).setTop(18).setHeight(106));
        this.backgroundWidth = 195;
        this.backgroundHeight = 222;
    }

    @Override
    public void init() {
        super.init();

        this.searchField = new AETextField(this.textRenderer, this.x + 104, this.y + 4, 65, 12);
        this.searchField.setHasBorder(false);
        this.searchField.setMaxLength(25);
        this.searchField.setEditableColor(0xFFFFFF);
        this.searchField.setVisible(true);
        this.searchField.setChangedListener(str -> this.refreshList());
        this.addChild(this.searchField);
        this.changeFocus(true);
    }

    @Override
    public void drawFG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.textRenderer.draw(matrices, this.getGuiDisplayName(GuiText.InterfaceTerminal.text()), 8, 6, 4210752);
        this.textRenderer.draw(matrices, GuiText.inventory.text(), 8, this.backgroundHeight - 96 + 3, 4210752);

        final int ex = this.getScrollBar().getCurrentScroll();

        this.handler.slots.removeIf(slot -> slot instanceof SlotDisconnected);

        int offset = 17;
        for (int x = 0; x < LINES_ON_PAGE && ex + x < this.lines.size(); x++) {
            final Object lineObj = this.lines.get(ex + x);
            if (lineObj instanceof ClientDCInternalInv) {
                final ClientDCInternalInv inv = (ClientDCInternalInv) lineObj;
                for (int z = 0; z < inv.getInventory().getSlotCount(); z++) {
                    this.handler.slots.add(new SlotDisconnected(inv, z, z * 18 + 8, 1 + offset));
                }
            } else if (lineObj instanceof String) {
                String name = (String) lineObj;
                final int rows = this.byName.get(name).size();
                if (rows > 1) {
                    name = name + " (" + rows + ')';
                }

                while (name.length() > 2 && this.textRenderer.getWidth(name) > 155) {
                    name = name.substring(0, name.length() - 1);
                }

                this.textRenderer.draw(matrices, name, 10, 6 + offset, 4210752);
            }
            offset += 18;
        }
    }

    @Override
    public boolean mouseClicked(final double xCoord, final double yCoord, final int btn) {
        if (btn == 1 && this.searchField.isMouseOver(xCoord, yCoord)) {
            this.searchField.setText("");
            return true;
        }

        return super.mouseClicked(xCoord, yCoord, btn);
    }

    @Override
    public void drawBG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX, final int mouseY,
            float partialTicks) {
        this.bindTexture("guis/interfaceterminal.png");
        drawTexture(matrices, offsetX, offsetY, 0, 0, this.backgroundWidth, this.backgroundHeight);

        int offset = 17;
        final int ex = this.getScrollBar().getCurrentScroll();

        for (int x = 0; x < LINES_ON_PAGE && ex + x < this.lines.size(); x++) {
            final Object lineObj = this.lines.get(ex + x);
            if (lineObj instanceof ClientDCInternalInv) {
                final ClientDCInternalInv inv = (ClientDCInternalInv) lineObj;

                RenderSystem.color4f(1, 1, 1, 1);
                final int width = inv.getInventory().getSlotCount() * 18;
                drawTexture(matrices, offsetX + 7, offsetY + offset, 7, 139, width, 18);
            }
            offset += 18;
        }

        if (this.searchField != null) {
            this.searchField.render(matrices, mouseX, mouseY, partialTicks);
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

        if (keyCode != GLFW.GLFW_KEY_ESCAPE) {
            if (AppEng.instance().isActionKey(ActionKey.TOGGLE_FOCUS, keyCode, scanCode)) {
                this.searchField.setFocused(!this.searchField.isFocused());
                return true;
            }

            // Forward keypresses to the search field
            if (this.searchField.isFocused()) {
                if (keyCode == GLFW.GLFW_KEY_ENTER) {
                    this.searchField.setFocused(false);
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

    public void postUpdate(final CompoundTag in) {
        if (in.getBoolean("clear")) {
            this.byId.clear();
            this.refreshList = true;
        }

        for (final Object oKey : in.getKeys()) {
            final String key = (String) oKey;
            if (key.startsWith("=")) {
                try {
                    final long id = Long.parseLong(key.substring(1), Character.MAX_RADIX);
                    final CompoundTag invData = in.getCompound(key);
                    Text un = Text.Serializer.fromJson(invData.getString("un"));
                    final ClientDCInternalInv current = this.getById(id, invData.getLong("sortBy"), un);

                    for (int x = 0; x < current.getInventory().getSlotCount(); x++) {
                        final String which = Integer.toString(x);
                        if (invData.contains(which)) {
                            current.getInventory().setInvStack(x, ItemStack.fromTag(invData.getCompound(which)),
                                    Simulation.ACTION);
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
     * Respects a search term if present (ignores case) and adding only matching
     * patterns.
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
            if (!found && !searchFilterLowerCase.isEmpty()) {
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

        this.getScrollBar().setRange(0, this.lines.size() - LINES_ON_PAGE, 2);
    }

    private boolean itemStackMatchesSearchTerm(final ItemStack itemStack, final String searchTerm) {
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

            final ItemStack parsedItemStack = ItemStack.fromTag(outTag.getCompound(i));
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
     * If this cache should be empty, it will populate it with an earlier cache if
     * available or at least the cache for the empty string.
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

    /**
     * The max amount of unique names and each inv row. Not affected by the
     * filtering.
     *
     * @return max amount of unique names and each inv row
     */
    private int getMaxRows() {
        return this.names.size() + this.byId.size();
    }

    private ClientDCInternalInv getById(final long id, final long sortBy, final Text name) {
        ClientDCInternalInv o = this.byId.get(id);

        if (o == null) {
            this.byId.put(id, o = new ClientDCInternalInv(9, id, sortBy, name));
            this.refreshList = true;
        }

        return o;
    }
}
