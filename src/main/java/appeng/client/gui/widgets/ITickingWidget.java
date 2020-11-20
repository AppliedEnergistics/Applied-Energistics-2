package appeng.client.gui.widgets;

import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;

/**
 * Indicates that a widget (anything that is in the {@link Screen#children()} list) should receive callbacks every tick.
 */
public interface ITickingWidget extends Element {

    void tick();

}
