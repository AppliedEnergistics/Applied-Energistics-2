package appeng.integration.modules.lmp;

import alexiil.mc.lib.multipart.api.AbstractPart;
import alexiil.mc.lib.multipart.api.MultipartEventBus;
import alexiil.mc.lib.multipart.api.MultipartHolder;
import alexiil.mc.lib.multipart.api.PartDefinition;
import alexiil.mc.lib.multipart.api.render.PartModelKey;
import alexiil.mc.lib.net.IMsgReadCtx;
import alexiil.mc.lib.net.IMsgWriteCtx;
import alexiil.mc.lib.net.InvalidInputDataException;
import alexiil.mc.lib.net.NetByteBuf;
import appeng.api.networking.IGridNode;
import appeng.api.parts.IFacadeContainer;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.parts.SelectedPart;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalBlockPos;
import appeng.client.render.cablebus.CableBusRenderState;
import appeng.helpers.AEMultiBlockEntity;
import appeng.hooks.ticking.TickHandler;
import appeng.parts.BusCollisionHelper;
import appeng.parts.CableBusContainer;
import appeng.util.Platform;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Objects;

public class CableBusPart extends AbstractPart implements AEMultiBlockEntity {
	// Voxel shapes for a cable connection to an adjacent block, used in isBlocked()
	private static final double SHORTER = 6.0 / 16.0;
	private static final double LONGER = 10.0 / 16.0;
	private static final double MIN_DIRECTION = 0;
	private static final double MAX_DIRECTION = 1.0;
	private static final VoxelShape[] SIDE_TESTS = {
			// DOWN(0, -1, 0),
			Shapes.box(SHORTER, MIN_DIRECTION, SHORTER, LONGER, SHORTER, LONGER),
			// UP(0, 1, 0),
			Shapes.box( SHORTER, LONGER, SHORTER, LONGER, MAX_DIRECTION, LONGER ),
			// NORTH(0, 0, -1),
			Shapes.box( SHORTER, SHORTER, MIN_DIRECTION, LONGER, LONGER, SHORTER ),
			// SOUTH(0, 0, 1),
			Shapes.box( SHORTER, SHORTER, LONGER, LONGER, LONGER, MAX_DIRECTION ),
			// WEST(-1, 0, 0),
			Shapes.box( MIN_DIRECTION, SHORTER, SHORTER, SHORTER, LONGER, LONGER ),
			// EAST(1, 0, 0),
			Shapes.box( LONGER, SHORTER, SHORTER, MAX_DIRECTION, LONGER, LONGER ),
	};


	private CableBusContainer cb = new CableBusContainer(this);
	private boolean canUpdate = false;

	public CableBusPart(PartDefinition definition, MultipartHolder holder) {
		super(definition, holder);
	}

	public CableBusContainer getCableBus() {
		return this.cb;
	}

	private void setCableBus(CableBusContainer cb) {
		this.cb = cb;
	}

	@Override
	public void writeCreationData(NetByteBuf buffer, IMsgWriteCtx ctx) {
		writeRenderData(buffer, ctx);
	}

	@Override
	public void writeRenderData(NetByteBuf buffer, IMsgWriteCtx ctx) {
		cb.writeToStream(buffer);
	}

	@Override
	public void readRenderData(NetByteBuf buffer, IMsgReadCtx ctx) throws InvalidInputDataException {
		cb.readFromStream(buffer);
	}

	@Override
	public void onAdded(MultipartEventBus bus) {
		super.onAdded(bus);
		// TODO: properly delay to first ticking tick with the tick handler
		if (!holder.getContainer().getMultipartWorld().isClientSide()) {
			TickHandler.instance().addCallable(holder.getContainer().getMultipartWorld(), () -> {
				if (holder.isPresent() && holder.getContainer().getFirstPart(CableBusPart.class) == this) {
					cb.updateConnections();
					cb.addToWorld();
					canUpdate = true;
				}
			});
		}
	}

	@Override
	public void onRemoved() {
		canUpdate = false;
		cb.removeFromWorld();
	}

	@Override
	public VoxelShape getShape() {
		var shape = cb.getShape();
		if (shape.isEmpty()) {
			// TODO: get rid of this, for now it prevents crashes
			return Shapes.block();
		} else {
			return shape;
		}
	}

	@Nullable
	@Override
	public PartModelKey getModelKey() {
		var renderState = getCableBus().getRenderState();
		renderState.setLevel(holder.getContainer().getMultipartWorld());
		renderState.setPos(holder.getContainer().getMultipartPos());
		return new ModelKey(renderState);
	}

	@Override
	public void addDrops(ItemDropTarget target, LootContext context) {
		var list = new ArrayList<ItemStack>();
		cb.addPartDrops(list);
		target.dropAll(list);
	}

	@Override
	public InteractionResult onUse(Player player, InteractionHand hand, BlockHitResult hit) {
		return cb.activate(player, hand, hit) ? InteractionResult.sidedSuccess(player.level.isClientSide) : InteractionResult.PASS;
	}

	@Override
	public CompoundTag toTag() {
		var tag = new CompoundTag();
		cb.writeToNBT(tag);
		return tag;
	}

	@Override
	public boolean recolourBlock(Direction side, AEColor colour, Player who) {
		return cb.recolourBlock(side, colour, who);
	}

	@Nullable
	@Override
	public IGridNode getGridNode(Direction dir) {
		return cb.getGridNode(dir);
	}

	@Override
	public AECableType getCableConnectionType(Direction dir) {
		return cb.getCableConnectionType(dir);
	}

	@Override
	public float getCableConnectionLength(AECableType cable) {
		return cb.getCableConnectionLength(cable);
	}

	@Override
	public IFacadeContainer getFacadeContainer() {
		return cb.getFacadeContainer();
	}

	@Nullable
	@Override
	public IPart getPart(@Nullable Direction side) {
		return cb.getPart(side);
	}

	@Override
	public boolean canAddPart(ItemStack partStack, @Nullable Direction side) {
		// Check that the shape fits first
		if (partStack.getItem() instanceof IPartItem<?>partItem) {
			var part = partItem.createPart();
			var boxes = new ArrayList<AABB>();
			var collisionHelper = new BusCollisionHelper(boxes, side, false);
			part.getBoxes(collisionHelper);

			for (var otherPart : holder.getContainer().getAllParts()) {
				if (otherPart != null) {
					for (var otherAabb : otherPart.getShape().toAabbs()) {
						for (var myAabb : boxes) {
							if (otherAabb.intersects(myAabb)) {
								return false;
							}
						}
					}
				}
			}
		}
		return cb.canAddPart(partStack, side);
	}

	@Nullable
	@Override
	public <T extends IPart> T addPart(IPartItem<T> partItem, @Nullable Direction side, @Nullable Player owner) {
		return cb.addPart(partItem, side, owner);
	}

	@Nullable
	@Override
	public <T extends IPart> T replacePart(IPartItem<T> partItem, @Nullable Direction side, @Nullable Player owner, @Nullable InteractionHand hand) {
		return cb.replacePart(partItem, side, owner, hand);
	}

	@Override
	public void removePart(@Nullable Direction side) {
		cb.removePart(side);
	}

	@Override
	public void markForUpdate() {
		// TODO: weird light level logic?
		if (canUpdate && !getBlockEntity().getLevel().isClientSide()) {
			sendNetworkUpdate(this, NET_RENDER_DATA);
		}
	}

	@Override
	public DimensionalBlockPos getLocation() {
		return new DimensionalBlockPos(getBlockEntity());
	}

	@Override
	public BlockEntity getBlockEntity() {
		return holder.getContainer().getMultipartBlockEntity();
	}

	@Override
	public AEColor getColor() {
		return cb.getColor();
	}

	@Override
	public void clearContainer() {
		this.setCableBus(new CableBusContainer(this));
	}

	@Override
	public boolean isBlocked(Direction side) {
		var testShape = SIDE_TESTS[side.get3DDataValue()];

		for (var part : holder.getContainer().getAllParts()) {
			if (part != this) {
				if (Shapes.joinIsNotEmpty(part.getShape(), testShape, BooleanOp.AND)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public SelectedPart selectPartLocal(Vec3 pos) {
		return cb.selectPartLocal(pos);
	}

	@Override
	public void markForSave() {
		if (holder.getContainer().getMultipartWorld() != null) {
			holder.getContainer().markChunkDirty();
		}
	}

	@Override
	public void partChanged() {
		notifyNeighbors();
	}

	@Override
	public boolean hasRedstone() {
		return cb.hasRedstone();
	}

	@Override
	public boolean isEmpty() {
		return cb.isEmpty();
	}

	@Override
	public void cleanup() {
		holder.remove();
	}

	@Override
	public void notifyNeighbors() {
		var level = holder.getContainer().getMultipartWorld();
		if (level != null) {
			var pos = holder.getContainer().getMultipartPos();
			if (level.hasChunkAt(pos) && !CableBusContainer.isLoading()) {
				Platform.notifyBlocksOfNeighbors(level, pos);
			}
		}
	}

	@Override
	public boolean isInWorld() {
		return cb.isInWorld();
	}

	public static class ModelKey extends PartModelKey {
		public final CableBusRenderState renderState;

		private ModelKey(CableBusRenderState renderState) {
			this.renderState = renderState;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			ModelKey modelKey = (ModelKey) o;
			return Objects.equals(renderState, modelKey.renderState);
		}

		@Override
		public int hashCode() {
			return Objects.hash(renderState);
		}
	}
}
