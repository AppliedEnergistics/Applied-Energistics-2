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

import net.minecraft.Util;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacket;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.server.level.ServerLevel;

import appeng.items.AEBaseItem;
import appeng.util.InteractionUtil;
import appeng.util.Platform;
import appeng.worldgen.meteorite.CraterType;
import appeng.worldgen.meteorite.MeteoritePlacer;
import appeng.worldgen.meteorite.PlacedMeteoriteSettings;
import appeng.worldgen.meteorite.debug.MeteoriteSpawner;

public class MeteoritePlacerItem extends AEBaseItem {

    private static final String MODE_TAG = "mode";

    public MeteoritePlacerItem(net.minecraft.world.item.Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if (world.isClientSide()) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }

        if (InteractionUtil.isInAlternateUseMode(player)) {
            final ItemStack itemStack = player.getItemInHand(hand);
            final CompoundTag tag = itemStack.getOrCreateTag();

            if (tag.contains(MODE_TAG)) {
                final byte mode = tag.getByte("mode");
                tag.putByte(MODE_TAG, (byte) ((mode + 1) % CraterType.values().length));
            } else {
                tag.putByte(MODE_TAG, (byte) CraterType.NORMAL.ordinal());
            }

            CraterType craterType = CraterType.values()[tag.getByte(MODE_TAG)];

            player.sendMessage(new TextComponent(craterType.name()), net.minecraft.Util.NIL_UUID);

            return InteractionResultHolder.success(itemStack);
        }

        return super.use(world, player, hand);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if (context.getLevel().isClientSide()) {
            return InteractionResult.PASS;
        }

        ServerPlayer player = (ServerPlayer) context.getPlayer();
        ServerLevel world = (ServerLevel) context.getLevel();
        net.minecraft.core.BlockPos pos = context.getClickedPos();

        if (player == null) {
            return InteractionResult.PASS;
        }

        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(MODE_TAG)) {
            tag.putByte(MODE_TAG, (byte) CraterType.NORMAL.ordinal());
        }

        // See MeteoriteStructure for original code
        float coreRadius = Platform.getRandomFloat() * 6.0f + 2;
        boolean pureCrater = Platform.getRandomFloat() > 0.5f;
        CraterType craterType = CraterType.values()[tag.getByte(MODE_TAG)];

        MeteoriteSpawner spawner = new MeteoriteSpawner();
        PlacedMeteoriteSettings spawned = spawner.trySpawnMeteoriteAtSuitableHeight(world, pos, coreRadius, craterType,
                pureCrater, false);

        if (spawned == null) {
            player.sendMessage(new TextComponent("Un-suitable Location."), net.minecraft.Util.NIL_UUID);
            return InteractionResult.FAIL;
        }

        // Since we don't know yet if the meteorite will be underground or not,
        // we have to assume maximum size
        int range = (int) Math.ceil((coreRadius * 2 + 5) * 5f);

        BoundingBox boundingBox = new BoundingBox(pos.getX() - range, pos.getY(), pos.getZ() - range,
                pos.getX() + range, pos.getY(), pos.getZ() + range);

        final MeteoritePlacer placer = new MeteoritePlacer(world, spawned, boundingBox);
        placer.place();

        player.sendMessage(new TextComponent("Spawned at y=" + spawned.getPos().getY() + " range=" + range
                + " biomeCategory=" + world.getBiome(pos).getBiomeCategory()), Util.NIL_UUID);

        // The placer will not send chunks to the player since it's used as part
        // of world-gen normally, so we'll have to do it ourselves. Since this
        // is a debug tool, we'll not care about being terribly efficient here
        ChunkPos.rangeClosed(new net.minecraft.world.level.ChunkPos(spawned.getPos()), 1).forEach(cp -> {
            LevelChunk c = world.getChunk(cp.x, cp.z);
            player.connection.send(new ClientboundLevelChunkPacket(c, 65535)); // 65535 == full chunk
        });

        return InteractionResult.sidedSuccess(world.isClientSide());
    }
}
