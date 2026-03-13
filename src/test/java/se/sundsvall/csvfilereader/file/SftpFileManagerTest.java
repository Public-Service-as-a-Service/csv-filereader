package se.sundsvall.csvfilereader.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SftpFileManagerTest {

	@Mock
	private SftpProperties sftpProperties;

	@InjectMocks
	private SftpFileManager sftpFileManager;

	@TempDir
	Path tempDir;

	@Test
	void downloadFileTest() {

		Path incomingDir = tempDir.resolve("incoming");
		String fileName = "file.csv";

		when(sftpProperties.username()).thenReturn("username");
		when(sftpProperties.password()).thenReturn("password");
		when(sftpProperties.remoteHost()).thenReturn("remoteHost");
		when(sftpProperties.connectTimeout()).thenReturn(Duration.ofSeconds(15));
		when(sftpProperties.sessionTimeout()).thenReturn(Duration.ofSeconds(15));

		sftpFileManager.downloadFile(incomingDir, fileName);
		verify(sftpProperties).username();
		verify(sftpProperties).password();
		verify(sftpProperties).remoteHost();
		verify(sftpProperties).connectTimeout();
		verify(sftpProperties).sessionTimeout();
	}

	@Test
	void testMoveOrganizationFiles() throws IOException {
		// Arrange
		Path sourceDir = tempDir.resolve("incoming");
		Path targetDir = tempDir.resolve("processed");
		Files.createDirectories(sourceDir);
		String content = "string";

		Path orgCsv = sourceDir.resolve("OrgExport.csv");
		Files.writeString(orgCsv, content);
		// Act
		sftpFileManager.moveFile(orgCsv, targetDir);
		// Assert
		Path moved = targetDir.resolve("OrgExport.csv");
		assertTrue(Files.exists(moved), "expected file to exist");
		assertFalse(Files.exists(orgCsv), "expected file to be moved");

		assertEquals(content, Files.readString(moved));
	}

	@Test
	void testMoveEmployeeFiles() throws IOException {
		// Arrange
		Path sourceDir = tempDir.resolve("incoming");
		Path targetDir = tempDir.resolve("processed");
		Files.createDirectories(sourceDir);
		Files.createDirectories(targetDir);

		String content = "string";

		Path empCsv = sourceDir.resolve("EmpExport.csv");
		Files.writeString(empCsv, content);
		// Act
		sftpFileManager.moveFile(empCsv, targetDir);
		// Assert
		Path moved = targetDir.resolve("EmpExport.csv");
		assertTrue(Files.exists(moved), "expected file to exist");
		assertFalse(Files.exists(empCsv), "expected file to be moved");

		assertEquals(content, Files.readString(moved));

	}

	@Test
	void testDeletePreviouslyProcessedFile() throws IOException {
		// Arrange
		Path processed = tempDir.resolve("processed.csv");
		Files.writeString(processed, "string");
		assertTrue(Files.exists(processed), "not deleted");
		// Act
		sftpFileManager.deletePreviouslyProcessedFile(processed);
		// Assert
		assertFalse(Files.exists(processed), "expected to be deleted");
	}

	@Test
	void deleteProcessedFileWhenFileDoesNotExistTest() throws IOException {
		// Arrange
		Path dir = tempDir.resolve("dir");
		Files.createDirectory(dir);

		Files.writeString(dir.resolve("file.txt"), "test");

		// Act
		IllegalStateException ex = assertThrows(
			IllegalStateException.class,
			() -> sftpFileManager.deletePreviouslyProcessedFile(dir));

		// Assert
		assertEquals("Failed to delete file", ex.getMessage());
	}

	@Test
	void testMoveFile_whenTargetDirIsAFile_shouldThrowIllegalStateException() throws IOException {
		Path incomingDir = tempDir.resolve("incoming");
		Files.createDirectories(incomingDir);

		Path filePath = incomingDir.resolve("OrgExport.csv");
		Files.writeString(filePath, "string");

		Path processedDir = tempDir.resolve("processed");
		Files.writeString(processedDir, "file");
		assertTrue(Files.isRegularFile(processedDir));

		assertThrows(IllegalStateException.class, () -> sftpFileManager.moveFile(filePath, processedDir));
	}

	@Test
	void verifyReadableWhenNoFileTest() {

		Path missingFile = tempDir.resolve("missing.csv");

		IllegalStateException exception = assertThrows(
			IllegalStateException.class, () -> sftpFileManager.verifyReadable(missingFile, "ORG"));

		assertTrue(exception.getMessage().startsWith("File does not exist:"));
	}

	@Test
	void verifyReadable_whenFileExists_doesNotThrow() throws Exception {
		Path file = tempDir.resolve("OrgExport.csv");

		Files.writeString(file, "string");

		assertDoesNotThrow(() -> sftpFileManager.verifyReadable(file, "ORG"));
	}

	@Test
	void verifyReadable_throwsException() throws IOException {
		Path directory = tempDir.resolve("directory");
		Files.createDirectory(directory);

		IllegalStateException exception = assertThrows(
			IllegalStateException.class, () -> sftpFileManager.verifyReadable(directory, "ORG"));

		assertTrue(exception.getMessage().startsWith("Failed reading file:"));
	}
}
