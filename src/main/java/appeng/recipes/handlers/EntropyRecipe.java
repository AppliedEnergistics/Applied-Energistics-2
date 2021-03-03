package appeng.recipes.handlers;

import java.util.Collections;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

import appeng.core.AppEng;
import appeng.items.tools.powered.EntropyManipulatorItem;

/**
 * A special recipe used for the {@link EntropyManipulatorItem}.
 */
public class EntropyRecipe implements IRecipe<IInventory> {

    public static final ResourceLocation TYPE_ID = AppEng.makeId("entropy");

    public static final IRecipeType<EntropyRecipe> TYPE = IRecipeType.register(TYPE_ID.toString());

    private final ResourceLocation id;
    private final EntropyMode mode;

    private final String inputBlockId;
    private final String inputFluidId;
    private final Block inputBlock;
    private final Fluid inputFluid;

    private final String outputBlockId;
    private final String outputFluidId;
    private final Block outputBlock;
    private final Fluid outputFluid;

    private final List<ItemStack> drops;

    public EntropyRecipe(ResourceLocation id, EntropyMode mode, String inputBlockId, String inputFluidId,
            String outputBlockId, String outputFluidId, List<ItemStack> drops) {
        this.id = id;
        this.mode = mode;

        this.inputBlockId = inputBlockId;
        this.inputFluidId = inputFluidId;

        this.inputBlock = inputBlockId != null
                ? ForgeRegistries.BLOCKS.getValue(new ResourceLocation(inputBlockId))
                : null;
        this.inputFluid = inputFluidId != null
                ? ForgeRegistries.FLUIDS.getValue(new ResourceLocation(inputFluidId))
                : null;

        this.outputBlockId = outputBlockId;
        this.outputFluidId = outputFluidId;

        this.outputBlock = outputBlockId != null
                ? ForgeRegistries.BLOCKS.getValue(new ResourceLocation(outputBlockId))
                : null;
        this.outputFluid = outputFluidId != null
                ? ForgeRegistries.FLUIDS.getValue(new ResourceLocation(outputFluidId))
                : null;

        this.drops = drops != null ? drops : Collections.emptyList();
    }

    @Override
    public boolean matches(IInventory inv, World worldIn) {
        return false;
    }

    @Override
    public ItemStack getCraftingResult(IInventory inv) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canFit(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return EntropyRecipeSerializer.INSTANCE;
    }

    @Override
    public IRecipeType<?> getType() {
        return TYPE;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.create();
    }

    public EntropyMode getMode() {
        return this.mode;
    }

    public String getInputBlockId() {
        return this.inputBlockId;
    }

    public Block getInputBlock() {
        return this.inputBlock;
    }

    public String getInputFluidId() {
        return this.inputFluidId;
    }

    public Fluid getInputFluid() {
        return this.inputFluid;
    }

    public String getOutputBlockId() {
        return this.outputBlockId;
    }

    public Block getOutputBlock() {
        return this.outputBlock;
    }

    public BlockState getOutputBlockState() {
        if (this.getOutputBlock() == null) {
            return null;
        }

        return getOutputBlock().getDefaultState();
    }

    public String getOutputFluidId() {
        return outputFluidId;
    }

    public Fluid getOutputFluid() {
        return this.outputFluid;
    }

    public FluidState getOutputFluidState() {
        if (this.getOutputFluid() == null) {
            return null;
        }

        return getOutputFluid().getDefaultState();
    }

    public List<ItemStack> getDrops() {
        return this.drops;
    }

    public static enum EntropyMode {
        HEAT,
        COOL;
    }
}
