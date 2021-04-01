package appeng.mixins;

import java.util.Map;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelManager;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ModelManager.class)
public interface BakedModelManagerAccessor {

    @Accessor
    Map<ResourceLocation, IBakedModel> getModels();

}
