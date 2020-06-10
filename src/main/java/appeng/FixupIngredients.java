package appeng;

import appeng.api.util.AEColor;
import appeng.core.AppEng;
import appeng.items.materials.MaterialType;
import appeng.items.parts.PartType;
import com.google.gson.*;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;

public class FixupIngredients {

    private static Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static JsonObject visitObjProps(JsonObject obj) {
        for (Map.Entry<String, JsonElement> e : obj.entrySet()) {
            e.setValue(visitAndReplace(e.getValue()));
        }
        return obj;
    }

    private static JsonElement visitAndReplace(JsonElement el) {
        if (el.isJsonArray()) {
            JsonArray arr = el.getAsJsonArray();
            for (int i = 0; i < arr.size(); i++) {
                arr.set(i, visitAndReplace(arr.get(i)));
            }
            return arr;
        }
        if (!el.isJsonObject()) {
            return el;
        }

        JsonObject obj = el.getAsJsonObject();
        if (obj.size() != 2 && (obj.size() != 3 || !obj.has("count"))) {
            return visitObjProps(obj);
        }
        Integer count = null;
        if (obj.has("count")) {
            count = obj.get("count").getAsInt();
        }

        JsonPrimitive type = obj.getAsJsonPrimitive("type");

        if (type == null) {
            return visitObjProps(obj);
        }

        if ("forge:ore_dict".equals(type.getAsString())) {
            String ore = obj.get("ore").getAsString();
            JsonObject r = new JsonObject();
            r.add("tag", new JsonPrimitive(AppEng.MOD_ID + ':' + "ore_" + ore));
            return r;
        }

        if (!type.getAsString().equals("appliedenergistics2:part"))  {
            return visitObjProps(obj);
        }
        String part = obj.getAsJsonPrimitive("part").getAsString();

        String itemName = null;
        if (part.startsWith("material.")) {
            String mtName = part.substring(9).toUpperCase();
            if ("WIRELESS".equals(mtName)) {
                mtName = "WIRELESS_RECEIVER";
            }

            itemName = mtName.toLowerCase();
        } else if (part.startsWith("part.")) {
            part = part.substring(5);

            if ("fluid_interface".equals(part)) {
                part = "cable_fluid_interface";
            } else if ("interface".equals(part)) {
                    part = "cable_interface";
            }
        }

        if (itemName == null) {
            // Handle tags
            String tagName = null;
            if ( part.equalsIgnoreCase("cable_glass")) {
                tagName = "glass_cable";
            } else if (part.equalsIgnoreCase("cable_covered")) {
                tagName = "covered_cable";
            } else if (part.equalsIgnoreCase("cable_smart")) {
                tagName = "smart_cable";
            } else if (part.equalsIgnoreCase("cable_dense_covered")) {
                tagName = "covered_dense_cable";
            } else if (part.equalsIgnoreCase("cable_dense_smart")) {
                tagName = "smart_dense_cable";
            }
            if (tagName != null) {
                JsonObject r = new JsonObject();
                r.add("tag", new JsonPrimitive(AppEng.MOD_ID + ':' + tagName));
                return r;
            }
            itemName = part.toLowerCase();
        }

        for (AEColor c : AEColor.values()) {
            String colorSuffix = '.' + c.registryPrefix;
            if (itemName.endsWith(colorSuffix)) {
                String p = itemName.substring(0, itemName.length() - colorSuffix.length());
                itemName = c.registryPrefix + "_" + p;
                break;
            }
        }

        if (itemName.startsWith("p2p_tunnel_")) {
            itemName = itemName.substring("p2p_tunnel_".length()) + "_p2p_tunnel";
        }

        JsonObject r = new JsonObject();
        r.add("item", new JsonPrimitive(AppEng.MOD_ID + ':' + itemName));
        if (count != null) {
            r.add("count", new JsonPrimitive(count));
        }
        return r;

    }

    public static void main(String[] args) throws IOException {

        Path p= Paths.get("D:\\Applied-Energistics-2\\src\\main\\resources\\data\\appliedenergistics2");

        Files.walkFileTree(p, new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (!file.getFileName().toString().endsWith(".json")) {
                    return FileVisitResult.CONTINUE;
                }
                if (file.getFileName().toString().contains("_constants")) {
                    return FileVisitResult.CONTINUE;
                }
                if (file.getFileName().toString().contains("_factories")) {
                    return FileVisitResult.CONTINUE;
                }

                JsonElement el;
                try (Reader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                     el = visitAndReplace(gson.fromJson(r, JsonElement.class));
                } catch (Exception e) {
                    System.err.println("Failed to process file " + file);
                    e.printStackTrace();
                    return FileVisitResult.CONTINUE;
                }

                try (Writer w = Files.newBufferedWriter(file, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                    gson.toJson(el, w);
                }

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });

    }

}
