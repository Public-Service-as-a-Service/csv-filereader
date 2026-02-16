package se.sundsvall.csvfilereader.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FileMover {

	private static final Logger log = LoggerFactory.getLogger(FileMover.class);

	public void moveFile(Path targetFile, Path targetDir) {
		try {
			Files.createDirectories(targetDir);

			Files.move(
				targetFile,
				targetDir.resolve(targetFile.getFileName()),
				StandardCopyOption.REPLACE_EXISTING);

			log.info("Moved file '{}' to '{}' after reading", targetFile, targetDir);
		} catch (IOException e) {
			throw new IllegalStateException("Failed to move processed files", e);
		}
	}

	public void deletePreviouslyProcessedFile(Path processedFile) {
		try {
			if (Files.deleteIfExists(processedFile)) {
				log.info("Old file deleted");
			}
		} catch (IOException e) {
			throw new IllegalStateException("Failed to delete file");
		}
	}
}
