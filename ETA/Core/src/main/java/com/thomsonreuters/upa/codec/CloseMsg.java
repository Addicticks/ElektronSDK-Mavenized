package com.thomsonreuters.upa.codec;

/**
 * UPA close message is used by a Consumer to indicate no further interest in a
 * stream. The stream should be closed as a result. The streamId indicates the
 * item stream to which {@link CloseMsg} applies.
 * 
 * @see Msg
 * @see CloseMsgFlags
 */
public interface CloseMsg extends Msg
{
    /**
     * Checks the presence of the Extended Header presence flag.<br />
     * <br />
     * Flags may also be bulk-get via {@link Msg#flags()}.
     * 
     * @see Msg#flags()
     * 
     * @return true - if exists; false if does not exist.
     */
    public boolean checkHasExtendedHdr();

    /**
     * Checks the presence of the Acknowledgment indication flag.<br />
     * <br />
     * Flags may also be bulk-get via {@link Msg#flags()}.
     * 
     * @see Msg#flags()
     * 
     * @return true - if exists; false if does not exist.
     */
    public boolean checkAck();

    /**
     * Sets the Extended Header presence flag.<br />
     * <br />
     * Flags may also be bulk-set via {@link Msg#flags(int)}.
     * 
     * @see Msg#flags(int)
     */
    public void applyHasExtendedHdr();

    /**
     * Sets the Acknowledgment indication flag.<br />
     * <br />
     * Flags may also be bulk-set via {@link Msg#flags(int)}.
     * 
     * @see Msg#flags(int)
     */
    public void applyAck();
}