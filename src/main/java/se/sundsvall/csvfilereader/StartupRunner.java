package se.sundsvall.csvfilereader;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import se.sundsvall.csvfilereader.scheduler.Scheduler;
import se.sundsvall.dept44.util.jacoco.ExcludeFromJacocoGeneratedCoverageReport;

@ExcludeFromJacocoGeneratedCoverageReport
@Component
class StartupRunner implements CommandLineRunner {
	private final Scheduler scheduler;

	StartupRunner(Scheduler scheduler) {
		this.scheduler = scheduler;
	}

	@Override
	public void run(String... args) {
		scheduler.importOrganizationsJob();
		scheduler.importEmployeesJob();
	}
}
