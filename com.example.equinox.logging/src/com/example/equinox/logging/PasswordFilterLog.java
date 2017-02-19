package com.example.equinox.logging;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import org.eclipse.core.runtime.adaptor.EclipseLog;

public class PasswordFilterLog extends EclipseLog {

	private static final String[] PASSWORD_ARGS = {"-sourcepassword", "-targetpassword"};

	public PasswordFilterLog() {
		super();
	}

	public PasswordFilterLog(File outFile) {
		super(outFile);
	}

	public PasswordFilterLog(Writer writer) {
		super(writer);
	}

	@Override
	protected void writeArgs(String header, String[] args) throws IOException {
		if (args == null || args.length == 0)
			return;
		write(header);
		for (int i = 0; i < args.length; i++) {
			//mask out the password argument for security
			if (i > 0 && isPasswordArgument(args[i - 1]))
				write(" (omitted)"); //$NON-NLS-1$
			else
				write(" " + args[i]); //$NON-NLS-1$
		}
		writeln();
	}
	
	private boolean isPasswordArgument(String arg) {
		for(String p_arg : PASSWORD_ARGS) {
			if(p_arg.equals(arg)) {
				return true;
			}
		}
		
		return false;
	}
}
