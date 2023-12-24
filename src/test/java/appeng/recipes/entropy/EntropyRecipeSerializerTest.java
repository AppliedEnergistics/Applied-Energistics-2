package appeng.recipes.entropy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;

import org.junit.jupiter.api.Test;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Blocks;

class EntropyRecipeSerializerTest {
    @Test
    void testCodecEncodeMinimal() {
        var recipe = EntropyRecipeBuilder.cool()
                .setInputBlock(Blocks.GRASS_BLOCK)
                .setOutputBlock(Blocks.DIRT)
                .build();

        var recipeJson = Recipe.CODEC.encodeStart(JsonOps.INSTANCE, recipe).get().left().get();
        assertEquals("""
                {"mode":"cool","input":{"block":{"id":"minecraft:grass_block"}},"output":{"block":{"id":"minecraft:dirt"}},"type":"ae2:entropy"}
                """.trim(), recipeJson.toString());
    }

    @Test
    void testCodecDecodeMinimal() {
        var jsonEl = new Gson().fromJson("""
                {"mode":"cool","input":{"block":{"id":"minecraft:grass_block"}},"output":{"block":{"id":"minecraft:dirt"}},"type":"ae2:entropy"}
                """, JsonElement.class);

        var recipe = (EntropyRecipe) Recipe.CODEC.decode(JsonOps.INSTANCE, jsonEl).getOrThrow(false, s -> {
            throw new RuntimeException(s);
        }).getFirst();
        assertEquals(EntropyMode.COOL, recipe.getMode());
        assertEquals(Optional.of(Blocks.GRASS_BLOCK), recipe.getInput().block().map(EntropyRecipe.BlockInput::block));
        assertEquals(Optional.of(Blocks.DIRT), recipe.getOutput().block().map(EntropyRecipe.BlockOutput::block));
    }
}
