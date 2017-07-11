package appeng.recipes.helpers;

import appeng.core.AppEng;
import com.google.gson.JsonObject;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;

/**
 * @author GuntherDW
 */
public class PartRecipeFactory implements IRecipeFactory
{
    @Override
    public IRecipe parse( JsonContext context, JsonObject json )
    {
        String type = JsonUtils.getString( json, "type" );
        if( type.contains( "shaped" ) )
        {
            PartShapedCraftingFactory recipe = PartShapedCraftingFactory.factory( context, json );
            CraftingHelper.ShapedPrimer primer = new CraftingHelper.ShapedPrimer();
            primer.width = recipe.getWidth();
            primer.height = recipe.getHeight();
            primer.mirrored = JsonUtils.getBoolean( json, "mirrored", true );
            primer.input = recipe.getIngredients();

            return new PartShapedCraftingFactory( new ResourceLocation( AppEng.MOD_ID, "part_shaped_crafting" ), recipe.getRecipeOutput(), primer );
        }
        else if( type.contains( "shapeless" ) )
        {
            PartShapelessCraftingFactory recipe = PartShapelessCraftingFactory.factory( context, json );

            return new PartShapelessCraftingFactory( new ResourceLocation( AppEng.MOD_ID, "part_shapeless_crafting" ), recipe.getIngredients(), recipe.getRecipeOutput() );
        }
        return null;
    }
}
