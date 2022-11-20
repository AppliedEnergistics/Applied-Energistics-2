package appeng.client.guidebook.document.interaction;

import appeng.client.guidebook.document.LytBlock;

public interface InteractiveElement {
    LytBlock getParentBlock();

    InteractiveElement hitTest(int x, int y);
}
