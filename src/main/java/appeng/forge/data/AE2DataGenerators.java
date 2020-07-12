package appeng.forge.data;

import appeng.forge.data.providers.loot.BlockDropProvider;
import appeng.forge.data.providers.recipes.SlabStairRecipes;
import appeng.forge.data.providers.tags.ConventionTagProvider;
import net.minecraft.data.DataGenerator;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

public class AE2DataGenerators {

    public static void dump() {
        Path output = Paths.get("../src/generated/resources");

        DataGenerator generator = new DataGenerator(output, Collections.emptyList());
        generator.install(new BlockDropProvider(output));
        generator.install(new SlabStairRecipes(output));
        generator.install(new ConventionTagProvider(output));
        try {
            generator.run();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
