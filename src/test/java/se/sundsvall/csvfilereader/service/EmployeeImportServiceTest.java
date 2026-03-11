package se.sundsvall.csvfilereader.service;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EmployeeImportServiceTest {
	@TempDir
	Path tempDir;

	@Mock
	JdbcTemplate jdbcTemplate;

	EmployeeImportService importService;

	@BeforeEach
	void setup() throws Exception {
		importService = new EmployeeImportService(jdbcTemplate);
		var field = EmployeeImportService.class.getDeclaredField("batchSize");
		field.setAccessible(true);
		field.setInt(importService, 10);
	}

	@Test
	void importEmployeeWithUnknownOrgTest() throws Exception {
		// Arrange
		Path empCsv = tempDir.resolve("emp.csv");
		Files.writeString(empCsv, """
			PersonId;Givenname;Lastname;WorkMobile;WorkPhone;Title;OrgId;PrimaryEMailAddress;ManagerId;ManagerCode
			10;förnamn;efternamn;;;Lärare;NoOrg;eva@test.com;;
			""", Charset.forName("Windows-1252"));

		when(jdbcTemplate.queryForList(anyString(), eq(String.class), any(Object[].class)))
			.thenReturn(List.of());

		// Act
		importService.importEmployee(empCsv);

		// Assert
		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<Object[]>> captor = (ArgumentCaptor<List<Object[]>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(List.class);

		verify(jdbcTemplate).batchUpdate(anyString(), captor.capture());

		List<Object[]> batch = captor.getValue();
		assertThat(1).isEqualTo(batch.size());

		Object[] row = batch.getFirst();

		assertThat("10").isEqualTo(row[0]);
		assertThat("förnamn").isEqualTo(row[1]);
		assertThat("efternamn").isEqualTo(row[2]);
		assertThat("Lärare").isEqualTo(row[5]);
		assertThat("UNKNOWN").isEqualTo(row[6]);
		assertThat("eva@test.com").isEqualTo(row[7]);
		assertThat(true).isEqualTo(row[10]);
	}

	@Test
	void importEmployee_throwsException() throws Exception {
		// Arrange
		Path missing = tempDir.resolve("missing.csv");

		// Act
		RuntimeException exception = assertThrows(RuntimeException.class, () -> importService.importEmployee(missing));

		// Assert
		assertTrue(exception.getMessage().startsWith("Error Importing organization from:"));
	}
}
