package se.sundsvall.cvsfilereader.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import se.sundsvall.csvfilereader.service.EmployeeImportService;

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
	void importEmployeeWithUnkownOrgTest() throws Exception {
		// Arrange
		Path empCsv = tempDir.resolve("emp.csv");
		Files.writeString(empCsv, """
			PersonId;Givenname;Lastname;WorkMobile;WorkPhone;Title;OrgId;PrimaryEMailAddress;ManagerId;ManagerCode
			10;förnamn;efternamn;;;Lärare;NoOrg;eva@test.com;;
			""");

		when(jdbcTemplate.queryForList(anyString(), eq(String.class), any(Object[].class)))
			.thenReturn(List.of());

		// Act
		importService.importEmployee(empCsv);

		// Assert
		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<Object[]>> captor = (ArgumentCaptor<List<Object[]>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(List.class);

		verify(jdbcTemplate).batchUpdate(anyString(), captor.capture());

		List<Object[]> batch = captor.getValue();
		assertEquals(1, batch.size());

		Object[] row = batch.getFirst();

		assertEquals("10", row[0]);
		assertEquals("förnamn", row[1]);
		assertEquals("efternamn", row[2]);
		assertEquals("Lärare", row[5]);
		assertEquals("UNKNOWN", row[6]);
		assertEquals("eva@test.com", row[7]);
		assertEquals(true, row[10]);
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
