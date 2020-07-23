
package appeng.parts.automation;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import appeng.api.config.Actionable;
import appeng.api.config.Settings;
import appeng.api.networking.security.IActionSource;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.cells.ICellContainer;
import appeng.api.storage.cells.ICellInventory;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.IConfigManager;
import appeng.helpers.IPriorityHost;

public abstract class AbstractFormationPlanePart<T extends IAEStack<T>> extends UpgradeablePart
        implements ICellContainer, IPriorityHost, IMEInventory<T> {

    private boolean wasActive = false;
    private int priority = 0;
    protected boolean blocked = false;

    public AbstractFormationPlanePart(ItemStack is) {
        super(is);
    }

    protected abstract void updateHandler();

    @Override
    protected int getUpgradeSlots() {
        return 5;
    }

    @Override
    public void upgradesChanged() {
        this.updateHandler();
    }

    @Override
    public void updateSetting(final IConfigManager manager, final Settings settingName, final Enum<?> newValue) {
        this.updateHandler();
        this.getHost().markForSave();
    }

    public void stateChanged() {
        final boolean currentActive = this.getProxy().isActive();
        if (this.wasActive != currentActive) {
            this.wasActive = currentActive;
            this.updateHandler();
            this.getHost().markForUpdate();
        }
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        int minX = 1;
        int minY = 1;
        int maxX = 15;
        int maxY = 15;

        final IPartHost host = this.getHost();
        if (host != null) {
            final BlockEntity te = host.getTile();

            final BlockPos pos = te.getPos();

            final Direction e = bch.getWorldX();
            final Direction u = bch.getWorldY();

            if (this.isTransitionPlane(te.getWorld().getBlockEntity(pos.offset(e.getOpposite())), this.getSide())) {
                minX = 0;
            }

            if (this.isTransitionPlane(te.getWorld().getBlockEntity(pos.offset(e)), this.getSide())) {
                maxX = 16;
            }

            if (this.isTransitionPlane(te.getWorld().getBlockEntity(pos.offset(u.getOpposite())), this.getSide())) {
                minY = 0;
            }

            if (this.isTransitionPlane(te.getWorld().getBlockEntity(pos.offset(u)), this.getSide())) {
                maxY = 16;
            }
        }

        bch.addBox(5, 5, 14, 11, 11, 15);
        bch.addBox(minX, minY, 15, maxX, maxY, 16);
    }

    public PlaneConnections getConnections() {

        final Direction facingRight, facingUp;
        AEPartLocation location = this.getSide();
        switch (location) {
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

        final IPartHost host = this.getHost();
        if (host != null) {
            final BlockEntity te = host.getTile();

            final BlockPos pos = te.getPos();

            if (this.isTransitionPlane(te.getWorld().getBlockEntity(pos.offset(facingRight.getOpposite())),
                    this.getSide())) {
                left = true;
            }

            if (this.isTransitionPlane(te.getWorld().getBlockEntity(pos.offset(facingRight)), this.getSide())) {
                right = true;
            }

            if (this.isTransitionPlane(te.getWorld().getBlockEntity(pos.offset(facingUp.getOpposite())),
                    this.getSide())) {
                down = true;
            }

            if (this.isTransitionPlane(te.getWorld().getBlockEntity(pos.offset(facingUp)), this.getSide())) {
                up = true;
            }
        }

        return PlaneConnections.of(up, right, down, left);
    }

    @Override
    public void onneighborUpdate(BlockView w, BlockPos pos, BlockPos neighbor) {
        if (pos.offset(this.getSide().getFacing()).equals(neighbor)) {
            final BlockEntity te = this.getHost().getTile();
            final AEPartLocation side = this.getSide();

            final BlockPos tePos = te.getPos().offset(side.getFacing());

            this.blocked = isBlocking(w, tePos);
        }
    }

    protected boolean isBlocking(BlockView w, BlockPos pos) {
        return !w.getBlockState(pos).getMaterial().isReplaceable();
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 1;
    }

    protected boolean isTransitionPlane(final BlockEntity blockTileEntity, final AEPartLocation side) {
        if (blockTileEntity instanceof IPartHost) {
            final IPart p = ((IPartHost) blockTileEntity).getPart(side);
            return p != null && this.getClass() == p.getClass();
        }
        return false;
    }

    @Override
    public T extractItems(final T request, final Actionable mode, final IActionSource src) {
        return null;
    }

    @Override
    public IItemList<T> getAvailableItems(final IItemList<T> out) {
        return out;
    }

    @Override
    public void readFromNBT(final CompoundTag data) {
        super.readFromNBT(data);
        this.priority = data.getInt("priority");
    }

    @Override
    public void writeToNBT(final CompoundTag data) {
        super.writeToNBT(data);
        data.putInt("priority", this.getPriority());
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public void setPriority(final int newValue) {
        this.priority = newValue;
        this.getHost().markForSave();
        this.updateHandler();
    }

    @Override
    public void blinkCell(final int slot) {
        // :P
    }

    @Override
    public void saveChanges(final ICellInventory<?> cell) {
        // nope!
    }
}
