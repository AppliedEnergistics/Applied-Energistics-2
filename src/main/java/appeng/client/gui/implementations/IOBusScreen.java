package appeng.client.gui.implementations;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

import appeng.client.gui.Blitter;
import appeng.container.implementations.IOBusContainer;

public class IOBusScreen extends UpgradeableScreen<IOBusContainer> {

    // Default background for import/export buses
    public static final Blitter BACKGROUND = Blitter.texture("guis/bus.png")
            .src(0, 0, 176, 184);

    public IOBusScreen(IOBusContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title, BACKGROUND);
        loadStyle("/screens/io_bus.json");
    }

}
