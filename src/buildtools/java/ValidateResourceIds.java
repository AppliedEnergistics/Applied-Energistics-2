import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class ValidateResourceIds {
    public static void main(String[] args) throws IOException {
        List<String> invalidPaths = new ArrayList<>();

        for (String folder : args) {
            var path = Paths.get(folder);
            System.out.println("Checking " + path);

            Files.walkFileTree(path, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    var f = path.relativize(file).toString().replace('\\', '/');

                    if (!ResourceLocation.isValidResourceLocation("dummy:" + f)) {
                        invalidPaths.add(f);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });

        }

        if (!invalidPaths.isEmpty()) {
            System.err.println("Invalid paths:");
            for (String invalidPath : invalidPaths) {
                System.err.println("  " + invalidPath);
            }
            System.exit(1);
        }
    }
}
