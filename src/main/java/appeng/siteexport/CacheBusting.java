package appeng.siteexport;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.common.hash.Hashing;

/**
 * We mark all of our assets as "immutable" when we upload them. That means the browser can cache them indefinitely
 * WITHOUT asking the server ever again for a new version of the asset. To make this work, we hash the content of the
 * asset and include a part of that hash in the filename. This means whenever the content of the asset changes, so does
 * the filename, essentially "busting" the cache.
 */
public final class CacheBusting {
    private static final char[] BASE64_SYMBOLS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
            .toCharArray();
    private static final BigInteger BASE_62 = BigInteger.valueOf(62);

    private CacheBusting() {
    }

    public static String create(byte[] content) {
        var contentHash = Hashing.sha256().hashBytes(content).asBytes();
        return encodeBase62(contentHash).substring(0, 12);
    }

    public static Path writeAsset(Path originalPath, byte[] content) throws IOException {
        var suffix = create(content);

        // Insert the cache busting suffix into the filename such that
        // blah.txt becomes blah.<hash>.txt
        var filename = originalPath.getFileName().toString();
        var idx = filename.indexOf('.');
        if (idx == -1) {
            filename += "." + suffix;
        } else {
            filename = filename.substring(0, idx) + "." + suffix + filename.substring(idx);
        }

        var newPath = originalPath.resolveSibling(filename);

        Files.createDirectories(newPath.getParent());
        Files.write(newPath, content);

        return newPath;
    }

    private static String encodeBase62(byte[] data) {
        var val = new BigInteger(1, data);
        var sb = new StringBuilder();
        while (val.compareTo(BigInteger.ZERO) > 0) {
            sb.append(BASE64_SYMBOLS[val.mod(BASE_62).intValue()]);
            val = val.divide(BASE_62);
        }
        return sb.reverse().toString();
    }
}
