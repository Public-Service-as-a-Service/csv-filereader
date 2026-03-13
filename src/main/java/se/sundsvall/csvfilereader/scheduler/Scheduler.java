package se.sundsvall.csvfilereader.scheduler;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import se.sundsvall.csvfilereader.file.FileManager;
import se.sundsvall.csvfilereader.service.EmployeeImportService;
import se.sundsvall.csvfilereader.service.OrganizationImportService;
import se.sundsvall.dept44.scheduling.Dept44Scheduled;

@EnableScheduling
@Configuration
public class Scheduler {

	@Value("${import.incoming-dir}")
	private Path incomingDir;
	@Value("${import.processed-dir}")
	private Path processedDir;

	@Value("${import.org-file-name}")
	private String orgFileName;
	@Value("${import.emp-file-name}")
	private String empFileName;

	private final EmployeeImportService employeeImportService;
	private final OrganizationImportService organizationImportService;
	private final FileManager fileManager;

	public Scheduler(EmployeeImportService employeeImportService, OrganizationImportService organizationImportService,
		FileManager fileManager) {
		this.employeeImportService = employeeImportService;
		this.organizationImportService = organizationImportService;
		this.fileManager = fileManager;
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

			fileManager.downloadFile(incomingDir, orgFileName);
			organizationImportService.importOrganizations(orgCsv);
			fileManager.deletePreviouslyProcessedFile(oldOrgCsv);
			fileManager.moveFile(orgCsv, processedDir);

		} catch (Exception e) {
			throw new RuntimeException("[ORG] Import failed", e);
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

			fileManager.downloadFile(incomingDir, empFileName);
			employeeImportService.importEmployee(empCsv);
			fileManager.deletePreviouslyProcessedFile(oldEmpFile);
			fileManager.moveFile(empCsv, processedDir);

		} catch (Exception e) {
			throw new RuntimeException("[EMP] Import failed", e);
		}
	}
}
