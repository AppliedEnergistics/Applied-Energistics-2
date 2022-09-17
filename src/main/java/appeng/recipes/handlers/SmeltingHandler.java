package appeng.recipes.handlers;


import appeng.recipes.IAERecipeFactory;
import appeng.recipes.factories.recipes.PartRecipeFactory;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.fml.common.registry.GameRegistry;


public class SmeltingHandler implements IAERecipeFactory {
    @Override
    public void register(JsonObject json, JsonContext ctx) {
        ItemStack result = PartRecipeFactory.getResult(json, ctx);
        ItemStack[] input = CraftingHelper.getIngredient(json.get("input"), ctx).getMatchingStacks();
        float xp = 0.0f;
        if (json.has("xp")) {
            xp = JsonUtils.getFloat(json, "xp");
        }

        for (int i = 0; i < input.length; ++i) {
            GameRegistry.addSmelting(input[i], result, xp);
        }
    }

}
