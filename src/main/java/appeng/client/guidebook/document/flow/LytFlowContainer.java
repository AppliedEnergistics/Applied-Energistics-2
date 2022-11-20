package appeng.client.guidebook.document.flow;

import appeng.client.guidebook.document.LytRect;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public interface LytFlowContainer extends LytFlowParent {
    /**
     * Gets a stream of all the bounding rectangles for given flow content.
     * Since flow content may be wrapped, it may consist of several disjointed bounding boxes.
     */
    Stream<LytRect> enumerateContentBounds(LytFlowContent content);

    @Nullable
    LytFlowContent pickContent(int x, int y);
}
