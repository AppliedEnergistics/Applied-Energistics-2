package appeng.integration.modules.create;

import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.core.definitions.AEBlockEntities;
import appeng.core.definitions.AEBlocks;
import com.simibubi.create.api.schematic.nbt.SafeNbtWriterRegistry;
import com.simibubi.create.api.schematic.requirement.SchematicRequirementRegistries;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.content.schematics.requirement.ItemRequirement.ItemUseType;
import com.simibubi.create.content.schematics.requirement.ItemRequirement.StackRequirement;
import net.createmod.catnip.levelWrappers.SchematicLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class CreateIntegration {
	public static void init() {
		SafeNbtWriterRegistry.REGISTRY.register(AEBlockEntities.CABLE_BUS.get(), (be, data, registries) -> {
			((AEBaseBlockEntity) be).saveAdditional(data, registries);
		});

		// The BE handles this
		SchematicRequirementRegistries.BLOCKS.register(AEBlocks.CABLE_BUS.block(), (state, blockEntity) -> ItemRequirement.NONE);
		SchematicRequirementRegistries.BLOCK_ENTITIES.register(AEBlockEntities.CABLE_BUS.get(), (be, state) -> {
			if (!(be.getLevel() instanceof SchematicLevel schematicLevel))
				return ItemRequirement.NONE;
			
			LootParams.Builder builder = new LootParams.Builder(schematicLevel.getLevel())
					.withParameter(LootContextParams.BLOCK_STATE, state)
					.withParameter(LootContextParams.BLOCK_ENTITY, be)
					.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(be.getBlockPos()))
					.withParameter(LootContextParams.TOOL, Items.DIAMOND_PICKAXE.getDefaultInstance());
			
			List<StackRequirement> stackRequirements = new ArrayList<>();
			for (ItemStack stack : state.getDrops(builder)) {
				stackRequirements.add(new StackRequirement(stack, ItemUseType.CONSUME));
			}
			return new ItemRequirement(stackRequirements);
		});
	}
}
