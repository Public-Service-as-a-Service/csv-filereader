package se.sundsvall.csvfilereader.file;

import java.nio.file.Path;
import org.apache.commons.vfs2.FileObject;
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
public class SftpFileManager extends AbstractFileManager {

	private static final Logger log = LoggerFactory.getLogger(SftpFileManager.class);
	SftpProperties sftpProperties;

	public SftpFileManager(SftpProperties sftpProperties) {
		this.sftpProperties = sftpProperties;
	}

	@Override
	public void downloadFile(Path dir, String fileName) {
		FileObject local = null;
		FileObject remote = null;
		try {
			FileSystemManager manager = VFS.getManager();
			FileSystemOptions options = new FileSystemOptions();

			var builder = SftpFileSystemConfigBuilder.getInstance();
			builder.setConnectTimeout(options, sftpProperties.connectTimeout());
			builder.setSessionTimeout(options, sftpProperties.sessionTimeout());

			local = manager.resolveFile(dir.resolve(fileName).toUri().toString());
			remote = manager.resolveFile(String.format("sftp://%s:%s@%s/%s",
				sftpProperties.username(),
				sftpProperties.password(),
				sftpProperties.remoteHost(),
				fileName), options);

			local.copyFrom(remote, Selectors.SELECT_SELF);
			log.info("File '{}' downloaded", fileName);

		} catch (FileSystemException e) {
			log.info("Error downloading file", e);
		} finally {
			if (local != null) {
				try {
					local.close();
				} catch (FileSystemException e) {
					log.warn("Failed to close local file", e);
				}
			}
			if (remote != null) {
				try {
					remote.close();
				} catch (FileSystemException e) {
					log.warn("Failed to close remote file", e);
				}
			}
		}
	}
}
