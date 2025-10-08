package appeng.blockentity.storage;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;

import appeng.blockentity.AEBaseBlockEntity;

public class SkyStoneTankBlockEntity extends AEBaseBlockEntity {

    public static final int BUCKET_CAPACITY = 16;

    protected FluidStacksResourceHandler tank = new FluidStacksResourceHandler(1,
            FluidType.BUCKET_VOLUME * BUCKET_CAPACITY) {
        @Override
        protected void onContentsChanged(int index, FluidStack previousContents) {
            SkyStoneTankBlockEntity.this.markForUpdate();
            SkyStoneTankBlockEntity.this.setChanged();
        }
    };

    public SkyStoneTankBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    @Override
    public void saveAdditional(ValueOutput data) {
        super.saveAdditional(data);
        data.putChild("tank", tank);
    }

    @Override
    public void loadTag(ValueInput data) {
        super.loadTag(data);
        tank.deserialize(data.childOrEmpty("content"));
    }

    public boolean onPlayerUse(Player player, InteractionHand hand) {
        return FluidUtil.interactWithFluidHandler(player, hand, getBlockPos(), tank);
    }

    public ResourceHandler<FluidResource> getFluidHandler() {
        return tank;
    }

    protected boolean readFromStream(RegistryFriendlyByteBuf data) {
        boolean ret = super.readFromStream(data);
        var input = TagValueInput.create(ProblemReporter.DISCARDING, data.registryAccess(), data.readNbt());
        tank.deserialize(input);
        return ret;
    }

    protected void writeToStream(RegistryFriendlyByteBuf data) {
        super.writeToStream(data);
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, data.registryAccess());
        tank.serialize(output);
        data.writeNbt(output.buildResult());
    }
}
