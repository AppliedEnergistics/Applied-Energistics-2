package appeng;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.ItemDefinition;
import appeng.init.InitItems;
import appeng.util.BootstrapMinecraft;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.Registry;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

@BootstrapMinecraft
public class ConvertItemModels {

    @Test
    public void convert() throws Exception {
        var gson = new Gson();

        var p = Paths.get("C:\\AE2\\Fabric\\src\\main\\resources\\assets\\ae2\\models\\block");
        var files = Files.walk(p, 99).toList();
        for (Path file : files) {
            if (Files.isDirectory(file)) {
                continue;
            }

            var id = AppEng.makeId(p.relativize(file).toString().replace('\\', '/').replace(".json", ""));

            var root = (JsonObject) gson.fromJson(Files.readString(file, StandardCharsets.UTF_8), JsonElement.class);
            if (root.keySet().equals(Set.of("parent", "textures"))) {
                var parent = root.getAsJsonPrimitive("parent").getAsString();
                if (!parent.equals("block/cube_all")) {
                    continue;
                }

                var textures = (JsonObject) root.get("textures");
                if (textures.keySet().equals(Set.of("all"))) {

                    for (var field : AEBlocks.class.getDeclaredFields()) {
                        field.setAccessible(true);
                        if (field.get(null) instanceof ItemDefinition itemDefinition) {
                            if (!itemDefinition.id().equals(id)) {
                                continue;
                            }

                            if (textures.get("all").getAsString().equals(id.toString().replace("ae2:", "ae2:block/"))) {

                                System.out.println("cubeAll(AEBlocks." + field.getName() + ");");
                            }

                        }
                    }
                }
            }
        }
    }
}
