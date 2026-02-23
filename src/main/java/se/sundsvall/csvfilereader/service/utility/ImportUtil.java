package se.sundsvall.csvfilereader.service.utility;

public class ImportUtil {

	public static String nullIfNullString(String string) {
		if (string == null)
			return null;
		String trimmed = string.trim();
		if (trimmed.isEmpty() || trimmed.equalsIgnoreCase("NULL"))
			return null;
		return trimmed;
	}
}
