package com.thomsonreuters.upa.valueadd.domainrep.rdm.directory;

/**
 * The types of RDM Directory Messages. rdmMsgType member in
 * {@link DirectoryMsg} may be set to one of these to indicate the specific
 * RDMDirectoryMsg class.
 * 
 * @see DirectoryMsg
 * @see DirectoryClose
 * @see DirectoryRefresh
 * @see DirectoryRequest
 * @see DirectoryConsumerStatus
 */
public enum DirectoryMsgType
{
    /** (0) Unknown */
    UNKNOWN(0), 
    
    /** (1) Directory Request */
    REQUEST(1), 
    
    /** (2) Directory Close */
    CLOSE(2), 
    
    /** (3) Directory Consumer Status */
    CONSUMER_STATUS(3),
    
    /** (4) Directory Refresh */
    REFRESH(4),
    
    /** (5) Directory Update */
    UPDATE(5), 
    
    /** (6) Directory Status */
    STATUS(6); 

    private DirectoryMsgType(int value)
    {
        this.value = value;
    }

    @SuppressWarnings("unused")
    private int value;
}
