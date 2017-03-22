package com.thomsonreuters.upa.transport;

/**
 * UPA Initialize Arguments used in the {@link Transport#initialize(InitArgs, Error)} call.
 * 
 * @see Transport
 */
public interface InitArgs
{
    /**
     * If locking is true, the global locking will be used by {@link Transport}.
     * 
     * @param locking the locking to set
     */
    public void globalLocking(boolean locking);

    /**
     * If true, the global lock is used.
     * 
     * @return the locking
     */
    public boolean globalLocking();

    /**
     * Clears UPA Initialize Arguments.
     */
    public void clear();
}
