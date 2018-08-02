package appeng.client.render.cablebus;

import appeng.api.AEApi;
import appeng.api.util.AEAxisAlignedBB;
import appeng.parts.misc.PartCableAnchor;
import appeng.thirdparty.codechicken.lib.model.CachedFormat;
import appeng.thirdparty.codechicken.lib.model.Quad;
import appeng.thirdparty.codechicken.lib.model.pipeline.BakedPipeline;
import appeng.thirdparty.codechicken.lib.model.pipeline.transformers.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.ForgeHooksClient;

import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;

/**
 * Created by covers1624 on 22/06/18.
 */
public class FacadeModelBuilder {

    private static ThreadLocal<BakedPipeline> pipelines = ThreadLocal.withInitial(() -> BakedPipeline.builder()//
            .addElement("clamper", QuadClamper.FACTORY)//
            .addElement("face_stripper", QuadFaceStripper.FACTORY)//
            .addElement("corner_kicker", QuadCornerKicker.FACTORY)//
            .addElement("interp", QuadReInterpolator.FACTORY)//
            .addElement("tinter", QuadTinter.FACTORY, false)//
            .addElement("transparent", QuadAlphaOverride.FACTORY, false, e -> e.setAlphaOverride(0x4C / 255F))//
            .build()//
    );
    private static ThreadLocal<Quad> collectors = ThreadLocal.withInitial(Quad::new);

    public static void buildFacadeQuads(BlockRenderLayer layer, CableBusRenderState renderState, long rand, List<BakedQuad> quads, Function<ResourceLocation, IBakedModel> modelLookup) {
        BakedPipeline pipeline = pipelines.get();
        Quad collectorQuad = collectors.get();
        boolean transparent = AEApi.instance().partHelper().getCableRenderMode().transparentFacades;
        Map<EnumFacing, FacadeRenderState> facadeStates = renderState.getFacades();
        List<AxisAlignedBB> partBoxes = renderState.getBoundingBoxes();
        Set<EnumFacing> sidesWithParts = renderState.getAttachments().keySet();
        IBlockAccess parentWorld = renderState.getWorld();
        BlockPos pos = renderState.getPos();
        BlockColors blockColors = Minecraft.getMinecraft().getBlockColors();
        boolean thinFacades = isUseThinFacades(partBoxes);

        for (Entry<EnumFacing, FacadeRenderState> entry : facadeStates.entrySet()) {
            EnumFacing side = entry.getKey();
            FacadeRenderState facadeRenderState = entry.getValue();
            boolean renderStilt = !sidesWithParts.contains(side);
            if (layer == BlockRenderLayer.CUTOUT && renderStilt) {
                for (ResourceLocation part : PartCableAnchor.FACADE_MODELS.getModels()) {
                    IBakedModel partModel = modelLookup.apply(part);
                    QuadRotator rotator = new QuadRotator();
                    quads.addAll(rotator.rotateQuads(gatherQuads(partModel, null, rand), side, EnumFacing.UP));
                }
            }
            if (transparent && layer != BlockRenderLayer.TRANSLUCENT) {
                continue;
            }

            IBlockState blockState = facadeRenderState.getSourceBlock();
            if (!transparent) {
                if (!blockState.getBlock().canRenderInLayer(blockState, layer)) {
                    continue;
                }
            }

            AxisAlignedBB facadeBox = getFacadeBox(side, thinFacades);
            AEAxisAlignedBB cutOutBox = getCutOutBox(facadeBox, partBoxes);
            List<AxisAlignedBB> holeStrips = getBoxes(facadeBox, cutOutBox, side.ordinal() >> 1);
            IBlockAccess facadeAccess = new FacadeBlockAccess(parentWorld, pos, side, blockState);

            BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();

            try {
                blockState = blockState.getActualState(facadeAccess, pos);
            } catch (Exception ignored) {
            }
            IBakedModel model = dispatcher.getModelForState(blockState);
            try {
                blockState = blockState.getBlock().getExtendedState(blockState, facadeAccess, pos);
            } catch (Exception ignored) {
            }

            List<BakedQuad> modelQuads = new ArrayList<>();
            if (transparent) {
                for (BlockRenderLayer forcedLayer : BlockRenderLayer.values()) {
                    if (blockState.getBlock().canRenderInLayer(blockState, forcedLayer)) {
                        ForgeHooksClient.setRenderLayer(forcedLayer);
                        modelQuads.addAll(gatherQuads(model, blockState, rand));
                    }
                }

                ForgeHooksClient.setRenderLayer(layer);
            } else {
                modelQuads.addAll(gatherQuads(model, blockState, rand));
            }

            if (modelQuads.isEmpty()) {
                continue;
            }

            QuadClamper clamper = pipeline.getElement("clamper", QuadClamper.class);
            QuadFaceStripper edgeStripper = pipeline.getElement("face_stripper", QuadFaceStripper.class);
            QuadTinter tinter = pipeline.getElement("tinter", QuadTinter.class);
            QuadCornerKicker kicker = pipeline.getElement("corner_kicker", QuadCornerKicker.class);

            int facadeMask = makeMask(facadeStates.keySet(), side);
            edgeStripper.setBounds(facadeBox);
            edgeStripper.setMask(facadeMask);

            kicker.setSide(side.ordinal());
            kicker.setFacadeMask(facadeMask);
            kicker.setBox(facadeBox);
            kicker.setThickness((thinFacades ? 1 : 2) / 16.0);

            for (BakedQuad quad : modelQuads) {
                CachedFormat format = CachedFormat.lookup(quad.getFormat());
                if (quad.hasTintIndex()) {
                    tinter.setTint(blockColors.colorMultiplier(blockState, facadeAccess, pos, quad.getTintIndex()));
                }
                for (AxisAlignedBB box : holeStrips) {
                    clamper.setClampBounds(box);
                    pipeline.reset(format);
                    collectorQuad.reset(format);
                    pipeline.setElementState("tinter", quad.hasTintIndex());
                    pipeline.setElementState("transparent", transparent);
                    pipeline.prepare(collectorQuad);

                    quad.pipe(pipeline);
                    if (collectorQuad.full) {
                        quads.add(collectorQuad.bake());
                    }
                }
            }
        }
    }

    private static List<BakedQuad> gatherQuads(IBakedModel model, IBlockState state, long rand) {
        List<BakedQuad> modelQuads = new ArrayList<>();
        for (EnumFacing face : EnumFacing.VALUES) {
            modelQuads.addAll(model.getQuads(state, face, rand));
        }
        modelQuads.addAll(model.getQuads(state, null, rand));
        return modelQuads;
    }

    /**
     * Given the actual facade bounding box, and the bounding boxes of all parts, determine the biggest union of AABB
     * that intersect with the
     * facade's bounding box. This AABB will need to be "cut out" when the facade is rendered.
     */
    @Nullable
    private static AEAxisAlignedBB getCutOutBox(AxisAlignedBB facadeBox, List<AxisAlignedBB> partBoxes) {
        AEAxisAlignedBB b = null;
        for (AxisAlignedBB bb : partBoxes) {
            if (bb.intersects(facadeBox)) {
                if (b == null) {
                    b = AEAxisAlignedBB.fromBounds(bb);
                } else {
                    b.maxX = Math.max(b.maxX, bb.maxX);
                    b.maxY = Math.max(b.maxY, bb.maxY);
                    b.maxZ = Math.max(b.maxZ, bb.maxZ);
                    b.minX = Math.min(b.minX, bb.minX);
                    b.minY = Math.min(b.minY, bb.minY);
                    b.minZ = Math.min(b.minZ, bb.minZ);
                }
            }
        }
        return b;
    }

    private static List<AxisAlignedBB> getBoxes(AxisAlignedBB fb, AEAxisAlignedBB hole, int axis) {
        if (hole == null) {
            return Collections.singletonList(fb);
        }
        //axis == 0, Up, Down
        //axis == 1, North, South
        //axis == 2, West, East
        List<AxisAlignedBB> boxes = new ArrayList<>();
        if (axis == 0) {//Up, Down, X, Z
            boxes.add(new AxisAlignedBB(fb.minX, fb.minY, fb.minZ, hole.minX, fb.maxY, fb.maxZ));
            boxes.add(new AxisAlignedBB(hole.maxX, fb.minY, fb.minZ, fb.maxX, fb.maxY, fb.maxZ));

            boxes.add(new AxisAlignedBB(hole.minX, fb.minY, fb.minZ, hole.maxX, fb.maxY, hole.minZ));
            boxes.add(new AxisAlignedBB(hole.minX, fb.minY, hole.maxZ, hole.maxX, fb.maxY, fb.maxZ));

        } else if (axis == 1) { //North, South, X, Y
            boxes.add(new AxisAlignedBB(fb.minX, fb.minY, fb.minZ, fb.maxX, hole.minY, fb.maxZ));
            boxes.add(new AxisAlignedBB(fb.minX, hole.maxY, fb.minZ, fb.maxX, fb.maxY, fb.maxZ));

            boxes.add(new AxisAlignedBB(fb.minX, hole.minY, fb.minZ, hole.minX, hole.maxY, fb.maxZ));
            boxes.add(new AxisAlignedBB(hole.maxX, hole.minY, fb.minZ, fb.maxX, hole.maxY, fb.maxZ));

        } else { // West, East, Y, Z
            boxes.add(new AxisAlignedBB(fb.minX, fb.minY, fb.minZ, fb.maxX, hole.minY, fb.maxZ));
            boxes.add(new AxisAlignedBB(fb.minX, hole.maxY, fb.minZ, fb.maxX, fb.maxY, fb.maxZ));

            boxes.add(new AxisAlignedBB(fb.minX, hole.minY, fb.minZ, fb.maxX, hole.maxY, hole.minZ));
            boxes.add(new AxisAlignedBB(fb.minX, hole.minY, hole.maxZ, fb.maxX, hole.maxY, fb.maxZ));
        }

        return boxes;
    }

    public static int makeMask(Set<EnumFacing> facadeSides, EnumFacing mySide) {
        int i = 0;
        for (EnumFacing side : facadeSides) {
            //Build a mask, we only care about directions that are not on our axis.
            if (side.getAxis() != mySide.getAxis()) {
                i = i | (1 << side.ordinal());
            }
        }
        return i;
    }

    /**
     * Determines if any of the part's bounding boxes intersects with the outside 2 voxel wide layer.
     * If so, we should use thinner facades (1 voxel deep).
     */
    private static boolean isUseThinFacades(List<AxisAlignedBB> partBoxes) {
        final double min = 2.0 / 16.0;
        final double max = 14.0 / 16.0;

        for (AxisAlignedBB bb : partBoxes) {
            int o = 0;
            o += bb.maxX > max ? 1 : 0;
            o += bb.maxY > max ? 1 : 0;
            o += bb.maxZ > max ? 1 : 0;
            o += bb.minX < min ? 1 : 0;
            o += bb.minY < min ? 1 : 0;
            o += bb.minZ < min ? 1 : 0;

            if (o >= 2) {
                return true;
            }
        }
        return false;
    }

    private static AxisAlignedBB getFacadeBox(EnumFacing side, boolean thinFacades) {
        double thickness = (thinFacades ? 1 : 2) / 16.0;

        switch (side) {
            case DOWN:
                return new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, thickness, 1.0);
            case EAST:
                return new AxisAlignedBB(1.0 - thickness, 0.0, 0.0, 1.0, 1.0, 1.0);
            case NORTH:
                return new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, thickness);
            case SOUTH:
                return new AxisAlignedBB(0.0, 0.0, 1.0 - thickness, 1.0, 1.0, 1.0);
            case UP:
                return new AxisAlignedBB(0.0, 1.0 - thickness, 0.0, 1.0, 1.0, 1.0);
            case WEST:
                return new AxisAlignedBB(0.0, 0.0, 0.0, thickness, 1.0, 1.0);
            default:
                throw new IllegalArgumentException("Unsupported face: " + side);
        }
    }
}
