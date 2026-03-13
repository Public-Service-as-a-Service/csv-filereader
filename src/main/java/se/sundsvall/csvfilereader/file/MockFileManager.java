package se.sundsvall.csvfilereader.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("mock")
public class MockFileManager extends AbstractFileManager {

	private static final Logger log = LoggerFactory.getLogger(MockFileManager.class);

	@Value("${import.file-source-dir}")
	private Path fileSourceDir;

	@Override
	public void downloadFile(Path dir, String fileName) {
		try {
			Files.createDirectories(dir);
			Files.copy(
				fileSourceDir.resolve(fileName),
				dir.resolve(fileName),
				StandardCopyOption.REPLACE_EXISTING);
			log.info("Mock: copied file '{}' from temp to incoming", fileName);
		} catch (IOException e) {
			log.error("Mock: failed to copy file '{}'", fileName, e);
		}
	}
}
