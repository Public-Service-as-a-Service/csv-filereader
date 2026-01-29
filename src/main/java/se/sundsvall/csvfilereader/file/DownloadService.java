package se.sundsvall.csvfilereader.file;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DownloadService {

	private static final Logger log = LoggerFactory.getLogger(DownloadService.class);

	public void fetchOrgFile(Path orgCsv) {
		// lägg till för att hämta filerna
		verifyReadable(orgCsv, "ORG");
		log.info("[ORG] File Downloaded");
	}

	public void fetchEmpFile(Path empCsv) {
		// lägg till för att hämta filerna
		verifyReadable(empCsv, "EMP");
		log.info("[EMP] File Downloaded");
	}

	private void verifyReadable(Path path, String label) {
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
