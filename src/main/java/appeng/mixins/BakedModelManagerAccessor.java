package appeng.mixins;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.util.Identifier;

@Mixin(BakedModelManager.class)
public interface BakedModelManagerAccessor {

    @Accessor
    Map<Identifier, BakedModel> getModels();

}
