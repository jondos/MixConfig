package gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import logging.LogHolder;
import logging.LogLevel;
import logging.LogType;

/**
 * Date panel class
 * @author Johannes Renner
 */
public class DatePanel extends JPanel implements ItemListener
{
	/** Vector containing all DateListeners */
	private Vector<DateListener> m_dateListeners = new Vector<DateListener>();
	
    /** The number of days in each month */	
	private final int[] m_daysPerMonth = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
	
	/** Array containing all months */
	private final String[] m_months = {"January", "February", "March", "April", "May", "June", "July", 
		                               "August", "September", "October", "November", "December"};
	
	/** JComboBoxes */
	private JComboBox m_cbDay;
	private JComboBox m_cbMonth;
    private JComboBox m_cbYear;
	
	/** Use models here */
	private DefaultComboBoxModel m_modelDay;
	private DefaultComboBoxModel m_modelMonth;
	private DefaultComboBoxModel m_modelYear;
	
	/** Calendar object for detecting date changes */
	private Calendar m_cal = null;
	private boolean m_bSuppressEvents = false;
	
	/** 
	 * Constructor
	 * @param date
	 */
	public DatePanel(Date date)
	{				
		// Create the layout
		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);
		// Create the constraints
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.WEST;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.weightx = 1;
		constraints.gridx = 0;
		constraints.gridy = 0;

		// Add the day
		m_modelDay = new DefaultComboBoxModel();
		m_cbDay = new JComboBox(m_modelDay);
		m_cbDay.addItemListener(this);
		constraints.insets.left = 0;
		add(m_cbDay, constraints);
		constraints.insets.left = 5;
        
		// Add the month
		m_modelMonth = new DefaultComboBoxModel(m_months);
		m_cbMonth = new JComboBox(m_modelMonth);
		m_cbMonth.setKeySelectionManager(new JComboBox.KeySelectionManager()
		{
			// XXX: Does this work?
			public int selectionForKey(char key, javax.swing.ComboBoxModel cbm)
			{
				int nr = key - '0';
				if (nr < 0 || nr > 9)
				{
					return -1;
				}

				if (nr < 3 && m_cbMonth.getSelectedIndex() == 0)
				{
					nr = 10 + nr;

				}
				return nr - 1;
			}
		});
		m_cbMonth.addItemListener(this);
		constraints.gridx++;
		add(m_cbMonth, constraints);
	
		// Add the year
		m_modelYear = new DefaultComboBoxModel(createArray(2000, 2030));
		m_cbYear = new JComboBox(m_modelYear);
		m_cbYear.addItemListener(this);
		constraints.gridx++;
		constraints.insets.right = 0;
		add(m_cbYear, constraints);

		// Finally set the date
		this.setDate(date);
	}
	
	/** 
	 * Update m_cbDay depending on the currently selected month
	 */
	private void updateDays()
	{
		// Store the currently selected day
		Object selectedDay = m_cbDay.getSelectedItem();
		// Get the index of the month, January is 0
		int monthIndex = m_cbMonth.getSelectedIndex();
		int numberOfDays = m_daysPerMonth[m_cbMonth.getSelectedIndex()];
		// Handle leap years
		int year = new Integer(m_cbYear.getSelectedItem().toString());
		if (isLeapYear(year) && monthIndex == 1)
		{
			LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Leap year + month is February --> 29 days");
		    numberOfDays = 29;
		}
		// Create a new model and set it to the ComboBox
		m_modelDay = new DefaultComboBoxModel(createArray(1, numberOfDays));
		m_cbDay.setModel(m_modelDay);		
		// Reset the selection
		if (selectedDay != null) m_cbDay.setSelectedItem(selectedDay);
		else m_cbDay.setSelectedIndex(0);
	}
	
	/**
	 * Check if a given year is a leap year: A year is a leap
	 * year if it is divisible by 4, but not divisible by 100 
	 * OR if it is divisible by 4 and 400.
	 * 
	 * @param year
	 * @return true if the given year is a leap year, else false
	 */
	private boolean isLeapYear(int year)
	{
		boolean ret = false;
		if (year%4 == 0) {
		    if (year%100 != 0) ret = true;
		    else if (year%400 == 0) ret = true;
		}
		return ret;
	}
	
	/**
	 * Return an array containing all integers in the range [start, end]
	 * 
	 * @param start
	 * @param end
	 * @return ret
	 */
	private Object[] createArray(int start, int end)
	{
		// FIXME: Rather do not return null
		if (!(end > start)) return null;
		else
		{
			int size = end-start+1;
		    Object[] ret = new Object[size];
	        for (int i = 0; i < size; i++) 
		    {
	        	// Increment after the assignment
	        	ret[i] = start++;
		    }
	        return ret;
		}
	}
	
	/** Return true if the date has changed since the last call of this method */
	private boolean dateHasChanged()
	{
		boolean ret = false;
		// Get the current date
		Calendar newCal = this.getCalendar();
		// Check if the date has changed by comparing day, month and year only
		if (m_cal == null ||
			m_cal.get(Calendar.DAY_OF_MONTH) != newCal.get(Calendar.DAY_OF_MONTH) ||
			m_cal.get(Calendar.MONTH) != newCal.get(Calendar.MONTH)	||
			m_cal.get(Calendar.YEAR) != newCal.get(Calendar.YEAR))
		{
			LogHolder.log(LogLevel.DEBUG, LogType.MISC, "The date has changed ..");
			ret = true;
		}
		// Always store the last calendar
		m_cal = newCal;
		return ret;
	}
	
	/** Set this DatePanel to a given date */
	public void setDate(Date date)
	{
		//LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Setting date to " + date);
		// Set a calendar object to the given date
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.setTime(date);
		// Suppress events
		m_bSuppressEvents = true;
		// Adjust JComboBoxes, year first
		m_cbYear.setSelectedItem(cal.get(Calendar.YEAR));
		m_cbMonth.setSelectedIndex(cal.get(Calendar.MONTH));
		m_cbDay.setSelectedItem(cal.get(Calendar.DAY_OF_MONTH));
		// Re-enable events
		m_bSuppressEvents = false;
		// Inform all listeners
		fireDateChanged();
	}

	/** Set a calendar to the given date and return it */
	public Calendar getCalendar() throws NumberFormatException
	{
		// Get calendar and clear
		Calendar cal = Calendar.getInstance();
		cal.clear();
		// Get the single parts of the date as integers
		int year = new Integer(m_cbYear.getSelectedItem().toString());
		int month = m_cbMonth.getSelectedIndex();
		int day = new Integer(m_cbDay.getSelectedItem().toString());
		// Set the calendar object to the specified date
		cal.set(year, month, day);
		return cal;
	}
	
	public Date getDate()
	{
		return getCalendar().getTime();
	}
	
	/** Implement ItemListener */
	public void itemStateChanged(ItemEvent a_event)
	{
		// Update the number of days if the month or year has changed
		if (a_event.getSource() == m_cbMonth || a_event.getSource() == m_cbYear)
		{
			// Get the state
			if (a_event.getStateChange() == ItemEvent.SELECTED)
			{
				// If the month has changed, call updateDays()
				//LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Month/year has changed, updating # of days..");
				this.updateDays();	
			}
		}
		// Call fireDateChanged only on *real* date changes
		if (a_event.getStateChange() == ItemEvent.SELECTED && !m_bSuppressEvents)
		{
			if (dateHasChanged()) this.fireDateChanged();
		}
	}
	
	// -------------------- LISTENER NOTIFICATION --------------------
	
	/** Call dateChanged() on all DateListeners */
	protected void fireDateChanged()
	{
		for (int i = 0; i < m_dateListeners.size(); i++)
		{
			(m_dateListeners.elementAt(i)).dateChanged();
		}
	}
	
    /** Add a DateListener */
	public void addDateListener(DateListener a_dateListener)
	{
		removeDateListener(a_dateListener);
		m_dateListeners.addElement(a_dateListener);
	}
	
    /** Remove a DateListener */
	public void removeDateListener(DateListener a_dateListener)
	{
		m_dateListeners.removeElement(a_dateListener);
	}
}