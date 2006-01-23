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
package gui.dialog;

import java.util.Vector;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Window;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Insets;
import java.awt.event.WindowListener;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.RootPaneContainer;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import gui.JAPHelp;
import gui.JAPHelpContext;
import gui.JAPHtmlMultiLineLabel;
import gui.JAPMessages;
import logging.LogHolder;
import logging.LogLevel;
import logging.LogType;

/**
 * This is a replacement for a dialog content pane. It defines an icon, buttons, a status bar for
 * information and error messages, an optional titled border around the content and a content pane
 * where own components can be placed. The content pane of the parent dialog is automatically replaced
 * with this one by calling the method <CODE>updateDialog()</CODE>. If the size of the dialog has not been
 * defined before, you will need to call pack() afterwards.
 * <P>Dialog content panes can be implemented as a  chained list, so that if someone clicks on a button,
 * the next or previous content pane in the list is displayed in the dialog. Use setDefaultButtonOperation()
 * and the ON_... events to activate this behaviour. Of course, the foward and back operations can be done
 * explicitly and without those events, too.</P>
 * If you have a chained list, you can display it as a Wizard, too. Every content pane in the list must
 * implement the interface DialogContentPane.IWizardSuitable and each content pane is recommended to
 * support BUTTON_OPERATION_WIZARD. Their "YES/OK" and "NO" buttons will automatically
 * be transformed into "Next" and "Previous", and all buttons are shown (Cancel, Previous, Next). If a class
 * wants to keep its own buttons as defined by the option type but act in a wizard, it has to implement
 * IWizardSuitableNoWizardButtons. This will prevent that is gets the wizard layout.
 *
 * @see gui.dialog.JAPDialog
 * @see javax.swing.JDialog
 * @see gui.dialog.DialogContentPane.Layout
 * @see gui.dialog.DialogContentPane.Options
 * @see gui.dialog.DialogContentPane.CheckError
 * @see gui.dialog.DialogContentPane.IWizardSuitable
 * @see gui.dialog.DialogContentPane.IWizardSuitableNoWizardButtons
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
	public static final int ON_CLICK_SHOW_PREVIOUS_CONTENT = 64;
	public static final int ON_YESOK_SHOW_PREVIOUS_CONTENT = 128;
	public static final int ON_NO_SHOW_PREVIOUS_CONTENT = 256;
	public static final int ON_CANCEL_SHOW_PREVIOUS_CONTENT = 512;
	public static final int ON_YESOK_HIDE_DIALOG = 1024;
	public static final int ON_NO_HIDE_DIALOG = 2048;
	public static final int ON_CANCEL_HIDE_DIALOG = 4096;
	public static final int ON_YESOK_DISPOSE_DIALOG = 8192;
	public static final int ON_NO_DISPOSE_DIALOG = 16384;
	public static final int ON_CANCEL_DISPOSE_DIALOG = 32768;

	private static int MIN_TEXT_LENGTH = 25;

	/**
	 * Is equal to ON_NO_SHOW_PREVIOUS_CONTENT | ON_YESOK_SHOW_NEXT_CONTENT | ON_CLICK_DISPOSE_DIALOG
	 * as the typical wizard behaviour . If the default button operation does not contain this behaviour,
	 * at least the class itself should implement an equal behaviour, or this content pane should not be
	 * displayed as a wizard (not implement IWizardSuitable).
	 */
	public static final int BUTTON_OPERATION_WIZARD =
		ON_NO_SHOW_PREVIOUS_CONTENT | ON_YESOK_SHOW_NEXT_CONTENT | ON_CLICK_DISPOSE_DIALOG;

	public static final String MSG_OK = DialogContentPane.class.getName() + "_OK";
	public static final String MSG_YES = DialogContentPane.class.getName() + "_yes";
	public static final String MSG_NO = DialogContentPane.class.getName() + "_no";
	public static final String MSG_NEXT = DialogContentPane.class.getName() + "_next";
	public static final String MSG_PREVIOUS = DialogContentPane.class.getName() + "_previous";
	public static final String MSG_FINISH = DialogContentPane.class.getName() + "_finish";
	public static final String MSG_CANCEL = DialogContentPane.class.getName() + "_cancel";
	public static final String MSG_OPERATION_FAILED = DialogContentPane.class.getName() + "_operationFailed";
	public static final String MSG_SEE_FULL_MESSAGE = DialogContentPane.class.getName() + "_seeFullMessage";

	public static final int DEFAULT_BUTTON_EMPTY = 0;
	public static final int DEFAULT_BUTTON_CANCEL = 1;
	public static final int DEFAULT_BUTTON_YES = 2;
	public static final int DEFAULT_BUTTON_OK = DEFAULT_BUTTON_YES;
	public static final int DEFAULT_BUTTON_NO = 3;
	public static final int DEFAULT_BUTTON_HELP = 4;
	public static final int DEFAULT_BUTTON_KEEP = 5;

	private DialogContentPane m_nextContentPane;
	private DialogContentPane m_previousContentPane;
	private RootPaneContainer m_parentDialog;
	private JComponent m_contentPane;
	private JPanel m_titlePane;
	private JPanel m_rootPane;
	private Container m_panelOptions;
	private JAPHtmlMultiLineLabel m_lblMessage;
	private LinkedDialog m_linkedDialog;
	private JAPHtmlMultiLineLabel m_lblText;
	private int m_messageType;
	private int m_optionType;
	private int m_defaultButtonOperation;
	private int m_value;
	private JAPHelpContext.IHelpContext m_helpContext;
	private JButton m_btnHelp;
	private JButton m_btnYesOK;
	private JButton m_btnNo;
	private JButton m_btnCancel;
	private ButtonListener m_buttonListener;
	private Icon m_icon;
	private boolean m_bHasHadWizardLayout;
	private GridBagConstraints m_constraints;
	private Vector m_rememberedErrors = new Vector();
	private Vector m_rememberedUpdateErrors = new Vector();
	private Container m_currentlyActiveContentPane;
	private Vector m_componentListeners = new Vector();
	private ComponentListener m_currentlyActiveContentPaneComponentListener;
	private int m_defaultButton;

	/**
	 * Contructs a new dialog content pane. Its layout is predefined, but may change if the content pane
	 * is part of a wizard.
	 * @param a_parentDialog the parent dialog; a content pane is always registered to a dialog, and
	 * may not change it in lifetime.
	 * @param a_strText A text that is shown withing the content pane. The text is interpreted as HTML. If
	 * you call pack() on the dialog when it is updated with this content pane, the text length is
	 * auto-formatted so that its width is not bigger than the content with respect to a minimum size.
	 * Notice: this only works correctly if you call pack() on an invisible dialog.
	 */
	public DialogContentPane(JDialog a_parentDialog, String a_strText)
	{
		this((RootPaneContainer)a_parentDialog, a_strText, new Layout(""), null);
	}

	/**
	 * Contructs a new dialog content pane. Its layout is predefined, but may change if the content pane
	 * is part of a wizard.
	 * @param a_parentDialog the parent dialog; a content pane is always registered to a dialog, and
	 * may not change it in lifetime.
	 * @param a_strText A text that is shown withing the content pane. The text is interpreted as HTML. If
	 * you call pack() on the dialog when it is updated with this content pane, the text length is
	 * auto-formatted so that its width is not bigger than the content with respect to a minimum size.
	 * Notice: this only works correctly if you call pack() on an invisible dialog.
	 */
	public DialogContentPane(JAPDialog a_parentDialog, String a_strText)
	{
		this((RootPaneContainer)a_parentDialog, a_strText, new Layout(""), null);
	}

	/**
	 * Contructs a new dialog content pane. Its layout is predefined, but may change if the content pane
	 * is part of a wizard.
	 * @param a_parentDialog the parent dialog; a content pane is always registered to a dialog, and
	 * may not change it in lifetime.
	 * @param a_strText A text that is shown withing the content pane. The text is interpreted as HTML. If
	 * you call pack() on the dialog when it is updated with this content pane, the text length is
	 * auto-formatted so that its width is not bigger than the content with respect to a minimum size.
	 * Notice: this only works correctly if you call pack() on an invisible dialog.
	 * @param a_layout the general layout of the content pane (icon, title, border, ...)
	 */
	public DialogContentPane(JDialog a_parentDialog, String a_strText, Layout a_layout)
	{
		this((RootPaneContainer)a_parentDialog, a_strText, a_layout, null);
	}

	/**
	 * Contructs a new dialog content pane. Its layout is predefined, but may change if the content pane
	 * is part of a wizard.
	 * @param a_parentDialog the parent dialog; a content pane is always registered to a dialog, and
	 * may not change it in lifetime.
	 * @param a_strText A text that is shown withing the content pane. The text is interpreted as HTML. If
	 * you call pack() on the dialog when it is updated with this content pane, the text length is
	 * auto-formatted so that its width is not bigger than the content with respect to a minimum size.
	 * Notice: this only works correctly if you call pack() on an invisible dialog.
	 * @param a_layout the general layout of the content pane (icon, title, border, ...)
	 */
	public DialogContentPane(JAPDialog a_parentDialog, String a_strText, Layout a_layout)
	{
		this((RootPaneContainer)a_parentDialog, a_strText, a_layout, null);
	}

	/**
	 * Contructs a new dialog content pane. Its layout is predefined, but may change if the content pane
	 * is part of a wizard.
	 * @param a_parentDialog the parent dialog; a content pane is always registered to a dialog, and
	 * may not change it in lifetime.
	 * @param a_strText A text that is shown withing the content pane. The text is interpreted as HTML. If
	 * you call pack() on the dialog when it is updated with this content pane, the text length is
	 * auto-formatted so that its width is not bigger than the content with respect to a minimum size.
	 * Notice: this only works correctly if you call pack() on an invisible dialog.
	 * @param a_options the button definitions
	 */
	public DialogContentPane(JDialog a_parentDialog, String a_strText, Options a_options)
	{
		this((RootPaneContainer)a_parentDialog, a_strText, new Layout(""), a_options);
	}

	/**
	 * Contructs a new dialog content pane. Its layout is predefined, but may change if the content pane
	 * is part of a wizard.
	 * @param a_parentDialog the parent dialog; a content pane is always registered to a dialog, and
	 * may not change it in lifetime.
	 * @param a_strText A text that is shown withing the content pane. The text is interpreted as HTML. If
	 * you call pack() on the dialog when it is updated with this content pane, the text length is
	 * auto-formatted so that its width is not bigger than the content with respect to a minimum size.
	 * Notice: this only works correctly if you call pack() on an invisible dialog.
	 * @param a_options the button definitions
	 */
	public DialogContentPane(JAPDialog a_parentDialog, String a_strText, Options a_options)
	{
		this((RootPaneContainer)a_parentDialog, a_strText, new Layout(""), a_options);
	}

	/**
	 * Contructs a new dialog content pane. Its layout is predefined, but may change if the content pane
	 * is part of a wizard.
	 * @param a_parentDialog the parent dialog; a content pane is always registered to a dialog, and
	 * may not change it in lifetime.
	 * @param a_strText A text that is shown withing the content pane. The text is interpreted as HTML. If
	 * you call pack() on the dialog when it is updated with this content pane, the text length is
	 * auto-formatted so that its width is not bigger than the content with respect to a minimum size.
	 * Notice: this only works correctly if you call pack() on an invisible dialog.
	 * @param a_layout the general layout of the content pane (icon, title, border, ...)
	 * @param a_options the button definitions
	 */
	public DialogContentPane(JDialog a_parentDialog, String a_strText, Layout a_layout, Options a_options)
	{
		this((RootPaneContainer)a_parentDialog, a_strText, a_layout, a_options);
	}

	/**
	 * Contructs a new dialog content pane. Its layout is predefined, but may change if the content pane
	 * is part of a wizard.
	 * @param a_parentDialog the parent dialog; a content pane is always registered to a dialog, and
	 * may not change it in lifetime.
	 * @param a_strText A text that is shown withing the content pane. The text is interpreted as HTML. If
	 * you call pack() on the dialog when it is updated with this content pane, the text length is
	 * auto-formatted so that its width is not bigger than the content with respect to a minimum size.
	 * Notice: this only works correctly if you call pack() on an invisible dialog.
	 * @param a_layout the general layout of the content pane (icon, title, border, ...)
	 * @param a_options the button definitions
	 */
	public DialogContentPane(JAPDialog a_parentDialog, String a_strText, Layout a_layout, Options a_options)
	{
		this((RootPaneContainer)a_parentDialog, a_strText, a_layout, a_options);
	}

	/**
	 * Contructs a new dialog content pane. Its layout is predefined, but may change if the content pane
	 * is part of a wizard.
	 * @param a_parentDialog the parent dialog; a content pane is always registered to a dialog, and
	 * may not change it in lifetime.
	 */
	public DialogContentPane(JDialog a_parentDialog)
	{
		this((RootPaneContainer)a_parentDialog, null, new Layout(""), null);
	}

	/**
	 * Contructs a new dialog content pane. Its layout is predefined, but may change if the content pane
	 * is part of a wizard.
	 * @param a_parentDialog the parent dialog; a content pane is always registered to a dialog, and
	 * may not change it in lifetime.
	 */
	public DialogContentPane(JAPDialog a_parentDialog)
	{
		this((RootPaneContainer)a_parentDialog, null, new Layout(""), null);
	}

	/**
	 * Contructs a new dialog content pane. Its layout is predefined, but may change if the content pane
	 * is part of a wizard.
	 * @param a_parentDialog the parent dialog; a content pane is always registered to a dialog, and
	 * may not change it in lifetime.
	 * @param a_layout the general layout of the content pane (icon, title, border, ...)
	 */
	public DialogContentPane(JDialog a_parentDialog, Layout a_layout)
	{
		this((RootPaneContainer)a_parentDialog, null, a_layout, null);
	}

	/**
	 * Contructs a new dialog content pane. Its layout is predefined, but may change if the content pane
	 * is part of a wizard.
	 * @param a_parentDialog the parent dialog; a content pane is always registered to a dialog, and
	 * may not change it in lifetime.
	 * @param a_layout the general layout of the content pane (icon, title, border, ...)
	 */
	public DialogContentPane(JAPDialog a_parentDialog, Layout a_layout)
	{
		this((RootPaneContainer)a_parentDialog, null, a_layout, null);
	}

	/**
	 * Contructs a new dialog content pane. Its layout is predefined, but may change if the content pane
	 * is part of a wizard.
	 * @param a_parentDialog the parent dialog; a content pane is always registered to a dialog, and
	 * may not change it in lifetime.
	 * @param a_options the button definitions
	 */
	public DialogContentPane(JDialog a_parentDialog, Options a_options)
	{
		this((RootPaneContainer)a_parentDialog, null, new Layout(""), a_options);
	}

	/**
	 * Contructs a new dialog content pane. Its layout is predefined, but may change if the content pane
	 * is part of a wizard.
	 * @param a_parentDialog the parent dialog; a content pane is always registered to a dialog, and
	 * may not change it in lifetime.
	 * @param a_options the button definitions
	 */
	public DialogContentPane(JAPDialog a_parentDialog, Options a_options)
	{
		this((RootPaneContainer)a_parentDialog, null, new Layout(""), a_options);
	}

	/**
	 * Contructs a new dialog content pane. Its layout is predefined, but may change if the content pane
	 * is part of a wizard.
	 * @param a_parentDialog the parent dialog; a content pane is always registered to a dialog, and
	 * may not change it in lifetime.
	 * @param a_layout the general layout of the content pane (icon, title, border, ...)
	 * @param a_options the button definitions
	 */
	public DialogContentPane(JDialog a_parentDialog, Layout a_layout, Options a_options)
	{
		this((RootPaneContainer)a_parentDialog, null, a_layout, a_options);
	}

	/**
	 * Contructs a new dialog content pane. Its layout is predefined, but may change if the content pane
	 * is part of a wizard.
	 * @param a_parentDialog the parent dialog; a content pane is always registered to a dialog, and
	 * may not change it in lifetime.
	 * @param a_layout the general layout of the content pane (icon, title, border, ...)
	 * @param a_options the button definitions
	 */
	public DialogContentPane(JAPDialog a_parentDialog, Layout a_layout, Options a_options)
	{
		this((RootPaneContainer)a_parentDialog, null, a_layout, a_options);
	}

	/**
	 * Contructs a new dialog content pane. Its layout is predefined, but may change if the content pane
	 * is part of a wizard.
	 * @param a_parentDialog the parent dialog; a content pane is always registered to a dialog, and
	 * may not change it in lifetime.
	 * @param a_strText A text that is shown withing the content pane. The text is interpreted as HTML. If
	 * you call pack() on the dialog when it is updated with this content pane, the text length is
	 * auto-formatted so that its width is not bigger than the content with respect to a minimum size.
	 * Notice: this only works correctly if you call pack() on an invisible dialog.
	 * @param a_layout the general layout of the content pane (icon, title, border, ...)
	 * @param a_options the button definitions
	 */
	private DialogContentPane(RootPaneContainer a_parentDialog, String a_strText,
							  Layout a_layout, Options a_options)
	{
		if (a_layout == null)
		{
			a_layout = new Layout((String)null);
		}
		if (a_options == null)
		{
			a_options = new Options((JAPHelpContext.IHelpContext)null);
		}

		init(a_parentDialog, a_layout.getTitle(), a_strText, a_options.getOptionType(),
			 a_layout.getMessageType(), a_layout.getIcon(), a_options.getHelpContext(),
			 a_options.getPreviousContentPane());
	}

	/**
	 * Initialises this content pane.
	 * @param a_parentDialog either a JDialog or a JAPDialog; <CODE>null</CODE> will lead to an Exception
	 * @param a_strTitle A title for the content pane that is shown in a TitledBorder. If the title is null
	 * or empty, no border is shown around the content pane. If the title is not null
	 * (may be an empty String), a status bar is shown between the content pane and the buttons.
	 * @param a_strText A text that is shown withing the content pane. The text is interpreted as HTML. If
	 * you call pack() on the dialog when it is updated with this content pane, the text length is
	 * auto-formatted so that its width is not bigger than the content with respect to a minimum size.
	 * Notice: this only works correctly if you call pack() on an invisible dialog.
	 * @param a_optionType one of the available option types the define the type and number of buttons
	 * @param a_messageType one of the available message types that define the message layout
	 * @param a_icon an Icon; if null, the icon will be chosen automatically depending on the message type
	 * @param a_helpContext a IHelpContext; if it returns an other help context value than null,
	 * a help button is shown that opens the context;
	 * @param a_previousContentPane A DialogContentPane that will be linked with this one; it gets this
	 * content pane as next content pane. Call moveToNextContentPane() and moveToPreviousContentPane() to
	 * move between the panes.
	 */
	private void init(RootPaneContainer a_parentDialog, String a_strTitle, String a_strText, int a_optionType,
					  int a_messageType, Icon a_icon, JAPHelpContext.IHelpContext a_helpContext,
					  DialogContentPane a_previousContentPane)
	{
		if (a_parentDialog == null)
		{
			throw new IllegalArgumentException("The parent dialog must not be null!");
		}

		if (a_previousContentPane != null && a_previousContentPane.m_parentDialog != a_parentDialog)
		{
			throw new IllegalArgumentException("Chained content panes must refer to the same dialog!");
		}

		if (!(a_optionType == OPTION_TYPE_EMPTY || a_optionType == OPTION_TYPE_DEFAULT ||
			a_optionType == OPTION_TYPE_CANCEL || a_optionType == OPTION_TYPE_OK_CANCEL ||
			a_optionType == OPTION_TYPE_YES_NO_CANCEL || a_optionType == OPTION_TYPE_YES_NO))
		{
			throw new IllegalArgumentException("Unknown option type!");
		}

		if (!(a_messageType == MESSAGE_TYPE_PLAIN || a_messageType == MESSAGE_TYPE_QUESTION ||
			a_messageType == MESSAGE_TYPE_ERROR || a_messageType == MESSAGE_TYPE_WARNING ||
			a_messageType == MESSAGE_TYPE_INFORMATION))
		{
			throw new IllegalArgumentException("Unknown message type!");
		}

		if (this instanceof IWizardSuitable)
		{
			m_defaultButtonOperation = BUTTON_OPERATION_WIZARD;
		}
		else
		{
			m_defaultButtonOperation = ON_CLICK_DO_NOTHING;
		}

		m_parentDialog = a_parentDialog;
		m_previousContentPane = a_previousContentPane;
		m_messageType = a_messageType;
		m_optionType = a_optionType;
		m_icon = a_icon;
		m_helpContext = a_helpContext;
		m_rootPane = new JPanel(new BorderLayout());
		m_titlePane = new JPanel(new GridBagLayout());
		m_rootPane.add(m_titlePane, BorderLayout.CENTER);


		addDialogComponentListener(new DialogComponentListener());
		addDialogWindowListener(new DialogWindowListener());


		m_constraints = new GridBagConstraints();
		m_constraints.gridx = 0;
		m_constraints.gridy = 0;
		m_constraints.weightx = 1;
		m_constraints.weighty = 1;
		m_constraints.gridy = 1;
		m_constraints.anchor = GridBagConstraints.NORTH;
		m_constraints.fill = GridBagConstraints.BOTH;

		m_contentPane = new JPanel();
		m_titlePane.add(m_contentPane, m_constraints);


		if (a_strTitle != null)
		{
			if (a_strTitle.trim().length() > 0)
			{
				m_titlePane.setBorder(new TitledBorder(a_strTitle));
			}
			// the status message bar is only shown if the title is a valid String (may be empty but not null)
			m_lblMessage = new JAPHtmlMultiLineLabel();
			m_lblMessage.setFontStyle(JAPHtmlMultiLineLabel.FONT_STYLE_BOLD);
			clearStatusMessage();
			m_rootPane.add(m_lblMessage, BorderLayout.SOUTH);
		}

		if (a_strText != null && a_strText.trim().length() > 0)
		{
			m_lblText = new JAPHtmlMultiLineLabel("<font color=#000000>" + a_strText + "</font>",
				SwingConstants.CENTER);
			m_lblText.setFontStyle(JAPHtmlMultiLineLabel.FONT_STYLE_PLAIN);
		}
		// set the contraints for the text label; they are used later
		m_constraints.gridy = 0;
		m_constraints.weighty = 0;
		m_constraints.fill = GridBagConstraints.HORIZONTAL;
		m_constraints.insets = new Insets(5, 5, 5, 5);

		// construct the chain of content panes
		if (m_previousContentPane != null)
		{
			m_previousContentPane.setNextContentPane(this);
		}

		m_bHasHadWizardLayout = false;
		m_value = RETURN_VALUE_UNINITIALIZED;

		// create the buttons
		createOptions();

		// set the default button; if possible, OK is chosen, otherwise cancel or help
		if (a_optionType == OPTION_TYPE_DEFAULT || a_optionType == OPTION_TYPE_YES_NO ||
			a_optionType == OPTION_TYPE_OK_CANCEL || a_optionType == OPTION_TYPE_YES_NO_CANCEL)
		{
			setDefaultButton(DEFAULT_BUTTON_OK);
		}
		else if (a_optionType == OPTION_TYPE_CANCEL)
		{
			setDefaultButton(DEFAULT_BUTTON_CANCEL);
		}
		else
		{
			if (getButtonHelp() != null)
			{
				setDefaultButton(DEFAULT_BUTTON_HELP);
			}
			else
			{
				setDefaultButton(DEFAULT_BUTTON_KEEP);
			}
		}
	}

	/**
	 * Content panes that are suitable for use in a wizard should implement this interface.
	 * If implemented, and the content pane is at least chained with one other content pane
	 * (next or previous), the buttons are displayed in the style of a wizard: "No" -> "Previous",
	 * "Yes" -> "Next", "Cancel". The last pane in the chain gets a "Finish" instead of "Next".
	 * <P> A class that implements the wizard layout has the button operation
	 * BUTTON_OPERATION_WIZARD by default. Of course, this may be altered by calling
	 * setDefaultButtonOperation(int). </P>
	 */
	public static interface IWizardSuitable
	{
	}

	/**
	 * Classes that are WizardSuitable but do not want to get the wizard buttons should implement this
	 * interface.
	 */
	public static interface IWizardSuitableNoWizardButtons extends IWizardSuitable
	{
	}

	/**
	 * A CheckError is used to set error conditions that prohibit operations. The error conditions may
	 * contain a message that is dispayed to the user and may do some additional actions.
	 */
	public static class CheckError
	{
		private String m_strMessage;

		/**
		 * A new CheckError with an empty message String. No message will be displayed to the user and
		 * no error will be logged.
		 */
		public CheckError()
		{
			this("");
		}

		/**
		 * A new CheckError with a message for the user.
		 * @param a_strMessage a message for the user; if empty, no message is displayed; if null,
		 * an error message is auto-generated
		 */
		public CheckError(String a_strMessage)
		{
			m_strMessage = a_strMessage;
		}

		/**
		 * The action that is done if this error is handled by any method.
		 */
		public void doErrorAction()
		{
		}

		/**
		 * The action that is done to reset the state before the call of doErrorAction().
		 * All methods that interpret doErrorAction() should interpret undoErrorAction() and call
		 * undoErrorAction() on all errors on that it had called doErrorAction() before interpreting
		 * new errors.
		 */
		public void undoErrorAction()
		{
		}

		/**
		 * The message to display to the user.
		 * @return the message to display to the user
		 */
		public final String getMessage()
		{
			return m_strMessage;
		}
	}

	/**
	 * Defines the buttons that are available in a dialog.
	 */
	public static final class Options
	{
		private int m_optionType;
		private DialogContentPane m_previousContentPane;
		private JAPHelpContext.IHelpContext m_helpContext;

		/**
		 * Creates new button options.
		 * @param a_optionType one of the available option types the define the type and number of buttons
		 */
		public Options(int a_optionType)
		{
			this(a_optionType, (JAPHelpContext.IHelpContext)null, null);
		}

		/**
		 * Creates new button options. No buttons are shown by default.
		 * @param a_strHelpContext a IHelpContext; if it returns an other help context value than null,
		 * a help button is shown that opens the context;
		 */
		public Options(String a_strHelpContext)
		{
			this(OPTION_TYPE_EMPTY, a_strHelpContext, null);
		}

		/**
		 * Creates new button options. No buttons are shown by default.
		 * @param a_helpContext a IHelpContext; if it returns an other help context value than null,
		 * a help button is shown that opens the context;
		 */
		public Options(JAPHelpContext.IHelpContext a_helpContext)
		{
			this(OPTION_TYPE_EMPTY, a_helpContext, null);
		}

		/**
		 * Creates new button options. No buttons are shown by default.
		 * @param a_previousContentPane A DialogContentPane that will be linked with this one; it gets this
		 * content pane as next content pane. Call moveToNextContentPane() and moveToPreviousContentPane() to
		 * move between the panes.
		 */
		public Options(DialogContentPane a_previousContentPane)
		{
			this(OPTION_TYPE_EMPTY, (JAPHelpContext.IHelpContext)null, a_previousContentPane);
		}

		/**
		 * Creates new button options. No buttons are shown by default.
		 * @param a_strHelpContext a IHelpContext; if it returns an other help context value than null,
		 * a help button is shown that opens the context;
		 * @param a_previousContentPane A DialogContentPane that will be linked with this one; it gets this
		 * content pane as next content pane. Call moveToNextContentPane() and moveToPreviousContentPane() to
		 * move between the panes.
		 */
		public Options(String a_strHelpContext, DialogContentPane a_previousContentPane)
		{
			this(OPTION_TYPE_EMPTY, a_strHelpContext, a_previousContentPane);
		}

		/**
		 * Creates new button options. No buttons are shown by default.
		 * @param a_helpContext a IHelpContext; if it returns an other help context value than null,
		 * a help button is shown that opens the context;
		 * @param a_previousContentPane A DialogContentPane that will be linked with this one; it gets this
		 * content pane as next content pane. Call moveToNextContentPane() and moveToPreviousContentPane() to
		 * move between the panes.
		 */
		public Options(JAPHelpContext.IHelpContext a_helpContext, DialogContentPane a_previousContentPane)
		{
			this(OPTION_TYPE_EMPTY, a_helpContext, a_previousContentPane);
		}

		/**
		 * Creates new button options.
		 * @param a_optionType one of the available option types the define the type and number of buttons
		 * @param a_previousContentPane A DialogContentPane that will be linked with this one; it gets this
		 * content pane as next content pane. Call moveToNextContentPane() and moveToPreviousContentPane() to
		 * move between the panes.
		 */
		public Options(int a_optionType, DialogContentPane a_previousContentPane)
		{
			this(a_optionType, (JAPHelpContext.IHelpContext)null, a_previousContentPane);
		}

		/**
		 * Creates new button options.
		 * @param a_optionType one of the available option types the define the type and number of buttons
		 * @param a_helpContext a IHelpContext; if it returns an other help context value than null,
		 * a help button is shown that opens the context;
		 */
		public Options(int a_optionType, JAPHelpContext.IHelpContext a_helpContext)
		{
			this(a_optionType, a_helpContext, null);
		}

		/**
		 * Creates new button options.
		 * @param a_optionType one of the available option types the define the type and number of buttons
		 * @param a_strHelpContext a IHelpContext; if it returns an other help context value than null,
		 * a help button is shown that opens the context;
		 */
		public Options(int a_optionType, String a_strHelpContext)
		{
			this(a_optionType, a_strHelpContext, null);
		}


		/**
		 * Creates new button options.
		 * @param a_optionType one of the available option types the define the type and number of buttons
		 * @param a_strHelpContext a IHelpContext; if it returns an other help context value than null,
		 * a help button is shown that opens the context;
		 * @param a_previousContentPane A DialogContentPane that will be linked with this one; it gets this
		 * content pane as next content pane. Call moveToNextContentPane() and moveToPreviousContentPane() to
		 * move between the panes.
		 */
		public Options(int a_optionType, final String a_strHelpContext, DialogContentPane a_previousContentPane)
		{
			this(a_optionType,
				 new JAPHelpContext.IHelpContext(){public String getHelpContext(){return a_strHelpContext;}},
				a_previousContentPane);
		}

		/**
		 * Creates new button options.
		 * @param a_optionType one of the available option types the define the type and number of buttons
		 * @param a_helpContext a IHelpContext; if it returns an other help context value than null,
		 * a help button is shown that opens the context;
		 * @param a_previousContentPane A DialogContentPane that will be linked with this one; it gets this
		 * content pane as next content pane. Call moveToNextContentPane() and moveToPreviousContentPane() to
		 * move between the panes.
		 */
		public Options(int a_optionType, JAPHelpContext.IHelpContext a_helpContext,
					   DialogContentPane a_previousContentPane)
		{
			m_optionType = a_optionType;
			m_helpContext = a_helpContext;
			m_previousContentPane = a_previousContentPane;
		}

		public int getOptionType()
		{
			return m_optionType;
		}

		public JAPHelpContext.IHelpContext getHelpContext()
		{
			return m_helpContext;
		}

		public DialogContentPane getPreviousContentPane()
		{
			return m_previousContentPane;
		}
	}

	/**
	 * Defines the general layout of a dialog.
	 */
	public static final class Layout
	{
		private String m_strTitle;
		private int m_messageType;
		private Icon m_icon;

		/**
		 * Creates a new Layout for the dialog content pane. The title is empty, therefore a status bar
		 * will be shown in the content pane.
		 * @param a_messageType The content pane's message type,
		 * e.g. MESSAGE_TYPE_PLAIN, MESSAGE_TYPE_ERROR, ...
		 */
		public Layout(int a_messageType)
		{
			this("", a_messageType, null);
		}

		/**
		 * Creates a new Layout for the dialog content pane.
		 * @param a_strTitle A title for the content pane that is shown in a TitledBorder. If the title is
		 * null or empty, no border is shown around the content pane. If the title is not null
		 * (may be an empty String), a status bar is shown between the content pane and the buttons.
		 */
		public Layout(String a_strTitle)
		{
			this(a_strTitle, MESSAGE_TYPE_PLAIN, null);
		}

		/**
		 * Creates a new Layout for the dialog content pane. The title is empty, therefore a status bar
		 * will be shown in the content pane.
		 * @param a_icon The icon for the content pane. If is is null, the icon will be automatically chosen
		 * depending on the message type.
		 */
		public Layout(Icon a_icon)
		{
			this("", MESSAGE_TYPE_PLAIN, a_icon);
		}

		/**
		 * Creates a new Layout for the dialog content pane. The title is empty, therefore a status bar
		 * will be shown in the content pane.
		 * @param a_messageType The content pane's message type,
		 * e.g. MESSAGE_TYPE_PLAIN, MESSAGE_TYPE_ERROR, ...
		 * @param a_icon The icon for the content pane. If is is null, the icon will be automatically chosen
		 * depending on the message type.
		 */
		public Layout(int a_messageType, Icon a_icon)
		{
			this("", a_messageType, a_icon);
		}

		/**
		 * Creates a new Layout for the dialog content pane.
		 * @param a_strTitle A title for the content pane that is shown in a TitledBorder. If the title is
		 * null or empty, no border is shown around the content pane. If the title is not null
		 * (may be an empty String), a status bar is shown between the content pane and the buttons.
		 * @param a_messageType The content pane's message type,
		 * e.g. MESSAGE_TYPE_PLAIN, MESSAGE_TYPE_ERROR, ...
		 * depending on the message type.
		 */
		public Layout(String a_strTitle, int a_messageType)
		{
			this(a_strTitle, a_messageType, null);
		}

		/**
		 * Creates a new Layout for the dialog content pane.
		 * @param a_strTitle A title for the content pane that is shown in a TitledBorder. If the title is
		 * null or empty, no border is shown around the content pane. If the title is not null
		 * (may be an empty String), a status bar is shown between the content pane and the buttons.
		 * @param a_icon The icon for the content pane. If is is null, the icon will be automatically chosen
		 * depending on the message type.
		 */
		public Layout(String a_strTitle, Icon a_icon)
		{
			this(a_strTitle, MESSAGE_TYPE_PLAIN, a_icon);
		}

		/**
		 * Creates a new Layout for the dialog content pane.
		 * @param a_strTitle A title for the content pane that is shown in a TitledBorder. If the title is
		 * null or empty, no border is shown around the content pane. If the title is not null
		 * (may be an empty String), a status bar is shown between the content pane and the buttons.
		 * @param a_messageType The content pane's message type,
		 * e.g. MESSAGE_TYPE_PLAIN, MESSAGE_TYPE_ERROR, ...
		 * @param a_icon The icon for the content pane. If is is null, the icon will be automatically chosen
		 * depending on the message type.
		 */
		public Layout(String a_strTitle, int a_messageType, Icon a_icon)
		{
			m_strTitle = a_strTitle;
			m_messageType = a_messageType;
			m_icon = a_icon;
		}

		/**
		 * Returns the title of the content pane that is shown in a TitledBorder. If the title is
		 * null or empty, no border is shown around the content pane. If the title is not null
		 * (may be an empty String), a status bar is shown between the content pane and the buttons.
		 * @return the title of the dialog content pane
		 */
		public String getTitle()
		{
			return m_strTitle;
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
	 * Calculates the optimal dialog size for a chain of content panes. The optimal size is defined
	 * as the size that is needed to the contents pane with the maximum width or the maximum size in
	 * the chain. The chain is defined as the content panes that are returned by calling
	 * <CODE> getNextContentPane() </CODE> on each one.
	 * <P> Note 1: This method needs to call updateDialog() for every content pane in the chain. YOu should
	 * therefore never call this method on a dialog that is visible! This would cause serious flickering
	 * in the best case, in the worst case the wrong dialog is shown afterwards (that is the last dialog
	 * in the chain). </P>
	 * <P> Note 2: If the content pane that should show up first is not the content pane you gave as
	 * argument, you will have to call <CODE> updateDialog() </CODE> on it after calling this method.</P>

	 * @param a_firstContentPane the first DialogContentPane in a chain of content panes; this method will
	 * call <CODE> updateDialog() </CODE> on it to initialise the dialog
	 * @todo make sure that several calls of this method lead to the same result
	 */
	public static void updateDialogOptimalSized(DialogContentPane a_firstContentPane)
	{
		int width, height;
		DialogContentPane nextContentPane;

		if (a_firstContentPane == null)
		{
			return;
		}

		width = 0;
		height = 0;
		nextContentPane = a_firstContentPane;
		if (a_firstContentPane.m_parentDialog instanceof Window)
		{
			Window dialog = (Window)a_firstContentPane.m_parentDialog;
			do
			{
				nextContentPane.updateDialog();
				dialog.pack();
				width = Math.max(width, dialog.getSize().width);
				height = Math.max(height, dialog.getSize().height);
				nextContentPane = nextContentPane.getNextContentPane();
			}
			while (nextContentPane != null);
			dialog.setSize(new Dimension(width, height));
		}
		else
		{
			JAPDialog dialog = (JAPDialog)a_firstContentPane.m_parentDialog;
			do
			{
				nextContentPane.updateDialog();
				dialog.pack();
				width = Math.max(width, dialog.getSize().width);
				height = Math.max(height, dialog.getSize().height);
				nextContentPane = nextContentPane.getNextContentPane();
			}
			while (nextContentPane != null);
			dialog.setSize(new Dimension(width, height));
		}

		// reset the preferred size of each content pane
		nextContentPane = a_firstContentPane;
		do
		{
			/*
			 * The root pane's preferred size is as big as the dialog, therefore the content pane should fit
			 * into the dialog. Otherwise, an additional pack() command would be needed.
			 */
			nextContentPane.m_rootPane.setPreferredSize(new Dimension(width, height));
			nextContentPane = nextContentPane.getNextContentPane();
		}
		while (nextContentPane != null);

		a_firstContentPane.updateDialog();
	}

	/**
	 * Returns if this content pane is formatted with the wizard layout. The "Yes" and "OK" buttons will
	 * be transformed to "Next", the "No" button is replaced by "Previous" and all buttons are shown
	 * (Cancel, Previous, Next), not regarding what buttons have been defined by the option type.
	 * If the dialog window is opened, the focus will automatically be set on "Next".
	 * <P> If a class wants to keep its own buttons as defined by the option type but act in a wizard,
	 * it has to implement IWizardSuitableNoWizardButtons.
	 * This will prevent that is gets the wizard layout. </P>
	 * @return if this content pane is formatted with the wizard layout
	 */
	public final boolean hasWizardLayout()
	{
		DialogContentPane contentPane = this;

		if (// this content pane must be suitable to be a wizard
			  !(contentPane instanceof IWizardSuitable) ||
			  // it must want to get the wizard buttons
			  contentPane instanceof IWizardSuitableNoWizardButtons ||
			  // at least one next or previous content pane is needed to get the wizard layout
			  (getNextContentPane() == null && getPreviousContentPane() == null))
		{
			return false;
		}

		// traverse the previous and next contents panes; if all of them have a wizard layout, we have, too
		while ((contentPane = contentPane.getPreviousContentPane()) != null)
		{
			if (!(contentPane instanceof IWizardSuitable))
			{
				return false;
			}
		}

		contentPane = this;

		while ((contentPane = contentPane.getNextContentPane()) != null)
		{
			if (!(contentPane instanceof IWizardSuitable))
			{
				return false;
			}
		}

		return true;
	}

	/**
	 * Returns the next content pane in the chained list of content panes.
	 * @return the next content pane in the chained list of content panes
	 */
	public final DialogContentPane getNextContentPane()
	{
		return m_nextContentPane;
	}

	/**
	 * Returns the previous content pane in the chained list of content panes.
	 * @return the previous content pane in the chained list of content panes
	 */
	public final DialogContentPane getPreviousContentPane()
	{
		return m_previousContentPane;
	}

	/**
	 * Shows the previous content pane in the dialog if it exists. Otherwise, the dialog is closed according
	 * to the default ON_CLICK operation. If no ON_CLICK operation is set, nothing is done by default.
	 * If the content pane exists, its checkUpdate() method is interpreted and the errors are handled.
	 * @return if a move to the previous content pane was done; false if no previous content pane does exist
	 * or if it refused to update the dialog
	 */
	public final boolean moveToPreviousContentPane()
	{
		return moveToContentPane(false);
	}

	/**
	 * Is called when the "Yes", "OK" or "Next" button is clicked.
	 * If one or more error occured, they should be returned as CheckErrors to inform the user. In this case,
	 * the automatic reaction on the button click is prohibited and getValue() will not change.
	 * Overwrite this method to set your own check; it returns <CODE> null </CODE> by default.
	 * This method should never be called directly and is only used internally.
	 * @return errors that prohibit the operation or null or an empty array if the operation is allowed
	 */
	public CheckError[] checkYesOK()
	{
		return null;
	}

	/**
	 * Is called when the "No" or "Previous" button is clicked.
	 * If one or more error occured, they should be returned as CheckErrors to inform the user. In this case,
	 * the automatic reaction on the button click is prohibited and getValue() will not change.
	 * Overwrite this method to set your own check; it returns <CODE> null </CODE> by default.
	 * This method should never be called directly and is only used internally.
	 * @return errors that prohibit the operation or null or an empty array if the operation is allowed
	 */
	public CheckError[] checkNo()
	{
		return null;
	}

	/**
	 * Is called when the "Cancel" button is clicked.
	 * If one or more error occured, they should be returned as CheckErrors to inform the user. In this case,
	 * the automatic reaction on the button click is prohibited and getValue() will not change.
	 * Overwrite this method to set your own check; it returns <CODE> null </CODE> by default.
	 * This method should never be called directly and is only used internally.
	 * @return errors that prohibit the operation or null or an empty array if the operation is allowed
	 */
	public CheckError[] checkCancel()
	{
		return null;
	}

	/**
	 * Is called when someone calls updateDialog() on this content pane. The update operation
	 * is only performed if null is returned. Otherwise, the caller may interpret the errors he gets from
	 * updateDialog. This is done by <CODE> moveToNextContentPane() </CODE> and
	 * <CODE> moveToPreviousContentPane() </CODE>.
	 * Overwrite this method to set your own check; it returns <CODE> null </CODE> by default.
	 * This method should never be called directly and is only used internally.
	 * @return errors that prohibit the operation or null or an empty array if the operation is allowed
	 */
	public CheckError[] checkUpdate()
	{
		return null;
	}

	/**
	 * If the previous content pane of this one calls moveToNextContentPane(), this content pane may tell him
	 * to skip it and move forward to the next one.
	 * Returns <CODE> false </CODE> by default but may be overwritten by subclasses.
	 * @return true if this content pane would like to be skipped as next content pane; false otherwise
	 */
	public boolean isSkippedAsNextContentPane()
	{
		return false;
	}

	/**
	 * If the next content pane of this one calls moveToPreviousContentPane(), this content pane may tell him
	 * to skip it and move forward to the next one.
	 * Returns <CODE> false </CODE> by default but may be overwritten by subclasses.
	 * @return true if this content pane would like to be skipped as previous content pane; false otherwise
	 */
	public boolean isSkippedAsPreviousContentPane()
	{
		return false;
	}

	/**
	 * Shows the next content pane in the dialog if it exists. Otherwise, the dialog is closed according
	 * to the default ON_CLICK operation. If no ON_CLICK operation is set, nothing is done by default.
	 * If the content pane exists, its checkUpdate() method is interpreted and the errors are handled.
	 * @return if a move to the next content pane was done; false if no next content pane does exist
	 * or if it refused to update the dialog
	 */
	public final boolean moveToNextContentPane()
	{
		return moveToContentPane(true);
	}

	/**
	 * Returns the content pane where elements may be placed freely.
	 * @return the content pane
	 */
	public final JComponent getContentPane()
	{
		return m_contentPane;
	}

	/**
	 * Set the parent dialog visible.
	 */
	public final void showDialog()
	{
		if (m_parentDialog instanceof Window)
		{
			((Window)m_parentDialog).setVisible(true);
		}
		else
		{
			((JAPDialog)m_parentDialog).setVisible(true);
		}
	}

	/**
	 * Replace the content pane of this content pane by another one.
	 * @param a_contentPane JComponent
	 */
	public final void setContentPane(JComponent a_contentPane)
	{
		m_rootPane.remove(m_contentPane);
		m_rootPane.add(a_contentPane, BorderLayout.CENTER);
		m_contentPane = a_contentPane;
	}

	/**
	 * Returns the help context or null if no help context is provided by this object.
	 * @return the help context or null if no help context is provided by this object
	 */
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
			m_lblMessage.removeMouseListener(m_linkedDialog);
			m_linkedDialog = null;
			m_lblMessage.setText("");
			m_lblMessage.setToolTipText(null);
		}
	}

	/**
	 * Prints an information message in the status bar. If the status bar is not available, a dialog window
	 * is opened. If the text is too long for the status bar, the text is cut and the user can see it by
	 * clicking on the stauts bar (a dialog window opens).
	 * @param a_message an information message
	 */
	public final void printStatusMessage(String a_message)
	{
		printStatusMessage(a_message, MESSAGE_TYPE_INFORMATION);
	}

	/**
	 * Prints a status message in the status bar. If the status bar is not available, a dialog window
	 * is opened. If the text is too long for the status bar, the text is cut and the user can see it by
	 * clicking on the stauts bar (a dialog window opens).
	 * @param a_message a status message
	 * @param a_messageType the message type; this has a influence on how the message is displayed
	 * (color, icon,...).
	 */
	public final void printStatusMessage(String a_message, int a_messageType)
	{
		if (m_lblMessage != null)
		{
			printStatusMessageInternal(a_message, a_messageType);
		}
		else
		{
			JAPDialog.showConfirmDialog(getContentPane(), a_message, OPTION_TYPE_DEFAULT, a_messageType);
		}
	}

	/**
	 * Prints an error message in the status bar. If the status bar is not available, a dialog window
	 * is opened. If the text is too long for the status bar, the text is cut and the user can see it by
	 * clicking on the stauts bar (a dialog window opens).
	 * @param a_message an error message
	 * @param a_logType the log type of this error
	 */
	public final void printErrorStatusMessage(String a_message, int a_logType)
	{
		printErrorStatusMessage(a_message, a_logType, null);
	}

	/**
	 * Prints an error message in the status bar. If the status bar is not available, a dialog window
	 * is opened. If the text is too long for the status bar, the text is cut and the user can see it by
	 * clicking on the stauts bar (a dialog window opens).
	 * @param a_message an error message
	 * @param a_logType the log type of this error
	 * @param a_throwable a Throwable that has been catched in the context of this error
	 */
	public final void printErrorStatusMessage(String a_message, int a_logType, Throwable a_throwable)
	{
		boolean bPossibleApplicationError = false;

		try {

			a_message = JAPDialog.retrieveErrorMessage(a_message, a_throwable);
			if (a_message == null)
			{
				a_message = JAPMessages.getString(JAPDialog.MSG_ERROR_UNKNOWN);
				bPossibleApplicationError = true;
			}


			if (m_lblMessage != null)
			{
				printStatusMessageInternal(a_message, MESSAGE_TYPE_ERROR);

				LogHolder.log(LogLevel.ERR, a_logType, a_message, true);
				if (a_throwable != null)
				{
					// the exception is only shown in debug mode or in case of an application error
					if (bPossibleApplicationError)
					{
						LogHolder.log(LogLevel.ERR, a_logType, a_throwable);
					}
					else
					{
						LogHolder.log(LogLevel.DEBUG, a_logType, a_throwable);
					}
				}

			}
			else
			{
				JAPDialog.showErrorDialog(getContentPane(), a_message, a_logType, a_throwable);
			}
		}
		catch (Throwable a_e)
		{
			JAPDialog.showErrorDialog(getContentPane(), LogType.GUI, a_e);
		}

	}

	/**
	 * Replaces the content pane of the parent dialog with the content defined in this object.
	 * @return the errors returned by checkUpdate() or null or an empty array if no errors occured and
	 * the update has been done
	 */
	public final synchronized CheckError[] updateDialog()
	{
		JDialog dialog;
		JOptionPane pane;
		Object[] options;
		CheckError[] errors;

		errors = checkUpdate();

		if (errors != null && errors.length > 0)
		{
			return errors;
		}

		createOptions();
		options = new Object[1];
		options[0] = m_panelOptions;
		if (m_lblText != null)
		{
			m_titlePane.remove(m_lblText);
		}
		pane = new JOptionPane(m_rootPane, m_messageType, 0, m_icon, options );
		dialog = pane.createDialog(null, "");
		dialog.pack();
		if (m_lblText != null)
		{
			if (isDialogVisible())
			{
				// the dialog is visible and will resize the label automatically as needed
				m_lblText = new JAPHtmlMultiLineLabel(m_lblText.getText(), m_lblText.getFont(),
					SwingConstants.CENTER);
			}
			else
			{
				if (m_lblText.getPreferredSize().width > (m_contentPane.getWidth() - 10))
				{
					// the width of the label must be restricted to make the pack() operation possible
					m_lblText.setPreferredWidth(Math.max(m_contentPane.getWidth() - 10, MIN_TEXT_LENGTH));
				}
			}

			m_titlePane.add(m_lblText, m_constraints);
		}

		clearStatusMessage();

		// initialize the new content pane
		if (m_currentlyActiveContentPane != null)
		{
			m_currentlyActiveContentPane.removeComponentListener(
						 m_currentlyActiveContentPaneComponentListener);
		}
		m_currentlyActiveContentPane = dialog.getContentPane();
		m_currentlyActiveContentPaneComponentListener = new ContentPaneComponentListener();
		m_currentlyActiveContentPane.addComponentListener(m_currentlyActiveContentPaneComponentListener);
		m_parentDialog.setContentPane(m_currentlyActiveContentPane);

		if (isDialogVisible())
		{
			// tell the listeners that the content pane is visible
			Vector listeners = (Vector)m_componentListeners.clone();
			for (int i = 0; i < listeners.size(); i++)
			{
				( (ComponentListener) listeners.elementAt(i)).componentShown(
					new ComponentEvent(m_currentlyActiveContentPane,
									   ComponentEvent.COMPONENT_SHOWN));
			}
		}


		// set default button
		if (m_defaultButton == DEFAULT_BUTTON_OK)
		{
			getDialog().getRootPane().setDefaultButton(getButtonYesOK());
		}
		else if (m_defaultButton == DEFAULT_BUTTON_CANCEL)
		{
			getDialog().getRootPane().setDefaultButton(getButtonCancel());
		}
		else if (m_defaultButton == DEFAULT_BUTTON_NO)
		{
			getDialog().getRootPane().setDefaultButton(getButtonNo());
		}
		else if (m_defaultButton == DEFAULT_BUTTON_HELP)
		{
			getDialog().getRootPane().setDefaultButton(getButtonHelp());
		}
		else if (m_defaultButton != DEFAULT_BUTTON_KEEP)
		{
			getDialog().getRootPane().setDefaultButton(null);
		}


		m_titlePane.invalidate();
		if (m_lblText != null)
		{
			m_lblText.invalidate();
		}
		m_rootPane.invalidate();
		m_contentPane.invalidate();
		if (m_parentDialog instanceof JAPDialog)
		{
			((JAPDialog) m_parentDialog).validate();

		}
		else
		{
			((Window) m_parentDialog).validate();
		}

		return null;
	}

	/**
	 * Returns the "Help" button.
	 * @return the "Help" button
	 */
	public final JButton getButtonHelp()
	{
		return m_btnHelp;
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

	/**
	 * Defines the button to be set as default button of the dialog when updateDialog() is called.
	 * If the content pane has a wizard layout, this setting is ignored. The default behaviour (done by the
	 * constructor) is that it is first tried to make the OK button the default button. If it is not
	 * available, the CANCEL button and the HELP buttons are tried. If those are not available, too, then
	 * no default button is set.
	 * @param a_defaultButton the button to be set as default button of the dialog when updateDialog() is
	 * called, e.g. DEFAULT_BUTTON_OK or DEFAULT_BUTTON_HELP;
	 * DEFAULT_BUTTON_EMPTY will set no button as default (null), DEFAULT_BUTTON_KEEP will keep whatever
	 * has been set before
	 */
	public final void setDefaultButton(int a_defaultButton)
	{
		if (a_defaultButton < DEFAULT_BUTTON_EMPTY || a_defaultButton > DEFAULT_BUTTON_KEEP)
		{
			m_defaultButton = DEFAULT_BUTTON_EMPTY;
		}
		else
		{
			m_defaultButton = a_defaultButton;
		}
	}

	/**
	 * Returns the button to be set as default button of the dialog when updateDialog() is called.
	 * @return the button to be set as default button of the dialog when updateDialog() is called,
	 * e.g. DEFAULT_BUTTON_OK
	 */
	public final int getDefaultButton()
	{
		return m_defaultButton;
	}

	/**
	 * Returns what happens if one of the buttons is clicked. Several actions can be combined,
	 * for example ON_CLICK_DISPOSE_DIALOG | ON_YESOK_SHOW_NEXT_CONTENT will dispose the dialog on
	 * "Cancel" and "No" but will show the next content pane on "Yes" or "OK". The ON_CLICK operation
	 * definitions are always overwritten by the button-specific operation definitions. If no operation
	 * is defined for a button, it will not set a value on click automatically, that means the dialog
	 * will keep its state if no one else sets the value by calling <CODE> setValue() </CODE>.
	 * @return what happens if one of the buttons is clicked
	 */
	public final int getDefaultButtonOperation()
	{
		return m_defaultButtonOperation;
	}

	/**
	 * Defines what happens if one of the buttons is clicked. Several actions can be combined,
	 * for example ON_CLICK_DISPOSE_DIALOG | ON_YESOK_SHOW_NEXT_CONTENT will dispose the dialog on
	 * "Cancel" and "No" but will show the next content pane on "Yes" or "OK". The ON_CLICK operation
	 * definitions are always overwritten by the button-specific operation definitions. If no operation
	 * is defined for a button, it will not set a value on click automatically, that means the dialog
	 * will keep its state if no one else sets the value by calling <CODE> setValue() </CODE>.
	 * May throw an InvalidArgumentException if objects of this type do not support setting the default
	 * button operation.
	 * <P> It is a good idea to set additional button operations preserving the old ones, for example
	 * ON_CANCEL_DISPOSE_DIALOG | getDefaultButtonOperation(). Single button operations may be removed by,
	 * for example, getDefaultButtonOperation() - ON_CANCEL_DISPOSE_DIALOG, but before that make
	 * sure that (getDefaultButtonOperation() & ON_CANCEL_DISPOSE_DIALOG) == ON_CANCEL_DISPOSE_DIALOG returns
	 * <CODE> true </CODE>. </P>
	 * @param a_defaultButtonOperation the default button operation
	 * @throws java.lang.IllegalArgumentException if objects of this type do not support setting the default
	 * button operation
	 */
	public final void setDefaultButtonOperation(int a_defaultButtonOperation)
		throws IllegalArgumentException
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

	/**
	 * Sets the button value. If the type is unknown, it is set to RETURN_VALUE_UNINITIALIZED.
	 * @param a_value the new button value
	 */
	public final void setValue(int a_value)
	{
		if (RETURN_VALUE_CANCEL == a_value || RETURN_VALUE_OK == a_value ||
			RETURN_VALUE_CLOSED == a_value || RETURN_VALUE_YES == a_value || RETURN_VALUE_NO == a_value)
		{
			m_value = a_value;
		}
		else
		{
			m_value = RETURN_VALUE_UNINITIALIZED;
		}
	}

	/**
	 * Returns if <CODE> getValue() </CODE> returns an other value than RETURN_VALUE_CANCEL,
	 * RETURN_VALUE_CLOSED or RETURN_VALUE_UNINITIALIZED.
	 * @return if <CODE> getValue() </CODE> returns an other value than RETURN_VALUE_CANCEL,
	 * RETURN_VALUE_CLOSED or RETURN_VALUE_UNINITIALIZED
	 */
	public final boolean hasValidValue()
	{
		return getValue() != RETURN_VALUE_CANCEL && getValue() != RETURN_VALUE_CLOSED &&
			getValue() != RETURN_VALUE_UNINITIALIZED;
	}

	/**
	 * Returns if the content pane is part of a visible dialog.
	 * @return if the content pane is part of a visible dialog
	 */
	public final boolean isVisible()
	{
		return m_currentlyActiveContentPane != null &&
			m_parentDialog.getContentPane() == m_currentlyActiveContentPane
			&& isDialogVisible();
	}

	/**
	 * Returns the parent Dialog. It is a Window or a JAPDialog.
	 * @return the parent Dialog
	 */
	public RootPaneContainer getDialog()
	{
		return m_parentDialog;
	}

	/**
	 * Returns if the parent dialog is visible. If your content pane contains a text, please do not
	 * perform a pack() operation on a visible dialog as there is a high possibility that the
	 * auto-formatting feature will not work. Packing is recommended on invisible dialogs only.
	 * @return if the parent dialog is visible
	 */
	public final boolean isDialogVisible()
	{
		return ((m_parentDialog instanceof JAPDialog && ( (JAPDialog) m_parentDialog).isVisible()) ||
				(m_parentDialog instanceof Window && ( (Window) m_parentDialog).isVisible()));
	}

	/**
	 * Adds a window listener to the parent dialog.
	 * @param a_listener a WindowListener
	 */
	public void addDialogWindowListener(WindowListener a_listener)
	{
		if (m_parentDialog instanceof Window)
		{
			((Window)m_parentDialog).addWindowListener(a_listener);
		}
		else
		{
			((JAPDialog)m_parentDialog).addWindowListener(a_listener);
		}
	}

	/**
	 * Adds a component listener. It it called in the following situations:
	 * <UL>
	 * <LI> componentShown: 1) Dialog window is opened or set to visible and the content pane is its
	 * current content pane. (Please note that with a JDialog no event is generated when the dialog is
	 * closed and made visible a second time.)
	 * 2) The Dialog window is already visible and is successfully updated with the
	 * current content pane. </LI>
	 * <LI> componentHidden: Dialog window is closed or set invisible, the content pane is set invisible
	 * while the dialog is visible, or the contentPane is shown in a JAPDialog (this is a hack to
	 * generate the componentShown event). </LI>
	 * <LI> componentResized: componentResized is called on the content pane </LI>
	 * <LI> componentMoved: componentMoved is called on the content pane </LI>
	 * </UL>
	 * The componentShown method is extremely useful if you wnat to to a specific action when the content
	 * pane is shown to the user, for example setting a focus on a special component or starting a thread.
	 * @param a_listener a ComponentListener
	 * @todo Find a way so that the componentShown method has full functionality in JDialogs; is there a way?
	 * I propose to use JAPDialog if you want this...
	 */
	public void addComponentListener(ComponentListener a_listener)
	{
		if (a_listener != null)
		{
			m_componentListeners.addElement(a_listener);
		}
	}

	/**
	 * Removes a component listener.
	 * @param a_listener a ComponentListener
	 */
	public void removeComponentListener(ComponentListener a_listener)
	{
		m_componentListeners.removeElement(a_listener);
	}

	/**
	 * Adds a component listener to the parent dialog.
	 * @param a_listener a ComponentListener
	 */
	public void addDialogComponentListener(ComponentListener a_listener)
	{
		if (m_parentDialog instanceof Window)
		{
			((Window)m_parentDialog).addComponentListener(a_listener);
		}
		else
		{
			((JAPDialog)m_parentDialog).addComponentListener(a_listener);
		}
	}

	/**
	 * Removes a component listener from the parent dialog.
	 * @param a_listener a ComponentListener
	 */
	public void removeDialogComponentListener(ComponentListener a_listener)
	{
		if (m_parentDialog instanceof Window)
		{
			((Window)m_parentDialog).removeComponentListener(a_listener);
		}
		else
		{
			((JAPDialog)m_parentDialog).removeComponentListener(a_listener);
		}
	}


	/**
	 * Removes a window listener from the parent dialog.
	 * @param a_listener a WindowListener
	 */
	public void removeDialogWindowListener(WindowListener a_listener)
	{
		if (m_parentDialog instanceof Window)
		{
			((Window)m_parentDialog).removeWindowListener(a_listener);
		}
		else
		{
			((JAPDialog)m_parentDialog).removeWindowListener(a_listener);
		}
	}

	/**
	 * Hides or disposed the parent dialog.
	 * @param a_bDispose if true, the dialog is disposed; otherwise, it is only hidden
	 */
	public final void closeDialog(boolean a_bDispose)
	{
		if (a_bDispose)
		{
			if (m_parentDialog instanceof Window)
			{
				((Window)m_parentDialog).dispose();
			}
			else
			{
				try
				{
					( (JAPDialog) m_parentDialog).dispose();
				}
				catch (IllegalMonitorStateException a_e)
				{
					LogHolder.log(LogLevel.DEBUG, LogType.GUI, a_e);
				}
			}
		}
		else
		{
			if (m_parentDialog instanceof Window)
			{
				((Window)m_parentDialog).setVisible(false);
			}
			else
			{
				((JAPDialog)m_parentDialog).setVisible(false);
			}
		}
	}


	private final void printStatusMessageInternal(String a_strMessage, int a_messageType)
	{
		String strMessage;
		Dimension  newSize;
		String strColor;
		String strHref;
		int length;

		// no HTML Tags are allowed in the message
		strMessage = JAPHtmlMultiLineLabel.removeTagsAndNewLines(a_strMessage);

		if (MESSAGE_TYPE_ERROR == a_messageType || MESSAGE_TYPE_WARNING == a_messageType)
		{
			strColor = "red";
		}
		else
		{
			strColor = "black";
		}

		// calculate the width of the label
		newSize = calculateMessageSize(strMessage);

		if (newSize.width > m_lblMessage.getSize().width)
		{
			String strMessageTitle;
			if (MESSAGE_TYPE_ERROR == a_messageType)
			{
				strMessageTitle = JAPMessages.getString(JAPDialog.MSG_TITLE_ERROR);
			}
			else if (MESSAGE_TYPE_WARNING == a_messageType)
			{
				strMessageTitle = JAPMessages.getString(JAPDialog.MSG_TITLE_WARNING);
			}
			else
			{
				strMessageTitle = JAPMessages.getString(JAPDialog.MSG_TITLE_INFO);
			}

			clearStatusMessage();
			length = Math.min(strMessage.length(), MIN_TEXT_LENGTH);
			strMessage = strMessage.substring(0, length) + "...";
			strHref =  " href=\"\"";
			m_lblMessage.setToolTipText(JAPMessages.getString(MSG_SEE_FULL_MESSAGE));
			m_linkedDialog = new LinkedDialog(a_strMessage, strMessageTitle,
											  OPTION_TYPE_DEFAULT, a_messageType);
			m_lblMessage.addMouseListener(m_linkedDialog);
		}
		else
		{
			clearStatusMessage();
			strHref = "";
		}
		strMessage = "<A style=\"color:" + strColor + "\"" + strHref + "> " + strMessage + " </A>";

		m_lblMessage.setText(strMessage);
		m_lblMessage.revalidate();
	}

	private class LinkedDialog extends MouseAdapter
	{
		private String m_strMessage;
		private String m_strTitle;
		private int m_optionType;
		private int m_messageType;

		public LinkedDialog(String a_strMessage, String a_strTitle, int a_optionType, int a_messageType)
		{
			m_strMessage = a_strMessage;
			m_strTitle = a_strTitle;
			m_optionType = a_optionType;
			m_messageType = a_messageType;
		}

		public void mouseClicked(MouseEvent a_event)
		{
			JAPDialog.showConfirmDialog(m_lblMessage, m_strMessage, m_strTitle, m_optionType, m_messageType);
		}
	}

	private static Dimension calculateMessageSize(String a_strMessage)
	{
		JAPHtmlMultiLineLabel label = new JAPHtmlMultiLineLabel(a_strMessage);
		label.setFontStyle(JAPHtmlMultiLineLabel.FONT_STYLE_PLAIN);
		JFrame frame = new JFrame();
		frame.getContentPane().add(label);
		frame.pack();
		return label.getSize();
	}

	private void setNextContentPane(DialogContentPane a_nextContentPane)
	{
		if (m_nextContentPane != null)
		{
			// remove the next content pane from the chain
			m_nextContentPane.m_previousContentPane = null;
		}
		m_nextContentPane = a_nextContentPane;
	}

	/**
	 * Shows a given content pane in the dialog if it exists. Otherwise, the dialog is closed according
	 * to the default ON_CLICK operation. If no ON_CLICK operation is set, nothing is done by default.
	 * If the content pane exists, its checkUpdate() method is interpreted and the errors are handled.
	 * @param a_bNext true if a move to the next content pane is done; false if a move to
	 * the previous content pane is done
	 * @return if a move to the given content pane was done; false if no previous content pane does exist
	 * or if it refused to update the dialog
	 */
	private boolean moveToContentPane(boolean a_bNext)
	{
		DialogContentPane currentContentPane;

		for (int i = m_rememberedErrors.size() - 1; i >= 0; i--)
		{
			( (CheckError) m_rememberedUpdateErrors.elementAt(i)).undoErrorAction();
			m_rememberedUpdateErrors.removeElementAt(i);
		}

		currentContentPane = this;
		if (a_bNext)
		{
			while ((currentContentPane = currentContentPane.getNextContentPane()) != null &&
				   currentContentPane.isSkippedAsNextContentPane());
		}
		else
		{
			while ((currentContentPane = currentContentPane.getPreviousContentPane()) != null &&
				   currentContentPane.isSkippedAsPreviousContentPane());
		}

		if (currentContentPane != null)
		{
			CheckError[] error = currentContentPane.updateDialog();
			boolean bFocused = false;

			if (error == null || error.length == 0)
			{
				if (currentContentPane.isVisible())
				{
					if (a_bNext)
					{
						if (currentContentPane.getButtonYesOK() != null &&
							currentContentPane.getButtonYesOK().isEnabled())
						{
							currentContentPane.getButtonYesOK().requestFocus();
							getDialog().getRootPane().setDefaultButton(currentContentPane.getButtonYesOK());
							bFocused = true;
						}
					}
					else
					{
						if (currentContentPane.getButtonNo() != null &&
							currentContentPane.getButtonNo().isEnabled())
						{
							currentContentPane.getButtonNo().requestFocus();
							getDialog().getRootPane().setDefaultButton(currentContentPane.getButtonNo());
							bFocused = true;
						}
						else if (currentContentPane.getButtonYesOK() != null &&
								 currentContentPane.getButtonYesOK().isEnabled())
						{
							currentContentPane.getButtonYesOK().requestFocus();
							getDialog().getRootPane().setDefaultButton(currentContentPane.getButtonYesOK());
							bFocused = true;
						}
					}
					if (!bFocused)
					{
						if (currentContentPane.getButtonCancel() != null &&
							currentContentPane.getButtonCancel().isEnabled())
						{
							currentContentPane.getButtonCancel().requestFocus();
							getDialog().getRootPane().setDefaultButton(currentContentPane.getButtonCancel());
						}
					}
				}
				return true;
			}
			else
			{
				printStatusMessage(error[0].getMessage(), MESSAGE_TYPE_ERROR);
				for (int i = 0; i < error.length; i++)
				{
					m_rememberedUpdateErrors.addElement(error[i]);
					error[i].doErrorAction();
				}
				return false;
			}
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


	private void createDefaultOptions()
	{
		m_panelOptions = new JPanel();
		if (OPTION_TYPE_YES_NO_CANCEL == m_optionType || OPTION_TYPE_OK_CANCEL == m_optionType ||
			OPTION_TYPE_CANCEL == m_optionType)
		{
			if (m_btnCancel == null)
			{
				m_btnCancel = new JButton();
				m_btnCancel.addActionListener(m_buttonListener);
			}
			m_btnCancel.setText(JAPMessages.getString(MSG_CANCEL));
			// the cancel button is always the first one if present
			m_panelOptions.add(m_btnCancel);
		}

		if (OPTION_TYPE_YES_NO == m_optionType || OPTION_TYPE_YES_NO_CANCEL == m_optionType)
		{
			// Button No
			if (m_btnNo == null)
			{
				m_btnNo = new JButton();
				m_btnNo.addActionListener(m_buttonListener);
			}
			m_btnNo.setText(JAPMessages.getString(MSG_NO));
			m_panelOptions.add(m_btnNo);


			// Button Yes/OK
			if (m_btnYesOK == null)
			{
				m_btnYesOK = new JButton();
				m_btnYesOK.addActionListener(m_buttonListener);
			}
			m_btnYesOK.setText(JAPMessages.getString(MSG_YES));
			m_panelOptions.add(m_btnYesOK);
		}
		else if (OPTION_TYPE_OK_CANCEL == m_optionType || OPTION_TYPE_DEFAULT == m_optionType)
		{
			if (m_btnYesOK == null)
			{
				m_btnYesOK = new JButton();
				m_btnYesOK.addActionListener(m_buttonListener);
			}
			m_btnYesOK.setText(JAPMessages.getString(MSG_OK));
			m_panelOptions.add(m_btnYesOK);
		}

		if (getHelpContext() != null)
		{
			m_btnHelp = JAPHelp.createHelpButton(this);
			m_panelOptions.add(m_btnHelp);
		}
	}

	private void createWizardOptions()
	{
		m_panelOptions = Box.createHorizontalBox();

		if (m_btnCancel == null)
		{
			m_btnCancel = new JButton();
			m_btnCancel.addActionListener(m_buttonListener);
		}
		m_btnCancel.setText(JAPMessages.getString(MSG_CANCEL));
		m_panelOptions.add(m_btnCancel);

		m_panelOptions.add(Box.createHorizontalGlue());
		if (getPreviousContentPane() != null)
		{
			if (m_btnNo == null)
			{
				m_btnNo = new JButton();
				m_btnNo.addActionListener(m_buttonListener);
			}
			m_btnNo.setText(JAPMessages.getString(MSG_PREVIOUS));
			m_panelOptions.add(m_btnNo);
		}
		if (m_btnYesOK == null)
		{
			m_btnYesOK = new JButton();
			m_btnYesOK.addActionListener(m_buttonListener);
		}
		if (getNextContentPane() != null)
		{
			m_btnYesOK.setText(JAPMessages.getString(MSG_NEXT));
		}
		else
		{
			m_btnYesOK.setText(JAPMessages.getString(MSG_FINISH));
		}
		m_panelOptions.add(m_btnYesOK);

		if (getHelpContext() != null)
		{
			if ( m_btnHelp == null)
			{
				m_btnHelp = JAPHelp.createHelpButton(this);
			}
			m_panelOptions.add(m_btnHelp);
		}
}

	private void createOptions()
	{
		boolean bHasWizardLayout = hasWizardLayout();

		if (m_panelOptions != null && !bHasWizardLayout && !(m_bHasHadWizardLayout && !bHasWizardLayout))
		{
			// no need to change the option buttons
			return;
		}
		m_bHasHadWizardLayout = bHasWizardLayout;

		if (m_buttonListener == null)
		{
			m_buttonListener = new ButtonListener();
		}

		if (bHasWizardLayout)
		{
			createWizardOptions();
		}
		else
		{
			createDefaultOptions();
		}
	}

	private class DialogWindowListener extends WindowAdapter
	{
		public void windowClosed(WindowEvent a_event)
		{
			if (getValue() == RETURN_VALUE_UNINITIALIZED)
			{
				setValue(RETURN_VALUE_CLOSED);
			}

			ComponentListener listener = m_currentlyActiveContentPaneComponentListener;
			if (listener != null)
			{
				listener.componentHidden(new ComponentEvent(
								m_currentlyActiveContentPane, ComponentEvent.COMPONENT_HIDDEN));
			}
		}
		public void windowOpened(WindowEvent a_event)
		{
			if (isVisible() && hasWizardLayout() && getButtonYesOK() != null)
			{
				getButtonYesOK().requestFocus();
				getDialog().getRootPane().setDefaultButton(getButtonYesOK());
			}

			if (getDialog() instanceof Window)
			{
				/**
				 * This is a patch for JDialog; componentShown is at least called on the first opening.
				 * Alas, it is not possible to generate this event when the dialog is closed and
				 * opened a second time.
				 */
				ComponentListener listener = m_currentlyActiveContentPaneComponentListener;
				if (listener != null)
				{
					listener.componentShown(new ComponentEvent(
						m_currentlyActiveContentPane, ComponentEvent.COMPONENT_SHOWN));
				}
			}
		}
	}

	private class DialogComponentListener extends ComponentAdapter
	{
		public void componentHidden(ComponentEvent a_event)
		{
			if (m_value == RETURN_VALUE_UNINITIALIZED)
			{
				m_value = RETURN_VALUE_CLOSED;
			}

			ComponentListener listener = m_currentlyActiveContentPaneComponentListener;
			if (listener != null)
			{
				listener.componentHidden(new ComponentEvent(
								m_currentlyActiveContentPane, ComponentEvent.COMPONENT_HIDDEN));
			}
		}

		public void componentShown(ComponentEvent a_event)
		{
			// does not work for JDialog in old JDKs (e.g. 1.1.8) and is therefore not used
		}
	}

	private class ContentPaneComponentListener extends ComponentAdapter
	{
		public void componentHidden(ComponentEvent a_event)
		{
			Vector listeners = (Vector)m_componentListeners.clone();
			for (int i = 0; i < listeners.size(); i++)
			{
				( (ComponentListener) listeners.elementAt(i)).componentHidden(a_event);
			}
		}

		public void componentShown(ComponentEvent a_event)
		{
			if (isVisible())
			{
				if (m_lblText != null)
				{
					// enable automatic resizing
					m_titlePane.remove(m_lblText);
					m_lblText = new JAPHtmlMultiLineLabel(m_lblText.getText(), m_lblText.getFont(),
						SwingConstants.CENTER);
					m_titlePane.add(m_lblText, m_constraints);
					m_titlePane.revalidate();
				}

				Vector listeners = (Vector)m_componentListeners.clone();
				for (int i = 0; i < listeners.size(); i++)
				{
					( (ComponentListener) listeners.elementAt(i)).componentShown(a_event);
				}
			}
		}

		public void componentResized(ComponentEvent a_event)
		{
			Vector listeners = (Vector)m_componentListeners.clone();
			for (int i = 0; i < listeners.size(); i++)
			{
				( (ComponentListener) listeners.elementAt(i)).componentResized(a_event);
			}
		}

		public void componentMoved(ComponentEvent a_event)
		{
			Vector listeners = (Vector)m_componentListeners.clone();
			for (int i = 0; i < listeners.size(); i++)
			{
				( (ComponentListener) listeners.elementAt(i)).componentMoved(a_event);
			}
		}
	}

	private class ButtonListener implements ActionListener
	{
		public void actionPerformed(ActionEvent a_event)
		{
			boolean bActionDone = false;
			CheckError[] errors;

			if (a_event == null || a_event.getSource() == null)
			{
				return;
			}

			if (a_event.getSource() == m_btnCancel)
			{
				errors = checkCancel();
				if (isSomethingDoneOnClick(errors,
										   ON_CANCEL_SHOW_NEXT_CONTENT, ON_CANCEL_SHOW_PREVIOUS_CONTENT,
										   ON_CANCEL_HIDE_DIALOG, ON_CANCEL_DISPOSE_DIALOG))
				{
					m_value = RETURN_VALUE_CANCEL;
				}
				bActionDone = doDefaultButtonOperation(errors, ON_CANCEL_SHOW_NEXT_CONTENT,
													   ON_CANCEL_SHOW_PREVIOUS_CONTENT,
													   ON_CANCEL_HIDE_DIALOG, ON_CANCEL_DISPOSE_DIALOG);
			}
			else if (a_event.getSource() == m_btnYesOK)
			{
				errors = checkYesOK();
				if (isSomethingDoneOnClick(errors, ON_YESOK_SHOW_NEXT_CONTENT, ON_YESOK_SHOW_PREVIOUS_CONTENT,
										   ON_YESOK_HIDE_DIALOG, ON_YESOK_DISPOSE_DIALOG))
				{
					if (OPTION_TYPE_YES_NO == m_optionType || OPTION_TYPE_YES_NO_CANCEL == m_optionType)
					{
						m_value = RETURN_VALUE_YES;
					}
					else
					{
						m_value = RETURN_VALUE_OK;
					}
				}
				bActionDone = doDefaultButtonOperation(errors, ON_YESOK_SHOW_NEXT_CONTENT,
					ON_YESOK_SHOW_PREVIOUS_CONTENT, ON_YESOK_HIDE_DIALOG, ON_YESOK_DISPOSE_DIALOG);
			}
			else //if (a_event.getSource() == m_btnNo)
			{
				errors = checkNo();
				if (isSomethingDoneOnClick(errors, ON_NO_SHOW_NEXT_CONTENT, ON_NO_SHOW_PREVIOUS_CONTENT,
										   ON_NO_HIDE_DIALOG, ON_NO_DISPOSE_DIALOG))
				{
					m_value = RETURN_VALUE_NO;
				}
				bActionDone = doDefaultButtonOperation(errors,
					ON_NO_SHOW_NEXT_CONTENT, ON_NO_SHOW_PREVIOUS_CONTENT,
					ON_NO_HIDE_DIALOG, ON_NO_DISPOSE_DIALOG);
			}

			if (!bActionDone && (errors == null || errors.length == 0))
			{
				doDefaultButtonOperation(errors, ON_CLICK_SHOW_NEXT_CONTENT, ON_CLICK_SHOW_PREVIOUS_CONTENT,
										 ON_CLICK_HIDE_DIALOG, ON_CLICK_DISPOSE_DIALOG);
			}
		}
	}

	/**
	 * Returns true if the click on a specific button will do an automatic action.
	 * @param a_errors CheckError[]
	 * @param a_opNext int
	 * @param a_opPrevious int
	 * @param a_opHide int
	 * @param a_opDispose int
	 * @return true if the click on a specific button will do an automatic action; false otherwise
	 */
	private boolean isSomethingDoneOnClick(CheckError[] a_errors,
										   int a_opNext, int a_opPrevious, int a_opHide, int a_opDispose)
	{
		return (a_errors == null || a_errors.length == 0) && ((getDefaultButtonOperation() & (
			  ON_CLICK_HIDE_DIALOG | ON_CLICK_DISPOSE_DIALOG | ON_CLICK_SHOW_NEXT_CONTENT |
			  ON_CLICK_SHOW_PREVIOUS_CONTENT |
			  a_opNext | a_opPrevious | a_opHide | a_opDispose)) > 0);
	}

	private boolean doDefaultButtonOperation(CheckError[] a_errors,
											 int a_opNext, int a_opPrevious, int a_opHide, int a_opDispose)
	{
		for (int i = m_rememberedErrors.size() - 1; i >= 0; i--)
		{
			((CheckError)m_rememberedErrors.elementAt(i)).undoErrorAction();
			m_rememberedErrors.removeElementAt(i);
		}

		// check if there are any errors that prohibit to continue
		if (a_errors != null && a_errors.length > 0)
		{
			String errorMessage = null;

			for (int i = 0; i < a_errors.length; i++)
			{
				if (a_errors[i] == null)
				{
					LogHolder.log(LogLevel.ERR, LogType.GUI, "Found a " + CheckError.class.getName() + " " +
								  "that is null! Ignoring it.");
					continue;
				}

				if (a_errors[i].getMessage() != null &&
					(errorMessage == null || (errorMessage != null && errorMessage.trim().length() == 0)))
				{
					errorMessage = a_errors[i].getMessage();
				}
				a_errors[i].doErrorAction();
				m_rememberedErrors.addElement(a_errors[i]);
			}
			if (errorMessage == null)
			{
				errorMessage = JAPMessages.getString(MSG_OPERATION_FAILED);
			}
			if (errorMessage.trim().length() > 0)
			{
				printStatusMessage(errorMessage, MESSAGE_TYPE_ERROR);
			}
			return false;
		}

		if (m_nextContentPane != null && (getDefaultButtonOperation() & a_opNext) > 0)
		{

			return moveToNextContentPane();
		}

		if (m_previousContentPane != null && (getDefaultButtonOperation() & a_opPrevious) > 0)
		{
			return moveToPreviousContentPane();
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