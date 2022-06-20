package appeng.recipes.handlers;

import appeng.core.AppEng;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.Objects;
import java.util.Set;

public final class TransformRecipe implements Recipe<Container>{
    public static final ResourceLocation TYPE_ID = AppEng.makeId("transform");
    public static final RecipeType<TransformRecipe> TYPE = RecipeType.register(TYPE_ID.toString());

    private final ResourceLocation id;
    public final Set<Item> additionalItems;
    public final Item output;
    public final int count;

    public TransformRecipe(ResourceLocation id, Set<Item> additionalItems, Item output, int count){
        this.id = id;
        this.additionalItems = additionalItems;
        this.output = output;
        this.count = count;
    }

    @Override
    public boolean matches(Container container, Level level){
        return false;
    }

    @Override
    public ItemStack assemble(Container container){
        return null;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height){
        return false;
    }

    @Override
    public ItemStack getResultItem(){
        return new ItemStack(output, count);
    }

    @Override
    public ResourceLocation getId(){
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer(){
        return TransformRecipeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType(){
        return TYPE;
    }

    public ResourceLocation id(){
        return id;
    }

    public Set<Item> additionalItems(){
        return additionalItems;
    }

    public Item output(){
        return output;
    }

    public int count(){
        return count;
    }

    @Override
    public boolean equals(Object obj){
        if(obj == this) return true;
        if(obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TransformRecipe)obj;
        return Objects.equals(this.id, that.id) &&
        Objects.equals(this.additionalItems, that.additionalItems) &&
        Objects.equals(this.output, that.output) &&
        this.count == that.count;
    }

    @Override
    public int hashCode(){
        return Objects.hash(id, additionalItems, output, count);
    }

    @Override
    public String toString(){
        return "TransformRecipe[" +
        "id=" + id + ", " +
        "additionalItems=" + additionalItems + ", " +
        "output=" + output + ", " +
        "count=" + count + ']';
    }

}
