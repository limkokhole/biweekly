package biweekly.io;

import static biweekly.util.TestUtils.assertWarnings;
import static biweekly.util.TestUtils.date;
import static biweekly.util.TestUtils.utc;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

import org.junit.ClassRule;
import org.junit.Test;

import biweekly.ICalendar;
import biweekly.component.DaylightSavingsTime;
import biweekly.component.StandardTime;
import biweekly.component.VTimezone;
import biweekly.property.ICalProperty;
import biweekly.util.DateTimeComponents;
import biweekly.util.DefaultTimezoneRule;

/*
 Copyright (c) 2013, Michael Angstadt
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met: 

 1. Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer. 
 2. Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution. 

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * @author Michael Angstadt
 */
public class StreamReaderTest {
	@ClassRule
	public static final DefaultTimezoneRule tzRule = new DefaultTimezoneRule(-1, 0);

	@Test
	public void timezones() throws Exception {
		StreamReaderImpl reader = new StreamReaderImpl();
		ICalendar ical = reader.readNext();
		TimezoneInfo tzinfo = reader.getTimezoneInfo();

		VTimezone component = ical.getTimezones().get(0);
		Iterator<TestProperty> it = ical.getProperties(TestProperty.class).iterator();

		//floating-time property
		TestProperty property = it.next();
		assertTrue(tzinfo.usesFloatingTime(property));
		assertNull(tzinfo.getComponent(property));
		assertNull(tzinfo.getTimeZone(property));
		assertNull(property.getParameters().getTimezoneId());
		assertEquals(date("2014-09-21 10:22:00"), property.date);

		//timezoned property
		property = it.next();
		assertFalse(tzinfo.usesFloatingTime(property));
		assertEquals(component, tzinfo.getComponent(property));
		assertTrue(tzinfo.getTimeZone(property) instanceof ICalTimeZone);
		assertNull(property.getParameters().getTimezoneId());
		assertEquals(utc("2014-10-01 04:07:00"), property.date);

		//timezoned property
		property = it.next();
		assertFalse(tzinfo.usesFloatingTime(property));
		assertEquals(component, tzinfo.getComponent(property));
		assertTrue(tzinfo.getTimeZone(property) instanceof ICalTimeZone);
		assertNull(property.getParameters().getTimezoneId());
		assertEquals(utc("2014-08-01 03:07:00"), property.date);

		//timezoned property
		property = it.next();
		assertFalse(tzinfo.usesFloatingTime(property));
		assertEquals(component, tzinfo.getComponent(property));
		assertTrue(tzinfo.getTimeZone(property) instanceof ICalTimeZone);
		assertNull(property.getParameters().getTimezoneId());
		assertEquals(utc("2013-12-01 04:07:00"), property.date);

		//property with Olsen TZID that doesn't point to a VTIMEZONE component
		property = it.next();
		assertFalse(tzinfo.usesFloatingTime(property));
		assertNull(tzinfo.getComponent(property));
		assertEquals(TimeZone.getTimeZone("America/New_York"), tzinfo.getTimeZone(property));
		assertNull(property.getParameters().getTimezoneId());
		assertEquals(utc("2014-07-04 13:00:00"), property.date);

		//property with TZID that doesn't point to a VTIMEZONE component
		property = it.next();
		assertFalse(tzinfo.usesFloatingTime(property));
		assertNull(tzinfo.getComponent(property));
		assertNull(tzinfo.getTimeZone(property));
		assertEquals("foobar", property.getParameters().getTimezoneId());
		assertEquals(date("2014-06-11 14:00:00"), property.date);

		assertFalse(it.hasNext());
		assertWarnings(2, reader);
	}

	@Test
	public void timezones_no_component_with_olsen_id() {

	}

	@Test
	public void timezones_no_component_without_olsen_id() {

	}

	private class StreamReaderImpl extends StreamReader {
		@Override
		protected ICalendar _readNext() {
			ICalendar ical = new ICalendar();

			VTimezone timezone = new VTimezone("tz");
			{
				StandardTime standard = new StandardTime();
				standard.setDateStart(new DateTimeComponents(2014, 9, 1, 2, 0, 0, false));
				standard.setTimezoneOffsetFrom(10, 0);
				standard.setTimezoneOffsetTo(9, 0);
				timezone.addStandardTime(standard);

				DaylightSavingsTime daylight = new DaylightSavingsTime();
				daylight.setDateStart(new DateTimeComponents(2014, 1, 1, 2, 0, 0, false));
				daylight.setTimezoneOffsetFrom(9, 0);
				daylight.setTimezoneOffsetTo(10, 0);
				timezone.addDaylightSavingsTime(daylight);
			}
			ical.addComponent(timezone);

			TestProperty floating = new TestProperty(date("2014-09-21 10:22:00"));
			context.addFloatingDate(floating);
			ical.addProperty(floating);

			TestProperty timezoned = new TestProperty(date("2014-10-01 13:07:00"));
			timezoned.getParameters().setTimezoneId(timezone.getTimezoneId().getValue());
			context.addTimezonedDate(timezone.getTimezoneId().getValue(), timezoned, timezoned.date, "20141001T130700");
			ical.addProperty(timezoned);

			timezoned = new TestProperty(date("2014-08-01 13:07:00"));
			timezoned.getParameters().setTimezoneId(timezone.getTimezoneId().getValue());
			context.addTimezonedDate(timezone.getTimezoneId().getValue(), timezoned, timezoned.date, "20140801T130700");
			ical.addProperty(timezoned);

			timezoned = new TestProperty(date("2013-12-01 13:07:00"));
			timezoned.getParameters().setTimezoneId(timezone.getTimezoneId().getValue());
			context.addTimezonedDate(timezone.getTimezoneId().getValue(), timezoned, timezoned.date, "20131201T130700");
			ical.addProperty(timezoned);

			timezoned = new TestProperty(date("2014-07-04 09:00:00"));
			timezoned.getParameters().setTimezoneId("America/New_York");
			context.addTimezonedDate("America/New_York", timezoned, timezoned.date, "20140704T090000");
			ical.addProperty(timezoned);

			timezoned = new TestProperty(date("2014-06-11 14:00:00"));
			timezoned.getParameters().setTimezoneId("foobar");
			context.addTimezonedDate("foobar", timezoned, timezoned.date, "20140611T140000");
			ical.addProperty(timezoned);

			return ical;
		}

		public void close() throws IOException {
			//empty
		}

	}

	private class TestProperty extends ICalProperty {
		private final Date date;

		public TestProperty(Date date) {
			this.date = date;
		}
	}
}
