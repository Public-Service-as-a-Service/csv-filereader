package se.sundsvall.csvfilereader.service.utility;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

public class ImportUtilTest {

	@Test
	void returnNullWhenInputNull() {
		assertNull(ImportUtil.nullIfNullString(null));
	}
}
