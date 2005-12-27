/*
 Copyright (c) 2000-2005, The JAP-Team
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
import java.util.Vector;

import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;
import javax.swing.JComponent;
import javax.swing.JMenuItem;

import anon.util.ResourceLoader;
import gui.JAPHelpContext.IHelpContext;

/* classes modified from Swing Example "Metalworks" */
/** Help window for the JAP. This is a singleton meaning that there exists only one help window all the time.*/
public final class JAPHelp extends JAPDialog
{
	// images
	private static final String IMG_HOME = JAPHelp.class.getName() + "_home.gif";
	private static final String IMG_PREVIOUS = JAPHelp.class.getName() + ("_previous.gif");
	private static final String IMG_NEXT = JAPHelp.class.getName() + ("_next.gif");

	// messages
	private static final String MSG_CLOSE_BUTTON = JAPHelp.class.getName() + ("_closeButton");
	private static final String MSG_HELP_WINDOW = JAPHelp.class.getName() + ("_helpWindow");
	private static final String MSG_HELP_PATH = JAPHelp.class.getName() + ("_helpPath");
	private static final String MSG_LANGUAGE = JAPHelp.class.getName() + ("_lang");
	private static final String MSG_LANGUAGE_SHORT = JAPHelp.class.getName() + ("_langshort");
	private static final String MSG_ERROR_EXT_URL = JAPHelp.class.getName() + ("_error_ext_URL");
	public static final String MSG_HELP_BUTTON = JAPHelp.class.getName() + ("_helpButton");
	public static final String MSG_HELP_MENU_ITEM = JAPHelp.class.getName() + ("_helpMenuItem");

	private static final int MAX_HELP_LANGUAGES = 6;

	private String helpPath = " ";
	private String helpLang = " ";
	private String langShort = " ";
	private JComboBox language;
	private HtmlPane m_htmlpaneTheHelpPane;

	private JButton m_closeButton;
	private JButton m_backButton;
	private JButton m_forwardButton;
	private JButton m_homeButton;

	private boolean m_initializing;
	private JAPHelpContext m_helpContext;

	private static JAPHelp ms_theJAPHelp = null;

	private JAPHelp(Frame parent, ExternalURLCaller a_urlCaller)
	{
		super(parent, JAPMessages.getString(MSG_HELP_WINDOW), false);

		m_initializing = true;
		m_helpContext = new JAPHelpContext();
		m_htmlpaneTheHelpPane = new HtmlPane(a_urlCaller);
		m_htmlpaneTheHelpPane.addPropertyChangeListener(new HelpListener());

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

		language = new JComboBox();

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
		buttonPanel.add(language);
		buttonPanel.add(new JLabel("   "));
		buttonPanel.add(m_closeButton);

		getContentPane().add(m_htmlpaneTheHelpPane, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.NORTH);
		getRootPane().setDefaultButton(m_closeButton);
		m_closeButton.addActionListener(new HelpListener());
		m_backButton.addActionListener(new HelpListener());
		m_forwardButton.addActionListener(new HelpListener());
		m_homeButton.addActionListener(new HelpListener());
		language.addActionListener(new HelpListener());
		for (int i = 1; i < MAX_HELP_LANGUAGES; i++)
		{
			try
			{
				String path = JAPMessages.getString(MSG_HELP_PATH + String.valueOf(i));

				helpLang = JAPMessages.getString(MSG_LANGUAGE + String.valueOf(i));
				String lshort = JAPMessages.getString(MSG_LANGUAGE_SHORT + String.valueOf(i));

				// This checks if the entry exists in the properties file
				// if yes, the item will be added
				if ( (helpLang.equals(MSG_LANGUAGE + String.valueOf(i))) != true)
				{
					language.addItem(helpLang);
				}

				// Make sure to use the language with number 1 listed in the properties file
				if (helpPath.equals(" ") && langShort.equals(" "))
				{
					helpPath = path;
					langShort = lshort;
				}
			}
			catch (Exception e)
			{
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
	public static void init(Frame a_parent, ExternalURLCaller a_urlCaller)
	{
		ms_theJAPHelp = new JAPHelp(a_parent, a_urlCaller);
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
	public static interface ExternalURLCaller
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
				String currentContext = m_helpContext.getContext();
				m_htmlpaneTheHelpPane.load(helpPath + currentContext + "_" + langShort + ".html");
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
		m_htmlpaneTheHelpPane.load(helpPath + "index_" + langShort + ".html");
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
			if (e.getSource() == language && !m_initializing)
			{
				helpPath =
					JAPMessages.getString(MSG_HELP_PATH + String.valueOf(language.getSelectedIndex() + 1));
				langShort = JAPMessages.getString(MSG_LANGUAGE_SHORT +
												  String.valueOf(language.getSelectedIndex() + 1));
				m_htmlpaneTheHelpPane.load(helpPath + m_helpContext.getContext() +
										   "_" + langShort + ".html");
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
		private ExternalURLCaller m_urlCaller;
		private JEditorPane html;
		private URL url;
		private Cursor cursor;
		private Vector m_history;
		private int m_historyPosition;

		public HtmlPane(ExternalURLCaller a_urlCaller)
		{
			if (a_urlCaller == null)
			{
				a_urlCaller = new ExternalURLCaller(){public boolean openURL(URL a_url) {return false;}};
			}
			m_urlCaller = a_urlCaller;
			html = new JEditorPane("text/html", "<html><body></body></html>");
			html.setEditable(false);
			html.addHyperlinkListener(this);
			m_history = new Vector();
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
		}

		/**
		 * Goes forward in the history and loads the appropriate file
		 */
		public void goForward()
		{
			m_historyPosition++;
			this.loadURL( (URL) m_history.elementAt(m_historyPosition));
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

		public void load(String fn)
		{
			URL url = ResourceLoader.getResourceURL(fn);
			if (url != null)
			{
				linkActivated(url);
			}
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
			Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
			html.setCursor(waitCursor);
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
				else if (url.getProtocol().equals("file"))
				{
					Document doc = html.getDocument();
					try
					{
						html.setPage(url);
					}
					catch (IOException ioe)
					{
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
						JAPDialog.showInfoDialog(html.getParent(), JAPMessages.getString(MSG_ERROR_EXT_URL),
												 new ExternalLinkedInformation(url));
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
				 */
				public void openLink()
				{
				}

				/**
				 * Returns true.
				 * @return true
				 */
				public boolean isCopyAllowed()
				{
					return true;
				}

				/**
				 * Returns false, as the dialog does not need to open an other window.
				 * @return false
				 */
				public boolean isDialogSemiModal()
				{
					return false;
				}
			}
		}
	}
}


