package appeng.api.stacks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.ListBuilder;

import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

class GenericStackListCodec implements Codec<List<@Nullable GenericStack>> {
    private final Codec<GenericStack> innerCodec;

    public GenericStackListCodec(Codec<GenericStack> innerCodec) {
        this.innerCodec = innerCodec;
    }

    @Override
    public <T> DataResult<T> encode(List<@Nullable GenericStack> input, DynamicOps<T> ops, T prefix) {
        final ListBuilder<T> builder = ops.listBuilder();

        for (var genericStack : input) {
            if (genericStack == null) {
                builder.add(ops.emptyMap());
            } else {
                builder.add(innerCodec.encodeStart(ops, genericStack));
            }
        }

        return builder.build(prefix);
    }

    @Override
    public <T> DataResult<Pair<List<@Nullable GenericStack>, T>> decode(DynamicOps<T> ops, T input) {

        return ops.getList(input).setLifecycle(Lifecycle.stable()).flatMap(stream -> {
            var elements = new ArrayList<GenericStack>();
            final Stream.Builder<T> failed = Stream.builder();
            // TODO: AtomicReference.getPlain/setPlain in java9+
            final MutableObject<DataResult<Unit>> result = new MutableObject<>(
                    DataResult.success(Unit.INSTANCE, Lifecycle.stable()));

            stream.accept(t -> {
                if (ops.emptyMap().equals(t)) {
                    elements.add(null);
                } else {
                    DataResult<Pair<GenericStack, T>> element = innerCodec.decode(ops, t);
                    element.error().ifPresent(e -> failed.add(t));
                    result.setValue(result.getValue().apply2stable((r, v) -> {
                        elements.add(v.getFirst());
                        return r;
                    }, element));
                }
            });

            final T errors = ops.createList(failed.build());

            final Pair<List<GenericStack>, T> pair = Pair.of(Collections.unmodifiableList(elements), errors);

            return result.getValue().map(unit -> pair).setPartial(pair);
        });
    }

}
