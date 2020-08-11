/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.debug;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import appeng.hooks.AEToolItem;
import appeng.items.AEBaseItem;
import appeng.util.Platform;
import appeng.worldgen.meteorite.CraterType;
import appeng.worldgen.meteorite.MeteoritePlacer;
import appeng.worldgen.meteorite.PlacedMeteoriteSettings;
import appeng.worldgen.meteorite.debug.MeteoriteSpawner;

public class MeteoritePlacerItem extends AEBaseItem implements AEToolItem {

    private static final String MODE_TAG = "mode";

    public MeteoritePlacerItem(Settings properties) {
        super(properties);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (world.isClient()) {
            return TypedActionResult.pass(player.getStackInHand(hand));
        }

        if (player.isSneaking()) {
            final ItemStack itemStack = player.getStackInHand(hand);
            final CompoundTag tag = itemStack.getOrCreateTag();

            if (tag.contains(MODE_TAG)) {
                final byte mode = tag.getByte("mode");
                tag.putByte(MODE_TAG, (byte) ((mode + 1) % CraterType.values().length));
            } else {
                tag.putByte(MODE_TAG, (byte) CraterType.NORMAL.ordinal());
            }

            CraterType craterType = CraterType.values()[tag.getByte(MODE_TAG)];

            player.sendSystemMessage(new LiteralText(craterType.name()), Util.NIL_UUID);

            return TypedActionResult.success(itemStack);
        }

        return super.use(world, player, hand);
    }

    @Override
    public ActionResult onItemUseFirst(ItemStack stack, ItemUsageContext context) {
        if (context.getWorld().isClient()) {
            return ActionResult.PASS;
        }

        ServerPlayerEntity player = (ServerPlayerEntity) context.getPlayer();
        ServerWorld world = (ServerWorld) context.getWorld();
        BlockPos pos = context.getBlockPos();

        if (player == null) {
            return ActionResult.PASS;
        }

        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(MODE_TAG)) {
            tag.putByte(MODE_TAG, (byte) CraterType.NORMAL.ordinal());
        }

        // See MeteoriteStructure for original code
        float coreRadius = (Platform.getRandomFloat() * 6.0f) + 2;
        boolean pureCrater = Platform.getRandomFloat() > 0.5f;
        CraterType craterType = CraterType.values()[tag.getByte(MODE_TAG)];

        MeteoriteSpawner spawner = new MeteoriteSpawner();
        PlacedMeteoriteSettings spawned = spawner.trySpawnMeteoriteAtSuitableHeight(world, pos, coreRadius, craterType,
                pureCrater, false);

        if (spawned == null) {
            player.sendMessage(new LiteralText("Un-suitable Location."), false);
            return ActionResult.FAIL;
        }

        // Since we don't know yet if the meteorite will be underground or not,
        // we have to assume maximum size
        int range = (int) Math.ceil((coreRadius * 2 + 5) * 5f);

        BlockBox boundingBox = new BlockBox(pos.getX() - range, pos.getY(), pos.getZ() - range, pos.getX() + range,
                pos.getY(), pos.getZ() + range);

        final MeteoritePlacer placer = new MeteoritePlacer(world, spawned, boundingBox);
        placer.place();

        player.sendMessage(new LiteralText("Spawned at y=" + spawned.getPos().getY() + " range=" + range
                + " biomeCategory=" + world.getBiome(pos).getCategory()), false);

        // The placer will not send chunks to the player since it's used as part
        // of world-gen normally, so we'll have to do it ourselves. Since this
        // is a debug tool, we'll not care about being terribly efficient here
        ChunkPos.stream(new ChunkPos(spawned.getPos()), 2).forEach(cp -> {
            WorldChunk c = world.getChunk(cp.x, cp.z);
            player.networkHandler.sendPacket(new ChunkDataS2CPacket(c, 65535)); // 65535 == full chunk
        });

        return ActionResult.SUCCESS;
    }
}
