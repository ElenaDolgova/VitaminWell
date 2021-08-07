import java.nio.file.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ZipDirectory implements Directory {
    private final FileSystem fs;
    private final Path path;
    private final String probeContentType;

    public ZipDirectory(Link displayFiles, Path path, FileSystem fs) {
        this.path = path != null ? path : displayFiles.getPath();
        this.probeContentType = displayFiles.getProbeContentType();
        this.fs = fs;
    }

    @Override
    public void getFiles(Consumer<List<? extends Link>> action, String ext) {
        if ("application/zip".equals(probeContentType)) {
            List<ZipFileLink> collect = Directory.streamAllFiles(fs, 2)
                    .filter(path -> {
                        if (ext == null || ext.length() == 0) return true;
                        return ext.equals(Directory.getExtension(path));
                    })
                    .filter(path -> {
                        Path parent = path.getParent();
                        // zip на macos создаются с доп дирекотрий корня
                        return parent != null && !path.startsWith("/__MACOSX");
                    })
                    .map(path -> new ZipFileLink(path, fs, false))
                    .sorted()
                    .collect(Collectors.toList());
            action.accept(collect);
        }
        int depth = path.getNameCount() + 1;
        action.accept(Directory.streamAllFiles(fs, depth)
                .filter(p -> p.startsWith("/" + path))
                .filter(p -> ext == null || ext.length() == 0 || p.endsWith(ext))
                .filter(p -> p.getNameCount() > path.getNameCount())
                .map(path -> new ZipFileLink(path, fs, false))
                .collect(Collectors.toList()));
    }

    @Override
    public String getDirectoryName() {
        return String.valueOf(path.getFileName());
    }

    @Override
    public String toString() {
        return getDirectoryName();
    }
}
