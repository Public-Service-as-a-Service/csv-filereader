package se.sundsvall.cvsfilereader;

import static org.springframework.boot.SpringApplication.run;

import se.sundsvall.dept44.ServiceApplication;
import se.sundsvall.dept44.util.jacoco.ExcludeFromJacocoGeneratedCoverageReport;

@ExcludeFromJacocoGeneratedCoverageReport
@ServiceApplication
public class Application {
	public static void main(final String... args) {
		run(Application.class, args);
	}
}
