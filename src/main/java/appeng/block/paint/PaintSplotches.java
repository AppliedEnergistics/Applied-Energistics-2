package appeng.block.paint;


import appeng.helpers.Splotch;
import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.List;


/**
 * Used to transfer the state about paint splotches from the game thread to the render thread.
 */
class PaintSplotches {

    private final List<Splotch> splotches;

    PaintSplotches(Collection<Splotch> splotches) {
        this.splotches = ImmutableList.copyOf(splotches);
    }

    List<Splotch> getSplotches() {
        return this.splotches;
    }

}
