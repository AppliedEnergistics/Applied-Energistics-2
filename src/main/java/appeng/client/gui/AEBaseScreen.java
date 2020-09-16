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
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fml.client.gui.GuiUtils;

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

public abstract class AEBaseScreen<T extends AEBaseContainer> extends ContainerScreen<T> {

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

    public AEBaseScreen(T container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
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
        return this.container.inventorySlots;
    }

    @Override
    public void render(MatrixStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        super.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        RenderSystem.pushMatrix();
        RenderSystem.translatef(this.guiLeft, this.guiTop, 0.0F);
        RenderSystem.enableDepthTest();
        for (final CustomSlotWidget c : this.guiSlots) {
            this.drawGuiSlot(matrixStack, c, mouseX, mouseY, partialTicks);
        }
        RenderSystem.disableDepthTest();
        for (final CustomSlotWidget c : this.guiSlots) {
            this.drawTooltip(matrixStack, c, mouseX - this.guiLeft, mouseY - this.guiTop);
        }
        RenderSystem.popMatrix();
        RenderSystem.enableDepthTest();

        this.func_230459_a_(matrixStack, mouseX, mouseY);

        for (final Object c : this.buttons) {
            if (c instanceof ITooltip) {
                this.drawTooltip(matrixStack, (ITooltip) c, mouseX, mouseY);
            }
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

    private void drawTooltip(MatrixStack matrixStack, ITooltip tooltip, int mouseX, int mouseY) {
        final int x = tooltip.getTooltipAreaX();
        int y = tooltip.getTooltipAreaY();

        if (x < mouseX && x + tooltip.getTooltipAreaWidth() > mouseX && tooltip.isTooltipAreaVisible()) {
            if (y < mouseY && y + tooltip.getTooltipAreaHeight() > mouseY) {
                if (y < 15) {
                    y = 15;
                }

                final ITextComponent msg = tooltip.getTooltipMessage();
                this.drawTooltip(matrixStack, x + 11, y + 4, msg);
            }
        }
    }

    protected void drawTooltip(MatrixStack matrices, int x, int y, ITextComponent message) {
        String tooltipText = message.getString();

        if (!tooltipText.isEmpty()) {
            String[] lines = tooltipText.split("\n"); // FIXME FABRIC
            List<ITextComponent> textLines = Arrays.stream(lines).map(StringTextComponent::new)
                    .collect(Collectors.toList());
            this.drawTooltip(matrices, x, y, textLines);
        }
    }

    // FIXME FABRIC: move out to json (?)
    private static final Style TOOLTIP_HEADER = Style.EMPTY.applyFormatting(TextFormatting.WHITE);
    private static final Style TOOLTIP_BODY = Style.EMPTY.applyFormatting(TextFormatting.GRAY);

    protected void drawTooltip(MatrixStack matrices, int x, int y, List<ITextComponent> lines) {
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

        this.renderTooltip(matrices, styledLines, x, y);

    }

    @Override
    protected final void drawGuiContainerForegroundLayer(MatrixStack matrixStack, final int x, final int y) {
        final int ox = this.guiLeft; // (width - xSize) / 2;
        final int oy = this.guiTop; // (height - ySize) / 2;
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        if (this.getScrollBar() != null) {
            this.getScrollBar().draw(matrixStack, this);
        }

        this.drawFG(matrixStack, ox, oy, x, y);
    }

    public abstract void drawFG(MatrixStack matrixStack, int offsetX, int offsetY, int mouseX, int mouseY);

    @Override
    protected final void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, final float f, final int x,
            final int y) {
        final int ox = this.guiLeft; // (width - xSize) / 2;
        final int oy = this.guiTop; // (height - ySize) / 2;
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.drawBG(matrixStack, ox, oy, x, y, f);

        final List<Slot> slots = this.getInventorySlots();
        for (final Slot slot : slots) {
            if (slot instanceof IOptionalSlot) {
                final IOptionalSlot optionalSlot = (IOptionalSlot) slot;
                if (optionalSlot.isRenderDisabled()) {
                    final AppEngSlot aeSlot = (AppEngSlot) slot;
                    if (aeSlot.isSlotEnabled()) {
                        GuiUtils.drawTexturedModalRect(ox + aeSlot.xPos - 1, oy + aeSlot.yPos - 1,
                                optionalSlot.getSourceX() - 1, optionalSlot.getSourceY() - 1, 18, 18, getBlitOffset());
                    } else {
                        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 0.4F);
                        RenderSystem.enableBlend();
                        GuiUtils.drawTexturedModalRect(ox + aeSlot.xPos - 1, oy + aeSlot.yPos - 1,
                                optionalSlot.getSourceX() - 1, optionalSlot.getSourceY() - 1, 18, 18, getBlitOffset());
                        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                    }
                }
            }
        }

        for (final CustomSlotWidget slot : this.guiSlots) {
            slot.drawBackground(ox, oy, getBlitOffset());
        }

    }

    @Override
    public boolean mouseClicked(final double xCoord, final double yCoord, final int btn) {
        this.drag_click.clear();

        if (btn == 1) {
            for (final Object o : this.buttons) {
                final Widget widget = (Widget) o;
                if (widget.isMouseOver(xCoord, yCoord)) {
                    return super.mouseClicked(xCoord, yCoord, 0);
                }
            }
        }

        for (CustomSlotWidget slot : this.guiSlots) {
            if (this.isPointInRegion(slot.getTooltipAreaX(), slot.getTooltipAreaY(), slot.getTooltipAreaWidth(),
                    slot.getTooltipAreaHeight(), xCoord, yCoord) && slot.canClick(getPlayer())) {
                slot.slotClicked(getPlayer().inventory.getItemStack(), btn);
            }
        }

        // Forward left mouse button down events to the scrollbar
        if (btn == 0 && this.getScrollBar() != null) {
            if (this.getScrollBar().mouseDown(xCoord - this.guiLeft, yCoord - this.guiTop)) {
                return true;
            }
        }

        return super.mouseClicked(xCoord, yCoord, btn);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // Forward left mouse button up events to the scrollbar
        if (button == 0 && this.getScrollBar() != null) {
            if (this.getScrollBar().mouseUp(mouseX - this.guiLeft, mouseY - this.guiTop)) {
                return true;
            }
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dragX, double dragY) {
        final Slot slot = this.getSlot((int) mouseX, (int) mouseY);
        final ItemStack itemstack = getPlayer().inventory.getItemStack();

        if (this.getScrollBar() != null) {
            // FIXME: Coordinate system of mouseX/mouseY is unclear
            this.getScrollBar().mouseDragged((int) mouseX - this.guiLeft, (int) mouseY - this.guiTop);
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

    // TODO 1.9.4 aftermath - Whole ClickType thing, to be checked.
    @Override
    protected void handleMouseClick(final Slot slot, final int slotIdx, final int mouseButton,
            final ClickType clickType) {
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

        if (InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), GLFW.GLFW_KEY_SPACE)) {
            if (this.enableSpaceClicking()) {
                IAEItemStack stack = null;
                if (slot instanceof SlotME) {
                    stack = ((SlotME) slot).getAEStack();
                }

                int slotNum = this.getInventorySlots().size();

                if (!(slot instanceof SlotME) && slot != null) {
                    slotNum = slot.slotNumber;
                }

                ((AEBaseContainer) this.container).setTargetStack(stack);
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

                    if (player.abilities.isCreativeMode) {
                        action = InventoryAction.CREATIVE_DUPLICATE;
                    }

                    break;

                default:
                case THROW: // drop item:
            }

            if (action != null) {
                final InventoryActionPacket p = new InventoryActionPacket(action, slot.getSlotIndex(),
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
                            && player.inventory.getItemStack().isEmpty()) {
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
                    } else if (player.abilities.isCreativeMode) {
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
                this.container.setTargetStack(stack);
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
                    this.dbl_whichItem = slot.getHasStack() ? slot.getStack().copy() : ItemStack.EMPTY;
                } else {
                    this.dbl_whichItem = ItemStack.EMPTY;
                }
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

    protected boolean func_195363_d(int keyCode, int scanCode) {
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
                        if (s.getSlotIndex() == j && s.inventory == ((AEBaseContainer) this.container).getPlayerInv()) {
                            if (!s.canTakeStack(((AEBaseContainer) this.container).getPlayerInv().player)) {
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
                                    && s.inventory == ((AEBaseContainer) this.container).getPlayerInv()) {
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
            // isPointInRegion
            if (this.isPointInRegion(slot.xPos, slot.yPos, 16, 16, mouseX, mouseY)) {
                return slot;
            }
        }

        return null;
    }

    public abstract void drawBG(MatrixStack matrixStack, int offsetX, int offsetY, int mouseX, int mouseY,
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
                this.container.setTargetStack(item);
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
        final ResourceLocation loc = new ResourceLocation(base, "textures/" + file);
        getMinecraft().getTextureManager().bindTexture(loc);
    }

    protected void drawItem(final int x, final int y, final ItemStack is) {
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
     * This overrides the base-class method through some access transformer
     * hackery...
     */
    @Override
    public void moveItems(MatrixStack matrices, Slot s) {
        if (s instanceof SlotME) {

            try {
                if (!this.isPowered()) {
                    fill(matrices, s.xPos, s.yPos, 16 + s.xPos, 16 + s.yPos, 0x66111111);
                }

                // Annoying but easier than trying to splice into render item
                super.moveItems(matrices, new Size1Slot((SlotME) s));

                this.stackSizeRenderer.renderStackSize(this.font, ((SlotME) s).getAEStack(), s.xPos, s.yPos);

            } catch (final Exception err) {
                AELog.warn("[AppEng] AE prevented crash while drawing slot: " + err.toString());
            }

            return;
        } else if (s instanceof IMEFluidSlot && ((IMEFluidSlot) s).shouldRenderAsFluid()) {
            final IMEFluidSlot slot = (IMEFluidSlot) s;
            final IAEFluidStack fs = slot.getAEFluidStack();

            if (fs != null && this.isPowered()) {
                RenderSystem.disableBlend();
                final Fluid fluid = fs.getFluid();
                FluidAttributes fluidAttributes = fluid.getAttributes();
                bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
                ResourceLocation fluidStillTexture = fluidAttributes.getStillTexture(fs.getFluidStack());
                final TextureAtlasSprite sprite = getMinecraft()
                        .getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(fluidStillTexture);

                // Set color for dynamic fluids
                // Convert int color to RGB
                float red = (fluidAttributes.getColor() >> 16 & 255) / 255.0F;
                float green = (fluidAttributes.getColor() >> 8 & 255) / 255.0F;
                float blue = (fluidAttributes.getColor() & 255) / 255.0F;
                RenderSystem.color3f(red, green, blue);

                blit(matrices, s.xPos, s.yPos, 0 /* FIXME: Validate this was previous the controls zindex */, 16, 16,
                        sprite);
                RenderSystem.enableBlend();

                this.fluidStackSizeRenderer.renderStackSize(this.font, fs, s.xPos, s.yPos);
            } else if (!this.isPowered()) {
                fill(matrices, s.xPos, s.yPos, 16 + s.xPos, 16 + s.yPos, 0x66111111);
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
                            final float par1 = aes.xPos;
                            final float par2 = aes.yPos;
                            final float par3 = uv_x * 16;
                            final float par4 = uv_y * 16;

                            final Tessellator tessellator = Tessellator.getInstance();
                            final BufferBuilder vb = tessellator.getBuffer();

                            vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

                            final float f1 = 0.00390625F;
                            final float f = 0.00390625F;
                            final float par6 = 16;
                            vb.pos(par1 + 0, par2 + par6, getBlitOffset()).tex((par3 + 0) * f, (par4 + par6) * f1)
                                    .color(1.0f, 1.0f, 1.0f, aes.getOpacityOfIcon()).endVertex();
                            final float par5 = 16;
                            vb.pos(par1 + par5, par2 + par6, getBlitOffset()).tex((par3 + par5) * f, (par4 + par6) * f1)
                                    .color(1.0f, 1.0f, 1.0f, aes.getOpacityOfIcon()).endVertex();
                            vb.pos(par1 + par5, par2 + 0, getBlitOffset()).tex((par3 + par5) * f, (par4 + 0) * f1)
                                    .color(1.0f, 1.0f, 1.0f, aes.getOpacityOfIcon()).endVertex();
                            vb.pos(par1 + 0, par2 + 0, getBlitOffset()).tex((par3 + 0) * f, (par4 + 0) * f1)
                                    .color(1.0f, 1.0f, 1.0f, aes.getOpacityOfIcon()).endVertex();
                            tessellator.draw();

                        } catch (final Exception err) {
                            err.printStackTrace();
                        }
                    }
                }

                if (!is.isEmpty() && s instanceof AppEngSlot) {
                    AppEngSlot aeSlot = (AppEngSlot) s;
                    if (aeSlot.getIsValid() == CalculatedValidity.NotAvailable) {
                        boolean isValid = s.isItemValid(is) || s instanceof OutputSlot
                                || s instanceof AppEngCraftingSlot || s instanceof DisabledSlot
                                || s instanceof InaccessibleSlot || s instanceof FakeSlot
                                || s instanceof RestrictedInputSlot || s instanceof SlotDisconnected;
                        if (isValid && s instanceof RestrictedInputSlot) {
                            try {
                                isValid = ((RestrictedInputSlot) s).isValid(is, getMinecraft().world);
                            } catch (final Exception err) {
                                AELog.debug(err);
                            }
                        }
                        aeSlot.setIsValid(isValid ? CalculatedValidity.Valid : CalculatedValidity.Invalid);
                    }

                    if (aeSlot.getIsValid() == CalculatedValidity.Invalid) {
                        setBlitOffset(100);
                        this.itemRenderer.zLevel = 100.0F;

                        fill(matrices, s.xPos, s.yPos, 16 + s.xPos, 16 + s.yPos, 0x66ff6666);

                        setBlitOffset(0);
                        this.itemRenderer.zLevel = 0.0F;
                    }
                }

                if (s instanceof AppEngSlot) {
                    ((AppEngSlot) s).setDisplay(true);
                    super.moveItems(matrices, s);
                } else {
                    super.moveItems(matrices, s);
                }

                return;
            } catch (final Exception err) {
                AELog.warn("[AppEng] AE prevented crash while drawing slot: " + err.toString());
            }
        }
        // do the usual for non-ME Slots.
        super.moveItems(matrices, s);
    }

    protected boolean isPowered() {
        return true;
    }

    public void bindTexture(final String file) {
        final ResourceLocation loc = new ResourceLocation(AppEng.MOD_ID, "textures/" + file);
        getMinecraft().getTextureManager().bindTexture(loc);
    }

    public void bindTexture(final ResourceLocation loc) {
        getMinecraft().getTextureManager().bindTexture(loc);
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

        if (this.getScrollBar() != null) {
            this.getScrollBar().tick();
        }

        for (IGuiEventListener child : children) {
            if (child instanceof ITickingWidget) {
                ((ITickingWidget) child).tick();
            }
        }
    }

}
