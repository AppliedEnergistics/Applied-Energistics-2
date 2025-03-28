package appeng.client.render.model;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.template.CustomLoaderBuilder;

public class BuiltInModelLoaderBuilder extends CustomLoaderBuilder {
    private ResourceLocation id;

    public BuiltInModelLoaderBuilder() {
        super(BuiltInModelLoader.ID, false);
    }

    public BuiltInModelLoaderBuilder id(ResourceLocation id) {
        this.id = id;
        return this;
    }

    @Override
    protected BuiltInModelLoaderBuilder copyInternal() {
        var result = new BuiltInModelLoaderBuilder();
        result.id = id;
        return result;
    }

    @Override
    public JsonObject toJson(JsonObject json) {
        var data = super.toJson(json);
        Preconditions.checkNotNull(id, "modelLocation must not be null");
        json.addProperty("ae2:model", id.toString());
        return data;
    }
}
