package com.thomsonreuters.upa.valueadd.domainrep.rdm.dictionary;

import com.thomsonreuters.upa.codec.Buffer;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.MsgBase;

/**
 * The RDM Dictionary Request.  
 * Used by an OMM Consumer to request a dictionary from a services that provides it.
 * @see MsgBase
 * @see DictionaryMsg
 */
public interface DictionaryRequest extends DictionaryMsg
{
    /**
     * Performs a deep copy of {@link DictionaryRequest} object.
     *
     * @param destRequestMsg Message to copy dictionary request object into. It cannot be null.
     * 
     * @return UPA return value indicating success or failure of copy operation.
     */
    public int copy(DictionaryRequest destRequestMsg);
    
    /**
     * dictionaryName -  The name of the dictionary being requested.
     * 
     * @return - dictionaryName
     */
    public Buffer dictionaryName();
    
    
    /**
     * Sets the the dictionaryName field for this message to the user specified
     * buffer. Buffer used by this object's dictionaryName field will be set to
     * passed in buffer's data and position. Note that this creates garbage if
     * buffer is backed by String object.
     * 
     * @param dictionaryName
     */
    public void dictionaryName(Buffer dictionaryName);
    
    
    /**
     * verbosity - The verbosity of information desired. Populated by
     * {@link com.thomsonreuters.upa.rdm.Dictionary.VerbosityValues}.
     * 
     * @return verbosity
     */
    public int verbosity();

    /**
     * verbosity - The verbosity of information desired. Populated by
     * {@link com.thomsonreuters.upa.rdm.Dictionary.VerbosityValues}.
     * 
     * @param verbosity
     */
    public void verbosity(int verbosity);
    
    /**
     * serviceId - The ID of the service to request the dictionary from.
     * 
     * @return serviceId
     */
    public int serviceId();
    
    /**
     * serviceId - The ID of the service to request the dictionary from.
     * 
     * @param serviceId
     */
    public void serviceId(int serviceId);
    
    /**
     * The RDM Dictionary request flags. Populated by {@link DictionaryRequestFlags}.
     * 
     * @return RDM Dictionary request flag
     */
    public int flags();
    
    /**
     * The RDM Dictionary request flags. Populated by {@link DictionaryRequestFlags}.
     * 
     * @param flags
     */
    public void flags(int flags);
    
    /** 
     * Checks if this request is streaming or not.
     * 
     * This flag can also be bulk-get by {@link #flags()}
     * 
     * @return true - if request is streaming, false - if not.
     */
    public boolean checkStreaming();
    
    /**
     * Makes this request streaming request.
     * 
     * This flag can also be bulk-set by {@link #flags(int)}
     */
    public void applyStreaming();
    
}