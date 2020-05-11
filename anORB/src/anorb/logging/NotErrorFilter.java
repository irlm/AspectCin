package anorb.logging;

import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class NotErrorFilter implements Filter {
	private static final int LIMIT = Level.WARNING.intValue();

	public boolean isLoggable(LogRecord record) {
		return record.getLevel().intValue() < LIMIT;
	}

}
