package com.thomsonreuters.upa.codec;

import com.thomsonreuters.upa.codec.Buffer;

/**
 * A data dictionary entry, containing field information and an enumeration table reference if present.
 * 
 * @see MfFieldTypes
 * @see DataTypes
 */
public interface DictionaryEntry 
{
    /**
     * Acronym.
     * 
     * @return the acronym
     */
    public Buffer acronym();

    /**
     * DDE Acronym.
     * 
     * @return the ddeAcronym
     */
    public Buffer ddeAcronym();

    /**
     * The fieldId the entry corresponds to.
     * 
     * @return the fid
     */
    public int fid();

    /**
     * The field to ripple data to.
     * 
     * @return the rippleToField
     */
    public int rippleToField();

    /**
     * Marketfeed Field Type.
     * 
     * @return the fieldType
     */
    public int fieldType();

    /**
     * Marketfeed length.
     * 
     * @return the length
     */
    public int length();

    /**
     * Marketfeed enum length.
     * 
     * @return the enumLength
     */
    public int enumLength();

    /**
     * RWF type.
     * 
     * @return the rwfType
     */
    public int rwfType();

    /**
     * RWF Length.
     * 
     * @return the rwfLength
     */
    public int rwfLength();

    /**
     * A reference to the corresponding enumerated types table, if this field uses one.
     * 
     * @return the enumTypeTable
     */
    public EnumTypeTable enumTypeTable();
}