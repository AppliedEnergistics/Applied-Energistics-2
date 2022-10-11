package appeng.parts.networking.cableshapes;

import java.util.Set;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.util.AECableType;

public class CoveredCableShape implements ICableShape {

    public static final ICableShape COVERED = new CoveredCableShape(AECableType.COVERED);

    public static final ICableShape SMART = new CoveredCableShape(AECableType.SMART);
    private final AECableType cableType;

    private CoveredCableShape(AECableType cableType) {
        this.cableType = cableType;
    }

    @Override
    public AECableType getCableConnectionType() {
        return cableType;
    }

    @Override
    public void addCableBoxes(Set<Direction> connections, @Nullable IPartHost host, @Nullable Level level, BlockPos pos,
            IPartCollisionHelper bch) {
        bch.addBox(5.0, 5.0, 5.0, 11.0, 11.0, 11.0);

        for (var of : connections) {
            addConnectionBoxes(of, host, level, pos, bch);
        }
    }

    @Override
    public void addConnectionBoxes(Direction side, @Nullable IPartHost host, @Nullable Level level, BlockPos pos,
            IPartCollisionHelper bch) {
        switch (side) {
            case DOWN -> bch.addBox(5.0, 0.0, 5.0, 11.0, 5.0, 11.0);
            case EAST -> bch.addBox(11.0, 5.0, 5.0, 16.0, 11.0, 11.0);
            case NORTH -> bch.addBox(5.0, 5.0, 0.0, 11.0, 11.0, 5.0);
            case SOUTH -> bch.addBox(5.0, 5.0, 11.0, 11.0, 11.0, 16.0);
            case UP -> bch.addBox(5.0, 11.0, 5.0, 11.0, 16.0, 11.0);
            case WEST -> bch.addBox(0.0, 5.0, 5.0, 5.0, 11.0, 11.0);
            default -> {
            }
        }
    }
}
