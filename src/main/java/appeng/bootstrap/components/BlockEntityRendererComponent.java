package appeng.bootstrap.components;

import java.util.function.Function;

import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;

/**
 * Registers a block entity renderer for a given block entity type. This must occur late in the client's initialization,
 * since the constructors of our block entity renderers rely on the client being fully initialized.
 */
public class BlockEntityRendererComponent<T extends BlockEntity> implements IClientSetupComponent {

    private final BlockEntityType<T> type;

    private final Function<BlockEntityRenderDispatcher, BlockEntityRenderer<T>> renderer;

    public BlockEntityRendererComponent(BlockEntityType<T> type,
            Function<BlockEntityRenderDispatcher, BlockEntityRenderer<T>> renderer) {
        this.type = type;
        this.renderer = renderer;
    }

    @Override
    public void setup() {
        BlockEntityRendererRegistry.INSTANCE.register(type, renderer);
    }

}
