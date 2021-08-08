package model;

import exception.FileProcessingException;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface Directory {
    Path getPath();

    String getName();

    boolean isDirectory();

    Directory createDirectory() throws FileProcessingException;

    void processFile(Consumer<InputStream> consumer) throws FileProcessingException;

    /**
     * Метод проходится по директории и достают из нее все элементы. После batchAction обрабатывает эти элементы.
     * Если в метод передан ext, то возвращаются файлы, удовлетворяющие переданному ext расширению
     * @param batchAction консьюмер, обрабатывающий вернувшиеся батчи файлов из текущей директории
     * @param ext расширение файлов, которые нужно вернуть
     * @throws FileProcessingException
     */
    void getFiles(Consumer<List<? extends Directory>> batchAction, String ext) throws FileProcessingException;

    @Nullable
    static String getProbeContentType(Path path) {
        String probeContentType = null;
        try {
            probeContentType = Files.probeContentType(path);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return probeContentType;
    }

    static Stream<Path> streamAllFiles(FileSystem fs, int depth) {
        return StreamSupport.stream(fs.getRootDirectories().spliterator(), false)
                .flatMap(it -> walkFiles(it, depth));
    }

    static Stream<Path> walkFiles(Path it, int depth) {
        try {
            return Files.walk(it, depth);
        } catch (IOException e) {
            throw new FileProcessingException("Unable to walk file tree", e);
        }
    }

    static String getExtension(Path p) {
        return FilenameUtils.getExtension(p.toString());
    }

    static boolean isZip(Path path) {
        String probeContentType = Directory.getProbeContentType(path);
        if (probeContentType == null){
            return false;
        }
        return probeContentType.contains("zip");
    }
}
