package appeng.recipes.entropy;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EntropyRecipeSerializerTest {
    @Test
    void testCodecEncodeMinimal() {
        var recipe = EntropyRecipeBuilder.cool()
                .setInputBlock(Blocks.GRASS_BLOCK)
                .setOutputBlock(Blocks.DIRT)
                .build();

        var recipeJson = Recipe.CODEC.encodeStart(JsonOps.INSTANCE, recipe).get().left().get();
        assertEquals("""
                {"outputBlock":"minecraft:dirt","mode":"cool","inputBlock":"minecraft:grass_block","type":"ae2:entropy"}
                """.trim(), recipeJson.toString());
    }

    @Test
    void testCodecDecodeMinimal() {
        var jsonEl = new Gson().fromJson("""
                {"outputBlock":"minecraft:dirt","mode":"cool","inputBlock":"minecraft:grass_block","type":"ae2:entropy"}
                """, JsonElement.class);

        var recipe = (EntropyRecipe) Recipe.CODEC.decode(JsonOps.INSTANCE, jsonEl).getOrThrow(false, s -> {
            throw new RuntimeException(s);
        }).getFirst();
        assertEquals(EntropyMode.COOL, recipe.getMode());
        assertEquals(Optional.of(Blocks.GRASS_BLOCK), recipe.getInputBlock());
        assertEquals(Optional.of(Blocks.DIRT), recipe.getOutputBlock());
    }
}