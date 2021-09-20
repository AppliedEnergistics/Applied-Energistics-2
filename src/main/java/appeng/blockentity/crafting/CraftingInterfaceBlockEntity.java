package appeng.blockentity.crafting;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import appeng.api.networking.IGridNodeListener;
import appeng.api.util.AECableType;
import appeng.blockentity.grid.AENetworkBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.helpers.iface.DualityCraftingInterface;
import appeng.helpers.iface.ICraftingInterfaceHost;
import appeng.util.Platform;

public class CraftingInterfaceBlockEntity extends AENetworkBlockEntity implements ICraftingInterfaceHost {
    private final DualityCraftingInterface duality = new DualityCraftingInterface(this.getMainNode(), this);
    private boolean omniDirectional = false;

    public CraftingInterfaceBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        this.duality.onMainNodeStateChanged();
    }

    public void setSide(final Direction facing) {
        if (isRemote()) {
            return;
        }

        Direction newForward = facing;

        if (!this.omniDirectional && this.getForward() == facing.getOpposite()) {
            newForward = facing;
        } else if (!this.omniDirectional
                && (this.getForward() == facing || this.getForward() == facing.getOpposite())) {
            this.omniDirectional = true;
        } else if (this.omniDirectional) {
            newForward = facing.getOpposite();
            this.omniDirectional = false;
        } else {
            newForward = Platform.rotateAround(this.getForward(), facing);
        }

        if (this.omniDirectional) {
            this.setOrientation(Direction.NORTH, Direction.UP);
        } else {
            Direction newUp = Direction.UP;
            if (newForward == Direction.UP || newForward == Direction.DOWN) {
                newUp = Direction.NORTH;
            }
            this.setOrientation(newForward, newUp);
        }

        this.configureNodeSides();
        this.markForUpdate();
        this.saveChanges();
    }

    private void configureNodeSides() {
        if (this.omniDirectional) {
            this.getMainNode().setExposedOnSides(EnumSet.allOf(Direction.class));
        } else {
            this.getMainNode().setExposedOnSides(EnumSet.complementOf(EnumSet.of(this.getForward())));
        }
    }

    @Override
    public void getDrops(final Level level, final BlockPos pos, final List<ItemStack> drops) {
        this.duality.addDrops(drops);
    }

    @Override
    public void onReady() {
        this.configureNodeSides();

        super.onReady();
        this.duality.updatePatterns();
    }

    @Override
    public CompoundTag save(final CompoundTag data) {
        super.save(data);
        data.putBoolean("omniDirectional", this.omniDirectional);
        this.duality.writeToNBT(data);
        return data;
    }

    @Override
    public void load(final CompoundTag data) {
        super.load(data);
        this.omniDirectional = data.getBoolean("omniDirectional");

        this.duality.readFromNBT(data);
    }

    @Override
    protected boolean readFromStream(final FriendlyByteBuf data) throws IOException {
        final boolean c = super.readFromStream(data);
        boolean oldOmniDirectional = this.omniDirectional;
        this.omniDirectional = data.readBoolean();
        return oldOmniDirectional != this.omniDirectional || c;
    }

    @Override
    protected void writeToStream(final FriendlyByteBuf data) throws IOException {
        super.writeToStream(data);
        data.writeBoolean(this.omniDirectional);
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return AEBlocks.CRAFTING_INTERFACE.stack();
    }

    @Override
    public DualityCraftingInterface getDuality() {
        return duality;
    }

    @Override
    public EnumSet<Direction> getTargets() {
        if (this.omniDirectional) {
            return EnumSet.allOf(Direction.class);
        }
        return EnumSet.of(this.getForward());
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return duality.getCapability(cap);
    }

    public boolean isOmniDirectional() {
        return this.omniDirectional;
    }
}
