package me.dessie.dessielib.resourcepack.assets;

import com.google.gson.JsonObject;
import me.dessie.dessielib.resourcepack.ResourcePackBuilder;
import me.dessie.dessielib.core.utils.json.JsonObjectBuilder;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Creates the pack.mcmeta & copies the pack.png for the resource pack.
 */
public class MetaAsset extends Asset {

    private final File mcmetaFile;

    private String description;
    private File icon;

    private int packFormat;

    public MetaAsset(String name, String description, File icon) {
        super(name);

        this.mcmetaFile = new File(this.getResourcePackFolder(), "pack.mcmeta");
        this.description = description;
        this.icon = icon;

        // default is 7
        this.packFormat = 7;
    }

    public File getMcmetaFile() {
        return mcmetaFile;
    }

    @Override
    public void init(ResourcePackBuilder builder) throws IOException {
        //Create the MCMETA file.
        this.getMcmetaFile().createNewFile();

        //Attempt the save the Icon if it was provided.
        if (this.getIcon() != null) {
            FileUtils.copyFile(this.getIcon(), new File(this.getResourcePackFolder() + "/pack.png"));
        }
    }

    @Override
    public void generate(ResourcePackBuilder builder) {
        //Generate the JSON
        JsonObject object = new JsonObjectBuilder().add("pack", new JsonObjectBuilder()
                .add("pack_format", packFormat)
                .add("description", this.getDescription()).getObject()).getObject();

        write(object, this.getMcmetaFile());
    }

    public void setPackFormat(int packFormat) {
        this.packFormat = packFormat;
    }

    public int getPackFormat() {
        return packFormat;
    }

    public File getIcon() {
        return icon;
    }
    public String getDescription() {
        return description;
    }
}
