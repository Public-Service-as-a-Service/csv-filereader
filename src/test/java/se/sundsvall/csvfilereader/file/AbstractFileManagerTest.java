package se.sundsvall.csvfilereader.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class AbstractFileManagerTest {

	@TempDir
	Path tempDir;

	private AbstractFileManager fileManager;

	@BeforeEach
	void setUp() {
		fileManager = new AbstractFileManager() {
			@Override
			public void downloadFile(Path dir, String fileName) {
				// Tested in Sftp and MockFileManagerTest
			}
		};
	}

	@Test
	void testMoveOrganizationFiles() throws IOException {
		Path sourceDir = tempDir.resolve("incoming");
		Path targetDir = tempDir.resolve("processed");
		Files.createDirectories(sourceDir);
		String content = "string";

		Path orgCsv = sourceDir.resolve("OrgExport.csv");
		Files.writeString(orgCsv, content);

		fileManager.moveFile(orgCsv, targetDir);

		Path moved = targetDir.resolve("OrgExport.csv");
		assertTrue(Files.exists(moved));
		assertFalse(Files.exists(orgCsv));
		assertEquals(content, Files.readString(moved));
	}

	@Test
	void testMoveEmployeeFiles() throws IOException {
		Path sourceDir = tempDir.resolve("incoming");
		Path targetDir = tempDir.resolve("processed");
		Files.createDirectories(sourceDir);
		Files.createDirectories(targetDir);
		String content = "string";

		Path empCsv = sourceDir.resolve("EmpExport.csv");
		Files.writeString(empCsv, content);

		fileManager.moveFile(empCsv, targetDir);

		Path moved = targetDir.resolve("EmpExport.csv");
		assertTrue(Files.exists(moved));
		assertFalse(Files.exists(empCsv));
		assertEquals(content, Files.readString(moved));
	}

	@Test
	void testMoveFile_whenTargetDirIsAFile_shouldThrowIllegalStateException() throws IOException {
		Path incomingDir = tempDir.resolve("incoming");
		Files.createDirectories(incomingDir);

		Path filePath = incomingDir.resolve("OrgExport.csv");
		Files.writeString(filePath, "string");

		Path processedDir = tempDir.resolve("processed");
		Files.writeString(processedDir, "file");

		assertThrows(IllegalStateException.class, () -> fileManager.moveFile(filePath, processedDir));
	}

	@Test
	void testDeletePreviouslyProcessedFile() throws IOException {
		Path processed = tempDir.resolve("processed.csv");
		Files.writeString(processed, "string");
		assertTrue(Files.exists(processed));

		fileManager.deletePreviouslyProcessedFile(processed);

		assertFalse(Files.exists(processed));
	}

	@Test
	void deleteProcessedFileWhenFileDoesNotExistTest() throws IOException {
		Path dir = tempDir.resolve("dir");
		Files.createDirectory(dir);
		Files.writeString(dir.resolve("file.txt"), "test");

		IllegalStateException ex = assertThrows(
			IllegalStateException.class,
			() -> fileManager.deletePreviouslyProcessedFile(dir));

		assertEquals("Failed to delete file", ex.getMessage());
	}

	@Test
	void verifyReadableWhenNoFileTest() {
		Path missingFile = tempDir.resolve("missing.csv");

		IllegalStateException exception = assertThrows(
			IllegalStateException.class, () -> fileManager.verifyReadable(missingFile, "ORG"));

		assertTrue(exception.getMessage().startsWith("File does not exist:"));
	}

	@Test
	void verifyReadable_whenFileExists_doesNotThrow() throws IOException {
		Path file = tempDir.resolve("OrgExport.csv");
		Files.writeString(file, "string");

		assertDoesNotThrow(() -> fileManager.verifyReadable(file, "ORG"));
	}

	@Test
	void verifyReadable_throwsException() throws IOException {
		Path directory = tempDir.resolve("directory");
		Files.createDirectory(directory);

		IllegalStateException exception = assertThrows(
			IllegalStateException.class, () -> fileManager.verifyReadable(directory, "ORG"));

		assertTrue(exception.getMessage().startsWith("Failed reading file:"));
	}
}
