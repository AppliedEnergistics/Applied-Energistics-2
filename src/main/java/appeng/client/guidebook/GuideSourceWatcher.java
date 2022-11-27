package appeng.client.guidebook;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.methvin.watcher.DirectoryChangeEvent;
import io.methvin.watcher.DirectoryChangeListener;
import io.methvin.watcher.DirectoryWatcher;

import net.minecraft.resources.ResourceLocation;

import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.compiler.ParsedGuidePage;
import appeng.core.AppEng;

class GuideSourceWatcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(GuideSourceWatcher.class);

    /**
     * The {@link ResourceLocation} namespace to use for files in the watched folder.
     */
    private final String namespace;

    private final Path sourceFolder;

    // Recursive directory watcher for the guidebook sources.
    @Nullable
    private final DirectoryWatcher sourceWatcher;

    // Queued changes that come in from a separate thread
    private final Map<ResourceLocation, ParsedGuidePage> changedPages = new HashMap<>();
    private final Set<ResourceLocation> deletedPages = new HashSet<>();

    private final ExecutorService watchExecutor;

    public GuideSourceWatcher(String namespace, Path sourceFolder) {
        this.namespace = namespace;
        this.sourceFolder = sourceFolder;
        if (!Files.isDirectory(sourceFolder)) {
            throw new RuntimeException("Cannot find the specified folder for the AE2 guidebook sources: "
                    + sourceFolder);
        }
        LOGGER.info("Watching guidebook sources in {}", sourceFolder);

        watchExecutor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("AE2GuidebookWatcher%d")
                .build());

        // Watch the folder recursively in a separate thread, queue up any changes and apply them
        // in the client tick.
        DirectoryWatcher watcher;
        try {
            watcher = DirectoryWatcher.builder()
                    .path(sourceFolder)
                    .fileHashing(false)
                    .listener(new Listener())
                    .build();
        } catch (IOException e) {
            LOGGER.error("Failed to watch for changes in the guidebook sources at {}", sourceFolder, e);
            watcher = null;
        }
        sourceWatcher = watcher;

        // Actually process changes in the client tick to prevent race conditions and other crashes
        if (sourceWatcher != null) {
            sourceWatcher.watchAsync(watchExecutor);
        }
    }

    public List<ParsedGuidePage> loadAll() {
        var stopwatch = Stopwatch.createStarted();

        // Find all potential pages
        var pagesToLoad = new HashMap<ResourceLocation, Path>();
        try {
            Files.walkFileTree(sourceFolder, new FileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    var pageId = getPageId(file);
                    if (pageId != null) {
                        pagesToLoad.put(pageId, file);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    LOGGER.error("Failed to list page {}", file, exc);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                    if (exc != null) {
                        LOGGER.error("Failed to list all pages in {}", dir, exc);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            LOGGER.error("Failed to list all pages in {}", sourceFolder, e);
        }

        LOGGER.info("Loading {} guidebook pages", pagesToLoad.size());
        var loadedPages = pagesToLoad.entrySet()
                .stream()
                .map(entry -> {
                    var path = entry.getValue();
                    try (var in = Files.newInputStream(path)) {
                        return PageCompiler.parse(AppEng.MOD_ID, entry.getKey(), in);

                    } catch (Exception e) {
                        LOGGER.error("Failed to reload guidebook page {}", path, e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();

        LOGGER.info("Loaded {} pages from {} in {}", loadedPages.size(), sourceFolder, stopwatch);

        return loadedPages;
    }

    public synchronized List<GuidePageChange> takeChanges() {

        if (deletedPages.isEmpty() && changedPages.isEmpty()) {
            return List.of();
        }

        var changes = new ArrayList<GuidePageChange>();

        for (var deletedPage : deletedPages) {
            changes.add(new GuidePageChange(deletedPage, null, null));
        }
        deletedPages.clear();

        for (var changedPage : changedPages.values()) {
            changes.add(new GuidePageChange(changedPage.getId(), null, changedPage));
        }
        changedPages.clear();

        return changes;
    }

    public synchronized void close() {
        changedPages.clear();
        deletedPages.clear();
        watchExecutor.shutdown();

        if (sourceWatcher != null) {
            try {
                sourceWatcher.close();
            } catch (IOException e) {
                LOGGER.error("Failed to close fileystem watcher for {}", sourceFolder);
            }
        }
    }

    private class Listener implements DirectoryChangeListener {
        @Override
        public void onEvent(DirectoryChangeEvent event) {
            if (event.isDirectory()) {
                return;
            }
            switch (event.eventType()) {
                case CREATE, MODIFY -> pageChanged(event.path());
                case DELETE -> pageDeleted(event.path());
            }
        }

        @Override
        public boolean isWatching() {
            return sourceWatcher != null && !sourceWatcher.isClosed();
        }

        @Override
        public void onException(Exception e) {
            LOGGER.error("Failed watching for changes", e);
        }
    }

    // Only call while holding the lock!
    private synchronized void pageChanged(Path path) {
        var pageId = getPageId(path);
        if (pageId == null) {
            return; // Probably not a page
        }

        // If it was previously deleted in the same change-set, undelete it
        deletedPages.remove(pageId);

        try (var in = Files.newInputStream(path)) {
            var page = PageCompiler.parse(AppEng.MOD_ID, pageId, in);
            changedPages.put(pageId, page);
        } catch (Exception e) {
            LOGGER.error("Failed to reload guidebook page {}", path, e);
        }
    }

    // Only call while holding the lock!
    private synchronized void pageDeleted(Path path) {
        var pageId = getPageId(path);
        if (pageId == null) {
            return; // Probably not a page
        }

        // If it was previously changed in the same change-set, remove the change
        changedPages.remove(pageId);
        deletedPages.add(pageId);
    }

    @Nullable
    private ResourceLocation getPageId(Path path) {
        var relativePath = sourceFolder.relativize(path);
        var relativePathStr = relativePath.toString().replace('\\', '/');
        if (!relativePathStr.endsWith(".md")) {
            return null;
        }
        if (!ResourceLocation.isValidResourceLocation(relativePathStr)) {
            return null;
        }
        return new ResourceLocation(namespace, relativePathStr);
    }
}