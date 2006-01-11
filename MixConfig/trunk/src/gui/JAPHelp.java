/*
 Copyright (c) 2000-2006, The JAP-Team
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
package gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.Vector;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;

import anon.util.ResourceLoader;
import gui.JAPHelpContext.IHelpContext;
import gui.dialog.JAPDialog;
import logging.LogHolder;
import logging.LogLevel;
import logging.LogType;

/**
 * Help window for the JAP. This is a singleton meaning that there exists only one help window all the time.
 * Each supported language is specified by the property languageCode[i], where [i] is the
 * language index starting with '1'. To fully support a language, at least the file index_xx.html is needed,
 * where xx is the two-letter language code given in the property. The number of supported languages is
 * unlimited, as long as the indices are incremented by '1'.
 * The help files are stored in one or more directories, specified by the message properties 'helpPath' and
 * helpPath[i]. It is enough to specify the property 'helpPath', specific directories for each language
 * are optional.
 * (classes modified from Swing Example "Metalworks")
 * @see gui.LanguageMapper
 * @see gui.JAPMessages
 */
public final class JAPHelp extends JAPDialog
{
	public static final String INDEX_CONTEXT = "index";

	// messages
	public static final String MSG_HELP_BUTTON = JAPHelp.class.getName() + ("_helpButton");
	public static final String MSG_HELP_MENU_ITEM = JAPHelp.class.getName() + ("_helpMenuItem");
	private static final String MSG_CLOSE_BUTTON = JAPHelp.class.getName() + ("_closeButton");
	private static final String MSG_HELP_WINDOW = JAPHelp.class.getName() + ("_helpWindow");
	private static final String MSG_HELP_PATH = JAPHelp.class.getName() + ("_helpPath");
	private static final String MSG_LANGUAGE_CODE = JAPHelp.class.getName() + ("_languageCode");
	private static final String MSG_ERROR_EXT_URL = JAPHelp.class.getName() + ("_errorExtURL");

	// images
	private static final String IMG_HOME = JAPHelp.class.getName() + "_home.gif";
	private static final String IMG_PREVIOUS = JAPHelp.class.getName() + ("_previous.gif");
	private static final String IMG_NEXT = JAPHelp.class.getName() + ("_next.gif");

	private String m_helpPath = " ";
	private LanguageMapper m_language = new LanguageMapper();
	private JComboBox m_comBoxLanguage;
	private HtmlPane m_htmlpaneTheHelpPane;

	private JButton m_closeButton;
	private JButton m_backButton;
	private JButton m_forwardButton;
	private JButton m_homeButton;

	private boolean m_initializing;
	private JAPHelpContext m_helpContext;

	private static JAPHelp ms_theJAPHelp = null;

	private JAPHelp(Frame parent, IExternalURLCaller a_urlCaller)
	{
		super(parent, JAPMessages.getString(MSG_HELP_WINDOW), false);

		m_initializing = true;
		m_helpContext = new JAPHelpContext();
		m_htmlpaneTheHelpPane = new HtmlPane(a_urlCaller);
		m_htmlpaneTheHelpPane.addPropertyChangeListener(new HelpListener());

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

		m_comBoxLanguage = new JComboBox();

		m_backButton = new JButton(GUIUtils.loadImageIcon(IMG_PREVIOUS, true));
		m_backButton.setBackground(Color.gray); //this together with the next lines sems to be
		m_backButton.setOpaque(false); //stupid but is necessary for JDK 1.5 on Windows XP (and maybe others)
		m_backButton.setBorder(new EmptyBorder(0, 0, 0, 0));
		m_backButton.setFocusPainted(false);

		m_forwardButton = new JButton(GUIUtils.loadImageIcon(IMG_NEXT, true));
		m_forwardButton.setBackground(Color.gray); //this together with the next lines sems to be
		m_forwardButton.setOpaque(false); //stupid but is necessary for JDK 1.5 on Windows XP (and maybe others)
		m_forwardButton.setBorder(new EmptyBorder(0, 0, 0, 0));
		m_forwardButton.setFocusPainted(false);

		m_homeButton = new JButton(GUIUtils.loadImageIcon(IMG_HOME, true));
		m_homeButton.setBackground(Color.gray); //this together with the next lines sems to be
		m_homeButton.setOpaque(false); //stupid but is necessary for JDK 1.5 on Windows XP (and maybe others)
		m_homeButton.setBorder(new EmptyBorder(0, 0, 0, 0));
		m_homeButton.setFocusPainted(false);

		m_closeButton = new JButton(JAPMessages.getString(MSG_CLOSE_BUTTON));
		m_forwardButton.setEnabled(false);
		m_backButton.setEnabled(false);

		buttonPanel.add(m_homeButton);
		buttonPanel.add(m_backButton);
		buttonPanel.add(m_forwardButton);
		buttonPanel.add(new JLabel("   "));
		buttonPanel.add(m_comBoxLanguage);
		buttonPanel.add(new JLabel("   "));
		buttonPanel.add(m_closeButton);

		getContentPane().add(m_htmlpaneTheHelpPane, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.NORTH);
		getRootPane().setDefaultButton(m_closeButton);
		m_closeButton.addActionListener(new HelpListener());
		m_backButton.addActionListener(new HelpListener());
		m_forwardButton.addActionListener(new HelpListener());
		m_homeButton.addActionListener(new HelpListener());
		m_comBoxLanguage.addActionListener(new HelpListener());
		for (int i = 1; true; i++)
		{
			try
			{
				String langCode = JAPMessages.getString(MSG_LANGUAGE_CODE + String.valueOf(i));
				LanguageMapper lang = new LanguageMapper(langCode, new Locale(langCode, ""));
				m_comBoxLanguage.addItem(lang);

				if ((m_helpPath.equals(" ") && m_language.getISOCode().length() == 0) ||
					lang.getISOCode().equals(JAPMessages.getLocale().getLanguage()))
				{
					m_helpPath = getHelpPath(i);
					m_language = lang;
					m_comBoxLanguage.setSelectedIndex(i - 1);
				}
			}
			catch (Exception e)
			{
				break;
			}
		}

		// set window size
		( (JComponent) getContentPane()).setPreferredSize(new Dimension(
			Math.min(Toolkit.getDefaultToolkit().getScreenSize().width - 50, 600),
			Math.min(Toolkit.getDefaultToolkit().getScreenSize().height - 80, 350)));
		pack();
		m_initializing = false;
	}

	/**
	 * Creates and initialises a new global help object with the given frame as parent frame.
	 * @param a_parent the parent frame of the help object
	 * @param a_urlCaller the caller that is used to open external URLs (may be null)
	 */
	public static void init(Frame a_parent, IExternalURLCaller a_urlCaller)
	{
		if (ms_theJAPHelp == null)
		{
			ms_theJAPHelp = new JAPHelp(a_parent, a_urlCaller);
		}
	}

	/**
	 * Returns the current help instance.
	 * @return the current help instance
	 */
	public static JAPHelp getInstance()
	{
		return ms_theJAPHelp;
	}

	/**
	 * An instance of this interface is needed to open external URLs.
	 */
	public static interface IExternalURLCaller
	{
		/**
		 * Returns if the caller was able to open the URL in the browser
		 * @param a_url a URL
		 * @return if the caller was able to open the URL in the browser
		 */
		boolean openURL(URL a_url);
	}

	/**
	 * Creates a button that opens the help window with the given context.
	 * @param a_helpContext a help context
	 * @return a button that opens the help window with the given context
	 */
	public static JButton createHelpButton(IHelpContext a_helpContext)
	{
		JButton helpButton = new JButton(JAPMessages.getString(MSG_HELP_BUTTON));
		helpButton.addActionListener(new HelpContextActionListener(a_helpContext));
		return helpButton;
	}

	/**
	 * Creates a menu item that opens the help window with the given context.
	 * @param a_helpContext a help context
	 * @return a menu item that opens the help window with the given context
	 */
	public static JMenuItem createHelpMenuItem(IHelpContext a_helpContext)
	{
		JMenuItem helpButton = new JMenuItem(JAPMessages.getString(MSG_HELP_MENU_ITEM));
		helpButton.addActionListener(new HelpContextActionListener(a_helpContext));
		return helpButton;
	}

	public void loadCurrentContext()
	{
		setVisible(true);
	}

	public void setVisible(boolean a_bVisible)
	{
		if (a_bVisible)
		{
			try
			{
				m_htmlpaneTheHelpPane.loadContext(m_helpPath, m_helpContext.getContext(), m_language);
				if (!isVisible())
				{
					super.setVisible(true);
				}
			}
			catch (Exception e)
			{
			}
		}
		else
		{
			super.setVisible(false);
		}
	}

	/**
	 * Returns the context object
	 * @return JAPHelpContext
	 */
	public JAPHelpContext getContextObj()
	{
		return m_helpContext;
	}

	private static final class HelpContextActionListener implements ActionListener
	{
		private IHelpContext m_helpContext;

		public HelpContextActionListener(IHelpContext a_helpContext)
		{
			m_helpContext = a_helpContext;
		}

		public void actionPerformed(ActionEvent a_event)
		{
			getInstance().getContextObj().setContext(m_helpContext);
			getInstance().loadCurrentContext();
			getInstance().toFront();
			getInstance().requestFocus();
		}
	}

	private void homePressed()
	{
		m_htmlpaneTheHelpPane.loadContext(m_helpPath, INDEX_CONTEXT, m_language);
	}

	private void closePressed()
	{
		setVisible(false);
	}

	private void backPressed()
	{
		m_htmlpaneTheHelpPane.goBack();
		checkNavigationButtons();
	}

	private void forwardPressed()
	{
		m_htmlpaneTheHelpPane.goForward();
		checkNavigationButtons();
	}

	/**
	 * Checks whether to enable or disable the forward and back buttons
	 */
	private void checkNavigationButtons()
	{
		if (m_htmlpaneTheHelpPane.backAllowed())
		{
			m_backButton.setEnabled(true);
		}
		else
		{
			m_backButton.setEnabled(false);
		}

		if (m_htmlpaneTheHelpPane.forwardAllowed())
		{
			m_forwardButton.setEnabled(true);
		}
		else
		{
			m_forwardButton.setEnabled(false);
		}
	}

	private class HelpListener implements ActionListener, PropertyChangeListener
	{
		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() == m_comBoxLanguage && !m_initializing)
			{
				m_helpPath = getHelpPath(m_comBoxLanguage.getSelectedIndex() + 1);
				m_language = new LanguageMapper(JAPMessages.getString(MSG_LANGUAGE_CODE +
					String.valueOf(m_comBoxLanguage.getSelectedIndex() + 1)));
				m_htmlpaneTheHelpPane.loadContext(m_helpPath, m_helpContext.getContext(), m_language);
			}
			else if (e.getSource() == m_closeButton)
			{
				closePressed();
			}
			else if (e.getSource() == m_backButton)
			{
				backPressed();
			}
			else if (e.getSource() == m_forwardButton)
			{
				forwardPressed();
			}
			else if (e.getSource() == m_homeButton)
			{
				homePressed();
			}
		}

		/**
		 * Listens to events fired by the HtmlPane in order to update the history buttons
		 * @param a_e PropertyChangeEvent
		 */
		public void propertyChange(PropertyChangeEvent a_e)
		{
			if (a_e.getSource() == m_htmlpaneTheHelpPane)
			{
				checkNavigationButtons();
			}
		}
	}

	private final class HtmlPane extends JScrollPane implements HyperlinkListener
	{
		private IExternalURLCaller m_urlCaller;
		private JEditorPane html;
		private URL url;
		private Cursor cursor;
		private Vector m_history;
		private Vector m_historyViewports;
		private int m_historyPosition;

		public HtmlPane(IExternalURLCaller a_urlCaller)
		{
			if (a_urlCaller == null)
			{
				a_urlCaller = new IExternalURLCaller(){public boolean openURL(URL a_url) {return false;}};
			}
			m_urlCaller = a_urlCaller;
			html = new JEditorPane("text/html", "<html><body></body></html>");
			html.setEditable(false);
			html.addHyperlinkListener(this);
			m_history = new Vector();
			m_historyViewports = new Vector();
			m_historyPosition = -1;

			getViewport().add(html);
			cursor = html.getCursor(); // ??? (hf)
		}

		public JEditorPane getPane()
		{
			return html;
		}

		/**
		 * Goes back in the history and loads the appropriate file
		 */
		public void goBack()
		{
			m_historyPosition--;
			this.loadURL( (URL) m_history.elementAt(m_historyPosition));
			//getViewport().setViewPosition((Point)m_historyViewports.elementAt(m_historyPosition));

		}

		/**
		 * Goes forward in the history and loads the appropriate file
		 */
		public void goForward()
		{
			m_historyPosition++;
			this.loadURL( (URL) m_history.elementAt(m_historyPosition));
			//getViewport().setViewPosition((Point)m_historyViewports.elementAt(m_historyPosition));

		}

		/**
		 * Adds the given URL to the browser history
		 * @param a_url URL
		 */
		private void addToHistory(URL a_url)
		{
			if (m_historyPosition == -1 ||
				!a_url.getFile().equalsIgnoreCase( ( (URL) m_history.elementAt(m_historyPosition)).getFile()))
			{
				m_history.insertElementAt(a_url, ++m_historyPosition);
			}
		}

		public boolean loadContext(String a_strHelpPath, String a_strContext, LanguageMapper a_language)
		{
			URL url = ResourceLoader.getResourceURL(
				 a_strHelpPath + a_strContext + "_" + a_language.getISOCode() + ".html");
			boolean bLoaded = false;

			if (url != null)
			{
				linkActivated(url);
				bLoaded = true;
			}
			else
			{
				LogHolder.log(LogLevel.WARNING, LogType.GUI,
							  "Could not load help context '" + a_strContext +
							  "_" + a_language.getISOCode() + "'");

				if(a_strContext != null)
				{
					if (!a_strContext.equals(INDEX_CONTEXT))
					{
						// try to load the index page
						bLoaded = loadContext(a_strHelpPath, INDEX_CONTEXT, a_language);
					}
					else if (a_language.equals(new LanguageMapper("EN")))
					{
						LogHolder.log(LogLevel.ERR, LogType.GUI,
									  "No index help file for language '" + a_language.getISOCode() +
									  "', help files seem to be corrupt!");
						return true;
					}

					if (!bLoaded)
					{
						// the help files for this language seem to be corrupted; switch to english
						LanguageMapper english = new LanguageMapper("EN");
						int i;
						for (i = 0; i < m_comBoxLanguage.getItemCount(); i++)
						{
							if (((LanguageMapper)m_comBoxLanguage.getItemAt(i)).equals(english) &&
								m_comBoxLanguage.getSelectedIndex() != i)
							{
								m_comBoxLanguage.setSelectedIndex(i);
								new HelpListener().actionPerformed(new ActionEvent(m_comBoxLanguage,0,""));
								break;
							}
						}

						LogHolder.log(LogLevel.WARNING, LogType.GUI,
									  "No index help file for language '" + a_language.getISOCode() +
									  "', switching to '" + english.getISOCode() + "'");

						bLoaded = true;
					}
				}
			}
			return bLoaded;
		}

		public void hyperlinkUpdate(HyperlinkEvent e)
		{
			if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
			{
				linkActivated(e.getURL());
			}
			else if (e.getEventType() == HyperlinkEvent.EventType.ENTERED)
			{
				html.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}
			else if (e.getEventType() == HyperlinkEvent.EventType.EXITED)
			{
				html.setCursor(cursor);
			}
		}

		private void linkActivated(URL u)
		{
			html.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			SwingUtilities.invokeLater(new PageLoader(u));
			//Update history
			this.addToHistory(u);
			this.cleanForwardHistory();
			//Make sure the window updates its history buttons
			this.firePropertyChange("CheckButtons", false, true);
		}

		/**
		 * Removes all entries from the forward history
		 */
		private void cleanForwardHistory()
		{
			for (int i = m_history.size() - 1; i > m_historyPosition; i--)
			{
				m_history.removeElementAt(i);
			}
		}

		/**
		 * Returns true if there are entries in the back history
		 * @return boolean
		 */
		public boolean backAllowed()
		{
			if (m_historyPosition <= 0)
			{
				return false;
			}
			else
			{
				return true;
			}
		}

		/**
		 * Returns true if there are entries in the forward history
		 * @return boolean
		 */
		public boolean forwardAllowed()
		{
			if (m_history.size() - 1 > m_historyPosition)
			{
				return true;
			}
			else
			{
				return false;
			}
		}

		/**
		 * Loads URL without adding it to the history
		 * @param a_url URL
		 */
		private void loadURL(URL a_url)
		{
			html.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			SwingUtilities.invokeLater(new PageLoader(a_url));
		}

		private final class PageLoader implements Runnable
		{
			PageLoader(URL u)
			{
				url = u;
			}

			public void run()
			{
				if (url == null)
				{
					// restore the original cursor
					html.setCursor(cursor);
					// PENDING(prinz) remove this hack when
					// automatic validation is activated.
					html.getParent().repaint();
				}
				else if (
					url.getProtocol().startsWith(ResourceLoader.SYSTEM_RESOURCE_TYPE_FILE)  ||
					url.getProtocol().startsWith(ResourceLoader.SYSTEM_RESOURCE_TYPE_ZIP) ||
					url.getProtocol().startsWith(ResourceLoader.SYSTEM_RESOURCE_TYPE_JAR) ||
					url.getProtocol().startsWith(ResourceLoader.SYSTEM_RESOURCE_TYPE_GENERIC))
				{
					Document doc = html.getDocument();
					try
					{
						html.setPage(url);
					}
					catch (IOException ioe)
					{
						/** @todo if this is a context page, try to load the english version */
						html.setDocument(doc);
						getToolkit().beep();
					}
					finally
					{
						// schedule the cursor to revert after
						// the paint has happended.
						url = null;
						SwingUtilities.invokeLater(this);
					}
				}
				else
				{
					if (!m_urlCaller.openURL(url))
					{
						html.setCursor(cursor);
						JAPDialog.showMessageDialog(html.getParent(), JAPMessages.getString(MSG_ERROR_EXT_URL),
												 new ExternalLinkedInformation(url));
					}
					if (m_historyPosition > 0)
					{
						m_historyPosition--;
						m_history.removeElementAt(m_history.size() - 1);
					}
				}
			}

			/**
			 * Needed to copy an external URL that could not be opened to the clip board.
			 */
			private class ExternalLinkedInformation implements JAPDialog.ILinkedInformation
			{
				private URL m_url;

				public ExternalLinkedInformation(URL a_url)
				{
					m_url = a_url;
				}

				/**
				 * Returns the URL that could not be opened in the help window.
				 * @return the URL
				 */
				public String getMessage()
				{
					return m_url.toString();
				}

				/**
				 * No action is performed on clicking the link.
				 * @param a_bState is ignored
				 */
				public void clicked(boolean a_bState)
				{
				}

				/**
				 * Returns TYPE_SELECTABLE_LINK.
				 * @return TYPE_SELECTABLE_LINK
				 */
				public int getType()
				{
					return TYPE_SELECTABLE_LINK;
				}

				/**
				 * Returns true, as the dialog does not need to open an other window.
				 * @return true
				 */
				public boolean isApplicationModalityForced()
				{
					return true;
				}
			}
		}
	}

	private static String getHelpPath(int a_languageIndex)
	{
		String strMessage = MSG_HELP_PATH + String.valueOf(a_languageIndex);
		String strHelpPath = JAPMessages.getString(strMessage);

		if (strHelpPath.equals(strMessage) || strHelpPath.trim().length() == 0)
		{
			return JAPMessages.getString(MSG_HELP_PATH);
		}

		return strHelpPath;
	}
}


