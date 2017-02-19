/*
 * Copied from {@link org.eclipse.core.runtime.internal.adaptor.EclipseLogHook}.
 */

package com.example.equinox.logging;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLConnection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;

import org.eclipse.core.runtime.adaptor.EclipseStarter;
import org.eclipse.core.runtime.adaptor.LocationManager;
import org.eclipse.osgi.baseadaptor.BaseAdaptor;
import org.eclipse.osgi.baseadaptor.HookConfigurator;
import org.eclipse.osgi.baseadaptor.HookRegistry;
import org.eclipse.osgi.baseadaptor.hooks.AdaptorHook;
import org.eclipse.osgi.framework.internal.core.Constants;
import org.eclipse.osgi.framework.internal.core.FrameworkProperties;
import org.eclipse.osgi.framework.log.FrameworkLog;
import org.eclipse.osgi.internal.baseadaptor.AdaptorUtil;
import org.eclipse.osgi.service.datalocation.Location;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

public class PasswordFilterLogHook implements HookConfigurator, AdaptorHook {
	// The eclipse log file extension */
	private static final String LOG_EXT = ".log"; //$NON-NLS-1$
	
	BaseAdaptor adaptor;

	private PasswordFilterLog frameworkLog;

	public void addHooks(HookRegistry hookRegistry) {
		hookRegistry.addAdaptorHook(this);
	}

	public void initialize(BaseAdaptor adaptor) {
		this.adaptor = adaptor;
	}

	public void frameworkStart(BundleContext context) throws BundleException {
		AdaptorUtil.register(FrameworkLog.class.getName(), adaptor.getFrameworkLog(), context);
		registerPerformanceLog(context);
	}

	public void frameworkStop(BundleContext context) throws BundleException {
		// TODO should unregister service registered a frameworkStart
	}

	public void frameworkStopping(BundleContext context) {
		// do nothing

	}

	public void addProperties(Properties properties) {
		// do nothing
	}

	public URLConnection mapLocationToURLConnection(String location) throws IOException {
		// do nothing
		return null;
	}

	public void handleRuntimeError(Throwable error) {
		// TODO Auto-generated method stub

	}

	public boolean matchDNChain(String pattern, String[] dnChain) {
		// do nothing
		return false;
	}

	public FrameworkLog createFrameworkLog() {
		String logFileProp = FrameworkProperties.getProperty(EclipseStarter.PROP_LOGFILE);
		if (logFileProp != null) {
			frameworkLog = new PasswordFilterLog(new File(logFileProp));
		} else {
			Location location = LocationManager.getConfigurationLocation();
			File configAreaDirectory = null;
			if (location != null)
				// TODO assumes the URL is a file: url
				configAreaDirectory = new File(location.getURL().getFile());

			if (configAreaDirectory != null) {
				String logFileName = Long.toString(System.currentTimeMillis()) + PasswordFilterLogHook.LOG_EXT;
				File logFile = new File(configAreaDirectory, logFileName);
				FrameworkProperties.setProperty(EclipseStarter.PROP_LOGFILE, logFile.getAbsolutePath());
				frameworkLog = new PasswordFilterLog(logFile);
			} else
				frameworkLog = new PasswordFilterLog();
		}
		if ("true".equals(FrameworkProperties.getProperty(EclipseStarter.PROP_CONSOLE_LOG))) //$NON-NLS-1$
			frameworkLog.setConsoleLog(true);
		
		return frameworkLog;
	}

	private void registerPerformanceLog(BundleContext context) {
		Object service = createPerformanceLog();
		String serviceName = FrameworkLog.class.getName();
		Hashtable serviceProperties = new Hashtable(7);
		Dictionary headers = context.getBundle().getHeaders();

		serviceProperties.put(Constants.SERVICE_VENDOR, headers.get(Constants.BUNDLE_VENDOR));
		serviceProperties.put(Constants.SERVICE_RANKING, new Integer(Integer.MIN_VALUE));
		serviceProperties.put(Constants.SERVICE_PID, context.getBundle().getBundleId() + '.' + service.getClass().getName());
		serviceProperties.put(FrameworkLog.SERVICE_PERFORMANCE, Boolean.TRUE.toString());

		context.registerService(serviceName, service, serviceProperties);
	}

	private FrameworkLog createPerformanceLog() {
		String logFileProp = FrameworkProperties.getProperty(EclipseStarter.PROP_LOGFILE);
		if (logFileProp != null) {
			int lastSlash = logFileProp.lastIndexOf(File.separatorChar);
			if (lastSlash > 0) {
				String logFile = logFileProp.substring(0, lastSlash + 1) + "performance.log"; //$NON-NLS-1$
				return new PasswordFilterLog(new File(logFile));
			}
		}
		//if all else fails, write to std err
		return new PasswordFilterLog(new PrintWriter(System.err));
	}
}
