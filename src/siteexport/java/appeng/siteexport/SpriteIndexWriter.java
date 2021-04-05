package appeng.siteexport;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.MinecraftVersion;
import net.minecraft.util.registry.Registry;

public class SpriteIndexWriter {

    private final SpriteIndexJson spriteIndex = new SpriteIndexJson();

    public SpriteIndexWriter(int width, int height, int rows, int cols, int margin) {
        spriteIndex.modVersion = ModVersion.get();
        spriteIndex.width = width;
        spriteIndex.height = height;
        spriteIndex.cols = cols;
        spriteIndex.rows = rows;
        spriteIndex.margin = margin;
    }

    public void add(ItemStack stack, int x, int y) {
        SpriteIndexEntryJson entry = new SpriteIndexEntryJson(stack, x, y);
        spriteIndex.sprites.add(entry);
    }

    public void write(Path file) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            gson.toJson(spriteIndex, writer);
        }
    }

    private static class SpriteIndexJson {

        private String generated;

        private String gameVersion;

        private String modVersion;

        private String filename;

        private int width;

        private int height;

        private int cols;

        private int rows;

        private int margin;

        private List<SpriteIndexEntryJson> sprites = new ArrayList<>();

        public SpriteIndexJson() {
            generated = Instant.now().toString();
            gameVersion = MinecraftVersion.load().getName();
        }

        public String getGenerated() {
            return generated;
        }

        public void setGenerated(String generated) {
            this.generated = generated;
        }

        public String getGameVersion() {
            return gameVersion;
        }

        public void setGameVersion(String gameVersion) {
            this.gameVersion = gameVersion;
        }

        public String getModVersion() {
            return modVersion;
        }

        public void setModVersion(String modVersion) {
            this.modVersion = modVersion;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getCols() {
            return cols;
        }

        public void setCols(int cols) {
            this.cols = cols;
        }

        public int getRows() {
            return rows;
        }

        public void setRows(int rows) {
            this.rows = rows;
        }

        public List<SpriteIndexEntryJson> getSprites() {
            return sprites;
        }

        public int getMargin() {
            return margin;
        }

        public void setMargin(int margin) {
            this.margin = margin;
        }
    }

    private static class SpriteIndexEntryJson {
        private String itemId;
        private JsonElement tag;
        private int x;
        private int y;

        public SpriteIndexEntryJson(ItemStack is, int x, int y) {
            this.itemId = Registry.ITEM.getKey(is.getItem()).toString();
            if (is.hasTag()) {
                this.tag = NBTDynamicOps.INSTANCE.convertMap(JsonOps.INSTANCE, is.getTag());
            }
            this.x = x;
            this.y = y;
        }

        public String getItemId() {
            return itemId;
        }

        public JsonElement getTag() {
            return tag;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

    }

}
