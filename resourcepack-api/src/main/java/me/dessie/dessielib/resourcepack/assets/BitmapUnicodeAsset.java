package me.dessie.dessielib.resourcepack.assets;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import me.dessie.dessielib.core.utils.Colors;
import me.dessie.dessielib.core.utils.json.JsonArrayBuilder;
import me.dessie.dessielib.core.utils.json.JsonObjectBuilder;
import me.dessie.dessielib.resourcepack.ResourcePackBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.logging.Level;

public class BitmapUnicodeAsset extends Asset {

    //The max pixel width for a stitched Atlas.
    private static final int maxPixelsPerAtlas = 300;

    //Holds the Unicode information
    private final File minecraftUnicodeFontFolder;

    //Where the textures for each Unicode are.
    private final File resourceUnicodeTexturesFolder;

    //The image file that this Unicode will be.
    private final File bitmappedFile;

    //The image file that this Unicode will be.
    private BufferedImage bitmappedImage;

    //Stores information about the bitmapped image within it's parent atlas.
    //This is null until the initialization process has completed.
    private AtlasData atlasData;

    //The actual texture reference. For example,
    //dessielib:font/hannahxxTea.png
    //minecraft:item/apple.png
    private String texture;

    private int height;
    private int ascent;

    //By default, Bitmap Assets will be stitched together into a texture atlas.
    //This gives the benefit that the images will take up less size, since compression works better on larger images.

    //However, when using an atlas, bitmapped heights are "ignored", as in, a bit map will not be resized.
    //If you create a bitmap atlas with 8x8 bitmaps, and set the height to 4, the bitmap will not be resized, and only half of it will display
    //(Or nothing will, enjoy your box)

    //Ideally, your bitmap images should be the exact size you want to render them in so you can use an atlas.
    boolean shouldUseInAtlas;

    //The Unicode symbol that this asset represents.
    //Will be 0 if this asset hasn't been generated.
    private int unicode = 0;

    /**
     * @param name The name of the asset
     * @param bitmappedImage A .png file to render as this bitmap
     */
    public BitmapUnicodeAsset(String name, File bitmappedImage) {
        this(name, bitmappedImage, null, 9, true);
    }

    /**
     * @param name The name of the asset
     * @param bitmappedImage A .png file to render as this bitmap
     * @param shouldUseInAtlas If the bitmap should be used in an atlas or referenced separately
     */
    public BitmapUnicodeAsset(String name, File bitmappedImage, boolean shouldUseInAtlas) {
        this(name, bitmappedImage, null, 9, shouldUseInAtlas);
    }

    /**
     * @param name The name of the asset
     * @param bitmappedImage A .png file to render as this bitmap
     * @param width Resize the image to this width
     * @param height Resize the image to this height
     */
    public BitmapUnicodeAsset(String name, File bitmappedImage, int width, int height) {
        this(name, bitmappedImage, width, height, true);
    }

    /**
     * @param name The name of the asset
     * @param bitmappedImage A .png file to render as this bitmap
     * @param width Resize the image to this width
     * @param height Resize the image to this height
     * @param shouldUseInAtlas If the bitmap should be used in an atlas or referenced separately
     */
    public BitmapUnicodeAsset(String name, File bitmappedImage, int width, int height, boolean shouldUseInAtlas) {
        this(name, bitmappedImage, null, 9, shouldUseInAtlas);

        if(this.getBitmappedImage() != null) {
            this.setHeight(height);
            Image scaled = this.getBitmappedImage().getScaledInstance(width, height, Image.SCALE_AREA_AVERAGING);
            this.bitmappedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            this.getBitmappedImage().getGraphics().drawImage(scaled, 0, 0, null);
        }
    }

    /**
     * @param name The name of the asset
     * @param bitmappedFile A bitmap image to represent this file.
     * @param texture The literal texture reference, such as dessielib:font/apple.png or minecraft:item/gold_ingot.png
     * @param ascent The ascent of the bitmap (vertical offset)
     *               Can be negative
     */
    public BitmapUnicodeAsset(String name, @Nullable File bitmappedFile, @Nullable String texture, int ascent, boolean shouldUseInAtlas) {
        super(name);

        //The image to use for this Unicode, which can be null if a Material was provided instead.
        this.bitmappedFile = bitmappedFile;
        this.shouldUseInAtlas = shouldUseInAtlas;

        //Read in the BufferedImage.
        if(this.getBitmappedFile() != null) {
            try {
                this.bitmappedImage = ImageIO.read(this.getBitmappedFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            this.bitmappedImage = null;
        }

        //The texture reference. If the bitmapped image exists, we're going to make sure the
        //texture references our namespaced texture file.
        this.texture = texture;

        //Set default height and ascent
        this.ascent = ascent;
        this.height = this.getBitmappedImage() == null ? 8 : this.getBitmappedImage().getHeight();

        //Files for the resource pack
        this.minecraftUnicodeFontFolder = new File(this.getAssetsFolder() + "/minecraft/font");
        this.resourceUnicodeTexturesFolder = new File(this.getNamespaceFolder() + "/textures/font");
    }

    /**
     * @param name The name of the asset
     * @param material A {@link Material} to render as this bitmap
     */
    public BitmapUnicodeAsset(String name, Material material) {
        this(name, null, "minecraft:item/" + material.name().toLowerCase(Locale.ROOT) + ".png",  9, false);
    }

    public File getBitmappedFile() {return bitmappedFile;}
    public BufferedImage getBitmappedImage() {return bitmappedImage;}
    public String getTexture() {return texture;}
    public int getAscent() {return ascent;}
    public int getHeight() {return height;}
    public AtlasData getAtlasData() {return atlasData;}
    public int getUnicode() {return this.unicode;}
    public boolean isUsedInAtlas() {return shouldUseInAtlas;}

    public BitmapUnicodeAsset setAscent(int ascent) {
        this.ascent = ascent;
        return this;
    }

    public BitmapUnicodeAsset setHeight(int height) {
        this.height = height;
        return this;
    }

    public BitmapUnicodeAsset setShouldUseInAtlas(boolean shouldUseInAtlas) {
        this.shouldUseInAtlas = shouldUseInAtlas;
        return this;
    }

    public void setUnicode(int unicode) {
        this.unicode = unicode;
    }

    public void setTexture(String texture) {
        this.texture = texture;
    }

    private void setAtlasData(AtlasData atlasData) {
        this.atlasData = atlasData;
    }

    public File getMinecraftUnicodeFontFolder() {return minecraftUnicodeFontFolder;}
    public File getResourceUnicodeTexturesFolder() {return resourceUnicodeTexturesFolder;}

    @Override
    public void init(ResourcePackBuilder builder) throws IOException {
        //Make the directories
        this.getMinecraftUnicodeFontFolder().mkdirs();
        this.getResourceUnicodeTexturesFolder().mkdirs();

        //Set all assets to initialized.
        List<BitmapUnicodeAsset> assets = builder.getAssetsOf(BitmapUnicodeAsset.class);
        assets.forEach(asset -> asset.setInitialized(true));

        Map<BitmapProperties, List<BitmapUnicodeAsset>> filtered = new LinkedHashMap<>();

        //Stitch together all the custom bitmaps
        //Filter the assets by width, height, and ascent. If all are equal, they should be stitched together.
        for(BitmapUnicodeAsset asset : assets) {
            if(asset.getBitmappedImage() != null) {
                BitmapProperties temp = new BitmapProperties(asset.getBitmappedImage().getWidth(), asset.getHeight(), asset.getAscent(), asset.isUsedInAtlas());

                //If shouldn't be used in an atlas, put it into its own list.
                if(!asset.isUsedInAtlas()) {
                    filtered.put(temp, Collections.singletonList(asset));
                    continue;
                }

                //Add all assets into a respective property.
                BitmapProperties properties = filtered.keySet().stream()
                        .filter(prop -> prop.equals(temp)).findAny().orElse(temp);

                filtered.putIfAbsent(properties, new ArrayList<>());
                filtered.get(properties).add(asset);
            }
        }

        int count = 1;
        for(BitmapProperties properties : filtered.keySet()) {
            //Just copy the Bitmap directly, since it's the only thing in this filter.
            if(filtered.get(properties).size() == 1) {
                BitmapUnicodeAsset asset = filtered.get(properties).get(0);

                //Asset doesn't have a Bitmap, so nothing to copy.
                if(asset.getBitmappedImage() == null) continue;

                ImageIO.write(asset.getBitmappedImage(), "png", new File(this.getResourceUnicodeTexturesFolder() + "/" + asset.getBitmappedFile().getName()));
                asset.setTexture(this.getNamespace() + ":font/" + asset.getBitmappedFile().getName());
            } else {
                //Get the stitched atlas.
                BufferedImage image = stitchBitmaps(filtered.get(properties));

                if(image == null) continue;

                //Set the texture for all the related assets for this property.
                for(BitmapUnicodeAsset asset : filtered.get(properties)) {
                    asset.setTexture(this.getNamespace() + ":font/" + "textures" + count + ".png");
                }

                //Write the texture atlas.
                ImageIO.write(image, "png", new File(this.getResourceUnicodeTexturesFolder() + "/textures" + count + ".png"));
                count++;
            }
        }
    }

    @Override
    public void generate(ResourcePackBuilder builder) throws IOException {
        List<BitmapUnicodeAsset> assets = builder.getAssetsOf(BitmapUnicodeAsset.class);
        assets.forEach(asset -> asset.setGenerated(true));
        JsonObjectBuilder object = new JsonObjectBuilder();

        //The start of our HEX unicode symbols, which is E000.
        int decimalHex = 0xE000;

        //Filter the assets by their texture reference.
        Map<String, List<BitmapUnicodeAsset>> filtered = new HashMap<>();
        for(BitmapUnicodeAsset asset : assets) {
            if(asset.getTexture() == null) continue;

            filtered.putIfAbsent(asset.getTexture(), new ArrayList<>());
            filtered.get(asset.getTexture()).add(asset);
        }

        JsonArrayBuilder array = new JsonArrayBuilder();

        for(String texture : filtered.keySet()) {
            JsonArrayBuilder chars = new JsonArrayBuilder();

            //Row, Unicode String for the chars property.
            Map<Integer, StringBuilder> property = new LinkedHashMap<>();
            for(BitmapUnicodeAsset asset : filtered.get(texture)) {
                // if the unicode value has been manually set
                // then use that value instead
                String unicode = "\\u" + Integer.toHexString(this.unicode != 0 ? this.unicode : decimalHex).toUpperCase(Locale.ROOT);

                if(asset.getAtlasData() == null) {
                    property.put(0, new StringBuilder(unicode));
                } else {
                    property.putIfAbsent(asset.getAtlasData().getRow(), new StringBuilder());
                    property.get(asset.getAtlasData().getRow()).append(unicode);
                }

                //Set the Unicode
                if (this.unicode == 0) {
                    asset.setUnicode(decimalHex++);
                }
            }

            //Make sure all rows have the same number of unicode characters, by appending \u0000
            if(property.size() > 1) {
                int length = property.get(0).length();
                for(Integer i : property.keySet()) {
                    while(property.get(i).length() < length) {
                        property.get(i).append("\\u0000");
                    }
                }
            }

            //Add the Unicode to the chars array
            for(Integer i : property.keySet()) {
                chars.add(property.get(i).toString());
            }

            //Build the array object
            array.add(new JsonObjectBuilder().add("type", "bitmap")
                    .add("file", texture)
                    .add("ascent", filtered.get(texture).get(0).getAscent())
                    .add("height", filtered.get(texture).get(0).getHeight())
                    .add("chars", chars.getArray()).getObject());
        }

        object.add("providers", array.getArray());
        writeWithUnicodeSupport(object.getObject(), new File(this.getMinecraftUnicodeFontFolder() + "/default.json"));
    }

    /**
     * This method assumes that all assets have a non-null value bitmapped image, and that all images have the same width, height, and ascent.
     * @param assets The assets to stitch
     * @return The stitched atlas
     */
    public BufferedImage stitchBitmaps(List<BitmapUnicodeAsset> assets) {
        if(assets.isEmpty()) return null;
        if(assets.get(0).getBitmappedImage().getWidth() > maxPixelsPerAtlas) {
            Bukkit.getLogger().log(Level.SEVERE, Colors.color("&cInvalid bitmap pixel width, single image exceeded maximum atlas width of " + maxPixelsPerAtlas + ". Skipped this bitmap."));
            return null;
        }

        //Calculate the width of the atlas
        int width = 0;
        for(BufferedImage image : assets.stream().map(BitmapUnicodeAsset::getBitmappedImage).toList()) {
            if(width + image.getWidth() > maxPixelsPerAtlas) {
                break;
            }
            width += image.getWidth();
        }

        //Calculate the height of the atlas
        int height = (int) Math.ceil((double) assets.stream().mapToInt(asset -> asset.getBitmappedImage().getWidth()).sum() / maxPixelsPerAtlas) * assets.get(0).getHeight();

        //Create the BufferedImage for the stitch
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = image.getGraphics();
        int row = 0;
        int col = 0;
        int x = 0;
        int y = 0;

        //Draw each of the images in their correct spot.
        for(BitmapUnicodeAsset buffer : assets) {
            graphics.drawImage(buffer.getBitmappedImage(), x, y, null);

            buffer.setAtlasData(new AtlasData(row, col));

            if(x + 2 * buffer.getBitmappedImage().getWidth() >= maxPixelsPerAtlas) {
                y += buffer.getHeight();
                x = 0;
                row++;
                col = 0;
            } else {
                x += buffer.getBitmappedImage().getWidth();
                col++;
            }
        }

        return image;
    }

    /**
     * Writes a JsonObject to a file with Unicode symbols supported.
     * Normally, Unicode symbols will be written as "\\uE000", which is invalid. This method
     * Should write them as "\uE000"
     *
     * @param object The JsonObject to write
     * @param file The file to write to
     */
    private static void writeWithUnicodeSupport(JsonObject object, File file) throws IOException {
        String json = new GsonBuilder().setPrettyPrinting().create().toJson(object);
        FileWriter writer = new FileWriter(file);

        for(String line : json.split("\\n")) {
            writer.write(line.replace("\\\\", "\\") + "\n");
        }
        writer.close();
    }

    private static class AtlasData {
        //Stores the row that this asset will be in within its atlas.
        int row;

        //Stores the column that this asset will be in within its atlas.
        int column;

        AtlasData(int row, int column) {
            this.row = row;
            this.column = column;
        }

        public int getRow() {return row;}
        public int getColumn() {return column;}
    }

    private static class BitmapProperties {
        private final int width;
        private final int height;
        private final int ascent;
        private final boolean useInAtlas;

        BitmapProperties(int width, int height, int ascent, boolean useInAtlas) {
            this.width = width;
            this.height = height;
            this.ascent = ascent;
            this.useInAtlas = useInAtlas;
        }

        public int getWidth() {return width;}
        public int getAscent() {return ascent;}
        public int getHeight() {return height;}
        public boolean isUseInAtlas() {return useInAtlas;}

        public boolean equals(BitmapProperties properties) {
            if(!properties.isUseInAtlas() || !this.isUseInAtlas()) return false;

            return this.getWidth() == properties.getWidth() && this.getHeight() == properties.getHeight() && this.getAscent() == properties.getAscent();
        }
    }
}
