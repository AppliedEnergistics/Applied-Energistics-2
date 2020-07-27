package appeng.core.sync.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;

/**
 * Created by covers1624 on 1/6/20.
 */
public class TargetPoint {

    public final ServerPlayerEntity excluded;
    public final double x;
    public final double y;
    public final double z;
    public final double r2;
    public final World world;

    public TargetPoint(double x, double y, double z, double r2, World world) {
        this(null, x, y, z, r2, world);
    }

    public TargetPoint(ServerPlayerEntity excluded, double x, double y, double z, double r2, World world) {
        this.excluded = excluded;
        this.x = x;
        this.y = y;
        this.z = z;
        this.r2 = r2;
        this.world = world;
    }

    public static TargetPoint at(double x, double y, double z, double r2, World world) {
        return new TargetPoint(x, y, z, r2, world);
    }

    public static TargetPoint at(ServerPlayerEntity excluded, double x, double y, double z, double r2,
                                 World world) {
        return new TargetPoint(excluded, x, y, z, r2, world);
    }
}
