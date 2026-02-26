package se.sundsvall.csvfilereader.file;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FileManager {

	private static final Logger log = LoggerFactory.getLogger(FileManager.class);

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

	public void verifyReadable(Path path, String label) {
		log.info("[{}] Checking file: {}", label, path.toAbsolutePath());

		if (!Files.exists(path)) {
			throw new IllegalStateException("File does not exist: " + path.toAbsolutePath());
		}

		if (!Files.isReadable(path)) {
			throw new IllegalStateException("File not readable: " + path.toAbsolutePath());
		}

		try {
			long size = Files.size(path);
			String firstLine = Files.readAllLines(path, StandardCharsets.UTF_8).stream()
				.findFirst()
				.orElse("");

			log.info("[{}] OK. Size={} bytes. First line: {}", label, size, firstLine);

		} catch (IOException e) {
			throw new IllegalStateException("Failed reading file: " + path.toAbsolutePath(), e);
		}
	}
}
