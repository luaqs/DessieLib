package me.dessie.dessielib.core.utils.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * A Builder style class for creating Gson {@link JsonArray}s
 */
public class JsonObjectBuilder {

    private JsonObject object;

    public JsonObjectBuilder() {
        this.object = new JsonObject();
    }

    public JsonObjectBuilder add(String key, JsonElement element) {
        object.add(key, element);
        return this;
    }

    public JsonObjectBuilder add(String key, String property) {
        object.addProperty(key, property);
        return this;
    }

    public JsonObjectBuilder add(String key, Number property) {
        object.addProperty(key, property);
        return this;
    }

    public JsonObjectBuilder add(String key, Boolean property) {
        object.addProperty(key, property);
        return this;
    }

    public JsonObjectBuilder add(String key, Character property) {
        object.addProperty(key, property);
        return this;
    }

    public JsonObject getObject() {
        return object;
    }
}
