package me.dessie.dessielib.resourcepack.assets;

import me.dessie.dessielib.resourcepack.ResourcePackBuilder;
import me.dessie.dessielib.core.utils.json.JsonObjectBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LanguageAsset extends Asset {

    private final File langFolder;
    private final Map<String, String> lang = new HashMap<>();

    public LanguageAsset() {
        this("en_us");
    }
    public LanguageAsset(String key, String value) {
        this("en_us", key, value);
    }

    public LanguageAsset(String language) {
        super(language);
        this.langFolder = new File(this.getAssetsFolder() + "/minecraft/lang");
    }

    public LanguageAsset(String language, String key, String value) {
        this(language);
        this.add(key, value);
    }

    public File getLangFolder() {return langFolder;}
    public Map<String, String> getLanguage() {return lang;}

    public LanguageAsset add(String key, String value) {
        this.getLanguage().put(key, value);
        return this;
    }

    @Override
    public void init(ResourcePackBuilder builder) {
        this.getLangFolder().mkdirs();
    }

    /**
     * Generates all the required language files that should be created with the texture pack.
     * @param builder The ResourcePackBuilder that is generating this Asset.
     */
    @Override
    public void generate(ResourcePackBuilder builder) {
        Map<String, List<LanguageAsset>> assets = new HashMap<>();

        //Sort the Asset files by their specific language folder, (en_us, es_LA, etc)
        for(LanguageAsset asset : builder.getAssetsOf(LanguageAsset.class)) {
            assets.putIfAbsent(asset.getName(), new ArrayList<>());
            assets.get(asset.getName()).add(asset);

            //Set generated to true, since all are generated at once.
            asset.setGenerated(true);
        }

        //For each language, create the JSON object and write it to its file.
        for(String lang : assets.keySet()) {
            JsonObjectBuilder object = new JsonObjectBuilder();
            for(LanguageAsset asset : assets.get(lang)) {
                for(String key : asset.getLanguage().keySet()) {
                    object.add(key, asset.getLanguage().get(key));
                }
            }
            write(object.getObject(), new File(this.getLangFolder() + "/" + lang + ".json"));
        }

    }
}
