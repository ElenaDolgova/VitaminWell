import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public interface Link extends Comparable<Link> {
    Path getPath();

    boolean isDirectory();

    String getName();

    Directory createDirectory() throws IOException;

    InputStream getInputStreamOfFile() throws IOException;

    default String getProbeContentType() {
        return getProbeContentType(getPath());
    }

    static String getProbeContentType(Path path) {
        String probeContentType = null;
        try {
            probeContentType = Files.probeContentType(path);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return probeContentType;
    }
}
