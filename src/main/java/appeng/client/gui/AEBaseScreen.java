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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import com.google.common.base.Stopwatch;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import org.lwjgl.glfw.GLFW;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import appeng.api.client.AEStackRendering;
import appeng.api.stacks.GenericStack;
import appeng.client.Point;
import appeng.client.gui.layout.SlotGridLayout;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.SlotPosition;
import appeng.client.gui.style.Text;
import appeng.client.gui.widgets.ITickingWidget;
import appeng.client.gui.widgets.ITooltip;
import appeng.client.gui.widgets.VerticalButtonBar;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.InventoryActionPacket;
import appeng.core.sync.packets.SwapSlotsPacket;
import appeng.helpers.InventoryAction;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantic;
import appeng.menu.SlotSemantics;
import appeng.menu.me.interaction.EmptyingAction;
import appeng.menu.me.interaction.StackInteractions;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.slot.CraftingTermSlot;
import appeng.menu.slot.DisabledSlot;
import appeng.menu.slot.FakeSlot;
import appeng.menu.slot.IOptionalSlot;
import appeng.menu.slot.ResizableSlot;
import appeng.util.ConfigMenuInventory;

public abstract class AEBaseScreen<T extends AEBaseMenu> extends AbstractContainerScreen<T> {

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
    private final Map<String, TextOverride> textOverrides = new HashMap<>();
    private final Set<SlotSemantic> hiddenSlots = new HashSet<>();
    protected final WidgetContainer widgets;
    protected final ScreenStyle style;

    public AEBaseScreen(T menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title);

        // Pre-initialize these fields since they're used in our constructors, but Vanilla only initializes them
        // in the init method
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
        this.font = Minecraft.getInstance().font;

        this.style = Objects.requireNonNull(style, "style");
        this.widgets = new WidgetContainer(style);
        this.widgets.add("verticalToolbar", this.verticalToolbar = new VerticalButtonBar());

        if (style.getBackground() != null) {
            this.imageWidth = style.getBackground().getSrcWidth();
            this.imageHeight = style.getBackground().getSrcHeight();
        }
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void init() {
        super.init();
        positionSlots(style);

        widgets.populateScreen(this::addRenderableWidget, getBounds(true), this);
    }

    private void positionSlots(ScreenStyle style) {

        for (var entry : style.getSlots().entrySet()) {
            var semantic = SlotSemantics.getOrThrow(entry.getKey());

            // Do not position slots that are hidden
            if (hiddenSlots.contains(semantic)) {
                continue;
            }

            List<Slot> slots = menu.getSlots(semantic);
            for (int i = 0; i < slots.size(); i++) {
                Slot slot = slots.get(i);

                // Special case for slots with overridable width/height, which use widget styles instead of
                // semantic based slot positioning
                if (slot instanceof ResizableSlot resizableSlot) {
                    var widgetStyle = style.getWidget(resizableSlot.getStyleId());
                    var pos = widgetStyle.resolve(getBounds(false));
                    slot.x = pos.getX();
                    slot.y = pos.getY();
                    resizableSlot.setWidth(widgetStyle.getWidth());
                    resizableSlot.setHeight(widgetStyle.getHeight());
                } else {
                    Point pos = getSlotPosition(entry.getValue(), i);

                    slot.x = pos.getX();
                    slot.y = pos.getY();
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

    private Rect2i getBounds(boolean absolute) {
        if (absolute) {
            return new Rect2i(leftPos, topPos, imageWidth, imageHeight);
        } else {
            return new Rect2i(0, 0, imageWidth, imageHeight);
        }
    }

    private List<Slot> getInventorySlots() {
        return this.menu.slots;
    }

    /**
     * This method is called directly before rendering the screen, and should be used to perform layout, and other
     * rendering-related updates.
     */
    @OverridingMethodsMustInvokeSuper
    protected void updateBeforeRender() {
    }

    @Override
    public void render(PoseStack poseStack, final int mouseX, final int mouseY, final float partialTicks) {
        this.updateBeforeRender();
        this.widgets.updateBeforeRender();

        super.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTicks);

        renderTooltips(poseStack, mouseX, mouseY);

        if (AEConfig.instance().isShowDebugGuiOverlays()) {
            // Show a green overlay on exclusion zones
            List<Rect2i> exclusionZones = getExclusionZones();
            for (Rect2i rectangle2d : exclusionZones) {
                fillRect(poseStack, rectangle2d, 0x7f00FF00);
            }

            hLine(poseStack, leftPos, leftPos + imageWidth - 1, topPos, 0xFFFFFFFF);
            hLine(poseStack, leftPos, leftPos + imageWidth - 1, topPos + imageHeight - 1, 0xFFFFFFFF);
            vLine(poseStack, leftPos, topPos, topPos + imageHeight, 0xFFFFFFFF);
            vLine(poseStack, leftPos + imageWidth - 1, topPos, topPos + imageHeight - 1, 0xFFFFFFFF);
        }
    }

    /**
     * Get the potential result of emptying an item for the purposes of setting a filter. Null even if the item could be
     * emptied, but the filter doesn't support the resulting key.
     */
    private EmptyingAction getEmptyingAction(Slot slot, ItemStack carried) {
        if (!(slot instanceof AppEngSlot appEngSlot) || carried.isEmpty()) {
            return null;
        }

        if (!(appEngSlot.getInventory() instanceof ConfigMenuInventory configInv)) {
            return null;
        }

        // See if we should offer the left-/right-click differentiation for setting a different filter
        var emptyingAction = StackInteractions.getEmptyingAction(menu.getCarried());
        if (emptyingAction != null) {
            var wrappedStack = GenericStack.wrapInItemStack(new GenericStack(emptyingAction.what(), 1));
            if (configInv.isItemValid(slot.slot, wrappedStack)) {
                return emptyingAction;
            }
        }

        return null;
    }

    private boolean renderEmptyingTooltip(PoseStack poseStack, int mouseX, int mouseY) {
        // See if we should offer the left-/right-click differentiation for setting a different filter
        var emptyingAction = getEmptyingAction(this.hoveredSlot, menu.getCarried());
        if (emptyingAction != null) {
            renderTooltip(poseStack, List.of(
                    new TextComponent("Left-Click: Set ").append(menu.getCarried().getHoverName())
                            .withStyle(ChatFormatting.GRAY).getVisualOrderText(),
                    new TextComponent("Right-Click: Set ").append(emptyingAction.description())
                            .withStyle(ChatFormatting.GRAY).getVisualOrderText()),
                    mouseX, mouseY);
            return true;
        }

        return false;
    }

    /**
     * Renders a potential tooltip (from one of the possible tooltip sources)
     */
    private void renderTooltips(PoseStack poseStack, int mouseX, int mouseY) {
        if (renderEmptyingTooltip(poseStack, mouseX, mouseY)) {
            return;
        } else if (this.hoveredSlot instanceof AppEngSlot appEngSlot) {
            var customTooltip = appEngSlot.getCustomTooltip(this::getTooltipFromItem, menu.getCarried());
            if (customTooltip != null) {
                this.renderTooltip(poseStack, customTooltip, Optional.empty(), mouseX, mouseY);
            }
        }

        this.renderTooltip(poseStack, mouseX, mouseY);

        // The line above should have render a tooltip if this condition is true, and no
        // additional tooltips should be shown
        if (this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            return;
        }

        for (var c : this.renderables) {
            if (c instanceof ITooltip tooltipWidget) {
                if (!tooltipWidget.isTooltipAreaVisible()) {
                    continue;
                }

                var area = tooltipWidget.getTooltipArea();
                if (mouseX >= area.getX() && mouseY >= area.getY() &&
                        mouseX < area.getX() + area.getWidth()
                        && mouseY < area.getY() + area.getHeight()) {
                    var tooltip = new Tooltip(tooltipWidget.getTooltipMessage());
                    if (!tooltip.getContent().isEmpty()) {
                        drawTooltip(poseStack, tooltip, mouseX, mouseY);
                    }
                }
            }
        }

        // Widget-container uses screen-relative coordinates while the rest uses window-relative
        Tooltip tooltip = this.widgets.getTooltip(mouseX - leftPos, mouseY - topPos);
        if (tooltip != null) {
            drawTooltip(poseStack, tooltip, mouseX, mouseY);
        }
    }

    private void drawTooltip(PoseStack poseStack, Tooltip tooltip, int mouseX, int mouseY) {
        drawTooltip(poseStack, mouseX, mouseY, tooltip.getContent());
    }

    // FIXME FABRIC: move out to json (?)
    private static final Style TOOLTIP_HEADER = Style.EMPTY.applyFormat(ChatFormatting.WHITE);
    private static final Style TOOLTIP_BODY = Style.EMPTY.applyFormat(ChatFormatting.GRAY);

    public void drawTooltip(PoseStack poseStack, int x, int y, List<Component> lines) {
        if (lines.isEmpty()) {
            return;
        }

        // Max width should be half screen with some padding.
        // Vanilla will place the tooltip on the right or left of the cursor
        // automatically, but uses a 12px offset (we use 20px for some extra space)
        int maxWidth = width / 2 - 20;

        // Make the first line white
        // All lines after the first are colored gray
        List<FormattedCharSequence> styledLines = new ArrayList<>(lines.size());
        for (int i = 0; i < lines.size(); i++) {
            Style style = i == 0 ? TOOLTIP_HEADER : TOOLTIP_BODY;
            styledLines.addAll(
                    ComponentRenderUtils.wrapComponents(lines.get(i).copy().withStyle(s -> style), maxWidth, font));
        }
        this.renderTooltip(poseStack, styledLines, x, y);

    }

    @Override
    protected final void renderLabels(PoseStack poseStack, final int x, final int y) {
        final int ox = this.leftPos;
        final int oy = this.topPos;

        widgets.drawForegroundLayer(poseStack, getBlitOffset(), getBounds(false), new Point(x - ox, y - oy));

        this.drawFG(poseStack, ox, oy, x, y);

        if (style != null) {
            for (Map.Entry<String, Text> entry : style.getText().entrySet()) {
                // Process text overrides
                TextOverride override = textOverrides.get(entry.getKey());
                drawText(poseStack, entry.getValue(), override);
            }
        }
    }

    private void drawText(PoseStack poseStack, Text text, @Nullable TextOverride override) {
        // Don't draw if the screen decided to hide this
        if (override != null && override.isHidden()) {
            return;
        }

        int color = style.getColor(text.getColor()).toARGB();

        // Allow overrides for which content is shown
        Component content = text.getText();
        if (override != null && override.getContent() != null) {
            content = override.getContent();
        }

        Point pos = text.getPosition().resolve(getBounds(false));

        if (text.isCenterHorizontally()) {
            int textWidth = this.font.width(content);
            pos = pos.move(-textWidth / 2, 0);
        }

        this.font.draw(
                poseStack,
                content,
                pos.getX(),
                pos.getY(),
                color);
    }

    public void drawFG(PoseStack poseStack, int offsetX, int offsetY, int mouseX, int mouseY) {
    }

    @Override
    protected final void renderBg(PoseStack poseStack, final float f, final int x,
            final int y) {

        this.drawBG(poseStack, leftPos, topPos, x, y, f);

        widgets.drawBackgroundLayer(poseStack, getBlitOffset(), getBounds(true), new Point(x - leftPos, y - topPos));

        for (final Slot slot : this.getInventorySlots()) {
            if (slot instanceof IOptionalSlot) {
                drawOptionalSlotBackground(poseStack, (IOptionalSlot) slot, false);
            }
        }
    }

    private void drawOptionalSlotBackground(PoseStack poseStack, IOptionalSlot slot, boolean alwaysDraw) {
        // If a slot is optional and doesn't currently render, we still need to provide a background for it
        if (alwaysDraw || slot.isRenderDisabled()) {
            // If the slot is disabled, shade the background overlay
            float alpha = slot.isSlotEnabled() ? 1.0f : 0.4f;

            Point pos = slot.getBackgroundPos();

            Icon.SLOT_BACKGROUND.getBlitter()
                    .dest(leftPos + pos.getX(), topPos + pos.getY())
                    .color(1, 1, 1, alpha)
                    .blit(poseStack, getBlitOffset());
        }
    }

    // Convert global mouse x,y to relative Point
    private Point getMousePoint(double x, double y) {
        return new Point((int) Math.round(x - leftPos), (int) Math.round(y - topPos));
    }

    @Override
    public boolean mouseScrolled(double x, double y, double wheelDelta) {
        if (wheelDelta != 0 && widgets.onMouseWheel(getMousePoint(x, y), wheelDelta)) {
            return true;
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
                for (var widget : this.children()) {
                    if (widget.isMouseOver(xCoord, yCoord)) {
                        return super.mouseClicked(xCoord, yCoord, 0);
                    }
                }
            } finally {
                handlingRightClick = false;
            }
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
        var itemstack = getMenu().getCarried();

        Point mousePos = new Point((int) Math.round(mouseX - leftPos), (int) Math.round(mouseY - topPos));
        if (widgets.onMouseDrag(mousePos, mouseButton)) {
            return true;
        }

        if (slot instanceof FakeSlot && !itemstack.isEmpty()) {
            this.drag_click.add(slot);
            if (this.drag_click.size() > 1) {
                for (final Slot dr : this.drag_click) {
                    var p = new InventoryActionPacket(
                            mouseButton == 0 ? InventoryAction.PICKUP_OR_SET_DOWN : InventoryAction.PLACE_SINGLE,
                            dr.index, 0);
                    NetworkHandler.instance().sendToServer(p);
                }
            }

            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, mouseButton, dragX, dragY);
        }
    }

    @Override
    protected void slotClicked(@Nullable Slot slot, final int slotIdx, final int mouseButton,
            final ClickType clickType) {

        // Do not allow clicks on disabled player inventory slots
        if (slot instanceof DisabledSlot) {
            return;
        }

        if (this.drag_click.size() <= 1
                && mouseButton == 1
                && getEmptyingAction(slot, menu.getCarried()) != null) {
            var p = new InventoryActionPacket(InventoryAction.EMPTY_ITEM, slotIdx, 0);
            NetworkHandler.instance().sendToServer(p);
            return;
        }

        if (slot instanceof FakeSlot) {
            if (this.drag_click.size() > 1) {
                return;
            }

            var action = mouseButton == 1 ? InventoryAction.SPLIT_OR_PLACE_SINGLE
                    : InventoryAction.PICKUP_OR_SET_DOWN;
            var p = new InventoryActionPacket(action, slotIdx, 0);
            NetworkHandler.instance().sendToServer(p);
            return;
        }

        if (slot instanceof CraftingTermSlot) {
            InventoryAction action;
            if (hasShiftDown()) {
                action = InventoryAction.CRAFT_SHIFT;
            } else {
                // Craft stack on right-click, craft single on left-click
                action = mouseButton == 1 ? InventoryAction.CRAFT_STACK : InventoryAction.CRAFT_ITEM;
            }

            final InventoryActionPacket p = new InventoryActionPacket(action, slotIdx, 0);
            NetworkHandler.instance().sendToServer(p);

            return;
        }

        if (slot != null &&
                InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_SPACE)) {
            int slotNum = slot.index;
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
                this.dbl_whichItem = slot.hasItem() ? slot.getItem().copy() : ItemStack.EMPTY;
            } else if (!this.dbl_whichItem.isEmpty()) {
                // a replica of the weird broken vanilla feature.

                final List<Slot> slots = this.getInventorySlots();
                for (final Slot inventorySlot : slots) {
                    if (inventorySlot != null && inventorySlot.mayPickup(getPlayer()) && inventorySlot.hasItem()
                            && isSameInventory(inventorySlot, slot)
                            && AbstractContainerMenu.canItemQuickReplace(inventorySlot, this.dbl_whichItem, true)) {
                        this.slotClicked(inventorySlot, inventorySlot.index, 0, ClickType.QUICK_MOVE);
                    }
                }
                this.dbl_whichItem = ItemStack.EMPTY;
            }

            this.disableShiftClick = false;
        }

        super.slotClicked(slot, slotIdx, mouseButton, clickType);
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int screenX, int screenY, int button) {
        // Consider clicks within attached compound widgets as still inside the screen
        var mousePos = new Point((int) Math.round(mouseX - screenX), (int) Math.round(mouseY - screenY));
        if (widgets.hitTest(mousePos)) {
            return false;
        }

        return super.hasClickedOutside(mouseX, mouseY, screenX, screenY, button);
    }

    @Override
    protected boolean checkHotbarKeyPressed(int keyCode, int scanCode) {
        return checkHotbarKeys(InputConstants.getKey(keyCode, scanCode));
    }

    protected LocalPlayer getPlayer() {
        // Our UIs are usually not opened when not in-game, so this should not be a
        // problem
        return Objects.requireNonNull(getMinecraft().player);
    }

    protected boolean checkHotbarKeys(final Key input) {
        final Slot theSlot = this.getSlotUnderMouse();

        if (getMenu().getCarried().isEmpty() && theSlot != null) {
            for (int j = 0; j < 9; ++j) {
                if (getMinecraft().options.keyHotbarSlots[j].matches(input.getValue(), -1)) {
                    final List<Slot> slots = this.getInventorySlots();
                    for (final Slot s : slots) {
                        if (s.slot == j && s.container == this.menu.getPlayerInventory()
                                && !s.mayPickup(this.menu.getPlayerInventory().player)) {
                            return false;
                        }
                    }

                    if (theSlot.getMaxStackSize() == 64) {
                        this.slotClicked(theSlot, theSlot.index, j, ClickType.SWAP);
                        return true;
                    } else {
                        for (final Slot s : slots) {
                            if (s.slot == j
                                    && s.container == this.menu.getPlayerInventory()) {
                                NetworkHandler.instance()
                                        .sendToServer(new SwapSlotsPacket(s.index, theSlot.index));
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    @Override
    protected boolean isHovering(Slot slot, double x, double y) {
        if (slot instanceof ResizableSlot resizableSlot) {
            var width = resizableSlot.getWidth();
            var height = resizableSlot.getHeight();
            return this.isHovering(slot.x, slot.y, width, height, x, y);
        }
        return super.isHovering(slot, x, y);
    }

    protected Slot getSlot(final int mouseX, final int mouseY) {
        var slots = this.getInventorySlots();
        for (var slot : slots) {
            if (this.isHovering(slot, mouseX, mouseY)) {
                return slot;
            }
        }

        return null;
    }

    public void drawBG(PoseStack poseStack, int offsetX, int offsetY, int mouseX, int mouseY,
            float partialTicks) {
        if (style.getBackground() != null) {
            style.getBackground().dest(offsetX, offsetY).blit(poseStack, getBlitOffset());
        }
    }

    public void drawItem(final int x, final int y, final ItemStack is) {
        this.itemRenderer.blitOffset = 100.0F;
        this.itemRenderer.renderAndDecorateItem(is, x, y);
        this.itemRenderer.blitOffset = 0.0F;
    }

    protected Component getGuiDisplayName(final Component in) {
        return title.getString().isEmpty() ? in : title;
    }

    /**
     * This overrides the base-class method through some access transformer hackery...
     */
    @Override
    public void renderSlot(PoseStack poseStack, Slot s) {
        if (s instanceof AppEngSlot appEngSlot) {
            try {
                renderAppEngSlot(poseStack, appEngSlot);
            } catch (final Exception err) {
                AELog.warn("[AppEng] AE prevented crash while drawing slot: " + err);
            }
        } else {
            super.renderSlot(poseStack, s);
        }
    }

    private void renderAppEngSlot(PoseStack poseStack, AppEngSlot s) {
        var is = s.getItem();

        // If the slot has a background icon, render it, but only if the slot is empty
        // or it requests the icon to be always drawn
        if ((s.renderIconWithItem() || is.isEmpty()) && s.isSlotEnabled() && s.getIcon() != null) {
            s.getIcon().getBlitter()
                    .dest(s.x, s.y)
                    .opacity(s.getOpacityOfIcon())
                    .blit(poseStack, getBlitOffset());
        }

        // Draw a red background for slots that are in an invalid state
        if (!s.isValid()) {
            fill(poseStack, s.x, s.y, 16 + s.x, 16 + s.y, 0x66ff6666);
        }

        // Makes it so the first call to s.getStack will return getDisplayStack instead, the finally is there
        // just for making absolutely sure the flag gets reset (it should be reset inside of getStack)
        s.setRendering(true);
        try {
            super.renderSlot(poseStack, s);
        } finally {
            s.setRendering(false);
        }
    }

    public void bindTexture(final String file) {
        final ResourceLocation loc = new ResourceLocation(AppEng.MOD_ID, "textures/" + file);
        RenderSystem.setShaderTexture(0, loc);
    }

    @Override
    public void containerTick() {
        super.containerTick();

        widgets.tick();

        for (GuiEventListener child : children()) {
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
    public List<Rect2i> getExclusionZones() {
        List<Rect2i> result = new ArrayList<>(2);
        widgets.addExclusionZones(result, getBounds(true));
        return result;
    }

    protected void fillRect(PoseStack poseStack, Rect2i rect, int color) {
        fill(poseStack, rect.getX(), rect.getY(), rect.getX() + rect.getWidth(), rect.getY() + rect.getHeight(), color);
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
                for (Slot slot : menu.getSlots(semantic)) {
                    slot.x = HIDDEN_SLOT_POS.getX();
                    slot.y = HIDDEN_SLOT_POS.getY();
                }
            }
        } else if (hiddenSlots.remove(semantic) && style != null) {
            positionSlots(style);
        }
    }

    /**
     * Changes the text that will be displayed for a text defined in this screen's style file.
     */
    protected final void setTextContent(String id, Component content) {
        getOrCreateTextOverride(id).setContent(content);
    }

    public ScreenStyle getStyle() {
        return style;
    }

    /**
     * Return the key of the resource if there is one at the given window coordinates. This is used by JEI/REI to allow
     * the U and R hotkeys to work for ingredients (such as fluids) that are not stored in normal slots, and for
     * ingredients wrapped in {@link appeng.items.misc.WrappedGenericStack}.
     * <p/>
     * The given coordinates are in window space.
     */
    @Nullable
    public GenericStack getStackUnderMouse(double mouseX, double mouseY) {
        // First check the vanilla slots
        if (hoveredSlot != null) {
            var item = hoveredSlot.getItem();
            return GenericStack.unwrapItemStack(item);
        }

        return null;
    }

    public final int getGuiLeft() {
        return this.leftPos;
    }

    public final int getGuiTop() {
        return this.topPos;
    }

    public final Minecraft getMinecraft() {
        return minecraft;
    }

    public final Slot getSlotUnderMouse() {
        return hoveredSlot;
    }

    public static boolean isSameInventory(Slot a, Slot b) {
        if (a instanceof AppEngSlot appEngSlotA && b instanceof AppEngSlot appEngSlotB) {
            return appEngSlotA.container == appEngSlotB.container;
        }
        return a.container == b.container;
    }

    @Override
    public List<Component> getTooltipFromItem(ItemStack stack) {
        var unwrapped = GenericStack.unwrapItemStack(stack);
        if (unwrapped != null) {
            return AEStackRendering.getTooltip(unwrapped.what());
        }
        return super.getTooltipFromItem(stack);
    }

    /**
     * Used by mixin to render the slot highlight.
     */
    public void renderCustomSlotHighlight(PoseStack poseStack, int x, int y, int z) {
        int w, h;
        if (this.hoveredSlot instanceof ResizableSlot resizableSlot) {
            w = resizableSlot.getWidth();
            h = resizableSlot.getHeight();
        } else {
            w = 16;
            h = 16;
        }

        // Same as the Vanilla method, just with dynamic width and height
        RenderSystem.disableDepthTest();
        RenderSystem.colorMask(true, true, true, false);
        fillGradient(poseStack, x, y, x + w, y + h, 0x80ffffff, 0x80ffffff, z);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.enableDepthTest();
    }

}
