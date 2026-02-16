package se.sundsvall.cvsfilereader.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
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
import se.sundsvall.csvfilereader.service.CsvImportService;

@ExtendWith(MockitoExtension.class)
public class CsvImportServiceTest {

	@TempDir
	Path tempDir;

	@Mock
	JdbcTemplate jdbcTemplate;

	CsvImportService importService;

	@BeforeEach
	void setup() throws Exception {
		importService = new CsvImportService(jdbcTemplate);
		var field = CsvImportService.class.getDeclaredField("batchSize");
		field.setAccessible(true);
		field.setInt(importService, 10);
	}

	@Test
	void importOrganizations() throws Exception {
		// Arrange
		Path orgCsv = tempDir.resolve("org.csv");

		Files.writeString(orgCsv, """
			CompanyId,OrgId,OrgName,ParentId,TreeLevel
			1,A,Org A,13,1
			""");

		// Act
		importService.importOrganizations(orgCsv);

		// Assert
		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<Object[]>> captor = (ArgumentCaptor<List<Object[]>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(List.class);

		verify(jdbcTemplate).batchUpdate(anyString(), captor.capture());

		assertEquals(2, captor.getValue().size());
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

		Object[] row = batch.get(0);

		assertEquals("10", row[0]);
		assertEquals("förnamn", row[1]);
		assertEquals("efternamn", row[2]);
		assertEquals("Lärare", row[5]);
		assertEquals("UNKNOWN", row[6]);
		assertEquals("eva@test.com", row[7]);
		assertEquals(true, row[10]);
	}
}
