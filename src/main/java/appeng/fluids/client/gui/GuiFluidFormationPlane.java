package appeng.fluids.client.gui;


import appeng.client.gui.implementations.GuiUpgradeable;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.core.localization.GuiText;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketSwitchGuis;
import appeng.fluids.client.gui.widgets.GuiFluidSlot;
import appeng.fluids.client.gui.widgets.GuiOptionalFluidSlot;
import appeng.fluids.container.ContainerFluidFormationPlane;
import appeng.fluids.parts.PartFluidFormationPlane;
import appeng.fluids.util.IAEFluidTank;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;

import java.io.IOException;


public class GuiFluidFormationPlane extends GuiUpgradeable {
    private final PartFluidFormationPlane plane;
    private GuiTabButton priority;

    public GuiFluidFormationPlane(InventoryPlayer inventoryPlayer, PartFluidFormationPlane te) {
        super(new ContainerFluidFormationPlane(inventoryPlayer, te));
        this.ySize = 251;
        this.plane = te;
    }

    @Override
    public void initGui() {
        super.initGui();

        final int xo = 8;
        final int yo = 23 + 6;

        final IAEFluidTank config = this.plane.getConfig();
        final ContainerFluidFormationPlane container = (ContainerFluidFormationPlane) this.inventorySlots;

        for (int y = 0; y < 7; y++) {
            for (int x = 0; x < 9; x++) {
                final int idx = y * 9 + x;
                if (y < 2) {
                    this.guiSlots.add(new GuiFluidSlot(config, idx, idx, xo + x * 18, yo + y * 18));
                } else {
                    this.guiSlots.add(new GuiOptionalFluidSlot(config, container, idx, idx, y - 2, xo, yo, x, y));
                }
            }
        }
    }

    @Override
    protected void addButtons() {
        this.buttonList.add(this.priority = new GuiTabButton(this.guiLeft + 154, this.guiTop, 2 + 4 * 16, GuiText.Priority.getLocal(), this.itemRender));
    }

    @Override
    protected String getBackground() {
        return "guis/storagebus.png";
    }

    @Override
    protected void actionPerformed(final GuiButton btn) throws IOException {
        super.actionPerformed(btn);
        if (btn == this.priority) {
            NetworkHandler.instance().sendToServer(new PacketSwitchGuis(GuiBridge.GUI_PRIORITY));
        }
    }

    @Override
    protected GuiText getName() {
        return GuiText.FluidFormationPlane;
    }
}
