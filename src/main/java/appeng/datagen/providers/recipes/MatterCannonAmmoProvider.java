package appeng.datagen.providers.recipes;

import java.util.function.Consumer;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.ItemLike;

import appeng.core.AppEng;
import appeng.core.definitions.AEItems;

public class MatterCannonAmmoProvider extends RecipeProvider {
    public MatterCannonAmmoProvider(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer) {
        registerAmmoTag(consumer, "nuggets/meatraw", new ResourceLocation("forge:nuggets/meatraw"), 32);
        registerAmmoTag(consumer, "nuggets/meatcooked", new ResourceLocation("forge:nuggets/meatcooked"), 32);
        registerAmmoTag(consumer, "nuggets/meat", new ResourceLocation("forge:nuggets/meat"), 32);
        registerAmmoTag(consumer, "nuggets/chicken", new ResourceLocation("forge:nuggets/chicken"), 32);
        registerAmmoTag(consumer, "nuggets/beef", new ResourceLocation("forge:nuggets/beef"), 32);
        registerAmmoTag(consumer, "nuggets/sheep", new ResourceLocation("forge:nuggets/sheep"), 32);
        registerAmmoTag(consumer, "nuggets/fish", new ResourceLocation("forge:nuggets/fish"), 32);

        // real level...
        registerAmmoTag(consumer, "nuggets/lithium", new ResourceLocation("forge:nuggets/lithium"), 6.941f);
        registerAmmoTag(consumer, "nuggets/beryllium", new ResourceLocation("forge:nuggets/beryllium"), 9.0122f);
        registerAmmoTag(consumer, "nuggets/boron", new ResourceLocation("forge:nuggets/boron"), 10.811f);
        registerAmmoTag(consumer, "nuggets/carbon", new ResourceLocation("forge:nuggets/carbon"), 12.0107f);
        registerAmmoTag(consumer, "nuggets/coal", new ResourceLocation("forge:nuggets/coal"), 12.0107f);
        registerAmmoTag(consumer, "nuggets/charcoal", new ResourceLocation("forge:nuggets/charcoal"), 12.0107f);
        registerAmmoTag(consumer, "nuggets/sodium", new ResourceLocation("forge:nuggets/sodium"), 22.9897f);
        registerAmmoTag(consumer, "nuggets/magnesium", new ResourceLocation("forge:nuggets/magnesium"), 24.305f);
        registerAmmoTag(consumer, "nuggets/aluminum", new ResourceLocation("forge:nuggets/aluminum"), 26.9815f);
        registerAmmoTag(consumer, "nuggets/silicon", new ResourceLocation("forge:nuggets/silicon"), 28.0855f);
        registerAmmoTag(consumer, "nuggets/phosphorus", new ResourceLocation("forge:nuggets/phosphorus"), 30.9738f);
        registerAmmoTag(consumer, "nuggets/sulfur", new ResourceLocation("forge:nuggets/sulfur"), 32.065f);
        registerAmmoTag(consumer, "nuggets/potassium", new ResourceLocation("forge:nuggets/potassium"), 39.0983f);
        registerAmmoTag(consumer, "nuggets/calcium", new ResourceLocation("forge:nuggets/calcium"), 40.078f);
        registerAmmoTag(consumer, "nuggets/scandium", new ResourceLocation("forge:nuggets/scandium"), 44.9559f);
        registerAmmoTag(consumer, "nuggets/titanium", new ResourceLocation("forge:nuggets/titanium"), 47.867f);
        registerAmmoTag(consumer, "nuggets/vanadium", new ResourceLocation("forge:nuggets/vanadium"), 50.9415f);
        registerAmmoTag(consumer, "nuggets/manganese", new ResourceLocation("forge:nuggets/manganese"), 54.938f);
        registerAmmoTag(consumer, "nuggets/iron", new ResourceLocation("forge:nuggets/iron"), 55.845f);
        registerAmmoTag(consumer, "nuggets/gold", new ResourceLocation("forge:nuggets/gold"), 196.96655f);
        registerAmmoTag(consumer, "nuggets/nickel", new ResourceLocation("forge:nuggets/nickel"), 58.6934f);
        registerAmmoTag(consumer, "nuggets/cobalt", new ResourceLocation("forge:nuggets/cobalt"), 58.9332f);
        registerAmmoTag(consumer, "nuggets/copper", new ResourceLocation("forge:nuggets/copper"), 63.546f);
        registerAmmoTag(consumer, "nuggets/zinc", new ResourceLocation("forge:nuggets/zinc"), 65.39f);
        registerAmmoTag(consumer, "nuggets/gallium", new ResourceLocation("forge:nuggets/gallium"), 69.723f);
        registerAmmoTag(consumer, "nuggets/germanium", new ResourceLocation("forge:nuggets/germanium"), 72.64f);
        registerAmmoTag(consumer, "nuggets/bromine", new ResourceLocation("forge:nuggets/bromine"), 79.904f);
        registerAmmoTag(consumer, "nuggets/krypton", new ResourceLocation("forge:nuggets/krypton"), 83.8f);
        registerAmmoTag(consumer, "nuggets/rubidium", new ResourceLocation("forge:nuggets/rubidium"), 85.4678f);
        registerAmmoTag(consumer, "nuggets/strontium", new ResourceLocation("forge:nuggets/strontium"), 87.62f);
        registerAmmoTag(consumer, "nuggets/yttrium", new ResourceLocation("forge:nuggets/yttrium"), 88.9059f);
        registerAmmoTag(consumer, "nuggets/zirconium", new ResourceLocation("forge:nuggets/zirconium"), 91.224f);
        registerAmmoTag(consumer, "nuggets/niobium", new ResourceLocation("forge:nuggets/niobium"), 92.9064f);
        registerAmmoTag(consumer, "nuggets/technetium", new ResourceLocation("forge:nuggets/technetium"), 98f);
        registerAmmoTag(consumer, "nuggets/ruthenium", new ResourceLocation("forge:nuggets/ruthenium"), 101.07f);
        registerAmmoTag(consumer, "nuggets/rhodium", new ResourceLocation("forge:nuggets/rhodium"), 102.9055f);
        registerAmmoTag(consumer, "nuggets/palladium", new ResourceLocation("forge:nuggets/palladium"), 106.42f);
        registerAmmoTag(consumer, "nuggets/silver", new ResourceLocation("forge:nuggets/silver"), 107.8682f);
        registerAmmoTag(consumer, "nuggets/cadmium", new ResourceLocation("forge:nuggets/cadmium"), 112.411f);
        registerAmmoTag(consumer, "nuggets/indium", new ResourceLocation("forge:nuggets/indium"), 114.818f);
        registerAmmoTag(consumer, "nuggets/tin", new ResourceLocation("forge:nuggets/tin"), 118.71f);
        registerAmmoTag(consumer, "nuggets/antimony", new ResourceLocation("forge:nuggets/antimony"), 121.76f);
        registerAmmoTag(consumer, "nuggets/iodine", new ResourceLocation("forge:nuggets/iodine"), 126.9045f);
        registerAmmoTag(consumer, "nuggets/tellurium", new ResourceLocation("forge:nuggets/tellurium"), 127.6f);
        registerAmmoTag(consumer, "nuggets/xenon", new ResourceLocation("forge:nuggets/xenon"), 131.293f);
        registerAmmoTag(consumer, "nuggets/cesium", new ResourceLocation("forge:nuggets/cesium"), 132.9055f);
        registerAmmoTag(consumer, "nuggets/barium", new ResourceLocation("forge:nuggets/barium"), 137.327f);
        registerAmmoTag(consumer, "nuggets/lanthanum", new ResourceLocation("forge:nuggets/lanthanum"), 138.9055f);
        registerAmmoTag(consumer, "nuggets/cerium", new ResourceLocation("forge:nuggets/cerium"), 140.116f);
        registerAmmoTag(consumer, "nuggets/tantalum", new ResourceLocation("forge:nuggets/tantalum"), 180.9479f);
        registerAmmoTag(consumer, "nuggets/tungsten", new ResourceLocation("forge:nuggets/tungsten"), 183.84f);
        registerAmmoTag(consumer, "nuggets/osmium", new ResourceLocation("forge:nuggets/osmium"), 190.23f);
        registerAmmoTag(consumer, "nuggets/iridium", new ResourceLocation("forge:nuggets/iridium"), 192.217f);
        registerAmmoTag(consumer, "nuggets/platinum", new ResourceLocation("forge:nuggets/platinum"), 195.078f);
        registerAmmoTag(consumer, "nuggets/lead", new ResourceLocation("forge:nuggets/lead"), 207.2f);
        registerAmmoTag(consumer, "nuggets/bismuth", new ResourceLocation("forge:nuggets/bismuth"), 208.9804f);
        registerAmmoTag(consumer, "nuggets/uranium", new ResourceLocation("forge:nuggets/uranium"), 238.0289f);
        registerAmmoTag(consumer, "nuggets/plutonium", new ResourceLocation("forge:nuggets/plutonium"), 244);

        // TE stuff...
        registerAmmoTag(consumer, "nuggets/invar", new ResourceLocation("forge:nuggets/invar"),
                (58.6934f + 55.845f + 55.845f) / 3.0f);
        registerAmmoTag(consumer, "nuggets/electrum", new ResourceLocation("forge:nuggets/electrum"),
                (107.8682f + 196.96655f) / 2.0f);

        registerAmmoItem(consumer, "matter_ball", AEItems.MATTER_BALL, 32.0f);
    }

    private void registerAmmoItem(Consumer<FinishedRecipe> consumer, String id, ItemLike item, float weight) {
        consumer.accept(new MatterCannonAmmo(
                AppEng.makeId("matter_cannon/" + id),
                null,
                item.asItem(),
                weight));
    }

    private void registerAmmoTag(Consumer<FinishedRecipe> consumer, String id, ResourceLocation tag, float weight) {
        consumer.accept(new MatterCannonAmmo(
                AppEng.makeId("matter_cannon/" + id),
                ItemTags.createOptional(tag),
                null,
                weight));
    }
}
