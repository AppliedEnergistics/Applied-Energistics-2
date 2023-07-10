// automatically generated by the FlatBuffers compiler, do not modify

package appeng.flatbuffers.scene;

import com.google.flatbuffers.BaseVector;
import com.google.flatbuffers.BooleanVector;
import com.google.flatbuffers.ByteVector;
import com.google.flatbuffers.Constants;
import com.google.flatbuffers.DoubleVector;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.FloatVector;
import com.google.flatbuffers.IntVector;
import com.google.flatbuffers.LongVector;
import com.google.flatbuffers.ShortVector;
import com.google.flatbuffers.StringVector;
import com.google.flatbuffers.Struct;
import com.google.flatbuffers.Table;
import com.google.flatbuffers.UnionVector;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@javax.annotation.processing.Generated(value="flatc")
@SuppressWarnings("unused")
public final class ExpSampler extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_23_5_26(); }
  public static ExpSampler getRootAsExpSampler(ByteBuffer _bb) { return getRootAsExpSampler(_bb, new ExpSampler()); }
  public static ExpSampler getRootAsExpSampler(ByteBuffer _bb, ExpSampler obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public ExpSampler __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public String texture() { int o = __offset(4); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer textureAsByteBuffer() { return __vector_as_bytebuffer(4, 1); }
  public ByteBuffer textureInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 4, 1); }
  public boolean linearFiltering() { int o = __offset(6); return o != 0 ? 0!=bb.get(o + bb_pos) : false; }
  public boolean mutateLinearFiltering(boolean linear_filtering) { int o = __offset(6); if (o != 0) { bb.put(o + bb_pos, (byte)(linear_filtering ? 1 : 0)); return true; } else { return false; } }
  public boolean useMipmaps() { int o = __offset(8); return o != 0 ? 0!=bb.get(o + bb_pos) : false; }
  public boolean mutateUseMipmaps(boolean use_mipmaps) { int o = __offset(8); if (o != 0) { bb.put(o + bb_pos, (byte)(use_mipmaps ? 1 : 0)); return true; } else { return false; } }

  public static int createExpSampler(FlatBufferBuilder builder,
      int textureOffset,
      boolean linearFiltering,
      boolean useMipmaps) {
    builder.startTable(3);
    ExpSampler.addTexture(builder, textureOffset);
    ExpSampler.addUseMipmaps(builder, useMipmaps);
    ExpSampler.addLinearFiltering(builder, linearFiltering);
    return ExpSampler.endExpSampler(builder);
  }

  public static void startExpSampler(FlatBufferBuilder builder) { builder.startTable(3); }
  public static void addTexture(FlatBufferBuilder builder, int textureOffset) { builder.addOffset(0, textureOffset, 0); }
  public static void addLinearFiltering(FlatBufferBuilder builder, boolean linearFiltering) { builder.addBoolean(1, linearFiltering, false); }
  public static void addUseMipmaps(FlatBufferBuilder builder, boolean useMipmaps) { builder.addBoolean(2, useMipmaps, false); }
  public static int endExpSampler(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public ExpSampler get(int j) { return get(new ExpSampler(), j); }
    public ExpSampler get(ExpSampler obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
}
