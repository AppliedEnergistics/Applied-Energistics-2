package appeng.integration.modules.chisel;

import appeng.block.networking.BlockCableBus;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.chisel.common.config.Configurations;

import static team.chisel.client.handler.BlockSpeedHandler.speedupBlocks;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ChiselBlockSpeedHandler {
    @SideOnly(Side.CLIENT)
    private static MovementInput manualInputCheck;

    @Optional.Method(modid = "chisel")
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void speedupPlayer(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START && event.side.isClient() && event.player.onGround && event.player instanceof EntityPlayerSP) {
            if (manualInputCheck == null) {
                manualInputCheck = new MovementInputFromOptions(Minecraft.getMinecraft().gameSettings);
            }
            EntityPlayerSP player = (EntityPlayerSP) event.player;
            BlockPos blockPosBelow = new BlockPos(player.posX, player.posY - (1 / 16D), player.posZ);
            IBlockState below = player.getEntityWorld().getBlockState(blockPosBelow);
            if (below.getBlock() instanceof BlockCableBus) {
                IBlockState f = ((BlockCableBus) below.getBlock()).getFacadeState(Minecraft.getMinecraft().world, blockPosBelow, EnumFacing.UP);
                if (speedupBlocks.contains(f.getBlock())) {
                    manualInputCheck.updatePlayerMoveState();
                    if ((manualInputCheck.moveForward != 0 || manualInputCheck.moveStrafe != 0) && !player.isInWater()) {
                        player.motionX *= Configurations.concreteVelocityMult;
                        player.motionZ *= Configurations.concreteVelocityMult;
                    }
                }
            }
        }
    }
}
