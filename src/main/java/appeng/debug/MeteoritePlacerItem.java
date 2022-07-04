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

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import appeng.hooks.AEToolItem;
import appeng.items.AEBaseItem;
import appeng.util.InteractionUtil;
import appeng.util.Platform;
import appeng.worldgen.meteorite.CraterType;
import appeng.worldgen.meteorite.MeteoritePlacer;
import appeng.worldgen.meteorite.PlacedMeteoriteSettings;
import appeng.worldgen.meteorite.debug.MeteoriteSpawner;

public class MeteoritePlacerItem extends AEBaseItem implements AEToolItem {

    private static final String MODE_TAG = "mode";

    public MeteoritePlacerItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
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

            player.sendSystemMessage(Component.literal(craterType.name()));

            return InteractionResultHolder.success(itemStack);
        }

        return super.use(level, player, hand);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if (context.getLevel().isClientSide()) {
            return InteractionResult.PASS;
        }

        ServerPlayer player = (ServerPlayer) context.getPlayer();
        ServerLevel level = (ServerLevel) context.getLevel();
        BlockPos pos = context.getClickedPos();

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
        PlacedMeteoriteSettings spawned = spawner.trySpawnMeteoriteAtSuitableHeight(level, pos, coreRadius, craterType,
                pureCrater);

        if (spawned == null) {
            player.sendSystemMessage(Component.literal("Un-suitable Location."));
            return InteractionResult.FAIL;
        }

        // Since we don't know yet if the meteorite will be underground or not,
        // we have to assume maximum size
        int range = (int) Math.ceil((coreRadius * 2 + 5) * 5f);

        BoundingBox boundingBox = new BoundingBox(pos.getX() - range, pos.getY() - 10, pos.getZ() - range,
                pos.getX() + range, pos.getY() + 10, pos.getZ() + range);

        MeteoritePlacer.place(level, spawned, boundingBox, level.random);

        player.sendSystemMessage(Component.literal("Spawned at y=" + spawned.getPos().getY() + " range=" + range));

        // The placer will not send chunks to the player since it's used as part
        // of world-gen normally, so we'll have to do it ourselves. Since this
        // is a debug tool, we'll not care about being terribly efficient here
        ChunkPos.rangeClosed(new ChunkPos(spawned.getPos()), 1).forEach(cp -> {
            LevelChunk c = level.getChunk(cp.x, cp.z);
            player.connection.send(Platform.getFullChunkPacket(c));
        });

        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
