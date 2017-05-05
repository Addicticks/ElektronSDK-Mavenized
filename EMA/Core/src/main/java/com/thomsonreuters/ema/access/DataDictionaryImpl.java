///*|-----------------------------------------------------------------------------
// *|            This source code is provided under the Apache 2.0 license      --
// *|  and is provided AS IS with no warranty or guarantee of fit for purpose.  --
// *|                See the project's LICENSE.md for details.                  --
// *|           Copyright Thomson Reuters 2017. All rights reserved.            --
///*|-----------------------------------------------------------------------------

package com.thomsonreuters.ema.access;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.thomsonreuters.ema.access.SeriesImpl;
import com.thomsonreuters.ema.rdm.DataDictionary;
import com.thomsonreuters.ema.rdm.DictionaryEntry;
import com.thomsonreuters.ema.rdm.EmaRdm;
import com.thomsonreuters.ema.rdm.EnumType;
import com.thomsonreuters.ema.rdm.EnumTypeTable;
import com.thomsonreuters.upa.codec.Int;
import com.thomsonreuters.upa.valueadd.common.VaPool;
import com.thomsonreuters.upa.codec.Buffer;
import com.thomsonreuters.upa.codec.CodecFactory;
import com.thomsonreuters.upa.codec.CodecReturnCodes;

class DataDictionaryImpl implements DataDictionary
{
    private com.thomsonreuters.upa.codec.DataDictionary 		rsslDataDictionary;
    private boolean												loadedFieldDictionary;
    private boolean												loadedEnumTypeDef;
    private OmmInvalidUsageExceptionImpl                        ommIUExcept;
    private ArrayList<DictionaryEntry>                          dictionaryEntryList;
    private ArrayList<EnumTypeTable>                            enumTypeTableList;
    private StringBuilder                                       errorString;
    private com.thomsonreuters.upa.codec.DecodeIterator 		rsslDecodeIterator;
    private boolean												ownRsslDataDictionary;
    private HashMap<String,Integer>								fieldNametoIdMap;
    
    private DictionaryEntryImpl									dictionaryEntryImpl = new DictionaryEntryImpl();
    private EnumTypeImpl										enumTypeImpl = new EnumTypeImpl();

    private Int                                                 rsslInt = CodecFactory.createInt();
    private com.thomsonreuters.upa.codec.Enum 					rsslEnumValue = com.thomsonreuters.upa.codec.CodecFactory.createEnum();
    private com.thomsonreuters.upa.transport.Error 				rsslError = com.thomsonreuters.upa.transport.TransportFactory.createError();
    private VaPool 												dictionaryEntryPool = new VaPool(false);
    private VaPool												enumTypeTablePool = new VaPool(false);
    
    private static final int									DEFAULT_DICTIONARY_ENTRY_SIZE = 40;
    private static final int									DEFAULT_ENUM_TABLE_ENTRY_SIZE = 1024;
    private static final int                                    DEFAULT_FRAGMENTATION_SIZE = 12800;
    private static final String									queryingOnlyText = "This DataDictionary instance is used for query data dictionary information only";
   
    
	DataDictionaryImpl(boolean ownDataDictionary)
	{
		ownRsslDataDictionary = ownDataDictionary;
		
		if ( ownRsslDataDictionary )
		{
			rsslDataDictionary = com.thomsonreuters.upa.codec.CodecFactory.createDataDictionary();
		}
		
		clearFlags();
	}
	
	DataDictionaryImpl(DataDictionaryImpl other)
	{
		ownRsslDataDictionary = true;
		
		rsslDataDictionary = com.thomsonreuters.upa.codec.CodecFactory.createDataDictionary();
	
		clearFlags();
		
		if ( !other.loadedFieldDictionary && !other.loadedEnumTypeDef )
		{
			return;
		}
		
		Series series = EmaFactory.createSeries();

		if ( other.loadedFieldDictionary )
		{
			other.encodeFieldDictionary(series, EmaRdm.DICTIONARY_VERBOSE);

			decodeFieldDictionary(series, EmaRdm.DICTIONARY_VERBOSE);

			series.clear();
		}

		if ( other.loadedEnumTypeDef )
		{
			other.encodeEnumTypeDictionary(series, EmaRdm.DICTIONARY_VERBOSE);

			decodeEnumTypeDictionary(series, EmaRdm.DICTIONARY_VERBOSE);
		}
	}
	
	void rsslDataDictionary(com.thomsonreuters.upa.codec.DataDictionary dataDictionary)
	{
		clearDictionaryEntryList();
		
		clearEnumTypeTableList();
		
		if ( fieldNametoIdMap !=  null )
		{
			fieldNametoIdMap.clear();
		}
		
		if( !ownRsslDataDictionary )
		{
			loadedFieldDictionary = true;
			loadedEnumTypeDef = true;
			
			rsslDataDictionary = dataDictionary;
		}
	}
	
	void clearDictionaryEntryList()
	{
		if ( dictionaryEntryList != null && dictionaryEntryList.size() != 0 )
		{
			for(int index = 0; index < dictionaryEntryList.size(); index ++ )
			{
				((DictionaryEntryImpl)dictionaryEntryList.get(index)).returnToPool();
			}
		
			dictionaryEntryList.clear();
		}
	}
	
	void clearEnumTypeTableList()
	{	
		if ( enumTypeTableList != null && enumTypeTableList.size() != 0 )
		{
			for(int index = 0; index < enumTypeTableList.size(); index++ )
			{
				((EnumTypeTableImpl)enumTypeTableList.get(index)).clear().returnToPool();
			}
		
			enumTypeTableList.clear();
		}
	}
	
	@Override
	public void clear() {
		
		clearFlags();
	
		clearDictionaryEntryList();
		
		clearEnumTypeTableList();
		
		if ( fieldNametoIdMap !=  null )
		{
			fieldNametoIdMap.clear();
		}
		
		if ( rsslDataDictionary != null )
		{
			rsslDataDictionary.clear();
		}
	}
	
	@Override
	public int minFid() {
		return rsslDataDictionary.minFid();
	}

	@Override
	public int maxFid() {
		return rsslDataDictionary.maxFid();
	}

	@Override
	public List<DictionaryEntry> entries() {
		
		if ( dictionaryEntryList == null )
		{
			dictionaryEntryList = new ArrayList<>(rsslDataDictionary.numberOfEntries());
		}
		
		if ( dictionaryEntryList.size() != rsslDataDictionary.numberOfEntries() )
		{
			clearDictionaryEntryList();
		}
		else
		{
			return dictionaryEntryList;
		}
		
		if ( loadedFieldDictionary )
		{
			com.thomsonreuters.upa.codec.DictionaryEntry dictionaryEntry;
			
			for( int fieldId = rsslDataDictionary.minFid(); fieldId <= rsslDataDictionary.maxFid(); fieldId++ )
			{
				dictionaryEntry = rsslDataDictionary.entry(fieldId);
				
				if ( dictionaryEntry != null )
				{
					dictionaryEntryList.add(getDictionaryEntry(this, dictionaryEntry));
				}
			}
		}
		
		return dictionaryEntryList;
	}
	
	@Override
	public List<EnumTypeTable> enumTables() {
		
		if ( enumTypeTableList == null )
		{
			enumTypeTableList = new ArrayList<>(rsslDataDictionary.enumTableCount());
		}
		
		if ( enumTypeTableList.size() != rsslDataDictionary.enumTableCount())
		{
			clearEnumTypeTableList();
		}
		else
		{
			return enumTypeTableList;
		}
		
		if( loadedEnumTypeDef )
		{
			com.thomsonreuters.upa.codec.EnumTypeTable enumTypeTable;
			
			for( int index = 0; index < rsslDataDictionary.enumTableCount(); index++ )
			{
				enumTypeTable = rsslDataDictionary.enumTables()[index];
				
				if ( enumTypeTable != null )
				{
					enumTypeTableList.add(getEnumTypeTable(enumTypeTable));
				}
			}
		}
		
		return enumTypeTableList;
	}

	@Override
	public int dictionaryId() {
		return rsslDataDictionary.infoDictionaryId();
	}

	@Override
	public String fieldVersion() {
		return rsslDataDictionary.infoFieldVersion().data() != null ? rsslDataDictionary.infoFieldVersion().toString() : "";
	}

	@Override
	public String enumRecordTemplateVersion() {
		return rsslDataDictionary.infoEnumRTVersion().data() != null ?  rsslDataDictionary.infoEnumRTVersion().toString() : "";
	}

	@Override
	public String enumDisplayTemplateVersion() {
		return rsslDataDictionary.infoEnumDTVersion().data() != null ?  rsslDataDictionary.infoEnumDTVersion().toString() : "";
	}

	@Override
	public String fieldFilename() {
		return rsslDataDictionary.infoFieldFilename().data() != null ? rsslDataDictionary.infoFieldFilename().toString() : "";
	}

	@Override
	public String fieldDescription() {
		return rsslDataDictionary.infoFieldDesc().data() != null ? rsslDataDictionary.infoFieldDesc().toString() : "";
	}

	@Override
	public String fieldBuild() {
		return rsslDataDictionary.infoFieldBuild().data() != null ? rsslDataDictionary.infoFieldBuild().toString() : "";
	}

	@Override
	public String fieldDate() {
		return rsslDataDictionary.infoFieldDate().data() != null ? rsslDataDictionary.infoFieldDate().toString() : "";
	}

	@Override
	public String enumFilename() {
		return rsslDataDictionary.infoEnumFilename().data() != null ? rsslDataDictionary.infoEnumFilename().toString() : "";
	}

	@Override
	public String enumDescription() {
		return rsslDataDictionary.infoEnumDesc().data() != null ? rsslDataDictionary.infoEnumDesc().toString() : "";
	}

	@Override
	public String enumDate() {
		return rsslDataDictionary.infoEnumDate().data() != null ? rsslDataDictionary.infoEnumDate().toString() : "";
	}

	@Override
	public void loadFieldDictionary(String filename) {
		
		if ( ownRsslDataDictionary )
		{
			if ( rsslDataDictionary.loadFieldDictionary(filename, rsslError) < 0 )
			{
				String errText = errorString().append("Unable to load field dictionary from file named ")
						.append(filename).append(OmmLoggerClient.CR)
						.append("Current working directory ")
						.append(System.getProperty("user.dir"))  
						.append(OmmLoggerClient.CR)
						.append("Reason='")
						.append(rsslError.text())
						.append("'").toString();
				throw ommIUExcept().message(errText);
			}
			else
			{
				loadedFieldDictionary = true;
			}
		}
		else
		{
			throw ommIUExcept().message(queryingOnlyText);
		}
	}

	@Override
	public void loadEnumTypeDictionary(String filename) {
		
		if ( ownRsslDataDictionary )
		{
			if ( rsslDataDictionary.loadEnumTypeDictionary(filename, rsslError) < 0 )
			{
				String errText = errorString().append("Unable to load enumerated type definition from file named ")
						.append(filename).append(OmmLoggerClient.CR)
						.append("Current working directory ")
						.append(System.getProperty("user.dir"))  
						.append(OmmLoggerClient.CR)
						.append("Reason='")
						.append(rsslError.text())
						.append("'").toString();
				throw ommIUExcept().message(errText);
			}
			else
			{
				loadedEnumTypeDef = true;
			}
		}
		else
		{
			throw ommIUExcept().message(queryingOnlyText);
		}
	}

	@Override
	public void encodeFieldDictionary(Series series, long verbosity) {
		
		if( !loadedFieldDictionary )
		{
			throw ommIUExcept().message("The field dictionary information was not loaded");
		}
		
		if ( series == null )
		{
			String errText = errorString().append("Passed in series parameter is null").toString();
			throw ommIUExcept().message(errText);
		}
		
		series.clear();
	
		SeriesImpl seriesImpl = (SeriesImpl)series;
		
		int fieldDictionarySize = rsslDataDictionary.numberOfEntries() > 0 ? (rsslDataDictionary.numberOfEntries() * DEFAULT_DICTIONARY_ENTRY_SIZE ) : 
			CollectionDataImpl.ENCODE_RSSL_BUFFER_INIT_SIZE;
		
		Utilities.reallocate(seriesImpl._rsslEncodeIter, fieldDictionarySize);
		
		seriesImpl._rsslBuffer = seriesImpl._rsslEncodeIter.buffer();
		
		rsslInt.value(rsslDataDictionary.minFid());
		
		int ret;
	
		while (  (ret = rsslDataDictionary.encodeFieldDictionary(seriesImpl._rsslEncodeIter, rsslInt, (int)verbosity, rsslError)) == CodecReturnCodes.DICT_PART_ENCODED )
		{
			Utilities.reallocate(seriesImpl._rsslEncodeIter, seriesImpl._rsslEncodeIter.buffer().capacity() * 2);
			rsslInt.value(rsslDataDictionary.minFid());
		}
		
		if ( ret != CodecReturnCodes.SUCCESS)
		{
			seriesImpl.clear();
			String errText = errorString().append("Failed to encode the field dictionary information")
								.append(OmmLoggerClient.CR)
								.append("Reason='")
								.append(rsslError.text())
								.append("'").toString();
					
			throw ommIUExcept().message(errText);
		}
		
		seriesImpl._encodeComplete = true;
	}

	@Override
	public void decodeFieldDictionary(Series series, long verbosity) {
				
		if ( ownRsslDataDictionary )
		{
			if ( series == null )
			{
				String errText = errorString().append("Passed in series parameter is null").toString();
				throw ommIUExcept().message(errText);
			}
			
			Buffer encodedBuffer = ((SeriesImpl)series).encodedData();
			
			com.thomsonreuters.upa.codec.DecodeIterator decodeIterator = decodeIterator();
			
			int ret = decodeIterator.setBufferAndRWFVersion(encodedBuffer, com.thomsonreuters.upa.codec.Codec.majorVersion(), 
					com.thomsonreuters.upa.codec.Codec.minorVersion());
			
			if (ret != CodecReturnCodes.SUCCESS)
		    {
		    	String errText = errorString().append("Failed to setBufferAndRWFVersion on rssl decode iterator. Reason='")
		    								.append(CodecReturnCodes.toString(ret))
		    								.append("'").toString();
		    	throw ommIUExcept().message(errText);
		    }
			
			ret = rsslDataDictionary.decodeFieldDictionary(decodeIterator, (int)verbosity, rsslError);
			
			if ( ret < CodecReturnCodes.SUCCESS )
			{
				String errText = errorString().append("Failed to decode the field dictionary information. Reason='")
						.append(rsslError.text())
						.append("'").toString();
				throw ommIUExcept().message(errText);
			}
			
			loadedFieldDictionary = true;
		}
		else
		{
			throw ommIUExcept().message(queryingOnlyText);
		}
	}

	@Override
	public void encodeEnumTypeDictionary(Series series, long verbosity) {
		
		if(!loadedEnumTypeDef)
		{
			throw ommIUExcept().message("The enumerated types dictionary was not loaded");
		}
		
		if ( series == null )
		{
			String errText = errorString().append("Passed in series parameter is null").toString();
			throw ommIUExcept().message(errText);
		}
		
		series.clear();
	
		SeriesImpl seriesImpl = (SeriesImpl)series;
		
		int enumTypeDictionarySize = rsslDataDictionary.enumTableCount() > 0 ? (rsslDataDictionary.enumTableCount() * DEFAULT_ENUM_TABLE_ENTRY_SIZE ) : 
			CollectionDataImpl.ENCODE_RSSL_BUFFER_INIT_SIZE;
		
		Utilities.reallocate(seriesImpl._rsslEncodeIter, enumTypeDictionarySize);
		
		seriesImpl._rsslBuffer = seriesImpl._rsslEncodeIter.buffer();

		int ret;
	
		while (  (ret = rsslDataDictionary.encodeEnumTypeDictionary(seriesImpl._rsslEncodeIter, (int)verbosity, rsslError)) == CodecReturnCodes.DICT_PART_ENCODED )
		{
			Utilities.reallocate(seriesImpl._rsslEncodeIter, seriesImpl._rsslEncodeIter.buffer().capacity() * 2);
		}
		
		if ( ret != CodecReturnCodes.SUCCESS)
		{
			seriesImpl.clear();
			String errText = errorString().append("Failed to encode the enumerated type definition")
								.append(OmmLoggerClient.CR)
								.append("Reason='")
								.append(rsslError.text())
								.append("'").toString();
					
			throw ommIUExcept().message(errText);
		}
		
		seriesImpl._encodeComplete = true;
	}
	
	@Override
	public int encodeEnumTypeDictionary(Series series, int currentEnumTableEntry, long verbosity, int fragmentationSize) {
		
		if ( series == null )
		{
			String errText = errorString().append("Passed in series parameter is null").toString();
			throw ommIUExcept().message(errText);
		}
		
		if( !loadedEnumTypeDef )
		{
			throw ommIUExcept().message("The enumerated types dictionary was not loaded");
		}
		
		series.clear();
	
		SeriesImpl seriesImpl = (SeriesImpl)series;
		
		int enumTypeDictionarySize = fragmentationSize > 0 ? fragmentationSize : DEFAULT_FRAGMENTATION_SIZE;
		
		Utilities.reallocate(seriesImpl._rsslEncodeIter, enumTypeDictionarySize);
		
		seriesImpl._rsslBuffer = seriesImpl._rsslEncodeIter.buffer();
		
		seriesImpl._rsslEncodeIter.buffer().data(seriesImpl._rsslEncodeIter.buffer().data(), 0, enumTypeDictionarySize);
		
		rsslInt.value(currentEnumTableEntry);

		int ret = rsslDataDictionary.encodeEnumTypeDictionaryAsMultiPart(seriesImpl._rsslEncodeIter, rsslInt, (int)verbosity, rsslError);
		
		if ( ret == CodecReturnCodes.DICT_PART_ENCODED)
		{
			seriesImpl._encodeComplete = true;
			return (int)rsslInt.toLong();
		}
		else if ( ret == CodecReturnCodes.SUCCESS )
		{
			seriesImpl._encodeComplete = true;
			return enumTables().size();
		}
	
		seriesImpl.clear();
		String errText = errorString().append("Failed to encode the enumerated type definition")
				.append(OmmLoggerClient.CR)
				.append("Reason='")
				.append(rsslError.text())
				.append("'").toString();
		
		throw ommIUExcept().message(errText);
	}
	
	@Override
	public int encodeFieldDictionary(Series series, int currentFid, long verbosity, int fragmentationSize) {
		
		if ( series == null )
		{
			String errText = errorString().append("Passed in series parameter is null").toString();
			throw ommIUExcept().message(errText);
		}
		
		if(!loadedFieldDictionary)
		{
			throw ommIUExcept().message("The field dictionary information was not loaded");
		}
		
		series.clear();
	
		SeriesImpl seriesImpl = (SeriesImpl)series;
		
		int fieldDictionarySize = fragmentationSize > 0 ? fragmentationSize : DEFAULT_FRAGMENTATION_SIZE;
		
		Utilities.reallocate(seriesImpl._rsslEncodeIter, fieldDictionarySize);
		
		seriesImpl._rsslBuffer = seriesImpl._rsslEncodeIter.buffer();
		
		seriesImpl._rsslEncodeIter.buffer().data(seriesImpl._rsslEncodeIter.buffer().data(), 0, fieldDictionarySize);
		
		rsslInt.value(currentFid);
		
		int ret;
	
		ret = rsslDataDictionary.encodeFieldDictionary(seriesImpl._rsslEncodeIter, rsslInt, (int)verbosity, rsslError);
		
		if ( ret == CodecReturnCodes.SUCCESS )
		{
			seriesImpl._encodeComplete = true;
			return rsslDataDictionary.maxFid();
		}
		if ( ret == CodecReturnCodes.DICT_PART_ENCODED )
		{
			seriesImpl._encodeComplete = true;
			return (int)rsslInt.toLong();
		}
		
		String errText = errorString().append("Failed to encode the field dictionary information. Reason='")
				.append(CodecReturnCodes.toString(ret))
				.append("'").toString();
		throw ommIUExcept().message(errText);
	}

	@Override
	public int extractDictionaryType(Series series) {
		
		if ( series == null )
		{
			String errText = errorString().append("Passed in series parameter is null").toString();
			throw ommIUExcept().message(errText);
		}
		
		Buffer encodedBuffer = ((SeriesImpl)series).encodedData();
		
		com.thomsonreuters.upa.codec.DecodeIterator decodeIterator = decodeIterator();
		
		int ret = decodeIterator.setBufferAndRWFVersion(encodedBuffer, com.thomsonreuters.upa.codec.Codec.majorVersion(), 
				com.thomsonreuters.upa.codec.Codec.minorVersion());
		
		if (ret != CodecReturnCodes.SUCCESS)
	    {
	    	String errText = errorString().append("Failed to setBufferAndRWFVersion on rssl decode iterator. Reason='")
	    								.append(CodecReturnCodes.toString(ret))
	    								.append("'").toString();
	    	throw ommIUExcept().message(errText);
	    }
		
		com.thomsonreuters.upa.codec.Int dictionaryType = com.thomsonreuters.upa.codec.CodecFactory.createInt();
		
		if ( rsslDataDictionary.extractDictionaryType(decodeIterator, dictionaryType, rsslError) < CodecReturnCodes.SUCCESS )
		{
			String errText = errorString().append("Failed to extract dictionary type. Reason='")
					.append(CodecReturnCodes.toString(ret))
					.append("'").toString();
			throw ommIUExcept().message(errText);
		}
		
		return (int)dictionaryType.toLong();
	}

	@Override
	public void decodeEnumTypeDictionary(Series series, long verbosity) {
		
		if ( ownRsslDataDictionary )
		{
			if ( series == null )
			{
				String errText = errorString().append("Passed in series parameter is null").toString();
				throw ommIUExcept().message(errText);
			}
			
			Buffer encodedBuffer = ((SeriesImpl)series).encodedData();
			
			com.thomsonreuters.upa.codec.DecodeIterator decodeIterator = decodeIterator();
			
			int ret = decodeIterator.setBufferAndRWFVersion(encodedBuffer, com.thomsonreuters.upa.codec.Codec.majorVersion(), 
					com.thomsonreuters.upa.codec.Codec.minorVersion());
			
			if (ret != CodecReturnCodes.SUCCESS)
		    {
		    	String errText = errorString().append("Failed to setBufferAndRWFVersion on rssl decode iterator. Reason='")
		    								.append(CodecReturnCodes.toString(ret))
		    								.append("'").toString();
		    	throw ommIUExcept().message(errText);
		    }
			
			ret = rsslDataDictionary.decodeEnumTypeDictionary(decodeIterator, (int)verbosity, rsslError);
			if ( ret < CodecReturnCodes.SUCCESS )
			{
				String errText = errorString().append("Failed to decode the enumerated types dictionary. Reason='")
						.append(rsslError.text())
						.append("'").toString();
				throw ommIUExcept().message(errText);
			}
			
			loadedEnumTypeDef = true;
		}
		else
		{
			throw ommIUExcept().message(queryingOnlyText);
		}
	}

	@Override
	public DictionaryEntry entry(int fieldId) {
		
		if( !loadedFieldDictionary )
		{
			throw ommIUExcept().message("The field dictionary information was not loaded");
		}
		
		com.thomsonreuters.upa.codec.DictionaryEntry dictionaryEntry = rsslDataDictionary.entry(fieldId);
		
		if ( dictionaryEntry != null )
		{
			return dictionaryEntryImpl.dictionaryEntry(this, dictionaryEntry);
		}
		
		throw ommIUExcept().message("The Field ID " + fieldId + " does not exist in the field dictionary");
	}


	@Override
	public EnumType enumType(int fieldId, int value) {
		
		if( !loadedEnumTypeDef )
		{
			throw ommIUExcept().message("The enumerated types dictionary was not loaded");
		}

		com.thomsonreuters.upa.codec.DictionaryEntry dictionaryEntry;
		
		dictionaryEntry = rsslDataDictionary.entry(fieldId);
		
		if ( dictionaryEntry != null )
		{
			rsslEnumValue.value(value);
			
			com.thomsonreuters.upa.codec.EnumType enumType = rsslDataDictionary.entryEnumType(dictionaryEntry, rsslEnumValue);
			
			if ( enumType != null )
			{
				return enumTypeImpl.enumType(enumType);
			}
		}
	
		throw ommIUExcept().message("The enum value " + value + " for the Field ID " + fieldId + " does not exist in enumerated type definitions");
	}

	@Override
	public boolean hasEntry(int fieldId) {
		
		if( !loadedFieldDictionary )
		{
			return false;
		}
		
		return rsslDataDictionary.entry(fieldId) != null ? true : false;
	}

	@Override
	public boolean hasEnumType(int fieldId, int value) {
		
		if( !loadedFieldDictionary &&  !loadedEnumTypeDef )
		{
			return false;
		}
		
		com.thomsonreuters.upa.codec.DictionaryEntry dictionaryEntry;
		
		dictionaryEntry = rsslDataDictionary.entry(fieldId);
		
		if ( dictionaryEntry != null )
		{
			rsslEnumValue.value(value);
			return rsslDataDictionary.entryEnumType(dictionaryEntry, rsslEnumValue) != null ? true : false;
		}
		
		return false;
	}
	
	@Override
	public boolean hasEntry(String fieldName) {
		
		if( !loadedFieldDictionary )
		{
			return false;
		}
		
		HashMap<String,Integer>	 nameToIdMap = fieldNameToIdMap();
		
		return nameToIdMap != null ? nameToIdMap.containsKey(fieldName) : false;
	}

	@Override
	public DictionaryEntry entry(String fieldName) {
		
		if( !loadedFieldDictionary )
		{
			throw ommIUExcept().message("The field dictionary information was not loaded");
		}
		
		if ( !hasEntry(fieldName) )
		{
			throw ommIUExcept().message("The Field name " + fieldName + " does not exist in the field dictionary");
		}
		
		return entry(fieldNameToIdMap().get(fieldName));
	}
	
	@Override
	public String toString() {
		
		if ( rsslDataDictionary.toString() == null || ( !loadedFieldDictionary && !loadedEnumTypeDef ) )
		{
			return "DataDictionary is not initialized";
		}
		
		return rsslDataDictionary.toString();
	}
	
	
	private HashMap<String,Integer> fieldNameToIdMap()
	{
		if (  loadedFieldDictionary )
		{
			if ( fieldNametoIdMap == null )
			{
				fieldNametoIdMap = new HashMap<>(rsslDataDictionary.numberOfEntries());
			}
			
			if ( fieldNametoIdMap.size() == 0 )
			{
				com.thomsonreuters.upa.codec.DictionaryEntry dictionaryEntry;
				
				for( int fieldId = rsslDataDictionary.minFid(); fieldId <= rsslDataDictionary.maxFid(); fieldId++ )
				{
					dictionaryEntry = rsslDataDictionary.entry(fieldId);
					
					if ( dictionaryEntry != null && dictionaryEntry.acronym().data() != null )
					{
						fieldNametoIdMap.put(dictionaryEntry.acronym().toString(), dictionaryEntry.fid());
					}
				}
			}
		}
		
		return fieldNametoIdMap;
	}
	
	private DictionaryEntryImpl getDictionaryEntry(DataDictionaryImpl dataDictionary, 
			com.thomsonreuters.upa.codec.DictionaryEntry dictionaryEntry)
	{	
		DictionaryEntryImpl dictionaryEntryImpl = (DictionaryEntryImpl)dictionaryEntryPool.poll();
		
		if ( dictionaryEntryImpl == null )
		{
			dictionaryEntryImpl = new DictionaryEntryImpl();
			dictionaryEntryPool.updatePool(dictionaryEntryImpl);
		}
		
		dictionaryEntryImpl.dictionaryEntry(dataDictionary, dictionaryEntry);
		
		return dictionaryEntryImpl;
	}
	
	private EnumTypeTableImpl getEnumTypeTable(com.thomsonreuters.upa.codec.EnumTypeTable enumTypeTable)
	{
		EnumTypeTableImpl enumTypeTableImpl = (EnumTypeTableImpl)enumTypeTablePool.poll();
		
		if ( enumTypeTableImpl == null )
		{
			enumTypeTableImpl = new EnumTypeTableImpl();
			enumTypeTablePool.updatePool(enumTypeTableImpl);
		}
		
		enumTypeTableImpl.enumTypeTable(enumTypeTable);
		
		return enumTypeTableImpl;
	}
	
	private void clearFlags()
	{
		loadedFieldDictionary = false;
		loadedEnumTypeDef = false;
	}
	
	private StringBuilder errorString()
	{
		if ( errorString == null )
		{
			errorString = new StringBuilder(255);
		}
		else
		{
			errorString.setLength(0);
		}
		
		return errorString;
	}
	
	private OmmInvalidUsageExceptionImpl ommIUExcept()
	{
		if (ommIUExcept == null)
			ommIUExcept = new OmmInvalidUsageExceptionImpl();

		return ommIUExcept;
	}
	
	private com.thomsonreuters.upa.codec.DecodeIterator decodeIterator()
	{
		if ( rsslDecodeIterator == null )
		{
			rsslDecodeIterator = com.thomsonreuters.upa.codec.CodecFactory.createDecodeIterator();
		}
		
		rsslDecodeIterator.clear();
		
		return rsslDecodeIterator;
	}
}
