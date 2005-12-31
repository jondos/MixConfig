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
 * Dialog content panes can be implemented as a forward chained list, so that if someone clicks on
 * "OK" or "Yes", the next content pane in the list is displayed in the dialog.
 * @see gui.JAPDialog
 * @author Rolf Wendolsky
 */
public class DialogContentPane implements JAPHelpContext.IHelpContext, IDialogOptions
{
	public static final int ON_CLICK_DO_NOTHING = 0;
	public static final int ON_CLICK_HIDE_DIALOG = 1;
	public static final int ON_CLICK_DISPOSE_DIALOG = 2;
	public static final int ON_CLICK_SHOW_NEXT_CONTENT = 4;
	public static final int ON_YESOK_SHOW_NEXT_CONTENT = 8;
	public static final int ON_NO_SHOW_NEXT_CONTENT = 16;
	public static final int ON_CANCEL_SHOW_NEXT_CONTENT = 32;
	public static final int ON_YESOK_HIDE_DIALOG =64;
	public static final int ON_NO_HIDE_DIALOG =128;
	public static final int ON_CANCEL_HIDE_DIALOG = 256;
	public static final int ON_YESOK_DISPOSE_DIALOG =512;
	public static final int ON_NO_DISPOSE_DIALOG =1024;
	public static final int ON_CANCEL_DISPOSE_DIALOG = 2048;

	private static final String MSG_OK = DialogContentPane.class.getName() + "_OK";
	private static final String MSG_YES = DialogContentPane.class.getName() + "_yes";
	private static final String MSG_NO = DialogContentPane.class.getName() + "_no";
	private static final String MSG_CANCEL = DialogContentPane.class.getName() + "_cancel";

	private DialogContentPane m_nextContentPane;
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
			 a_layout.getMessageType(), a_layout.getIcon(), a_options.getHelpContext(),
			 a_options.getNextContentPane());
	}

    private void init(RootPaneContainer a_parentDialog, String a_title, int a_optionType,
					  int a_messageType, Icon a_icon, JAPHelpContext.IHelpContext a_helpContext,
					  DialogContentPane a_nextContentPane)
	{
		if (a_parentDialog == null)
		{
			throw new IllegalArgumentException("The parent dialog must not be null!");
		}

		if (a_parentDialog instanceof JDialog)
		{
			((JDialog)a_parentDialog).addWindowListener(new DialogWindowListener());
		}
		else
		{
			((JAPDialog)a_parentDialog).addWindowListener(new DialogWindowListener());
		}

		if (a_nextContentPane != null && a_nextContentPane.m_parentDialog != a_parentDialog)
		{
			throw new IllegalArgumentException("Chained content panes must refer to the same dialog!");
		}

		m_nextContentPane = a_nextContentPane;
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
	 * Defines the buttons that are available in a dialog.
	 */
	public static final class Options
	{
		private int m_optionType;
		private DialogContentPane m_nextContentPane;
		private JAPHelpContext.IHelpContext m_helpContext;

		public Options(int a_optionType)
		{
			this(a_optionType, (JAPHelpContext.IHelpContext)null, null);
		}

		public Options(String a_strHelpContext)
		{
			this(OPTION_TYPE_EMPTY, a_strHelpContext, null);
		}

		public Options(String a_strHelpContext, DialogContentPane a_nextContentPane)
		{
			this(OPTION_TYPE_EMPTY, a_strHelpContext, a_nextContentPane);
		}

		public Options(JAPHelpContext.IHelpContext a_helpContext, DialogContentPane a_nextContentPane)
		{
			this(OPTION_TYPE_EMPTY, a_helpContext, a_nextContentPane);
		}

		public Options(int a_optionType, DialogContentPane a_nextContentPane)
		{
			this(a_optionType, (JAPHelpContext.IHelpContext)null, a_nextContentPane);
		}

		public Options(int a_optionType, JAPHelpContext.IHelpContext a_helpContext)
		{
			this(a_optionType, a_helpContext, null);
		}

		public Options(int a_optionType, String a_strHelpContext)
		{
			this(a_optionType, a_strHelpContext, null);
		}


		public Options(int a_optionType, final String a_strHelpContext, DialogContentPane a_nextContentPane)
		{
			this(a_optionType,
				 new JAPHelpContext.IHelpContext(){public String getHelpContext(){return a_strHelpContext;}},
				a_nextContentPane);
		}

		public Options(int a_optionType,JAPHelpContext.IHelpContext a_helpContext,
					   DialogContentPane a_nextContentPane)
		{
			m_optionType = a_optionType;
			m_helpContext = a_helpContext;
			m_nextContentPane = a_nextContentPane;
		}

		public int getOptionType()
		{
			return m_optionType;
		}

		public JAPHelpContext.IHelpContext getHelpContext()
		{
			return m_helpContext;
		}

		public DialogContentPane getNextContentPane()
		{
			return m_nextContentPane;
		}
	}

	/**
	 * Defines the general layout of a dialog.
	 */
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

		/**
		 * Creates a new Layout for the dialog content pane.
		 * @param a_title The title of the dialog content pane. If it is empty or null, the content pane won't
		 * have a border. If it is null, the status message field will be replaced by modal dialogs.
		 * @param a_messageType The content pane's message type,
		 * e.g. MESSAGE_TYPE_PLAIN, MESSAGE_TYPE_ERROR, ...
		 * @param a_icon The icon for the content pane. If is is null, the icon will be automatically chosen
		 * depending on the message type.
		 */
		public Layout(String a_title, int a_messageType, Icon a_icon)
		{
			m_title = a_title;
			m_messageType = a_messageType;
			m_icon = a_icon;
		}

		/**
		 * Returns the title of the dialog content pane. If it is empty or null, the content pane won't have a
		 * border. If it is null, the status message field will be replaced by modal dialogs.
		 * @return the title of the dialog content pane
		 */
		public String getTitle()
		{
			return m_title;
		}

		/**
		 * The content pane's message type, e.g. MESSAGE_TYPE_PLAIN, MESSAGE_TYPE_ERROR, ...
		 * @return content pane's message type
		 */
		public int getMessageType()
		{
			return m_messageType;
		}

		/**
		 * Returns the icon for the content pane. If is is null, the icon will be automatically chosen
		 * depending on the message type.
		 * @return icon for the content pane.
		 */
		public Icon getIcon()
		{
			return m_icon;
		}
	}

	/**
	 * Returns the next content pane in the list of chained content panes.
	 * @return the next content pane in the list of chained content panes
	 */
	public final DialogContentPane getNextContentPane()
	{
		return m_nextContentPane;
	}

	/**
	 * Shows the next content pane in the dialog if it exists. Otherwise, the dialog is closed according
	 * to the default ON_CLICK operation.
	 */
	public final boolean moveToNextContentPane()
	{
		if (m_nextContentPane != null)
		{
			m_nextContentPane.updateDialog();
			return true;
		}
		else
		{
			if ((getDefaultButtonOperation() & ON_CLICK_DISPOSE_DIALOG) > 0)
			{
				closeDialog(true);
			}
			else if ((getDefaultButtonOperation() & ON_CLICK_HIDE_DIALOG) > 0)
			{
				closeDialog(false);
			}
			return false;
		}
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

	public final void printStatusMessage(String a_message)
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

	public final void printErrorStatusMessage(String a_message, int a_logType)
	{
		printErrorStatusMessage(a_message, a_logType, null);
	}

	public final void printErrorStatusMessage(String a_message, int a_logType, Throwable a_throwable)
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
		if (m_parentDialog instanceof JAPDialog)
		{
			((JAPDialog) m_parentDialog).validate();
		}
		else
		{
			((JDialog) m_parentDialog).validate();
		}
	}

	/**
	 * Returns the "Cancel" button.
	 * @return the "Cancel" button
	 */
	public final JButton getButtonCancel()
	{
		return m_btnCancel;
	}

	/**
	 * Returns the "Yes" or "OK" button.
	 * @return the "Yes" or "OK" button
	 */
	public final JButton getButtonYesOK()
	{
		return m_btnYesOK;
	}

	/**
	 * Returns the "No" button.
	 * @return the "No" button
	 */
	public final JButton getButtonNo()
	{
		return m_btnNo;
	}

	public final int getDefaultButtonOperation()
	{
		return m_defaultButtonOperation;
	}

	/**
	 * Defines what happens if one of the buttons is clicked. Several actions can be combined,
	 * for example ON_CLICK_DISPOSE_DIALOG + ON_YESOK_SHOW_NEXT_CONTENT will dispose the dialog on
	 * "Cancel" and "No" but will show the next content pane on "Yes" or "OK". The ON_CLICK operation
	 * definitions are always weaker than the button-specific operation definitions.
	 * @param a_defaultButtonOperation int
	 */
	public final void setDefaultButtonOperation(int a_defaultButtonOperation)
	{
		m_defaultButtonOperation = a_defaultButtonOperation;
	}

	/**
	 * Returns the button value the user has selected.
	 * @return the button value the user has selected
	 */
	public final int getValue()
	{
		return m_value;
	}

	public final void closeDialog(boolean a_bDispose)
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

		if (OPTION_TYPE_CANCEL_YES_NO == m_optionType || OPTION_TYPE_CANCEL_OK == m_optionType)
		{
			m_btnCancel = new JButton(JAPMessages.getString(MSG_CANCEL));
			m_btnCancel.addActionListener(listener);
			// the cancel button is always the first one if present
			m_panelOptions.add(m_btnCancel);
		}

		if (OPTION_TYPE_YES_NO == m_optionType || OPTION_TYPE_CANCEL_YES_NO == m_optionType)
		{
			m_btnYesOK = new JButton(JAPMessages.getString(MSG_YES));
			m_btnYesOK.addActionListener(listener);
			m_panelOptions.add(m_btnYesOK);
			m_btnNo = new JButton(JAPMessages.getString(MSG_NO));
			m_btnNo.addActionListener(listener);
			m_panelOptions.add(m_btnNo);
		}
		else if (OPTION_TYPE_CANCEL_OK == m_optionType || OPTION_TYPE_DEFAULT == m_optionType)
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
			boolean bActionDone = false;

			if (a_event == null || a_event.getSource() == null)
			{
				return;
			}

			if (a_event.getSource() == m_btnCancel)
			{
				m_value = RETURN_VALUE_CANCEL;
				bActionDone = doDefaultButtonOperation(ON_CANCEL_SHOW_NEXT_CONTENT,
					ON_CANCEL_HIDE_DIALOG, ON_CANCEL_DISPOSE_DIALOG);
			}
			else if (a_event.getSource() == m_btnYesOK)
			{
				if (OPTION_TYPE_YES_NO == m_optionType  || OPTION_TYPE_CANCEL_YES_NO == m_optionType)
				{
					m_value = RETURN_VALUE_YES;
				}
				else
				{
					m_value = RETURN_VALUE_OK;
				}
				bActionDone = doDefaultButtonOperation(ON_YESOK_SHOW_NEXT_CONTENT,
					ON_YESOK_HIDE_DIALOG, ON_YESOK_DISPOSE_DIALOG);
			}
			else if (a_event.getSource() == m_btnNo)
			{
				m_value = RETURN_VALUE_NO;
				bActionDone = doDefaultButtonOperation(ON_NO_SHOW_NEXT_CONTENT,
					ON_NO_HIDE_DIALOG, ON_NO_DISPOSE_DIALOG);
			}

			if (!bActionDone)
			{
				doDefaultButtonOperation(ON_CLICK_SHOW_NEXT_CONTENT, ON_CLICK_HIDE_DIALOG,
										 ON_CLICK_DISPOSE_DIALOG);
			}
		}
	}

	private boolean doDefaultButtonOperation(int a_opNext, int a_opHide, int a_opDispose)
	{
		if (m_nextContentPane != null && (getDefaultButtonOperation() & a_opNext) > 0)
		{
			m_nextContentPane.updateDialog();
			return true;
		}

		if ((getDefaultButtonOperation() & a_opDispose) > 0)
		{
			closeDialog(true);
			return true;
		}

		if ((getDefaultButtonOperation() & a_opHide) > 0)
		{
			closeDialog(false);
			return true;
		}

		return false;
	}
}
