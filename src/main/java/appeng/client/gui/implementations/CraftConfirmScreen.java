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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import appeng.client.gui.Blitter;
import com.google.common.base.Joiner;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.widgets.Scrollbar;
import appeng.container.implementations.CraftConfirmContainer;
import appeng.core.Api;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;
import appeng.util.Platform;

public class CraftConfirmScreen extends AEBaseScreen<CraftConfirmContainer> {

    private static final Blitter BACKGROUND = Blitter.texture("guis/craftingreport.png").src(0, 0, 238, 206);

    private final AESubScreen subGui;

    private final int rows = 5;

    private final IItemList<IAEItemStack> storage = Api.instance().storage()
            .getStorageChannel(IItemStorageChannel.class).createList();
    private final IItemList<IAEItemStack> pending = Api.instance().storage()
            .getStorageChannel(IItemStorageChannel.class).createList();
    private final IItemList<IAEItemStack> missing = Api.instance().storage()
            .getStorageChannel(IItemStorageChannel.class).createList();

    private final List<IAEItemStack> visual = new ArrayList<>();

    private Button start;
    private Button selectCPU;
    private int tooltip = -1;

    public CraftConfirmScreen(CraftConfirmContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title, BACKGROUND);
        this.subGui = new AESubScreen(this, container.getTarget());

        this.setScrollBar(new Scrollbar());
    }

    @Override
    public void init() {
        super.init();

        this.start = new Button(this.guiLeft + 162, this.guiTop + this.ySize - 25, 50, 20, GuiText.Start.text(),
                btn -> start());
        this.start.active = false;
        this.addButton(this.start);

        this.selectCPU = new Button(this.guiLeft + (219 - 180) / 2, this.guiTop + this.ySize - 68, 180, 20,
                getNextCpuButtonLabel(), btn -> selectNextCpu());
        this.selectCPU.active = false;
        this.addButton(this.selectCPU);

        addButton(new Button(this.guiLeft + 6, this.guiTop + this.ySize - 25, 50, 20, GuiText.Cancel.text(),
                btn -> subGui.goBack()));

        this.setScrollBar();
    }

    @Override
    public void render(MatrixStack matrixStack, final int mouseX, final int mouseY, final float btn) {
        this.updateCPUButtonText();

        this.start.active = !(this.container.hasNoCPU() || this.isSimulation());
        this.selectCPU.active = !this.isSimulation();

        this.tooltip = -1;

        final int offY = 23;
        int y = 0;
        int x = 0;
        for (int z = 0; z <= 4 * 5; z++) {
            final int minX = guiLeft + 9 + x * 67;
            final int minY = guiTop + 22 + y * offY;

            if (minX < mouseX && minX + 67 > mouseX) {
                if (minY < mouseY && minY + offY - 2 > mouseY) {
                    this.tooltip = z;
                    break;
                }
            }

            x++;

            if (x > 2) {
                y++;
                x = 0;
            }
        }

        super.render(matrixStack, mouseX, mouseY, btn);
    }

    private void updateCPUButtonText() {
        this.selectCPU.setMessage(getNextCpuButtonLabel());
    }

    private ITextComponent getNextCpuButtonLabel() {
        if (this.container.hasNoCPU()) {
            return GuiText.NoCraftingCPUs.text();
        }

        ITextComponent cpuName;
        if (this.container.cpuName == null) {
            cpuName = GuiText.Automatic.text();
        } else {
            cpuName = this.container.cpuName;
        }

        return GuiText.CraftingCPU.withSuffix(": ").append(cpuName);
    }

    private boolean isSimulation() {
        return this.container.isSimulation();
    }

    @Override
    public void drawFG(MatrixStack matrixStack, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY) {
        final long BytesUsed = this.container.getUsedBytes();
        final String byteUsed = NumberFormat.getInstance().format(BytesUsed);
        final String Add = BytesUsed > 0 ? (byteUsed + ' ' + GuiText.BytesUsed.getLocal())
                : GuiText.CalculatingWait.getLocal();
        this.font.drawString(matrixStack, GuiText.CraftingPlan.getLocal() + " - " + Add, 8, 7, COLOR_DARK_GRAY);

        String dsp = null;

        if (this.isSimulation()) {
            dsp = GuiText.Simulation.getLocal();
        } else {
            dsp = this.container.getCpuAvailableBytes() > 0
                    ? (GuiText.Bytes.getLocal() + ": " + this.container.getCpuAvailableBytes() + " : "
                            + GuiText.CoProcessors.getLocal() + ": " + this.container.getCpuCoProcessors())
                    : GuiText.Bytes.getLocal() + ": N/A : " + GuiText.CoProcessors.getLocal() + ": N/A";
        }

        final int offset = (219 - this.font.getStringWidth(dsp)) / 2;
        this.font.drawString(matrixStack, dsp, offset, 165, COLOR_DARK_GRAY);

        final int sectionLength = 67;

        int x = 0;
        int y = 0;
        final int xo = 9;
        final int yo = 22;
        final int viewStart = this.getScrollBar().getCurrentScroll() * 3;
        final int viewEnd = viewStart + 3 * this.rows;

        String dspToolTip = "";
        final List<String> lineList = new ArrayList<>();
        int toolPosX = 0;
        int toolPosY = 0;

        final int offY = 23;

        for (int z = viewStart; z < Math.min(viewEnd, this.visual.size()); z++) {
            final IAEItemStack refStack = this.visual.get(z);// repo.getReferenceItem( z );
            if (refStack != null) {
                RenderSystem.pushMatrix();
                RenderSystem.scalef(0.5f, 0.5f, 0.5f);

                final IAEItemStack stored = this.storage.findPrecise(refStack);
                final IAEItemStack pendingStack = this.pending.findPrecise(refStack);
                final IAEItemStack missingStack = this.missing.findPrecise(refStack);

                int lines = 0;

                if (stored != null && stored.getStackSize() > 0) {
                    lines++;
                }
                if (missingStack != null && missingStack.getStackSize() > 0) {
                    lines++;
                }
                if (pendingStack != null && pendingStack.getStackSize() > 0) {
                    lines++;
                }

                final int negY = ((lines - 1) * 5) / 2;
                int downY = 0;

                if (stored != null && stored.getStackSize() > 0) {
                    String str = Long.toString(stored.getStackSize());
                    if (stored.getStackSize() >= 10000) {
                        str = Long.toString(stored.getStackSize() / 1000) + 'k';
                    }
                    if (stored.getStackSize() >= 10000000) {
                        str = Long.toString(stored.getStackSize() / 1000000) + 'm';
                    }

                    str = GuiText.FromStorage.getLocal() + ": " + str;
                    final int w = 4 + this.font.getStringWidth(str);
                    this.font.drawString(matrixStack, str,
                            (int) ((x * (1 + sectionLength) + xo + sectionLength - 19 - (w * 0.5)) * 2),
                            (y * offY + yo + 6 - negY + downY) * 2, COLOR_DARK_GRAY);

                    if (this.tooltip == z - viewStart) {
                        lineList.add(GuiText.FromStorage.getLocal() + ": " + Long.toString(stored.getStackSize()));
                    }

                    downY += 5;
                }

                boolean red = false;
                if (missingStack != null && missingStack.getStackSize() > 0) {
                    String str = Long.toString(missingStack.getStackSize());
                    if (missingStack.getStackSize() >= 10000) {
                        str = Long.toString(missingStack.getStackSize() / 1000) + 'k';
                    }
                    if (missingStack.getStackSize() >= 10000000) {
                        str = Long.toString(missingStack.getStackSize() / 1000000) + 'm';
                    }

                    str = GuiText.Missing.text().getString() + ": " + str;
                    final int w = 4 + this.font.getStringWidth(str);
                    this.font.drawString(matrixStack, str,
                            (int) ((x * (1 + sectionLength) + xo + sectionLength - 19 - (w * 0.5)) * 2),
                            (y * offY + yo + 6 - negY + downY) * 2, COLOR_DARK_GRAY);

                    if (this.tooltip == z - viewStart) {
                        lineList.add(GuiText.Missing.getLocal() + ": " + Long.toString(missingStack.getStackSize()));
                    }

                    red = true;
                    downY += 5;
                }

                if (pendingStack != null && pendingStack.getStackSize() > 0) {
                    String str = Long.toString(pendingStack.getStackSize());
                    if (pendingStack.getStackSize() >= 10000) {
                        str = Long.toString(pendingStack.getStackSize() / 1000) + 'k';
                    }
                    if (pendingStack.getStackSize() >= 10000000) {
                        str = Long.toString(pendingStack.getStackSize() / 1000000) + 'm';
                    }

                    str = GuiText.ToCraft.getLocal() + ": " + str;
                    final int w = 4 + this.font.getStringWidth(str);
                    this.font.drawString(matrixStack, str,
                            (int) ((x * (1 + sectionLength) + xo + sectionLength - 19 - (w * 0.5)) * 2),
                            (y * offY + yo + 6 - negY + downY) * 2, COLOR_DARK_GRAY);

                    if (this.tooltip == z - viewStart) {
                        lineList.add(GuiText.ToCraft.getLocal() + ": " + Long.toString(pendingStack.getStackSize()));
                    }
                }

                RenderSystem.popMatrix();
                final int posX = x * (1 + sectionLength) + xo + sectionLength - 19;
                final int posY = y * offY + yo;

                final ItemStack is = refStack.asItemStackRepresentation();

                if (this.tooltip == z - viewStart) {
                    dspToolTip = Platform.getItemDisplayName(refStack).getString();

                    if (lineList.size() > 0) {
                        dspToolTip = dspToolTip + '\n' + Joiner.on("\n").join(lineList);
                    }

                    toolPosX = x * (1 + sectionLength) + xo + sectionLength - 8;
                    toolPosY = y * offY + yo;
                }

                this.drawItem(posX, posY, is);

                if (red) {
                    final int startX = x * (1 + sectionLength) + xo;
                    final int startY = posY - 4;
                    fill(matrixStack, startX, startY, startX + sectionLength, startY + offY, 0x1AFF0000);
                }

                x++;

                if (x > 2) {
                    y++;
                    x = 0;
                }
            }
        }

        if (this.tooltip >= 0 && !dspToolTip.isEmpty()) {
            this.drawTooltip(matrixStack, toolPosX, toolPosY + 10, new StringTextComponent(dspToolTip));
        }
    }

    @Override
    public void drawBG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY, float partialTicks) {
        this.setScrollBar();
        super.drawBG(matrices, offsetX, offsetY, mouseX, mouseY, partialTicks);
    }

    private void setScrollBar() {
        final int size = this.visual.size();

        this.getScrollBar().setTop(19).setLeft(218).setHeight(114);
        this.getScrollBar().setRange(0, (size + 2) / 3 - this.rows, 1);
    }

    public void postUpdate(final List<IAEItemStack> list, final byte ref) {
        switch (ref) {
            case 0:
                for (final IAEItemStack l : list) {
                    this.handleInput(this.storage, l);
                }
                break;

            case 1:
                for (final IAEItemStack l : list) {
                    this.handleInput(this.pending, l);
                }
                break;

            case 2:
                for (final IAEItemStack l : list) {
                    this.handleInput(this.missing, l);
                }
                break;
        }

        for (final IAEItemStack l : list) {
            final long amt = this.getTotal(l);

            if (amt <= 0) {
                this.deleteVisualStack(l);
            } else {
                final IAEItemStack is = this.findVisualStack(l);
                is.setStackSize(amt);
            }
        }

        this.setScrollBar();
    }

    private void handleInput(final IItemList<IAEItemStack> s, final IAEItemStack l) {
        IAEItemStack a = s.findPrecise(l);

        if (l.getStackSize() <= 0) {
            if (a != null) {
                a.reset();
            }
        } else {
            if (a == null) {
                s.add(l.copy());
                a = s.findPrecise(l);
            }

            if (a != null) {
                a.setStackSize(l.getStackSize());
            }
        }
    }

    private long getTotal(final IAEItemStack is) {
        final IAEItemStack a = this.storage.findPrecise(is);
        final IAEItemStack c = this.pending.findPrecise(is);
        final IAEItemStack m = this.missing.findPrecise(is);

        long total = 0;

        if (a != null) {
            total += a.getStackSize();
        }

        if (c != null) {
            total += c.getStackSize();
        }

        if (m != null) {
            total += m.getStackSize();
        }

        return total;
    }

    private void deleteVisualStack(final IAEItemStack l) {
        final Iterator<IAEItemStack> i = this.visual.iterator();
        while (i.hasNext()) {
            final IAEItemStack o = i.next();
            if (o.equals(l)) {
                i.remove();
                return;
            }
        }
    }

    private IAEItemStack findVisualStack(final IAEItemStack l) {
        for (final IAEItemStack o : this.visual) {
            if (o.equals(l)) {
                return o;
            }
        }

        final IAEItemStack stack = l.copy();
        this.visual.add(stack);
        return stack;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int p_keyPressed_3_) {
        if (!this.checkHotbarKeys(InputMappings.getInputByCode(keyCode, scanCode))) {
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                this.start();
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, p_keyPressed_3_);
    }

    private void selectNextCpu() {
        final boolean backwards = isHandlingRightClick();
        NetworkHandler.instance().sendToServer(new ConfigValuePacket("Terminal.Cpu", backwards ? "Prev" : "Next"));
    }

    private void start() {
        NetworkHandler.instance().sendToServer(new ConfigValuePacket("Terminal.Start", "Start"));
    }

}
