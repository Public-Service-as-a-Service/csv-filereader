package se.sundsvall.cvsfilereader.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

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
import se.sundsvall.csvfilereader.service.OrganizationImportService;

@ExtendWith(MockitoExtension.class)
public class OrganizationImportServiceTest {

	@TempDir
	Path tempDir;

	@Mock
	JdbcTemplate jdbcTemplate;

	OrganizationImportService importService;

	@BeforeEach
	void setup() throws Exception {
		importService = new OrganizationImportService(jdbcTemplate);
		var field = OrganizationImportService.class.getDeclaredField("batchSize");
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
	void importOrganization_throwsException() throws Exception {
		// Arrange
		Path missing = tempDir.resolve("missing.csv");

		// Act
		RuntimeException exception = assertThrows(RuntimeException.class, () -> importService.importOrganizations(missing));

		// Assert
		assertTrue(exception.getMessage().startsWith("Error Importing organization from:"));
	}
}
