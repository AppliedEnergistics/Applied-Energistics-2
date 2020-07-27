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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.mojang.blaze3d.systems.RenderSystem;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import alexiil.mc.lib.attributes.fluid.render.FluidRenderFace;
import alexiil.mc.lib.attributes.fluid.render.FluidVolumeRenderer;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.widgets.CustomSlotWidget;
import appeng.client.gui.widgets.ITickingWidget;
import appeng.client.gui.widgets.ITooltip;
import appeng.client.gui.widgets.Scrollbar;
import appeng.client.me.InternalSlotME;
import appeng.client.me.SlotDisconnected;
import appeng.client.me.SlotME;
import appeng.client.render.StackSizeRenderer;
import appeng.container.AEBaseContainer;
import appeng.container.slot.AppEngCraftingSlot;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.AppEngSlot.CalculatedValidity;
import appeng.container.slot.CraftingTermSlot;
import appeng.container.slot.DisabledSlot;
import appeng.container.slot.FakeSlot;
import appeng.container.slot.IOptionalSlot;
import appeng.container.slot.InaccessibleSlot;
import appeng.container.slot.OutputSlot;
import appeng.container.slot.PatternTermSlot;
import appeng.container.slot.RestrictedInputSlot;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.InventoryActionPacket;
import appeng.core.sync.packets.SwapSlotsPacket;
import appeng.fluids.client.render.FluidStackSizeRenderer;
import appeng.fluids.container.slots.IMEFluidSlot;
import appeng.helpers.InventoryAction;
import appeng.mixins.SlotMixin;

public abstract class AEBaseScreen<T extends AEBaseContainer> extends HandledScreen<T> {

    public static final int COLOR_DARK_GRAY = 4210752;
    private final List<InternalSlotME> meSlots = new ArrayList<>();
    // drag y
    private final Set<Slot> drag_click = new HashSet<>();
    private final StackSizeRenderer stackSizeRenderer = new StackSizeRenderer();
    private final FluidStackSizeRenderer fluidStackSizeRenderer = new FluidStackSizeRenderer();
    private Scrollbar myScrollBar = null;
    private boolean disableShiftClick = false;
    private Stopwatch dbl_clickTimer = Stopwatch.createStarted();
    private ItemStack dbl_whichItem = ItemStack.EMPTY;
    private Slot bl_clicked;
    protected final List<CustomSlotWidget> guiSlots = new ArrayList<>();

    public AEBaseScreen(T container, PlayerInventory playerInventory, Text title) {
        super(container, playerInventory, title);
    }

    public MinecraftClient getClient() {
        return Preconditions.checkNotNull(client);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public void init() {
        super.init();

        final List<Slot> slots = this.getInventorySlots();
        slots.removeIf(slot -> slot instanceof SlotME);

        for (final InternalSlotME me : this.meSlots) {
            slots.add(new SlotME(me));
        }
    }

    private List<Slot> getInventorySlots() {
        return this.handler.slots;
    }

    @Override
    public void render(MatrixStack matrices, final int mouseX, final int mouseY, final float partialTicks) {
        super.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, partialTicks);

        RenderSystem.pushMatrix();
        RenderSystem.translatef(this.x, this.y, 0.0F);
        RenderSystem.enableDepthTest();
        for (final CustomSlotWidget c : this.guiSlots) {
            this.drawGuiSlot(matrices, c, mouseX, mouseY, partialTicks);
        }
        RenderSystem.disableDepthTest();
        for (final CustomSlotWidget c : this.guiSlots) {
            this.drawTooltip(matrices, c, mouseX - this.x, mouseY - this.y);
        }
        RenderSystem.popMatrix();
        RenderSystem.enableDepthTest();

        this.drawMouseoverTooltip(matrices, mouseX, mouseY);

        for (final Object c : this.buttons) {
            if (c instanceof ITooltip) {
                this.drawTooltip(matrices, (ITooltip) c, mouseX, mouseY);
            }
        }
    }

    protected void drawGuiSlot(MatrixStack matrices, CustomSlotWidget slot, int mouseX, int mouseY,
            float partialTicks) {
        if (slot.isSlotEnabled()) {
            final int left = slot.xPos();
            final int top = slot.yPos();
            final int right = left + slot.getWidth();
            final int bottom = top + slot.getHeight();

            slot.drawContent(getClient(), mouseX, mouseY, partialTicks);

            if (this.isPointWithinBounds(left, top, slot.getWidth(), slot.getHeight(), mouseX, mouseY)
                    && slot.canClick(getPlayer())) {
                RenderSystem.colorMask(true, true, true, false);
                this.fillGradient(matrices, left, top, right, bottom, -2130706433, -2130706433);
                RenderSystem.colorMask(true, true, true, true);
            }
        }
    }

    private void drawTooltip(MatrixStack matrices, ITooltip tooltip, int mouseX, int mouseY) {
        final int x = tooltip.xPos();
        int y = tooltip.yPos();

        if (x < mouseX && x + tooltip.getWidth() > mouseX && tooltip.isVisible()) {
            if (y < mouseY && y + tooltip.getHeight() > mouseY) {
                if (y < 15) {
                    y = 15;
                }

                final Text msg = tooltip.getMessage();
                if (msg != null && !msg.getString().isEmpty()) {
                    this.drawTooltip(matrices, x + 11, y + 4, msg);
                }
            }
        }
    }

    protected void drawTooltip(MatrixStack matrices, int x, int y, Text message) {
        String[] lines = message.getString().split("\n"); // FIXME FABRIC
        List<Text> textLines = Arrays.stream(lines).map(LiteralText::new).collect(Collectors.toList());
        this.drawTooltip(matrices, x, y, textLines);
    }

    // FIXME FABRIC: move out to json (?)
    private static final Style TOOLTIP_HEADER = Style.EMPTY.withColor(Formatting.WHITE);
    private static final Style TOOLTIP_BODY = Style.EMPTY.withColor(Formatting.GRAY);

    protected void drawTooltip(MatrixStack matrices, int x, int y, List<Text> lines) {
        if (lines.isEmpty()) {
            return;
        }

        // Make the first line white
        // All lines after the first are colored gray
        List<Text> styledLines = new ArrayList<>(lines.size());
        for (int i = 0; i < lines.size(); i++) {
            Style style = (i == 0) ? TOOLTIP_HEADER : TOOLTIP_BODY;
            styledLines.add(lines.get(i).copy().styled(s -> style));
        }

        this.renderTooltip(matrices, styledLines, x, y);
    }

    @Override
    protected final void drawForeground(MatrixStack matrices, final int x, final int y) {
        final int ox = this.x; // (width - xSize) / 2;
        final int oy = this.y; // (height - ySize) / 2;
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        if (this.getScrollBar() != null) {
            this.getScrollBar().draw(matrices, this);
        }

        this.drawFG(matrices, ox, oy, x, y);
    }

    public abstract void drawFG(MatrixStack matrices, int offsetX, int offsetY, int mouseX, int mouseY);

    @Override
    protected final void drawBackground(MatrixStack matrices, final float f, final int x, final int y) {
        final int ox = this.x; // (width - xSize) / 2;
        final int oy = this.y; // (height - ySize) / 2;
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.drawBG(matrices, ox, oy, x, y, f);

        final List<Slot> slots = this.getInventorySlots();
        for (final Slot slot : slots) {
            if (slot instanceof IOptionalSlot) {
                final IOptionalSlot optionalSlot = (IOptionalSlot) slot;
                if (optionalSlot.isRenderDisabled()) {
                    final AppEngSlot aeSlot = (AppEngSlot) slot;
                    if (aeSlot.isSlotEnabled()) {
                        drawTexture(matrices, ox + aeSlot.x - 1, oy + aeSlot.y - 1, optionalSlot.getSourceX() - 1,
                                optionalSlot.getSourceY() - 1, 18, 18);
                    } else {
                        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 0.4F);
                        RenderSystem.enableBlend();
                        drawTexture(matrices, ox + aeSlot.x - 1, oy + aeSlot.y - 1, optionalSlot.getSourceX() - 1,
                                optionalSlot.getSourceY() - 1, 18, 18);
                        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                    }
                }
            }
        }

        for (final CustomSlotWidget slot : this.guiSlots) {
            slot.drawBackground(matrices, ox, oy, getZOffset());
        }

    }

    @Override
    public boolean mouseClicked(final double xCoord, final double yCoord, final int btn) {
        this.drag_click.clear();

        if (btn == 1) {
            for (final Object o : this.buttons) {
                final AbstractButtonWidget widget = (AbstractButtonWidget) o;
                if (widget.isMouseOver(xCoord, yCoord)) {
                    return super.mouseClicked(xCoord, yCoord, 0);
                }
            }
        }

        for (CustomSlotWidget slot : this.guiSlots) {
            if (this.isPointWithinBounds(slot.xPos(), slot.yPos(), slot.getWidth(), slot.getHeight(), xCoord, yCoord)
                    && slot.canClick(getPlayer())) {
                slot.slotClicked(getPlayer().inventory.getCursorStack(), btn);
            }
        }

        if (this.getScrollBar() != null) {
            this.getScrollBar().click(xCoord - this.x, yCoord - this.y);
        }

        return super.mouseClicked(xCoord, yCoord, btn);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dragX, double dragY) {

        final Slot slot = this.getSlot((int) mouseX, (int) mouseY);
        final ItemStack itemstack = getPlayer().inventory.getCursorStack();

        if (this.getScrollBar() != null) {
            // FIXME: Coordinate system of mouseX/mouseY is unclear
            this.getScrollBar().click((int) mouseX - this.x, (int) mouseY - this.y);
        }

        if (slot instanceof FakeSlot && !itemstack.isEmpty()) {
            this.drag_click.add(slot);
            if (this.drag_click.size() > 1) {
                for (final Slot dr : this.drag_click) {
                    final InventoryActionPacket p = new InventoryActionPacket(
                            mouseButton == 0 ? InventoryAction.PICKUP_OR_SET_DOWN : InventoryAction.PLACE_SINGLE, dr.id,
                            0);
                    NetworkHandler.instance().sendToServer(p);
                }
            }

            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, mouseButton, dragX, dragY);
        }
    }

    // TODO 1.9.4 aftermath - Whole SlotActionType thing, to be checked.
    @Override
    protected void onMouseClick(final Slot slot, final int slotIdx, final int mouseButton,
            final SlotActionType clickType) {
        final PlayerEntity player = getPlayer();

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

        if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_SPACE)) {
            if (this.enableSpaceClicking()) {
                IAEItemStack stack = null;
                if (slot instanceof SlotME) {
                    stack = ((SlotME) slot).getAEStack();
                }

                int slotNum = this.getInventorySlots().size();

                if (!(slot instanceof SlotME) && slot != null) {
                    slotNum = slot.id;
                }

                this.handler.setTargetStack(stack);
                final InventoryActionPacket p = new InventoryActionPacket(InventoryAction.MOVE_REGION, slotNum, 0);
                NetworkHandler.instance().sendToServer(p);
                return;
            }
        }

        if (slot instanceof SlotDisconnected) {
            InventoryAction action = null;

            switch (clickType) {
                case PICKUP: // pickup / set-down.
                    action = (mouseButton == 1) ? InventoryAction.SPLIT_OR_PLACE_SINGLE
                            : InventoryAction.PICKUP_OR_SET_DOWN;
                    break;
                case QUICK_MOVE:
                    action = (mouseButton == 1) ? InventoryAction.PICKUP_SINGLE : InventoryAction.SHIFT_CLICK;
                    break;

                case CLONE: // creative dupe:

                    if (player.isCreative()) {
                        action = InventoryAction.CREATIVE_DUPLICATE;
                    }

                    break;

                default:
                case THROW: // drop item:
            }

            if (action != null) {
                final InventoryActionPacket p = new InventoryActionPacket(action, getSlotIndex(slot),
                        ((SlotDisconnected) slot).getSlot().getId());
                NetworkHandler.instance().sendToServer(p);
            }

            return;
        }

        if (slot instanceof SlotME) {
            InventoryAction action = null;
            IAEItemStack stack = null;

            switch (clickType) {
                case PICKUP: // pickup / set-down.
                    action = (mouseButton == 1) ? InventoryAction.SPLIT_OR_PLACE_SINGLE
                            : InventoryAction.PICKUP_OR_SET_DOWN;
                    stack = ((SlotME) slot).getAEStack();

                    if (stack != null && action == InventoryAction.PICKUP_OR_SET_DOWN && stack.getStackSize() == 0
                            && player.inventory.getCursorStack().isEmpty()) {
                        action = InventoryAction.AUTO_CRAFT;
                    }

                    break;
                case QUICK_MOVE:
                    action = (mouseButton == 1) ? InventoryAction.PICKUP_SINGLE : InventoryAction.SHIFT_CLICK;
                    stack = ((SlotME) slot).getAEStack();
                    break;

                case CLONE: // creative dupe:

                    stack = ((SlotME) slot).getAEStack();
                    if (stack != null && stack.isCraftable()) {
                        action = InventoryAction.AUTO_CRAFT;
                    } else if (player.isCreative()) {
                        final IAEItemStack slotItem = ((SlotME) slot).getAEStack();
                        if (slotItem != null) {
                            action = InventoryAction.CREATIVE_DUPLICATE;
                        }
                    }
                    break;

                default:
                case THROW: // drop item:
            }

            if (action != null) {
                this.handler.setTargetStack(stack);
                final InventoryActionPacket p = new InventoryActionPacket(action, this.getInventorySlots().size(), 0);
                NetworkHandler.instance().sendToServer(p);
            }

            return;
        }

        if (!this.disableShiftClick && hasShiftDown() && mouseButton == 0) {
            this.disableShiftClick = true;

            if (this.dbl_whichItem.isEmpty() || this.bl_clicked != slot
                    || this.dbl_clickTimer.elapsed(TimeUnit.MILLISECONDS) > 250) {
                // some simple double click logic.
                this.bl_clicked = slot;
                this.dbl_clickTimer = Stopwatch.createStarted();
                if (slot != null) {
                    this.dbl_whichItem = slot.hasStack() ? slot.getStack().copy() : ItemStack.EMPTY;
                } else {
                    this.dbl_whichItem = ItemStack.EMPTY;
                }
            } else if (!this.dbl_whichItem.isEmpty()) {
                // a replica of the weird broken vanilla feature.

                final List<Slot> slots = this.getInventorySlots();
                for (final Slot inventorySlot : slots) {
                    if (inventorySlot != null && inventorySlot.canTakeItems(getPlayer()) && inventorySlot.hasStack()
                            && inventorySlot.inventory == slot.inventory
                            && ScreenHandler.canInsertItemIntoSlot(inventorySlot, this.dbl_whichItem, true)) {
                        this.onMouseClick(inventorySlot, inventorySlot.id, 0, SlotActionType.QUICK_MOVE);
                    }
                }
                this.dbl_whichItem = ItemStack.EMPTY;
            }

            this.disableShiftClick = false;
        }

        super.onMouseClick(slot, slotIdx, mouseButton, clickType);
    }

    protected ClientPlayerEntity getPlayer() {
        // Our UIs are usually not opened when not in-game, so this should not be a
        // problem
        return Preconditions.checkNotNull(getClient().player);
    }

    protected int getSlotIndex(Slot slot) {
        return ((SlotMixin) slot).getIndex();
    }

    protected boolean checkHotbarKeys(int keyCode, int scanCode) {
        final Slot theSlot = this.focusedSlot;

        if (getPlayer().inventory.getCursorStack().isEmpty() && theSlot != null) {
            for (int j = 0; j < 9; ++j) {
                if (getClient().options.keysHotbar[j].matchesKey(keyCode, scanCode)) {
                    final List<Slot> slots = this.getInventorySlots();
                    for (final Slot s : slots) {
                        if (getSlotIndex(s) == j && s.inventory == this.handler.getPlayerInv()) {
                            if (!s.canTakeItems(this.handler.getPlayerInv().player)) {
                                return false;
                            }
                        }
                    }

                    if (theSlot.getMaxStackAmount() == 64) {
                        this.onMouseClick(theSlot, theSlot.id, j, SlotActionType.SWAP);
                        return true;
                    } else {
                        for (final Slot s : slots) {
                            if (getSlotIndex(s) == j && s.inventory == this.handler.getPlayerInv()) {
                                NetworkHandler.instance().sendToServer(new SwapSlotsPacket(s.id, theSlot.id));
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
    public void removed() {
        super.removed();
    }

    protected Slot getSlot(final int mouseX, final int mouseY) {
        final List<Slot> slots = this.getInventorySlots();
        for (final Slot slot : slots) {
            // isPointWithinBounds
            if (this.isPointWithinBounds(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
                return slot;
            }
        }

        return null;
    }

    public abstract void drawBG(MatrixStack matrices, int offsetX, int offsetY, int mouseX, int mouseY,
            float partialTicks);

    @Override
    public boolean mouseScrolled(double x, double y, double wheelDelta) {
        if (wheelDelta != 0 && hasShiftDown()) {
            this.mouseWheelEvent(x, y, wheelDelta / Math.abs(wheelDelta));
            return true;
        } else if (wheelDelta != 0 && this.getScrollBar() != null) {
            this.getScrollBar().wheel(wheelDelta);
            return true;
        }
        return false;
    }

    private void mouseWheelEvent(final double x, final double y, final double wheel) {
        final Slot slot = this.getSlot((int) x, (int) y);
        if (slot instanceof SlotME) {
            final IAEItemStack item = ((SlotME) slot).getAEStack();
            if (item != null) {
                this.handler.setTargetStack(item);
                final InventoryAction direction = wheel > 0 ? InventoryAction.ROLL_DOWN : InventoryAction.ROLL_UP;
                final int times = (int) Math.abs(wheel);
                final int inventorySize = this.getInventorySlots().size();
                for (int h = 0; h < times; h++) {
                    final InventoryActionPacket p = new InventoryActionPacket(direction, inventorySize, 0);
                    NetworkHandler.instance().sendToServer(p);
                }
            }
        }
    }

    protected boolean enableSpaceClicking() {
        return true;
    }

    public void bindTexture(final String base, final String file) {
        final Identifier loc = new Identifier(base, "textures/" + file);
        getClient().getTextureManager().bindTexture(loc);
    }

    protected void drawItem(final int x, final int y, final ItemStack is) {
        this.itemRenderer.zOffset = 100.0F;
        this.itemRenderer.renderInGuiWithOverrides(is, x, y);

        this.itemRenderer.zOffset = 0.0F;
    }

    protected Text getGuiDisplayName(final Text in) {
        return this.hasCustomInventoryName() ? new LiteralText(this.getInventoryName()) : in;
    }

    private boolean hasCustomInventoryName() {
        return this.handler.getCustomName() != null;
    }

    private String getInventoryName() {
        return this.handler.getCustomName();
    }

    /**
     * This overrides the base-class method through some access transformer
     * hackery...
     */
    @Override
    public void drawSlot(MatrixStack matrices, Slot s) {
        if (s instanceof SlotME) {

            try {
                if (!this.isPowered()) {
                    fill(matrices, s.x, s.y, 16 + s.x, 16 + s.y, 0x66111111);
                }

                // Annoying but easier than trying to splice into render item
                super.drawSlot(matrices, new Size1Slot((SlotME) s));

                this.stackSizeRenderer.renderStackSize(this.textRenderer, ((SlotME) s).getAEStack(), s.x, s.y);

            } catch (final Exception err) {
                AELog.warn("[AppEng] AE prevented crash while drawing slot: " + err.toString());
            }

            return;
        } else if (s instanceof IMEFluidSlot && ((IMEFluidSlot) s).shouldRenderAsFluid()) {
            final IMEFluidSlot slot = (IMEFluidSlot) s;
            final IAEFluidStack fs = slot.getAEFluidStack();

            if (fs != null && this.isPowered()) {
                List<FluidRenderFace> faces = new ArrayList<>();
                faces.add(FluidRenderFace.createFlatFaceZ(0, 0, 0, 16, 16, 0, 1 / 16., false, false));

                matrices.push();
                matrices.translate(s.x, s.y, 0);

                FluidVolume fluidStack = fs.getFluidStack();
                fluidStack.render(faces, FluidVolumeRenderer.VCPS, matrices);
                RenderSystem.runAsFancy(FluidVolumeRenderer.VCPS::draw);
                matrices.pop();

                this.fluidStackSizeRenderer.renderStackSize(this.textRenderer, fs, s.x, s.y);
            } else if (!this.isPowered()) {
                fill(matrices, s.x, s.y, 16 + s.x, 16 + s.y, 0x66111111);
            }

            return;
        } else {
            try {
                final ItemStack is = s.getStack();
                if (s instanceof AppEngSlot && (((AppEngSlot) s).renderIconWithItem() || is.isEmpty())
                        && (((AppEngSlot) s).shouldDisplay())) {
                    final AppEngSlot aes = (AppEngSlot) s;
                    if (aes.getIcon() >= 0) {
                        this.bindTexture("guis/states.png");

                        try {
                            final int uv_y = aes.getIcon() / 16;
                            final int uv_x = aes.getIcon() - uv_y * 16;

                            RenderSystem.enableBlend();
                            RenderSystem.enableTexture();
                            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                            RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
                            final float par1 = aes.x;
                            final float par2 = aes.y;
                            final float par3 = uv_x * 16;
                            final float par4 = uv_y * 16;

                            final Tessellator tessellator = Tessellator.getInstance();
                            final BufferBuilder vb = tessellator.getBuffer();

                            vb.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR_TEXTURE);

                            final float f1 = 0.00390625F;
                            final float f = 0.00390625F;
                            final float par6 = 16;
                            vb.vertex(par1 + 0, par2 + par6, getZOffset())
                                    .color(1.0f, 1.0f, 1.0f, aes.getOpacityOfIcon())
                                    .texture((par3 + 0) * f, (par4 + par6) * f1).next();
                            final float par5 = 16;
                            vb.vertex(par1 + par5, par2 + par6, getZOffset())
                                    .color(1.0f, 1.0f, 1.0f, aes.getOpacityOfIcon())
                                    .texture((par3 + par5) * f, (par4 + par6) * f1).next();
                            vb.vertex(par1 + par5, par2 + 0, getZOffset())
                                    .color(1.0f, 1.0f, 1.0f, aes.getOpacityOfIcon())
                                    .texture((par3 + par5) * f, (par4 + 0) * f1).next();
                            vb.vertex(par1 + 0, par2 + 0, getZOffset()).color(1.0f, 1.0f, 1.0f, aes.getOpacityOfIcon())
                                    .texture((par3 + 0) * f, (par4 + 0) * f1).next();
                            tessellator.draw();

                        } catch (final Exception err) {
                            err.printStackTrace();
                        }
                    }
                }

                if (!is.isEmpty() && s instanceof AppEngSlot) {
                    AppEngSlot aeSlot = (AppEngSlot) s;
                    if (aeSlot.getIsValid() == CalculatedValidity.NotAvailable) {
                        boolean isValid = s.canInsert(is) || s instanceof OutputSlot || s instanceof AppEngCraftingSlot
                                || s instanceof DisabledSlot || s instanceof InaccessibleSlot || s instanceof FakeSlot
                                || s instanceof RestrictedInputSlot || s instanceof SlotDisconnected;
                        if (isValid && s instanceof RestrictedInputSlot) {
                            try {
                                isValid = ((RestrictedInputSlot) s).isValid(is, getClient().world);
                            } catch (final Exception err) {
                                AELog.debug(err);
                            }
                        }
                        aeSlot.setIsValid(isValid ? CalculatedValidity.Valid : CalculatedValidity.Invalid);
                    }

                    if (aeSlot.getIsValid() == CalculatedValidity.Invalid) {
                        setZOffset(100);
                        this.itemRenderer.zOffset = 100.0F;

                        fill(matrices, s.x, s.y, 16 + s.x, 16 + s.y, 0x66ff6666);

                        setZOffset(0);
                        this.itemRenderer.zOffset = 0.0F;
                    }
                }

                if (s instanceof AppEngSlot) {
                    ((AppEngSlot) s).setDisplay(true);
                    super.drawSlot(matrices, s);
                } else {
                    super.drawSlot(matrices, s);
                }

                return;
            } catch (final Exception err) {
                AELog.warn("[AppEng] AE prevented crash while drawing slot: " + err.toString());
            }
        }
        // do the usual for non-ME Slots.
        super.drawSlot(matrices, s);
    }

    protected boolean isPowered() {
        return true;
    }

    public void bindTexture(final String file) {
        final Identifier loc = new Identifier(AppEng.MOD_ID, "textures/" + file);
        getClient().getTextureManager().bindTexture(loc);
    }

    public void bindTexture(final Identifier loc) {
        getClient().getTextureManager().bindTexture(loc);
    }

    protected Scrollbar getScrollBar() {
        return this.myScrollBar;
    }

    protected void setScrollBar(final Scrollbar myScrollBar) {
        this.myScrollBar = myScrollBar;
    }

    protected List<InternalSlotME> getMeSlots() {
        return this.meSlots;
    }

    public void tick() {
        super.tick();
        for (Element child : children) {
            if (child instanceof ITickingWidget) {
                ((ITickingWidget) child).tick();
            }
        }
    }

}
