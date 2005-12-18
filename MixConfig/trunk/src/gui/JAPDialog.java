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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowListener;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.text.View;
import javax.swing.JTextPane;

import logging.LogHolder;
import logging.LogLevel;
import logging.LogType;


/**
 * This is the generic implementation for a modal, user resizeable dialog. Use the root pane and content pane
 * (getRootPane() and getContentPane() methods) for customization.
 * @see javax.swing.JDialog
 * @see javax.swing.JOptionPane
 *
 * @author Rolf Wendolsky
 */
public class JAPDialog
{
	public static final double GOLDEN_RATIO_PHI = (1.0 + Math.sqrt(5.0)) / 2.0;
	private static final int UNLIMITED_HEIGHT = 1000;

	private static final String MSG_INFO = JAPDialog.class.getName() + "_info";
	private static final String MSG_CONFIRMATION = JAPDialog.class.getName() + "_confirmation";
	private static final String MSG_ERROR_TITLE = JAPDialog.class.getName() + "_error_title";
	private static final String MSG_ERROR_UNKNOWN = JAPDialog.class.getName() + "_error_unknown";
	private static final String MSG_ERROR_UNDISPLAYABLE = JAPDialog.class.getName() + "error_undisplayable";

	private boolean m_bDisplayable = true;
	private boolean m_bLocationSetManually = false;

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
	 * Creates a new instance of JAPDialog. It is user-resizable and modal.
	 * @param a_parentComponent The parent component for this dialog. If it is null or the parent
	 *                          component is not within a frame, the dialog's parent frame is the
	 *                          default frame.
	 * @param a_strTitle The title String for this dialog.
	 * @param a_bModal if the dialog should be modal
	 */
	public JAPDialog(Component a_parentComponent, String a_strTitle, boolean a_bModal)
	{
		m_parentComponent = a_parentComponent;
		JOptionPane optionPane = new JOptionPane();
		m_internalDialog = optionPane.createDialog(a_parentComponent, a_strTitle);
		m_internalDialog.getContentPane().removeAll();
		m_internalDialog.setResizable(true);
		m_internalDialog.setModal(a_bModal);
		m_parentWindow = getParentWindow(a_parentComponent);
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
	 * Creates a new instance of JAPDialog. It is user-resizable and modal.
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
	 * Creates a new instance of JAPDialog. It is user-resizable and modal.
	 * @param a_parentDialog The parent dialog for this dialog. If it is null,
	 *                       the dialog's parent frame is the default frame.
	 * @param a_strTitle The title String for this dialog.
	 */
	public JAPDialog(JAPDialog a_parentDialog, String a_strTitle)
	{
		this(getInternalDialog(a_parentDialog), a_strTitle);
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
	 * Displays an info message dialog. Words are wrapped automatically if a message line is too long.
	 * @param a_parentDialog The parent dialog for this dialog. If it is null,
	 *                       the dialog's parent frame is the default frame.
	 * @param a_message The message to be displayed
	 */
	public static void showInfoDialog(JAPDialog a_parentDialog, String a_message)
	{
		showInfoDialog(getInternalDialog(a_parentDialog), a_message);
	}

	/**
	 * Displays an info message dialog. Words are wrapped automatically if a message line is too long.
	 * @param a_parentDialog The parent dialog for this dialog. If it is null,
	 *                       the dialog's parent frame is the default frame.
	 * @param a_message The message to be displayed
	 * @param a_linkedInformation a clickable information message that is appended to the text
	 */
	public static void showInfoDialog(JAPDialog a_parentDialog, String a_message,
									  ILinkedInformation a_linkedInformation)
	{
		showInfoDialog(getInternalDialog(a_parentDialog), a_message, a_linkedInformation);
	}

	/**
	 * Displays an info message dialog. Words are wrapped automatically if a message line is too long.
	 * @param a_parentComponent The parent component for this dialog. If it is null or the parent
	 *                          component is not within a frame, the dialog's parent frame is the
	 *                          default frame.
	 * @param a_message The message to be displayed
	 */
	public static void showInfoDialog(Component a_parentComponent, String a_message)
	{
		showInfoDialog(a_parentComponent, JAPMessages.getString(MSG_INFO), a_message, (Icon)null);
	}

	/**
	 * Displays an info message dialog. Words are wrapped automatically if a message line is too long.
	 * @param a_parentComponent The parent component for this dialog. If it is null or the parent
	 *                          component is not within a frame, the dialog's parent frame is the
	 *                          default frame.
	 * @param a_message The message to be displayed
	 * @param a_linkedInformation a clickable information message that is appended to the text
	 */
	public static void showInfoDialog(Component a_parentComponent, String a_message,
									  ILinkedInformation a_linkedInformation)
	{
		showInfoDialog(a_parentComponent, JAPMessages.getString(MSG_INFO), a_message, (Icon)null,
					   a_linkedInformation);
	}

	/**
	 * Displays an info message dialog. Words are wrapped automatically if a message line is too long.
	 * @param a_parentDialog The parent dialog for this dialog. If it is null,
	 *                       the dialog's parent frame is the default frame.
	 * @param a_title The title of the message dialog
	 * @param a_message The message to be displayed
	 */
	public static void showInfoDialog(JAPDialog a_parentDialog, String a_title, String a_message)
	{
		showInfoDialog(getInternalDialog(a_parentDialog), a_title, a_message);
	}

	/**
	 * Displays an info message dialog. Words are wrapped automatically if a message line is too long.
	 * @param a_parentDialog The parent dialog for this dialog. If it is null,
	 *                       the dialog's parent frame is the default frame.
	 * @param a_title The title of the message dialog
	 * @param a_message The message to be displayed
	 * @param a_linkedInformation a clickable information message that is appended to the text
	 */
	public static void showInfoDialog(JAPDialog a_parentDialog, String a_title, String a_message,
									  ILinkedInformation a_linkedInformation)
	{
		showInfoDialog(getInternalDialog(a_parentDialog), a_title, a_message, a_linkedInformation);
	}

	/**
	 * Displays an info message dialog. Words are wrapped automatically if a message line is too long.
	 * @param a_parentComponent The parent component for this dialog. If it is null or the parent
	 *                          component is not within a frame, the dialog's parent frame is the
	 *                          default frame.
	 * @param a_title The title of the message dialog
	 * @param a_message The message to be displayed
	 */
	public static void showInfoDialog(Component a_parentComponent, String a_title, String a_message)
	{
		showInfoDialog(a_parentComponent, a_title, a_message, (Icon)null);
	}

	/**
	 * Displays an info message dialog. Words are wrapped automatically if a message line is too long.
	 * @param a_parentComponent The parent component for this dialog. If it is null or the parent
	 *                          component is not within a frame, the dialog's parent frame is the
	 *                          default frame.
	 * @param a_title The title of the message dialog
	 * @param a_message The message to be displayed
	 * @param a_linkedInformation a clickable information message that is appended to the text
	 */
	public static void showInfoDialog(Component a_parentComponent, String a_title, String a_message,
									  ILinkedInformation a_linkedInformation)
	{
		showInfoDialog(a_parentComponent, a_title, a_message, null, a_linkedInformation);
	}

	/**
	 * Displays an info message dialog. Words are wrapped automatically if a message line is too long.
	 * @param a_parentDialog The parent dialog for this dialog. If it is null,
	 *                       the dialog's parent frame is the default frame.
	 * @param a_message The message to be displayed
	 * @param a_icon an icon that will be displayed on the dialog
	 */
	public static void showInfoDialog(JAPDialog a_parentDialog, String a_message, Icon a_icon)
	{
		showInfoDialog(getInternalDialog(a_parentDialog), a_message, a_icon);
	}

	/**
	 * Displays an info message dialog. Words are wrapped automatically if a message line is too long.
	 * @param a_parentDialog The parent dialog for this dialog. If it is null,
	 *                       the dialog's parent frame is the default frame.
	 * @param a_message The message to be displayed
	 * @param a_icon an icon that will be displayed on the dialog
	 * @param a_linkedInformation a clickable information message that is appended to the text
	 */
	public static void showInfoDialog(JAPDialog a_parentDialog, String a_message, Icon a_icon,
									  ILinkedInformation a_linkedInformation)
	{
		showInfoDialog(getInternalDialog(a_parentDialog), a_message, a_icon, a_linkedInformation);
	}

	/**
	 * Displays an info message dialog. Words are wrapped automatically if a message line is too long.
	 * @param a_parentComponent The parent component for this dialog. If it is null or the parent
	 *                          component is not within a frame, the dialog's parent frame is the
	 *                          default frame.
	 * @param a_message The message to be displayed
	 * @param a_icon an icon that will be displayed on the dialog
	 */
	public static void showInfoDialog(Component a_parentComponent, String a_message, Icon a_icon)
	{
		showInfoDialog(a_parentComponent, JAPMessages.getString(MSG_INFO), a_message, a_icon);
	}

	/**
	 * Displays an info message dialog. Words are wrapped automatically if a message line is too long.
	 * @param a_parentComponent The parent component for this dialog. If it is null or the parent
	 *                          component is not within a frame, the dialog's parent frame is the
	 *                          default frame.
	 * @param a_message The message to be displayed
	 * @param a_icon an icon that will be displayed on the dialog
	 * @param a_linkedInformation a clickable information message that is appended to the text
	 */
	public static void showInfoDialog(Component a_parentComponent, String a_message, Icon a_icon,
									  ILinkedInformation a_linkedInformation)
	{
		showInfoDialog(a_parentComponent, JAPMessages.getString(MSG_INFO), a_message, a_icon,
			a_linkedInformation);
	}

	/**
	 * Displays an info message dialog. Words are wrapped automatically if a message line is too long.
	 * @param a_parentDialog The parent dialog for this dialog. If it is null,
	 *                       the dialog's parent frame is the default frame.
	 * @param a_title The title of the message dialog
	 * @param a_message The message to be displayed
	 * @param a_icon an icon that will be displayed on the dialog
	 */
	public static void showInfoDialog(JAPDialog a_parentDialog, String a_title, String a_message, Icon a_icon)
	{
		showInfoDialog(getInternalDialog(a_parentDialog), a_title, a_message, a_icon);
	}

	/**
	 * Displays an info message dialog. Words are wrapped automatically if a message line is too long.
	 * @param a_parentDialog The parent dialog for this dialog. If it is null,
	 *                       the dialog's parent frame is the default frame.
	 * @param a_title The title of the message dialog
	 * @param a_message The message to be displayed
	 * @param a_icon an icon that will be displayed on the dialog
	 * @param a_linkedInformation a clickable information message that is appended to the text
	 */
	public static void showInfoDialog(JAPDialog a_parentDialog, String a_title, String a_message, Icon a_icon,
									  ILinkedInformation a_linkedInformation)
	{
		showInfoDialog(getInternalDialog(a_parentDialog), a_title, a_message, a_icon, a_linkedInformation);
	}

	/**
	 * Displays an info message dialog. Words are wrapped automatically if a message line is too long.
	 * @param a_parentComponent The parent component for this dialog. If it is null or the parent
	 *                          component is not within a frame, the dialog's parent frame is the
	 *                          default frame.
	 * @param a_title The title of the message dialog
	 * @param a_message The message to be displayed
	 * @param a_icon an icon that will be displayed on the dialog
	 */
	public static void showInfoDialog(Component a_parentComponent, String a_title, String a_message,
									  Icon a_icon)
	{
		showMessageDialog(a_parentComponent, a_title, a_message,
						  JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, a_icon, null);
	}

	/**
	 * Displays an info message dialog. Words are wrapped automatically if a message line is too long.
	 * @param a_parentComponent The parent component for this dialog. If it is null or the parent
	 *                          component is not within a frame, the dialog's parent frame is the
	 *                          default frame.
	 * @param a_title The title of the message dialog
	 * @param a_message The message to be displayed
	 * @param a_icon an icon that will be displayed on the dialog
	 * @param a_linkedInformation a clickable information message that is appended to the text
	 */
	public static void showInfoDialog(Component a_parentComponent, String a_title, String a_message,
									  Icon a_icon, ILinkedInformation a_linkedInformation)
	{
		showMessageDialog(a_parentComponent, a_title, a_message, JOptionPane.INFORMATION_MESSAGE,
						  JOptionPane.DEFAULT_OPTION, a_icon, a_linkedInformation);
	}

	/**
	 * Displays an info message dialog. Words are wrapped automatically if a message line is too long.
	 * @param a_parentComponent The parent component for this dialog. If it is null or the parent
	 *                          component is not within a frame, the dialog's parent frame is the
	 *                          default frame.
	 * @param a_title The title of the message dialog
	 * @param a_message The message to be displayed
	 * @param a_icon an icon that will be displayed on the dialog
	 * @param a_messageType use the message types from JOptionPane
	 * @param a_optionType use the option types from JOptionPane
	 * @param a_linkedInformation a clickable information message that is appended to the text
	 * @return the value the user has selected. UNINITIALIZED_VALUE implies
	 * the user has not yet made a choice, null means the user closed the window with
	 * out choosing anything. Otherwise the returned value will be one of the options defined in this object.
	 * @see javax.swing.JOptionPane
	 */
	public static Object showMessageDialog(Component a_parentComponent, String a_title, String a_message,
										   int a_messageType, int a_optionType, Icon a_icon,
										   ILinkedInformation a_linkedInformation)
	{
		JDialog dialog;
		JOptionPane pane;
		JComponent contentPane;
		String message;
		String strLinkedInformation;
		JAPHtmlMultiLineLabel label;
		PreferredWidthBoxPanel dummyBox;

		if (a_message == null)
		{
			a_message = "";
		}
		message = a_message;

		if (a_linkedInformation != null && a_linkedInformation.getMessage() != null &&
			a_linkedInformation.getMessage().trim().length() > 0)
		{
			strLinkedInformation = "<a href=\"\">" + a_linkedInformation.getMessage() + "</a>";
			message += "<br>" + strLinkedInformation;
		}
		else
		{
			strLinkedInformation = null;
		}

		/*
		 * Set the dialog parameters and get its label and content pane.
		 */
		label = new JAPHtmlMultiLineLabel(message);
		dialog = new JOptionPane(label, a_messageType, a_optionType, a_icon).createDialog(
			  a_parentComponent, a_title);
		// trick: a dialog's content pane is always a JComponent; it is needed to set the min/max size
		contentPane = (JComponent)dialog.getContentPane();


		// get the minimum width and height that is needed to display this dialog without any text
		Dimension minDimension = new JOptionPane("", a_messageType, a_optionType, a_icon).
			createDialog(a_parentComponent, a_title).getContentPane().getSize();

		/**
		 * Calculate the optimal dialog size with respect to the golden ratio.
		 * The width defines the longer side.
		 */
		Dimension bestDimension = null;
		double currentDelta;
		double bestDelta;
		int currentWidth;
		int bestWidth;
		int failed;

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
		for (int i = 0; i < 5; i++)
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
			contentPane.setMaximumSize(new Dimension(currentWidth, UNLIMITED_HEIGHT));
			dummyBox.setPreferredWidth(currentWidth);
			dialog.setContentPane(dummyBox);
			dialog.pack();
			( (View) label.getClientProperty("html")).setSize((float) label.getWidth(), UNLIMITED_HEIGHT);
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
		label = new JAPHtmlMultiLineLabel(a_message);
		dummyBox.add(label);
		if (strLinkedInformation != null)
		{
			JComponent linkLabel;
			if (a_linkedInformation.isCopyAllowed())
			{   /** @todo this is not nice in most of the old JDKs) */
				JTextPane textPane = GUIUtils.createSelectableAndResizeableLabel(dummyBox);
				textPane.setText(a_linkedInformation.getMessage());
				textPane.setFont(label.getFont());

				//System.out.println(( (View) label.getClientProperty("html")).get);
				//System.out.println(textPane.getFont().getSize());
				//System.out.println(label.getFont().getSize());
				textPane.setForeground(java.awt.Color.blue);
				linkLabel = textPane;
			}
			else
			{
				linkLabel = new JAPHtmlMultiLineLabel(strLinkedInformation);
			}

			linkLabel.addMouseListener(new LinkedInformationClickListener(a_linkedInformation, dialog));
			dummyBox.add(linkLabel);
		}

		pane =  new JOptionPane(dummyBox, a_messageType, a_optionType, a_icon);
		dialog = pane.createDialog(a_parentComponent, a_title);
		((JComponent)dialog.getContentPane()).setPreferredSize(bestDimension);
		dialog.pack();
		if (bestDelta != getGoldenRatioDelta(dialog))
		{
			LogHolder.log(LogLevel.NOTICE, LogType.GUI, "Calculated dialog size differs from real size!");
		}
		//System.out.println(getGoldenRatioDelta(dialog));
		dialog.setLocationRelativeTo(a_parentComponent);
		dialog.setResizable(false);
		dialog.setVisible(true);
		return pane.getValue();
	}

	/**
	 * Displays a message dialog that asks the user for a confirmation.
	 * Words are wrapped automatically if a message line is too long.
	 * @param a_parentDialog The parent dialog for this dialog. If it is null,
	 *                       the dialog's parent frame is the default frame.
	 * @param a_message The message to be displayed
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
	 * @param a_message The message to be displayed
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
	 * @param a_message The message to be displayed
	 * @return true if the answer was 'yes'; fale otherwise
	 */
	public static boolean showYesNoDialog(Component a_parentComponent, String a_message)
	{
		return showYesNoDialog(a_parentComponent, null, a_message);
	}

	/**
	 * Displays a message dialog that asks the user for a confirmation.
	 * Words are wrapped automatically if a message line is too long.
	 * @param a_parentComponent The parent component for this dialog. If it is null or the parent
	 *                          component is not within a frame, the dialog's parent frame is the
	 *                          default frame.
	 * @param a_message The message to be displayed
	 * @param a_linkedInformation a clickable information message that is appended to the text
	 * @return true if the answer was 'yes'; fale otherwise
	 */
	public static boolean showYesNoDialog(Component a_parentComponent, String a_message,
										  ILinkedInformation a_linkedInformation)
	{
		return showYesNoDialog(a_parentComponent, null, a_message, a_linkedInformation);
	}

	/**
	 * Displays a message dialog that asks the user for a confirmation.
	 * Words are wrapped automatically if a message line is too long.
	 * @param a_parentDialog The parent dialog for this dialog. If it is null,
	 *                       the dialog's parent frame is the default frame.
	 * @param a_title The title of the message dialog
	 * @param a_message The message to be displayed
	 * @return true if the answer was 'yes'; fale otherwise
	 */
	public static boolean showYesNoDialog(JAPDialog a_parentDialog, String a_title, String a_message)
	{
		return showYesNoDialog(getInternalDialog(a_parentDialog), a_title, a_message);
	}

	/**
	 * Displays a message dialog that asks the user for a confirmation.
	 * Words are wrapped automatically if a message line is too long.
	 * @param a_parentDialog The parent dialog for this dialog. If it is null,
	 *                       the dialog's parent frame is the default frame.
	 * @param a_title The title of the message dialog
	 * @param a_message The message to be displayed
	 * @param a_linkedInformation a clickable information message that is appended to the text
	 * @return true if the answer was 'yes'; fale otherwise
	 */
	public static boolean showYesNoDialog(JAPDialog a_parentDialog, String a_title, String a_message,
										  ILinkedInformation a_linkedInformation)
	{
		return showYesNoDialog(getInternalDialog(a_parentDialog), a_title, a_message, a_linkedInformation);
	}

	/**
	 * Displays a message dialog that asks the user for a confirmation.
	 * Words are wrapped automatically if a message line is too long.
	 * @param a_parentComponent The parent component for this dialog. If it is null or the parent
	 *                          component is not within a frame, the dialog's parent frame is the
	 *                          default frame.
	 * @param a_title The title of the message dialog
	 * @param a_message The message to be displayed
	 * @return true if the answer was 'yes'; fale otherwise
	 */
	public static boolean showYesNoDialog(Component a_parentComponent, String a_title, String a_message)
	{
		return showYesNoDialog(a_parentComponent, a_title, a_message, null);
	}

	/**
	 * Displays a message dialog that asks the user for a confirmation.
	 * Words are wrapped automatically if a message line is too long.
	 * @param a_parentComponent The parent component for this dialog. If it is null or the parent
	 *                          component is not within a frame, the dialog's parent frame is the
	 *                          default frame.
	 * @param a_title The title of the message dialog
	 * @param a_message The message to be displayed
	 * @param a_linkedInformation a clickable information message that is appended to the text
	 * @return true if the answer was 'yes'; fale otherwise
	 */
	public static boolean showYesNoDialog(Component a_parentComponent, String a_title, String a_message,
										  ILinkedInformation a_linkedInformation)
	{
		Object response;

		if (a_title == null || a_title.trim().length() == 0)
		{
			a_title = JAPMessages.getString(MSG_CONFIRMATION);
		}
		response = showMessageDialog(a_parentComponent, a_title, a_message, JOptionPane.QUESTION_MESSAGE,
									 JOptionPane.YES_NO_OPTION, null, a_linkedInformation);
		if (response == null || !(response instanceof Integer))
		{
			return false;
		}

		return ((Integer)response).intValue() == JOptionPane.YES_OPTION;
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
		showErrorDialog(a_parentDialog, null, a_message, a_logType);
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
	public static void showErrorDialog(JAPDialog a_parentDialog, Throwable a_throwable, String a_message,
									   int a_logType)
	{
		showErrorDialog(getInternalDialog(a_parentDialog), a_throwable, a_message, a_logType);
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
	public static void showErrorDialog(JAPDialog a_parentDialog, Throwable a_throwable, String a_title,
										String a_message, int a_logType)
	{
		showErrorDialog(getInternalDialog(a_parentDialog), a_throwable, a_title, a_message, a_logType);
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
		showErrorDialog(a_parentComponent, null, a_message, a_logType);
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
	public static void showErrorDialog(Component a_parentComponent, Throwable a_throwable, String a_message,
									   int a_logType)
	{
		showErrorDialog(a_parentComponent, a_throwable, null, a_message, a_logType);
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
	public static void showErrorDialog(Component a_parentComponent, Throwable a_throwable, String a_title,
										String a_message, int a_logType)
	{
		a_message = retrieveErrorMessage(a_throwable, a_message);
		LogHolder.log(LogLevel.ERR, a_logType, a_message, true);
		if (a_throwable != null)
		{
			// the exception is only shown in debug mode
			LogHolder.log(LogLevel.DEBUG, a_logType, a_throwable);
		}

		try
		{
			if (a_title == null || a_title.trim().length() == 0)
			{
				a_title = JAPMessages.getString(MSG_ERROR_TITLE);
			}
			showMessageDialog(a_parentComponent, a_title, a_message,
							  JOptionPane.ERROR_MESSAGE, JOptionPane.DEFAULT_OPTION, null, null);
		}
		catch (Exception e)
		{
			LogHolder.log(LogLevel.EXCEPTION, LogType.GUI, JAPMessages.getString(MSG_ERROR_UNDISPLAYABLE));
			LogHolder.log(LogLevel.EXCEPTION, LogType.GUI, e);
		}
	}

	/**
	 * Classes of this type are used to append a clickable message at the end of a dialog message.
	 * You may also allow to copy the message to the cip board and define any after-click-action
	 * that you want.
	 */
	public static interface ILinkedInformation
	{
		/**
		 * Returns the information message.
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
	}

	/**
	 * This is an example implementation of ILinkedInformation. It registers a help context in the
	 * dialog that is opened when the user clicks on a "More info..." or a self-defined String.
	 */
	public static final class LinkedHelpContext implements ILinkedInformation
	{
		private static final String MSG_MORE_INFO = LinkedHelpContext.class.getName() + "_more_info";

		private String m_strMessage;
		private String m_strHelpContext;

		public LinkedHelpContext(String a_strHelpContext, String a_strMessage)
		{
			if (a_strMessage == null || a_strMessage.trim().length() == 0)
			{
				a_strMessage = JAPMessages.getString(MSG_MORE_INFO);
			}
			m_strHelpContext = a_strHelpContext;
			m_strMessage = a_strMessage;
		}

		public LinkedHelpContext(String a_strHelpContext)
		{
			this(a_strHelpContext, null);
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
			JAPHelp.getInstance().getContextObj().setContext(m_strHelpContext);
			JAPHelp.getInstance().setVisible(true);
			JAPHelp.getInstance().requestFocus();
		}
		/**
		 * This makes no sense and is not allowed
		 * @return boolean
		 */
		public boolean isCopyAllowed()
		{
			return false;
		}
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
	public final JComponent getContentPane()
	{
		return (JComponent)m_internalDialog.getContentPane();
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
			if (!m_bDisplayable)
			{
				throw new RuntimeException("Dialog has been disposed and cannot be made visible!");
			}
			else
			{
				if (!m_bLocationSetManually && !isVisible())
				{
					if (a_bCenterOnParentComponent)
					{
						m_internalDialog.setLocationRelativeTo(m_parentComponent);
					}
					else
					{
						GUIUtils.positionRightUnderWindow(m_internalDialog, getOwner());
					}
				}
			}
		}
		m_internalDialog.setVisible(a_bVisible);
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
	 * Defines the dialog as modal or not.
	 * @param a_bModal true if the dialog should be modal; false otherwise
	 */
	public final void setModal(boolean a_bModal)
	{
		m_internalDialog.setModal(a_bModal);
	}

	/**
	 * Disposes the dialog (set it to invisible and releases all resources).
	 */
	public final void dispose()
	{
		m_internalDialog.dispose();
	}

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
		setLocationCenteredOn(getParentComponent());
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

	/**
	 * Defines the reaction of this dialog on a klick on the close button in the dialog's title bar.
	 * @param a_windowAction insert an element of javax.swing.WindowConstants
	 * @see javax.swing.WindowConstants
	 */
	public final void setDefaultCloseOperation(int a_windowAction)
	{
		m_internalDialog.setDefaultCloseOperation(a_windowAction);
	}

	/**
	 * Adds a WindowListener to the dialog.
	 * @param a_listener a WindowListener
	 * @see java.awt.event.WindowListener
	 */
	public final void addWindowListener(WindowListener a_listener)
	{
		m_internalDialog.addWindowListener(a_listener);
	}

	/**
	 * Removes a specific WindowListener from the dialog.
	 * @param a_listener a WindowListener
	 * @see java.awt.event.WindowListener
	 */
	public final void removeWindowListener(WindowListener a_listener)
	{
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
	 * Retrieves an error message from a Throwable and a message String that may be shown to the
	 * user. By default, this is the given message. If no message is given, it is tried to get the error
	 * message from the Throwable.
	 * @param a_e a Throwable (may be null)
	 * @param a_message an error message (may be null)
	 * @return the retrieved error message
	 */
	private static String retrieveErrorMessage(Throwable a_e, String a_message)
	{
		if (a_message == null)
		{
			if (a_e == null || a_e.getMessage() == null)
			{
				a_message = JAPMessages.getString(MSG_ERROR_UNKNOWN);
			}
			else
			{
				a_message = a_e.getMessage();
			}
		}

		return a_message;
	}

	/**
	 * Finds the first parent that is a window.
	 * @param a_parentComponent a Component
	 * @return the first parent that is a window
	 */
	private static Window getParentWindow(Component a_parentComponent)
	{
		while (a_parentComponent != null && ! (a_parentComponent instanceof Window))
		{
			a_parentComponent = a_parentComponent.getParent();
		}
		return (Window)a_parentComponent;
	}

	private static class LinkedInformationClickListener extends MouseAdapter
	{
		private ILinkedInformation m_linkedInformation;
		private JDialog m_dialog;

		public LinkedInformationClickListener(ILinkedInformation a_linkedInformation,
											  JDialog a_dialog)
		{
			m_linkedInformation = a_linkedInformation;
			m_dialog = a_dialog;
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
}
