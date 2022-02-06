package appeng.blockentity.storage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import appeng.blockentity.AEBaseBlockEntity;

public class SkyStoneTankBlockEntity extends AEBaseBlockEntity {

    public static final int BUCKET_CAPACITY = 16;

    protected FluidTank tank = new FluidTank(FluidAttributes.BUCKET_VOLUME * BUCKET_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            SkyStoneTankBlockEntity.this.markForUpdate();
            SkyStoneTankBlockEntity.this.setChanged();
        }
    };

    private final LazyOptional<IFluidHandler> holder = LazyOptional.of(() -> tank);

    public SkyStoneTankBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    @Override
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        if (!tank.isEmpty()) {
            tank.writeToNBT(data);
        }
    }

    @Override
    public void loadTag(CompoundTag data) {
        super.loadTag(data);
        tank.readFromNBT(data);
    }

    public boolean onPlayerUse(Player player) {
        return FluidUtil.interactWithFluidHandler(player, InteractionHand.MAIN_HAND, tank);
    }

    public IFluidHandler getStorage(Direction direction) {
        return tank;
    }

    public IFluidTank getStorage() {
        return tank;
    }

    @Override
    @Nonnull
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return holder.cast();
        }
        return super.getCapability(capability, facing);
    }

    protected boolean readFromStream(FriendlyByteBuf data) {
        boolean ret = super.readFromStream(data);
        tank.readFromNBT(data.readNbt());
        return ret;
    }

    protected void writeToStream(FriendlyByteBuf data) {
        super.writeToStream(data);
        var tag = new CompoundTag();
        tank.writeToNBT(tag);
        data.writeNbt(tag);
    }
}
