/*
 Copyright (c) 2000 - 2005, The JAP-Team
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

import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.RootPaneContainer;
import javax.swing.border.TitledBorder;

import logging.LogHolder;
import logging.LogLevel;

/**
 * This is a replacement for a dialog content pane. It defines an icon, buttons, a status bar for
 * info and error messages and a content pane where own components may be placed. The content pane
 * of the parent dialog is automatically replaced with this one by calling the method
 * <CODE>updateDialog()</CODE>. Sometimes it is needed to call pack() afterwards.
 * @see gui.JAPDialog
 * @author Rolf Wendolsky
 */
public final class DialogContentPane implements JAPHelpContext.IHelpContext, IDialogOptions
{
	public static final int ON_CLICK_DO_NOTHING = 0;
	public static final int ON_CLICK_HIDE_DIALOG = 1;
	public static final int ON_CLICK_DISPOSE_DIALOG = 2;

	private static final String MSG_OK = DialogContentPane.class.getName() + "_OK";
	private static final String MSG_YES = DialogContentPane.class.getName() + "_yes";
	private static final String MSG_NO = DialogContentPane.class.getName() + "_no";
	private static final String MSG_CANCEL = DialogContentPane.class.getName() + "_cancel";

	private RootPaneContainer m_parentDialog;
	private JComponent m_contentPane;
	private JPanel m_titlePane;
	private JPanel m_rootPane;
	private JLabel m_lblMessage;
	private JPanel m_panelOptions;
	private int m_messageType;
	private int m_optionType;
	private JAPHelpContext.IHelpContext m_helpContext;
	private JButton m_btnYesOK;
	private JButton m_btnNo;
	private JButton m_btnCancel;
	private int m_defaultButtonOperation;
	private int m_value;
	private Icon m_icon;


	public static final class Options
	{
		private int m_optionType;
		private JAPHelpContext.IHelpContext m_helpContext;

		public Options(int a_optionType)
		{
			this(a_optionType, (JAPHelpContext.IHelpContext)null);
		}

		public Options(String a_strHelpContext)
		{
			this(OPTION_TYPE_EMPTY, a_strHelpContext);
		}

		public Options(int a_optionType, final String a_strHelpContext)
		{
			this(a_optionType,
				 new JAPHelpContext.IHelpContext(){public String getHelpContext(){return a_strHelpContext;}});
		}

		public Options(int a_optionType,JAPHelpContext.IHelpContext a_helpContext)
		{
			m_optionType = a_optionType;
			m_helpContext = a_helpContext;
		}

		public int getOptionType()
		{
			return m_optionType;
		}

		public JAPHelpContext.IHelpContext getHelpContext()
		{
			return m_helpContext;
		}
	}

	public static final class Layout
	{
		private String m_title;
		private int m_messageType;
		private Icon m_icon;

		public Layout(int a_messageType)
		{
			this("", a_messageType, null);
		}

		public Layout(String a_title)
		{
			this(a_title, MESSAGE_TYPE_PLAIN, null);
		}

		public Layout(Icon a_icon)
		{
			this("", MESSAGE_TYPE_PLAIN, a_icon);
		}

		public Layout(int a_messageType, Icon a_icon)
		{
			this("", a_messageType, a_icon);
		}

		public Layout(String a_title, int a_messageType)
		{
			this(a_title, a_messageType, null);
		}

		public Layout(String a_title, Icon a_icon)
		{
			this(a_title, MESSAGE_TYPE_PLAIN, a_icon);
		}

		public Layout(String a_title, int a_messageType, Icon a_icon)
		{
			m_title = a_title;
			m_messageType = a_messageType;
			m_icon = a_icon;
		}

		public String getTitle()
		{
			return m_title;
		}

		public int getMessageType()
		{
			return m_messageType;
		}

		public Icon getIcon()
		{
			return m_icon;
		}
	}

	public DialogContentPane(JDialog a_parentDialog)
	{
		this((RootPaneContainer)a_parentDialog, new Layout(""), null);
	}

	public DialogContentPane(JAPDialog a_parentDialog)
	{
		this((RootPaneContainer)a_parentDialog, new Layout(""), null);
	}

	public DialogContentPane(JDialog a_parentDialog, Layout a_layout)
	{
		this((RootPaneContainer)a_parentDialog, a_layout, null);
	}

	public DialogContentPane(JAPDialog a_parentDialog, Layout a_layout)
	{
		this((RootPaneContainer)a_parentDialog, a_layout, null);
	}

	public DialogContentPane(JDialog a_parentDialog, Options a_options)
	{
		this((RootPaneContainer)a_parentDialog, new Layout(""), a_options);
	}

	public DialogContentPane(JAPDialog a_parentDialog, Options a_options)
	{
		this((RootPaneContainer)a_parentDialog, new Layout(""), a_options);
	}

	public DialogContentPane(JDialog a_parentDialog, Layout a_layout, Options a_options)
	{
		this((RootPaneContainer)a_parentDialog, a_layout, a_options);
	}

	public DialogContentPane(JAPDialog a_parentDialog, Layout a_layout, Options a_options)
	{
		this((RootPaneContainer)a_parentDialog, a_layout, a_options);
	}

	private DialogContentPane(RootPaneContainer a_parentDialog, Layout a_layout, Options a_options)
	{
		if (a_layout == null)
		{
			a_layout = new Layout((String)null);
		}
		if (a_options == null)
		{
			a_options = new Options((String)null);
		}

		init(a_parentDialog, a_layout.getTitle(), a_options.getOptionType(),
			 a_layout.getMessageType(), a_layout.getIcon(), a_options.getHelpContext());
	}

    private void init(RootPaneContainer a_parentDialog, String a_title, int a_optionType,
					  int a_messageType, Icon a_icon, JAPHelpContext.IHelpContext a_helpContext)
	{
		if (a_parentDialog instanceof JDialog)
		{
			((JDialog)a_parentDialog).addWindowListener(new DialogWindowListener());
		}
		else
		{
			((JAPDialog)a_parentDialog).addWindowListener(new DialogWindowListener());
		}

		m_parentDialog = a_parentDialog;
		m_messageType = a_messageType;
		m_optionType = a_optionType;
		m_icon = a_icon;
		m_helpContext = a_helpContext;
		m_rootPane = new JPanel(new BorderLayout());
		m_titlePane = new JPanel();
		m_rootPane.add(m_titlePane, BorderLayout.CENTER);
		m_contentPane = new JPanel();
		m_titlePane.add(m_contentPane);

		if (a_title != null)
		{
			if (a_title.trim().length() > 0)
			{
				m_titlePane.setBorder(new TitledBorder(a_title));
			}
			m_lblMessage = new JLabel();
			clearStatusMessage();
			m_rootPane.add(m_lblMessage, BorderLayout.SOUTH);
		}
		setDefaultButtonOperation(ON_CLICK_DO_NOTHING);
		m_value = RETURN_VALUE_UNINITIALIZED;
		createOptions();
	}

	/**
	 * Returns the content pane where elements may be placed freely.
	 * @return the content pane
	 */
	public final JComponent getContentPane()
	{
		return m_contentPane;
	}

	public final void setContentPane(JComponent a_contentPane)
	{
		m_rootPane.remove(m_contentPane);
		m_rootPane.add(a_contentPane, BorderLayout.CENTER);
		m_contentPane = a_contentPane;
	}

	public final String getHelpContext()
	{
		if (m_helpContext == null)
		{
			return null;
		}

		return m_helpContext.getHelpContext();
	}

	/**
	 * Resets the text in the status message line to an empty String.
	 */
	public final void clearStatusMessage()
	{
		if (m_lblMessage != null)
		{
			m_lblMessage.setText("T");
			m_lblMessage.setPreferredSize(m_lblMessage.getPreferredSize());
			m_lblMessage.setText("");
		}
	}

	public void printStatusMessage(String a_message)
	{
		if (m_lblMessage != null)
		{
			m_lblMessage.setForeground(Color.black);
			m_lblMessage.setText(a_message);
			m_lblMessage.revalidate();
		}
		else
		{
			JAPDialog.showMessageDialog(getContentPane(), a_message);
		}
	}

	public void printErrorStatusMessage(String a_message, int a_logType)
	{
		printErrorStatusMessage(a_message, a_logType, null);
	}

	public void printErrorStatusMessage(String a_message, int a_logType, Throwable a_throwable)
	{
		if (m_lblMessage != null)
		{
			a_message = JAPDialog.retrieveErrorMessage(a_throwable, a_message);
			LogHolder.log(LogLevel.ERR, a_logType, a_message, true);
			if (a_throwable != null)
			{
				// the exception is only shown in debug mode
				LogHolder.log(LogLevel.DEBUG, a_logType, a_throwable);
			}
			m_lblMessage.setForeground(Color.red);
			m_lblMessage.setText(a_message);
			m_lblMessage.revalidate();
		}
		else
		{
			JAPDialog.showErrorDialog(getContentPane(), a_message, a_logType, a_throwable);
		}
	}

	/**
	 * Replaces the content pane of the parent dialog with the content defined in this object.
	 */
	public final void updateDialog()
	{
		Object[] options = new Object[1];
		options[0] = m_panelOptions;
		JOptionPane pane = new JOptionPane(m_rootPane, m_messageType, 0, m_icon, options );
		m_parentDialog.setContentPane(pane.createDialog(null, "").getContentPane());
	}

	/**
	 * Returns the "Cancel" button.
	 * @return the "Cancel" button
	 */
	public JButton getButtonCancel()
	{
		return m_btnCancel;
	}

	/**
	 * Returns the "Yes" or "OK" button.
	 * @return the "Yes" or "OK" button
	 */
	public JButton getButtonYesOK()
	{
		return m_btnYesOK;
	}

	/**
	 * Returns the "No" button.
	 * @return the "No" button
	 */
	public JButton getButtonNo()
	{
		return m_btnNo;
	}

	public final int getDefaultButtonOperation()
	{
		return m_defaultButtonOperation;
	}

	public final void setDefaultButtonOperation(int a_defaultButtonOperation)
	{
		m_defaultButtonOperation = a_defaultButtonOperation;
	}

	/**
	 * Returns the button value the user has selected.
	 * @return the button value the user has selected
	 */
	public int getValue()
	{
		return m_value;
	}

	public void closeDialog(boolean a_bDispose)
	{
		if (a_bDispose)
		{
			if (m_parentDialog instanceof JDialog)
			{
				((JDialog)m_parentDialog).dispose();
			}
			else
			{
				((JAPDialog)m_parentDialog).dispose();
			}
		}
		else
		{
			if (m_parentDialog instanceof JDialog)
			{
				((JDialog)m_parentDialog).setVisible(false);
			}
			else
			{
				((JAPDialog)m_parentDialog).setVisible(false);
			}
		}
	}

	private void createOptions()
	{
		ButtonListener listener = new ButtonListener();

		m_panelOptions = new JPanel();

		if (OPTION_TYPE_YES_NO_CANCEL == m_optionType || OPTION_TYPE_OK_CANCEL == m_optionType)
		{
			m_btnCancel = new JButton(JAPMessages.getString(MSG_CANCEL));
			m_btnCancel.addActionListener(listener);
			// the cancel button is always the first one if present
			m_panelOptions.add(m_btnCancel);
		}

		if (OPTION_TYPE_YES_NO == m_optionType || OPTION_TYPE_YES_NO_CANCEL == m_optionType)
		{
			m_btnYesOK = new JButton(JAPMessages.getString(MSG_YES));
			m_btnYesOK.addActionListener(listener);
			m_panelOptions.add(m_btnYesOK);
			m_btnNo = new JButton(JAPMessages.getString(MSG_NO));
			m_btnNo.addActionListener(listener);
			m_panelOptions.add(m_btnNo);
		}
		else if (OPTION_TYPE_OK_CANCEL == m_optionType || OPTION_TYPE_DEFAULT == m_optionType)
		{
			m_btnYesOK = new JButton(JAPMessages.getString(MSG_OK));
			m_btnYesOK.addActionListener(listener);
			m_panelOptions.add(m_btnYesOK);
		}

		if (getHelpContext() != null)
		{
			m_panelOptions.add(JAPHelp.createHelpButton(this));
		}
	}

	private class DialogWindowListener extends WindowAdapter
	{
		public void windowClosed(WindowEvent a_event)
		{
			if (m_value == RETURN_VALUE_UNINITIALIZED)
			{
				m_value = RETURN_VALUE_CLOSED;
			}
		}
	}

	private class ButtonListener implements ActionListener
	{
		public void actionPerformed(ActionEvent a_event)
		{
			if (m_btnCancel != null && a_event.getSource() == m_btnCancel)
			{
				m_value = RETURN_VALUE_CANCEL;
			}
			else if (a_event.getSource() == m_btnYesOK)
			{
				if (OPTION_TYPE_YES_NO == m_optionType  || OPTION_TYPE_YES_NO_CANCEL == m_optionType)
				{
					m_value = RETURN_VALUE_YES;
				}
				else
				{
					m_value = RETURN_VALUE_OK;
				}
			}
			else if (m_btnNo != null && a_event.getSource() == m_btnNo)
			{
				m_value = RETURN_VALUE_NO;
			}

			if (getDefaultButtonOperation() == ON_CLICK_DISPOSE_DIALOG)
			{
				closeDialog(true);
			}
			else if (getDefaultButtonOperation() == ON_CLICK_HIDE_DIALOG)
			{
				closeDialog(false);
			}
		}
	}
}
