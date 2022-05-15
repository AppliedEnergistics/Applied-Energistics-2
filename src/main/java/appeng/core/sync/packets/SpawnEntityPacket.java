package appeng.core.sync.packets;

import java.util.UUID;
import java.util.function.Consumer;

import io.netty.buffer.Unpooled;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import appeng.core.sync.BasePacket;

// This is essentially a copy of ClientboundAddEntityPacket, supporting custom entities
public class SpawnEntityPacket extends BasePacket {
    public static final double MAGICAL_QUANTIZATION = 8000.0D;
    private int id;
    private UUID uuid;
    private double x;
    private double y;
    private double z;
    private int xa;
    private int ya;
    private int za;
    private int xRot;
    private int yRot;
    private EntityType<?> type;
    public static final double LIMIT = 3.9D;
    private FriendlyByteBuf extraData;

    public SpawnEntityPacket(FriendlyByteBuf friendlyByteBuf) {
        this.id = friendlyByteBuf.readVarInt();
        this.uuid = friendlyByteBuf.readUUID();
        this.type = Registry.ENTITY_TYPE.byId(friendlyByteBuf.readVarInt());
        this.x = friendlyByteBuf.readDouble();
        this.y = friendlyByteBuf.readDouble();
        this.z = friendlyByteBuf.readDouble();
        this.xRot = friendlyByteBuf.readByte();
        this.yRot = friendlyByteBuf.readByte();
        this.xa = friendlyByteBuf.readShort();
        this.ya = friendlyByteBuf.readShort();
        this.za = friendlyByteBuf.readShort();
        this.extraData = new FriendlyByteBuf(friendlyByteBuf.copy());
    }

    public SpawnEntityPacket(int id, UUID uuid, double x, double y, double z, float pitch, float yaw,
            EntityType<?> type, Vec3 velocity, Consumer<FriendlyByteBuf> extraSpawnData) {
        this.id = id;
        this.uuid = uuid;
        this.x = x;
        this.y = y;
        this.z = z;
        this.xRot = Mth.floor(pitch * 256.0F / 360.0F);
        this.yRot = Mth.floor(yaw * 256.0F / 360.0F);
        this.type = type;
        this.xa = (int) (Mth.clamp(velocity.x, -3.9D, 3.9D) * MAGICAL_QUANTIZATION);
        this.ya = (int) (Mth.clamp(velocity.y, -3.9D, 3.9D) * MAGICAL_QUANTIZATION);
        this.za = (int) (Mth.clamp(velocity.z, -3.9D, 3.9D) * MAGICAL_QUANTIZATION);

        var data = new FriendlyByteBuf(Unpooled.buffer());
        data.writeInt(this.getPacketID());
        data.writeVarInt(this.id);
        data.writeUUID(this.uuid);
        data.writeVarInt(Registry.ENTITY_TYPE.getId(this.type));
        data.writeDouble(this.x);
        data.writeDouble(this.y);
        data.writeDouble(this.z);
        data.writeByte(this.xRot);
        data.writeByte(this.yRot);
        data.writeShort(this.xa);
        data.writeShort(this.ya);
        data.writeShort(this.za);
        extraSpawnData.accept(data);

        this.configureWrite(data);
    }

    public SpawnEntityPacket(Entity entity, Consumer<FriendlyByteBuf> extraSpawnData) {
        this(entity.getId(), entity.getUUID(), entity.getX(), entity.getY(), entity.getZ(),
                entity.getXRot(),
                entity.getYRot(), entity.getType(), entity.getDeltaMovement(), extraSpawnData);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void clientPacketData(Player player) {
        ClientLevel world = (ClientLevel) player.level;

        Entity entity = type.create(world);

        if (entity != null) {
            entity.setPacketCoordinates(x, y, z);
            // NOTE: Vanilla doesn't do this for all spawned entities, but why transfer the
            // velocity???
            entity.setDeltaMovement(xa, ya, za);
            entity.setPacketCoordinates(x, y, z);
            entity.moveTo(x, y, z);
            entity.setXRot((float) (xRot * 360) / 256.0F);
            entity.setYRot((float) (yRot * 360) / 256.0F);
            entity.setId(id);
            entity.setUUID(uuid);

            if (entity instanceof ICustomEntity) {
                ((ICustomEntity) entity).readAdditionalSpawnData(extraData);
            }

            world.putNonPlayerEntity(id, entity);
        }
    }

    public static <T extends Entity & ICustomEntity> Packet<?> create(T entity) {
        // Not having this typed local variable will lead to a JVM bug because
        // the method reference will have a target type of "Entity" and not
        // "ICustomEntity"
        @SuppressWarnings("UnnecessaryLocalVariable")
        ICustomEntity customEntity = entity;
        SpawnEntityPacket packet = new SpawnEntityPacket(entity, customEntity::writeAdditionalSpawnData);
        return ServerPlayNetworking.createS2CPacket(BasePacket.CHANNEL, packet.getPayload());
    }

}
