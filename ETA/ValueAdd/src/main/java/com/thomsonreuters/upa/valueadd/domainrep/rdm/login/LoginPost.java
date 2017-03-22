package com.thomsonreuters.upa.valueadd.domainrep.rdm.login;


/**
 * The RDM Login Post.  Used for an off-stream Post message.
 * 
 * @see LoginMsg
 * 
 * @deprecated use {@link com.thomsonreuters.upa.codec.PostMsg} instead
 */
@Deprecated
public interface LoginPost extends LoginMsg
{
    /**
     * Performs a deep copy of {@link LoginPost} object.
     *
     * @param destPostMsg Message to copy login post object into. It cannot be null.
     * 
     * @return UPA return value indicating success or failure of copy operation.
     */
    public int copy(LoginPost destPostMsg);
}