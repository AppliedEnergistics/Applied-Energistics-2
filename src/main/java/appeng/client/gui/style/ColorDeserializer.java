package appeng.client.gui.style;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

enum ColorDeserializer implements JsonDeserializer<Color> {

    INSTANCE;

    @Override
    public Color deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        return Color.parse(json.getAsString());
    }
}
