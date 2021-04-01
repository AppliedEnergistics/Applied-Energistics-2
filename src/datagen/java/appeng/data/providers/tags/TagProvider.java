package appeng.data.providers.tags;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.block.Block;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

import appeng.core.AppEng;
import appeng.data.providers.IAE2DataProvider;

public abstract class TagProvider implements IAE2DataProvider {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    protected static final String TYPE_ITEMS = "items";
    protected static final String TYPE_BLOCKS = "blocks";

    private final String defaultNamespace;

    private final Path outputPath;

    private DirectoryCache cache;

    protected TagProvider(String defaultNamespace, Path outputPath) {
        this.defaultNamespace = defaultNamespace;
        this.outputPath = outputPath;
    }

    @Override
    public void act(DirectoryCache cache) throws IOException {
        this.cache = cache;
        try {
            generate();
        } finally {
            this.cache = null;
        }
    }

    protected abstract void generate() throws IOException;

    protected void addItemTag(String name, IItemProvider... items) throws IOException {
        ResourceLocation id = parseId(name);

        List<String> itemIds = Arrays.stream(items).map(IItemProvider::asItem).map(Registry.ITEM::getKey)
                .map(ResourceLocation::toString).collect(Collectors.toList());
        writeTagFile(id.getNamespace(), TYPE_ITEMS, id.getPath(), itemIds);
    }

    protected void addBlockTag(String name, Block... blocks) throws IOException {
        ResourceLocation id = parseId(name);

        List<String> itemIds = Arrays.stream(blocks).map(Registry.BLOCK::getKey).map(ResourceLocation::toString)
                .collect(Collectors.toList());
        writeTagFile(id.getNamespace(), TYPE_BLOCKS, id.getPath(), itemIds);
    }

    protected void writeTagFile(String namespace, String tagType, String tagName, List<String> entries)
            throws IOException {
        JsonObject rootObj = new JsonObject();
        JsonArray valuesArr = new JsonArray();
        for (String entry : entries) {
            valuesArr.add(entry);
        }
        rootObj.add("values", valuesArr);

        Path path = outputPath.resolve("data/" + namespace + "/tags/" + tagType + "/" + tagName + ".json");
        IDataProvider.save(GSON, this.cache, rootObj, path);
    }

    @Override
    public String getName() {
        return AppEng.MOD_NAME + " Tags (" + defaultNamespace + ")";
    }

    private ResourceLocation parseId(String name) {
        if (name.contains(":")) {
            return new ResourceLocation(name);
        } else {
            return new ResourceLocation(defaultNamespace, name);
        }
    }

}
