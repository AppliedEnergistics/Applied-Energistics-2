package appeng.data;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.minecraft.Bootstrap;
import net.minecraft.data.DataGenerator;

import appeng.core.AEConfig;
import appeng.core.Api;
import appeng.core.CreativeTab;
import appeng.data.providers.loot.BlockDropProvider;
import appeng.data.providers.recipes.SlabStairRecipes;
import appeng.data.providers.tags.ConventionTagProvider;

public class Entrypoint implements PreLaunchEntrypoint {

    public static void dump() throws Exception {
        Path output = Paths.get("../src/generated/resources");

        DataGenerator generator = new DataGenerator(output, Collections.emptyList());
        generator.install(new BlockDropProvider(output));
        generator.install(new SlabStairRecipes(output));
        generator.install(new ConventionTagProvider(output));
        generator.run();
    }

    @Override
    public void onPreLaunch() {
        Bootstrap.initialize();
        AEConfig.load(new File("config"));
        CreativeTab.init(); // Vanilla data gens use item tab associations
        Api.INSTANCE = new Api();

        try {
            dump();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        System.exit(0);

    }

}
