package se.sundsvall.csvfilereader.file;

import java.nio.file.Path;

public interface FileManager {
	void downloadFile(Path dir, String fileName);

	void moveFile(Path targetFile, Path targetDir);

	void deletePreviouslyProcessedFile(Path processedFile);

	void verifyReadable(Path path, String label);
}
