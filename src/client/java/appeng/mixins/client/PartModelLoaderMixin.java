package appeng.mixins.client;

import appeng.client.AppEngClientRendering;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.resources.model.BlockStateModelLoader;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(BlockStateModelLoader.class)
public class PartModelLoaderMixin {
    @WrapMethod(method = "loadBlockStates")
    private static CompletableFuture<BlockStateModelLoader.LoadedModels> loadBlockStates(
            ResourceManager resourceManager,
            Executor executor,
            Operation<CompletableFuture<BlockStateModelLoader.LoadedModels>> operation
    ) {
        var partModelReload = AppEngClientRendering.getInstance().getPartModels().reload(resourceManager, executor);
        return partModelReload.thenCombine(operation.call(resourceManager, executor),
                (unused, loadedModels) -> loadedModels);
    }
}
