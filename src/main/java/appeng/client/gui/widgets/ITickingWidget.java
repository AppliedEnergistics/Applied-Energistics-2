package appeng.client.gui.widgets;

import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;

/**
 * Indicates that a widget (anything that is in the {@link Screen#getEventListeners()} list) should receive callbacks every tick.
 */
public interface ITickingWidget extends IGuiEventListener {

    void tick();

}
