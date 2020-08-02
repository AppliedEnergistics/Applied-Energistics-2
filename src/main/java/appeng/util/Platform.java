/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.util;

import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnits;
import appeng.api.config.SearchBoxMode;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.SortOrder;
import appeng.api.definitions.IItemDefinition;
import appeng.api.definitions.IMaterials;
import appeng.api.features.AEFeature;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.implementations.items.IAEWrench;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.Api;
import appeng.core.AppEng;
import appeng.core.stats.AeStats;
import appeng.fluids.util.AEFluidStack;
import appeng.hooks.TickHandler;
import appeng.integration.abstraction.ReiFacade;
import appeng.me.GridAccessException;
import appeng.me.GridNode;
import appeng.me.helpers.AENetworkProxy;
import appeng.util.helpers.ItemComparisonHelper;
import appeng.util.helpers.P2PHelper;
import appeng.util.item.AEItemStack;
import appeng.util.prioritylist.IPartitionList;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Recipe;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.RayTraceContext;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * @author AlgorithmX2
 * @author thatsIch
 * @version rv2
 * @since rv0
 */
public class Platform {

    public static final int DEF_OFFSET = 16;

    private static final FabricLoader FABRIC = FabricLoader.getInstance();

    private static final boolean CLIENT_INSTALL = FABRIC.getEnvironmentType() == EnvType.CLIENT;

    /*
     * random source, use it for item drop locations...
     */
    private static final Random RANDOM_GENERATOR = new Random();

    private static final ItemComparisonHelper ITEM_COMPARISON_HELPER = new ItemComparisonHelper();

    public static ItemComparisonHelper itemComparisons() {
        return ITEM_COMPARISON_HELPER;
    }

    private static final P2PHelper P2P_HELPER = new P2PHelper();

    public static P2PHelper p2p() {
        return P2P_HELPER;
    }

    public static Random getRandom() {
        return RANDOM_GENERATOR;
    }

    public static float getRandomFloat() {
        return RANDOM_GENERATOR.nextFloat();
    }

    /**
     * This displays the value for encoded longs ( double *100 )
     *
     * @param n      to be formatted long value
     * @param isRate if true it adds a /t to the formatted string
     *
     * @return formatted long value
     */
    public static String formatPowerLong(final long n, final boolean isRate) {
        double p = ((double) n) / 100;

        final PowerUnits displayUnits = AEConfig.instance().getSelectedPowerUnit();
        p = PowerUnits.AE.convertTo(displayUnits, p);

        final String[] preFixes = { "k", "M", "G", "T", "P", "T", "P", "E", "Z", "Y" };
        String unitName = displayUnits.name();

        String level = "";
        int offset = 0;
        while (p > 1000 && offset < preFixes.length) {
            p /= 1000;
            level = preFixes[offset];
            offset++;
        }

        final DecimalFormat df = new DecimalFormat("#.##");
        return df.format(p) + ' ' + level + unitName + (isRate ? "/t" : "");
    }

    public static Direction crossProduct(final Direction forward, final Direction up) {
        final int west_x = forward.getOffsetY() * up.getOffsetZ() - forward.getOffsetZ() * up.getOffsetY();
        final int west_y = forward.getOffsetZ() * up.getOffsetX() - forward.getOffsetX() * up.getOffsetZ();
        final int west_z = forward.getOffsetX() * up.getOffsetY() - forward.getOffsetY() * up.getOffsetX();

        switch (west_x + west_y * 2 + west_z * 3) {
            case 1:
                return Direction.EAST;
            case -1:
                return Direction.WEST;

            case 2:
                return Direction.UP;
            case -2:
                return Direction.DOWN;

            case 3:
                return Direction.SOUTH;
            case -3:
                return Direction.NORTH;
        }

        // something is better then nothing?
        return Direction.NORTH;
    }

    /**
     * @return True if client-side classes (such as Renderers) are available.
     */
    public static boolean hasClientClasses() {
        return FABRIC.getEnvironmentType() == EnvType.CLIENT;
    }

    /*
     * returns true if the code is on the client.
     */
    public static boolean isClient() {
        return !AppEng.instance().isOnServerThread();
    }

    /*
     * returns true if client classes are available.
     */
    public static boolean isClientInstall() {
        return CLIENT_INSTALL;
    }

    public static boolean hasPermissions(final DimensionalCoord dc, final PlayerEntity player) {
        if (!dc.isInWorld(player.world)) {
            return false;
        }
        return player.world.canPlayerModifyAt(player, dc.getPos());
    }

    public static boolean checkPermissions(final PlayerEntity player, final Object accessInterface,
            SecurityPermissions requiredPermission, boolean notifyPlayer) {
        // FIXME: Check permissions...
        if (requiredPermission != null && accessInterface instanceof IActionHost) {
            final IGridNode gn = ((IActionHost) accessInterface).getActionableNode();
            if (gn != null) {
                final IGrid g = gn.getGrid();
                if (g != null) {
                    final boolean requirePower = false;
                    if (requirePower) {
                        final IEnergyGrid eg = g.getCache(IEnergyGrid.class);
                        if (!eg.isNetworkPowered()) {
                            // FIXME trace logging?
                            return false;
                        }
                    }

                    final ISecurityGrid sg = g.getCache(ISecurityGrid.class);
                    if (!sg.hasPermission(player, requiredPermission)) {
                        player.sendSystemMessage(
                                new TranslatableText("appliedenergistics2.permission_denied").formatted(Formatting.RED),
                                Util.NIL_UUID);
                        // FIXME trace logging?
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public static ItemStack[] getBlockDrops(final World w, final BlockPos pos) {
        // FIXME: Check assumption here and if this could only EVER be called with a
        // server world
        if (!(w instanceof ServerWorld)) {
            return new ItemStack[0];
        }

        ServerWorld serverWorld = (ServerWorld) w;

        final BlockState state = w.getBlockState(pos);
        final BlockEntity tileEntity = w.getBlockEntity(pos);

        List<ItemStack> out = Block.getDroppedStacks(state, serverWorld, pos, tileEntity);

        return out.toArray(new ItemStack[0]);
    }

    /*
     * Generates Item entities in the world similar to how items are generally
     * dropped.
     */
    public static void spawnDrops(final World w, final BlockPos pos, final List<ItemStack> drops) {
        if (isServer()) {
            for (final ItemStack i : drops) {
                if (!i.isEmpty()) {
                    if (i.getCount() > 0) {
                        final double offset_x = (getRandomInt() % 32 - 16) / 82;
                        final double offset_y = (getRandomInt() % 32 - 16) / 82;
                        final double offset_z = (getRandomInt() % 32 - 16) / 82;
                        final ItemEntity ei = new ItemEntity(w, 0.5 + offset_x + pos.getX(),
                                0.5 + offset_y + pos.getY(), 0.2 + offset_z + pos.getZ(), i.copy());
                        w.spawnEntity(ei);
                    }
                }
            }
        }
    }

    /*
     * returns true if the code is on the server.
     */
    public static boolean isServer() {
        return !isClient();
    }

    public static int getRandomInt() {
        return Math.abs(RANDOM_GENERATOR.nextInt());
    }

    @Environment(EnvType.CLIENT)
    public static List<Text> getTooltip(final Object o) {
        if (o == null) {
            return Collections.emptyList();
        }

        ItemStack itemStack = ItemStack.EMPTY;
        if (o instanceof AEItemStack) {
            final AEItemStack ais = (AEItemStack) o;
            return ais.getToolTip();
        } else if (o instanceof ItemStack) {
            itemStack = (ItemStack) o;
        } else {
            return Collections.emptyList();
        }

        try {
            TooltipContext tooltipFlag = MinecraftClient.getInstance().options.advancedItemTooltips
                    ? TooltipContext.Default.ADVANCED
                    : TooltipContext.Default.NORMAL;
            return itemStack.getTooltip(MinecraftClient.getInstance().player, tooltipFlag);
        } catch (final Exception errB) {
            return Collections.emptyList();
        }
    }

    public static String getModId(final IAEItemStack is) {
        if (is == null) {
            return "** Null";
        }

        final String n = ((AEItemStack) is).getModID();
        return n == null ? "** Null" : n;
    }

    public static String getModId(final IAEFluidStack fs) {
        if (fs == null || fs.getFluidStack().isEmpty()) {
            return "** Null";
        }

        final Identifier n = Registry.FLUID.getId(fs.getFluidStack().getRawFluid());
        return n == Registry.FLUID.getDefaultId() ? "** Null" : n.getNamespace();
    }

    public static String getModName(String modId) {
        return "" + Formatting.BLUE + Formatting.ITALIC
                + FABRIC.getModContainer(modId).map(mc -> mc.getMetadata().getName()).orElse(null);
    }

    public static Text getItemDisplayName(final Object o) {
        if (o == null) {
            return new LiteralText("** Null");
        }

        ItemStack itemStack = ItemStack.EMPTY;
        if (o instanceof AEItemStack) {
            final Text n = ((AEItemStack) o).getDisplayName();
            return n == null ? new LiteralText("** Null") : n;
        } else if (o instanceof ItemStack) {
            itemStack = (ItemStack) o;
        } else {
            return new LiteralText("**Invalid Object");
        }

        try {
            return itemStack.getName();
        } catch (final Exception errA) {
            try {
                return new TranslatableText(itemStack.getTranslationKey());
            } catch (final Exception errB) {
                return new LiteralText("** Exception");
            }
        }
    }

    public static Text getFluidDisplayName(Object o) {
        if (o == null) {
            return new LiteralText("** Null");
        }
        FluidVolume fluidStack = null;
        if (o instanceof AEFluidStack) {
            fluidStack = ((AEFluidStack) o).getFluidStack();
        } else if (o instanceof FluidVolume) {
            fluidStack = (FluidVolume) o;
        } else {
            return new LiteralText("**Invalid Object");
        }
        return fluidStack.getName();
    }

    public static boolean isWrench(final PlayerEntity player, final ItemStack eq, final BlockPos pos) {
        if (!eq.isEmpty()) {
            try {
                // TODO: Build Craft Wrench?
                /*
                 * if( eq.getItem() instanceof IToolWrench ) { IToolWrench wrench =
                 * (IToolWrench) eq.getItem(); return wrench.canWrench( player, x, y, z ); }
                 */

                // FIXME if( eq.getItem() instanceof cofh.api.item.IToolHammer )
                // FIXME {
                // FIXME return ( (cofh.api.item.IToolHammer) eq.getItem() ).isUsable( eq,
                // player, pos );
                // FIXME }
            } catch (final Throwable ignore) { // explodes without BC

            }

            if (eq.getItem() instanceof IAEWrench) {
                final IAEWrench wrench = (IAEWrench) eq.getItem();
                return wrench.canWrench(eq, player, pos);
            }
        }
        return false;
    }

    public static boolean isChargeable(final ItemStack i) {
        if (i.isEmpty()) {
            return false;
        }
        final Item it = i.getItem();
        if (it instanceof IAEItemPowerStorage) {
            return ((IAEItemPowerStorage) it).getPowerFlow(i) != AccessRestriction.READ;
        }
        return false;
    }

    /**
     * Returns a random element from the given collection.
     *
     * @return null if the collection is empty
     */
    @Nullable
    public static <T> T pickRandom(final Collection<T> outs) {
        if (outs.isEmpty()) {
            return null;
        }

        int index = RANDOM_GENERATOR.nextInt(outs.size());
        return Iterables.get(outs, index, null);
    }

    public static AEPartLocation rotateAround(final AEPartLocation forward, final AEPartLocation axis) {
        if (axis == AEPartLocation.INTERNAL || forward == AEPartLocation.INTERNAL) {
            return forward;
        }

        switch (forward) {
            case DOWN:
                switch (axis) {
                    case DOWN:
                        return forward;
                    case UP:
                        return forward;
                    case NORTH:
                        return AEPartLocation.EAST;
                    case SOUTH:
                        return AEPartLocation.WEST;
                    case EAST:
                        return AEPartLocation.NORTH;
                    case WEST:
                        return AEPartLocation.SOUTH;
                    default:
                        break;
                }
                break;
            case UP:
                switch (axis) {
                    case NORTH:
                        return AEPartLocation.WEST;
                    case SOUTH:
                        return AEPartLocation.EAST;
                    case EAST:
                        return AEPartLocation.SOUTH;
                    case WEST:
                        return AEPartLocation.NORTH;
                    default:
                        break;
                }
                break;
            case NORTH:
                switch (axis) {
                    case UP:
                        return AEPartLocation.WEST;
                    case DOWN:
                        return AEPartLocation.EAST;
                    case EAST:
                        return AEPartLocation.UP;
                    case WEST:
                        return AEPartLocation.DOWN;
                    default:
                        break;
                }
                break;
            case SOUTH:
                switch (axis) {
                    case UP:
                        return AEPartLocation.EAST;
                    case DOWN:
                        return AEPartLocation.WEST;
                    case EAST:
                        return AEPartLocation.DOWN;
                    case WEST:
                        return AEPartLocation.UP;
                    default:
                        break;
                }
                break;
            case EAST:
                switch (axis) {
                    case UP:
                        return AEPartLocation.NORTH;
                    case DOWN:
                        return AEPartLocation.SOUTH;
                    case NORTH:
                        return AEPartLocation.UP;
                    case SOUTH:
                        return AEPartLocation.DOWN;
                    default:
                        break;
                }
            case WEST:
                switch (axis) {
                    case UP:
                        return AEPartLocation.SOUTH;
                    case DOWN:
                        return AEPartLocation.NORTH;
                    case NORTH:
                        return AEPartLocation.DOWN;
                    case SOUTH:
                        return AEPartLocation.UP;
                    default:
                        break;
                }
            default:
                break;
        }
        return forward;
    }

    public static Direction rotateAround(final Direction forward, final Direction axis) {
        switch (forward) {
            case DOWN:
                switch (axis) {
                    case DOWN:
                        return forward;
                    case UP:
                        return forward;
                    case NORTH:
                        return Direction.EAST;
                    case SOUTH:
                        return Direction.WEST;
                    case EAST:
                        return Direction.NORTH;
                    case WEST:
                        return Direction.SOUTH;
                    default:
                        break;
                }
                break;
            case UP:
                switch (axis) {
                    case NORTH:
                        return Direction.WEST;
                    case SOUTH:
                        return Direction.EAST;
                    case EAST:
                        return Direction.SOUTH;
                    case WEST:
                        return Direction.NORTH;
                    default:
                        break;
                }
                break;
            case NORTH:
                switch (axis) {
                    case UP:
                        return Direction.WEST;
                    case DOWN:
                        return Direction.EAST;
                    case EAST:
                        return Direction.UP;
                    case WEST:
                        return Direction.DOWN;
                    default:
                        break;
                }
                break;
            case SOUTH:
                switch (axis) {
                    case UP:
                        return Direction.EAST;
                    case DOWN:
                        return Direction.WEST;
                    case EAST:
                        return Direction.DOWN;
                    case WEST:
                        return Direction.UP;
                    default:
                        break;
                }
                break;
            case EAST:
                switch (axis) {
                    case UP:
                        return Direction.NORTH;
                    case DOWN:
                        return Direction.SOUTH;
                    case NORTH:
                        return Direction.UP;
                    case SOUTH:
                        return Direction.DOWN;
                    default:
                        break;
                }
            case WEST:
                switch (axis) {
                    case UP:
                        return Direction.SOUTH;
                    case DOWN:
                        return Direction.NORTH;
                    case NORTH:
                        return Direction.DOWN;
                    case SOUTH:
                        return Direction.UP;
                    default:
                        break;
                }
            default:
                break;
        }
        return forward;
    }

    public static LookDirection getPlayerRay(final PlayerEntity playerIn) {
        // FIXME this currently cant be modded in Fabric, see
        // net.minecraft.server.network.ServerPlayerInteractionManager.processBlockBreakingAction
        double reachDistance = 36.0D;
        return getPlayerRay(playerIn, reachDistance);
    }

    public static LookDirection getPlayerRay(final PlayerEntity playerIn, double reachDistance) {
        final double x = playerIn.prevX + (playerIn.getX() - playerIn.prevX);
        final double y = playerIn.prevY + (playerIn.getY() - playerIn.prevY) + playerIn.getStandingEyeHeight();
        final double z = playerIn.prevZ + (playerIn.getZ() - playerIn.prevZ);

        final float playerPitch = playerIn.prevPitch + (playerIn.pitch - playerIn.prevPitch);
        final float playerYaw = playerIn.prevYaw + (playerIn.yaw - playerIn.prevYaw);

        final float yawRayX = MathHelper.sin(-playerYaw * 0.017453292f - (float) Math.PI);
        final float yawRayZ = MathHelper.cos(-playerYaw * 0.017453292f - (float) Math.PI);

        final float pitchMultiplier = -MathHelper.cos(-playerPitch * 0.017453292F);
        final float eyeRayY = MathHelper.sin(-playerPitch * 0.017453292F);
        final float eyeRayX = yawRayX * pitchMultiplier;
        final float eyeRayZ = yawRayZ * pitchMultiplier;

        final Vec3d from = new Vec3d(x, y, z);
        final Vec3d to = from.add(eyeRayX * reachDistance, eyeRayY * reachDistance, eyeRayZ * reachDistance);

        return new LookDirection(from, to);
    }

    public static HitResult rayTrace(final PlayerEntity p, final boolean hitBlocks, final boolean hitEntities) {
        final World w = p.getEntityWorld();

        final float f = 1.0F;
        float f1 = p.prevPitch + (p.pitch - p.prevPitch) * f;
        final float f2 = p.prevYaw + (p.yaw - p.prevYaw) * f;
        final double d0 = p.prevX + (p.getX() - p.prevX) * f;
        final double d1 = p.prevY + (p.getY() - p.prevY) * f + 1.62D - p.getHeightOffset();
        final double d2 = p.prevZ + (p.getZ() - p.prevZ) * f;
        final Vec3d vec3 = new Vec3d(d0, d1, d2);
        final float f3 = MathHelper.cos(-f2 * 0.017453292F - (float) Math.PI);
        final float f4 = MathHelper.sin(-f2 * 0.017453292F - (float) Math.PI);
        final float f5 = -MathHelper.cos(-f1 * 0.017453292F);
        final float f6 = MathHelper.sin(-f1 * 0.017453292F);
        final float f7 = f4 * f5;
        final float f8 = f3 * f5;
        final double d3 = 32.0D;

        final Vec3d vec31 = vec3.add(f7 * d3, f6 * d3, f8 * d3);

        final Box bb = new Box(Math.min(vec3.x, vec31.x), Math.min(vec3.y, vec31.y), Math.min(vec3.z, vec31.z),
                Math.max(vec3.x, vec31.x), Math.max(vec3.y, vec31.y), Math.max(vec3.z, vec31.z)).expand(16, 16, 16);

        Entity entity = null;
        double closest = 9999999.0D;
        if (hitEntities) {
            final List<Entity> list = w.getOtherEntities(p, bb);

            for (final Entity entity1 : list) {
                if (entity1.isAlive() && entity1 != p && !(entity1 instanceof ItemEntity)) {
                    // prevent killing / flying of mounts.
                    if (entity1.isConnectedThroughVehicle(p)) {
                        continue;
                    }

                    f1 = 0.3F;
                    // FIXME: Three different bounding boxes available, should double-check
                    final Box boundingBox = entity1.getBoundingBox().expand(f1, f1, f1);
                    final Vec3d rtResult = boundingBox.rayTrace(vec3, vec31).orElse(null);

                    if (rtResult != null) {
                        final double nd = vec3.squaredDistanceTo(rtResult);

                        if (nd < closest) {
                            entity = entity1;
                            closest = nd;
                        }
                    }
                }
            }
        }

        HitResult pos = null;
        Vec3d vec = null;

        if (hitBlocks) {
            vec = new Vec3d(d0, d1, d2);
            // FIXME: passing p as entity here might be incorrect
            pos = w.rayTrace(new RayTraceContext(vec3, vec31, RayTraceContext.ShapeType.COLLIDER,
                    RayTraceContext.FluidHandling.ANY, p));
        }

        if (entity != null && pos != null && pos.getPos().squaredDistanceTo(vec) > closest) {
            pos = new EntityHitResult(entity);
        } else if (entity != null && pos == null) {
            pos = new EntityHitResult(entity);
        }

        return pos;
    }

    public static <T extends IAEStack<T>> T poweredExtraction(final IEnergySource energy, final IMEInventory<T> cell,
            final T request, final IActionSource src) {
        return poweredExtraction(energy, cell, request, src, Actionable.MODULATE);
    }

    public static <T extends IAEStack<T>> T poweredExtraction(final IEnergySource energy, final IMEInventory<T> cell,
            final T request, final IActionSource src, final Actionable mode) {
        Preconditions.checkNotNull(energy);
        Preconditions.checkNotNull(cell);
        Preconditions.checkNotNull(request);
        Preconditions.checkNotNull(src);
        Preconditions.checkNotNull(mode);

        final T possible = cell.extractItems(request.copy(), Actionable.SIMULATE, src);

        long retrieved = 0;
        if (possible != null) {
            retrieved = possible.getStackSize();
        }

        final double energyFactor = Math.max(1.0, cell.getChannel().transferFactor());
        final double availablePower = energy.extractAEPower(retrieved / energyFactor, Actionable.SIMULATE,
                PowerMultiplier.CONFIG);
        final long itemToExtract = Math.min((long) ((availablePower * energyFactor) + 0.9), retrieved);

        if (itemToExtract > 0) {
            if (mode == Actionable.MODULATE) {
                energy.extractAEPower(retrieved / energyFactor, Actionable.MODULATE, PowerMultiplier.CONFIG);
                possible.setStackSize(itemToExtract);
                final T ret = cell.extractItems(possible, Actionable.MODULATE, src);

                if (ret != null) {
                    src.player()
                            .ifPresent(player -> AeStats.ItemsExtracted.addToPlayer(player, (int) ret.getStackSize()));
                }
                return ret;
            } else {
                return possible.setStackSize(itemToExtract);
            }
        }

        return null;
    }

    public static <T extends IAEStack<T>> T poweredInsert(final IEnergySource energy, final IMEInventory<T> cell,
            final T input, final IActionSource src) {
        return poweredInsert(energy, cell, input, src, Actionable.MODULATE);
    }

    /**
     * @return The remainder (non-inserted) _or_ null if everything was inserted.
     */
    public static <T extends IAEStack<T>> T poweredInsert(final IEnergySource energy, final IMEInventory<T> cell,
            final T input, final IActionSource src, final Actionable mode) {
        Preconditions.checkNotNull(energy);
        Preconditions.checkNotNull(cell);
        Preconditions.checkNotNull(input);
        Preconditions.checkNotNull(src);
        Preconditions.checkNotNull(mode);

        final T overflow = cell.injectItems(input.copy(), Actionable.SIMULATE, src);

        long transferAmount = input.getStackSize();
        if (overflow != null) {
            transferAmount -= overflow.getStackSize();
        }

        final double energyFactor = Math.max(1.0, cell.getChannel().transferFactor());
        final double availablePower = energy.extractAEPower(transferAmount / energyFactor, Actionable.SIMULATE,
                PowerMultiplier.CONFIG);
        final long itemToAdd = Math.min((long) ((availablePower * energyFactor) + 0.9), transferAmount);

        if (itemToAdd > 0) {
            if (mode == Actionable.MODULATE) {
                energy.extractAEPower(transferAmount / energyFactor, Actionable.MODULATE, PowerMultiplier.CONFIG);
                if (itemToAdd < input.getStackSize()) {
                    final long original = input.getStackSize();
                    final T leftover = input.copy();
                    final T split = input.copy();

                    leftover.decStackSize(itemToAdd);
                    split.setStackSize(itemToAdd);
                    leftover.add(cell.injectItems(split, Actionable.MODULATE, src));

                    src.player().ifPresent(player -> {
                        final long diff = original - leftover.getStackSize();
                        AeStats.ItemsInserted.addToPlayer(player, (int) diff);
                    });

                    return leftover;
                }

                final T ret = cell.injectItems(input, Actionable.MODULATE, src);

                src.player().ifPresent(player -> {
                    final long diff = ret == null ? input.getStackSize() : input.getStackSize() - ret.getStackSize();
                    AeStats.ItemsInserted.addToPlayer(player, (int) diff);
                });

                return ret;
            } else {
                final T ret = input.copy().setStackSize(input.getStackSize() - itemToAdd);
                return (ret != null && ret.getStackSize() > 0) ? ret : null;
            }
        }

        return input;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void postChanges(final IStorageGrid gs, final ItemStack removed, final ItemStack added,
            final IActionSource src) {
        for (final IStorageChannel<?> chan : Api.instance().storage().storageChannels()) {
            final IItemList<?> myChanges = chan.createList();

            if (!removed.isEmpty()) {
                final IMEInventory myInv = Api.instance().registries().cell().getCellInventory(removed, null, chan);
                if (myInv != null) {
                    myInv.getAvailableItems(myChanges);
                    for (final IAEStack is : myChanges) {
                        is.setStackSize(-is.getStackSize());
                    }
                }
            }
            if (!added.isEmpty()) {
                final IMEInventory myInv = Api.instance().registries().cell().getCellInventory(added, null, chan);
                if (myInv != null) {
                    myInv.getAvailableItems(myChanges);
                }

            }
            gs.postAlterationOfStoredItems(chan, myChanges, src);
        }
    }

    public static <T extends IAEStack<T>> void postListChanges(final IItemList<T> before, final IItemList<T> after,
            final IMEMonitorHandlerReceiver<T> meMonitorPassthrough, final IActionSource source) {
        final List<T> changes = new ArrayList<>();

        for (final T is : before) {
            is.setStackSize(-is.getStackSize());
        }

        for (final T is : after) {
            before.add(is);
        }

        for (final T is : before) {
            if (is.getStackSize() != 0) {
                changes.add(is);
            }
        }

        if (!changes.isEmpty()) {
            meMonitorPassthrough.postChange(null, changes, source);
        }
    }

    public static boolean securityCheck(final GridNode a, final GridNode b) {
        if (a.getLastSecurityKey() == -1 && b.getLastSecurityKey() == -1) {
            return true;
        } else if (a.getLastSecurityKey() == b.getLastSecurityKey()) {
            return true;
        }

        final boolean a_isSecure = isPowered(a.getGrid()) && a.getLastSecurityKey() != -1;
        final boolean b_isSecure = isPowered(b.getGrid()) && b.getLastSecurityKey() != -1;

        if (AEConfig.instance().isFeatureEnabled(AEFeature.LOG_SECURITY_AUDITS)) {
            final String locationA = a.getGridBlock().isWorldAccessible() ? a.getGridBlock().getLocation().toString()
                    : "notInWorld";
            final String locationB = b.getGridBlock().isWorldAccessible() ? b.getGridBlock().getLocation().toString()
                    : "notInWorld";

            AELog.info(
                    "Audit: Node A [isSecure=%b, key=%d, playerID=%d, location={%s}] vs Node B[isSecure=%b, key=%d, playerID=%d, location={%s}]",
                    a_isSecure, a.getLastSecurityKey(), a.getPlayerID(), locationA, b_isSecure, b.getLastSecurityKey(),
                    b.getPlayerID(), locationB);
        }

        // can't do that son...
        if (a_isSecure && b_isSecure) {
            return false;
        }

        if (!a_isSecure && b_isSecure) {
            return checkPlayerPermissions(b.getGrid(), a.getPlayerID());
        }

        if (a_isSecure && !b_isSecure) {
            return checkPlayerPermissions(a.getGrid(), b.getPlayerID());
        }

        return true;
    }

    private static boolean isPowered(final IGrid grid) {
        if (grid == null) {
            return false;
        }

        final IEnergyGrid eg = grid.getCache(IEnergyGrid.class);
        return eg.isNetworkPowered();
    }

    private static boolean checkPlayerPermissions(final IGrid grid, final int playerID) {
        if (grid == null) {
            return true;
        }

        final ISecurityGrid gs = grid.getCache(ISecurityGrid.class);

        if (gs == null) {
            return true;
        }

        if (!gs.isAvailable()) {
            return true;
        }

        return gs.hasPermission(playerID, SecurityPermissions.BUILD);
    }

    public static void configurePlayer(final PlayerEntity player, final AEPartLocation side, final BlockEntity tile) {
        float pitch = 0.0f;
        float yaw = 0.0f;
        // player.yOffset = 1.8f;

        switch (side) {
            case DOWN:
                pitch = 90.0f;
                // player.getOffsetY() = -1.8f;
                break;
            case EAST:
                yaw = -90.0f;
                break;
            case NORTH:
                yaw = 180.0f;
                break;
            case SOUTH:
                yaw = 0.0f;
                break;
            case INTERNAL:
                break;
            case UP:
                pitch = 90.0f;
                break;
            case WEST:
                yaw = 90.0f;
                break;
        }

        player.refreshPositionAndAngles(tile.getPos().getX() + 0.5, tile.getPos().getY() + 0.5,
                tile.getPos().getZ() + 0.5, yaw, pitch);
    }

    public static boolean canAccess(final AENetworkProxy gridProxy, final IActionSource src) {
        try {
            if (src.player().isPresent()) {
                return gridProxy.getSecurity().hasPermission(src.player().get(), SecurityPermissions.BUILD);
            } else if (src.machine().isPresent()) {
                final IActionHost te = src.machine().get();
                final IGridNode n = te.getActionableNode();
                if (n == null) {
                    return false;
                }

                final int playerID = n.getPlayerID();
                return gridProxy.getSecurity().hasPermission(playerID, SecurityPermissions.BUILD);
            } else {
                return false;
            }
        } catch (final GridAccessException gae) {
            return false;
        }
    }

    public static ItemStack extractItemsByRecipe(final IEnergySource energySrc, final IActionSource mySrc,
            final IMEMonitor<IAEItemStack> src, final World w, final Recipe<CraftingInventory> r,
            final ItemStack output, final CraftingInventory ci, final ItemStack providedTemplate, final int slot,
            final IItemList<IAEItemStack> items, final Actionable realForFake,
            final IPartitionList<IAEItemStack> filter) {
        if (energySrc.extractAEPower(1, Actionable.SIMULATE, PowerMultiplier.CONFIG) > 0.9) {
            if (providedTemplate == null) {
                return ItemStack.EMPTY;
            }

            final AEItemStack ae_req = AEItemStack.fromItemStack(providedTemplate);
            ae_req.setStackSize(1);

            if (filter == null || filter.isListed(ae_req)) {
                final IAEItemStack ae_ext = src.extractItems(ae_req, realForFake, mySrc);
                if (ae_ext != null) {
                    final ItemStack extracted = ae_ext.createItemStack();
                    if (!extracted.isEmpty()) {
                        energySrc.extractAEPower(1, realForFake, PowerMultiplier.CONFIG);
                        return extracted;
                    }
                }
            }

            final boolean checkFuzzy = /*
                                        * FIXME ae_req.getOre().isPresent() || FIXME providedTemplate.getDamage() ==
                                        * OreDictionary.WILDCARD_VALUE ||
                                        */ providedTemplate.hasTag() || providedTemplate.isDamageable();

            if (items != null && checkFuzzy) {
                for (final IAEItemStack x : items) {
                    final ItemStack sh = x.getDefinition();
                    if ((Platform.itemComparisons().isEqualItemType(providedTemplate,
                            sh) /* FIXME || ae_req.sameOre( x ) */ ) && !ItemStack.areItemsEqual(sh, output)) { // Platform.isSameItemType(
                                                                                                                // sh,
                                                                                                                // providedTemplate
                                                                                                                // )
                        final ItemStack cp = sh.copy();
                        cp.setCount(1);
                        ci.setStack(slot, cp);
                        if (r.matches(ci, w) && ItemStack.areItemsEqual(r.craft(ci), output)) {
                            final IAEItemStack ax = x.copy();
                            ax.setStackSize(1);
                            if (filter == null || filter.isListed(ax)) {
                                final IAEItemStack ex = src.extractItems(ax, realForFake, mySrc);
                                if (ex != null) {
                                    energySrc.extractAEPower(1, realForFake, PowerMultiplier.CONFIG);
                                    return ex.createItemStack();
                                }
                            }
                        }
                        ci.setStack(slot, providedTemplate);
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * Gets the container item for the given item or EMPTY. A container item is what
     * remains when the item is used for crafting, i.E. the empty bucket for a
     * bucket of water.
     */
    public static ItemStack getRecipeRemainder(final ItemStack stackInSlot) {
        if (stackInSlot == null) {
            return ItemStack.EMPTY;
        }

        final Item i = stackInSlot.getItem();
        if (i == null || !i.hasRecipeRemainder()) {
            if (stackInSlot.getCount() > 1) {
                stackInSlot.setCount(stackInSlot.getCount() - 1);
                return stackInSlot;
            }
            return ItemStack.EMPTY;
        }

        ItemStack ci = new ItemStack(i.getRecipeRemainder());
        if (!ci.isEmpty() && ci.isDamageable() && ci.getDamage() == ci.getMaxDamage()) {
            ci = ItemStack.EMPTY;
        }

        return ci;
    }

    public static void notifyBlocksOfNeighbors(final World world, final BlockPos pos) {
        if (!world.isClient) {
            TickHandler.instance().addCallable(world, new BlockUpdate(pos));
        }
    }

    public static boolean canRepair(final AEFeature type, final ItemStack a, final ItemStack b) {
        if (b.isEmpty() || a.isEmpty()) {
            return false;
        }

        if (type == AEFeature.CERTUS_QUARTZ_TOOLS) {
            final IItemDefinition certusQuartzCrystal = Api.instance().definitions().materials().certusQuartzCrystal();

            return certusQuartzCrystal.isSameAs(b);
        }

        if (type == AEFeature.NETHER_QUARTZ_TOOLS) {
            return Items.QUARTZ == b.getItem();
        }

        return false;
    }

    public static float getEyeOffset(final PlayerEntity player) {
        assert player.world.isClient : "Valid only on client";
        // FIXME: The entire premise of this seems broken
        return (float) player.getEyeY() - 1.62F;
    }

    public static boolean isRecipePrioritized(final ItemStack what) {
        final IMaterials materials = Api.instance().definitions().materials();

        boolean isPurified = materials.purifiedCertusQuartzCrystal().isSameAs(what);
        isPurified |= materials.purifiedFluixCrystal().isSameAs(what);
        isPurified |= materials.purifiedNetherQuartzCrystal().isSameAs(what);

        return isPurified;
    }

    public static boolean isSortOrderAvailable(SortOrder order) {
        return true;
    }

    public static boolean isSearchModeAvailable(SearchBoxMode mode) {
        if (mode.isRequiresJei()) {
            return ReiFacade.instance().isEnabled();
        }
        return true;
    }

    public static ItemStack copyStackWithSize(ItemStack stack, int size) {
        if (!stack.isEmpty() && size > 0) {
            ItemStack copy = stack.copy();
            copy.setCount(size);
            return copy;
        }
        return ItemStack.EMPTY;
    }

    public static boolean canStack(ItemStack a, ItemStack b) {
        return itemComparisons().isSameItem(a, b);
    }

}
