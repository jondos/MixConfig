/*
 Copyright (c) 2000 - 2006, The JAP-Team
 All rights reserved.
 Redistribution and use in source and binary forms, with or without modification,
 are permitted provided that the following conditions are met:

  - Redistributions of source code must retain the above copyright notice,
 this list of conditions and the following disclaimer.

  - Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation and/or
 other materials provided with the distribution.

  - Neither the name of the University of Technology Dresden, Germany nor the names of its contributors
 may be used to endorse or promote products derived from this software without specific
 prior written permission.


 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS
 OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS
 BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE
 */
package platform;

import gui.dialog.JAPDialog;
import gui.help.JAPHelp.IExternalEMailCaller;
import gui.help.JAPHelp.IExternalURLCaller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;
import java.util.Vector;

import logging.LogHolder;
import logging.LogLevel;
import logging.LogType;
import anon.util.ClassUtil;

/**
 * This abstract class provides access to OS-specific implementations of certain
 * functions. It tries to instantiate an OS-specific class by determining on which
 * operating system JAP is currently running.
 */
public abstract class AbstractOS implements IExternalURLCaller, IExternalEMailCaller
{
	public static final String URL_MAIL_TO = "mailto:";

	/**
	 * Make sure that the default OS is the last OS in the array.
	 */
	private static Class[] REGISTERED_PLATFORM_CLASSES =
		{
		LinuxOS.class, WindowsOS.class, MacOS.class, UnknownOS.class};

	private static final String[] BROWSERLIST =
		{
		"firefox", "iexplore", "explorer", "mozilla", "konqueror", "mozilla-firefox", "opera"
	};

	/**
	 * The instanciated operation system class.
	 * (no, ms_operating system does not mean only Microsoft OS are supported... ;-))
	 */
	private static AbstractOS ms_operatingSystem;

	private IURLErrorNotifier m_notifier;
	private AbstractURLOpener m_URLOpener;
	private Properties m_envVars;

	private static File ms_tmpDir;
	
	static
	{
		// Needs to be done according to the JDK because java.io.tmpdir
		// seems to return wrong values on some Linux and Solaris systems.
		String tmpDir = System.getProperty("java.io.tmpdir");
		if(tmpDir.compareTo("/var/tmp/") == 0)
			tmpDir = "/tmp/";

		// Assure that the tmpDir has a trailing File.seperator
		if(tmpDir.lastIndexOf(File.pathSeparator) != (tmpDir.length() - 1))
			tmpDir = tmpDir + File.separator;

		ms_tmpDir = new File(tmpDir);
	}
	
	public static abstract class AbstractURLOpener
	{	
		private Process m_portableFirefoxProcess = null;
		private boolean m_bOneSessionOnly = false;
		
		public synchronized boolean openURL(URL a_url)
		{
			String[] cmd;
			
			if (getBrowserCommand() == null || a_url == null)
			{
				// no path to portable browser was given; use default
				return false;
			}
				
			if(m_portableFirefoxProcess != null && m_bOneSessionOnly)
			{
				try
				{
					int ffExitValue = m_portableFirefoxProcess.exitValue();
					LogHolder.log(LogLevel.INFO, LogType.MISC,
						"previous portable firefox process exited "+
						((ffExitValue == 0) ? "normally " : "anormally ")+
						"(exit value "+ffExitValue+").");
				}
				catch(IllegalThreadStateException itse)
				{
					LogHolder.log(LogLevel.WARNING, LogType.MISC,
						"Portable Firefox process is still running!");
					return true; // do not start a second process or another browser
				}
			}
			
			cmd = new String[]{getBrowserCommand(), a_url.toString()};
			try
			{
				m_portableFirefoxProcess = Runtime.getRuntime().exec(cmd);
				return true;
			} 
			catch (SecurityException se)
			{
				LogHolder.log(LogLevel.WARNING, LogType.MISC,
						"You are not allowed to lauch portable firefox: ", se);
			}
			catch (IOException ioe3) 
			{
				LogHolder.log(LogLevel.WARNING, LogType.MISC,
						"Error occured while launching portable browser with command '" + cmd[0] + " " + 
						cmd[1] + "'",ioe3);	
			}
			
			return false;
		}
		
		public abstract String getBrowserCommand();
		
		public abstract URL getDefaultURL();
		
		public final synchronized boolean openBrowser()
		{
			boolean bReturn;
			m_bOneSessionOnly = true;
			bReturn = openURL(getDefaultURL());
			m_bOneSessionOnly = false;
			return bReturn;			
		}
	}

	public static interface IURLErrorNotifier
	{
		void checkNotify(URL a_url);
	}

	/**
	 * Instantiates an OS-specific class. If no specific class is found, the default OS
	 * (which is a dummy implementation) is instanciated.
	 * @return the instanciated operating system class
	 */
	public static final AbstractOS getInstance()
	{
		for (int i = 0; ms_operatingSystem == null && i < REGISTERED_PLATFORM_CLASSES.length; i++)
		{
			try
			{
				ms_operatingSystem =
					(AbstractOS) REGISTERED_PLATFORM_CLASSES[i].newInstance();
			}
			catch (Exception a_e)
			{
				LogHolder.log(LogLevel.DEBUG, LogType.MISC,
							  "Cannot instantiate class " + REGISTERED_PLATFORM_CLASSES[i] +
							  ". Trying to instanciate another platform class.");
			}
			if (ms_operatingSystem != null)
			{
				ms_operatingSystem.m_notifier = new IURLErrorNotifier()
				{
					public void checkNotify(URL a_url)
					{
						// do nothing
					}
				};
			}
		}

		return ms_operatingSystem;
	}

	public void init(IURLErrorNotifier a_notifier, AbstractURLOpener a_URLOpener)
	{
		if (a_notifier != null)
		{
			m_notifier = a_notifier;
		}
		if (a_URLOpener != null)
		{
			m_URLOpener = a_URLOpener;
		}
	}

	public JAPDialog.ILinkedInformation createURLLink(final URL a_url, final String a_optionalText)
	{
		return createURLLink(a_url, a_optionalText, null);
	}

	public JAPDialog.ILinkedInformation createURLLink(final URL a_url, final String a_optionalText,
		String a_helpContext)
	{
		if (a_url == null)
		{
			return null;
		}

		JAPDialog.ILinkedInformation link = new JAPDialog.LinkedHelpContext(a_helpContext)
		{
			public int getType()
			{
				return JAPDialog.ILinkedInformation.TYPE_LINK;
			}
			public void clicked(boolean a_bState)
			{
				openURL(a_url);
			}
			public String getMessage()
			{
				if (a_optionalText == null || a_optionalText.trim().length() == 0)
				{
					return a_url.toString();
				}
				else
				{
					return a_optionalText;
				}
			}
		};

		return link;
	}

	public final boolean openEMail(String a_mailto)
	{
		if (a_mailto == null)
		{
			return false;
		}
		if (!a_mailto.startsWith(URL_MAIL_TO))
		{
			return openLink(URL_MAIL_TO + a_mailto);
		}
		else
		{
			return openLink(a_mailto);
		}
	}

	public final boolean isDefaultURLAvailable()
	{
		if (m_URLOpener != null)
		{
			return (m_URLOpener.getDefaultURL() != null && m_URLOpener.getBrowserCommand() != null);
		}
		return false;
	}
	
	/**
	 * Just opens the browser with the default URL. Does not work
	 * if no default URL is available.
	 * @return if the browser could be opened with the default URL
	 */
	public final boolean openBrowser()
	{
		if (m_URLOpener != null)
		{
			return m_URLOpener.openBrowser();
		}
		return false;
	}
	
	public final boolean openURL(URL a_url)
	{
		boolean success = false;
		if (a_url == null)
		{
			return false;
		}

		String[] browser = BROWSERLIST;
		String url = getAsString(a_url);

		m_notifier.checkNotify(a_url);

		if (m_URLOpener != null)
		{
			success = m_URLOpener.openURL(a_url);
		}
		if (!success)
		{
			success = openLink(url);
		}
		if (!success)
		{
			for (int i = 0; i < browser.length; i++)
			{
				try
				{
					Runtime.getRuntime().exec(new String[]{browser[i], url});
					success = true;
					break;
				}
				catch (SecurityException a_e)
				{					
					LogHolder.log(LogLevel.ERR, LogType.MISC, a_e);
					break;
				}
				catch (Exception ex)
				{
				}
			}
		}

		if (!success)
		{
			LogHolder.log(LogLevel.ERR, LogType.MISC, "Cannot open URL in browser");
		}
		return success;
	}

	/**
	 * Implementations must return a valid path to the config file.
	 */
	public abstract String getConfigPath(String a_applicationName);

	protected abstract boolean openLink(String a_link);

	protected String getAsString(URL a_url)
	{
		if (a_url == null)
		{
			return null;
		}
		return a_url.toString();
	}

	public boolean isHelpAutoInstalled()
	{
		return false;
	}
	
	/**
	 * returns the default destination path for an external installtion of the help files. May be os specific.
	 * This method returns the path to the folder which contains the Jarfile or users working directory
	 * if JonDo is not executed. from a Jar file. 
	 * A specific OS class may override this method if the help path should be another one.
	 * @return the path to the folder which contains the Jarfile or the users working directory 
	 * 			if JonDo is not executed from a Jar file
	 */
	public String getDefaultHelpPath(String a_applicationName)
	{
		File classParentFile = ClassUtil.getClassDirectory(this.getClass());
		if(classParentFile != null) // && classParentFile.getPath().endsWith(".jar"))
		{						
			return classParentFile.getParent();
		}

		return System.getProperty("user.dir");
	}
	
	/**
	 * If available, returns the specific directory path where the application data (config files etc.)
	 * is stored by default.
	 * @return
	 */
	public String getAppdataDefaultDirectory(String a_applicationName)
	{
		return null;
	}
	
	/**
	 * Returns a vector of all running VMs. This only works on the Sun VM
	 * @return a vector of all running Virtual Machines
	 */
	public Vector getActiveVMs()
	{
		Vector r_vms = new Vector();
		int id = 0;

		if(!ms_tmpDir.isDirectory())
			return r_vms;

		// Loop through all directories that match the filter
		String[] dirs = ms_tmpDir.list(new FilenameFilter()
		{
			public boolean accept(File a_dir, String a_name)
			{
				return a_name.startsWith("hsperfdata_");
			}
		});
		if(dirs == null) return r_vms;

		for(int i = 0; i < dirs.length; i++)
		{
			File dir = new File(ms_tmpDir + File.separator + dirs[i]);
			if(!dir.isDirectory())
				continue;

			// Loop through all files in the directory. Each file represents one VM
			String[] files = dir.list();

			if(files != null)
			{
				for(int j = 0; j < files.length; j++)
				{
					File file = new File(dir + File.separator + files[j]);
					if(file.isFile() && file.canRead())
					{
						try
						{
							if((id = Integer.parseInt(file.getName())) != 0)
								r_vms.addElement(new VMPerfDataFile(id));
						}
						catch(NumberFormatException e) { continue; }
					}
				}
			}
		}

		return r_vms;
	}
	
	public String getTempPath()
	{
		String tempDir = null;
		
		try
		{
			tempDir = System.getProperty("java.io.tmpdir", null);
			if (tempDir != null && !tempDir.endsWith(File.separator))
			{
				tempDir += File.separator;
			}
		}
		catch (Throwable a_e)
		{
			LogHolder.log(LogLevel.ERR, LogType.MISC, a_e);
		}
		
		return tempDir;
	}
	
	public String getProperty(String a_systemProperty)
	{
		String property = null;
		
		if (a_systemProperty == null || a_systemProperty.trim().length() == 0)
		{
			return null;
		}
		
		try
		{
			property = System.getProperty(a_systemProperty, null);
		}
		catch (Throwable a_e)
		{
			LogHolder.log(LogLevel.ERR, LogType.MISC, "Could not get system property " + a_systemProperty);
		}
		
		return property;
	}
	
	public String getenv(String a_environmentVariable)
	{
		String env = null;
	
		if (a_environmentVariable == null || a_environmentVariable.trim().length() == 0)
		{
			return null;
		}		
		
		try
		{
			env = System.getenv(a_environmentVariable);
		}
		catch (SecurityException a_e)
		{
			LogHolder.log(LogLevel.ERR, LogType.MISC, a_e);
		}
		catch (Error a_e)
		{
			// not supported in Java versions from 1.2 to 1.4; ignore		
		}
		
		if (env == null && m_envVars != null)
		{
			env = m_envVars.getProperty(a_environmentVariable);
		}
		
		if (env == null)
		{
			try
			{
				env = System.getProperty(a_environmentVariable);
			}
			catch (Throwable a_e)
			{
				LogHolder.log(LogLevel.ERR, LogType.MISC, a_e);
			}
		}
		
		return env;
	}
	
	protected void initEnv(String a_envCommand)
	{		
		Process envProcess;

		try
		{
			envProcess = Runtime.getRuntime().exec(a_envCommand);
			m_envVars = new Properties();
			
			BufferedReader br = new BufferedReader(new InputStreamReader(envProcess.getInputStream()));
			String line;
			while ((line = br.readLine()) != null)
			{
				int idx = line.indexOf('=');
				String key = line.substring(0, idx);
				String value = line.substring(idx + 1);
				m_envVars.put(key, value);
			}
		}
		catch (IOException a_e)
		{
			LogHolder.log(LogLevel.EXCEPTION, LogType.MISC, 
					"Could not parse environment variables.", a_e);
		}	
		catch (SecurityException a_e)
		{
			LogHolder.log(LogLevel.ERR, LogType.MISC, a_e);
		}	
	}
}
