package se.sundsvall.cvsfilereader.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FileService {

	private static final Logger log = LoggerFactory.getLogger(FileService.class);

	public void moveOrganizationFiles(Path orgCsv, Path targetDir) {
		try {
			Files.createDirectories(targetDir);

			Files.move(
				orgCsv,
				targetDir.resolve(orgCsv.getFileName()),
				StandardCopyOption.REPLACE_EXISTING);

		} catch (IOException e) {
			throw new IllegalStateException("Failed to move processed files", e);
		}
	}

	public void moveEmployeeFiles(Path empCsv, Path targetDir) {
		try {
			Files.createDirectories(targetDir);

			Files.move(
				empCsv,
				targetDir.resolve(empCsv.getFileName()),
				StandardCopyOption.REPLACE_EXISTING);

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
