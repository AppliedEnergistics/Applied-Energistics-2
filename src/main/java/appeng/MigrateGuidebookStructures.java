package appeng;

import appeng.api.ids.AEComponents;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.crafting.pattern.EncodedCraftingPattern;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.nbt.TagVisitor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.item.ItemStack;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MigrateGuidebookStructures {
    public static void main(String[] args) throws Exception {
        List<Path> snbtFiles;
        try (var s = Files.walk(Paths.get("C:\\AE2\\Forge\\guidebook")).filter(p -> Files.isRegularFile(p) && p.getFileName().toString().endsWith(".snbt"))) {
            snbtFiles = s.toList();
        }

        System.out.println(snbtFiles);

        try {
            for (var snbtFile : snbtFiles) {
                var snbtContent = Files.readString(snbtFile);
                var tag = TagParser.parseTag(snbtContent);

                tag.accept(new MigrateStackVisitor());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    static class MigrateStackVisitor implements TagVisitor {
        @Override
        public void visitCompound(CompoundTag tag) {
            for (String key : tag.getAllKeys()) {
                tag.get(key).accept(this);
            }

            // Heuristically determine if this is an itemstack tag
            // "Count", "id" are required, tag and Slot are allowed
            // If it has no "tag" it doesn't need conversion either...
            if (!tag.contains("Count", Tag.TAG_BYTE) || !tag.contains("id", Tag.TAG_STRING) || !tag.contains("tag", Tag.TAG_COMPOUND)) {
                return;
            }

            for (var otherKey : tag.getAllKeys()) {
                if (!otherKey.equals("id") && !otherKey.equals("Count") && !otherKey.equals("tag") && !otherKey.equals("Slot")) {
                    System.err.println(tag);
                    return;
                }
            }

            var itemId = tag.getString("id");
            var stackNbt = tag.getCompound("tag");
            tag.remove("tag");
            var components = DataComponentPatch.builder();

            switch (itemId) {
                case "ae2:quantum_entangled_singularity" -> {
                    var freq = stackNbt.getLong("freq");
                    if (freq != 0) {
                        stackNbt.remove("freq");
                        components.set(AEComponents.ENTANGLED_SINGULARITY_ID, freq);
                    }
                }
                // Convert basic cell inventory NBT
                case "ae2:fluid_storage_cell_1k", "ae2:fluid_storage_cell_4k", "ae2:fluid_storage_cell_16k",
                     "ae2:fluid_storage_cell_64k", "ae2:fluid_storage_cell_256k",
                     "ae2:item_storage_cell_1k", "ae2:item_storage_cell_4k", "ae2:item_storage_cell_16k",
                     "ae2:item_storage_cell_64k", "ae2:item_storage_cell_256k" -> {
                    List<GenericStack> stacks = new ArrayList<>();
                    if (stackNbt.contains("amts", Tag.TAG_LONG_ARRAY)) {
                        var amounts = stackNbt.getLongArray("amts");
                        var keys = stackNbt.getList("keys", Tag.TAG_COMPOUND);
                        stackNbt.remove("amts");
                        stackNbt.remove("keys");
                        stackNbt.remove("ic");

                        for (int i = 0; i < amounts.length; i++) {
                            var amount = amounts[i];
                            var key = AEKey.CODEC.decode(NbtOps.INSTANCE, keys.getCompound(i)).getOrThrow().getFirst();
                            stacks.add(new GenericStack(key, amount));
                        }
                    }
                    components.set(AEComponents.STORAGE_CELL_INV, stacks);
                }
                case "ae2:crafting_pattern" -> {
                    var in = stackNbt.getList("in", Tag.TAG_COMPOUND);
                    var out = stackNbt.getCompound("out");
                    var recipe = stackNbt.getString("recipe");
                    var substitute = stackNbt.getByte("substitute");
                    var substituteFluids = stackNbt.getByte("substituteFluids");
                    stackNbt.remove("in");
                    stackNbt.remove("out");
                    stackNbt.remove("recipe");
                    stackNbt.remove("substitute");
                    stackNbt.remove("substituteFluids");

                    var inStacks = ItemStack.OPTIONAL_CODEC.listOf().decode(NbtOps.INSTANCE, in).getOrThrow().getFirst();
                    var outStack = ItemStack.CODEC.decode(NbtOps.INSTANCE, out).getOrThrow().getFirst();

                    var pattern = new EncodedCraftingPattern(
                            inStacks,
                            outStack,
                            new ResourceLocation(recipe),
                            substitute != 0,
                            substituteFluids != 0
                    );
                    components.set(AEComponents.ENCODED_CRAFTING_PATTERN, pattern);
                }
            }

            var patch = components.build();
            if (!patch.isEmpty()) {
                tag.put("components", DataComponentPatch.CODEC.encodeStart(NbtOps.INSTANCE, patch).getOrThrow());
            }

            /*DataFixers.getDataFixer().update(
                    References.ITEM_STACK,
                    new Dynamic<>(NbtOps.INSTANCE, stackNbt),

            )*/

            if (!stackNbt.isEmpty()) {
                throw new IllegalStateException("Itemstack NBT not fully converted: " + itemId + " " + stackNbt);
            }
        }

        @Override
        public void visitString(StringTag stringTag) {

        }

        @Override
        public void visitByte(ByteTag byteTag) {

        }

        @Override
        public void visitShort(ShortTag shortTag) {

        }

        @Override
        public void visitInt(IntTag intTag) {

        }

        @Override
        public void visitLong(LongTag longTag) {

        }

        @Override
        public void visitFloat(FloatTag floatTag) {

        }

        @Override
        public void visitDouble(DoubleTag doubleTag) {

        }

        @Override
        public void visitByteArray(ByteArrayTag byteArrayTag) {

        }

        @Override
        public void visitIntArray(IntArrayTag intArrayTag) {

        }

        @Override
        public void visitLongArray(LongArrayTag longArrayTag) {

        }

        @Override
        public void visitList(ListTag listTag) {
            for (Tag tag : listTag) {
                tag.accept(this);
            }
        }

        @Override
        public void visitEnd(EndTag endTag) {

        }
    }

}
