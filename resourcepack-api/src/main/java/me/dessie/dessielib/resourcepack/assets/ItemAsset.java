package me.dessie.dessielib.resourcepack.assets;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.dessie.dessielib.resourcepack.ResourcePackBuilder;
import me.dessie.dessielib.core.utils.json.JsonArrayBuilder;
import me.dessie.dessielib.core.utils.json.JsonObjectBuilder;
import org.apache.commons.io.FileUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ItemAsset extends Asset {

    private final File texture;
    private final File modelJson;
    private final ItemStack item;
    private int customModelId;

    private final File resourceItemTextureFolder;
    private final File resourceItemModelFolder;
    private final File minecraftItemModelFolder;

    public ItemAsset(String name, File texture, @Nullable File modelJson, ItemStack item) {
        super(name);
        if(texture == null || !texture.exists()) throw new IllegalArgumentException("Texture is null or does not exist");
        if(item == null || item.getItemMeta() == null) throw new IllegalArgumentException("Item & ItemMeta cannot be null");

        this.texture = texture;
        this.item = item;
        this.modelJson = modelJson;

        this.resourceItemTextureFolder = new File(this.getNamespaceFolder() + "/textures/item");
        this.resourceItemModelFolder = new File(this.getNamespaceFolder() + "/models/item");
        this.minecraftItemModelFolder = new File(this.getAssetsFolder() + "/minecraft/models/item");
    }

    public ItemAsset(File texture, ItemStack item) {
        this(texture, null, item);
    }

    public ItemAsset(File texture, File modelJson, ItemStack item) {
        this(Objects.requireNonNull(texture.getName()).split("\\.png")[0], texture, modelJson, item);
    }

    public void setCustomModelId(int customModelId) {
        this.customModelId = customModelId;

        ItemMeta meta = this.getItem().getItemMeta();
        meta.setCustomModelData(customModelId);
        this.getItem().setItemMeta(meta);
    }

    public int getCustomModelId() {
        return customModelId;
    }
    public File getTexture() {
        return texture;
    }
    public ItemStack getItem() {
        return item;
    }
    public File getModel() { return modelJson; }

    public File getMinecraftItemModelFolder() {
        return minecraftItemModelFolder;
    }
    public File getResourceItemModelFolder() {
        return resourceItemModelFolder;
    }
    public File getResourceItemTextureFolder() {
        return resourceItemTextureFolder;
    }

    @Override
    public void init(ResourcePackBuilder builder) {
        //Create the files.
        this.getMinecraftItemModelFolder().mkdirs();
        this.getResourceItemModelFolder().mkdirs();
        this.getResourceItemTextureFolder().mkdirs();

        //Copy the texture png file to it's proper folder.
        try {
            FileUtils.copyFile(this.getTexture(), new File(this.getResourceItemTextureFolder() + "/" + this.getTexture().getName()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates/Edits the required JSON model file for a custom model and texture of an ItemStack.
     *
     * If a JSON model is supplied, this method will verify that the Custom Model's textures all
     * point to the correct places.
     *
     * If a JSON model is not supplied, it will be created to correctly point to the proper (2D) texture.
     * @param builder The ResourcePackBuilder that is generating this asset
     */
    @Override
    public void generate(ResourcePackBuilder builder) {
        //Generate the Minecraft overrides.
        //For example, create the file for stick.json to replace sticks.
        Map<Material, List<ItemAsset>> materials = new HashMap<>();

        List<ItemAsset> itemAssets = builder.getAssetsOf(ItemAsset.class);

        AtomicInteger counter = new AtomicInteger();
        itemAssets.forEach(asset -> {
            materials.putIfAbsent(asset.getItem().getType(), new ArrayList<>());
            materials.get(asset.getItem().getType()).add(asset);

            //Set all Assets as generated, since we only want this to run once.
            asset.setGenerated(true);

            //Set the Custom Model data for this item.
            //If this is the first one, start at 0
            if(counter.get() == 0) {
                asset.setCustomModelId(1);
            } else {
                //Otherwise, increment it by 1
                asset.setCustomModelId(itemAssets.get(itemAssets.size() - 1).getCustomModelId() + 1);
            }
            counter.getAndIncrement();
        });

        for(Material material : materials.keySet()) {
            JsonArrayBuilder array = new JsonArrayBuilder();
            for(ItemAsset asset : materials.get(material)) {
                String materialName = asset.getItem().getType().name().toLowerCase();
                File assetFile = new File(this.getMinecraftItemModelFolder(), materialName + ".json");
                try {
                    //Generate the .json for the asset with the appropriate custom model id.
                    assetFile.createNewFile();
                    JsonObject object = new JsonObjectBuilder().add("parent", "minecraft:item/generated")
                            .add("textures", new JsonObjectBuilder().add("layer0", "minecraft:item/" + materialName).getObject())
                            .add("overrides", array
                                    .add(new JsonObjectBuilder().add("predicate", new JsonObjectBuilder()
                                                    .add("custom_model_data", asset.getCustomModelId()).getObject())
                                            .add("model", this.getNamespace() + ":item/" + asset.getName().split("\\.png")[0]).getObject()).getArray())
                            .getObject();
                    //Save the JSON file.
                    write(object, assetFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        File customModel = new File(this.getResourceItemModelFolder(), this.getName() + ".json");
        if(this.getModel() == null) {
            JsonObject object = new JsonObjectBuilder().add("parent", "minecraft:item/generated")
                    .add("textures", new JsonObjectBuilder()
                            .add("layer0", this.getNamespace() + ":item/" + this.getName()).getObject()).getObject();

            write(object, customModel);
        } else {
            try {
                //Load in the provided JSON file.
                JsonObject modelJson = new JsonParser().parse(new FileReader(this.getModel())).getAsJsonObject();
                JsonObject textures = modelJson.get("textures").getAsJsonObject();

                //Make sure the textures point to the correct .png textures.
                for(Map.Entry<String, JsonElement> entry : textures.entrySet()) {
                    if(!entry.getValue().getAsString().startsWith("minecraft:")) {
                        textures.addProperty(entry.getKey(), this.getNamespace() + ":item/" + this.getName());
                    }
                }

                //Write the file.
                write(modelJson, customModel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
