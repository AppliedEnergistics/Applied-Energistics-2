package appeng.recipes.factories.conditions;

import appeng.core.Api;
import appeng.core.AppEng;
import com.google.gson.JsonObject;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;

import java.util.function.BooleanSupplier;

public class PartExists implements IConditionFactory {

    private static final String JSON_MATERIAL_KEY = "part";

    @Override
    public BooleanSupplier parse(JsonContext jsonContext, JsonObject jsonObject )
    {
        final boolean result;

        if( JsonUtils.isString( jsonObject, JSON_MATERIAL_KEY ) )
        {
            final String part = JsonUtils.getString( jsonObject, JSON_MATERIAL_KEY );
            final Object item = Api.INSTANCE.registries().recipes().resolveItem( AppEng.MOD_ID, part );

            result = item != null;
        }
        else
        {
            result = false;
        }

        return () -> result;

    }
}
