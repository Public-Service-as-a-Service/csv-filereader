package se.sundsvall.cvsfilereader.scheduler;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import se.sundsvall.csvfilereader.file.FileManager;
import se.sundsvall.csvfilereader.scheduler.Scheduler;
import se.sundsvall.csvfilereader.service.EmployeeImportService;
import se.sundsvall.csvfilereader.service.OrganizationImportService;

public class SchedulerTest {

	@TempDir
	Path tempDir;

	@Test
	void importOrganizationsJobTest() throws Exception {
		// arrange
		EmployeeImportService employeeImportService = Mockito.mock(EmployeeImportService.class);
		OrganizationImportService organizationImportService = Mockito.mock(OrganizationImportService.class);
		FileManager fileManager = Mockito.mock(FileManager.class);

		Scheduler scheduler = new Scheduler(employeeImportService, organizationImportService, fileManager);

		Path tempDownloadDir = tempDir.resolve("temp-download");
		Path incomingDir = tempDir.resolve("incoming");
		Path processedDir = tempDir.resolve("processed");

		Files.createDirectories(tempDownloadDir);
		Files.createDirectories(incomingDir);
		Files.createDirectories(processedDir);

		String orgCsv = "org.csv";

		Path orgCsvPath = tempDownloadDir.resolve(orgCsv);
		Files.writeString(orgCsvPath, "CompanyId,OrgId,OrgName,ParentId,TreeLevel\n1,A,Root,,0\n");

		setField(scheduler, "tempDownloadDir", tempDownloadDir);
		setField(scheduler, "incomingDir", incomingDir);
		setField(scheduler, "processedDir", processedDir);
		setField(scheduler, "orgFileName", orgCsv);

		Path expectedFile = incomingDir.resolve(orgCsv);
		Path expectedOldFile = processedDir.resolve(orgCsv);

		// act
		scheduler.importOrganizationsJob();

		assert Files.exists(expectedFile);

		// and dependencies were called with correct paths
		verify(organizationImportService).importOrganizations(expectedFile);
		verify(fileManager).deletePreviouslyProcessedFile(expectedOldFile);
		verify(fileManager).moveFile(expectedFile, processedDir);
	}

	@Test
	void importEmployeesJobTest() throws Exception {
		// arrange
		EmployeeImportService employeeImportService = Mockito.mock(EmployeeImportService.class);
		OrganizationImportService organizationImportService = Mockito.mock(OrganizationImportService.class);
		FileManager fileManager = Mockito.mock(FileManager.class);

		Scheduler scheduler = new Scheduler(employeeImportService, organizationImportService, fileManager);

		Path tempDownloadDir = tempDir.resolve("temp-download");
		Path incomingDir = tempDir.resolve("incoming");
		Path processedDir = tempDir.resolve("processed");

		Files.createDirectories(tempDownloadDir);
		Files.createDirectories(incomingDir);
		Files.createDirectories(processedDir);

		String empCsv = "emp.csv";

		Path empCsvPath = tempDownloadDir.resolve(empCsv);
		Files.writeString(empCsvPath, "PersonId;Givenname;Lastname;123;Alice;Andersson");

		setField(scheduler, "tempDownloadDir", tempDownloadDir);
		setField(scheduler, "incomingDir", incomingDir);
		setField(scheduler, "processedDir", processedDir);
		setField(scheduler, "empFileName", empCsv);

		Path expectedFile = incomingDir.resolve(empCsv);
		Path expectedOldFile = processedDir.resolve(empCsv);

		// act
		scheduler.importEmployeesJob();

		assert Files.exists(expectedFile);

		verify(employeeImportService).importEmployee(expectedFile);
		verify(fileManager).deletePreviouslyProcessedFile(expectedOldFile);
		verify(fileManager).moveFile(expectedFile, processedDir);
	}

	@Test
	void importEmployeeJob_throwsException() throws IOException {
		EmployeeImportService employeeImportService = Mockito.mock(EmployeeImportService.class);
		OrganizationImportService organizationImportService = Mockito.mock(OrganizationImportService.class);
		FileManager fileManager = Mockito.mock(FileManager.class);

		Scheduler scheduler = new Scheduler(employeeImportService, organizationImportService, fileManager);

		Path tempDownloadDir = tempDir.resolve("temp-download");
		Path incomingDir = tempDir.resolve("incoming");
		Path processedDir = tempDir.resolve("processed");

		Files.createDirectories(tempDir.resolve(tempDownloadDir));
		Files.createDirectories(tempDir.resolve(incomingDir));
		Files.createDirectories(tempDir.resolve(processedDir));

		Files.writeString(tempDir.resolve(tempDownloadDir).resolve("emp.csv"), "test");

		setField(scheduler, "tempDownloadDir", tempDownloadDir);
		setField(scheduler, "incomingDir", incomingDir);
		setField(scheduler, "processedDir", processedDir);
		setField(scheduler, "empFileName", "emp.csv");

		doThrow(new RuntimeException("exception")).when(employeeImportService).importEmployee(any(Path.class));

		RuntimeException exception = assertThrows(RuntimeException.class, scheduler::importEmployeesJob);

		assertTrue(exception.getMessage().startsWith("[EMP] Import failed"));
	}

	@Test
	void importOrganizationJob_throwsException() throws IOException {
		EmployeeImportService employeeImportService = Mockito.mock(EmployeeImportService.class);
		OrganizationImportService organizationImportService = Mockito.mock(OrganizationImportService.class);
		FileManager fileManager = Mockito.mock(FileManager.class);

		Scheduler scheduler = new Scheduler(employeeImportService, organizationImportService, fileManager);

		Path tempDownloadDir = tempDir.resolve("temp-download");
		Path incomingDir = tempDir.resolve("incoming");
		Path processedDir = tempDir.resolve("processed");

		Files.createDirectories(tempDir.resolve(tempDownloadDir));
		Files.createDirectories(tempDir.resolve(incomingDir));
		Files.createDirectories(tempDir.resolve(processedDir));

		Files.writeString(tempDir.resolve(tempDownloadDir).resolve("org.csv"), "test");

		setField(scheduler, "tempDownloadDir", tempDownloadDir);
		setField(scheduler, "incomingDir", incomingDir);
		setField(scheduler, "processedDir", processedDir);
		setField(scheduler, "orgFileName", "org.csv");

		doThrow(new RuntimeException("exception")).when(organizationImportService).importOrganizations(any(Path.class));

		RuntimeException exception = assertThrows(RuntimeException.class, scheduler::importOrganizationsJob);

		assertTrue(exception.getMessage().startsWith("[ORG] Import failed"));
	}
}
