package appeng.client.api.renderer.parts;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

@FunctionalInterface
public interface PartRendererProvider<T> {
    PartRenderer<? super T, ?> create(BlockEntityRendererProvider.Context context);
}
