package appeng.bootstrap.components;

import java.util.function.Function;

import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

/**
 * Registers a block entity renderer for a given block entity type. This must occur late in the client's initialization,
 * since the constructors of our block entity renderers rely on the client being fully initialized.
 */
public class BlockEntityRendererComponent<T extends TileEntity> implements IClientSetupComponent {

    private final TileEntityType<T> type;

    private final Function<TileEntityRendererDispatcher, TileEntityRenderer<T>> renderer;

    public BlockEntityRendererComponent(TileEntityType<T> type,
            Function<TileEntityRendererDispatcher, TileEntityRenderer<T>> renderer) {
        this.type = type;
        this.renderer = renderer;
    }

    @Override
    public void setup() {
        BlockEntityRendererRegistry.INSTANCE.register(type, renderer);
    }

}
