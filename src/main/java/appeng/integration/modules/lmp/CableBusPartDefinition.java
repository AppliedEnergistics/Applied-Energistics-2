package appeng.integration.modules.lmp;

import alexiil.mc.lib.multipart.api.AbstractPart;
import alexiil.mc.lib.multipart.api.MultipartHolder;
import alexiil.mc.lib.multipart.api.PartDefinition;
import alexiil.mc.lib.net.IMsgReadCtx;
import alexiil.mc.lib.net.InvalidInputDataException;
import alexiil.mc.lib.net.NetByteBuf;
import appeng.core.AppEng;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class CableBusPartDefinition {
	private static final ResourceLocation ID = AppEng.makeId("cablebus");

	public static final PartDefinition INSTANCE = new PartDefinition(ID, new PartDefinition.IPartNbtReader() {
		@Override
		public AbstractPart readFromNbt(PartDefinition definition, MultipartHolder holder, CompoundTag nbt) {
			var cableBusPart = new CableBusPart(definition, holder);
			cableBusPart.getCableBus().readFromNBT(nbt);
			return cableBusPart;
		}
	}, new PartDefinition.IPartNetLoader() {
		@Override
		public AbstractPart loadFromBuffer(PartDefinition definition, MultipartHolder holder, NetByteBuf buffer, IMsgReadCtx ctx) throws InvalidInputDataException {
			var cableBusPart = new CableBusPart(definition, holder);
			cableBusPart.readRenderData(buffer, ctx);
			return cableBusPart;
		}
	});

	public static void init() {
		INSTANCE.register();
	}
}
