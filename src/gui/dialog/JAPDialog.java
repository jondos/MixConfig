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

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import java.util.EventListener;
import java.util.Vector;

import java.awt.MenuContainer;
import java.awt.MenuComponent;
import java.awt.Font;
import java.awt.Image;
import java.awt.image.ImageObserver;
import java.awt.Event;
import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.MenuComponent;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.ComponentListener;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLayeredPane;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextPane;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants.CharacterConstants;

import logging.LogHolder;
import logging.LogLevel;
import logging.LogType;

import gui.*;

/**
 * This is the generic implementation for an optionally modal, resizable a dialog. Use the root pane and
 * content pane (getRootPane() and getContentPane() methods) for customization.
 * <BR>
 * The customizable dialogs show the same behaviour as the standard JDialog, except for the modality
 * attribute:
 * Modal JAPDialogs are only modal for their parent window and the parent of its parent and so on, but not
 * for other Dialogs or Windows. This allows for example to access a non-modal help window at the time a
 * modal dialog is displayed.
 * <BR>
 * This class is also a replacement for JOptionPane: it allows for the same type of dialogs, and
 * even has the same syntax (save that it only accepts String messages), but with additional features.
 * JAPDialog option panes
 * <UL>
 * <LI> are auto-formatted in the golden ratio if possible by a quick heuristic </LI>
 * <LI> interpret the text message as HTML, without adding html or body tags </LI>
 * <LI> may get an HTML link that triggers an arbitrary event, for example show a help window on clicking
 *      (interface ILinkedInformation) </LI>
 * </UL>
 * These features take the need to put newlines or HTML breaks into the message text to format the dialog.
 * This is done fully automatically. Also, dialog texts may get smaller, without ignoring important
 * information. This information may be stored behind the optional dialog link. For displaying a simple
 * link to a JAPHelp window, for example, there is a class named LinkedHelpContext. Its implementation
 * should cover most needs.
 *
 * <P> Warning: This is a really complex class handling many bugs and differences in different JDKs.
 * If you change something here, be sure you know what you are doing and test the class at least with
 * the following JDKs: </P>
 *
 * <UL>
 * <LI> Microsoft JView </LI>
 * <LI> 1.1.8 </LI>
 * <LI> 1.2.2 </LI>
 * <LI> 1.3.x </LI>
 * <LI> 1.4.x </LI>
 * <LI> 1.5.x </LI>
 * </UL>

 *
 * @see javax.swing.JDialog
 * @see javax.swing.JOptionPane
 * @see gui.dialog.DialogContentPane
 * @see ILinkedInformation
 *
 * @author Rolf Wendolsky
 */
public class JAPDialog implements Accessible, WindowConstants, RootPaneContainer, MenuContainer,
	ImageObserver, IDialogOptions
{
	public static final double GOLDEN_RATIO_PHI = (1.0 + Math.sqrt(5.0)) / 2.0;

	public static final String MSG_ERROR_UNKNOWN = JAPDialog.class.getName() + "_errorUnknown";
	public static final String MSG_TITLE_INFO = JAPDialog.class.getName() + "_titleInfo";
	public static final String MSG_TITLE_CONFIRMATION = JAPDialog.class.getName() + "_titleConfirmation";
	public static final String MSG_TITLE_WARNING = JAPDialog.class.getName() + "_titleWarning";
	public static final String MSG_TITLE_ERROR = JAPDialog.class.getName() + "_titleError";
	public static final String MSG_ERROR_UNDISPLAYABLE = JAPDialog.class.getName() + "_errorUndisplayable";

	private static final int NUMBER_OF_HEURISTIC_ITERATIONS = 5;

	private boolean m_bLocationSetManually = false;
	private boolean m_bModal;
	private boolean m_bBlockParentWindow = false;
	private int m_defaultCloseOperation;
	private Vector m_windowListeners = new Vector();

	/**
	 * Stores the instance of JDialog for internal use.
	 */
	private JDialog m_internalDialog;

	/**
	 * This stores the parent component of this dialog.
	 */
	private Component m_parentComponent;

	/**
	 * This stores the parent window of this dialog.
	 */
	private Window m_parentWindow;

	/**
	 * Creates a new instance of JAPDialog. It is user-resizable.
	 * @param a_parentComponent The parent component for this dialog. If it is null or the parent
	 *                          component is not within a frame, the dialog's parent frame is the
	 *                          default frame.
	 * @param a_strTitle The title String for this dialog.
	 * @param a_bModal if the dialog should be modal
	 */
	public JAPDialog(Component a_parentComponent, String a_strTitle, boolean a_bModal)
	{
		JOptionPane optionPane = new JOptionPane();
		m_internalDialog = optionPane.createDialog(a_parentComponent, a_strTitle);
		m_internalDialog.getContentPane().removeAll();
		m_internalDialog.setResizable(true);
		setDefaultCloseOperation(m_internalDialog.getDefaultCloseOperation());
		init(m_internalDialog, a_bModal, a_parentComponent);
	}

	/**
	 * Creates a new instance of JAPDialog. It is user-resizable and modal.
	 * @param a_parentComponent The parent component for this dialog. If it is null or the parent
	 *                          component is not within a frame, the dialog's parent frame is the
	 *                          default frame.
	 * @param a_strTitle The title String for this dialog.
	 */
	public JAPDialog(Component a_parentComponent, String a_strTitle)
	{
		this(a_parentComponent, a_strTitle, true);
	}

	/**
	 * Creates a new instance of JAPDialog. It is user-resizable.
	 * @param a_parentDialog The parent dialog for this dialog. If it is null,
	 *                       the dialog's parent frame is the default frame.
	 * @param a_strTitle The title String for this dialog.
	 * @param a_bModal if the dialog should be modal
	 */
	public JAPDialog(JAPDialog a_parentDialog, String a_strTitle, boolean a_bModal)
	{
		this(getInternalDialog(a_parentDialog), a_strTitle, a_bModal);
	}

	/**
	 * Creates a new instance of JAPDialog. It is user-resizable.
	 * @param a_parentDialog The parent dialog for this dialog. If it is null,
	 *                       the dialog's parent frame is the default frame.
	 * @param a_strTitle The title String for this dialog.
	 */
	public JAPDialog(JAPDialog a_parentDialog, String a_strTitle)
	{
		this(getInternalDialog(a_parentDialog), a_strTitle);
	}

	private void init(JDialog a_dialog, boolean a_bModal, Component a_parentComponent)
	{
		EventListener[] listeners;

		m_parentComponent = a_parentComponent;
		m_internalDialog = a_dialog;
		m_internalDialog.setModal(false);
		m_internalDialog.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		/* Old JDKs ignore the default closing operation, therefore it is tried to remove the window listener.
		 * This removes the flimmering effect that occurs when the internal dialog is closed before enabling
		 * the parent window.
		 */
		try
		{
			//listeners = m_internalDialog.getListeners(WindowListener.class);
			listeners = (EventListener[]) JDialog.class.getMethod(
						 "getListeners", new Class[]
						 {Class.class}).invoke(m_internalDialog, new Object[]{WindowListener.class});
		}
		catch (Exception a_e)
		{
			// method is only available in JDKs >= 1.3
			listeners = null;
		}
		for (int i = 0; listeners != null && i < listeners.length; i++)
		{
			m_internalDialog.removeWindowListener((WindowListener)listeners[i]);
		}

		addWindowListener(new WindowClosingAdapter(this));
		m_parentWindow = getParentWindow(getParentComponent());
		setModal(a_bModal);
	}

	/**
	 * Calculates the difference from a window's size and the golden ratio.
	 * @param a_window a Window
	 * @return the difference from a window's size and the golden ratio
	 */
	public static double getGoldenRatioDelta(Window a_window)
	{
		return a_window.getSize().height * GOLDEN_RATIO_PHI - a_window.getSize().width;
	}

	/**
	 * Calculates the difference from a JAPDialog's size and the golden ratio.
	 * @param a_dialog a JAPDialog
	 * @return the difference from a JAPDialog's size and the golden ratio
	 */
	public static double getGoldenRatioDelta(JAPDialog a_dialog)
	{
		return a_dialog.getSize().height * GOLDEN_RATIO_PHI - a_dialog.getSize().width;
	}

	/**
	 * Displays an info message dialog. Words are wrapped automatically if a message line is too long.
	 * @param a_parentDialog The parent dialog for this dialog. If it is null,
	 *                       the dialog's parent frame is the default frame.
	 * @param a_message The message to be displayed. It is interpreted as HTML. You do not need to put in
	 * formatting tags, as the text will be auto-formatted in a way that the dialog's size is very close
	 * to the golden ratio.
	 */
	public static void showMessageDialog(JAPDialog a_parentDialog, String a_message)
	{
		showMessageDialog(getInternalDialog(a_parentDialog), a_message);
	}

	/**
	 * Displays an info message dialog. Words are wrapped automatically if a message line is too long.
	 * @param a_parentDialog The parent dialog for this dialog. If it is null,
	 *                       the dialog's parent frame is the default frame.
	 * @param a_message The message to be displayed. It is interpreted as HTML. You do not need to put in
	 * formatting tags, as the text will be auto-formatted in a way that the dialog's size is very close
	 * to the golden ratio.
	 * @param a_linkedInformation a clickable information message that is appended to the text
	 */
	public static void showMessageDialog(JAPDialog a_parentDialog, String a_message,
										 ILinkedInformation a_linkedInformation)
	{
		showMessageDialog(getInternalDialog(a_parentDialog), a_message, a_linkedInformation);
	}

	/**
	 * Displays an info message dialog. Words are wrapped automatically if a message line is too long.
	 * @param a_parentComponent The parent component for this dialog. If it is null or the parent
	 *                          component is not within a frame, the dialog's parent frame is the
	 *                          default frame.
	 * @param a_message The message to be displayed. It is interpreted as HTML. You do not need to put in
	 * formatting tags, as the text will be auto-formatted in a way that the dialog's size is very close
	 * to the golden ratio.
	 */
	public static void showMessageDialog(Component a_parentComponent, String a_message)
	{
		showMessageDialog(a_parentComponent, a_message, JAPMessages.getString(MSG_TITLE_INFO), (Icon)null);
	}

	/**
	 * Displays an info message dialog. Words are wrapped automatically if a message line is too long.
	 * @param a_parentComponent The parent component for this dialog. If it is null or the parent
	 *                          component is not within a frame, the dialog's parent frame is the
	 *                          default frame.
	 * @param a_message The message to be displayed. It is interpreted as HTML. You do not need to put in
	 * formatting tags, as the text will be auto-formatted in a way that the dialog's size is very close
	 * to the golden ratio.
	 * @param a_linkedInformation a clickable information message that is appended to the text
	 */
	public static void showMessageDialog(Component a_parentComponent, String a_message,
										 ILinkedInformation a_linkedInformation)
	{
		showMessageDialog(a_parentComponent, a_message, JAPMessages.getString(MSG_TITLE_INFO), (Icon)null,
					   a_linkedInformation);
	}

	/**
	 * Displays an info message dialog. Words are wrapped automatically if a message line is too long.
	 * @param a_parentDialog The parent dialog for this dialog. If it is null,
	 *                       the dialog's parent frame is the default frame.
	 * @param a_title The title of the message dialog
	 * @param a_message The message to be displayed. It is interpreted as HTML. You do not need to put in
	 * formatting tags, as the text will be auto-formatted in a way that the dialog's size is very close
	 * to the golden ratio.
	 */
	public static void showMessageDialog(JAPDialog a_parentDialog, String a_message, String a_title)
	{
		showMessageDialog(getInternalDialog(a_parentDialog), a_message, a_title);
	}

	/**
	 * Displays an info message dialog. Words are wrapped automatically if a message line is too long.
	 * @param a_parentDialog The parent dialog for this dialog. If it is null,
	 *                       the dialog's parent frame is the default frame.
	 * @param a_title The title of the message dialog
	 * @param a_message The message to be displayed. It is interpreted as HTML. You do not need to put in
	 * formatting tags, as the text will be auto-formatted in a way that the dialog's size is very close
	 * to the golden ratio.
	 * @param a_linkedInformation a clickable information message that is appended to the text
	 */
	public static void showMessageDialog(JAPDialog a_parentDialog, String a_message, String a_title,
										 ILinkedInformation a_linkedInformation)
	{
		showMessageDialog(getInternalDialog(a_parentDialog), a_message, a_title, a_linkedInformation);
	}

	/**
	 * Displays an info message dialog. Words are wrapped automatically if a message line is too long.
	 * @param a_parentComponent The parent component for this dialog. If it is null or the parent
	 *                          component is not within a frame, the dialog's parent frame is the
	 *                          default frame.
	 * @param a_title The title of the message dialog
	 * @param a_message The message to be displayed. It is interpreted as HTML. You do not need to put in
	 * formatting tags, as the text will be auto-formatted in a way that the dialog's size is very close
	 * to the golden ratio.
	 */
	public static void showMessageDialog(Component a_parentComponent, String a_message, String a_title)
	{
		showMessageDialog(a_parentComponent, a_message, a_title, (Icon)null);
	}

	/**
	 * Displays an info message dialog. Words are wrapped automatically if a message line is too long.
	 * @param a_parentComponent The parent component for this dialog. If it is null or the parent
	 *                          component is not within a frame, the dialog's parent frame is the
	 *                          default frame.
	 * @param a_title The title of the message dialog
	 * @param a_message The message to be displayed. It is interpreted as HTML. You do not need to put in
	 * formatting tags, as the text will be auto-formatted in a way that the dialog's size is very close
	 * to the golden ratio.
	 * @param a_linkedInformation a clickable information message that is appended to the text
	 */
	public static void showMessageDialog(Component a_parentComponent, String a_message, String a_title,
										 ILinkedInformation a_linkedInformation)
	{
		showMessageDialog(a_parentComponent, a_message, a_title, null, a_linkedInformation);
	}

	/**
	 * Displays an info message dialog. Words are wrapped automatically if a message line is too long.
	 * @param a_parentDialog The parent dialog for this dialog. If it is null,
	 *                       the dialog's parent frame is the default frame.
	 * @param a_message The message to be displayed. It is interpreted as HTML. You do not need to put in
	 * formatting tags, as the text will be auto-formatted in a way that the dialog's size is very close
	 * to the golden ratio.
	 * @param a_icon an icon that will be displayed on the dialog
	 */
	public static void showMessageDialog(JAPDialog a_parentDialog, String a_message, Icon a_icon)
	{
		showMessageDialog(getInternalDialog(a_parentDialog), a_message, a_icon);
	}

	/**
	 * Displays an info message dialog. Words are wrapped automatically if a message line is too long.
	 * @param a_parentDialog The parent dialog for this dialog. If it is null,
	 *                       the dialog's parent frame is the default frame.
	 * @param a_message The message to be displayed. It is interpreted as HTML. You do not need to put in
	 * formatting tags, as the text will be auto-formatted in a way that the dialog's size is very close
	 * to the golden ratio.
	 * @param a_icon an icon that will be displayed on the dialog
	 * @param a_linkedInformation a clickable information message that is appended to the text
	 */
	public static void showMessageDialog(JAPDialog a_parentDialog, String a_message, Icon a_icon,
										 ILinkedInformation a_linkedInformation)
	{
		showMessageDialog(getInternalDialog(a_parentDialog), a_message, a_icon, a_linkedInformation);
	}

	/**
	 * Displays an info message dialog. Words are wrapped automatically if a message line is too long.
	 * @param a_parentComponent The parent component for this dialog. If it is null or the parent
	 *                          component is not within a frame, the dialog's parent frame is the
	 *                          default frame.
	 * @param a_message The message to be displayed. It is interpreted as HTML. You do not need to put in
	 * formatting tags, as the text will be auto-formatted in a way that the dialog's size is very close
	 * to the golden ratio.
	 * @param a_icon an icon that will be displayed on the dialog
	 */
	public static void showMessageDialog(Component a_parentComponent, String a_message, Icon a_icon)
	{
		showMessageDialog(a_parentComponent, a_message, JAPMessages.getString(MSG_TITLE_INFO), a_icon);
	}

	/**
	 * Displays an info message dialog. Words are wrapped automatically if a message line is too long.
	 * @param a_parentComponent The parent component for this dialog. If it is null or the parent
	 *                          component is not within a frame, the dialog's parent frame is the
	 *                          default frame.
	 * @param a_message The message to be displayed. It is interpreted as HTML. You do not need to put in
	 * formatting tags, as the text will be auto-formatted in a way that the dialog's size is very close
	 * to the golden ratio.
	 * @param a_icon an icon that will be displayed on the dialog
	 * @param a_linkedInformation a clickable information message that is appended to the text
	 */
	public static void showMessageDialog(Component a_parentComponent, String a_message, Icon a_icon,
									  ILinkedInformation a_linkedInformation)
	{
		showMessageDialog(a_parentComponent, a_message, JAPMessages.getString(MSG_TITLE_INFO), a_icon,
						  a_linkedInformation);
	}

	/**
	 * Displays an info message dialog. Words are wrapped automatically if a message line is too long.
	 * @param a_parentDialog The parent dialog for this dialog. If it is null,
	 *                       the dialog's parent frame is the default frame.
	 * @param a_title The title of the message dialog
	 * @param a_message The message to be displayed. It is interpreted as HTML. You do not need to put in
	 * formatting tags, as the text will be auto-formatted in a way that the dialog's size is very close
	 * to the golden ratio.
	 * @param a_icon an icon that will be displayed on the dialog
	 */
	public static void showMessageDialog(JAPDialog a_parentDialog, String a_message, String a_title,
										 Icon a_icon)
	{
		showMessageDialog(getInternalDialog(a_parentDialog), a_message, a_title, a_icon);
	}

	/**
	 * Displays an info message dialog. Words are wrapped automatically if a message line is too long.
	 * @param a_parentDialog The parent dialog for this dialog. If it is null,
	 *                       the dialog's parent frame is the default frame.
	 * @param a_title The title of the message dialog
	 * @param a_message The message to be displayed. It is interpreted as HTML. You do not need to put in
	 * formatting tags, as the text will be auto-formatted in a way that the dialog's size is very close
	 * to the golden ratio.
	 * @param a_icon an icon that will be displayed on the dialog
	 * @param a_linkedInformation a clickable information message that is appended to the text
	 */
	public static void showMessageDialog(JAPDialog a_parentDialog, String a_message, String a_title,
										 Icon a_icon, ILinkedInformation a_linkedInformation)
	{
		showMessageDialog(getInternalDialog(a_parentDialog), a_message, a_title, a_icon, a_linkedInformation);
	}

	/**
	 * Displays an info message dialog. Words are wrapped automatically if a message line is too long.
	 * @param a_parentComponent The parent component for this dialog. If it is null or the parent
	 *                          component is not within a frame, the dialog's parent frame is the
	 *                          default frame.
	 * @param a_title The title of the message dialog
	 * @param a_message The message to be displayed. It is interpreted as HTML. You do not need to put in
	 * formatting tags, as the text will be auto-formatted in a way that the dialog's size is very close
	 * to the golden ratio.
	 * @param a_icon an icon that will be displayed on the dialog
	 */
	public static void showMessageDialog(Component a_parentComponent, String a_message, String a_title,
										 Icon a_icon)
	{
		showMessageDialog(a_parentComponent, a_message, a_title, a_icon, null);
	}

	/**
	 * Displays an info message dialog. Words are wrapped automatically if a message line is too long.
	 * @param a_parentComponent The parent component for this dialog. If it is null or the parent
	 *                          component is not within a frame, the dialog's parent frame is the
	 *                          default frame.
	 * @param a_title The title of the message dialog
	 * @param a_message The message to be displayed. It is interpreted as HTML. You do not need to put in
	 * formatting tags, as the text will be auto-formatted in a way that the dialog's size is very close
	 * to the golden ratio.
	 * @param a_icon an icon that will be displayed on the dialog
	 * @param a_linkedInformation a clickable information message that is appended to the text
	 */
	public static void showMessageDialog(Component a_parentComponent, String a_message, String a_title,
										 Icon a_icon, ILinkedInformation a_linkedInformation)
	{
		if (a_title == null)
		{
			a_title = JAPMessages.getString(MSG_TITLE_CONFIRMATION);
		}

		showConfirmDialog(a_parentComponent, a_message, a_title, OPTION_TYPE_DEFAULT,
						 MESSAGE_TYPE_INFORMATION, a_icon, a_linkedInformation);
	}

	/**
	 * Displays a warning message dialog. Words are wrapped automatically if a message line is too long.
	 * @param a_parentDialog The parent dialog for this dialog. If it is null,
	 *                       the dialog's parent frame is the default frame.
	 * @param a_message The message to be displayed. It is interpreted as HTML. You do not need to put in
	 * formatting tags, as the text will be auto-formatted in a way that the dialog's size is very close
	 * to the golden ratio.
	 */
	public static void showWarningDialog(JAPDialog a_parentDialog, String a_message)
	{
		showWarningDialog(a_parentDialog, a_message, null, null);
	}

	/**
	 * Displays a warning message dialog. Words are wrapped automatically if a message line is too long.
	 * @param a_parentComponent The parent component for this dialog. If it is null or the parent
	 *                          component is not within a frame, the dialog's parent frame is the
	 *                          default frame.
	 * @param a_message The message to be displayed. It is interpreted as HTML. You do not need to put in
	 * formatting tags, as the text will be auto-formatted in a way that the dialog's size is very close
	 * to the golden ratio.
	 */
	public static void showWarningDialog(Component a_parentComponent, String a_message)
	{
		showWarningDialog(a_parentComponent, a_message, null, null);
	}

	/**
	 * Displays a warning message dialog. Words are wrapped automatically if a message line is too long.
	 * @param a_parentDialog The parent dialog for this dialog. If it is null,
	 *                       the dialog's parent frame is the default frame.
	 * @param a_title The title of the message dialog
	 * @param a_message The message to be displayed. It is interpreted as HTML. You do not need to put in
	 * formatting tags, as the text will be auto-formatted in a way that the dialog's size is very close
	 * to the golden ratio.
	 */
	public static void showWarningDialog(JAPDialog a_parentDialog, String a_message, String a_title)
	{
		showWarningDialog(a_parentDialog, a_message, a_title, null);
	}

	/**
	 * Displays a warning message dialog. Words are wrapped automatically if a message line is too long.
	 * @param a_parentComponent The parent component for this dialog. If it is null or the parent
	 *                          component is not within a frame, the dialog's parent frame is the
	 *                          default frame.
	 * @param a_title The title of the message dialog
	 * @param a_message The message to be displayed. It is interpreted as HTML. You do not need to put in
	 * formatting tags, as the text will be auto-formatted in a way that the dialog's size is very close
	 * to the golden ratio.
	 */
	public static void showWarningDialog(Component a_parentComponent, String a_message, String a_title)
	{
		showWarningDialog(a_parentComponent, a_message, a_title, null);
	}

	/**
	 * Displays a warning message dialog. Words are wrapped automatically if a message line is too long.
	 * @param a_parentDialog The parent dialog for this dialog. If it is null,
	 *                       the dialog's parent frame is the default frame.
	 * @param a_title The title of the message dialog
	 * @param a_message The message to be displayed. It is interpreted as HTML. You do not need to put in
	 * formatting tags, as the text will be auto-formatted in a way that the dialog's size is very close
	 * to the golden ratio.
	 * @param a_linkedInformation a clickable information message that is appended to the text
	 */
	public static void showWarningDialog(JAPDialog a_parentDialog, String a_message, String a_title,
										 ILinkedInformation a_linkedInformation)
	{
		showWarningDialog(getInternalDialog(a_parentDialog), a_message, a_title, a_linkedInformation);
	}

	/**
	 * Displays a warning message dialog. Words are wrapped automatically if a message line is too long.
	 * @param a_parentComponent The parent component for this dialog. If it is null or the parent
	 *                          component is not within a frame, the dialog's parent frame is the
	 *                          default frame.
	 * @param a_title The title of the message dialog
	 * @param a_message The message to be displayed. It is interpreted as HTML. You do not need to put in
	 * formatting tags, as the text will be auto-formatted in a way that the dialog's size is very close
	 * to the golden ratio.
	 * @param a_linkedInformation a clickable information message that is appended to the text
	 */
	public static void showWarningDialog(Component a_parentComponent, String a_message, String a_title,
										 ILinkedInformation a_linkedInformation)
	{
		if (a_title == null)
		{
			a_title = JAPMessages.getString(MSG_TITLE_WARNING);
		}

		showConfirmDialog(a_parentComponent, a_message, a_title, OPTION_TYPE_DEFAULT, MESSAGE_TYPE_WARNING,
						 null, a_linkedInformation);
	}

	/**
	 * Displays a message dialog. Words are wrapped automatically if a message line is too long.
	 * @param a_parentDialog The parent dialog for this dialog. If it is null,
	 *                       the dialog's parent frame is the default frame.
	 * @param a_title The title of the message dialog
	 * @param a_message The message to be displayed. It is interpreted as HTML. You do not need to put in
	 * formatting tags, as the text will be auto-formatted in a way that the dialog's size is very close
	 * to the golden ratio.
	 * @param a_icon an icon that will be displayed on the dialog
	 * @param a_messageType use the message types from JOptionPane
	 * @param a_optionType use the option types from JOptionPane
	 * @return The value the user has selected. RETURN_VALUE_UNINITIALIZED implies
	 * the user has not yet made a choice.
	 * @see javax.swing.JOptionPane
	 */
	public static int showConfirmDialog(JAPDialog a_parentDialog, String a_message, String a_title,
									   int a_optionType, int a_messageType, Icon a_icon)
	{
		return showConfirmDialog(a_parentDialog, a_message, a_title, a_optionType, a_messageType, a_icon,
								 null);
	}

	/**
	 * Displays a message dialog. Words are wrapped automatically if a message line is too long.
	 * @param a_parentComponent The parent component for this dialog. If it is null or the parent
	 *                          component is not within a frame, the dialog's parent frame is the
	 *                          default frame.
	 * @param a_title The title of the message dialog
	 * @param a_message The message to be displayed. It is interpreted as HTML. You do not need to put in
	 * formatting tags, as the text will be auto-formatted in a way that the dialog's size is very close
	 * to the golden ratio.
	 * @param a_icon an icon that will be displayed on the dialog
	 * @param a_messageType use the message types from JOptionPane
	 * @param a_optionType use the option types from JOptionPane
	 * @return The value the user has selected. RETURN_VALUE_UNINITIALIZED implies
	 * the user has not yet made a choice.
	 * @see javax.swing.JOptionPane
	 */
	public static int showConfirmDialog(Component a_parentComponent, String a_message, String a_title,
									   int a_optionType, int a_messageType, Icon a_icon)
	{
		return showConfirmDialog(a_parentComponent, a_message, a_title, a_optionType, a_messageType, a_icon,
								null);
	}

	/**
	 * Displays a message dialog. Words are wrapped automatically if a message line is too long.
	 * @param a_parentDialog The parent dialog for this dialog. If it is null,
	 *                       the dialog's parent frame is the default frame.
	 * @param a_title The title of the message dialog
	 * @param a_message The message to be displayed. It is interpreted as HTML. You do not need to put in
	 * formatting tags, as the text will be auto-formatted in a way that the dialog's size is very close
	 * to the golden ratio.
	 * @param a_icon an icon that will be displayed on the dialog
	 * @param a_messageType use the message types from JOptionPane
	 * @param a_optionType use the option types from JOptionPane
	 * @param a_linkedInformation a clickable information message that is appended to the text
	 * @return The value the user has selected. RETURN_VALUE_UNINITIALIZED implies
	 * the user has not yet made a choice.
	 * @see javax.swing.JOptionPane
	 */
	public static int showConfirmDialog(JAPDialog a_parentDialog, String a_message, String a_title,
									   int a_optionType, int a_messageType, Icon a_icon,
									   ILinkedInformation a_linkedInformation)
	{
		return showConfirmDialog(getInternalDialog(a_parentDialog), a_message, a_title, a_optionType,
								a_messageType, a_icon, a_linkedInformation);
	}

	/**
	 * Displays a confirm dialog. Words are wrapped automatically if a message line is too long.
	 * This method is the 'hear' of the show...Dialog() logic.
	 * @param a_parentComponent The parent component for this dialog. If it is null or the parent
	 *                          component is not within a frame, the dialog's parent frame is the
	 *                          default frame.
	 * @param a_title The title of the message dialog
	 * @param a_message The message to be displayed. It is interpreted as HTML. You do not need to put in
	 * formatting tags, as the text will be auto-formatted in a way that the dialog's size is very close
	 * to the golden ratio.
	 * @param a_icon an icon that will be displayed on the dialog
	 * @param a_messageType use the message types from JOptionPane
	 * @param a_optionType use the option types from JOptionPane
	 * @param a_linkedInformation a clickable information message that is appended to the text
	 * @return The value the user has selected. RETURN_VALUE_UNINITIALIZED implies
	 * the user has not yet made a choice.
	 * @see javax.swing.JOptionPane
	 */
	public static int showConfirmDialog(Component a_parentComponent, String a_message, String a_title,
										int a_optionType, int a_messageType, Icon a_icon,
										ILinkedInformation a_linkedInformation)
	{
		JAPDialog dialog;
		JAPHelpContext.IHelpContext helpContext = null;
		DialogContentPane dialogContentPane;
		JComponent contentPane;
		String message;
		String strLinkedInformation;
		JAPHtmlMultiLineLabel label;
		PreferredWidthBoxPanel dummyBox;
		JComponent linkLabel;

		if (a_message == null)
		{
			a_message = "";
		}
		message = a_message;

		if (a_title == null)
		{
			a_title = JAPMessages.getString(MSG_TITLE_CONFIRMATION);
		}

		/*
		 * If the linked information contains a help context, display the help button instead of a link
		 */
		if (a_linkedInformation instanceof JAPHelpContext.IHelpContext)
		{
			helpContext = (JAPHelpContext.IHelpContext)a_linkedInformation;
			a_linkedInformation = null;
		}

		if (a_linkedInformation != null && a_linkedInformation.getMessage() != null &&
			a_linkedInformation.getMessage().trim().length() > 0)
		{
			strLinkedInformation =
				JAPHtmlMultiLineLabel.removeTagsAndNewLines(a_linkedInformation.getMessage());
			message += JAPHtmlMultiLineLabel.TAG_BREAK + JAPHtmlMultiLineLabel.TAG_A_OPEN +
				strLinkedInformation + JAPHtmlMultiLineLabel.TAG_A_CLOSE;
		}
		else
		{
			strLinkedInformation = null;
		}

		/*
		 * Set the dialog parameters and get its label and content pane.
		 */
		label = new JAPHtmlMultiLineLabel(message);
		label.setFontStyle(JAPHtmlMultiLineLabel.FONT_STYLE_PLAIN);
		dialog = new JAPDialog(a_parentComponent, a_title, true);
		dialogContentPane = new DialogContentPane(dialog,
												  new DialogContentPane.Layout(null, a_messageType, a_icon),
												  new DialogContentPane.Options(a_optionType, helpContext));
		dialogContentPane.setDefaultButtonOperation(DialogContentPane.ON_CLICK_DISPOSE_DIALOG);
		dialogContentPane.setContentPane(label);
		dialogContentPane.updateDialog();
		// trick: a dialog's content pane is always a JComponent; it is needed to set the min/max size
		contentPane = (JComponent)dialog.getContentPane();


		/**
		 * Calculate the optimal dialog size with respect to the golden ratio.
		 * The width defines the longer side.
		 */
		Dimension bestDimension = null;
		Dimension minDimension;
		double currentDelta;
		double bestDelta;
		int currentWidth;
		int bestWidth;
		int failed;

		// get the minimum width and height that is needed to display this dialog without any text
		minDimension =
			new JOptionPane("", a_messageType, a_optionType, a_icon).
			createDialog(a_parentComponent, a_title).getContentPane().getSize();

		// set the maximum width that is allowed for the content pane
		int maxWidth = (int)getParentWindow(a_parentComponent).getSize().width;
		if (maxWidth < (int)minDimension.width * 2)
		{
			maxWidth = (int)minDimension.width * 2;
		}
		// if the text in the content pane is short, reduce the max width to the text length
		maxWidth = Math.min(contentPane.getWidth(), maxWidth);

		// put the content pane in a box and the box in the dialog
		contentPane.setMinimumSize(minDimension);
		dummyBox = new PreferredWidthBoxPanel();
		dummyBox.add(contentPane);

		/**
		 * Do a quick heuristic to approximate the golden ratio for the dialog size.
		 */
		bestDelta = Double.MAX_VALUE;
		currentWidth = maxWidth;
		bestWidth =  currentWidth;
		failed = 0;
		for (int i = 0; i < NUMBER_OF_HEURISTIC_ITERATIONS; i++)
		{
			/**
			 * Set the exact width of the frame.
			 * The following trick must be explained:
			 * Get the HTML view of the label and set its width to the current width of the label that is
			 * defined by the total width of the surrounding content pane. The height of the view may be
			 * unlimited, as the view will adapt its height automatically so that the whole text is
			 * displayed respecting the width that has been set.
			 * @see javax.swing.JLabel.Bounds()
			 * @see javax.swing.JLabel.getTextRectangle()
			 * @see javax.swing.SwingUtilities
			 * @see javax.swing.plaf.basic.BasicHTML
			 * @see javax.swing.text.html.HTMLEditorKit
			 */
			contentPane.setMaximumSize(
						 new Dimension(currentWidth, JAPHtmlMultiLineLabel.UNLIMITED_LABEL_HEIGHT));
			dummyBox.setPreferredWidth(currentWidth);
			dialog.setContentPane(dummyBox);
			dialog.pack();
			label.setPreferredWidth(label.getWidth());
			dialog.pack();

			currentWidth = dummyBox.getWidth();
			currentDelta = getGoldenRatioDelta(dialog);
			if (Math.abs(currentDelta) < Math.abs(bestDelta))
			{
				bestDimension = new Dimension(dummyBox.getSize());
				bestDelta = currentDelta;
				bestWidth = currentWidth;
				currentWidth += bestDelta / 2.0;
				failed = 0;
			}
			else
			{
				currentWidth = bestWidth + (int)(bestDelta / (3.0 * (failed + 1.0)));
				failed++;
			}

			// the objective function value
			//System.out.println("bestDelta: " + bestDelta + "  currentDelta:" + currentDelta);

			currentWidth = (int)Math.max(currentWidth, minDimension.width);
			if (currentWidth == bestWidth)
			{
				break;
			}
		}

		/*
		System.out.println("CurrentSize: " + dummyBox.getSize() + "_" + contentPane.getSize());
		System.out.println("MaximumSize: " + dummyBox.getMaximumSize() + "_" + contentPane.getMaximumSize());
		System.out.println("PreferredSize: " + dummyBox.getPreferredSize() + "_" + contentPane.getPreferredSize());
		*/

		/**
		 * Recreate the dialog and set its final size.
		 */
		dummyBox = new PreferredWidthBoxPanel();
		label = new JAPHtmlMultiLineLabel("<font color=#000000>" + a_message + "</font>");
		label.setFontStyle(JAPHtmlMultiLineLabel.FONT_STYLE_PLAIN);
		dummyBox.add(label);
		linkLabel = null;
		if (strLinkedInformation != null)
		{
			if (a_linkedInformation.isCopyAllowed())
			{   /** @todo this is not nice in most of the old JDKs) */
				JTextPane textPane = GUIUtils.createSelectableAndResizeableLabel(dummyBox);
				/*
				SimpleAttributeSet attributes;
				attributes = new SimpleAttributeSet(textPane.getCharacterAttributes());
				attributes.addAttribute(CharacterConstants.Underline, Boolean.TRUE);
				textPane.setCharacterAttributes(attributes, true);
			*/

				textPane.setText(strLinkedInformation);
				textPane.setFont(label.getFont());
				textPane.setMargin(new java.awt.Insets(0,0,0,0));
				textPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0,0,1,0));
				textPane.setForeground(java.awt.Color.blue);
				linkLabel = textPane;
			}
			else
			{
				linkLabel = new JAPHtmlMultiLineLabel(JAPHtmlMultiLineLabel.TAG_A_OPEN +
					strLinkedInformation + JAPHtmlMultiLineLabel.TAG_A_CLOSE);
			}

			dummyBox.add(linkLabel);
		}

		dialogContentPane.setContentPane(dummyBox);
		dialogContentPane.updateDialog();
		if (strLinkedInformation != null)
		{
			linkLabel.addMouseListener(new LinkedInformationClickListener(a_linkedInformation));
		}
		((JComponent)dialog.getContentPane()).setPreferredSize(bestDimension);
		dialog.pack();
		if (bestDelta != getGoldenRatioDelta(dialog))
		{
			LogHolder.log(LogLevel.NOTICE, LogType.GUI, "Calculated dialog size differs from real size!");
		}
		LogHolder.log(LogLevel.DEBUG, LogType.GUI, "Dialog golden ratio delta: " + getGoldenRatioDelta(dialog));

		dialog.setResizable(false);
		dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		dialog.setVisible(true);

		return dialogContentPane.getValue();
	}

	/**
	 * Displays a message dialog that asks the user for a confirmation.
	 * Words are wrapped automatically if a message line is too long.
	 * @param a_parentDialog The parent dialog for this dialog. If it is null,
	 *                       the dialog's parent frame is the default frame.
	 * @param a_message The message to be displayed. It is interpreted as HTML. You do not need to put in
	 * formatting tags, as the text will be auto-formatted in a way that the dialog's size is very close
	 * to the golden ratio.
	 * @return true if the answer was 'yes'; fale otherwise
	 */
	public static boolean showYesNoDialog(JAPDialog a_parentDialog, String a_message)
	{
		return showYesNoDialog(getInternalDialog(a_parentDialog), a_message);
	}

	/**
	 * Displays a message dialog that asks the user for a confirmation.
	 * Words are wrapped automatically if a message line is too long.
	 * @param a_parentDialog The parent dialog for this dialog. If it is null,
	 *                       the dialog's parent frame is the default frame.
	 * @param a_message The message to be displayed. It is interpreted as HTML. You do not need to put in
	 * formatting tags, as the text will be auto-formatted in a way that the dialog's size is very close
	 * to the golden ratio.
	 * @param a_linkedInformation a clickable information message that is appended to the text
	 * @return true if the answer was 'yes'; fale otherwise
	 */
	public static boolean showYesNoDialog(JAPDialog a_parentDialog, String a_message,
										  ILinkedInformation a_linkedInformation)
	{
		return showYesNoDialog(getInternalDialog(a_parentDialog), a_message, a_linkedInformation);
	}

	/**
	 * Displays a message dialog that asks the user for a confirmation.
	 * Words are wrapped automatically if a message line is too long.
	 * @param a_parentComponent The parent component for this dialog. If it is null or the parent
	 *                          component is not within a frame, the dialog's parent frame is the
	 *                          default frame.
	 * @param a_message The message to be displayed. It is interpreted as HTML. You do not need to put in
	 * formatting tags, as the text will be auto-formatted in a way that the dialog's size is very close
	 * to the golden ratio.
	 * @return true if the answer was 'yes'; fale otherwise
	 */
	public static boolean showYesNoDialog(Component a_parentComponent, String a_message)
	{
		return showYesNoDialog(a_parentComponent, a_message, (String)null);
	}

	/**
	 * Displays a message dialog that asks the user for a confirmation.
	 * Words are wrapped automatically if a message line is too long.
	 * @param a_parentComponent The parent component for this dialog. If it is null or the parent
	 *                          component is not within a frame, the dialog's parent frame is the
	 *                          default frame.
	 * @param a_message The message to be displayed. It is interpreted as HTML. You do not need to put in
	 * formatting tags, as the text will be auto-formatted in a way that the dialog's size is very close
	 * to the golden ratio.
	 * @param a_linkedInformation a clickable information message that is appended to the text
	 * @return true if the answer was 'yes'; fale otherwise
	 */
	public static boolean showYesNoDialog(Component a_parentComponent, String a_message,
										  ILinkedInformation a_linkedInformation)
	{
		return showYesNoDialog(a_parentComponent, a_message, null, a_linkedInformation);
	}

	/**
	 * Displays a message dialog that asks the user for a confirmation.
	 * Words are wrapped automatically if a message line is too long.
	 * @param a_parentDialog The parent dialog for this dialog. If it is null,
	 *                       the dialog's parent frame is the default frame.
	 * @param a_title The title of the message dialog
	 * @param a_message The message to be displayed. It is interpreted as HTML. You do not need to put in
	 * formatting tags, as the text will be auto-formatted in a way that the dialog's size is very close
	 * to the golden ratio.
	 * @return true if the answer was 'yes'; fale otherwise
	 */
	public static boolean showYesNoDialog(JAPDialog a_parentDialog,  String a_message, String a_title)
	{
		return showYesNoDialog(getInternalDialog(a_parentDialog), a_message, a_title);
	}

	/**
	 * Displays a message dialog that asks the user for a confirmation.
	 * Words are wrapped automatically if a message line is too long.
	 * @param a_parentDialog The parent dialog for this dialog. If it is null,
	 *                       the dialog's parent frame is the default frame.
	 * @param a_title The title of the message dialog
	 * @param a_message The message to be displayed. It is interpreted as HTML. You do not need to put in
	 * formatting tags, as the text will be auto-formatted in a way that the dialog's size is very close
	 * to the golden ratio.
	 * @param a_linkedInformation a clickable information message that is appended to the text
	 * @return true if the answer was 'yes'; fale otherwise
	 */
	public static boolean showYesNoDialog(JAPDialog a_parentDialog, String a_message, String a_title,
										  ILinkedInformation a_linkedInformation)
	{
		return showYesNoDialog(getInternalDialog(a_parentDialog), a_message, a_title, a_linkedInformation);
	}

	/**
	 * Displays a message dialog that asks the user for a confirmation.
	 * Words are wrapped automatically if a message line is too long.
	 * @param a_parentComponent The parent component for this dialog. If it is null or the parent
	 *                          component is not within a frame, the dialog's parent frame is the
	 *                          default frame.
	 * @param a_title The title of the message dialog
	 * @param a_message The message to be displayed. It is interpreted as HTML. You do not need to put in
	 * formatting tags, as the text will be auto-formatted in a way that the dialog's size is very close
	 * to the golden ratio.
	 * @return true if the answer was 'yes'; fale otherwise
	 */
	public static boolean showYesNoDialog(Component a_parentComponent, String a_message, String a_title)
	{
		return showYesNoDialog(a_parentComponent, a_message, a_title, null);
	}

	/**
	 * Displays a message dialog that asks the user for a confirmation.
	 * Words are wrapped automatically if a message line is too long.
	 * @param a_parentComponent The parent component for this dialog. If it is null or the parent
	 *                          component is not within a frame, the dialog's parent frame is the
	 *                          default frame.
	 * @param a_title The title of the message dialog
	 * @param a_message The message to be displayed. It is interpreted as HTML. You do not need to put in
	 * formatting tags, as the text will be auto-formatted in a way that the dialog's size is very close
	 * to the golden ratio.
	 * @param a_linkedInformation a clickable information message that is appended to the text
	 * @return true if the answer was 'yes'; fale otherwise
	 */
	public static boolean showYesNoDialog(Component a_parentComponent, String a_message, String a_title,
										  ILinkedInformation a_linkedInformation)
	{
		int response;

		if (a_title == null)
		{
			a_title = JAPMessages.getString(MSG_TITLE_CONFIRMATION);
		}
		response = showConfirmDialog(a_parentComponent, a_message, a_title, OPTION_TYPE_YES_NO,
									MESSAGE_TYPE_QUESTION, null, a_linkedInformation);

		return RETURN_VALUE_YES == response;
	}

	/**
	 * Brings up a dialog where the number of choices is determined by the optionType parameter.
	 * The messageType parameter is primarily used to supply a default icon from the look and feel.
	 * Words are wrapped automatically if a message line is too long.
	 * @param a_parentDialog The parent dialog for this dialog. If it is null,
	 *                       the dialog's parent frame is the default frame.
	 * @param a_message The message to be displayed. It is interpreted as HTML. You do not need to put in
	 * formatting tags, as the text will be auto-formatted in a way that the dialog's size is very close
	 * to the golden ratio.
	 * @param a_messageType use the message types from JOptionPane
	 * @param a_optionType use the option types from JOptionPane
	 * @param a_icon an icon that will be displayed on the dialog
	 * @return an int indicating the option selected by the user
	 * @see javax.swing.JOptionPane
	 */
	public static int showConfirmDialog(JAPDialog a_parentDialog, String a_message,
										int a_optionType, int a_messageType, Icon a_icon)
	{
		return showConfirmDialog(getInternalDialog(a_parentDialog), a_message, null,
								 a_optionType, a_messageType, a_icon, null);
	}

	/**
	 * Brings up a dialog where the number of choices is determined by the optionType parameter.
	 * The messageType parameter is primarily used to supply a default icon from the look and feel.
	 * Words are wrapped automatically if a message line is too long.
	 * @param a_parentComponent The parent component for this dialog. If it is null or the parent
	 *                          component is not within a frame, the dialog's parent frame is the
	 *                          default frame.
	 * @param a_message The message to be displayed. It is interpreted as HTML. You do not need to put in
	 * formatting tags, as the text will be auto-formatted in a way that the dialog's size is very close
	 * to the golden ratio.
	 * @param a_messageType use the message types from JOptionPane
	 * @param a_optionType use the option types from JOptionPane
	 * @param a_icon an icon that will be displayed on the dialog
	 * @return an int indicating the option selected by the user
	 * @see javax.swing.JOptionPane
	 */
	public static int showConfirmDialog(Component a_parentComponent, String a_message,
										int a_optionType, int a_messageType, Icon a_icon)
	{
		return showConfirmDialog(a_parentComponent, a_message, null, a_optionType, a_messageType, a_icon,
								 null);
	}

	/**
	 * Brings up a dialog where the number of choices is determined by the optionType parameter.
	 * The messageType parameter is primarily used to supply a default icon from the look and feel.
	 * Words are wrapped automatically if a message line is too long.
	 * @param a_parentDialog The parent dialog for this dialog. If it is null,
	 *                       the dialog's parent frame is the default frame.
	 * @param a_message The message to be displayed. It is interpreted as HTML. You do not need to put in
	 * formatting tags, as the text will be auto-formatted in a way that the dialog's size is very close
	 * to the golden ratio.
	 * @param a_messageType use the message types from JOptionPane
	 * @param a_optionType use the option types from JOptionPane
	 * @return an int indicating the option selected by the user
	 * @see javax.swing.JOptionPane
	 */
	public static int showConfirmDialog(JAPDialog a_parentDialog, String a_message,
										int a_optionType, int a_messageType)
	{
		return showConfirmDialog(getInternalDialog(a_parentDialog), a_message, null,
								 a_optionType, a_messageType, null, null);
	}

	/**
	 * Brings up a dialog where the number of choices is determined by the optionType parameter.
	 * The messageType parameter is primarily used to supply a default icon from the look and feel.
	 * Words are wrapped automatically if a message line is too long.
	 * @param a_parentComponent The parent component for this dialog. If it is null or the parent
	 *                          component is not within a frame, the dialog's parent frame is the
	 *                          default frame.
	 * @param a_message The message to be displayed. It is interpreted as HTML. You do not need to put in
	 * formatting tags, as the text will be auto-formatted in a way that the dialog's size is very close
	 * to the golden ratio.
	 * @param a_messageType use the message types from JOptionPane
	 * @param a_optionType use the option types from JOptionPane
	 * @return an int indicating the option selected by the user
	 * @see javax.swing.JOptionPane
	 */
	public static int showConfirmDialog(Component a_parentComponent, String a_message,
										int a_optionType, int a_messageType)
	{
		return showConfirmDialog(a_parentComponent, a_message, null, a_optionType, a_messageType, null,
								 null);
	}

	/**
	 * Brings up a dialog where the number of choices is determined by the optionType parameter.
	 * The messageType parameter is primarily used to supply a default icon from the look and feel.
	 * Words are wrapped automatically if a message line is too long.
	 * @param a_parentDialog The parent dialog for this dialog. If it is null,
	 *                       the dialog's parent frame is the default frame.
	 * @param a_message The message to be displayed. It is interpreted as HTML. You do not need to put in
	 * formatting tags, as the text will be auto-formatted in a way that the dialog's size is very close
	 * to the golden ratio.
	 * @param a_messageType use the message types from JOptionPane
	 * @param a_optionType use the option types from JOptionPane
	 * @param a_linkedInformation a clickable information message that is appended to the text
	 * @return an int indicating the option selected by the user
	 * @see javax.swing.JOptionPane
	 */
	public static int showConfirmDialog(JAPDialog a_parentDialog, String a_message,
										int a_optionType, int a_messageType,
										ILinkedInformation a_linkedInformation)
	{
		return showConfirmDialog(getInternalDialog(a_parentDialog), a_message, null,
								 a_optionType, a_messageType, null, a_linkedInformation);
	}

	/**
	 * Brings up a dialog where the number of choices is determined by the optionType parameter.
	 * The messageType parameter is primarily used to supply a default icon from the look and feel.
	 * Words are wrapped automatically if a message line is too long.
	 * @param a_parentComponent The parent component for this dialog. If it is null or the parent
	 *                          component is not within a frame, the dialog's parent frame is the
	 *                          default frame.
	 * @param a_message The message to be displayed. It is interpreted as HTML. You do not need to put in
	 * formatting tags, as the text will be auto-formatted in a way that the dialog's size is very close
	 * to the golden ratio.
	 * @param a_messageType use the message types from JOptionPane
	 * @param a_optionType use the option types from JOptionPane
	 * @param a_linkedInformation a clickable information message that is appended to the text
	 * @return an int indicating the option selected by the user
	 * @see javax.swing.JOptionPane
	 */
	public static int showConfirmDialog(Component a_parentComponent, String a_message,
										int a_optionType, int a_messageType,
										ILinkedInformation a_linkedInformation)
	{
		return showConfirmDialog(a_parentComponent, a_message, null,
								 a_optionType, a_messageType, null, a_linkedInformation);
	}

	/**
	 * Brings up a dialog where the number of choices is determined by the optionType parameter.
	 * The messageType parameter is primarily used to supply a default icon from the look and feel.
	 * Words are wrapped automatically if a message line is too long.
	 * @param a_parentDialog The parent dialog for this dialog. If it is null,
	 *                       the dialog's parent frame is the default frame.
	 * @param a_title The title of the message dialog
	 * @param a_message The message to be displayed. It is interpreted as HTML. You do not need to put in
	 * formatting tags, as the text will be auto-formatted in a way that the dialog's size is very close
	 * to the golden ratio.
	 * @param a_messageType use the message types from JOptionPane
	 * @param a_optionType use the option types from JOptionPane
	 * @return an int indicating the option selected by the user
	 * @see javax.swing.JOptionPane
	 */
	public static int showConfirmDialog(JAPDialog a_parentDialog, String a_message, String a_title,
										int a_optionType, int a_messageType)
	{
		return showConfirmDialog(getInternalDialog(a_parentDialog), a_message, a_title,
								 a_optionType, a_messageType, null, null);
	}

	/**
	 * Brings up a dialog where the number of choices is determined by the optionType parameter.
	 * The messageType parameter is primarily used to supply a default icon from the look and feel.
	 * Words are wrapped automatically if a message line is too long.
	 * @param a_parentComponent The parent component for this dialog. If it is null or the parent
	 *                          component is not within a frame, the dialog's parent frame is the
	 *                          default frame.
	 * @param a_title The title of the message dialog
	 * @param a_message The message to be displayed. It is interpreted as HTML. You do not need to put in
	 * formatting tags, as the text will be auto-formatted in a way that the dialog's size is very close
	 * to the golden ratio.
	 * @param a_messageType use the message types from JOptionPane
	 * @param a_optionType use the option types from JOptionPane
	 * @return an int indicating the option selected by the user
	 * @see javax.swing.JOptionPane
	 */
	public static int showConfirmDialog(Component a_parentComponent, String a_message, String a_title,
										int a_optionType, int a_messageType)
	{
		return showConfirmDialog(a_parentComponent, a_message, a_title,
								 a_optionType, a_messageType, null, null);
	}


	/**
	 * Brings up a dialog where the number of choices is determined by the optionType parameter.
	 * The messageType parameter is primarily used to supply a default icon from the look and feel.
	 * Words are wrapped automatically if a message line is too long.
	 * @param a_parentDialog The parent dialog for this dialog. If it is null,
	 *                       the dialog's parent frame is the default frame.
	 * @param a_title The title of the message dialog
	 * @param a_message The message to be displayed. It is interpreted as HTML. You do not need to put in
	 * formatting tags, as the text will be auto-formatted in a way that the dialog's size is very close
	 * to the golden ratio.
	 * @param a_messageType use the message types from JOptionPane
	 * @param a_optionType use the option types from JOptionPane
	 * @param a_linkedInformation a clickable information message that is appended to the text
	 * @return an int indicating the option selected by the user
	 * @see javax.swing.JOptionPane
	 */
	public static int showConfirmDialog(JAPDialog a_parentDialog, String a_message, String a_title,
										int a_optionType, int a_messageType,
										ILinkedInformation a_linkedInformation)
	{
		return showConfirmDialog(getInternalDialog(a_parentDialog), a_message, a_title,
								 a_optionType, a_messageType, null, a_linkedInformation);
	}

	/**
	 * Brings up a dialog where the number of choices is determined by the optionType parameter.
	 * The messageType parameter is primarily used to supply a default icon from the look and feel.
	 * Words are wrapped automatically if a message line is too long.
	 * @param a_parentComponent The parent component for this dialog. If it is null or the parent
	 *                          component is not within a frame, the dialog's parent frame is the
	 *                          default frame.
	 * @param a_title The title of the message dialog
	 * @param a_message The message to be displayed. It is interpreted as HTML. You do not need to put in
	 * formatting tags, as the text will be auto-formatted in a way that the dialog's size is very close
	 * to the golden ratio.
	 * @param a_messageType use the message types from JOptionPane
	 * @param a_optionType use the option types from JOptionPane
	 * @param a_linkedInformation a clickable information message that is appended to the text
	 * @return an int indicating the option selected by the user
	 * @see javax.swing.JOptionPane
	 */
	public static int showConfirmDialog(Component a_parentComponent, String a_message, String a_title,
										int a_optionType, int a_messageType,
										ILinkedInformation a_linkedInformation)
	{
		return showConfirmDialog(a_parentComponent, a_message, a_title, a_optionType, a_messageType,
									null, a_linkedInformation);
	}

	/**
	 * Displays a dialog showing an error message to the user and logs the error message
	 * to the currently used Log.
	 * @param a_parentDialog The parent dialog for this dialog. If it is null or the parent
	 *                       dialog is not within a frame, the dialog's parent frame is the
	 *                       default frame.
	 * @param a_message a message that is shown to the user (may be null)
	 * @param a_logType the log type for this error
	 * @see logging.LogHolder
	 * @see logging.LogType
	 * @see logging.Log
	 */
	public static void showErrorDialog(JAPDialog a_parentDialog, String a_message, int a_logType)
	{
		showErrorDialog(a_parentDialog, a_message, a_logType, (Throwable)null);
	}

	/**
	 * Displays a dialog showing an error message to the user and logs the error message
	 * to the currently used Log.
	 * @param a_parentComponent The parent component for this dialog. If it is null or the parent
	 *                          component is not within a frame, the dialog's parent frame is the
	 *                          default frame.
	 * @param a_message a message that is shown to the user (may be null)
	 * @param a_logType the log type for this error
	 * @see logging.LogHolder
	 * @see logging.LogType
	 * @see logging.Log
	 */
	public static void showErrorDialog(Component a_parentComponent, String a_message, int a_logType)
	{
		showErrorDialog(a_parentComponent, a_message, a_logType, (Throwable)null);
	}

	/**
	 * Displays a dialog showing an error message to the user and logs the error message
	 * to the currently used Log.
	 * @param a_parentComponent The parent component for this dialog. If it is null or the parent
	 *                          component is not within a frame, the dialog's parent frame is the
	 *                          default frame.
	 * @param a_title a title for the error message (may be null)
	 * @param a_message a message that is shown to the user (may be null)
	 * @param a_logType the log type for this error
	 * @see logging.LogHolder
	 * @see logging.LogType
	 * @see logging.Log
	 */
	public static void showErrorDialog(Component a_parentComponent, String a_message,  int a_logType,
									   String a_title)
	{
		showErrorDialog(a_parentComponent, a_message, a_title, a_logType, null);
	}


	/**
	 * Displays a dialog showing an error message to the user and logs the error message
	 * to the currently used Log.
	 * @param a_parentDialog The parent dialog for this dialog. If it is null or the parent
	 *                       dialog is not within a frame, the dialog's parent frame is the
	 *                       default frame.
	 * @param a_title a title for the error message (may be null)
	 * @param a_message a message that is shown to the user (may be null)
	 * @param a_logType the log type for this error
	 * @see logging.LogHolder
	 * @see logging.LogType
	 * @see logging.Log
	 */
	public static void showErrorDialog(JAPDialog a_parentDialog, String a_message,  int a_logType,
									   String a_title)
	{
		showErrorDialog(getInternalDialog(a_parentDialog), a_message, a_title, a_logType, null);
	}

	/**
	 * Displays a dialog showing an error message to the user and logs the error message
	 * to the currently used Log.
	 * @param a_parentComponent The parent component for this dialog. If it is null or the parent
	 *                          component is not within a frame, the dialog's parent frame is the
	 *                          default frame.
	 * @param a_title a title for the error message (may be null)
	 * @param a_message a message that is shown to the user (may be null)
	 * @param a_logType the log type for this error
	 * @see logging.LogHolder
	 * @see logging.LogType
	 * @see logging.Log
	 */
	public static void showErrorDialog(Component a_parentComponent, String a_message, String a_title,
									   int a_logType)
	{
		showErrorDialog(a_parentComponent, a_message, a_title, a_logType, null);
	}

	/**
	 * Displays a dialog showing an error message to the user and logs the error message
	 * to the currently used Log.
	 * @param a_parentDialog The parent dialog for this dialog. If it is null or the parent
	 *                       dialog is not within a frame, the dialog's parent frame is the
	 *                       default frame.
	 * @param a_throwable a Throwable that has been caught (may be null)
	 * @param a_logType the log type for this error
	 * @see logging.LogHolder
	 * @see logging.LogType
	 * @see logging.Log
	 */
	public static void showErrorDialog(JAPDialog a_parentDialog, int a_logType, Throwable a_throwable)
	{
		showErrorDialog(getInternalDialog(a_parentDialog), null, null, a_logType, a_throwable);
	}

	/**
	 * Displays a dialog showing an error message to the user and logs the error message
	 * to the currently used Log.
	 * @param a_parentComponent The parent component for this dialog. If it is null or the parent
	 *                          component is not within a frame, the dialog's parent frame is the
	 *                          default frame.
	 * @param a_throwable a Throwable that has been caught (may be null)
	 * @param a_logType the log type for this error
	 * @see logging.LogHolder
	 * @see logging.LogType
	 * @see logging.Log
	 */
	public static void showErrorDialog(Component a_parentComponent,  int a_logType,
									   Throwable a_throwable)
	{
		showErrorDialog(a_parentComponent, null, null, a_logType, a_throwable);
	}


	/**
	 * Displays a dialog showing an error message to the user and logs the error message
	 * to the currently used Log.
	 * @param a_parentDialog The parent dialog for this dialog. If it is null or the parent
	 *                       dialog is not within a frame, the dialog's parent frame is the
	 *                       default frame.
	 * @param a_throwable a Throwable that has been caught (may be null)
	 * @param a_message a message that is shown to the user (may be null)
	 * @param a_logType the log type for this error
	 * @see logging.LogHolder
	 * @see logging.LogType
	 * @see logging.Log
	 */
	public static void showErrorDialog(JAPDialog a_parentDialog, String a_message, int a_logType,
									   Throwable a_throwable)
	{
		showErrorDialog(getInternalDialog(a_parentDialog), a_message, a_logType, a_throwable);
	}

	/**
	 * Displays a dialog showing an error message to the user and logs the error message
	 * to the currently used Log.
	 * @param a_parentComponent The parent component for this dialog. If it is null or the parent
	 *                          component is not within a frame, the dialog's parent frame is the
	 *                          default frame.
	 * @param a_throwable a Throwable that has been caught (may be null)
	 * @param a_message a message that is shown to the user (may be null)
	 * @param a_logType the log type for this error
	 * @see logging.LogHolder
	 * @see logging.LogType
	 * @see logging.Log
	 */
	public static void showErrorDialog(Component a_parentComponent,  String a_message, int a_logType,
									   Throwable a_throwable)
	{
		showErrorDialog(a_parentComponent, a_message, null, a_logType, a_throwable);
	}

	/**
	 * Displays a dialog showing an error message to the user and logs the error message
	 * to the currently used Log.
	 * @param a_parentDialog The parent dialog for this dialog. If it is null or the parent
	 *                       dialog is not within a frame, the dialog's parent frame is the
	 *                       default frame.
	 * @param a_throwable a Throwable that has been caught (may be null)
	 * @param a_title a title for the error message (may be null)
	 * @param a_message a message that is shown to the user (may be null)
	 * @param a_logType the log type for this error
	 * @see logging.LogHolder
	 * @see logging.LogType
	 * @see logging.Log
	 */
	public static void showErrorDialog(JAPDialog a_parentDialog, String a_message, String a_title,
									   int a_logType, Throwable a_throwable)
	{
		showErrorDialog(getInternalDialog(a_parentDialog), a_message, a_title, a_logType, a_throwable);
	}

	/**
	 * Displays a dialog showing an error message to the user and logs the error message
	 * to the currently used Log.
	 * @param a_parentComponent The parent component for this dialog. If it is null or the parent
	 *                          component is not within a frame, the dialog's parent frame is the
	 *                          default frame.
	 * @param a_throwable a Throwable that has been caught (may be null)
	 * @param a_title a title for the error message (may be null)
	 * @param a_message a message that is shown to the user (may be null)
	 * @param a_logType the log type for this error
	 * @see logging.LogHolder
	 * @see logging.LogType
	 * @see logging.Log
	 */
	public static void showErrorDialog(Component a_parentComponent, String a_message, String a_title,
									   int a_logType, Throwable a_throwable)
	{
		boolean bPossibleApplicationError = false;

		a_message = retrieveErrorMessage(a_message, a_throwable);
		if (a_message == null)
		{
			a_message = JAPMessages.getString(MSG_ERROR_UNKNOWN);
			bPossibleApplicationError = true;
		}

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

		try
		{
			if (a_title == null)
			{
				a_title = JAPMessages.getString(MSG_TITLE_ERROR);
			}
			showConfirmDialog(a_parentComponent, a_message, a_title,
							 OPTION_TYPE_DEFAULT, MESSAGE_TYPE_ERROR, null, null);
		}
		catch (Exception e)
		{
			LogHolder.log(LogLevel.EXCEPTION, LogType.GUI, JAPMessages.getString(MSG_ERROR_UNDISPLAYABLE));
			LogHolder.log(LogLevel.EXCEPTION, LogType.GUI, e);
		}
	}

	/**
	 * Retrieves an error message from a Throwable and a message String that may be shown to the
	 * user. By default, this is the given message. If no message is given, it is tried to get the error
	 * message from the Throwable. A log message for the error is written automatically.
	 * @param a_throwable a Throwable (may be null)
	 * @param a_message an error message (may be null)
	 * @return the retrieved error message or null if no error message could be found; this would
	 * indicate a serious application error
	 */
	public static String retrieveErrorMessage(String a_message, Throwable a_throwable)
	{
		if (a_message == null || a_message.trim().length() == 0)
		{
			if (a_throwable == null || a_throwable.getMessage() == null)
			{
				a_message = null;

			}
			else
			{
				a_message = a_throwable.getMessage();
				if (a_message == null || a_message.trim().length() == 0)
				{
					a_message = null;
				}
			}
		}

		return a_message;
	}

	/**
	 * Classes of this type are used to append a clickable message at the end of a dialog message.
	 * You may also allow to copy the message to the cip board and define any after-click-action
	 * that you want.
	 */
	public static interface ILinkedInformation
	{
		/**
		 * Returns the information message. This must be normal text, HTML is not allowed and
		 * tags are filtered out.
		 * @return the information message
		 */
		public String getMessage();
		/**
		 * The action that is performed when the link is clicked, for example opening a browser
		 * window, an E-Mail client or a help page.
		 */
		public void openLink();
		/**
		 * Returns if the user is allowed to copy the link text.
		 * @return if the user is allowed to copy the link text
		 */
		public boolean isCopyAllowed();
		/**
		 * Returns if the dialog should only be modal for its direct parent. This would means
		 * that other application windows will still be accessible if the dialog is modal.
		 * @return if the dialog should only be modal for its direct parent
		 */
		public boolean isDialogSemiModal();
	}

	/**
	 * This is an example implementation of ILinkedInformation. It registers a help context in the
	 * dialog that is opened when the user clicks on a "More info..." or a self-defined String.
	 */
	public static final class LinkedHelpContext implements ILinkedInformation, JAPHelpContext.IHelpContext
	{
		private static final String MSG_MORE_INFO = LinkedHelpContext.class.getName() + "_moreInfo";

		private String m_strMessage;
		private JAPHelpContext.IHelpContext m_helpContext;

		public LinkedHelpContext(final String a_strHelpContext, String a_strMessage)
		{
			this(new JAPHelpContext.IHelpContext(){public String getHelpContext(){return a_strHelpContext;}},
				a_strMessage);
		}

		public LinkedHelpContext(JAPHelpContext.IHelpContext a_helpContext, String a_strMessage)
		{
			if (a_strMessage == null || a_strMessage.trim().length() == 0)
			{
				a_strMessage = JAPMessages.getString(MSG_MORE_INFO);
			}
			m_helpContext = a_helpContext;
			m_strMessage = a_strMessage;
		}

		public LinkedHelpContext(JAPHelpContext.IHelpContext a_helpContext)
		{
			this(a_helpContext, null);
		}

		public LinkedHelpContext(String a_strHelpContext)
		{
			this(a_strHelpContext, null);
		}

		public String getHelpContext()
		{
			if (m_helpContext == null)
			{
				return null;
			}
			return m_helpContext.getHelpContext();
		}

		public String getMessage()
		{
			return m_strMessage;
		}
		/**
		 * Opens a help window with the registered context.
		 */
		public void openLink()
		{
			JAPHelp.getInstance().getContextObj().setContext(m_helpContext);
			JAPHelp.getInstance().setVisible(true);
			JAPHelp.getInstance().requestFocus();
		}
		/**
		 * This makes no sense and is not allowed.
		 * @return false
		 */
		public boolean isCopyAllowed()
		{
			return false;
		}
		/**
		 * Returns true as otherwise the help window would not be accessible.
		 * @return true
		 */
		public boolean isDialogSemiModal()
		{
			return true;
		}
	}

	/**
	 * Returns the glass pane.
	 * @return the glass pane
	 */
	public Component getGlassPane()
	{
		return m_internalDialog.getGlassPane();
	}

	/**
	 * Returns the JLayeredPane.
	 * @return the JLayeredPane
	 */
	public JLayeredPane getLayeredPane()
	{
		return m_internalDialog.getLayeredPane();
	}

	/**
	 * Returns the root pane of the dialog.
	 * @return the root pane of the dialog
	 */
	public JRootPane getRootPane()
	{
		return m_internalDialog.getRootPane();
	}

	/**
	 * Returns the content pane that can be used to place elements on the dialog.
	 * @return the dialog's content pane
	 */
	public final Container getContentPane()
	{
		return m_internalDialog.getContentPane();
	}

	/**
	 * Sets a new content pane for this dialog.
	 * @param a_contentPane a new content pane for this dialog
	 */
	public void setContentPane(Container a_contentPane)
	{
		m_internalDialog.setContentPane(a_contentPane);
	}

	/**
	 * Sets a new glass pane for this dialog.
	 * @param a_glassPane a new glass pane for this dialog
	 */
	public void setGlassPane(Component a_glassPane)
	{
		m_internalDialog.setGlassPane(a_glassPane);
	}

	/**
	 * Sets a new JLayeredPane for this dialog.
	 * @param a_layeredPane a new JLayeredPane for this dialog
	 */
	public void setLayeredPane(JLayeredPane a_layeredPane)
	{
		m_internalDialog.setLayeredPane(a_layeredPane);
	}

	/**
	 * Returns the parent Component.
	 * @return the parent Component
	 */
	public final Component getParentComponent()
	{
		return m_parentComponent;
	}

	/**
	 * Returns the parent Window. The parent Window may be but does not need to be the same than the
	 * parent Component.
	 * @return the parent Window
	 */
	public final Window getOwner()
	{
		return m_parentWindow;
	}

	/**
	 * Returns if the dialog is visible on the screen.
	 * @return true if the dialog is visible on the screen; false otherwise
	 */
	public boolean isVisible()
	{
		return m_internalDialog.isVisible();
	}

	/**
	 * Shows or hides the dialog. If shown, the dialog is centered over the parent component.
	 * Subclasses may override this method to change this centering behaviour.
	 * @param a_bVisible 'true' shows the dialog; 'false' hides it
	 */
	public void setVisible(boolean a_bVisible)
	{
		setVisible(a_bVisible, true);
	}

	/**
	 * Shows or hides the dialog.
	 * @param a_bVisible 'true' shows the dialog; 'false' hides it
	 * @param a_bCenterOnParentComponent if true, the dialog is centered on the parent component;
	 * otherwise, it is positioned right under the parent window (owner window)
	 */
	public final void setVisible(boolean a_bVisible, boolean a_bCenterOnParentComponent)
	{
		if (a_bVisible)
		{
			if (!m_bLocationSetManually && !isVisible())
			{
				if (a_bCenterOnParentComponent)
				{
					m_internalDialog.setLocationRelativeTo(getParentComponent());
				}
				else
				{
					GUIUtils.positionRightUnderWindow(m_internalDialog, getOwner());
				}
			}

		}
		setVisibleInternal(a_bVisible);
	}

	/**
	 * Sets the title of this dialog.
	 * @param a_title the title of this dialog
	 */
	public final void setTitle(String a_title)
	{
		m_internalDialog.setTitle(a_title);
	}

	/**
	 * Sets the menubar for this dialog.
	 * @param a_menubar the menubar being placed in the dialog
	 */
	public void setJMenuBar(JMenuBar a_menubar)
	{
		m_internalDialog.setJMenuBar(a_menubar);
	}

	/**
	 * Returns the menubar set on this dialog.
	 * @return the menubar set on this dialog
	 */
	public JMenuBar getJMenuBar()
	{
		return m_internalDialog.getJMenuBar();
	}

	/**
	 * If this Window is visible, brings this Window to the front and may make it the focused Window.
	 */
	public void toFront()
	{
		m_internalDialog.toFront();
	}

	/**
	 * If this Window is visible, sends this Window to the back and may cause it to lose focus or
	 * activation if it is the focused or active Window.
	 */
	public void toBack()
	{
		m_internalDialog.toBack();
	}

	/**
	 * Defines the dialog as modal or not.
	 * @param a_bModal true if the dialog should be modal; false otherwise
	 */
	public final void setModal(boolean a_bModal)
	{
		if (!isVisible())
		{
			m_bModal = a_bModal;
		}
		// the internal dialog is always non-modal
	}

	/**
	 * Returns if the dialog is modal.
	 * @return if the dialog is modal
	 */
	public boolean isModal()
	{
		return m_bModal;
	}

	/**
	 * Returns if the dialog is resizable by the user.
	 * @return if the dialog is resizable by the user
	 */
	public boolean isResizable()
	{
		return m_internalDialog.isResizable();
	}

	/**
	 * Disposes the dialog (set it to invisible and releases all resources).
	 * @todo Causes a Thread deadlock if called from Threads other than the main Thread or the
	 * AWT Event Thread. Removing the setVisible(false) would solve this problem, but causes a
	 * java.lang.IllegalMonitorStateException with JDK 1.2.2.
	 * If Threads are startet with gui.dialog.WorkerContentPane, everything is OK, so don't worry.
	 */
	public final void dispose()
	{
		if (m_bBlockParentWindow)
		{
			m_bBlockParentWindow = false;
			m_parentWindow.setEnabled(true);
			m_parentWindow.setVisible(true);
		}

		m_internalDialog.setVisible(false);
		m_internalDialog.dispose();

		synchronized (m_internalDialog.getTreeLock())
		{
			m_internalDialog.getTreeLock().notifyAll();
		}
	}

	/**
	 * Validates the dialog. Should be called after changing the content pane when the dialog is visible.
	 */
	public void validate()
	{
		m_internalDialog.validate();
	}

	/**
	 * Try to get the focus.
	 */
	public void requestFocus()
	{
		m_internalDialog.requestFocus();
	}

	/**
	 * Returns the size of the dialog window.
	 * @return the size of the dialog window
	 */
	public final Dimension getSize()
	{
		return m_internalDialog.getSize();
	}

	/**
	 * Sets the size of the dialog window.
	 * @param a_width the new window width
	 * @param a_height the new window height
	 */
	public final void setSize(int a_width, int a_height)
	{
			m_internalDialog.setSize(a_width, a_height);
	}

	/**
	 * Sets the location of the dialog 'manually'. After that, no automatic alignment is done by this dialog.
	 * @param a_location a Point on the screen
	 */
	public final void setLocation(Point a_location)
	{
		m_bLocationSetManually = true;
		m_internalDialog.setLocation(a_location);
	}

	/**
	 * The dialog is centered on the given Component.
	 * Sets the location of the dialog 'manually'. After that, no automatic alignment is done by this dialog.
	 * @param a_component a Component
	 */
	public final void setLocationCenteredOn(Component a_component)
	{
		m_bLocationSetManually = true;
		m_internalDialog.setLocationRelativeTo(a_component);
	}

	/**
	 * The dialog is centered on the parent Component.
	 * Sets the location of the dialog 'manually'. After that, no automatic alignment is done by this dialog.
	 */
	public final void setLocationCenteredOnParent()
	{
		m_bLocationSetManually = true;
		m_internalDialog.setLocationRelativeTo(getParentComponent());
	}

	/**
	 * Centers this dialog relative to the screen.
	 * Sets the location of the dialog 'manually'. After that, no automatic alignment is done by this dialog.
	 */
	public final void setLocationCenteredOnScreen()
	{
		m_bLocationSetManually = true;
		GUIUtils.centerOnScreen(m_internalDialog);
	}

	/**
	 * The dialog is positioned right under the owner window.
	 * Sets the location of the dialog 'manually'. After that, no automatic alignment is done by this dialog.
	 */
	public final void setLocationRelativeToOwner()
	{
		m_bLocationSetManually = true;
		GUIUtils.positionRightUnderWindow(m_internalDialog, getOwner());
	}

	/**
	 * Sets the location of the dialog 'manually'. After that,
	 * no automatic alignment is done by this dialog.
	 * @param x a x cooredinate on the screen
	 * @param y a y cooredinate on the screen
	 */
	public final void setLocation(int x, int y)
	{
		m_bLocationSetManually = true;
		m_internalDialog.setLocation(x, y);
	}

	/**
	 * Sets the size of the dialog window.
	 * @param a_size the new size of the dialog window
	 */
	public final void setSize(Dimension a_size)
	{
		m_internalDialog.setSize(a_size);
	}

	/**
	 * Allows to set the dialog resizable or fixed-sized.
	 * @param a_bResizable true if the dialog should become resizable; false otherwise
	 */
	public void setResizable(boolean a_bResizable)
	{
		m_internalDialog.setResizable(a_bResizable);
	}

	/**
	 * Returns the dialog's location on the screen.
	 * @return the dialog's location on the screen
	 */
	public final Point getLocation()
	{
		return m_internalDialog.getLocation();
	}

	public boolean imageUpdate(Image a_image, int a_infoflags, int a_x, int a_y, int a_width, int a_height)
	{
		return m_internalDialog.imageUpdate(a_image, a_infoflags, a_x, a_y, a_width, a_height);
	}

	/**
	 * Returns the AccessibleContext associated with this dialog
	 * @return the AccessibleContext associated with this dialog
	 */
	public final AccessibleContext getAccessibleContext()
	{
		return m_internalDialog.getAccessibleContext();
	}

	public Font getFont()
	{
		return m_internalDialog.getFont();
	}

	public void remove(MenuComponent a_component)
	{
		m_internalDialog.remove(a_component);
	}

	/**
	 * @param a_event an Event
	 * @return if the event has been dispatched successfully
	 * @deprecated As of JDK version 1.1 replaced by dispatchEvent(AWTEvent).
	 */
	public boolean postEvent(Event a_event)
	{
		return m_internalDialog.postEvent(a_event);
	}

	/**
	 * Defines the reaction of this dialog on a click on the close button in the dialog's title bar.
	 * @param a_windowAction insert an element of javax.swing.WindowConstants
	 * @see javax.swing.WindowConstants
	 */
	public final void setDefaultCloseOperation(int a_windowAction)
	{
		m_defaultCloseOperation = a_windowAction;
	}

	/**
	 * Returns the reaction of this dialog on a click on the close button in the dialog's title bar.
	 * @return a javax.swing.WindowConstant
	 * @see javax.swing.WindowConstants
	 */
	public final int getDefaultCloseOperation()
	{
		return m_defaultCloseOperation;
	}

	/**
	 * Adds a WindowListener to the dialog.
	 * @param a_listener a WindowListener
	 * @see java.awt.event.WindowListener
	 */
	public final void addWindowListener(WindowListener a_listener)
	{
		m_windowListeners.addElement(a_listener);
		m_internalDialog.addWindowListener(a_listener);
	}

	/**
	 * Adds a Componentistener to the dialog.
	 * @param a_listener a ComponentListener
	 * @see java.awt.event.ComponentListener
	 */
	public final void addComponentListener(ComponentListener a_listener)
	{
		m_internalDialog.addComponentListener(a_listener);
	}

	/**
	 * Removes a specific ComponentListener from the dialog.
	 * @param a_listener a ComponentListener
	 * @see java.awt.event.ComponentListener
	 */
	public final void removeComponentListener(ComponentListener a_listener)
	{
		m_internalDialog.removeComponentListener(a_listener);
	}

	/**
	 * Removes a specific WindowListener from the dialog.
	 * @param a_listener a WindowListener
	 * @see java.awt.event.WindowListener
	 */
	public final void removeWindowListener(WindowListener a_listener)
	{
		m_windowListeners.removeElement(a_listener);
		m_internalDialog.removeWindowListener(a_listener);
	}

	/**
	 * Sets the dialog to the optimal size.
	 */
	public final void pack()
	{
		m_internalDialog.pack();
	}

	/**
	 * Returns the internal dialog of a JAPDialog or null if there is none.
	 * @param a_dialog a JAPDialog
	 * @return the internal dialog of a JAPDialog or null if there is none
	 */
	private static Window getInternalDialog(JAPDialog a_dialog)
	{
		if (a_dialog == null)
		{
			return null;
		}

		return a_dialog.m_internalDialog;
	}

	/**
	 * Finds the first parent that is a window.
	 * @param a_parentComponent a Component
	 * @return the first parent that is a window
	 */
	private static Window getParentWindow(Component a_parentComponent)
	{
		Component parentComponent = a_parentComponent;
		while (parentComponent != null && ! (parentComponent instanceof Window))
		{

			parentComponent = parentComponent.getParent();
		}
		return (Window)parentComponent;
	}

	private static class WindowClosingAdapter extends WindowAdapter
	{
		private JAPDialog m_dialog;

		public WindowClosingAdapter(JAPDialog a_dialog)
		{
			m_dialog = a_dialog;
		}

		public void windowClosing(WindowEvent a_event)
		{
			if (m_dialog.getDefaultCloseOperation() == DISPOSE_ON_CLOSE)
			{
				try
				{
					m_dialog.dispose();
				}
				catch (IllegalMonitorStateException a_e)
				{
					LogHolder.log(LogLevel.DEBUG, LogType.GUI, a_e);
				}
			}
			else if (m_dialog.getDefaultCloseOperation() == HIDE_ON_CLOSE)
			{
				m_dialog.setVisible(false);
			}
			else
			{
				/*
				 * This covers the case that, in old JDKs, a click on the close icon will always close
				 * the dialog, not regarding which closing pocily has been set. As it is not possible to
				 * catch this event if we do not own the AWT event thread in the setVisible() method,
				 * we have to make the internal dialog visible again in this place.
				 * In never JDKs >= 1.3 all WindowListeners are removed from the internal dialog, so this
				 * problem does not come up there.
				 * Notice: This bug causes a little flickering, but this should not harm.
				 */
				if (!m_dialog.isVisible())
				{
					m_dialog.m_internalDialog.setVisible(true);
					LogHolder.log(LogLevel.INFO, LogType.GUI, "Fixed old JRE dialog closing bug.");
				}
			}
		}
	}

	private static class LinkedInformationClickListener extends MouseAdapter
	{
		private ILinkedInformation m_linkedInformation;

		public LinkedInformationClickListener(ILinkedInformation a_linkedInformation)
		{
			m_linkedInformation = a_linkedInformation;
		}

		public void mouseClicked(MouseEvent a_event)
		{
			m_linkedInformation.openLink();
		}
	}

	private static class PreferredWidthBoxPanel extends JPanel
	{
		private int m_preferredWidth;

		public PreferredWidthBoxPanel()
		{
			BoxLayout layout;
			m_preferredWidth = 0;
			layout = new BoxLayout(this, BoxLayout.Y_AXIS);
			setLayout(layout);
		}
		public void setPreferredWidth(int a_preferredWidth)
		{
			m_preferredWidth = a_preferredWidth;
		}


		public Dimension getPreferredSize()
		{
			if (m_preferredWidth <= 0)
			{
				return super.getPreferredSize();
			}
			return new Dimension(m_preferredWidth, (int)super.getPreferredSize().height);
		}
	}

	/**
	 * Finds the first focusable Component in a Container and sets the focus on it.
	 * @param a_container a Container
	 * @return if a Component has been focused
	 */
	private static boolean requestFocusForFirstFocusableComponent(Container a_container)
	{
		// see if isFocusable() is available; then we do not need this patch
		try
		{
			Container.class.getMethod("isFocusable", null).invoke(a_container, null);
			return true;
		}
		catch (Exception a_e)
		{
		}

		for (int i = 0; i < a_container.getComponentCount(); i++)
		{
			if (a_container.getComponent(i) instanceof Container)
			{
				if (requestFocusForFirstFocusableComponent((Container)a_container.getComponent(i)))
				{
					return true;
				}
			}

			if (a_container.getComponent(i).isFocusTraversable())
			{
				a_container.getComponent(i).requestFocus();
				return true;
			}
		}
		return false;
	}

	private void setVisibleInternal(boolean a_bVisible)
	{
		if (isVisible() && m_bBlockParentWindow && !a_bVisible)
		{
			m_parentWindow.setEnabled(true);
			m_parentWindow.setVisible(true);
		}

		m_bBlockParentWindow = (a_bVisible && m_bModal);
		if (m_bBlockParentWindow)
		{
			// must be set disabled before showing the dialog
			m_parentWindow.setEnabled(false);
		}

		synchronized (m_internalDialog.getTreeLock())
		{
			m_internalDialog.setVisible(a_bVisible);
			if (a_bVisible)
			{
				// fix for JDK 1.1.8 that does not auto-focus the first focusable component
				requestFocusForFirstFocusableComponent(m_internalDialog.getContentPane());
			}
			m_internalDialog.getTreeLock().notifyAll();
		}

		if (m_bBlockParentWindow)
		{
			try
			{
				BlockedWindowDeactivationAdapter windowDeactivationAdapter =
					new BlockedWindowDeactivationAdapter();

				m_parentWindow.addWindowListener(windowDeactivationAdapter);
				m_parentWindow.addFocusListener(windowDeactivationAdapter);

				if (SwingUtilities.isEventDispatchThread())
				{
					EventQueue theQueue = m_internalDialog.getToolkit().getSystemEventQueue();

					while (isVisible())
					{
						AWTEvent event = theQueue.getNextEvent();

						if (m_bBlockParentWindow && m_parentWindow.isEnabled())
						{
							// another dialog has enabled the parent; set it back to diabled
							m_parentWindow.setEnabled(false);
						}
						Class classActiveEvent;
						try
						{
							// java.awt.ActiveEvent is not known in JDKs < 1.2
							classActiveEvent = Class.forName("java.awt.ActiveEvent");
						}
						catch (ClassNotFoundException a_e)
						{
							classActiveEvent = null;
						}
						Object src = event.getSource();
						if (src == m_internalDialog && event instanceof WindowEvent)
						{
							if ( ( (WindowEvent) event).getID() == WindowEvent.WINDOW_CLOSING)
							{
								for (int i = 0; i < m_windowListeners.size(); i++)
								{
									( (WindowListener) m_windowListeners.elementAt(i)).windowClosing(
										(WindowEvent)event);
								}

								/*
								 * Hide this event from the internal dialog. This removes the flimmering
								 * effect that occurs when the internal dialog is closed before enabling
								 * the parent window.
								 */
								continue;
							}
						}
						if (classActiveEvent != null && classActiveEvent.isInstance(event))
						{
							// ((ActiveEvent) event).dispatch();
							classActiveEvent.getMethod("dispatch", null).invoke(event, null);
						}
						else if (src instanceof Component)
						{
							try
							{
								( (Component) src).dispatchEvent(event);
							}
							catch (IllegalMonitorStateException a_e)
							{
								LogHolder.log(LogLevel.NOTICE, LogType.GUI, a_e);
							}
						}
						else if (src instanceof MenuComponent)
						{
							( (MenuComponent) src).dispatchEvent(event);
						}
					}
				}
				else
				{
					/**
					 * Dialogs going in here are less secure against 'conflicting' components that enable
					 * the parent. These event are only handled by focusGained() and windowActivated().
					 */
					synchronized (m_internalDialog.getTreeLock())
					{
						while (isVisible())
						{
							try
							{
								m_internalDialog.getTreeLock().wait();
							}
							catch (InterruptedException e)
							{
								break;
							}
						}
						m_internalDialog.getTreeLock().notifyAll();
					}
				}
				m_parentWindow.removeWindowListener(windowDeactivationAdapter);
				m_parentWindow.removeFocusListener(windowDeactivationAdapter);
			}
			catch (Exception a_e)
			{
				LogHolder.log(LogLevel.EXCEPTION, LogType.GUI, a_e);
			}

			if (!m_parentWindow.isEnabled())
			{
				m_bBlockParentWindow = false;
				m_parentWindow.setEnabled(true);
				m_parentWindow.setVisible(true);
			}
		}

		synchronized (m_internalDialog.getTreeLock())
		{
			m_internalDialog.getTreeLock().notifyAll();
		}
	}

	private class BlockedWindowDeactivationAdapter extends WindowAdapter implements FocusListener
	{
		public void windowActivated(WindowEvent e)
		{
			deactivate(e.getWindow());
		}

		public void focusGained(FocusEvent a_event)
		{
			deactivate((Window)a_event.getComponent());
		}

		public void focusLost(FocusEvent a_event)
		{
		}

		private void deactivate(Window a_window)
		{
			if (m_bBlockParentWindow)
			{
				requestFocus();
				if (a_window.isEnabled())
				{
					a_window.setEnabled(false);
				}
			}
		}
	}
}
