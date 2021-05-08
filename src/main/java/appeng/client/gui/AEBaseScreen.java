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

package appeng.client.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ArrayListMultimap;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;

import appeng.client.Point;
import appeng.client.gui.layout.SlotGridLayout;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.SlotPosition;
import appeng.client.gui.style.Text;
import appeng.client.gui.widgets.CustomSlotWidget;
import appeng.client.gui.widgets.ITickingWidget;
import appeng.client.gui.widgets.ITooltip;
import appeng.client.gui.widgets.VerticalButtonBar;
import appeng.container.AEBaseContainer;
import appeng.container.SlotSemantic;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.CraftingTermSlot;
import appeng.container.slot.DisabledSlot;
import appeng.container.slot.FakeSlot;
import appeng.container.slot.IOptionalSlot;
import appeng.container.slot.PatternTermSlot;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.InventoryActionPacket;
import appeng.core.sync.packets.SwapSlotsPacket;
import appeng.helpers.InventoryAction;

public abstract class AEBaseScreen<T extends AEBaseContainer> extends ContainerScreen<T> {

    private static final Point HIDDEN_SLOT_POS = new Point(-9999, -9999);

    /**
     * Commonly used id for text that is used to show the dialog title.
     */
    public static final String TEXT_ID_DIALOG_TITLE = "dialog_title";

    private final VerticalButtonBar verticalToolbar;

    // drag y
    private final Set<Slot> drag_click = new HashSet<>();
    private boolean disableShiftClick = false;
    private Stopwatch dbl_clickTimer = Stopwatch.createStarted();
    private ItemStack dbl_whichItem = ItemStack.EMPTY;
    private Slot bl_clicked;
    private boolean handlingRightClick;
    private final List<CustomSlotWidget> guiSlots = new ArrayList<>();
    private final ArrayListMultimap<SlotSemantic, CustomSlotWidget> guiSlotsBySemantic = ArrayListMultimap.create();
    private final Map<String, TextOverride> textOverrides = new HashMap<>();
    private final EnumSet<SlotSemantic> hiddenSlots = EnumSet.noneOf(SlotSemantic.class);
    protected final WidgetContainer widgets;
    protected final ScreenStyle style;

    public AEBaseScreen(T container, PlayerInventory playerInventory, ITextComponent title, ScreenStyle style) {
        super(container, playerInventory, title);

        // Pre-initialize these fields since they're used in our constructors, but Vanilla only initializes them
        // in the init method
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
        this.font = Minecraft.getInstance().fontRenderer;

        this.style = Objects.requireNonNull(style, "style");
        this.widgets = new WidgetContainer(style);
        this.widgets.add("verticalToolbar", this.verticalToolbar = new VerticalButtonBar());

        if (style.getBackground() != null) {
            this.xSize = style.getBackground().getSrcWidth();
            this.ySize = style.getBackground().getSrcHeight();
        }
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void init() {
        super.init();
        positionSlots(style);

        widgets.populateScreen(this::addButton, getBounds(true), this);
    }

    private void positionSlots(ScreenStyle style) {

        for (Map.Entry<SlotSemantic, SlotPosition> entry : style.getSlots().entrySet()) {
            // Do not position slots that are hidden
            if (hiddenSlots.contains(entry.getKey())) {
                continue;
            }

            List<Slot> slots = container.getSlots(entry.getKey());
            for (int i = 0; i < slots.size(); i++) {
                Slot slot = slots.get(i);

                Point pos = getSlotPosition(entry.getValue(), i);

                slot.xPos = pos.getX();
                slot.yPos = pos.getY();
            }

            // Do the same for GUI-only slots, which are used in Fluid-related UIs that do not deal with normal slots
            List<CustomSlotWidget> guiSlots = guiSlotsBySemantic.get(entry.getKey());
            if (guiSlots != null) {
                for (int i = 0; i < guiSlots.size(); i++) {
                    CustomSlotWidget guiSlot = guiSlots.get(i);
                    Point pos = getSlotPosition(entry.getValue(), i);
                    guiSlot.setPos(pos);
                }
            }
        }

    }

    private Point getSlotPosition(SlotPosition position, int semanticIndex) {
        Point pos = position.resolve(getBounds(false));

        SlotGridLayout grid = position.getGrid();
        if (grid != null) {
            pos = grid.getPosition(pos.getX(), pos.getY(), semanticIndex);
        }
        return pos;
    }

    private Rectangle2d getBounds(boolean absolute) {
        if (absolute) {
            return new Rectangle2d(guiLeft, guiTop, xSize, ySize);
        } else {
            return new Rectangle2d(0, 0, xSize, ySize);
        }
    }

    private List<Slot> getInventorySlots() {
        return this.container.inventorySlots;
    }

    protected final void addSlot(CustomSlotWidget slot, SlotSemantic semantic) {
        guiSlots.add(slot);
        guiSlotsBySemantic.put(semantic, slot);
    }

    /**
     * This method is called directly before rendering the screen, and should be used to perform layout, and other
     * rendering-related updates.
     */
    @OverridingMethodsMustInvokeSuper
    protected void updateBeforeRender() {
    }

    @Override
    public void render(MatrixStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        this.updateBeforeRender();
        this.widgets.updateBeforeRender();

        super.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        matrixStack.push();
        matrixStack.translate(this.guiLeft, this.guiTop, 0.0F);
        RenderSystem.enableDepthTest();
        for (final CustomSlotWidget c : this.guiSlots) {
            this.drawGuiSlot(matrixStack, c, mouseX, mouseY, partialTicks);
        }
        RenderSystem.disableDepthTest();
        for (final CustomSlotWidget c : this.guiSlots) {
            Tooltip tooltip = c.getTooltip(mouseX, mouseY);
            if (tooltip != null) {
                drawTooltip(matrixStack, tooltip, mouseX, mouseY);
            }
        }
        matrixStack.pop();
        RenderSystem.enableDepthTest();

        renderTooltips(matrixStack, mouseX, mouseY);

        if (AEConfig.instance().isShowDebugGuiOverlays()) {
            // Show a green overlay on exclusion zones
            List<Rectangle2d> exclusionZones = getExclusionZones();
            for (Rectangle2d rectangle2d : exclusionZones) {
                fillRect(matrixStack, rectangle2d, 0x7f00FF00);
            }

            hLine(matrixStack, guiLeft, guiLeft + xSize - 1, guiTop, 0xFFFFFFFF);
            hLine(matrixStack, guiLeft, guiLeft + xSize - 1, guiTop + ySize - 1, 0xFFFFFFFF);
            vLine(matrixStack, guiLeft, guiTop, guiTop + ySize, 0xFFFFFFFF);
            vLine(matrixStack, guiLeft + xSize - 1, guiTop, guiTop + ySize - 1, 0xFFFFFFFF);
        }
    }

    /**
     * Renders a potential tooltip (from one of the possible tooltip sources)
     */
    private void renderTooltips(MatrixStack matrixStack, int mouseX, int mouseY) {
        this.renderHoveredTooltip(matrixStack, mouseX, mouseY);

        // The line above should have render a tooltip if this condition is true, and no
        // additional tooltips should be shown
        if (this.hoveredSlot != null && this.hoveredSlot.getHasStack()) {
            return;
        }

        for (Widget c : this.buttons) {
            if (c instanceof ITooltip) {
                Tooltip tooltip = ((ITooltip) c).getTooltip(mouseX, mouseY);
                if (tooltip != null) {
                    drawTooltip(matrixStack, tooltip, mouseX, mouseY);
                }
            }
        }

        // Widget-container uses screen-relative coordinates while the rest uses window-relative
        Tooltip tooltip = this.widgets.getTooltip(mouseX - guiLeft, mouseY - guiTop);
        if (tooltip != null) {
            drawTooltip(matrixStack, tooltip, mouseX, mouseY);
        }
    }

    protected void drawGuiSlot(MatrixStack matrixStack, CustomSlotWidget slot, int mouseX, int mouseY,
            float partialTicks) {
        if (slot.isSlotEnabled()) {
            final int left = slot.getTooltipAreaX();
            final int top = slot.getTooltipAreaY();
            final int right = left + slot.getTooltipAreaWidth();
            final int bottom = top + slot.getTooltipAreaHeight();

            slot.drawContent(matrixStack, getMinecraft(), mouseX, mouseY, partialTicks);

            if (this.isPointInRegion(left, top, slot.getTooltipAreaWidth(), slot.getTooltipAreaHeight(), mouseX, mouseY)
                    && slot.canClick(getPlayer())) {
                RenderSystem.colorMask(true, true, true, false);
                this.fillGradient(matrixStack, left, top, right, bottom, -2130706433, -2130706433);
                RenderSystem.colorMask(true, true, true, true);
            }
        }
    }

    private void drawTooltip(MatrixStack matrixStack, Tooltip tooltip, int mouseX, int mouseY) {
        this.renderWrappedToolTip(matrixStack, tooltip.getContent(), mouseX, mouseY, font);

    }

    // FIXME FABRIC: move out to json (?)
    private static final Style TOOLTIP_HEADER = Style.EMPTY.applyFormatting(TextFormatting.WHITE);
    private static final Style TOOLTIP_BODY = Style.EMPTY.applyFormatting(TextFormatting.GRAY);

    public void drawTooltip(MatrixStack matrices, int x, int y, List<ITextComponent> lines) {
        if (lines.isEmpty()) {
            return;
        }

        // Make the first line white
        // All lines after the first are colored gray
        List<ITextComponent> styledLines = new ArrayList<>(lines.size());
        for (int i = 0; i < lines.size(); i++) {
            Style style = (i == 0) ? TOOLTIP_HEADER : TOOLTIP_BODY;
            styledLines.add(lines.get(i).deepCopy().modifyStyle(s -> style));
        }

        this.func_243308_b(matrices, styledLines, x, y);

    }

    @Override
    protected final void drawGuiContainerForegroundLayer(MatrixStack matrixStack, final int x, final int y) {
        final int ox = this.guiLeft;
        final int oy = this.guiTop;

        widgets.drawForegroundLayer(matrixStack, getBlitOffset(), getBounds(false), new Point(x - ox, y - oy));

        this.drawFG(matrixStack, ox, oy, x, y);

        if (style != null) {
            for (Map.Entry<String, Text> entry : style.getText().entrySet()) {
                // Process text overrides
                TextOverride override = textOverrides.get(entry.getKey());

                // Don't draw if the screen decided to hide this
                if (override != null && override.isHidden()) {
                    continue;
                }

                Text text = entry.getValue();
                int color = style.getColor(text.getColor()).toARGB();

                // Allow overrides for which content is shown
                ITextComponent content = text.getText();
                if (override != null && override.getContent() != null) {
                    content = override.getContent();
                }

                Point pos = text.getPosition().resolve(getBounds(false));

                if (text.isCenterHorizontally()) {
                    int textWidth = this.font.getStringPropertyWidth(content);
                    pos = pos.move(-textWidth / 2, 0);
                }

                this.font.func_243248_b(
                        matrixStack,
                        content,
                        pos.getX(),
                        pos.getY(),
                        color);
            }
        }
    }

    public void drawFG(MatrixStack matrixStack, int offsetX, int offsetY, int mouseX, int mouseY) {
    }

    @Override
    protected final void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, final float f, final int x,
            final int y) {

        this.drawBG(matrixStack, guiLeft, guiTop, x, y, f);

        widgets.drawBackgroundLayer(matrixStack, getBlitOffset(), getBounds(true), new Point(x - guiLeft, y - guiTop));

        for (final Slot slot : this.getInventorySlots()) {
            if (slot instanceof IOptionalSlot) {
                drawOptionalSlotBackground(matrixStack, (IOptionalSlot) slot, false);
            }
        }

        for (final CustomSlotWidget slot : this.guiSlots) {
            if (slot instanceof IOptionalSlot) {
                drawOptionalSlotBackground(matrixStack, (IOptionalSlot) slot, true);
            }
        }

    }

    private void drawOptionalSlotBackground(MatrixStack matrixStack, IOptionalSlot slot, boolean alwaysDraw) {
        // If a slot is optional and doesn't currently render, we still need to provide a background for it
        if (alwaysDraw || slot.isRenderDisabled()) {
            // If the slot is disabled, shade the background overlay
            float alpha = slot.isSlotEnabled() ? 1.0f : 0.4f;

            Point pos = slot.getBackgroundPos();

            Icon.SLOT_BACKGROUND.getBlitter()
                    .dest(guiLeft + pos.getX(), guiTop + pos.getY())
                    .color(1, 1, 1, alpha)
                    .blit(matrixStack, getBlitOffset());
        }
    }

    // Convert global mouse x,y to relative Point
    private Point getMousePoint(double x, double y) {
        return new Point((int) Math.round(x - guiLeft), (int) Math.round(y - guiTop));
    }

    @Override
    public boolean mouseScrolled(double x, double y, double wheelDelta) {
        if (wheelDelta != 0) {
            if (widgets.onMouseWheel(getMousePoint(x, y), wheelDelta)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseClicked(final double xCoord, final double yCoord, final int btn) {
        this.drag_click.clear();

        // Forward right-clicks as-if they were left-clicks
        if (btn == 1) {
            handlingRightClick = true;
            try {
                for (Widget widget : this.buttons) {
                    if (widget.isMouseOver(xCoord, yCoord)) {
                        return super.mouseClicked(xCoord, yCoord, 0);
                    }
                }
            } finally {
                handlingRightClick = false;
            }
        }

        CustomSlotWidget slot = getGuiSlotAt(xCoord, yCoord);
        if (slot != null) {
            slot.slotClicked(getPlayer().inventory.getItemStack(), btn);
        }

        if (widgets.onMouseDown(getMousePoint(xCoord, yCoord), btn)) {
            return true;
        }

        return super.mouseClicked(xCoord, yCoord, btn);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (widgets.onMouseUp(getMousePoint(mouseX, mouseY), button)) {
            return true;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dragX, double dragY) {
        final Slot slot = this.getSlot((int) mouseX, (int) mouseY);
        final ItemStack itemstack = getPlayer().inventory.getItemStack();

        Point mousePos = new Point((int) Math.round(mouseX - guiLeft), (int) Math.round(mouseY - guiTop));
        if (widgets.onMouseDrag(mousePos, mouseButton)) {
            return true;
        }

        if (slot instanceof FakeSlot && !itemstack.isEmpty()) {
            this.drag_click.add(slot);
            if (this.drag_click.size() > 1) {
                for (final Slot dr : this.drag_click) {
                    final InventoryActionPacket p = new InventoryActionPacket(
                            mouseButton == 0 ? InventoryAction.PICKUP_OR_SET_DOWN : InventoryAction.PLACE_SINGLE,
                            dr.slotNumber, 0);
                    NetworkHandler.instance().sendToServer(p);
                }
            }

            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, mouseButton, dragX, dragY);
        }
    }

    @Override
    protected void handleMouseClick(@Nullable Slot slot, final int slotIdx, final int mouseButton,
            final ClickType clickType) {

        // Do not allow clicks on disabled player inventory slots
        if (slot instanceof DisabledSlot) {
            return;
        }

        if (slot instanceof FakeSlot) {
            final InventoryAction action = mouseButton == 1 ? InventoryAction.SPLIT_OR_PLACE_SINGLE
                    : InventoryAction.PICKUP_OR_SET_DOWN;

            if (this.drag_click.size() > 1) {
                return;
            }

            final InventoryActionPacket p = new InventoryActionPacket(action, slotIdx, 0);
            NetworkHandler.instance().sendToServer(p);

            return;
        }

        if (slot instanceof PatternTermSlot) {
            if (mouseButton == 6) {
                return; // prevent weird double clicks..
            }

            NetworkHandler.instance().sendToServer(((PatternTermSlot) slot).getRequest(hasShiftDown()));
        } else if (slot instanceof CraftingTermSlot) {
            if (mouseButton == 6) {
                return; // prevent weird double clicks..
            }

            InventoryAction action;
            if (hasShiftDown()) {
                action = InventoryAction.CRAFT_SHIFT;
            } else {
                // Craft stack on right-click, craft single on left-click
                action = (mouseButton == 1) ? InventoryAction.CRAFT_STACK : InventoryAction.CRAFT_ITEM;
            }

            final InventoryActionPacket p = new InventoryActionPacket(action, slotIdx, 0);
            NetworkHandler.instance().sendToServer(p);

            return;
        }

        if (slot != null &&
                InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), GLFW.GLFW_KEY_SPACE)) {
            int slotNum = slot.slotNumber;
            final InventoryActionPacket p = new InventoryActionPacket(InventoryAction.MOVE_REGION, slotNum, 0);
            NetworkHandler.instance().sendToServer(p);
            return;
        }

        if (slot != null && !this.disableShiftClick && hasShiftDown() && mouseButton == 0) {
            this.disableShiftClick = true;

            if (this.dbl_whichItem.isEmpty() || this.bl_clicked != slot
                    || this.dbl_clickTimer.elapsed(TimeUnit.MILLISECONDS) > 250) {
                // some simple double click logic.
                this.bl_clicked = slot;
                this.dbl_clickTimer = Stopwatch.createStarted();
                this.dbl_whichItem = slot.getHasStack() ? slot.getStack().copy() : ItemStack.EMPTY;
            } else if (!this.dbl_whichItem.isEmpty()) {
                // a replica of the weird broken vanilla feature.

                final List<Slot> slots = this.getInventorySlots();
                for (final Slot inventorySlot : slots) {
                    if (inventorySlot != null && inventorySlot.canTakeStack(getPlayer()) && inventorySlot.getHasStack()
                            && inventorySlot.isSameInventory(slot)
                            && Container.canAddItemToSlot(inventorySlot, this.dbl_whichItem, true)) {
                        this.handleMouseClick(inventorySlot, inventorySlot.slotNumber, 0, ClickType.QUICK_MOVE);
                    }
                }
                this.dbl_whichItem = ItemStack.EMPTY;
            }

            this.disableShiftClick = false;
        }

        super.handleMouseClick(slot, slotIdx, mouseButton, clickType);
    }

    @Override
    protected boolean itemStackMoved(int keyCode, int scanCode) {
        return checkHotbarKeys(InputMappings.getInputByCode(keyCode, scanCode));
    }

    protected ClientPlayerEntity getPlayer() {
        // Our UIs are usually not opened when not in-game, so this should not be a
        // problem
        return Preconditions.checkNotNull(getMinecraft().player);
    }

    protected boolean checkHotbarKeys(final InputMappings.Input input) {
        final Slot theSlot = this.getSlotUnderMouse();

        if (getPlayer().inventory.getItemStack().isEmpty() && theSlot != null) {
            for (int j = 0; j < 9; ++j) {
                if (getMinecraft().gameSettings.keyBindsHotbar[j].isActiveAndMatches(input)) {
                    final List<Slot> slots = this.getInventorySlots();
                    for (final Slot s : slots) {
                        if (s.getSlotIndex() == j && s.inventory == this.container.getPlayerInventory()) {
                            if (!s.canTakeStack(this.container.getPlayerInventory().player)) {
                                return false;
                            }
                        }
                    }

                    if (theSlot.getSlotStackLimit() == 64) {
                        this.handleMouseClick(theSlot, theSlot.slotNumber, j, ClickType.SWAP);
                        return true;
                    } else {
                        for (final Slot s : slots) {
                            if (s.getSlotIndex() == j
                                    && s.inventory == this.container.getPlayerInventory()) {
                                NetworkHandler.instance()
                                        .sendToServer(new SwapSlotsPacket(s.slotNumber, theSlot.slotNumber));
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    protected Slot getSlot(final int mouseX, final int mouseY) {
        final List<Slot> slots = this.getInventorySlots();
        for (final Slot slot : slots) {
            if (this.isPointInRegion(slot.xPos, slot.yPos, 16, 16, mouseX, mouseY)) {
                return slot;
            }
        }

        return null;
    }

    public void drawBG(MatrixStack matrixStack, int offsetX, int offsetY, int mouseX, int mouseY,
            float partialTicks) {
        if (style.getBackground() != null) {
            style.getBackground().dest(offsetX, offsetY).blit(matrixStack, getBlitOffset());
        }
    }

    public void drawItem(final int x, final int y, final ItemStack is) {
        this.itemRenderer.zLevel = 100.0F;

        // FIXME I dont think this is needed anymore...
        RenderHelper.enableStandardItemLighting();
        this.itemRenderer.renderItemAndEffectIntoGUI(is, x, y);
        RenderHelper.disableStandardItemLighting();

        this.itemRenderer.zLevel = 0.0F;
    }

    protected ITextComponent getGuiDisplayName(final ITextComponent in) {
        return title.getString().isEmpty() ? in : title;
    }

    /**
     * This overrides the base-class method through some access transformer hackery...
     */
    @Override
    protected void moveItems(MatrixStack matrices, Slot s) {
        if (s instanceof AppEngSlot) {
            try {
                renderAppEngSlot(matrices, (AppEngSlot) s);
            } catch (final Exception err) {
                AELog.warn("[AppEng] AE prevented crash while drawing slot: " + err);
            }
        } else {
            super.moveItems(matrices, s);
        }
    }

    private void renderAppEngSlot(MatrixStack matrices, AppEngSlot s) {
        final ItemStack is = s.getStack();

        // If the slot has a background icon, render it, but only if the slot is empty
        // or it requests the icon to be always drawn
        if ((s.renderIconWithItem() || is.isEmpty()) && s.isSlotEnabled()) {
            if (s.getIcon() != null) {
                s.getIcon().getBlitter()
                        .dest(s.xPos, s.yPos)
                        .opacity(s.getOpacityOfIcon())
                        .blit(matrices, getBlitOffset());
            }
        }

        // Draw a red background for slots that are in an invalid state
        if (!s.isValid()) {
            fill(matrices, s.xPos, s.yPos, 16 + s.xPos, 16 + s.yPos, 0x66ff6666);
        }

        // Makes it so the first call to s.getStack will return getDisplayStack instead, the finally is there
        // just for making absolutly sure the flag gets reset (it should be reset inside of getStack)
        s.setRendering(true);
        try {
            super.moveItems(matrices, s);
        } finally {
            s.setRendering(false);
        }
    }

    public void bindTexture(final String file) {
        final ResourceLocation loc = new ResourceLocation(AppEng.MOD_ID, "textures/" + file);
        getMinecraft().getTextureManager().bindTexture(loc);
    }

    @Override
    public void tick() {
        super.tick();

        widgets.tick();

        for (IGuiEventListener child : children) {
            if (child instanceof ITickingWidget) {
                ((ITickingWidget) child).tick();
            }
        }
    }

    /**
     * Returns true while the current event being handled is a click of the right mouse button.
     */
    public boolean isHandlingRightClick() {
        return handlingRightClick;
    }

    /**
     * Adds a button to the vertical toolbar to the left of the screen and returns that button to the caller. The button
     * will automatically be positioned. This button will automatically be re-added to the screen when it's resized.
     */
    protected final <B extends Button> B addToLeftToolbar(B button) {
        verticalToolbar.add(button);
        return button;
    }

    /**
     * Returns rectangles in UI-space that define areas of the screen occluded by this GUI, in addition to the rectangle
     * defined by [guiLeft, guiTop, xSize, ySize], which is assumed to be occluded. This is used for moving JEI items
     * out of the way.
     */
    public List<Rectangle2d> getExclusionZones() {
        List<Rectangle2d> result = new ArrayList<>(2);
        widgets.addExclusionZones(result, getBounds(true));
        return result;
    }

    protected void fillRect(MatrixStack matrices, Rectangle2d rect, int color) {
        fill(matrices, rect.getX(), rect.getY(), rect.getX() + rect.getWidth(), rect.getY() + rect.getHeight(), color);
    }

    private TextOverride getOrCreateTextOverride(String id) {
        return textOverrides.computeIfAbsent(id, x -> new TextOverride());
    }

    /**
     * Hides (or shows) a text that is defined in this screen's style file.
     */
    protected final void setTextHidden(String id, boolean hidden) {
        getOrCreateTextOverride(id).setHidden(hidden);
    }

    /**
     * Hides (or shows) a group of slots based on semantic.
     */
    protected final void setSlotsHidden(SlotSemantic semantic, boolean hidden) {
        if (hidden) {
            if (hiddenSlots.add(semantic)) {
                // This isn't the greatest tactic but allows us to do this for every slot-type.
                // This approach has been used to hide slots since 1.7
                for (Slot slot : container.getSlots(semantic)) {
                    slot.xPos = HIDDEN_SLOT_POS.getX();
                    slot.yPos = HIDDEN_SLOT_POS.getY();
                }
            }
        } else {
            if (hiddenSlots.remove(semantic) && style != null) {
                positionSlots(style);
            }
        }
    }

    /**
     * Gets the GUI slot under the given coordinates.
     *
     * @param x X coordinate in window space.
     * @param y Y coordinate in window space.
     */
    @Nullable
    private CustomSlotWidget getGuiSlotAt(double x, double y) {
        for (CustomSlotWidget slot : this.guiSlots) {
            if (this.isPointInRegion(slot.getTooltipAreaX(), slot.getTooltipAreaY(), slot.getTooltipAreaWidth(),
                    slot.getTooltipAreaHeight(), x, y) && slot.canClick(getPlayer())) {
                return slot;
            }
        }

        return null;
    }

    public List<CustomSlotWidget> getGuiSlots() {
        return Collections.unmodifiableList(guiSlots);
    }

    /**
     * Changes the text that will be displayed for a text defined in this screen's style file.
     */
    protected final void setTextContent(String id, ITextComponent content) {
        getOrCreateTextOverride(id).setContent(content);
    }

    public ScreenStyle getStyle() {
        return style;
    }

    /**
     * Return a Vanilla ItemStack or FluidStack if there is one at the given window coordinates. This is used by JEI to
     * allow the U and R hotkeys to work for ingredients (such as fluids) that are not stored in normal slots.
     * <p/>
     * The given coordinates are in window space.
     */
    @Nullable
    public Object getIngredientUnderMouse(double mouseX, double mouseY) {
        IIngredientSupplier ingredientSupplier = null;

        // First search for custom widgets
        CustomSlotWidget guiSlot = getGuiSlotAt(mouseX, mouseY);
        if (guiSlot instanceof IIngredientSupplier) {
            ingredientSupplier = (IIngredientSupplier) guiSlot;
        }

        // Then any of the children
        if (ingredientSupplier == null) {
            for (IGuiEventListener child : super.children) {
                if (child instanceof IIngredientSupplier && child.isMouseOver(mouseX, mouseY)) {
                    ingredientSupplier = (IIngredientSupplier) child;
                    break;
                }
            }
        }

        Object ingredient = null;
        if (ingredientSupplier != null) {
            ingredient = ingredientSupplier.getFluidIngredient();
            if (ingredient == null) {
                ingredient = ingredientSupplier.getItemIngredient();
            }
        }

        return ingredient;
    }
}
