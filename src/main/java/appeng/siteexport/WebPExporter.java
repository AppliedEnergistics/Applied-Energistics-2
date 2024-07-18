package appeng.siteexport;

import static org.bytedeco.ffmpeg.global.avformat.avio_close_dyn_buf;
import static org.bytedeco.ffmpeg.global.avformat.avio_get_dyn_buf;
import static org.bytedeco.ffmpeg.global.avformat.avio_open_dyn_buf;
import static org.bytedeco.ffmpeg.global.avutil.av_dict_set;
import static org.bytedeco.ffmpeg.global.avutil.av_dict_set_int;
import static org.bytedeco.ffmpeg.global.avutil.av_free;

import com.mojang.blaze3d.platform.NativeImage;

import org.bytedeco.ffmpeg.avcodec.AVCodec;
import org.bytedeco.ffmpeg.avcodec.AVCodecContext;
import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.avformat.AVFormatContext;
import org.bytedeco.ffmpeg.avformat.AVIOContext;
import org.bytedeco.ffmpeg.avformat.AVStream;
import org.bytedeco.ffmpeg.avutil.AVDictionary;
import org.bytedeco.ffmpeg.avutil.AVFrame;
import org.bytedeco.ffmpeg.avutil.AVRational;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avformat;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.ffmpeg.global.swscale;
import org.bytedeco.ffmpeg.swscale.SwsContext;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.DoublePointer;

/**
 * Uses ffmpeg to write WebP animations.
 */
public class WebPExporter implements AutoCloseable {
    private final AVCodec codec;
    private final AVFormatContext formatContext;
    private final AVStream stream;
    private final AVCodecContext codecContext;
    private final AVPacket packet;
    private final AVFrame frame;
    private final AVFrame rgbFrame;
    private final SwsContext swsCtx;

    public WebPExporter(int width, int height, Format format) {
        try {
            // Allocate format context
            formatContext = new AVFormatContext();
            check(avformat.avformat_alloc_output_context2(formatContext, null, "webp", null));

            // Create new video stream
            stream = avformat.avformat_new_stream(formatContext, null);
            if (stream.isNull()) {
                throw new RuntimeException("Couldn't create new stream");
            }

            codec = avcodec.avcodec_find_encoder(avcodec.AV_CODEC_ID_WEBP);
            if (codec.isNull()) {
                throw new RuntimeException("Couldn't find webp codec");
            }

            codecContext = avcodec.avcodec_alloc_context3(codec);
            if (codecContext.isNull()) {
                throw new RuntimeException("Couldn't alloc context");
            }

            packet = avcodec.av_packet_alloc();
            if (packet.isNull()) {
                throw new RuntimeException("Packet cannot be allocated");
            }

            codecContext.width(width);
            codecContext.height(height);
            var targetFormat = switch (format) {
                case LOSSLESS_ALPHA, LOSSLESS -> avutil.AV_PIX_FMT_RGB32;
                case NORMAL -> avutil.AV_PIX_FMT_YUV420P;
                case NORMAL_ALPHA -> avutil.AV_PIX_FMT_YUVA420P;
            };
            codecContext.pix_fmt(targetFormat);

            codecContext.time_base(new AVRational());
            codecContext.time_base().num(1);
            codecContext.time_base().den(20); // 20 fps to match tickrate
            codecContext.framerate(new AVRational());
            codecContext.framerate().num(20); // 20 fps to match tickrate
            codecContext.framerate().den(1);

            /* Some formats want stream headers to be separate. */
            if ((formatContext.oformat().flags() & avformat.AVFMT_GLOBALHEADER) != 0) {
                codecContext.flags(codecContext.flags() | avcodec.AV_CODEC_FLAG_GLOBAL_HEADER);
            }

            var codecOptions = new AVDictionary();
            if (format.lossless) {
                av_dict_set_int(codecOptions, "lossless", 1, 0);
            } else {
                av_dict_set(codecOptions, "preset", "icon", 0);
            }

            // Open codec
            check(avcodec.avcodec_open2(codecContext, codec, codecOptions));

            var codecpar = stream.codecpar();
            check(avcodec.avcodec_parameters_from_context(
                    codecpar,
                    codecContext));
            stream.codecpar(codecpar);

            stream.time_base(new AVRational());
            stream.time_base().num(1);
            stream.time_base().den(20); // 20 fps to match tickrate

            // Frame used to push data into the codec
            frame = avutil.av_frame_alloc();
            if (frame.isNull()) {
                throw new RuntimeException("Failed to allocate frame");
            }
            frame.format(codecContext.pix_fmt());
            frame.width(codecContext.width());
            frame.height(codecContext.height());
            check(avutil.av_frame_get_buffer(frame, 0));

            // Input frame in RGBA format, we copy the rendered images to this for
            // conversion to the pixel format used by the codec
            rgbFrame = avutil.av_frame_alloc();
            if (rgbFrame.isNull()) {
                throw new RuntimeException("Failed to allocate frame");
            }
            rgbFrame.format(format.alpha ? avutil.AV_PIX_FMT_RGBA : avutil.AV_PIX_FMT_RGB0);
            rgbFrame.width(codecContext.width());
            rgbFrame.height(codecContext.height());
            check(avutil.av_frame_get_buffer(rgbFrame, 0));

            // We use in-memory dynamic buffers
            AVIOContext pb = new AVIOContext(null);
            check(avio_open_dyn_buf(pb));
            formatContext.pb(pb);

            // Conversion context for RGBA->YUV
            swsCtx = swscale.sws_getContext(
                    rgbFrame.width(), rgbFrame.height(), rgbFrame.format(),
                    frame.width(), frame.height(), frame.format(),
                    swscale.SWS_FAST_BILINEAR, null, null, (DoublePointer) null);
            if (swsCtx.isNull()) {
                throw new RuntimeException("Failed to allocate sws context");
            }

            // Write header
            var formatOptions = new AVDictionary();
            // Infinite loop
            av_dict_set_int(formatOptions, "loop", 0, 0);
            check(avformat.avformat_write_header(formatContext, formatOptions));
        } catch (RuntimeException e) {
            close(); // Free any resources allocated up to this point.
            throw e;
        }
    }

    public void writeFrame(int index, NativeImage nativeImage) {
        check(avutil.av_frame_make_writable(frame));

        // This is slow. We could reach into NativeImage's native pointer
        var pixels = nativeImage.getPixelsRGBA();
        var data = rgbFrame.data(0);
        for (int i = 0; i < pixels.length; i++) {
            data.putInt(i * 4L, pixels[i]);
        }

        // Convert from in-memory pixel format to format required by codec
        swscale.sws_scale(swsCtx, rgbFrame.data(),
                rgbFrame.linesize(), 0, codecContext.height(), frame.data(),
                frame.linesize());

        frame.pts(index);

        encode(formatContext, codecContext, packet, stream, frame);
    }

    public byte[] finish() {
        encode(formatContext, codecContext, packet, stream, null);

        // Write the end of the file
        check(avformat.av_write_trailer(formatContext));

        // Get the current output buffer
        var pb = new BytePointer();
        var pbSize = avio_get_dyn_buf(formatContext.pb(), pb);
        var data = new byte[pbSize];
        pb.get(data);

        return data;
    }

    private static void encode(AVFormatContext formatContext, AVCodecContext codecContext, AVPacket packet,
            AVStream stream, AVFrame frame) {
        // Send frame to codec
        check(avcodec.avcodec_send_frame(codecContext, frame));

        // Get the output packet, if any (codec may buffer frames)
        while (true) {
            var err = avcodec.avcodec_receive_packet(codecContext, packet);
            if (err == avutil.AVERROR_EAGAIN() || err == avutil.AVERROR_EOF) {
                return;
            }
            check(err);

            // Rescale output packet timestamp values from codec to stream timebase
            avcodec.av_packet_rescale_ts(packet, codecContext.time_base(), stream.time_base());
            packet.stream_index(stream.index());

            // Write packet
            check(avformat.av_interleaved_write_frame(formatContext, packet));
        }
    }

    // Helper to check libav* return codes
    private static void check(int err) {
        if (err < 0) {
            throw new RuntimeException(getErrorString(err) + " (" + err + ")");
        }
    }

    /* Custom implementation of missing av_err2str() ffmpeg function */
    private static String getErrorString(int err) {
        var e = new BytePointer(512);
        if (avutil.av_strerror(err, e, 512) < 0) {
            return "Unknown Error";
        }
        return e.getString().substring(0, (int) BytePointer.strlen(e));
    }

    @Override
    public void close() {
        if (codecContext != null) {
            avcodec.avcodec_free_context(codecContext);
        }
        if (frame != null) {
            avutil.av_frame_free(frame);
        }
        if (rgbFrame != null) {
            avutil.av_frame_free(rgbFrame);
        }
        if (codec != null) {
            codec.close();
        }
        if (formatContext != null && !formatContext.pb().isNull()) {
            var pb = new BytePointer();
            avio_close_dyn_buf(formatContext.pb(), pb);
            av_free(pb);
        }
        if (formatContext != null) {
            avformat.avformat_free_context(formatContext);
        }
    }

    public enum Format {
        LOSSLESS_ALPHA(true, true),
        LOSSLESS(true, false),
        NORMAL_ALPHA(false, true),
        NORMAL(false, false);

        final boolean lossless;
        final boolean alpha;

        Format(boolean lossless, boolean alpha) {
            this.lossless = lossless;
            this.alpha = alpha;
        }
    }
}
