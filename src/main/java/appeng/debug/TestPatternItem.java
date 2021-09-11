package appeng.debug;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.AEApi;
import appeng.api.crafting.IPatternDetailsDecoder;
import appeng.api.networking.crafting.IPatternDetails;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.items.AEBaseItem;
import appeng.util.fluid.AEFluidStack;
import appeng.util.item.AEItemStack;

/**
 * Test item that can act as a pattern depending on its enchantment. Use {@code /enchant @s <enchant>} to enchant it.
 */
public class TestPatternItem extends AEBaseItem {
    public TestPatternItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return true;
    }

    static {
        AEApi.patternDetailsHelper().registerDecoder(new IPatternDetailsDecoder() {
            @Override
            public boolean isEncodedPattern(ItemStack stack) {
                return decodePattern(stack, null, false) != null;
            }

            @Nullable
            @Override
            public IPatternDetails decodePattern(ItemStack stack, Level level, boolean autoRecovery) {
                if (stack.getCount() == 1 && stack.getItem() instanceof TestPatternItem) {
                    // Aqua affinity gives: 1 bucket + 1 water -> 1 full bucket.
                    if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.AQUA_AFFINITY, stack) > 0) {
                        return new PatternBuilder(stack, item(Items.WATER_BUCKET))
                                .addPreciseInput(1, item(Items.BUCKET))
                                .addPreciseInput(1, fluid(Fluids.WATER, 1000))
                                .build();
                    }
                    // Depth strider gives: 1 dirt + 2x water bucket -> 1 grass.
                    if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.DEPTH_STRIDER, stack) > 0) {
                        return new PatternBuilder(stack, item(Items.GRASS))
                                .addPreciseInput(1, item(Items.DIRT))
                                .addPreciseInput(2, item(Items.WATER_BUCKET))
                                .build();
                    }
                    // Unbreaking gives: 1 water -> 1 full bucket, to test dispatching the water.
                    if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.UNBREAKING, stack) > 0) {
                        return new PatternBuilder(stack, item(Items.WATER_BUCKET))
                                .addPreciseInput(1, fluid(Fluids.WATER, 1000))
                                .build();
                    }
                }
                return null;
            }
        });
    }

    private static IAEItemStack item(Item item) {
        return AEItemStack.fromItemStack(new ItemStack(item));
    }

    private static IAEFluidStack fluid(Fluid fluid, int amount) {
        return AEFluidStack.fromFluidStack(new FluidStack(fluid, amount));
    }

    private static class PatternBuilder {
        private final IAEStack[] outputs;
        private final List<IPatternDetails.IInput> inputs = new ArrayList<>();
        private final ItemStack definition;

        public PatternBuilder(ItemStack definition, IAEStack... outputs) {
            this.definition = definition;
            this.outputs = outputs;
        }

        public PatternBuilder addPreciseInput(long multiplier, IAEStack... possibleInputs) {
            inputs.add(new IPatternDetails.IInput() {
                @Override
                public IAEStack[] getPossibleInputs() {
                    return possibleInputs;
                }

                @Override
                public long getMultiplier() {
                    return multiplier;
                }

                @Override
                public boolean isValid(IAEStack input, Level level) {
                    for (var possibleInput : possibleInputs) {
                        if (possibleInput.equals(input)) {
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public boolean allowFuzzyMatch() {
                    return false;
                }

                @Nullable
                @Override
                public IAEStack getContainerItem(IAEStack template) {
                    return null;
                }
            });
            return this;
        }

        public IPatternDetails build() {
            return new IPatternDetails() {
                @Override
                public ItemStack getDefinition() {
                    return definition;
                }

                @Override
                public boolean isCrafting() {
                    return false;
                }

                @Override
                public IInput[] getInputs() {
                    return inputs.toArray(IInput[]::new);
                }

                @Override
                public IAEStack[] getOutputs() {
                    return outputs;
                }
            };
        }
    }
}
