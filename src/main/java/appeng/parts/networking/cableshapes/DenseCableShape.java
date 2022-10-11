package appeng.parts.networking.cableshapes;

import java.util.Set;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import appeng.api.networking.GridHelper;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.util.AECableType;

public class DenseCableShape implements ICableShape {

    public static final ICableShape COVERED = new DenseCableShape(AECableType.DENSE_COVERED);

    public static final ICableShape SMART = new DenseCableShape(AECableType.DENSE_SMART);

    private final AECableType cableType;

    private DenseCableShape(AECableType cableType) {
        this.cableType = cableType;
    }

    @Override
    public AECableType getCableConnectionType() {
        return cableType;
    }

    @Override
    public void addCableBoxes(
            Set<Direction> connections,
            @Nullable IPartHost host,
            @Nullable Level level,
            BlockPos pos,
            IPartCollisionHelper bch) {

        var min = getMin(bch);
        var max = getMax(bch);
        bch.addBox(min, min, min, max, max, max);

        for (var side : connections) {
            addConnectionBoxes(side, host, level, pos, bch);
        }
    }

    private static double getMin(IPartCollisionHelper bch) {
        return bch.isBBCollision() ? 4.9 : 3.0;
    }

    private static double getMax(IPartCollisionHelper bch) {
        return bch.isBBCollision() ? 11.1 : 13.0;
    }

    @Override
    public void addConnectionBoxes(Direction side, @Nullable IPartHost host, @Nullable Level level, BlockPos pos,
            IPartCollisionHelper bch) {
        if (isDense(level, pos, side)) {
            var min = getMin(bch);
            var max = getMax(bch);

            switch (side) {
                case DOWN -> bch.addBox(min, 0.0, min, max, min, max);
                case EAST -> bch.addBox(max, min, min, 16.0, max, max);
                case NORTH -> bch.addBox(min, min, 0.0, max, max, min);
                case SOUTH -> bch.addBox(min, min, max, max, max, 16.0);
                case UP -> bch.addBox(min, max, min, max, 16.0, max);
                case WEST -> bch.addBox(0.0, min, min, min, max, max);
                default -> {
                }
            }
        } else {
            CoveredCableShape.COVERED.addConnectionBoxes(side, host, level, pos, bch);
        }
    }

    private boolean isDense(Level level, BlockPos pos, Direction of) {
        var adjacentHost = GridHelper.getNodeHost(level, pos.relative(of));

        if (adjacentHost != null) {
            var t = adjacentHost.getCableConnectionType(of.getOpposite());
            return t.isDense();
        }

        return false;
    }
}
