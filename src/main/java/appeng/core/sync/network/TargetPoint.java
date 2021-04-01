package appeng.core.sync.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;

/**
 * Created by covers1624 on 1/6/20.
 */
public class TargetPoint {

    public final ServerPlayerEntity excluded;
    public final double x;
    public final double y;
    public final double z;
    public final double radius;
    public final World world;

    public TargetPoint(double x, double y, double z, double radius, World world) {
        this(null, x, y, z, radius, world);
    }

    public TargetPoint(ServerPlayerEntity excluded, double x, double y, double z, double radius, World world) {
        this.excluded = excluded;
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
        this.world = world;
    }

}
