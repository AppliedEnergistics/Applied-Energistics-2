package appeng.parts.networking.cableshapes;

import java.util.Set;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.util.AECableType;

public class GlassCableShape implements ICableShape {

    public static final GlassCableShape INSTANCE = new GlassCableShape();

    private GlassCableShape() {
    }

    @Override
    public AECableType getCableConnectionType() {
        return AECableType.GLASS;
    }

    @Override
    public void addCableBoxes(Set<Direction> connections, @Nullable IPartHost host, @Nullable Level level, BlockPos pos,
            IPartCollisionHelper bch) {
        bch.addBox(6.0, 6.0, 6.0, 10.0, 10.0, 10.0);

        if (host != null) {
            for (var dir : Direction.values()) {
                var p = host.getPart(dir);
                if (p != null) {
                    var dist = p.getCableConnectionLength(this.getCableConnectionType());

                    if (dist > 8) {
                        continue;
                    }

                    switch (dir) {
                        case DOWN -> bch.addBox(6.0, dist, 6.0, 10.0, 6.0, 10.0);
                        case EAST -> bch.addBox(10.0, 6.0, 6.0, 16.0 - dist, 10.0, 10.0);
                        case NORTH -> bch.addBox(6.0, 6.0, dist, 10.0, 10.0, 6.0);
                        case SOUTH -> bch.addBox(6.0, 6.0, 10.0, 10.0, 10.0, 16.0 - dist);
                        case UP -> bch.addBox(6.0, 10.0, 6.0, 10.0, 16.0 - dist, 10.0);
                        case WEST -> bch.addBox(dist, 6.0, 6.0, 6.0, 10.0, 10.0);
                        default -> {
                        }
                    }
                }
            }
        }

        for (var of : connections) {
            switch (of) {
                case DOWN -> bch.addBox(6.0, 0.0, 6.0, 10.0, 6.0, 10.0);
                case EAST -> bch.addBox(10.0, 6.0, 6.0, 16.0, 10.0, 10.0);
                case NORTH -> bch.addBox(6.0, 6.0, 0.0, 10.0, 10.0, 6.0);
                case SOUTH -> bch.addBox(6.0, 6.0, 10.0, 10.0, 10.0, 16.0);
                case UP -> bch.addBox(6.0, 10.0, 6.0, 10.0, 16.0, 10.0);
                case WEST -> bch.addBox(0.0, 6.0, 6.0, 6.0, 10.0, 10.0);
                default -> {
                }
            }
        }
    }
}
