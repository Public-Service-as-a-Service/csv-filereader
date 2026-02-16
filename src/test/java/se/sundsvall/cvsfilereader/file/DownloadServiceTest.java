package se.sundsvall.cvsfilereader.file;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import se.sundsvall.csvfilereader.file.DownloadService;

public class DownloadServiceTest {

	@TempDir
	Path tempDir;

	private final DownloadService service = new DownloadService();

	@Test
	void fetchEmpFileTest() throws Exception {
		Path empCsv = tempDir.resolve("empExport.csv");
		Files.writeString(empCsv, "employeeDetails");
		assertDoesNotThrow(() -> service.fetchEmpFile(empCsv));

	}

	@Test
	void fetchOrgFileTest() throws Exception {
		Path orgCsv = tempDir.resolve("orgExport.csv");
		Files.writeString(orgCsv, "organizationDetails");
		assertDoesNotThrow(() -> service.fetchOrgFile(orgCsv));

	}

	@Test
	void verifyReadableWhenNoFileTest() {

		Path missingFile = tempDir.resolve("missing.csv");

		IllegalStateException exception = assertThrows(
			IllegalStateException.class, () -> service.verifyReadable(missingFile, "ORG"));

		assertTrue(exception.getMessage().startsWith("File does not exist:"));
	}

}
