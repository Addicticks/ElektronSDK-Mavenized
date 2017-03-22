package com.thomsonreuters.upa.perftools.common;


/**
 * Latency random array options. 
 */
public class LatencyRandomArrayOptions
{
	private int	_totalMsgsPerSec;	// Total messages sent per second
	private int	_latencyMsgsPerSec;	// Total number of latency messages sent per second
	private int	_ticksPerSec;		// Bursts of messages sent per second by the user.
	private int	_arrayCount;		// Increasing this adds more random values to use 
							 		// (each individual array contains one second's worth of 
							 		// values).
	
	/**
	 * Clears the latency random array options.
	 */
	public void clear()
	{
		_totalMsgsPerSec = 0;
		_latencyMsgsPerSec = 0;
		_ticksPerSec = 0;
		_arrayCount = 0;
	}
	
	/**
	 * Total messages sent per second.
	 */
	public int totalMsgsPerSec()
	{
		return _totalMsgsPerSec;
	}
	
	/** Total messages sent per second */
	public void totalMsgsPerSec(int totalMsgsPerSec)
	{
		_totalMsgsPerSec = totalMsgsPerSec;
	}

	/** Total number of latency messages sent per second */
	public int latencyMsgsPerSec()
	{
		return _latencyMsgsPerSec;
	}

	/** Total number of latency messages sent per second */
	public void latencyMsgsPerSec(int latencyMsgsPerSec)
	{
		_latencyMsgsPerSec = latencyMsgsPerSec;
	}

	/** Bursts of messages sent per second by the user. */
	public int ticksPerSec()
	{
		return _ticksPerSec;
	}

	/** Bursts of messages sent per second by the user. */
	public void ticksPerSec(int ticksPerSec)
	{
		_ticksPerSec = ticksPerSec;
	}
	
    /**
     * Returns the number of elements in the array. Each individual array
     * contains one second's worth of values. This method returns number of
     * elements in this array.
     * 
     * @return number of elements in the array that contains one second's worth of values.
     */
	public int arrayCount()
	{
		return _arrayCount;
	}


	/**
     * Sets the number of elements in the array. Increasing this adds more random values
     * to use (each individual array contains one second's worth of values).
     * 
     * @param arrayCount
     */
	public void arrayCount(int arrayCount)
	{
		_arrayCount = arrayCount;
	}
}
