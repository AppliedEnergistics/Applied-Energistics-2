package appeng.client.guidebook.compiler;

import java.util.Map;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;

public record Frontmatter(
        @Nullable FrontmatterNavigation navigationEntry,
        Map<String, Object> additionalProperties) {
    public static Frontmatter parse(ResourceLocation pageId, String yamlText) {
        var yaml = new Yaml(new SafeConstructor(new LoaderOptions()));

        FrontmatterNavigation navigation = null;
        Map<String, Object> data = yaml.load(yamlText);
        var navigationObj = data.remove("navigation");
        if (navigationObj != null) {
            if (!(navigationObj instanceof Map<?, ?>navigationMap)) {
                throw new IllegalArgumentException("The navigation key in the frontmatter has to be a map");
            }

            var title = getString(navigationMap, "title");
            if (title == null) {
                throw new IllegalArgumentException("title is missing in navigation frontmatter");
            }
            var parentIdStr = getString(navigationMap, "parent");
            var position = 0;
            if (navigationMap.containsKey("position")) {
                position = getInt(navigationMap, "position");
            }
            var iconIdStr = getString(navigationMap, "icon");
            CompoundTag iconNbt = getCompound(navigationMap, "icon_nbt");

            ResourceLocation parentId = null;
            if (parentIdStr != null) {
                parentId = IdUtils.resolveId(parentIdStr, pageId.getNamespace());
            }

            ResourceLocation iconId = null;
            if (iconIdStr != null) {
                iconId = IdUtils.resolveId(iconIdStr, pageId.getNamespace());
            }

            navigation = new FrontmatterNavigation(title, parentId, position, iconId, iconNbt);
        }

        return new Frontmatter(
                navigation,
                Map.copyOf(data));
    }

    @Nullable
    private static String getString(Map<?, ?> map, String key) {
        var value = map.get(key);
        if (value != null && !(value instanceof String)) {
            throw new IllegalArgumentException("Key " + key + " has to be a String!");
        }
        return (String) value;
    }

    private static int getInt(Map<?, ?> map, String key) {
        var value = map.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Key " + key + " is missing in navigation frontmatter");
        }
        if (!(value instanceof Number number)) {
            throw new IllegalArgumentException("Key " + key + " has to be a number!");
        }
        return number.intValue();
    }

    @Nullable
    private static CompoundTag getCompound(Map<?, ?> map, String key) {
        var value = map.get(key);
        if (value == null) {
            return null;
        }
        if (!(value instanceof String string)) {
            throw new IllegalArgumentException("Key " + key + " has to be a string (SNBT format)!");
        }
        try {
            return TagParser.parseTag(string);
        } catch (CommandSyntaxException e) {
            throw new IllegalArgumentException("Key " + key + " is not a valid SNBT string: " + string, e);
        }
    }
}
