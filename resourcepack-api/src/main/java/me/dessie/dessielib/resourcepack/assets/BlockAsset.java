package me.dessie.dessielib.resourcepack.assets;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.dessie.dessielib.resourcepack.ResourcePackBuilder;
import me.dessie.dessielib.core.utils.json.JsonObjectBuilder;
import org.apache.commons.io.FileUtils;
import org.bukkit.Material;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class BlockAsset extends Asset {

    private final List<TextureAsset> textures = new ArrayList<>();
    private final File modelJson;
    private final Material block;

    private final File resourceBlockTextureFolder;
    private final File resourceBlockModelFolder;
    private final File minecraftBlockModelFolder;

    //Determines if the Minecraft version of this item should be replaced.
    private boolean replaces;

    //The name of the file in the assets/minecraft folder.
    private final String replacementName;

    //The parent model type to apply to this Block
    //For example, "minecraft:block/fence_post" or "minecraft:block/template_anvil"
    //This also supports custom models.
    private String parentModelType;

    public BlockAsset(String name, Material block, @Nullable File modelJson, TextureAsset... textures) {
        this(name, block, block.name().toLowerCase(Locale.ROOT), modelJson, true, textures);
    }

    public BlockAsset(String name, Material block, @Nullable File modelJson, boolean replaceAll, TextureAsset... textures) {
        this(name, block, block.name().toLowerCase(Locale.ROOT), modelJson, replaceAll, textures);
    }

    /**
     * @param name The name of the custom block asset.
     * @param block The Material this asset replaces
     * @param replacementName The name within the resource pack. Generally this can be ignored because it can
     *                        be assumed from the Material.
     *
     *                        However, in some cases, such as the side of a fence, you'll need to force the name to replace
     *                        "oak_fence_side".
     * @param modelJson A provided default model JSON
     * @param replaceAll If this model replaces all states of this block
     *                   If this is false, you need to use BlockStateAssets to determine when this model is shown.
     * @param textures The textures
     */
    public BlockAsset(String name, Material block, String replacementName, @Nullable File modelJson, boolean replaceAll, TextureAsset... textures) {
        super(name);
        if(block == null || !block.isBlock()) throw new IllegalArgumentException("Block must not be null and must be type of Block!");

        this.textures.addAll(new ArrayList<>(Arrays.asList(textures)));
        this.block = block;
        this.replacementName = replacementName;
        this.modelJson = modelJson;
        this.replaces = replaceAll;

        //Define the files.
        this.resourceBlockTextureFolder = new File(this.getNamespaceFolder() + "/textures/block");
        this.resourceBlockModelFolder = new File(this.getNamespaceFolder() + "/models/block");
        this.minecraftBlockModelFolder = new File(this.getAssetsFolder() + "/minecraft/models/block");

        //Set the default parent.
        this.parentModelType = "minecraft:block/cube_all";
    }

    public BlockAsset setReplaces(boolean replaces) {
        this.replaces = replaces;
        return this;
    }

    public BlockAsset setParentModelType(String parentModelType) {
        this.parentModelType = parentModelType;
        return this;
    }

    public String getParentModel() {return parentModelType;}
    public File getModel() {
        return modelJson;
    }
    public List<TextureAsset> getTextures() {return textures;}
    public Material getBlock() {
        return block;
    }
    public String getReplacementName() {return replacementName;}
    public boolean isReplaces() {return replaces;}

    public String getParentModelType() {return parentModelType;}

    public File getResourceBlockModelFolder() {return resourceBlockModelFolder;}
    public File getResourceBlockTextureFolder() {return resourceBlockTextureFolder;}
    public File getMinecraftBlockModelFolder() {return minecraftBlockModelFolder;}

    @Override
    public void init(ResourcePackBuilder builder) throws IOException {
        this.getMinecraftBlockModelFolder().mkdirs();

        this.getResourceBlockModelFolder().mkdirs();
        this.getResourceBlockTextureFolder().mkdirs();

        //Copy the textures
        for(TextureAsset texture : this.getTextures()) {
            if(texture.getTextureFile() == null) continue;
            FileUtils.copyFile(texture.getTextureFile(), new File(this.getResourceBlockTextureFolder() + "/" + texture.getTextureFile().getName()));
        }
    }

    @Override
    public void generate(ResourcePackBuilder builder) {
        String fileName = this.getReplacementName() + ".json";

        //Create the Minecraft model replacement file.
        //Only do this if all states should be overwritten.
        //------------------------------------------------
        if(this.isReplaces()) {
            File assetFile = new File(this.getMinecraftBlockModelFolder(), fileName);
            try {
                //Generate the .json for the asset with the appropriate custom model id.
                assetFile.createNewFile();
                JsonObject json = new JsonObjectBuilder().add("parent", this.getNamespace() + ":block/" + this.getName())
                        .getObject();
                //Save the JSON file.
                write(json, assetFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //------------------------------------------------

        //Create or Copy the model file.
        //------------------------------------------------
        File customModel = new File(this.getResourceBlockModelFolder(), this.getName() + ".json");
        if(this.getModel() == null) {
            JsonObjectBuilder textureObject = new JsonObjectBuilder();
            for(TextureAsset texture : this.getTextures()) {
                //If the file is null, the texture is just whatever name they provided.
                //If the file isn't null, we get the texture from its name.
                if(texture.getTextureFile() == null) {
                    textureObject.add(texture.getKey(), texture.getName());
                } else {
                    textureObject.add(texture.getKey(), this.getNamespace() + ":block/" + texture.getName());
                }
            }

            JsonObject json = new JsonObjectBuilder().add("parent", this.getParentModel())
                    .add("textures", textureObject.getObject()).getObject();

            write(json, customModel);
        } else {
            try {
                //Load in the provided JSON file.
                JsonObject modelJson = new JsonParser().parse(new FileReader(this.getModel())).getAsJsonObject();
                JsonObject textures = modelJson.get("textures").getAsJsonObject();

                //Make sure the textures point to the correct .png textures.
                for(Map.Entry<String, JsonElement> entry : textures.entrySet()) {
                    //Remap to the custom namespace if it's not Minecraft.
                    if(!entry.getValue().getAsString().startsWith("minecraft:")) {
                        textures.addProperty(entry.getKey(), this.getNamespace() + ":block/" + this.getName());
                    }
                }

                //Write the file.
                write(modelJson, customModel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //------------------------------------------------

    }
}
