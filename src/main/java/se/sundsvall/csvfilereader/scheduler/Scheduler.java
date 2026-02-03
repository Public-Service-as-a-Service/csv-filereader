package se.sundsvall.csvfilereader.scheduler;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import se.sundsvall.csvfilereader.file.DownloadService;
import se.sundsvall.csvfilereader.file.FileMover;
import se.sundsvall.csvfilereader.service.CsvImportService;
import se.sundsvall.dept44.scheduling.Dept44Scheduled;

@EnableScheduling
@Configuration
public class Scheduler {

	@Value("${import.temp-download-dir}")
	Path tempDownloadDir;

	@Value("${import.incoming-dir}")
	Path incomingDir;
	@Value("${import.processed-dir}")
	Path processedDir;

	@Value("${import.org-file-name}")
	String orgFileName;
	@Value("${import.emp-file-name}")
	String empFileName;

	private final DownloadService downloadService;
	private final CsvImportService csvImportService;
	private final FileMover fileMover;

	public Scheduler(
		DownloadService downloadService,
		CsvImportService csvImportService,
		FileMover fileMover) {
		this.downloadService = downloadService;
		this.csvImportService = csvImportService;
		this.fileMover = fileMover;
	}

	@Dept44Scheduled(
		cron = "${scheduler.scheduled-org-import.cron}",
		name = "${scheduler.scheduled-org-import.name}",
		lockAtMostFor = "${scheduler.scheduled-org-import.shedlock-lock-at-most-for}",
		maximumExecutionTime = "${scheduler.scheduled-org-import.maximum-execution-time}")

	public void importOrganizationsJob() {

		Path orgCsv = incomingDir.resolve(orgFileName);
		Path oldOrgCsv = processedDir.resolve(orgFileName);

		try {
			// Tillfälligt tills vi fakriskt laddar ner filerna
			Files.copy(tempDownloadDir.resolve(orgFileName), orgCsv, StandardCopyOption.REPLACE_EXISTING);

			downloadService.fetchOrgFile(orgCsv);
			csvImportService.importOrganizations(orgCsv);
			fileMover.deletePreviouslyProcessedFile(oldOrgCsv);
			fileMover.moveFile(orgCsv, processedDir);

		} catch (Exception e) {
			throw new RuntimeException("[ORG]Import failed", e);
		}
	}

	@Dept44Scheduled(
		cron = "${scheduler.scheduled-emp-import.cron}",
		name = "${scheduler.scheduled-emp-import.name}",
		lockAtMostFor = "${scheduler.scheduled-emp-import.shedlock-lock-at-most-for}",
		maximumExecutionTime = "${scheduler.scheduled-emp-import.maximum-execution-time}")

	public void importEmployeesJob() {
		Path empCsv = incomingDir.resolve(empFileName);
		Path oldEmpFile = processedDir.resolve(empFileName);
		try {
			// Tillfälligt tills vi fakriskt laddar ner filerna
			Files.copy(tempDownloadDir.resolve(empFileName), empCsv, StandardCopyOption.REPLACE_EXISTING);

			downloadService.fetchEmpFile(empCsv);
			csvImportService.importEmployee(empCsv);
			fileMover.deletePreviouslyProcessedFile(oldEmpFile);
			fileMover.moveFile(empCsv, processedDir);

		} catch (Exception e) {
			throw new RuntimeException("[EMP] Importing failed", e);
		}
	}
}
