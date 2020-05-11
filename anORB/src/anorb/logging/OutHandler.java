package anorb.logging;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class OutHandler extends Handler {

	private Date[] dat;

	private MessageFormat formater;

	private PrintStream out;

	public OutHandler(PrintStream out,String format) {
		this.formater = new MessageFormat(format);
		this.out = out;
		dat = new Date[1];
		dat[0] = new Date();
	}

	@Override
	public void publish(LogRecord record) {
		if (!isLoggable(record)) {
		    return;
		}
		dat[0].setTime(record.getMillis());
		StringBuffer sb = new StringBuffer();
		formater.format(dat, sb, null);
		out.print(sb.toString());
		out.print(' ');
		out.println(record.getMessage());
		if (record.getThrown() != null) {
		    try {
		        StringWriter sw = new StringWriter();
		        PrintWriter pw = new PrintWriter(sw);
		        record.getThrown().printStackTrace(pw);
		        pw.close();
			out.println(sw.toString());
		    } catch (Exception ex) {
		    }
		}
	}

	@Override
	public void flush() {
		out.flush();
	}

	@Override
	public void close() throws SecurityException {
		out.close();
	}

}
