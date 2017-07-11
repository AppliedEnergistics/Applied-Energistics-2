package appeng.recipes.helpers;

import appeng.api.recipes.ResolverResult;
import appeng.api.recipes.ResolverResultSet;
import appeng.core.AELog;
import appeng.core.Api;
import appeng.core.AppEng;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import javax.annotation.Nonnull;

/**
 * @author GuntherDW
 */
public class PartShapelessCraftingFactory extends ShapelessOreRecipe
{

    public PartShapelessCraftingFactory( ResourceLocation group, NonNullList<Ingredient> input, @Nonnull ItemStack result )
    {
        super( group, input, result );
    }

    // Copied from ShapelessOreRecipe.java, modified a bit.
    public static PartShapelessCraftingFactory factory( JsonContext context, JsonObject json)
    {
        String group = JsonUtils.getString(json, "group", "");

        NonNullList<Ingredient> ings = NonNullList.create();
        for( JsonElement ele : JsonUtils.getJsonArray( json, "ingredients" ) )
            ings.add( CraftingHelper.getIngredient( ele, context ) );

        if( ings.isEmpty() )
            throw new JsonParseException("No ingredients for shapeless recipe");

        JsonObject resultObject = (JsonObject) json.get( "result" );

        String ingredient = resultObject.get( "part" ).getAsString();
        Object result = (Object) Api.INSTANCE.registries().recipes().resolveItem( AppEng.MOD_ID, ingredient );
        if( result instanceof ResolverResultSet )
        {
            ResolverResultSet resolverResultSet = (ResolverResultSet) result;
            return new PartShapelessCraftingFactory(group.isEmpty() ? null : new ResourceLocation(group), ings, resolverResultSet.results.toArray( new ItemStack[resolverResultSet.results.size()] )[0]);
        }
        else if( result instanceof ResolverResult )
        {
            ResolverResult resolverResult = (ResolverResult) result;

            Item item = Item.getByNameOrId( AppEng.MOD_ID + ":" + resolverResult.itemName );

            if( item == null )
                AELog.warn( "item was null for " + resolverResult.itemName + " ( " + ingredient + " )!" );

            ItemStack itemStack = new ItemStack( item, 1, resolverResult.damageValue, resolverResult.compound );

            return new PartShapelessCraftingFactory(group.isEmpty() ? null : new ResourceLocation(group), ings, itemStack);
        }
        return null;
    }
}
