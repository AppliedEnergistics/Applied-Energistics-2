package appeng.client.guidebook.compiler.tags;

import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import appeng.client.guidebook.color.ColorValue;
import appeng.client.guidebook.color.ConstantColor;
import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.document.LytErrorSink;
import appeng.libs.mdast.mdx.model.MdxJsxAttribute;
import appeng.libs.mdast.mdx.model.MdxJsxElementFields;

/**
 * utilities for dealing with attributes of {@link MdxJsxElementFields}.
 */
public final class MdxAttrs {

    private static final Pattern COLOR_PATTERN = Pattern.compile("^#([0-9a-fA-F]{2}){3,4}$");

    private MdxAttrs() {
    }

    @Nullable
    public static ResourceLocation getRequiredId(PageCompiler compiler, LytErrorSink errorSink, MdxJsxElementFields el,
            String attribute) {
        var id = el.getAttributeString(attribute, null);
        if (id == null) {
            errorSink.appendError(compiler, "Missing " + attribute + " attribute.", el);
            return null;
        }

        id = id.trim(); // Trim leading/trailing whitespace for easier use

        ResourceLocation itemId;
        try {
            return compiler.resolveId(id);
        } catch (ResourceLocationException e) {
            errorSink.appendError(compiler, "Malformed id " + id + ": " + e.getMessage(), el);
            return null;
        }
    }

    @Nullable
    public static Pair<ResourceLocation, Block> getRequiredBlockAndId(PageCompiler compiler, LytErrorSink errorSink,
            MdxJsxElementFields el, String attribute) {
        var itemId = getRequiredId(compiler, errorSink, el, attribute);

        var resultItem = BuiltInRegistries.BLOCK.getOptional(itemId).orElse(null);
        if (resultItem == null) {
            errorSink.appendError(compiler, "Missing block: " + itemId, el);
            return null;
        }
        return Pair.of(itemId, resultItem);
    }

    @Nullable
    public static Pair<ResourceLocation, Item> getRequiredItemAndId(PageCompiler compiler, LytErrorSink errorSink,
            MdxJsxElementFields el, String attribute) {
        var itemId = getRequiredId(compiler, errorSink, el, attribute);

        var resultItem = BuiltInRegistries.ITEM.getOptional(itemId).orElse(null);
        if (resultItem == null) {
            errorSink.appendError(compiler, "Missing item: " + itemId, el);
            return null;
        }
        return Pair.of(itemId, resultItem);
    }

    public static Item getRequiredItem(PageCompiler compiler, LytErrorSink errorSink, MdxJsxElementFields el,
            String attribute) {
        var result = getRequiredItemAndId(compiler, errorSink, el, attribute);
        if (result != null) {
            return result.getRight();
        }
        return null;
    }

    public static float getFloat(PageCompiler compiler, LytErrorSink errorSink, MdxJsxElementFields el, String name,
            float defaultValue) {
        var attrValue = el.getAttributeString(name, null);
        if (attrValue == null) {
            return defaultValue;
        }

        try {
            return Float.parseFloat(attrValue);
        } catch (NumberFormatException e) {
            errorSink.appendError(compiler, "Malformed floating point value: '" + attrValue + "'", el);
            return defaultValue;
        }
    }

    public static int getInt(PageCompiler compiler, LytErrorSink errorSink, MdxJsxElementFields el, String name,
            int defaultValue) {
        var attrValue = el.getAttributeString(name, null);
        if (attrValue == null) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(attrValue);
        } catch (NumberFormatException e) {
            errorSink.appendError(compiler, "Malformed integer value: '" + attrValue + "'", el);
            return defaultValue;
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T extends Enum<T> & StringRepresentable> T getEnum(PageCompiler compiler, LytErrorSink errorSink,
            MdxJsxElementFields el, String name, T defaultValue) {

        var stringValue = el.getAttributeString(name, defaultValue.getSerializedName());

        var clazz = (Class<T>) defaultValue.getClass();
        for (var constant : clazz.getEnumConstants()) {
            if (constant.getSerializedName().equals(stringValue)) {
                return constant;
            }
        }

        errorSink.appendError(compiler, "Unrecognized option for attribute " + name + ": " + stringValue, el);
        return null;
    }

    public static BlockState applyBlockStateProperties(PageCompiler compiler, LytErrorSink errorSink,
            MdxJsxElementFields el, BlockState state) {
        for (var attrNode : el.attributes()) {
            if (!(attrNode instanceof MdxJsxAttribute attr)) {
                continue;
            }
            var attrName = attr.name;
            if (!attrName.startsWith("p:")) {
                continue;
            }
            var statePropertyName = attrName.substring("p:".length());
            var stateDefinition = state.getBlock().getStateDefinition();
            var property = stateDefinition.getProperty(statePropertyName);
            if (property == null) {
                errorSink.appendError(compiler, "block doesn't have property " + statePropertyName, el);
                continue;
            }
            state = applyProperty(compiler, errorSink, el, state, property, attr.getStringValue());
        }
        return state;
    }

    private static <T extends Comparable<T>> BlockState applyProperty(PageCompiler compiler,
            LytErrorSink errorSink,
            MdxJsxElementFields el,
            BlockState state,
            Property<T> property,
            String stringValue) {
        var propertyValue = property.getValue(stringValue);
        if (propertyValue.isEmpty()) {
            errorSink.appendError(compiler, "Invalid value  for property " + property + ": " + stringValue, el);
            return state;
        }

        return state.setValue(property, propertyValue.get());
    }

    public static BlockPos getPos(PageCompiler compiler, LytErrorSink errorSink, MdxJsxElementFields el) {
        var x = getInt(compiler, errorSink, el, "x", 0);
        var y = getInt(compiler, errorSink, el, "y", 0);
        var z = getInt(compiler, errorSink, el, "z", 0);
        return new BlockPos(x, y, z);
    }

    public static ColorValue getColor(PageCompiler compiler, LytErrorSink errorSink, MdxJsxElementFields el,
            String name, ColorValue defaultColor) {
        var colorStr = el.getAttributeString(name, null);
        if (colorStr != null) {
            var m = COLOR_PATTERN.matcher(colorStr);
            if (!m.matches()) {
                errorSink.appendError(compiler, "Color must have format #AARRGGBB", el);
                return defaultColor;
            }

            int r, g, b;
            int a = 255;
            if (colorStr.length() == 7) {
                r = Integer.valueOf(colorStr.substring(1, 3), 16);
                g = Integer.valueOf(colorStr.substring(3, 5), 16);
                b = Integer.valueOf(colorStr.substring(5, 7), 16);
            } else {
                a = Integer.valueOf(colorStr.substring(1, 3), 16);
                r = Integer.valueOf(colorStr.substring(3, 5), 16);
                g = Integer.valueOf(colorStr.substring(5, 7), 16);
                b = Integer.valueOf(colorStr.substring(7, 9), 16);
            }
            return new ConstantColor(FastColor.ARGB32.color(a, r, g, b));
        }

        return defaultColor;
    }

}
