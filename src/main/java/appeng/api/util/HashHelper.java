package appeng.api.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.RecordBuilder;

import org.jetbrains.annotations.NotNull;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.entity.PotDecorations;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongConsumer;

import appeng.core.AELog;

public final class HashHelper {
    public static final long Upper32BitMask = 0xffff_ffff_0000_0000L;
    public static final long Int32HashMultiplier = 0xcaab_dd49_0000_0001L;
    public static final long SingleHashMultiplier = 0xa27c_d305_0000_0001L;
    public static final long TrueHash = 0x8a21_9611_5125_2f09L;
    public static final long Int8HashMultiplier = 0xde45_d078_e9c2_6b01L;
    public static final long Int16HashMultiplier = 0x95ce_58f3_f2f9_0001L;

    public static final long ByteBufferHashStepMultiplier = 0xcce3_6a8a_2491_0af5L;
    public static final long ByteBufferHashStepIncrement = 0x82f0_140a_5504_0d2dL;
    public static final long ListHashStepMultiplier = 0xf9bc_4abf_95dd_c3bdL;
    public static final long ListHashStepIncrement = 0xfdaf_1f17_9f9a_a90dL;
    public static final long ListHashEmptyInitializationValue = 0xa694_d119_309f_2a69L;
    public static final long FluidStackHashStepMultiplier = 0xb8d7_8e1e_34e7_db35L;

    private HashHelper() {
    }

    public static long calculateMapEntryHash(long key, long value) {
        long vh = value * 0x4B08_ED39_44AC_043DL;
        long kh = key * 0x8709_f1aa_043f_0fd9L;
        vh ^= vh >>> 32;
        kh ^= kh << 32;
        vh ^= kh;
        vh *= 0x4B08_ED39_44AC_043DL;
        kh += vh;
        kh ^= kh >>> 32;
        return kh;
    }

    public static long calculateTypedHash(int type, long value) {
        long kh = Integer.toUnsignedLong(type) * 0xbf89_06cd_b840_a696L;
        long vh = value * 0xd0de_74bb_809c_8fe9L;
        kh ^= kh << 32;
        kh += 0xE582_29C7L;
        vh ^= kh;
        vh *= 0xd0de_74bb_809c_8fe9L;
        kh += vh;
        kh ^= kh >>> 32;
        return kh;
    }

    public static long calculateTypedHash(int type, int value) {
        return calculateTypedHash(type, Integer.toUnsignedLong(value));
    }

    public static long combineInt32Hash(int left, int right) {
        long hash = Integer.toUnsignedLong(left) << 32;
        hash += Integer.toUnsignedLong(right);
        hash *= 0xe928_e0e1_7107_91d1L;
        hash ^= hash >>> 32;
        return hash;
    }

    public static long hashByteBuf(ByteBuf input) {
        long hash = 0x8157_c6c6_8dbe_2a1bL;
        if (input.readableBytes() > 0) {
            hash *= ByteBufferHashStepMultiplier;
            hash += ByteBufferHashStepIncrement;
            hash ^= hash >>> 32;
            while (input.readableBytes() >= Long.BYTES) {
                var value = input.readLongLE();
                hash += value;
                hash *= ByteBufferHashStepMultiplier;
                hash += ByteBufferHashStepIncrement;
                hash ^= hash >>> 32;
            }
            if (input.isReadable()) {
                var buffer = new byte[Long.BYTES];
                var byteBuffer = ByteBuffer.wrap(buffer);
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                int remaining = input.readableBytes();
                input.readBytes(buffer, 0, remaining);
                var value = byteBuffer.getLong(0);
                var padding = 0x85fd_3a36_b93d_b485L << (8 * remaining);
                value ^= padding;
                hash += value;
                hash *= ByteBufferHashStepMultiplier;
                hash += ByteBufferHashStepIncrement;
                hash ^= hash >>> 32;
            }
        }
        return hash;
    }

    public static long hashByteBuffer(ByteBuffer input) {
        var wrappedBuffer = Unpooled.wrappedBuffer(input);
        try {
            return hashByteBuf(wrappedBuffer);
        } finally {
            wrappedBuffer.release();
        }
    }

    public static long hashComponentMap(@Nullable Iterable<TypedDataComponent<?>> componentMap) {
        long r = 0;
        if (componentMap != null) {
            for (var entry : componentMap) {
                var type = entry.type();
                if (type.isTransient())
                    continue;
                var keyHash = hashString(type.toString());
                var value = entry.value();
                long valueHash = calculateHash(value);
                r += calculateMapEntryHash(keyHash, valueHash);
            }
        }
        return r;
    }

    public static long hashComponentPatch(@Nullable DataComponentPatch componentPatch) {
        long r = 0;
        if (componentPatch != null && !componentPatch.isEmpty()) {
            var entries = componentPatch.entrySet();
            for (var entry : entries) {
                var type = entry.getKey();
                if (type.isTransient())
                    continue;
                var keyHash = hashString(type.toString());
                var value = entry.getValue();
                long valueHash = -1;
                if (value.isPresent()) {
                    valueHash = calculateHash(value.get());
                }
                r += calculateMapEntryHash(keyHash, valueHash);
            }
        }
        return r;
    }

    public static long hashMapValue(Map<?, ?> componentMap) {
        long r = 0;
        if (componentMap != null && !componentMap.isEmpty()) {
            for (var entry : componentMap.entrySet()) {
                var key = entry.getKey();
                var keyHash = calculateHash(key);
                var value = entry.getValue();
                long valueHash = calculateHash(value);
                r += calculateMapEntryHash(keyHash, valueHash);
            }
        }
        return r;
    }

    public static long hashString(String value) {
        long hash = 0;
        if (!value.isEmpty()) {
            var consumer = new CharHashListConsumer();
            value.chars().forEachOrdered(consumer);
            hash = consumer.getHash();
        }
        return hash;
    }

    /// Calculates the hash value of well-known data component values.
    public static long calculateHash(Object value) {
        return switch (value) {
            case null -> 0L;
            case Enum<?> enumValue -> hashEnum(enumValue);
            case ItemStack itemStack -> hashItemStack(itemStack);
            case Item item -> hashItem(item);
            case FluidStack fluidStack -> hashFluidStack(fluidStack);
            case Fluid fluid -> hashFluid(fluid);
            case Boolean boolValue -> hashBoolean(boolValue);
            case Long longValue -> longValue;
            case Double doubleValue -> hashDouble(doubleValue);
            case Integer integerValue -> hashInt32(integerValue);
            case Float floatValue -> hashSingle(floatValue);
            case Short shortValue -> hashInt16(shortValue);
            case Byte byteValue -> hashSByte(byteValue);
            case BigDecimal bigDecimalValue -> calculateTypedHash(0x8869_2419, bigDecimalValue.hashCode());
            case BigInteger bigIntegerValue -> calculateTypedHash(0xab5b_e641, bigIntegerValue.hashCode());
            case Number numberValue -> calculateTypedHash(0x8490_da45, numberValue.hashCode());
            case String stringValue -> hashString(stringValue);
            case Holder<?> holder -> hashHolder(holder);
            case Component component -> hashComponent(component);
            case ItemLore lore -> hashComponentList(lore.lines());
            case ResourceLocation resourceLocation -> hashString(resourceLocation.toString());
            case ResourceKey<?> resourceKey -> hashResourceKey(resourceKey);
            case StringRepresentable stringRepresentable -> hashString(stringRepresentable.getSerializedName());
            case ItemEnchantments enchantments -> hashItemEnchantments(enchantments);
            case ByteBuffer byteBuffer -> hashByteBuffer(byteBuffer);
            case CustomData customData -> hashCustomData(customData);
            case WritableBookContent writableBookContent -> hashWritableBookContent(writableBookContent);
            case WrittenBookContent writtenBookContent -> hashWrittenBookContent(writtenBookContent);
            case ItemContainerContents itemContainerContents -> hashItemContainerContents(itemContainerContents);
            case BundleContents bundleContents -> hashItemStackIterable(bundleContents.items());
            case ChargedProjectiles chargedProjectiles -> hashItemStackIterable(chargedProjectiles.getItems());
            case PotDecorations potDecorations -> hashPotDecorations(potDecorations);
            case FireworkExplosion fireworkExplosion -> hashFireworkExplosion(fireworkExplosion);
            case Fireworks fireworks -> hashFireworks(fireworks);
            case IntList intList -> hashIntList(intList);
            case PatchedDataComponentMap patchedDataComponentMap ->
                hashComponentPatch(patchedDataComponentMap.asPatch());
            case DataComponentMap dataComponentMap -> hashComponentMap(dataComponentMap);
            case List<?> list -> hashList(list);
            default -> hashComplicatedComponentValue(value);
        };
    }

    private static long hashBoolean(Boolean boolValue) {
        return boolValue ? TrueHash : ~TrueHash;
    }

    private static long hashComplicatedComponentValue(Object value) {
        long hash = Integer.toUnsignedLong(value.hashCode());
        hash *= 0xf7a4_2d8e_9e67_c175L;
        hash ^= hash >>> 32;
        return hash;
    }

    private static long hashComponent(Component component) {
        var hashConsumer = new HashContentConsumer();
        var result = component.visit(hashConsumer, Style.EMPTY);
        long hash = 0;
        if (result.isPresent()) {
            hash = result.orElse(0L);
            hash = listHashStep(hash);
        }
        return hash;
    }

    private static long hashComponentList(List<Component> values) {
        long hash = ListHashEmptyInitializationValue;
        if (!values.isEmpty()) {
            hash = listHashStep(hash);
            for (var v : values) {
                var value = hashComponent(v);
                hash += value;
                hash = listHashStep(hash);
            }
        }
        return hash;
    }

    private static long hashCustomData(CustomData value) {
        var result = CustomData.CODEC.encodeStart(HashOps.INSTANCE, value).resultOrPartial();
        long hash = result.orElse(0L);
        if (result.isEmpty()) {
            // Fallback hash calculation
            long q = Integer.toUnsignedLong(value.hashCode());
            q |= Integer.toUnsignedLong(value.size()) << 32;
            q *= 0xf858_9587_1983_a041L;
            q ^= q >>> 32;
            hash = q;
        }
        return hash;
    }

    private static long hashDouble(Double doubleValue) {
        return Double.doubleToRawLongBits(doubleValue);
    }

    private static long hashEnum(Enum<?> enumValue) {
        return calculateTypedHash(0xb937_df15, enumValue.ordinal());
    }

    private static long hashFireworkExplosion(FireworkExplosion explosion) {
        long hash = 0;
        if (explosion != null) {
            var shape = explosion.shape().ordinal();
            var hasTrail = explosion.hasTrail() ? 1 : 0;
            var hasTwinkle = explosion.hasTwinkle() ? 1 : 0;
            var boolValues = hasTrail * 2 + hasTwinkle;
            var combined = combineInt32Hash(shape, boolValues);
            hash += combined;
            hash = listHashStep(hash);
            var colors = explosion.colors();
            hash += hashIntList(colors);
            hash = listHashStep(hash);
            var fadeColors = explosion.fadeColors();
            hash += hashIntList(fadeColors);
            hash = listHashStep(hash);
        }
        return hash;
    }

    private static long hashFireworks(Fireworks fireworks) {
        long hash = ListHashEmptyInitializationValue;
        if (fireworks != null) {
            hash = 0xffe_4a27_73f1_b9b5L + hashInt32(fireworks.flightDuration());
            hash = listHashStep(hash);
            var explosions = fireworks.explosions();
            hash += hashInt32(explosions.size());
            hash = listHashStep(hash);
            for (var explosion : explosions) {
                hash += hashFireworkExplosion(explosion);
                hash = listHashStep(hash);
            }
        }
        return hash;
    }

    public static long hashFluidAndComponents(FluidStack stack) {
        long r = 0xb208_c4f4_5f06_4efdL;
        if (stack != null && !stack.isEmpty()) {
            r += hashFluid(stack.getFluid());
            r *= FluidStackHashStepMultiplier;
            r ^= r >> 32;
            r += HashHelper.hashComponentPatch(stack.getComponentsPatch());
            r *= FluidStackHashStepMultiplier;
        }
        return r ^ (r >> 32);
    }

    public static long hashFluidStack(FluidStack stack) {
        long r = 0xb208_c4f4_5f06_4efdL;
        if (stack != null && !stack.isEmpty()) {
            r += hashFluid(stack.getFluid());
            r *= FluidStackHashStepMultiplier;
            r ^= r >> 32;
            r += HashHelper.hashComponentPatch(stack.getComponentsPatch());
            r *= FluidStackHashStepMultiplier;
            r ^= r >> 32;
            r += stack.getAmount();
            r *= FluidStackHashStepMultiplier;
        }
        return r ^ (r >> 32);
    }

    public static long hashFluid(Fluid fluid) {
        return 0xb8d7_8e1e_34e7_db35L * fluid.hashCode();
    }

    public static long listHashStep(long hash) {
        hash *= ListHashStepMultiplier;
        hash += ListHashStepIncrement;
        hash ^= hash >>> 32;
        return hash;
    }

    private static long hashHolder(Holder<?> holder) {
        var key = holder.unwrapKey();
        long hash = 0;
        if (key.isPresent()) {
            var resourceKey = key.get();
            hash = hashResourceKey(resourceKey);
        }
        return hash;
    }

    private static long hashResourceKey(ResourceKey<?> resourceKey) {
        return combineInt32Hash(resourceKey.registry().hashCode(), resourceKey.location().hashCode());
    }

    private static long hashInt16(Short shortValue) {
        return Short.toUnsignedLong(shortValue) * Int16HashMultiplier;
    }

    public static long hashInt32(Integer integerValue) {
        return Integer.toUnsignedLong(integerValue) * Int32HashMultiplier;
    }

    private static long hashIntList(IntList values) {
        long hash = ListHashEmptyInitializationValue;
        if (!values.isEmpty()) {
            hash = listHashStep(hash);
            for (var v : values) {
                var value = hashInt32(v);
                hash += value;
                hash = listHashStep(hash);
            }
        }
        return hash;
    }

    public static long hashItemStack(@Nullable ItemStack stack) {
        long r = 0x5993_84ed_c24d_95edL;
        if (stack != null && !stack.isEmpty()) {
            r += hashItem(stack.getItem());
            r *= 2817661010293465519L;
            r ^= r >> 32;
            r += HashHelper.hashComponentPatch(stack.getComponentsPatch());
            r *= 2817661010293465519L;
            r ^= r >> 32;
            r += stack.getCount();
            r *= 2817661010293465519L;
        }
        return r ^ (r >> 32);
    }

    public static long hashItemAndComponents(@Nullable ItemStack stack) {
        long r = 6454648847654032877L;
        if (stack != null && !stack.isEmpty()) {
            r += hashItem(stack.getItem());
            r *= 2817661010293465519L;
            r ^= r >> 32;
            r += HashHelper.hashComponentPatch(stack.getComponentsPatch());
            r *= 2817661010293465519L;
        }
        return r ^ (r >> 32);
    }

    public static long hashItem(@NotNull Item item) {
        return 0x3c8c_356d_22bc_4275L * item.hashCode();
    }

    private static long hashItemContainerContents(ItemContainerContents value) {
        long hash = ListHashEmptyInitializationValue;
        if (value.getSlots() > 0) {
            hash = listHashStep(hash);
            for (int i = 0; i < value.getSlots(); i++) {
                var stack = value.getStackInSlot(i);
                var sh = hashItemStack(stack);
                hash += sh;
                hash = listHashStep(hash);
            }
        }
        return hash;
    }

    private static long hashItemEnchantments(ItemEnchantments enchantments) {
        long r = 0;
        if (enchantments != null && !enchantments.isEmpty()) {
            for (var entry : enchantments.entrySet()) {
                var key = entry.getKey();
                var keyHash = hashHolder(key);
                var value = entry.getIntValue();
                long valueHash = hashInt32(value);
                r += calculateMapEntryHash(keyHash, valueHash);
            }
        }
        return r;
    }

    private static long hashItemStackIterable(Iterable<ItemStack> contents) {
        long hash = 0xffe_4a27_73f1_b9b5L;
        for (var stack : contents) {
            var sh = hashItemStack(stack);
            hash += sh;
            hash = listHashStep(hash);
        }
        return hash;
    }

    private static long hashList(List<?> values) {
        long hash = ListHashEmptyInitializationValue;
        if (!values.isEmpty()) {
            hash = listHashStep(hash);
            for (var v : values) {
                var value = calculateHash(v);
                hash += value;
                hash = listHashStep(hash);
            }
        }
        return hash;
    }

    private static long hashPotDecorations(PotDecorations value) {
        long hash = ListHashEmptyInitializationValue;
        var values = value.ordered();
        if (!values.isEmpty()) {
            hash = listHashStep(hash);
            for (var v : values) {
                var vh = hashItem(v);
                hash += vh;
                hash = listHashStep(hash);
            }
        }
        return hash;
    }

    private static long hashSByte(Byte byteValue) {
        return Byte.toUnsignedLong(byteValue) * Int8HashMultiplier;
    }

    private static long hashSingle(Float floatValue) {
        return Integer.toUnsignedLong(Float.floatToRawIntBits(floatValue)) * SingleHashMultiplier;
    }

    private static long hashWritableBookContent(WritableBookContent content) {
        long hash = ListHashEmptyInitializationValue;
        var pages = content.pages();
        if (!pages.isEmpty()) {
            hash = listHashStep(hash);
            for (var v : pages) {
                // Profanity filters can throw some entropy away, so we use unfiltered values.
                var value = hashString(v.get(false));
                hash += value;
                hash = listHashStep(hash);
            }
        }
        return hash;
    }

    private static long hashWrittenBookContent(WrittenBookContent content) {
        long hash = ListHashEmptyInitializationValue;
        var pages = content.pages();
        if (!pages.isEmpty()) {
            hash = listHashStep(hash);
            for (var v : pages) {
                // Profanity filters can throw some entropy away, so we use unfiltered values.
                var value = hashComponent(v.get(false));
                hash += value;
                hash = listHashStep(hash);
            }
        }
        return hash;
    }

    public static final class Int64HashListConsumer implements LongConsumer {
        private long hash = 0;
        private long keyGeneratorState = 0xb7a5_3670_ce41_fa4fL;

        @Override
        public void accept(long value) {
            var kh = keyGeneratorState;
            var rh = hash;
            long vh = value;
            keyGeneratorState = kh * 0xb43f_8023_73f2_29c5L + 0xb7a5_3670_ce41_fa4fL;
            rh *= 0xeca9_41c4_6bb8_9a75L;
            vh ^= vh >>> 32;
            kh ^= kh << 32;
            rh ^= rh >>> 32;
            vh ^= kh;
            vh *= 0xa254_4943_0db6_0e45L;
            kh += vh;
            hash = kh + rh;
        }

        public long getHash() {
            return hash;
        }
    }

    public static final class Int32HashListConsumer implements IntConsumer {
        private long hash = 0;
        private long keyGeneratorState = 0x968e_b855_adf5_f441L;

        @Override
        public void accept(int value) {
            var kh = keyGeneratorState;
            long vh = Integer.toUnsignedLong(value);
            keyGeneratorState = kh * 0xa254_4943_0db6_0e45L + 0x968e_b855_adf5_f441L;
            kh ^= kh << 32;
            kh = kh & Upper32BitMask | vh;
            kh += hash;
            kh *= 0xeca9_41c4_6bb8_9a75L;
            kh ^= kh >>> 32;
            hash = kh;
        }

        public long getHash() {
            return hash;
        }
    }

    public static final class CharHashListConsumer implements IntConsumer {
        private long hash = 0;
        private long keyGeneratorState = 0x90c3_87ff_1f06_0e01L;

        @Override
        public void accept(int value) {
            var kh = keyGeneratorState;
            long vh = Integer.toUnsignedLong(value);
            keyGeneratorState = kh * 0xf7be_7af8_97e9_b4adL + 0x90c3_87ff_1f06_0e01L;
            kh ^= kh >>> 32;
            kh ^= vh;
            kh += hash;
            kh *= 0xeca9_41c4_6bb8_9a75L;
            kh ^= kh >>> 32;
            hash = kh;
        }

        public long getHash() {
            return hash;
        }
    }

    public static final class HashOps implements DynamicOps<Long> {

        public static final long ByteBufferHashStepMultiplier = 0xcce3_6a8a_2491_0af5L;
        public static final long ByteBufferHashStepIncrement = 0x82f0_140a_5504_0d2dL;
        public static final HashOps INSTANCE;

        static {
            INSTANCE = new HashOps();
        }

        private HashOps() {
        }

        private static String getMessage() {
            return "Not Supported!";
        }

        @Override
        public Long empty() {
            return 0L;
        }

        @Override
        public Long emptyMap() {
            return 0L;
        }

        @Override
        public Long emptyList() {
            return 0L;
        }

        @Override
        public <U> U convertTo(DynamicOps<U> outOps, Long input) {
            return outOps.createLong(input);
        }

        @Override
        public DataResult<Number> getNumberValue(Long input) {
            return DataResult.error(HashOps::getMessage);
        }

        @Override
        public Long createNumeric(Number i) {
            return switch (i) {
                case Long longValue -> longValue;
                case Double doubleValue -> createDouble(doubleValue);
                case Integer integerValue -> createInt(integerValue);
                case Short shortValue -> createShort(shortValue);
                case Byte byteValue -> createByte(byteValue);
                case BigDecimal bigDecimalValue -> calculateTypedHash(0x8869_2419, bigDecimalValue.hashCode());
                case BigInteger bigIntegerValue -> calculateTypedHash(0xab5b_e641, bigIntegerValue.hashCode());
                case null, default -> calculateTypedHash(i.hashCode(), i.longValue());
            };
        }

        @Override
        public Long createByte(byte value) {
            return hashSByte(value);
        }

        @Override
        public Long createShort(short value) {
            return hashInt16(value);
        }

        @Override
        public Long createInt(int value) {
            return hashInt32(value);
        }

        @Override
        public Long createLong(long value) {
            return value;
        }

        @Override
        public Long createFloat(float value) {
            return hashSingle(value);
        }

        @Override
        public Long createDouble(double value) {
            return hashDouble(value);
        }

        @Override
        public Long createBoolean(boolean value) {
            return hashBoolean(value);
        }

        @Override
        public DataResult<String> getStringValue(Long input) {
            return DataResult.error(HashOps::getMessage);
        }

        @Override
        public Long createString(String value) {
            long hash = 0;
            if (!value.isEmpty()) {
                var consumer = new CharHashListConsumer();
                value.chars().forEachOrdered(consumer);
                hash = consumer.getHash();
            }
            return hash;
        }

        @Override
        public DataResult<Long> mergeToList(Long list, Long value) {
            return DataResult.success(list + value);
        }

        @Override
        public DataResult<Long> mergeToMap(Long map, Long key, Long value) {
            return DataResult.success(map + calculateMapEntryHash(key, value));
        }

        @Override
        public DataResult<Stream<Pair<Long, Long>>> getMapValues(Long input) {
            return DataResult.error(HashOps::getMessage);
        }

        @Override
        public Long createMap(Stream<Pair<Long, Long>> map) {
            return map.map(p -> calculateMapEntryHash(p.getFirst(), p.getSecond())).reduce(0L, Long::sum);
        }

        @Override
        public DataResult<Stream<Long>> getStream(Long input) {
            return DataResult.error(HashOps::getMessage);
        }

        @Override
        public Long createList(Stream<Long> input) {
            var consumer = new Int64HashListConsumer();
            input.forEachOrdered(consumer);
            return consumer.getHash();
        }

        @Override
        public Long createByteList(ByteBuffer input) {
            return hashByteBuffer(input);
        }

        @Override
        public Long createIntList(IntStream input) {
            var consumer = new Int32HashListConsumer();
            input.forEachOrdered(consumer);
            return consumer.getHash();
        }

        @Override
        public Long createLongList(LongStream input) {
            var consumer = new Int64HashListConsumer();
            input.forEachOrdered(consumer);
            return consumer.getHash();
        }

        @Override
        public Long remove(Long input, String key) {
            AELog.debug("Tried to remove the key \"%s\" with the HashOps while it is not supported!", key);
            return input;
        }

        @Override
        public RecordBuilder<Long> mapBuilder() {
            return new HashRecordBuilder(this);
        }

        public static final class HashRecordBuilder extends RecordBuilder.AbstractUniversalBuilder<Long, Long> {
            HashRecordBuilder(HashOps ops) {
                super(ops);
            }

            @Override
            protected Long append(Long key, Long value, Long builder) {
                return builder + calculateMapEntryHash(key, value);
            }

            @Override
            protected Long initBuilder() {
                return 0L;
            }

            @Override
            protected DataResult<Long> build(Long builder, Long prefix) {
                return DataResult.success(builder + prefix);
            }
        }
    }

    public static final class HashContentConsumer implements FormattedText.StyledContentConsumer<Long> {
        long hash = 0;

        @Override
        public @NotNull Optional<Long> accept(Style style, String s) {
            var h = hash;
            h = listHashStep(h);
            h += combineInt32Hash(style.hashCode(), s.hashCode());
            hash = h;
            return Optional.of(h);
        }

        public long setHash(long hash) {
            var oldHash = this.hash;
            this.hash = hash;
            return oldHash;
        }
    }
}
