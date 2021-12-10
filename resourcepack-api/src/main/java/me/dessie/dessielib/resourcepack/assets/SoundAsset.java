package me.dessie.dessielib.resourcepack.assets;

import me.dessie.dessielib.resourcepack.ResourcePackBuilder;
import me.dessie.dessielib.core.utils.json.JsonArrayBuilder;
import me.dessie.dessielib.core.utils.json.JsonObjectBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.bukkit.SoundCategory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SoundAsset extends Asset {

    private String path;
    private File soundFile;
    private SoundCategory category;
    private boolean stream;

    private final File resourceSoundsFolder;

    /**
     *
     * @param path The path name for the sound file.
     *             Examples:
     *             entity.enderman
     *             block.sand
     * @param source The SoundSource this sound plays from
     * @param stream If the sound should be streamed from the File instead of played at once.
     *               Use this if your sound is long, such as a music disc.
     * @param soundFile The .ogg file to play when this sound is played.
     */
    public SoundAsset(String path, SoundCategory source, boolean stream, File soundFile) {
        super(soundFile.getName().split("\\.ogg")[0]);

        if(!FilenameUtils.getExtension(soundFile.getName()).equalsIgnoreCase("ogg")) {
            throw new IllegalArgumentException("Sound files must be in ogg format!");
        }

        this.resourceSoundsFolder = new File(this.getNamespaceFolder() + "/sounds");

        this.stream = stream;
        this.path = path;
        this.soundFile = soundFile;
        this.category = source;
    }

    public SoundAsset(String path, SoundCategory source, File soundFile) {
        this(path, source, false, soundFile);
    }

    public boolean isStreamed() {
        return stream;
    }
    public SoundCategory getCategory() { return category; }

    public File getSoundFile() {return soundFile;}
    public String getPath() {return path;}
    public File getResourceSoundsFolder() {return resourceSoundsFolder;}

    @Override
    public void init(ResourcePackBuilder builder) {
        this.getResourceSoundsFolder().mkdirs();

        String assetPath = this.getPath().replace(".", "/");
        //Copy the given Sound File to the proper path.
        try {
            FileUtils.copyFile(this.getSoundFile(), new File(this.getResourceSoundsFolder() + "/" + assetPath + "/" + this.getSoundFile().getName()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates the sounds.json file to create all the custom Sounds for the resourcepack.
     * Also copies the sound files into their respective directories.
     */
    @Override
    public void generate(ResourcePackBuilder builder) {
        JsonObjectBuilder object = new JsonObjectBuilder();

        Map<String, List<SoundAsset>> sounds = new HashMap<>();
        builder.getAssetsOf(SoundAsset.class).forEach(sound -> {
            sounds.putIfAbsent(sound.getPath(), new ArrayList<>());
            sounds.get(sound.getPath()).add(sound);

            //Set all Assets as generated, since we only want this to run once.
            sound.setGenerated(true);
        });

        for(String path : sounds.keySet()) {
            JsonArrayBuilder array = new JsonArrayBuilder();
            for(SoundAsset asset : sounds.get(path)) {
                String assetPath = asset.getPath().replace(".", "/");
                object.add(asset.getPath(),
                        new JsonObjectBuilder().add("sounds", array
                                .add(new JsonObjectBuilder()
                                        .add("name", this.getNamespace() + ":" + assetPath + "/" + asset.getName())
                                        .add("stream", asset.isStreamed()).getObject())
                                .getArray()).getObject());
            }
        }
        write(object.getObject(), new File(this.getNamespaceFolder() + "/sounds.json"));
    }
}
