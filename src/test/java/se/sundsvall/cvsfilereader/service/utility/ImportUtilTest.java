package se.sundsvall.cvsfilereader.service.utility;

import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import se.sundsvall.csvfilereader.service.utility.ImportUtil;

public class ImportUtilTest {

	@Test
	void returnNullWhenInputNull() {
		assertNull(ImportUtil.nullIfNullString(null));
	}
}
