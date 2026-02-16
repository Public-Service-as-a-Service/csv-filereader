package se.sundsvall.cvsfilereader.scheduler;

import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import se.sundsvall.csvfilereader.file.DownloadService;
import se.sundsvall.csvfilereader.file.FileMover;
import se.sundsvall.csvfilereader.scheduler.Scheduler;
import se.sundsvall.csvfilereader.service.CsvImportService;

public class SchedulerTest {

	@TempDir
	Path tempDir;

	@Test
	void importOrganizationsJobTest() throws Exception {
		// arrange
		DownloadService downloadService = Mockito.mock(DownloadService.class);
		CsvImportService csvImportService = Mockito.mock(CsvImportService.class);
		FileMover fileMover = Mockito.mock(FileMover.class);

		Scheduler scheduler = new Scheduler(downloadService, csvImportService, fileMover);

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
		verify(downloadService).fetchOrgFile(expectedFile);
		verify(csvImportService).importOrganizations(expectedFile);
		verify(fileMover).deletePreviouslyProcessedFile(expectedOldFile);
		verify(fileMover).moveFile(expectedFile, processedDir);
	}

	@Test
	void importEmployeesJobTest() throws Exception {
		// arrange
		DownloadService downloadService = Mockito.mock(DownloadService.class);
		CsvImportService csvImportService = Mockito.mock(CsvImportService.class);
		FileMover fileMover = Mockito.mock(FileMover.class);

		Scheduler scheduler = new Scheduler(downloadService, csvImportService, fileMover);

		Path tempDownloadDir = tempDir.resolve("temp-download");
		Path incomingDir = tempDir.resolve("incoming");
		Path processedDir = tempDir.resolve("processed");

		Files.createDirectories(tempDownloadDir);
		Files.createDirectories(incomingDir);
		Files.createDirectories(processedDir);

		String empCsv = "emp.csv";

		Path empCsvPath = tempDownloadDir.resolve(empCsv);
		Files.writeString(empCsvPath, "PersonId;Givenname;Lastname\n123;Alice;Andersson\n");

		setField(scheduler, "tempDownloadDir", tempDownloadDir);
		setField(scheduler, "incomingDir", incomingDir);
		setField(scheduler, "processedDir", processedDir);
		setField(scheduler, "empFileName", empCsv);

		Path expectedFile = incomingDir.resolve(empCsv);
		Path expectedOldFile = processedDir.resolve(empCsv);

		// act
		scheduler.importEmployeesJob();

		assert Files.exists(expectedFile);

		// and dependencies were called with correct paths
		verify(downloadService).fetchEmpFile(expectedFile);
		verify(csvImportService).importEmployee(expectedFile);
		verify(fileMover).deletePreviouslyProcessedFile(expectedOldFile);
		verify(fileMover).moveFile(expectedFile, processedDir);
	}
}
