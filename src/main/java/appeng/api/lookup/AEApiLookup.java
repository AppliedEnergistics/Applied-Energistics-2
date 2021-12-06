package appeng.api.lookup;

import java.util.function.BiFunction;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Wraps an API lookup on fabric or a capability on forge to allow for platform-independent API queries.
 */
public final class AEApiLookup<A> {
    private final BlockApiLookup<A, Direction> lookup;

    public AEApiLookup(BlockApiLookup<A, Direction> lookup) {
        this.lookup = lookup;
    }

    public AEApiCache<A> createCache(ServerLevel level, BlockPos pos, Direction side) {
        return new AEApiCache<>(lookup, level, pos, side);
    }

    @Nullable
    public A find(Level level, BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity blockEntity,
            Direction side) {
        return lookup.find(level, pos, state, blockEntity, side);
    }

    @Nullable
    public A find(@Nullable BlockEntity blockEntity, Direction side) {
        if (blockEntity == null) {
            return null;
        }
        return lookup.find(blockEntity.getLevel(), blockEntity.getBlockPos(), null, blockEntity, side);
    }

    public <T extends BlockEntity> void registerForBlockEntity(BiFunction<? super T, Direction, @Nullable A> provider,
            BlockEntityType<T> blockEntityType) {
        lookup.registerForBlockEntity(provider, blockEntityType);
    }

    @SuppressWarnings("unchecked")
    public void registerSelf(BlockEntityType<?> blockEntityType) {
        registerForBlockEntity((be, part) -> (A) be, blockEntityType);
    }

    public BlockApiLookup<A, Direction> getLookup() {
        return lookup;
    }
}
