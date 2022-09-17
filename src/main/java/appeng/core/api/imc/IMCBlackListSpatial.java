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

package appeng.core.api.imc;


import appeng.api.AEApi;
import appeng.core.AELog;
import appeng.core.api.IIMCProcessor;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCMessage;


public class IMCBlackListSpatial implements IIMCProcessor {

    @Override
    public void process(final IMCMessage m) {

        final ItemStack is = m.getItemStackValue();
        if (!is.isEmpty()) {
            final Block blk = Block.getBlockFromItem(is.getItem());
            if (blk != Blocks.AIR) {
                AEApi.instance().registries().movable().blacklistBlock(blk);
                return;
            }
        }

        AELog.info("Bad Block blacklisted by " + m.getSender());
    }
}
