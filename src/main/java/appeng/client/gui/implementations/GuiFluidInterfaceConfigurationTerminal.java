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
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiCustomSlot;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiScrollbar;
import appeng.client.gui.widgets.MEGuiTextField;
import appeng.client.me.ClientDCInternalFluidInv;
import appeng.client.me.SlotDisconnected;
import appeng.container.implementations.ContainerFluidInterfaceConfigurationTerminal;
import appeng.container.interfaces.IJEIGhostIngredients;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.fluids.client.gui.widgets.GuiFluidTank;
import appeng.fluids.util.AEFluidStack;
import appeng.helpers.InventoryAction;
import appeng.parts.reporting.PartFluidInterfaceConfigurationTerminal;
import appeng.util.BlockPosUtils;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import com.google.common.collect.HashMultimap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.DimensionManager;
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


public class GuiFluidInterfaceConfigurationTerminal extends AEBaseGui implements IJEIGhostIngredients {

    private static final int LINES_ON_PAGE = 6;

    // TODO: copied from GuiMEMonitorable. It looks not changed, maybe unneeded?
    private final int offsetX = 21;

    private final HashMap<Long, ClientDCInternalFluidInv> byId = new HashMap<>();
    private final HashMultimap<String, ClientDCInternalFluidInv> byName = HashMultimap.create();
    private final HashMap<ClientDCInternalFluidInv, BlockPos> blockPosHashMap = new HashMap<>();
    private final HashMap<GuiButton, ClientDCInternalFluidInv> guiButtonHashMap = new HashMap<>();

    private final Map<GuiFluidTank, ClientDCInternalFluidInv> guiFluidTankClientDCInternalFluidInvMap = new Object2ObjectOpenHashMap<>();
    private final Map<ClientDCInternalFluidInv, Integer> numUpgradesMap = new HashMap<>();
    private final ArrayList<String> names = new ArrayList<>();
    private final ArrayList<Object> lines = new ArrayList<>();
    private final Set<Object> matchedStacks = new HashSet<>();

    private final Map<String, Set<Object>> cachedSearches = new WeakHashMap<>();

    private boolean refreshList = false;
    private MEGuiTextField searchFieldInputs;
    private final PartFluidInterfaceConfigurationTerminal partInterfaceTerminal;
    private final HashMap<ClientDCInternalFluidInv, Integer> dimHashMap = new HashMap<>();
    public Map<IGhostIngredientHandler.Target<?>, Object> mapTargetSlot = new HashMap<>();

    public GuiFluidInterfaceConfigurationTerminal(final InventoryPlayer inventoryPlayer, final PartFluidInterfaceConfigurationTerminal te) {
        super(new ContainerFluidInterfaceConfigurationTerminal(inventoryPlayer, te));

        this.partInterfaceTerminal = te;
        final GuiScrollbar scrollbar = new GuiScrollbar();
        this.setScrollBar(scrollbar);
        this.xSize = 208;
        this.ySize = 235;
    }

    @Override
    public void initGui() {
        super.initGui();

        this.getScrollBar().setLeft(189);
        this.getScrollBar().setHeight(106);
        this.getScrollBar().setTop(31);

        this.searchFieldInputs = new MEGuiTextField(this.fontRenderer, this.guiLeft + Math.max(32, this.offsetX), this.guiTop + 17, 65, 12);
        this.searchFieldInputs.setEnableBackgroundDrawing(false);
        this.searchFieldInputs.setMaxStringLength(25);
        this.searchFieldInputs.setTextColor(0xFFFFFF);
        this.searchFieldInputs.setVisible(true);
        this.searchFieldInputs.setFocused(false);

        this.searchFieldInputs.setText(partInterfaceTerminal.in);
    }

    @Override
    public void onGuiClosed() {
        partInterfaceTerminal.saveSearchStrings(this.searchFieldInputs.getText().toLowerCase());
        super.onGuiClosed();
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.buttonList.clear();

        this.fontRenderer.drawString(this.getGuiDisplayName(GuiText.FluidInterfaceConfigurationTerminal.getLocal()), 8, 6, 4210752);
        this.fontRenderer.drawString(GuiText.inventory.getLocal(), this.offsetX + 2, this.ySize - 96 + 3, 4210752);

        final int currentScroll = this.getScrollBar().getCurrentScroll();

        this.guiSlots.removeIf(slot -> slot instanceof GuiFluidTank);

        int offset = 30;
        int linesDraw = 0;
        for (int x = 0; x < LINES_ON_PAGE && linesDraw < LINES_ON_PAGE && currentScroll + x < this.lines.size(); x++) {
            final Object lineObj = this.lines.get(currentScroll + x);
            if (lineObj instanceof ClientDCInternalFluidInv) {
                final ClientDCInternalFluidInv inv = (ClientDCInternalFluidInv) lineObj;

                GuiButton guiButton = new GuiImgButton(guiLeft + 4, guiTop + offset, Settings.ACTIONS, ActionItems.HIGHLIGHT_INTERFACE);
                guiButtonHashMap.put(guiButton, inv);
                this.buttonList.add(guiButton);
                int extraLines = numUpgradesMap.get(inv);

                for (int row = 0; row < 1 + extraLines && linesDraw < LINES_ON_PAGE; ++row) {
                    for (int z = 0; z < 5; z++) {
                        GuiFluidTank tankSlot = new GuiFluidTank(inv.getInventory(), z + (row * 5), z + (row * 5), (z * 18 + 22), offset, 16, 16);
                        this.guiSlots.add(tankSlot);
                        guiFluidTankClientDCInternalFluidInvMap.put(tankSlot, inv);
                        if (this.matchedStacks.contains(inv.getInventory().getFluidInSlot(z + (row * 5)))) {
                            drawRect(z * 18 + 22, offset, z * 18 + 22 + 16, offset + 16, 0x2A00FF00);
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
        }
    }

    @Override
    protected void mouseClicked(final int xCoord, final int yCoord, final int btn) throws IOException {
        this.searchFieldInputs.mouseClicked(xCoord, yCoord, btn);

        if (btn == 1 && this.searchFieldInputs.isMouseIn(xCoord, yCoord)) {
            this.searchFieldInputs.setText("");
            this.refreshList();
        }

        for (GuiCustomSlot slot : this.guiSlots) {
            if (slot instanceof GuiFluidTank) {
                if (this.isPointInRegion(slot.xPos(), slot.yPos(), slot.getWidth(), slot.getHeight(), xCoord, yCoord) && slot.canClick(this.mc.player)) {
                    NetworkHandler.instance().sendToServer(new PacketInventoryAction(InventoryAction.PICKUP_OR_SET_DOWN, slot.getId(), guiFluidTankClientDCInternalFluidInvMap.get(slot).getId()));
                    return;
                }
            }
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
    }

    @Override
    public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.bindTexture("guis/interfaceconfigurationterminal.png");
        this.drawTexturedModalRect(offsetX, offsetY, 0, 0, this.xSize, this.ySize);

        int offset = 29;
        final int ex = this.getScrollBar().getCurrentScroll();
        int linesDraw = 0;
        for (int x = 0; x < LINES_ON_PAGE && linesDraw < LINES_ON_PAGE && ex + x < this.lines.size(); x++) {
            final Object lineObj = this.lines.get(ex + x);
            if (lineObj instanceof ClientDCInternalFluidInv) {
                GlStateManager.color(1, 1, 1, 1);
                final int width = 5 * 18;

                int extraLines = numUpgradesMap.get(lineObj);

                for (int row = 0; row < 1 + extraLines && linesDraw < LINES_ON_PAGE; ++row) {
                    this.drawTexturedModalRect(offsetX + 20, offsetY + offset, 20, 170, width, 18);
                    offset += 18;
                    linesDraw++;
                }
            } else {
                offset += 18;
                linesDraw++;
            }
        }

        if (this.searchFieldInputs != null) {
            this.searchFieldInputs.drawTextBox();
        }
    }

    @Override
    protected void keyTyped(final char character, final int key) throws IOException {
        if (!this.checkHotbarKeys(key)) {
            if (character == ' ' && this.searchFieldInputs.getText().isEmpty() && this.searchFieldInputs.isFocused()) {
                return;
            }

            if (this.searchFieldInputs.textboxKeyTyped(character, key)) {
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
                    final ClientDCInternalFluidInv current = this.getById(id, invData.getLong("sortBy"), invData.getString("un"));
                    blockPosHashMap.put(current, NBTUtil.getPosFromTag(invData.getCompoundTag("pos")));
                    dimHashMap.put(current, invData.getInteger("dim"));
                    numUpgradesMap.put(current, invData.getInteger("numUpgrades"));

                    for (int x = 0; x < current.getInventory().getSlots(); x++) {
                        final String which = Integer.toString(x);
                        if (invData.hasKey(which)) {
                            current.getInventory().setFluidInSlot(x, AEFluidStack.fromNBT(invData.getCompoundTag(which)));
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

        final String searchFieldInputs = this.searchFieldInputs.getText().toLowerCase();

        final Set<Object> cachedSearch = this.getCacheForSearchTerm(searchFieldInputs);
        final boolean rebuild = cachedSearch.isEmpty();

        for (final ClientDCInternalFluidInv entry : this.byId.values()) {
            // ignore inventory if not doing a full rebuild and cache already marks it as miss.
            if (!rebuild && !cachedSearch.contains(entry)) {
                continue;
            }

            // Shortcut to skip any filter if search term is ""/empty

            boolean found = searchFieldInputs.isEmpty();

            // Search if the current inventory holds a pattern containing the search term.
            if (!found) {
                int slot = 0;
                for (int i = 0; i < entry.getInventory().getSlots(); i++) {
                    if (slot > 8 + numUpgradesMap.get(entry) * 9) {
                        break;
                    }
                    IAEFluidStack fs = entry.getInventory().getFluidInSlot(i);
                    if (this.fluidStackMatchesSearchTerm(fs, searchFieldInputs)) {
                        found = true;
                        matchedStacks.add(fs);
                    }
                    slot++;
                }
            }
            // if found, filter skipped or machine name matching the search term, add it
            if (found || entry.getName().toLowerCase().contains(searchFieldInputs)) {
                this.byName.put(entry.getName(), entry);
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

            final ArrayList<ClientDCInternalFluidInv> clientInventories = new ArrayList<>();
            clientInventories.addAll(this.byName.get(n));

            Collections.sort(clientInventories);
            this.lines.addAll(clientInventories);
        }

        this.getScrollBar().setRange(0, this.lines.size() - 1, 1);
    }

    private boolean fluidStackMatchesSearchTerm(final IAEFluidStack iaeFluidStack, final String searchTerm) {
        if (iaeFluidStack == null) {
            return false;
        }

        boolean foundMatchingItemStack = false;

        final String displayName = Platform.getFluidDisplayName(iaeFluidStack).toLowerCase();

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

    private ClientDCInternalFluidInv getById(final long id, final long sortBy, final String string) {
        ClientDCInternalFluidInv o = this.byId.get(id);

        if (o == null) {
            this.byId.put(id, o = new ClientDCInternalFluidInv(5, id, sortBy, string, 64));
            this.refreshList = true;
        }

        return o;
    }

    @Override
    public List<IGhostIngredientHandler.Target<?>> getPhantomTargets(Object ingredient) {
        if (!(ingredient instanceof ItemStack)) {
            return Collections.emptyList();
        }
        List<IGhostIngredientHandler.Target<?>> targets = new ArrayList<>();
        for (Slot slot : this.inventorySlots.inventorySlots) {
            if (slot instanceof SlotDisconnected) {
                ItemStack itemStack = (ItemStack) ingredient;
                IGhostIngredientHandler.Target<Object> target = new IGhostIngredientHandler.Target<Object>() {
                    @Override
                    public Rectangle getArea() {
                        return new Rectangle(getGuiLeft() + slot.xPos, getGuiTop() + slot.yPos, 16, 16);
                    }

                    @Override
                    public void accept(Object ingredient) {
                        final PacketInventoryAction p;
                        try {
                            p = new PacketInventoryAction(InventoryAction.PLACE_JEI_GHOST_ITEM, (SlotDisconnected) slot, AEItemStack.fromItemStack(itemStack));
                            NetworkHandler.instance().sendToServer(p);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };
                targets.add(target);
                mapTargetSlot.putIfAbsent(target, slot);
            }
        }
        return targets;
    }

    @Override
    public Map<IGhostIngredientHandler.Target<?>, Object> getFakeSlotTargetMap() {
        return IJEIGhostIngredients.super.getFakeSlotTargetMap();
    }
}
