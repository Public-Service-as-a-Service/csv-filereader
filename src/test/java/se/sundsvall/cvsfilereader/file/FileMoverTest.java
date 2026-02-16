package se.sundsvall.cvsfilereader.file;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import se.sundsvall.csvfilereader.file.FileMover;

public class FileMoverTest {

	private final FileMover fileMover = new FileMover();

	@TempDir
	Path tempDir;

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
		fileMover.moveFile(orgCsv, targetDir);
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
		fileMover.moveFile(empCsv, targetDir);
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
		fileMover.deletePreviouslyProcessedFile(processed);
		// Assert
		assertFalse(Files.exists(processed), "expected to be deleted");
	}

	@Test
	void deleteProcessedFileWhenFileDoesNotExistTest() {
		// Arrange
		Path missingFile = tempDir.resolve("missingFile.csv");
		// Act
		fileMover.deletePreviouslyProcessedFile(missingFile);
		// Assert
		assertFalse(Files.exists(missingFile));
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

		assertThrows(IllegalStateException.class, () -> fileMover.moveFile(filePath, processedDir));
	}
}
