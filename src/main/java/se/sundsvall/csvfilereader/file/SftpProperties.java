package se.sundsvall.csvfilereader.file;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("integration.sftp")
public record SftpProperties(
	String username,
	String password,
	String remoteHost,
	Duration connectTimeout,
	Duration sessionTimeout) {
}
