package appeng.tile.networking;

import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

// FIXME FABRIC DUMMY
public class ControllerBlockEntity implements IGridHost {
    @Nullable
    @Override
    public IGridNode getGridNode(@Nonnull AEPartLocation dir) {
        return null;
    }

    @Nonnull
    @Override
    public AECableType getCableConnectionType(@Nonnull AEPartLocation dir) {
        return null;
    }

    @Override
    public void securityBreak() {

    }

    public BlockPos getPos() {
        return null;
    }
}
