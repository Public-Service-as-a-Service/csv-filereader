package se.sundsvall.csvfilereader.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class MockFileManagerTest {

	@TempDir
	Path tempDir;

	private MockFileManager mockFileManager;

	@BeforeEach
	void setUp() {
		mockFileManager = new MockFileManager();
		ReflectionTestUtils.setField(mockFileManager, "fileSourceDir", tempDir.resolve("temp"));
	}

	@Test
	void downloadFile_copiesFileFromTempToIncoming() throws IOException {
		Path tempSource = tempDir.resolve("temp");
		Path incomingDir = tempDir.resolve("incoming");
		Files.createDirectories(tempSource);

		String content = "col1;col2\nval1;val2";
		Files.writeString(tempSource.resolve("OrgExport.csv"), content);

		mockFileManager.downloadFile(incomingDir, "OrgExport.csv");

		Path copied = incomingDir.resolve("OrgExport.csv");
		assertTrue(Files.exists(copied));
		assertEquals(content, Files.readString(copied));
	}

	@Test
	void downloadFile_whenSourceFileMissing_doesNotThrow() {
		Path incomingDir = tempDir.resolve("incoming");

		assertDoesNotThrow(() -> mockFileManager.downloadFile(incomingDir, "missing.csv"));
	}
}
