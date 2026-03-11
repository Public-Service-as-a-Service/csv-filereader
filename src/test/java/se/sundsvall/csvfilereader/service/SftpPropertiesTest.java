package se.sundsvall.csvfilereader.service;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import se.sundsvall.csvfilereader.file.SftpProperties;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class SftpPropertiesTest {

	@Test
	void PropertiesTest() {
		String username = "user";
		String password = "pass";
		String remoteHost = "sftp.server.example";
		Duration connectTimeout = Duration.ofSeconds(15);
		Duration sessionTimeout = Duration.ofSeconds(15);

		var properties = new SftpProperties(
			username, password, remoteHost, connectTimeout, sessionTimeout);

		assertThat(username).isEqualTo(properties.username());
		assertThat(password).isEqualTo(properties.password());
		assertThat(remoteHost).isEqualTo(properties.remoteHost());
		assertThat(connectTimeout).isEqualTo(properties.connectTimeout());
		assertThat(sessionTimeout).isEqualTo(properties.sessionTimeout());
	}
}
