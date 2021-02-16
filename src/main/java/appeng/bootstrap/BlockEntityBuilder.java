package appeng.bootstrap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.function.BiFunction;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Function3;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import appeng.api.features.AEFeature;
import appeng.block.AEBaseTileBlock;
import appeng.bootstrap.components.BlockEntityRendererComponent;
import appeng.bootstrap.components.ITileEntityRegistrationComponent;
import appeng.bootstrap.definitions.TileEntityDefinition;
import appeng.core.AppEng;
import appeng.core.features.ActivityState;
import appeng.core.features.BlockStackSrc;
import appeng.tile.AEBaseBlockEntity;
import appeng.util.Platform;

/**
 * Used to define our block entities and all of their properties that are relevant to registering them.
 *
 * @param <T>
 */
public class BlockEntityBuilder<T extends AEBaseBlockEntity> {

    private final FeatureFactory factory;

    private final Identifier id;

    // The block entity class
    private final Class<T> tileClass;

    private BlockEntityType<T> type;

    // The factory for creating block entity objects
    private final Function3<BlockEntityType<T>, BlockPos, BlockState, T> supplier;

    private TileEntityRendering<T> tileEntityRendering;

    private final List<Block> blocks = new ArrayList<>();

    private final EnumSet<AEFeature> features = EnumSet.noneOf(AEFeature.class);

    public BlockEntityBuilder(FeatureFactory factory, String id, Class<T> tileClass,
            Function3<BlockEntityType<T>, BlockPos, BlockState, T> supplier) {
        this.factory = factory;
        this.id = AppEng.makeId(id);
        this.tileClass = tileClass;
        this.supplier = supplier;

        if (Platform.hasClientClasses()) {
            this.tileEntityRendering = new TileEntityRendering<>();
        }
    }

    public BlockEntityBuilder<T> features(AEFeature... features) {
        this.features.clear();
        this.addFeatures(features);
        return this;
    }

    public BlockEntityBuilder<T> addFeatures(AEFeature... features) {
        Collections.addAll(this.features, features);
        return this;
    }

    public BlockEntityBuilder<T> rendering(TileEntityRenderingCustomizer<T> customizer) {
        customizer.customize(tileEntityRendering);
        return this;
    }

    @SuppressWarnings("unchecked")
    public TileEntityDefinition build() {

        this.factory.addBootstrapComponent((ITileEntityRegistrationComponent) () -> {
            if (blocks.isEmpty()) {
                throw new IllegalStateException("No blocks make use of this block entity: " + tileClass);
            }

            BiFunction<BlockPos, BlockState, T> factory = (pos, state) -> supplier.apply(type, pos, state);
            type = FabricBlockEntityTypeBuilder.create(factory::apply, blocks.toArray(new Block[0])).build(null);

            Registry.register(Registry.BLOCK_ENTITY_TYPE, id, type);

            AEBaseBlockEntity.registerTileItem(tileClass, new BlockStackSrc(blocks.get(0), ActivityState.Enabled));

            for (Block block : blocks) {
                if (block instanceof AEBaseTileBlock) {
                    AEBaseTileBlock<T> baseTileBlock = (AEBaseTileBlock<T>) block;
                    baseTileBlock.setTileEntity(tileClass, factory);
                }
            }

            if (Platform.hasClientClasses()) {
                buildClient();
            }
        });

        return new TileEntityDefinition(this::addBlock);

    }

    @Environment(EnvType.CLIENT)
    private void buildClient() {
        if (tileEntityRendering.tileEntityRenderer != null) {
            factory.addBootstrapComponent(
                    new BlockEntityRendererComponent<T>(type, tileEntityRendering.tileEntityRenderer));
        }
    }

    private void addBlock(Block block) {
        Preconditions.checkState(type == null, "No more blocks can be added after registration completed.");
        this.blocks.add(block);
    }

}
