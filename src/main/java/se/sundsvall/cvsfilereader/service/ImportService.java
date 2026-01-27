package se.sundsvall.cvsfilereader.service;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import se.sundsvall.cvsfilereader.dto.EmployeeDTO;
import se.sundsvall.cvsfilereader.dto.OrganizationDTO;

@Service
public class ImportService {

	private static final int BatchSize = 200;
	private static final Logger log = LoggerFactory.getLogger(ImportService.class);

	private final JdbcTemplate jdbcTemplate;

	public ImportService(JdbcTemplate jdbcTemplate) {

		this.jdbcTemplate = jdbcTemplate;
	}

	public void importOrganizations(Path orgCsv) {

		String sql = """
			INSERT INTO organizations (company_id, org_id, org_name, parent_id, tree_level)
			VALUES (?, ?, ?, ?, ?)
			ON DUPLICATE KEY UPDATE
			  org_name = VALUES(org_name),
			  parent_id = VALUES(parent_id),
			  tree_level = VALUES(tree_level)
			""";

		CsvMapper csvMapper = new CsvMapper();
		CsvSchema schema = buildOrganizationSchema();

		List<Object[]> batch = new ArrayList<>(BatchSize);
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

				if (batch.size() >= BatchSize) {
					jdbcTemplate.batchUpdate(sql, batch);
					processed += batch.size();
					batch.clear();
					log.info("[ORG] upsert complete. Rows sent to DB: {}", processed);
				}
			}
			if (!batch.isEmpty()) {
				jdbcTemplate.batchUpdate(sql, batch);
				processed += batch.size();
			}
			log.info("[ORG] final upsert complete. Rows sent to DB: {}", processed);

		} catch (IOException e) {
			throw new RuntimeException("Error Importing organization from:" + orgCsv.getFileName().toAbsolutePath(), e);
		}

	}

	public void importEmployee(Path empCsv) {

		String sql = """
			INSERT INTO employees (uuid, first_name, last_name, work_mobile, work_phone, work_title, org_id, email, manager_id, manager_code, active_employee)
			VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
			""";

		CsvMapper csvMapper = new CsvMapper();
		CsvSchema schema = buildEmployeeSchema();

		List<Object[]> batch = new ArrayList<>(BatchSize);
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

				if (batch.size() >= BatchSize) {
					nullOutUnknownOrgIds(batch);
					jdbcTemplate.batchUpdate(sql, batch);
					processed += batch.size();
					batch.clear();
					log.info("[EMP] upsert complete. Rows sent to DB: {}", processed);

				}

			}
			if (!batch.isEmpty()) {
				nullOutUnknownOrgIds(batch);
				jdbcTemplate.batchUpdate(sql, batch);
				processed += batch.size();
			}
			log.info("[EMP] final upsert complete. Rows sent to DB: {}", processed);

		} catch (IOException e) {
			throw new RuntimeException("Error Importing organization from:" + empCsv.getFileName().toAbsolutePath(), e);
		}

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

	// gittan gav denna
	private void nullOutUnknownOrgIds(List<Object[]> empBatch) {
		final int ORG_ID_INDEX = 6;
		final int EMAIL_INDEX = 7;

		var orgIds = empBatch.stream()
			.map(arr -> (String) arr[ORG_ID_INDEX])
			.filter(s -> s != null && !s.isBlank())
			.distinct()
			.toList();

		if (orgIds.isEmpty())
			return;

		String inSql = "SELECT org_id FROM organizations WHERE org_id IN (" +
			orgIds.stream().map(x -> "?")
				.collect(java.util.stream.Collectors.joining(",")) +   // <-- comma, not semicolon
			")";

		var existing = new java.util.HashSet<>(
			jdbcTemplate.queryForList(inSql, String.class, orgIds.toArray()));

		for (Object[] row : empBatch) {
			String orgId = (String) row[ORG_ID_INDEX];
			if (orgId != null && !orgId.isBlank() && !existing.contains(orgId)) {
				row[ORG_ID_INDEX] = null;

				log.warn("[EMP] org_id '{}' not found in organizations -> inserting employee with org_id=NULL (email={})",
					orgId, row[EMAIL_INDEX]);
			}
		}
	}

}
