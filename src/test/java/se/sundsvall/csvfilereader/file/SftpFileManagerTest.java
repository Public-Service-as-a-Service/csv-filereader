package se.sundsvall.csvfilereader.file;

import java.nio.file.Path;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
}
