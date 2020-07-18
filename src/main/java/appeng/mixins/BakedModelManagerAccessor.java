package appeng.mixins;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(BakedModelManager.class)
public interface BakedModelManagerAccessor {

    @Accessor
    Map<Identifier, BakedModel> getModels();

}
