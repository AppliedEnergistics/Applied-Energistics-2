package appeng.bootstrap.components;

import java.util.function.Function;

import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

/**
 * Registers a tile entity renderer for a given tile entity type. This must occur late in the client's initialization,
 * since the constructors of our tile entity renderers rely on the client being fully initialized.
 */
public class TileEntityRendererComponent<T extends TileEntity> implements IClientSetupComponent {

    private final TileEntityType<T> type;

    private final Function<TileEntityRendererDispatcher, TileEntityRenderer<T>> renderer;

    public TileEntityRendererComponent(TileEntityType<T> type,
            Function<TileEntityRendererDispatcher, TileEntityRenderer<T>> renderer) {
        this.type = type;
        this.renderer = renderer;
    }

    @Override
    public void setup() {
        BlockEntityRendererRegistry.INSTANCE.register(type, renderer);
    }

}
