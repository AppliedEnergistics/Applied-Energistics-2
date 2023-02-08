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


import appeng.api.AEApi;
import appeng.api.config.ActionItems;
import appeng.api.config.Settings;
import appeng.api.config.TerminalStyle;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiScrollbar;
import appeng.client.gui.widgets.MEGuiTextField;
import appeng.client.me.ClientDCInternalInv;
import appeng.client.me.SlotDisconnected;
import appeng.container.implementations.ContainerInterfaceTerminal;
import appeng.container.slot.AppEngSlot;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import appeng.helpers.DualityInterface;
import appeng.parts.reporting.PartInterfaceTerminal;
import appeng.util.BlockPosUtils;
import appeng.util.Platform;
import com.google.common.collect.HashMultimap;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.Loader;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import static appeng.client.render.BlockPosHighlighter.hilightBlock;
import static appeng.helpers.ItemStackHelper.stackFromNBT;


public class GuiInterfaceTerminal extends AEBaseGui {

    private int rows = 6;

    // TODO: copied from GuiMEMonitorable. It looks not changed, maybe unneeded?
    private final int offsetX = 21;
    private int maxRows = Integer.MAX_VALUE;

    protected int jeiOffset = Loader.isModLoaded("jei") ? 24 : 0;

    private int reservedSpace = 0;

    private final HashMap<Long, ClientDCInternalInv> byId = new HashMap<>();
    private final HashMultimap<String, ClientDCInternalInv> byName = HashMultimap.create();
    private final HashMap<ClientDCInternalInv, BlockPos> blockPosHashMap = new HashMap<>();
    private final HashMap<GuiButton, ClientDCInternalInv> guiButtonHashMap = new HashMap<>();
    private final Map<ClientDCInternalInv, Integer> numUpgradesMap = new HashMap<>();
    private final ArrayList<String> names = new ArrayList<>();
    private final ArrayList<Object> lines = new ArrayList<>();
    private final Set<Object> matchedStacks = new HashSet<>();
    private final Set<ClientDCInternalInv> matchedInterfaces = new HashSet<>();
    private final Map<String, Set<Object>> cachedSearches = new WeakHashMap<>();

    private boolean refreshList = false;
    private MEGuiTextField searchFieldOutputs;
    private MEGuiTextField searchFieldInputs;
    private final PartInterfaceTerminal partInterfaceTerminal;
    private GuiButton guiButtonHide;
    private GuiButton guiButtonNextAssembler;
    private GuiImgButton terminalStyleBox;

    private final HashMap<ClientDCInternalInv, Integer> dimHashMap = new HashMap<>();
    private int drawnRows = 0;

    public GuiInterfaceTerminal(final InventoryPlayer inventoryPlayer, final PartInterfaceTerminal te) {
        super(new ContainerInterfaceTerminal(inventoryPlayer, te));

        this.partInterfaceTerminal = te;
        final GuiScrollbar scrollbar = new GuiScrollbar();
        this.setScrollBar(scrollbar);
        this.xSize = 208;
        this.ySize = 255;
        this.setReservedSpace(82);
    }

    @Override
    public void initGui() {
        this.maxRows = AEConfig.instance()
                .getConfigManager()
                .getSetting(
                        Settings.TERMINAL_STYLE) != TerminalStyle.TALL ? 6 : Integer.MAX_VALUE;

        final int magicNumber = 82 + 14;
        final int extraSpace = this.height - magicNumber - this.reservedSpace;

        this.rows = (int) Math.floor(extraSpace / 18);
        if (this.rows > this.maxRows) {
            this.rows = this.maxRows;
        }

        if (this.rows < 6) {
            this.rows = 6;
        }

        this.ySize = magicNumber + this.rows * 18 + this.reservedSpace;
        // this.guiTop = top;
        final int unusedSpace = this.height - this.ySize;
        this.guiTop = (int) Math.floor(unusedSpace / (unusedSpace < 0 ? 3.8f : 2.0f));

        super.initGui();

        this.getScrollBar().setLeft(189);
        this.getScrollBar().setHeight(106);
        this.getScrollBar().setTop(51);

        this.searchFieldInputs = new MEGuiTextField(this.fontRenderer, this.guiLeft + Math.max(32, this.offsetX), this.guiTop + 25, 65, 12);
        this.searchFieldInputs.setEnableBackgroundDrawing(false);
        this.searchFieldInputs.setMaxStringLength(25);
        this.searchFieldInputs.setTextColor(0xFFFFFF);
        this.searchFieldInputs.setVisible(true);
        this.searchFieldInputs.setFocused(false);

        this.searchFieldOutputs = new MEGuiTextField(this.fontRenderer, this.guiLeft + Math.max(32, this.offsetX), this.guiTop + 38, 65, 12);
        this.searchFieldOutputs.setEnableBackgroundDrawing(false);
        this.searchFieldOutputs.setMaxStringLength(25);
        this.searchFieldOutputs.setTextColor(0xFFFFFF);
        this.searchFieldOutputs.setVisible(true);
        this.searchFieldOutputs.setFocused(true);

        this.searchFieldInputs.setText(partInterfaceTerminal.in);
        this.searchFieldOutputs.setText(partInterfaceTerminal.out);

        this.fontRenderer.drawString(this.getGuiDisplayName(GuiText.InterfaceTerminal.getLocal()), 8, 6, 4210752);
        this.fontRenderer.drawString(GuiText.inventory.getLocal(), this.offsetX + 2, this.ySize - 96 + 3, 4210752);

    }

    protected void repositionSlot(final AppEngSlot s) {
        this.ySize = 82 + 14 + this.drawnRows * 18 + this.reservedSpace;

        s.yPos = s.getY() + this.ySize - 112;
        s.xPos = s.getX() + 14;
    }

    @Override
    public void onGuiClosed() {
        partInterfaceTerminal.saveSearchStrings(this.searchFieldInputs.getText().toLowerCase(), this.searchFieldOutputs.getText().toLowerCase());
        super.onGuiClosed();
    }

    @Override
    public List<Rectangle> getJEIExclusionArea() {
        Rectangle tallButton = new Rectangle(this.guiLeft - 18, this.guiTop + 24 + jeiOffset, 18, 18);
        List<Rectangle> area = new ArrayList<>();
        area.add(tallButton);
        return area;
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.buttonList.clear();

        final int currentScroll = this.getScrollBar().getCurrentScroll();

        this.guiButtonNextAssembler = new GuiImgButton(guiLeft + 123, guiTop + 25, Settings.ACTIONS, ActionItems.FREE_MOLECULAR_SLOT_SHORTCUT);
        this.buttonList.add(guiButtonNextAssembler);

        guiButtonHide = new GuiImgButton(guiLeft + 141, guiTop + 25, Settings.ACTIONS, this.partInterfaceTerminal.onlyInterfacesWithFreeSlots ? ActionItems.TOGGLE_SHOW_FULL_INTERFACES_OFF : ActionItems.TOGGLE_SHOW_FULL_INTERFACES_ON);
        this.buttonList.add(guiButtonHide);

        this.buttonList.add(this.terminalStyleBox = new GuiImgButton(this.guiLeft - 18, guiTop + 24 + jeiOffset, Settings.TERMINAL_STYLE, AEConfig.instance()
                .getConfigManager()
                .getSetting(Settings.TERMINAL_STYLE)));

        this.inventorySlots.inventorySlots.removeIf(slot -> slot instanceof SlotDisconnected);

        for (final Slot s : this.inventorySlots.inventorySlots) {
            if (s instanceof AppEngSlot) {
                if (s.xPos < 197) {
                    this.repositionSlot((AppEngSlot) s);
                }
            }
        }

        int offset = 52;
        int linesDraw = 0;
        for (int x = 0; x < rows && linesDraw < rows && currentScroll + x < this.lines.size(); x++) {
            final Object lineObj = this.lines.get(currentScroll + x);
            if (lineObj instanceof ClientDCInternalInv) {
                final ClientDCInternalInv inv = (ClientDCInternalInv) lineObj;

                GuiButton guiButton = new GuiImgButton(guiLeft + 4, guiTop + offset, Settings.ACTIONS, ActionItems.HIGHLIGHT_INTERFACE);
                guiButtonHashMap.put(guiButton, inv);
                this.buttonList.add(guiButton);
                int extraLines = numUpgradesMap.get(inv);

                for (int row = 0; row < 1 + extraLines && linesDraw < rows; ++row) {
                    for (int z = 0; z < 9; z++) {
                        this.inventorySlots.inventorySlots.add(new SlotDisconnected(inv, z + (row * 9), (z * 18 + 22), offset));
                        if (this.matchedStacks.contains(inv.getInventory().getStackInSlot(z + (row * 9)))) {
                            drawRect(z * 18 + 22, offset, z * 18 + 22 + 16, offset + 16, 0x8A00FF00);
                        } else if (!matchedInterfaces.contains(inv)) {
                            drawRect(z * 18 + 22, offset, z * 18 + 22 + 16, offset + 16, 0x6A000000);
                        }
                    }
                    linesDraw++;
                    offset += 18;
                }
            } else if (lineObj instanceof String) {
                String name = (String) lineObj;
                final int rows = this.byName.get(name).size();
                if (rows > 1) {
                    name = name + " (" + rows + ')';
                }

                while (name.length() > 2 && this.fontRenderer.getStringWidth(name) > 155) {
                    name = name.substring(0, name.length() - 1);
                }
                this.fontRenderer.drawString(name, this.offsetX + 2, 5 + offset, 4210752);
                linesDraw++;
                offset += 18;
            }
        }

        if (searchFieldInputs.isMouseIn(mouseX, mouseY)) {
            drawTooltip(Mouse.getEventX() * this.width / this.mc.displayWidth - offsetX, mouseY - guiTop, "Inputs OR names");
        } else if (searchFieldOutputs.isMouseIn(mouseX, mouseY)) {
            drawTooltip(Mouse.getEventX() * this.width / this.mc.displayWidth - offsetX, mouseY - guiTop, "Outputs OR names");
        }

    }

    @Override
    protected void mouseClicked(final int xCoord, final int yCoord, final int btn) throws IOException {
        this.searchFieldInputs.mouseClicked(xCoord, yCoord, btn);

        if (btn == 1 && this.searchFieldInputs.isMouseIn(xCoord, yCoord)) {
            this.searchFieldInputs.setText("");
            this.refreshList();
        }

        this.searchFieldOutputs.mouseClicked(xCoord, yCoord, btn);

        if (btn == 1 && this.searchFieldOutputs.isMouseIn(xCoord, yCoord)) {
            this.searchFieldOutputs.setText("");
            this.refreshList();
        }

        super.mouseClicked(xCoord, yCoord, btn);
    }

    @Override
    protected void actionPerformed(final GuiButton btn) throws IOException {
        if (guiButtonHashMap.containsKey(btn)) {
            BlockPos blockPos = blockPosHashMap.get(guiButtonHashMap.get(this.selectedButton));
            BlockPos blockPos2 = mc.player.getPosition();
            int playerDim = mc.world.provider.getDimension();
            int interfaceDim = dimHashMap.get(guiButtonHashMap.get(this.selectedButton));
            if (playerDim != interfaceDim) {
                try {
                    mc.player.sendStatusMessage(new TextComponentString("Interface located at dimension: " + interfaceDim + " [" + DimensionManager.getWorld(interfaceDim).provider.getDimensionType().getName() + "] and cant be highlighted"), false);
                } catch (Exception e) {
                    mc.player.sendStatusMessage(new TextComponentString("Interface is located in another dimension and cannot be highlighted"), false);
                }
            } else {
                hilightBlock(blockPos, System.currentTimeMillis() + 500 * BlockPosUtils.getDistance(blockPos, blockPos2), playerDim);
                mc.player.sendStatusMessage(new TextComponentString("The interface is now highlighted at " + "X: " + blockPos.getX() + " Y: " + blockPos.getY() + " Z: " + blockPos.getZ()), false);
            }
            mc.player.closeScreen();
        }

        if (btn instanceof GuiImgButton) {
            final boolean backwards = Mouse.isButtonDown(1);

            final GuiImgButton iBtn = (GuiImgButton) btn;
            if (iBtn.getSetting() != Settings.ACTIONS) {
                final Enum cv = iBtn.getCurrentValue();
                final Enum next = Platform.rotateEnum(cv, backwards, iBtn.getSetting().getPossibleValues());

                if (btn == this.terminalStyleBox) {
                    AEConfig.instance().getConfigManager().putSetting(iBtn.getSetting(), next);
                }

                iBtn.set(next);

                if (next.getClass() == TerminalStyle.class) {
                    this.reinitalize();
                }
            }
        }
        if (btn == guiButtonHide) {
            partInterfaceTerminal.onlyInterfacesWithFreeSlots = !partInterfaceTerminal.onlyInterfacesWithFreeSlots;
            this.refreshList();
        }

        if (btn == guiButtonNextAssembler) {
            // Set Search to "Molecular Assembler" and set "Only Free Interface"
            boolean currentOnlyInterfacesWithFreeSlots = this.partInterfaceTerminal.onlyInterfacesWithFreeSlots;
            String currentSearchText = this.searchFieldOutputs.getText();

            this.partInterfaceTerminal.onlyInterfacesWithFreeSlots = true;
            this.searchFieldOutputs.setText("Molecular Assembler");

            this.refreshList();

            this.partInterfaceTerminal.onlyInterfacesWithFreeSlots = currentOnlyInterfacesWithFreeSlots;
            this.searchFieldOutputs.setText(currentSearchText);
        }
    }

    private void reinitalize() {
        this.buttonList.clear();
        this.initGui();
    }

    @Override
    public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.bindTexture("guis/newinterfaceterminal.png");
        //split the texture for the top
        this.drawTexturedModalRect(offsetX, offsetY, 0, 0, this.xSize, 166);

        int offset = 51;
        final int ex = this.getScrollBar().getCurrentScroll();
        int linesDraw = 0;
        for (int x = 0; x < rows && linesDraw < rows && ex + x < this.lines.size(); x++) {
            final Object lineObj = this.lines.get(ex + x);
            if (lineObj instanceof ClientDCInternalInv) {
                GlStateManager.color(1, 1, 1, 1);
                final int width = 9 * 18;

                int extraLines = numUpgradesMap.get(lineObj);

                for (int row = 0; row < 1 + extraLines && linesDraw < rows; ++row) {
                    if (linesDraw == 6) {
                        this.drawTexturedModalRect(offsetX, offsetY + offset + 7, 0, 166, 190, 7);
                        this.drawTexturedModalRect(offsetX, offsetY + offset + 14, 0, 166, 190, 4);
                    } else if (linesDraw >= 7) {
                        this.drawTexturedModalRect(offsetX, offsetY + offset, 0, 173, 190, 18);
                    }
                    this.drawTexturedModalRect(offsetX + 20, offsetY + offset, 20, 173, width, 18);

                    offset += 18;
                    linesDraw++;
                }
            } else {
                if (linesDraw == 6) {
                    this.drawTexturedModalRect(offsetX, offsetY + offset + 7, 0, 166, 190, 7);
                    this.drawTexturedModalRect(offsetX, offsetY + offset + 14, 0, 166, 190, 4);
                    this.drawTexturedModalRect(offsetX, offsetY + offset, 0, 53, 183, 18);
                } else if (linesDraw >= 7) {
                    this.drawTexturedModalRect(offsetX, offsetY + offset, 0, 53, 183, 18);
                    this.drawTexturedModalRect(offsetX + 183, offsetY + offset, 183, 166, 7, 18);
                }
                offset += 18;
                linesDraw++;
            }
        }
        offset = 51 + Math.max(6 * 18, linesDraw * 18);
        if (linesDraw > 6) {
            this.drawTexturedModalRect(offsetX, offsetY + offset, 0, 166, 190, 7);
        }
        this.drawTexturedModalRect(offsetX, offsetY + offset + 7, 0, 166, this.xSize, 90);

        this.drawnRows = Math.max(6, linesDraw);

        if (this.searchFieldInputs != null) {
            this.searchFieldInputs.drawTextBox();
        }

        if (this.searchFieldOutputs != null) {
            this.searchFieldOutputs.drawTextBox();
        }
    }

    @Override
    protected void keyTyped(final char character, final int key) throws IOException {
        if (!this.checkHotbarKeys(key)) {
            if (character == ' ' && this.searchFieldInputs.getText().isEmpty() && this.searchFieldInputs.isFocused()) {
                return;
            }

            if (character == ' ' && this.searchFieldOutputs.getText().isEmpty() && this.searchFieldOutputs.isFocused()) {
                return;
            }

            if (this.searchFieldInputs.textboxKeyTyped(character, key) || this.searchFieldOutputs.textboxKeyTyped(character, key)) {
                this.refreshList();
            } else {
                super.keyTyped(character, key);
            }
        }
    }

    public void postUpdate(final NBTTagCompound in) {
        if (in.getBoolean("clear")) {
            this.byId.clear();
            this.refreshList = true;
        }

        for (final Object oKey : in.getKeySet()) {
            final String key = (String) oKey;
            if (key.startsWith("=")) {
                try {
                    final long id = Long.parseLong(key.substring(1), Character.MAX_RADIX);
                    final NBTTagCompound invData = in.getCompoundTag(key);
                    final ClientDCInternalInv current = this.getById(id, invData.getLong("sortBy"), invData.getString("un"));
                    blockPosHashMap.put(current, NBTUtil.getPosFromTag(invData.getCompoundTag("pos")));
                    dimHashMap.put(current, invData.getInteger("dim"));
                    numUpgradesMap.put(current, invData.getInteger("numUpgrades"));

                    for (int x = 0; x < current.getInventory().getSlots(); x++) {
                        final String which = Integer.toString(x);
                        if (invData.hasKey(which)) {
                            current.getInventory().setStackInSlot(x, stackFromNBT(invData.getCompoundTag(which)));
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
        this.buttonList.clear();
        this.matchedStacks.clear();
        this.matchedInterfaces.clear();

        final String searchFieldInputs = this.searchFieldInputs.getText().toLowerCase();
        final String searchFieldOutputs = this.searchFieldOutputs.getText().toLowerCase();

        final Set<Object> cachedSearch = this.getCacheForSearchTerm("IN:" + searchFieldInputs + " OUT:" + searchFieldOutputs + partInterfaceTerminal.onlyInterfacesWithFreeSlots);
        final boolean rebuild = cachedSearch.isEmpty();

        for (final ClientDCInternalInv entry : this.byId.values()) {
            // ignore inventory if not doing a full rebuild and cache already marks it as miss.
            if (!rebuild && !cachedSearch.contains(entry)) {
                continue;
            }

            // Shortcut to skip any filter if search term is ""/empty

            boolean found = (searchFieldInputs.isEmpty() && searchFieldOutputs.isEmpty() && !partInterfaceTerminal.onlyInterfacesWithFreeSlots);
            boolean interfaceHasFreeSlots = false;

            // Search if the current inventory holds a pattern containing the search term.
            if (!found) {
                int slot = 0;
                for (final ItemStack itemStack : entry.getInventory()) {
                    if (slot > 8 + numUpgradesMap.get(entry) * 9) {
                        break;
                    }
                    if (!searchFieldInputs.isEmpty() && !searchFieldOutputs.isEmpty()) {
                        if (this.itemStackMatchesSearchTerm(itemStack, searchFieldInputs, 0) || this.itemStackMatchesSearchTerm(itemStack, searchFieldOutputs, 1)) {
                            found = true;
                            matchedStacks.add(itemStack);
                        }
                    } else if (!searchFieldInputs.isEmpty()) {
                        if (this.itemStackMatchesSearchTerm(itemStack, searchFieldInputs, 0)) {
                            found = true;
                            matchedStacks.add(itemStack);
                        }
                    } else if (!searchFieldOutputs.isEmpty()) {
                        if (this.itemStackMatchesSearchTerm(itemStack, searchFieldOutputs, 1)) {
                            found = true;
                            matchedStacks.add(itemStack);
                        }
                    }
                    // If only Interfaces with empty slots should be shown, check that here
                    if (itemStack.isEmpty()) {
                        interfaceHasFreeSlots = true;
                    }
                    slot++;
                }
            }
            // if found, filter skipped or machine name matching the search term, add it
            if ((searchFieldInputs.isEmpty() && searchFieldOutputs.isEmpty()) ||
                    !searchFieldInputs.isEmpty() && entry.getName().toLowerCase().contains(searchFieldInputs) ||
                    (!searchFieldOutputs.isEmpty() && entry.getName().toLowerCase().contains(searchFieldOutputs))) {
                this.matchedInterfaces.add(entry);
                found = true;
            }
            if (found) {
                if (!partInterfaceTerminal.onlyInterfacesWithFreeSlots) {
                    this.byName.put(entry.getName(), entry);
                    cachedSearch.add(entry);
                } else if (interfaceHasFreeSlots) {
                    this.byName.put(entry.getName(), entry);
                    cachedSearch.add(entry);
                }
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

            final ArrayList<ClientDCInternalInv> clientInventories = new ArrayList<>();
            clientInventories.addAll(this.byName.get(n));

            Collections.sort(clientInventories);
            this.lines.addAll(clientInventories);
        }

        this.getScrollBar().setRange(0, this.lines.size() - 1, 1);
    }

    private boolean itemStackMatchesSearchTerm(final ItemStack itemStack, final String searchTerm, int pass) {
        if (itemStack.isEmpty()) {
            return false;
        }

        final NBTTagCompound encodedValue = itemStack.getTagCompound();

        if (encodedValue == null) {
            return searchTerm.matches(GuiText.InvalidPattern.getLocal());
        }

        NBTTagList tag = new NBTTagList();

        if (pass == 0) {
            tag = encodedValue.getTagList("in", 10);
        } else {
            tag = encodedValue.getTagList("out", 10);
        }

        boolean foundMatchingItemStack = false;

        for (int i = 0; i < tag.tagCount(); i++) {
            final ItemStack parsedItemStack = new ItemStack(tag.getCompoundTagAt(i));
            if (!parsedItemStack.isEmpty()) {
                final String displayName = Platform
                        .getItemDisplayName(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(parsedItemStack))
                        .toLowerCase();

                for (String term : searchTerm.split(" ")) {
                    if (term.length() > 1 && (term.startsWith("-") || term.startsWith("!"))) {
                        term = term.substring(1);
                        if (displayName.contains(term)) {
                            return false;
                        }
                    } else if (displayName.contains(term)) {
                        foundMatchingItemStack = true;
                    }
                }
            }
        }
        return foundMatchingItemStack;
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

    /**
     * The max amount of unique names and each inv row. Not affected by the filtering.
     *
     * @return max amount of unique names and each inv row
     */
    private int getMaxRows() {
        return this.names.size() + this.byId.size();
    }

    private ClientDCInternalInv getById(final long id, final long sortBy, final String string) {
        ClientDCInternalInv o = this.byId.get(id);

        if (o == null) {
            this.byId.put(id, o = new ClientDCInternalInv(DualityInterface.NUMBER_OF_PATTERN_SLOTS, id, sortBy, string));
            this.refreshList = true;
        }

        return o;
    }

    int getReservedSpace() {
        return this.reservedSpace;
    }

    void setReservedSpace(final int reservedSpace) {
        this.reservedSpace = reservedSpace;
    }
}
