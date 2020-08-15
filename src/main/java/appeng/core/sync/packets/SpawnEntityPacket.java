package appeng.core.sync.packets;

import java.util.UUID;
import java.util.function.Consumer;

import io.netty.buffer.Unpooled;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;

import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;

// This is essentially a copy of EntitySpawnS2CPacket, supporting custom entities
public class SpawnEntityPacket extends BasePacket {
    private int id;
    private UUID uuid;
    private double x;
    private double y;
    private double z;
    private int velocityX;
    private int velocityY;
    private int velocityZ;
    private int pitch;
    private int yaw;
    private EntityType<?> entityTypeId;
    private PacketByteBuf extraData;

    public SpawnEntityPacket(PacketByteBuf buf) {
        this.id = buf.readVarInt();
        this.uuid = buf.readUuid();
        this.entityTypeId = Registry.ENTITY_TYPE.get(buf.readVarInt());
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.pitch = buf.readByte();
        this.yaw = buf.readByte();
        this.velocityX = buf.readShort();
        this.velocityY = buf.readShort();
        this.velocityZ = buf.readShort();
        this.extraData = new PacketByteBuf(buf.copy());
    }

    public SpawnEntityPacket(int id, UUID uuid, double x, double y, double z, float pitch, float yaw,
            EntityType<?> entityTypeId, Vec3d velocity, Consumer<PacketByteBuf> extraSpawnData) {
        this.id = id;
        this.uuid = uuid;
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = MathHelper.floor(pitch * 256.0F / 360.0F);
        this.yaw = MathHelper.floor(yaw * 256.0F / 360.0F);
        this.entityTypeId = entityTypeId;
        this.velocityX = (int) (MathHelper.clamp(velocity.x, -3.9D, 3.9D) * 8000.0D);
        this.velocityY = (int) (MathHelper.clamp(velocity.y, -3.9D, 3.9D) * 8000.0D);
        this.velocityZ = (int) (MathHelper.clamp(velocity.z, -3.9D, 3.9D) * 8000.0D);

        final PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
        data.writeInt(this.getPacketID());
        data.writeVarInt(this.id);
        data.writeUuid(this.uuid);
        data.writeVarInt(Registry.ENTITY_TYPE.getRawId(this.entityTypeId));
        data.writeDouble(this.x);
        data.writeDouble(this.y);
        data.writeDouble(this.z);
        data.writeByte(this.pitch);
        data.writeByte(this.yaw);
        data.writeShort(this.velocityX);
        data.writeShort(this.velocityY);
        data.writeShort(this.velocityZ);
        extraSpawnData.accept(data);

        this.configureWrite(data);
    }

    public SpawnEntityPacket(Entity entity, Consumer<PacketByteBuf> extraSpawnData) {
        this(entity.getEntityId(), entity.getUuid(), entity.getX(), entity.getY(), entity.getZ(), entity.pitch,
                entity.yaw, entity.getType(), entity.getVelocity(), extraSpawnData);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void clientPacketData(INetworkInfo network, PlayerEntity player) {
        ClientWorld world = (ClientWorld) player.world;

        EntityType<?> entityType = entityTypeId;
        Entity entity = entityType.create(world);

        if (entity != null) {
            entity.updateTrackedPosition(x, y, z);
            // NOTE: Vanilla doesn't do this for all spawned entities, but why transfer the
            // velocity???
            entity.setVelocityClient(velocityX, velocityY, velocityZ);
            entity.refreshPositionAfterTeleport(x, y, z);
            entity.pitch = (float) (pitch * 360) / 256.0F;
            entity.yaw = (float) (yaw * 360) / 256.0F;
            entity.setEntityId(id);
            entity.setUuid(uuid);

            if (entity instanceof ICustomEntity) {
                ((ICustomEntity) entity).readAdditionalSpawnData(extraData);
            }

            world.addEntity(id, entity);
        }
    }

    public static <T extends Entity & ICustomEntity> Packet<?> create(T entity) {
        // Not having this typed local variable will lead to a JVM bug because
        // the method reference will have a target type of "Entity" and not
        // "ICustomEntity"
        @SuppressWarnings("UnnecessaryLocalVariable")
        ICustomEntity customEntity = entity;
        SpawnEntityPacket packet = new SpawnEntityPacket(entity, customEntity::writeAdditionalSpawnData);
        return packet.toPacket(NetworkSide.CLIENTBOUND);
    }

}
