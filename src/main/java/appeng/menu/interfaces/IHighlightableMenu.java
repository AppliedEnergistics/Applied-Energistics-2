package appeng.menu.interfaces;

import appeng.api.stacks.AEKey;

/**
 * Implement this interface on any menu that can highlight something in-world.
 */
public interface IHighlightableMenu {
    void highlight(AEKey what);
}
