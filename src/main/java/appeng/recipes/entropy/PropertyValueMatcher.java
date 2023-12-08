package appeng.recipes.entropy;

import java.util.List;
import java.util.Map;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;

public sealed interface PropertyValueMatcher permits PropertyValueMatcher.SingleValue,PropertyValueMatcher.MultiValue,PropertyValueMatcher.Range {
    Codec<PropertyValueMatcher> CODEC = new Codec<>() {
        @Override
        public <T> DataResult<Pair<PropertyValueMatcher, T>> decode(DynamicOps<T> ops, T input) {
            // Try single string value first
            var singleValueResult = Codec.STRING.decode(ops, input)
                    .map(pair -> pair.mapFirst(value -> (PropertyValueMatcher) new SingleValue(value)));
            if (singleValueResult.error().isEmpty()) {
                return singleValueResult;
            }

            // Then try list value
            var listValueResult = Codec.STRING.listOf().decode(ops, input)
                    .map(pair -> pair.mapFirst(value -> (PropertyValueMatcher) new MultiValue(value)));
            if (listValueResult.error().isEmpty()) {
                return listValueResult;
            }

            // Then try min/max object
            var rangeValueResult = Range.CODEC.decode(ops, input)
                    .map(pair -> pair.mapFirst(value -> (PropertyValueMatcher) value));
            if (rangeValueResult.error().isEmpty()) {
                return rangeValueResult;
            }

            // If all three fail, combine the errors
            return DataResult.error(
                    () -> "Property values need to be strings, list of strings, or objects with min/max properties");
        }

        @Override
        public <T> DataResult<T> encode(PropertyValueMatcher input, DynamicOps<T> ops, T prefix) {
            if (input instanceof SingleValue singleValue) {
                return Codec.STRING.encode(singleValue.value(), ops, prefix);
            } else if (input instanceof MultiValue multiValue) {
                return Codec.STRING.listOf().encode(multiValue.values(), ops, prefix);
            } else if (input instanceof Range range) {
                return Range.CODEC.encode(range, ops, prefix);
            } else {
                throw new IllegalStateException("This cannot happen");
            }
        }
    };

    Codec<Map<String, PropertyValueMatcher>> MAP_CODEC = Codec.unboundedMap(Codec.STRING, CODEC);

    static PropertyValueMatcher fromNetwork(FriendlyByteBuf buffer) {
        var type = buffer.readByte();
        return switch (type) {
            case 0 -> new SingleValue(buffer.readUtf());
            case 1 -> new MultiValue(buffer.readList(FriendlyByteBuf::readUtf));
            case 2 -> new Range(buffer.readUtf(), buffer.readUtf());
            default -> throw new IllegalStateException("Invalid property value matcher type: " + type);
        };
    }

    static void toNetwork(FriendlyByteBuf buffer, PropertyValueMatcher matcher) {
        matcher.toNetwork(buffer);
    }

    void toNetwork(FriendlyByteBuf buffer);

    void validate(Property<? extends Comparable<?>> property);

    <T extends Comparable<T>> boolean matches(Property<T> property, StateHolder<?, ?> state);

    record SingleValue(String value) implements PropertyValueMatcher {
        @Override
        public void toNetwork(FriendlyByteBuf buffer) {
            buffer.writeByte(0);
            buffer.writeUtf(value);
        }

        @Override
        public void validate(Property<? extends Comparable<?>> property) {
            if (property.getValue(value).isEmpty()) {
                throw new IllegalStateException(
                        "Property " + property.getName() + " does not have value '" + value + "'");
            }
        }

        @Override
        public <T extends Comparable<T>> boolean matches(Property<T> property, StateHolder<?, ?> state) {
            var currentValue = property.getName(state.getValue(property));
            return value.equals(currentValue);
        }
    }

    record MultiValue(List<String> values) implements PropertyValueMatcher {
        @Override
        public void toNetwork(FriendlyByteBuf buffer) {
            buffer.writeByte(1);
            buffer.writeCollection(values, FriendlyByteBuf::writeUtf);
        }

        @Override
        public void validate(Property<? extends Comparable<?>> property) {
            for (String value : values) {
                if (property.getValue(value).isEmpty()) {
                    throw new IllegalStateException(
                            "Property " + property.getName() + " does not have value '" + value + "'");
                }
            }
        }

        @Override
        public <T extends Comparable<T>> boolean matches(Property<T> property, StateHolder<?, ?> state) {
            var currentValue = property.getName(state.getValue(property));
            for (var value : values) {
                return value.equals(currentValue);
            }
            return true;
        }
    }

    record Range(String min, String max) implements PropertyValueMatcher {
        static Codec<Range> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                Codec.STRING.fieldOf("min").forGetter(Range::min),
                Codec.STRING.fieldOf("max").forGetter(Range::max)).apply(builder, Range::new));

        @Override
        public void toNetwork(FriendlyByteBuf buffer) {
            buffer.writeByte(2);
            buffer.writeUtf(min);
            buffer.writeUtf(max);
        }

        @Override
        public void validate(Property<? extends Comparable<?>> property) {
            if (property.getValue(min).isEmpty()) {
                throw new IllegalStateException(
                        "Property " + property.getName() + " does not have value '" + min + "'");
            }
            if (property.getValue(max).isEmpty()) {
                throw new IllegalStateException(
                        "Property " + property.getName() + " does not have value '" + max + "'");
            }
        }

        @Override
        public <T extends Comparable<T>> boolean matches(Property<T> property, StateHolder<?, ?> state) {
            var minValue = property.getValue(min).orElseThrow();
            var maxValue = property.getValue(max).orElseThrow();
            var value = state.getValue(property);
            return value.compareTo(minValue) >= 0 && value.compareTo(maxValue) <= 0;
        }
    }
}
