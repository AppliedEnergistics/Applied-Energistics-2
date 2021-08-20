package appeng.mixins;

import java.util.Map;

import com.google.gson.JsonElement;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;

import appeng.core.AppEng;
import appeng.hooks.RecipeConditions;

@Mixin(RecipeManager.class)
public class ConditionalRecipeMixin {

    /**
     * All this does is essentially filter out resources for which the conditions don't match, before the resource
     * manager actually loads them.
     */
    @Inject(method = "apply", at = @At("HEAD"))
    private void filterUnmatchedConditions(Map<ResourceLocation, JsonElement> map,
            ResourceManager resourceManager,
            ProfilerFiller profilerFiller,
            CallbackInfo info) {
        var iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            var id = entry.getKey();
            var json = entry.getValue();

            if (id.getNamespace().equals(AppEng.MOD_ID) && !RecipeConditions.areSatisfied(json)) {
                iterator.remove();
            }
        }
    }
}
