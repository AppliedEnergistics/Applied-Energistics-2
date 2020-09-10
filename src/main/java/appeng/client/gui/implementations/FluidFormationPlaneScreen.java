
package appeng.client.gui.implementations;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.widgets.FluidSlotWidget;
import appeng.client.gui.widgets.OptionalFluidSlotWidget;
import appeng.client.gui.widgets.TabButton;
import appeng.container.implementations.FluidFormationPlaneContainer;
import appeng.container.implementations.PriorityContainer;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.SwitchGuisPacket;
import appeng.util.fluid.IAEFluidTank;

public class FluidFormationPlaneScreen extends UpgradeableScreen<FluidFormationPlaneContainer> {
    public FluidFormationPlaneScreen(FluidFormationPlaneContainer container, PlayerInventory playerInventory,
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
                    this.guiSlots.add(new FluidSlotWidget(config, idx, idx, xo + x * 18, yo + y * 18));
                } else {
                    this.guiSlots.add(new OptionalFluidSlotWidget(config, container, idx, idx, y - 2, xo, yo, x, y));
                }
            }
        }
    }

    @Override
    protected void addButtons() {
        this.addButton(new TabButton(this.guiLeft + 154, this.guiTop, 2 + 4 * 16, GuiText.Priority.text(),
                this.itemRenderer, btn -> openPriorityGui()));
    }

    private void openPriorityGui() {
        NetworkHandler.instance().sendToServer(new SwitchGuisPacket(PriorityContainer.TYPE));
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
