package me.dessie.dessielib.core.utils.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

/**
 * A Builder style class for creating Gson {@link JsonArray}s
 */
public class JsonArrayBuilder {

    private JsonArray array;

    public JsonArrayBuilder() {
        this.array = new JsonArray();
    }

    public JsonArrayBuilder add(JsonElement property) {
        array.add(property);
        return this;
    }

    public JsonArrayBuilder add(String property) {
        array.add(property);
        return this;
    }

    public JsonArrayBuilder add(Number property) {
        array.add(property);
        return this;
    }

    public JsonArrayBuilder add(Character property) {
        array.add(property);
        return this;
    }

    public JsonArrayBuilder add(Boolean property) {
        array.add(property);
        return this;
    }

    public JsonArray getArray() {
        return array;
    }
}
