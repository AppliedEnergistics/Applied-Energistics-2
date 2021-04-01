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
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.PacketDirection;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
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
    private PacketBuffer extraData;

    public SpawnEntityPacket(PacketBuffer buf) {
        this.id = buf.readVarInt();
        this.uuid = buf.readUniqueId();
        this.entityTypeId = Registry.ENTITY_TYPE.getByValue(buf.readVarInt());
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.pitch = buf.readByte();
        this.yaw = buf.readByte();
        this.velocityX = buf.readShort();
        this.velocityY = buf.readShort();
        this.velocityZ = buf.readShort();
        this.extraData = new PacketBuffer(buf.copy());
    }

    public SpawnEntityPacket(int id, UUID uuid, double x, double y, double z, float pitch, float yaw,
            EntityType<?> entityTypeId, Vector3d velocity, Consumer<PacketBuffer> extraSpawnData) {
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

        final PacketBuffer data = new PacketBuffer(Unpooled.buffer());
        data.writeInt(this.getPacketID());
        data.writeVarInt(this.id);
        data.writeUniqueId(this.uuid);
        data.writeVarInt(Registry.ENTITY_TYPE.getId(this.entityTypeId));
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

    public SpawnEntityPacket(Entity entity, Consumer<PacketBuffer> extraSpawnData) {
        this(entity.getEntityId(), entity.getUniqueID(), entity.getPosX(), entity.getPosY(), entity.getPosZ(), entity.rotationPitch,
                entity.rotationYaw, entity.getType(), entity.getMotion(), extraSpawnData);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void clientPacketData(INetworkInfo network, PlayerEntity player) {
        ClientWorld world = (ClientWorld) player.world;

        EntityType<?> entityType = entityTypeId;
        Entity entity = entityType.create(world);

        if (entity != null) {
            entity.setPacketCoordinates(x, y, z);
            // NOTE: Vanilla doesn't do this for all spawned entities, but why transfer the
            // velocity???
            entity.setVelocity(velocityX, velocityY, velocityZ);
            entity.moveForced(x, y, z);
            entity.rotationPitch = (float) (pitch * 360) / 256.0F;
            entity.rotationYaw = (float) (yaw * 360) / 256.0F;
            entity.setEntityId(id);
            entity.setUniqueId(uuid);

            if (entity instanceof ICustomEntity) {
                ((ICustomEntity) entity).readAdditionalSpawnData(extraData);
            }

            world.addEntity(id, entity);
        }
    }

    public static <T extends Entity & ICustomEntity> IPacket<?> create(T entity) {
        // Not having this typed local variable will lead to a JVM bug because
        // the method reference will have a target type of "Entity" and not
        // "ICustomEntity"
        @SuppressWarnings("UnnecessaryLocalVariable")
        ICustomEntity customEntity = entity;
        SpawnEntityPacket packet = new SpawnEntityPacket(entity, customEntity::writeAdditionalSpawnData);
        return packet.toPacket(PacketDirection.CLIENTBOUND);
    }

}
