package appeng.data.providers.tags;

import java.io.IOException;
import java.nio.file.Path;

import appeng.core.AppEng;

public class ToolTagProviders extends TagProvider {

    public ToolTagProviders(Path outputPath) {
        super("fabric", outputPath);
    }

    @Override
    protected void generate() throws IOException {
        addItemTag("axes", ITEMS.netherQuartzAxe(), ITEMS.certusQuartzAxe());
        addItemTag("hoes", ITEMS.netherQuartzHoe(), ITEMS.certusQuartzHoe());
        addItemTag("pickaxes", ITEMS.netherQuartzPick(), ITEMS.certusQuartzPick());
        addItemTag("shovels", ITEMS.netherQuartzShovel(), ITEMS.certusQuartzShovel());
        addItemTag("swords", ITEMS.netherQuartzSword(), ITEMS.certusQuartzSword());
    }

    @Override
    public String getName() {
        return AppEng.MOD_NAME + " Tool Tags";
    }

}
