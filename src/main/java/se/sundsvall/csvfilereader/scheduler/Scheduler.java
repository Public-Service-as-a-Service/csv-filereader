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

	private final Path incomingDir;
	private final Path processedDir;
	private final String orgFileName;
	private final String empFileName;

	private final DownloadService downloadService;
	private final CsvImportService csvImportService;
	private final FileMover fileMover;

	private final Path tempDownloadDir;

	public Scheduler(
		@Value("${import.incoming-dir}") Path incomingDir,
		@Value("${import.processed-dir}") Path processedDir,
		@Value("${import.org-file-name}") String orgFileName,
		@Value("${import.emp-file-name}") String empFileName,
		DownloadService downloadService,
		CsvImportService csvImportService,
		FileMover fileMover,

		@Value("${import.temp-download-dir}") Path tempDownloadDir) {
		this.incomingDir = incomingDir;
		this.processedDir = processedDir;
		this.orgFileName = orgFileName;
		this.empFileName = empFileName;
		this.downloadService = downloadService;
		this.csvImportService = csvImportService;
		this.fileMover = fileMover;

		this.tempDownloadDir = tempDownloadDir;

	}

	@Dept44Scheduled(
		cron = "${scheduler.scheduled-task.cron}",
		name = "${scheduler.scheduled-task.name}",
		lockAtMostFor = "${scheduler.scheduled-task.shedlock-lock-at-most-for}",
		maximumExecutionTime = "${scheduler.scheduled-task.maximum-execution-time}")

	public void launchJobs() {

		Path orgCsv = incomingDir.resolve(orgFileName);
		Path empCsv = incomingDir.resolve(empFileName);
		Path oldOrgCsv = processedDir.resolve(orgFileName);
		Path oldEmpFile = processedDir.resolve(empFileName);

		try {
			// tills vi fakriskt laddar ner filerna
			Files.copy(tempDownloadDir.resolve(orgFileName), orgCsv, StandardCopyOption.REPLACE_EXISTING);
			Files.copy(tempDownloadDir.resolve(empFileName), empCsv, StandardCopyOption.REPLACE_EXISTING);

			downloadService.fetchOrgFile(orgCsv);
			downloadService.fetchEmpFile(empCsv);

			csvImportService.importOrganizations(orgCsv);
			csvImportService.importEmployee(empCsv);

			fileMover.deletePreviouslyProcessedFile(oldOrgCsv);
			fileMover.deletePreviouslyProcessedFile(oldEmpFile);

			fileMover.moveEmployeeFiles(empCsv, processedDir);
			fileMover.moveOrganizationFiles(orgCsv, processedDir);

		} catch (Exception e) {
			throw new RuntimeException("import failed", e);
		}
	}
}
