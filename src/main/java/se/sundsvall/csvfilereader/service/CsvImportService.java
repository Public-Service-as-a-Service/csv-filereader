package se.sundsvall.csvfilereader.service;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import se.sundsvall.csvfilereader.db.dto.EmployeeDTO;
import se.sundsvall.csvfilereader.db.dto.OrganizationDTO;

@Service
public class CsvImportService {

	@Value("${import.batch-size}")
	private int batchSize;

	private static final Logger log = LoggerFactory.getLogger(CsvImportService.class);

	private final JdbcTemplate jdbcTemplate;

	public CsvImportService(JdbcTemplate jdbcTemplate) {

		this.jdbcTemplate = jdbcTemplate;
	}

	public void importOrganizations(Path orgCsv) {

		String sql = """
			INSERT INTO organization (company_id, org_id, org_name, parent_org_id, tree_level)
			VALUES (?, ?, ?, ?, ?)
			ON DUPLICATE KEY UPDATE
			  org_name = VALUES(org_name),
			  parent_org_id = VALUES(parent_org_id),
			  tree_level = VALUES(tree_level)
			""";

		CsvMapper csvMapper = new CsvMapper();
		CsvSchema schema = buildOrganizationSchema();

		List<Object[]> batch = new ArrayList<>(batchSize);
		int processed = 0;

		try (BufferedReader reader = Files.newBufferedReader(orgCsv, StandardCharsets.UTF_8)) {

			MappingIterator<OrganizationDTO> it = csvMapper.readerFor(OrganizationDTO.class)
				.with(schema)
				.readValues(reader);
			while (it.hasNext()) {

				var row = it.next();

				batch.add(new Object[] {
					nullIfNullString(row.CompanyId),
					nullIfNullString(row.OrgId),
					nullIfNullString(row.OrgName),
					nullIfNullString(row.ParentId),
					nullIfNullString(row.TreeLevel),

				});

				if (batch.size() >= batchSize) {
					jdbcTemplate.batchUpdate(sql, batch);
					processed += batch.size();
					batch.clear();
					log.info("[ORG] upsert complete. Rows sent to DB: {}", processed);
				}
			}
			if (!batch.isEmpty()) {
				batch.add(new Object[] {
					// CompanyId,OrgId,OrgName,ParentId,TreeLevel
					1, "UNKNOWN", "Övriga personer", 13, 2
				});
				log.info("[ORG] creating organization for UNKNOWN");

				jdbcTemplate.batchUpdate(sql, batch);
				processed += batch.size();
			}
			log.info("[ORG] final upsert complete. Rows sent to DB: {}", processed);

		} catch (IOException e) {
			throw new RuntimeException("Error Importing organization from:" + orgCsv.getFileName().toAbsolutePath(), e);
		}

	}

	public void importEmployee(Path empCsv) {

		var importStartedAt = jdbcTemplate.queryForObject("SELECT CURRENT_TIMESTAMP()", java.sql.Timestamp.class);

		String sql = """
			INSERT INTO employee (person_id, first_name, last_name, work_mobile, work_phone, work_title, org_id, email, manager_id, manager_code, active_employee)
			VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
				ON DUPLICATE KEY UPDATE
				    first_name      = VALUES(first_name),
				    last_name       = VALUES(last_name),
				    work_mobile     = VALUES(work_mobile),
				    work_phone      = VALUES(work_phone),
				    work_title      = VALUES(work_title),
				    email           = VALUES(email),
				    manager_id      = VALUES(manager_id),
				    manager_code    = VALUES(manager_code),
				    active_employee = VALUES(active_employee),
				    updated_at      = CURRENT_TIMESTAMP
			""";

		CsvMapper csvMapper = new CsvMapper();
		CsvSchema schema = buildEmployeeSchema();

		List<Object[]> batch = new ArrayList<>(batchSize);
		int processed = 0;

		try (BufferedReader reader = Files.newBufferedReader(empCsv, StandardCharsets.UTF_8)) {
			MappingIterator<EmployeeDTO> it = csvMapper.readerFor(EmployeeDTO.class)
				.with(schema)
				.readValues(reader);
			while (it.hasNext()) {

				var row = it.next();

				batch.add(new Object[] {
					nullIfNullString(row.PersonId),
					nullIfNullString(row.Givenname),
					nullIfNullString(row.Lastname),
					nullIfNullString(row.WorkMobile),
					nullIfNullString(row.WorkPhone),
					nullIfNullString(row.Title),
					nullIfNullString(row.OrgId),
					nullIfNullString(row.PrimaryEMailAddress),
					nullIfNullString(row.ManagerId),
					nullIfNullString(row.ManagerCode),
					true
				});

				if (batch.size() >= batchSize) {
					determineUnknownOrgIds(batch);
					jdbcTemplate.batchUpdate(sql, batch);
					processed += batch.size();
					batch.clear();
					log.info("[EMP] upsert complete. Rows sent to DB: {}", processed);

				}

			}
			if (!batch.isEmpty()) {
				determineUnknownOrgIds(batch);
				jdbcTemplate.batchUpdate(sql, batch);
				processed += batch.size();
			}
			log.info("[EMP] final upsert complete. Rows sent to DB: {}", processed);

		} catch (IOException e) {
			throw new RuntimeException("Error Importing organization from:" + empCsv.getFileName().toAbsolutePath(), e);
		}

		int deactivated = jdbcTemplate.update("""
			    UPDATE employee
			    SET active_employee = false
			    WHERE active_employee = true
			      AND (updated_at IS NULL OR updated_at < ?)
			""", importStartedAt);

		log.info("[EMP] non updated employees set to inactive: {}", deactivated);

	}

	private CsvSchema buildEmployeeSchema() {
		return CsvSchema.emptySchema()
			.withHeader()
			.withColumnSeparator(';');
	}

	private CsvSchema buildOrganizationSchema() {
		return CsvSchema.emptySchema()
			.withHeader()
			.withColumnSeparator(',');
	}

	private static String nullIfNullString(String string) {
		if (string == null)
			return null;
		String trimmed = string.trim();
		if (trimmed.isEmpty() || trimmed.equalsIgnoreCase("NULL"))
			return null;
		return trimmed;
	}

	private void determineUnknownOrgIds(List<Object[]> empBatch) {
		final int orgIdIndex = 6;
		final int emailIndex = 7;

		var orgIds = empBatch.stream()
			.map(arr -> (String) arr[orgIdIndex])
			.filter(s -> s != null && !s.isBlank())
			.distinct()
			.toList();

		if (orgIds.isEmpty())
			return;

		String inSql = "SELECT org_id FROM organization WHERE org_id IN (" +
			orgIds.stream().map(x -> "?")
				.collect(java.util.stream.Collectors.joining(",")) +
			")";

		var existing = new java.util.HashSet<>(
			jdbcTemplate.queryForList(inSql, String.class, orgIds.toArray()));

		for (Object[] row : empBatch) {
			String orgId = (String) row[orgIdIndex];
			if (orgId != null && !orgId.isBlank() && !existing.contains(orgId)) {
				row[orgIdIndex] = "UNKNOWN";

				log.warn("[EMP] org_id '{}' not found,setting to UNKNOWN for user: (email={})",
					orgId, row[emailIndex]);
			}
		}
	}

}
