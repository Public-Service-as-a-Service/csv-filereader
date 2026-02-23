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
import se.sundsvall.csvfilereader.db.dto.OrganizationDTO;
import se.sundsvall.csvfilereader.service.utility.ImportUtil;

@Service
public class OrganizationImportService {
	@Value("${import.organization-batch-size}")
	private int batchSize;

	private static final Logger log = LoggerFactory.getLogger(OrganizationImportService.class);

	private final JdbcTemplate jdbcTemplate;

	public OrganizationImportService(JdbcTemplate jdbcTemplate) {
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
					ImportUtil.nullIfNullString(row.CompanyId),
					ImportUtil.nullIfNullString(row.OrgId),
					ImportUtil.nullIfNullString(row.OrgName),
					ImportUtil.nullIfNullString(row.ParentId),
					ImportUtil.nullIfNullString(row.TreeLevel),

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

	private CsvSchema buildOrganizationSchema() {
		return CsvSchema.emptySchema()
			.withHeader()
			.withColumnSeparator(',');
	}
}
