package appeng.datagen.providers.models;

import com.google.gson.JsonObject;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.IGeneratedBlockState;

import appeng.api.orientation.BlockOrientation;
import appeng.api.orientation.IOrientationStrategy;

class VariantsBuilder implements IGeneratedBlockState {
    private final Block block;
    private final JsonObject blockStateDef = new JsonObject();
    private final JsonObject variantsDef;

    public VariantsBuilder(Block block) {
        this.block = block;
        this.variantsDef = new JsonObject();
        this.blockStateDef.add("variants", variantsDef);
    }

    public VariantsBuilder generateRotations(BlockModelBuilder model) {
        generateRotations(block.defaultBlockState(), model);
        return this;
    }

    public VariantsBuilder generateRotations(BlockState baseState, BlockModelBuilder model) {
        var strategy = IOrientationStrategy.get(baseState);
        strategy.getAllStates(baseState).forEachOrdered(blockState -> {
            var stateText = new StringBuilder();
            for (var property : strategy.getProperties()) {
                if (stateText.length() > 0) {
                    stateText.append(',');
                }
                appendStateProperty(blockState, property, stateText);
            }

            var modelRotation = BlockOrientation.get(strategy, blockState);
            var rotationX = modelRotation.getAngleX();
            var rotationY = modelRotation.getAngleY();
            var rotationZ = modelRotation.getAngleZ();

            var modelObj = new JsonObject();
            modelObj.addProperty("model", model.getLocation().toString());
            if (rotationX != 0) {
                modelObj.addProperty("x", rotationX);
            }
            if (rotationY != 0) {
                modelObj.addProperty("x", rotationY);
            }
            if (rotationZ != 0) {
                modelObj.addProperty("ae2:z", rotationZ);
            }
            variantsDef.add(stateText.toString(), modelObj);
        });
        return this;
    }

    public JsonObject toJson() {
        return blockStateDef;
    }

    private static <T extends Comparable<T>> void appendStateProperty(BlockState blockState,
            Property<T> property,
            StringBuilder stateText) {
        stateText.append(property.getName());
        stateText.append('=');
        stateText.append(property.getName(blockState.getValue(property)));
    }
}
