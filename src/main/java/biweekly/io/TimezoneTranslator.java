package biweekly.io;

import java.util.TimeZone;

import biweekly.component.VTimezone;

/**
 * Converts between iCalendar {@link VTimezone} components and Java
 * {@link TimeZone} objects.
 * @author Michael Angstadt
 */
public interface TimezoneTranslator {
	/**
	 * Converts a Java {@link TimeZone} object to an iCalendar {@link VTimezone}
	 * component.
	 * @param timezone the timezone object
	 * @return the timezone component
	 * @throws IllegalArgumentException if the timezone object cannot be
	 * converted
	 */
	VTimezone toICalVTimezone(TimeZone timezone) throws IllegalArgumentException;
}
