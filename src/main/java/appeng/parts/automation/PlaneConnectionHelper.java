package appeng.parts.automation;

import javax.annotation.Nullable;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.util.AEPartLocation;
import appeng.parts.AEBasePart;

/**
 * Helps plane parts (annihilation, formation) with determining and checking for connections to adjacent plane parts of
 * the same type to form a visually larger plane.
 */
public final class PlaneConnectionHelper {

    private final AEBasePart part;

    public PlaneConnectionHelper(AEBasePart part) {
        this.part = part;
    }

    /**
     * Gets on which sides this part has adjacent planes that it visually connects to
     */
    public PlaneConnections getConnections() {
        TileEntity hostTileEntity = getHostTileEntity();
        AEPartLocation side = part.getSide();

        final Direction facingRight, facingUp;
        switch (side) {
            case UP:
                facingRight = Direction.EAST;
                facingUp = Direction.NORTH;
                break;
            case DOWN:
                facingRight = Direction.WEST;
                facingUp = Direction.NORTH;
                break;
            case NORTH:
                facingRight = Direction.WEST;
                facingUp = Direction.UP;
                break;
            case SOUTH:
                facingRight = Direction.EAST;
                facingUp = Direction.UP;
                break;
            case WEST:
                facingRight = Direction.SOUTH;
                facingUp = Direction.UP;
                break;
            case EAST:
                facingRight = Direction.NORTH;
                facingUp = Direction.UP;
                break;
            default:
            case INTERNAL:
                return PlaneConnections.of(false, false, false, false);
        }

        boolean left = false, right = false, down = false, up = false;

        if (hostTileEntity != null) {
            World world = hostTileEntity.getWorld();
            BlockPos pos = hostTileEntity.getPos();

            if (isCompatiblePlaneAdjacent(world.getTileEntity(pos.offset(facingRight.getOpposite())))) {
                left = true;
            }

            if (isCompatiblePlaneAdjacent(world.getTileEntity(pos.offset(facingRight)))) {
                right = true;
            }

            if (isCompatiblePlaneAdjacent(world.getTileEntity(pos.offset(facingUp.getOpposite())))) {
                down = true;
            }

            if (isCompatiblePlaneAdjacent(world.getTileEntity(pos.offset(facingUp)))) {
                up = true;
            }
        }

        return PlaneConnections.of(up, right, down, left);
    }

    /**
     * Get the bounding boxes of this plane parts components.
     */
    public void getBoxes(IPartCollisionHelper bch) {
        int minX = 1;
        int minY = 1;
        int maxX = 15;
        int maxY = 15;

        TileEntity hostTile = getHostTileEntity();
        if (hostTile != null) {
            World world = hostTile.getWorld();

            final BlockPos pos = hostTile.getPos();

            final Direction e = bch.getWorldX();
            final Direction u = bch.getWorldY();

            if (isCompatiblePlaneAdjacent(world.getTileEntity(pos.offset(e.getOpposite())))) {
                minX = 0;
            }

            if (isCompatiblePlaneAdjacent(world.getTileEntity(pos.offset(e)))) {
                maxX = 16;
            }

            if (isCompatiblePlaneAdjacent(world.getTileEntity(pos.offset(u.getOpposite())))) {
                minY = 0;
            }

            if (isCompatiblePlaneAdjacent(world.getTileEntity(pos.offset(u)))) {
                maxY = 16;
            }
        }

        bch.addBox(5, 5, 14, 11, 11, 15);
        bch.addBox(minX, minY, 15, maxX, maxY, 16);
    }

    /**
     * Call this when an adjacent block has changed since the connections need to be recalculated.
     */
    public void updateConnections() {
        // Not needed in Fabric, since model data is automatically updated
    }

    private boolean isCompatiblePlaneAdjacent(@Nullable TileEntity adjacentTileEntity) {
        if (adjacentTileEntity instanceof IPartHost) {
            final IPart p = ((IPartHost) adjacentTileEntity).getPart(part.getSide());
            return p != null && p.getClass() == part.getClass();
        }
        return false;
    }

    private TileEntity getHostTileEntity() {
        IPartHost host = part.getHost();
        if (host != null) {
            return host.getTile();
        }
        return null;
    }

}
