///*|-----------------------------------------------------------------------------
// *|            This source code is provided under the Apache 2.0 license      --
// *|  and is provided AS IS with no warranty or guarantee of fit for purpose.  --
// *|                See the project's LICENSE.md for details.                  --
// *|           Copyright Thomson Reuters 2015. All rights reserved.            --
///*|-----------------------------------------------------------------------------

package com.thomsonreuters.ema.access;

/**
 * OmmDateTime represents DateTime info in Omm.
 * <br>OmmDateTime encapsulates year, month, day, hour, minute, second, millisecond,
 * microsecond and nanosecond information.
 * 
 * OmmDateTime is a read only class.
 * 
 * @see Data
 */
public interface OmmDateTime extends Data
{
	/**
	 * Returns Year.
	 * @return value of year
	 */
	public int year();

	/**
	 * Returns Month.
	 * @return value of month
	 */
	public int month();

	/**
	 * Returns Day.
	 * @return value of day
	 */
	public int day();

	/**
	 * Returns Hour.
	 * @return value of hour
	 */
	public int hour();

	/**
	 * Returns Minute.
	 * @return value of minutes
	 */
	public int minute();

	/**
	 * Returns Second.
	 * @return value of seconds
	 */
	public int second();

	/**
	 * Returns Millisecond.
	 * @return value of milliseconds
	 */
	public int millisecond();

	/**
	 * Returns Microsecond.
	 * @return value of microseconds
	 */
	public int microsecond();

	/**
	 * Returns Nanosecond.
	 * @return value of nanoseconds
	 */
	public int nanosecond();
}