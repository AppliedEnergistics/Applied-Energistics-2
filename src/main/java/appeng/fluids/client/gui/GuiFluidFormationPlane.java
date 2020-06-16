
package appeng.fluids.client.gui;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

import appeng.client.gui.implementations.GuiUpgradeable;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.container.implementations.ContainerPriority;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketSwitchGuis;
import appeng.fluids.client.gui.widgets.GuiFluidSlot;
import appeng.fluids.client.gui.widgets.GuiOptionalFluidSlot;
import appeng.fluids.container.ContainerFluidFormationPlane;
import appeng.fluids.util.IAEFluidTank;

public class GuiFluidFormationPlane extends GuiUpgradeable<ContainerFluidFormationPlane> {
    public GuiFluidFormationPlane(ContainerFluidFormationPlane container, PlayerInventory playerInventory,
            ITextComponent title) {
        super(container, playerInventory, title);
        this.ySize = 251;
    }

    @Override
    public void init() {
        super.init();

        final int xo = 8;
        final int yo = 23 + 6;

        final IAEFluidTank config = container.getFluidConfigInventory();

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
        this.addButton(new GuiTabButton(this.guiLeft + 154, this.guiTop, 2 + 4 * 16, GuiText.Priority.getLocal(),
                this.itemRenderer, btn -> openPriorityGui()));
    }

    private void openPriorityGui() {
        NetworkHandler.instance().sendToServer(new PacketSwitchGuis(ContainerPriority.TYPE));
    }

    @Override
    protected String getBackground() {
        return "guis/storagebus.png";
    }

    @Override
    protected GuiText getName() {
        return GuiText.FluidFormationPlane;
    }
}
