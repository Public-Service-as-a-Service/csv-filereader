package se.sundsvall.csvfilereader.file;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!mock")
@EnableConfigurationProperties(SftpProperties.class)
public class SftpFileManager implements FileManager {

	private static final Logger log = LoggerFactory.getLogger(SftpFileManager.class);
	SftpProperties sftpProperties;

	public SftpFileManager(SftpProperties sftpProperties) {
		this.sftpProperties = sftpProperties;
	}

	@Override
	public void downloadFile(Path dir, String fileName) {

		try {
			FileSystemManager manager = VFS.getManager();
			FileSystemOptions options = new FileSystemOptions();

			var builder = SftpFileSystemConfigBuilder.getInstance();
			builder.setConnectTimeout(options, sftpProperties.connectTimeout());
			builder.setSessionTimeout(options, sftpProperties.sessionTimeout());

			var local = manager.resolveFile(dir.resolve(fileName).toUri().toString());
			var remote = manager.resolveFile(String.format("sftp://%s:%s@%s/%s",
				sftpProperties.username(),
				sftpProperties.password(),
				sftpProperties.remoteHost(),
				fileName), options);

			local.copyFrom(remote, Selectors.SELECT_SELF);
			log.info("File '{}' downloaded", fileName);
			local.close();
			remote.close();

		} catch (FileSystemException e) {
			log.info("Error downloading file", e);
		}
	}

	@Override
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

	@Override
	public void deletePreviouslyProcessedFile(Path processedFile) {
		try {
			if (Files.deleteIfExists(processedFile)) {
				log.info("Old file deleted");
			}
		} catch (IOException e) {
			throw new IllegalStateException("Failed to delete file");
		}
	}

	@Override
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
