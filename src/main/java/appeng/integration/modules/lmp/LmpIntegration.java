package appeng.integration.modules.lmp;

import alexiil.mc.lib.multipart.api.AbstractPart;
import alexiil.mc.lib.multipart.api.MultipartContainer;
import alexiil.mc.lib.multipart.api.MultipartHolder;
import alexiil.mc.lib.multipart.api.NativeMultipart;
import alexiil.mc.lib.multipart.api.render.PartModelBaker;
import alexiil.mc.lib.multipart.api.render.PartRenderContext;
import alexiil.mc.lib.multipart.api.render.PartStaticModelRegisterEvent;
import appeng.client.render.cablebus.CableBusBakedModel;
import appeng.core.definitions.AEBlockEntities;
import appeng.core.definitions.AEBlocks;
import net.fabricmc.fabric.api.event.client.player.ClientPickBlockApplyCallback;
import net.fabricmc.fabric.api.event.client.player.ClientPickBlockGatherCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class LmpIntegration {
	public static void init() {
		NativeMultipart.LOOKUP.registerForBlockEntity((cableBus, ignored) -> {
			return new NativeMultipart() {
				@Nullable
				@Override
				public List<MultipartContainer.MultipartCreator> getMultipartConversion(Level world, BlockPos pos, BlockState state) {
					return List.of(new MultipartContainer.MultipartCreator() {
						@Override
						public AbstractPart create(MultipartHolder holder) {
							var cableBusPart = new CableBusPart(CableBusPartDefinition.INSTANCE, holder);
							var nbt = new CompoundTag();
							cableBus.getCableBus().writeToNBT(nbt);
							cableBusPart.getCableBus().readFromNBT(nbt);
							return cableBusPart;
						}
					});
				}
			};
		}, AEBlockEntities.CABLE_BUS);
		CableBusPartDefinition.init();

		initClient();
	}

	public static void initClient() {
		PartStaticModelRegisterEvent.EVENT.register(renderer -> {
			renderer.register(CableBusPart.ModelKey.class, new PartModelBaker<CableBusPart.ModelKey>() {
				@Override
				public void emitQuads(CableBusPart.ModelKey key, PartRenderContext ctx) {
					var state = key.renderState;
					var cableBusBlockState = AEBlocks.CABLE_BUS.block().defaultBlockState();
					var bakedModel = (CableBusBakedModel) Minecraft.getInstance().getBlockRenderer().getBlockModel(cableBusBlockState);
					bakedModel.emitBlockQuads(state, state.getLevel(), cableBusBlockState, state.getPos(), () -> new Random(42), ctx); // TODO: use random from PartRenderContext
				}
			});
		});

		// Ideally LMP would support this better
		ClientPickBlockGatherCallback.EVENT.register((player, result) -> {
			if (result instanceof BlockHitResult bhr) {
				var container = MultipartContainer.ATTRIBUTE.getFirstOrNull(player.level, bhr.getBlockPos());
				if (container != null) {
					var cableBus = container.getFirstPart(CableBusPart.class);
					if (cableBus != null) {
						var hitPart = cableBus.selectPartWorld(result.getLocation());
						if (hitPart.part != null) {
							return new ItemStack(hitPart.part.getPartItem());
						} else if (hitPart.facade != null) {
							return hitPart.facade.getItemStack();
						}
					}
				}
			}
			return ItemStack.EMPTY;
		});
	}
}
