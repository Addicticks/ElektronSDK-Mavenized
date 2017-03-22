///*|-----------------------------------------------------------------------------
// *|            This source code is provided under the Apache 2.0 license      --
// *|  and is provided AS IS with no warranty or guarantee of fit for purpose.  --
// *|                See the project's LICENSE.md for details.                  --
// *|           Copyright Thomson Reuters 2015. All rights reserved.            --
///*|-----------------------------------------------------------------------------

package com.thomsonreuters.ema.access;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import com.thomsonreuters.ema.access.DataType.DataTypes;
import com.thomsonreuters.ema.access.OmmError.ErrorCode;
import com.thomsonreuters.upa.codec.Buffer;
import com.thomsonreuters.upa.codec.CodecFactory;
import com.thomsonreuters.upa.codec.CodecReturnCodes;
import com.thomsonreuters.upa.codec.DataDictionary;
import com.thomsonreuters.upa.codec.VectorEntryActions;

class VectorImpl extends CollectionDataImpl implements Vector
{
	private com.thomsonreuters.upa.codec.Vector	_rsslVector = com.thomsonreuters.upa.codec.CodecFactory.createVector();
	private LinkedList<VectorEntry> _vectorCollection = new LinkedList<VectorEntry>(); 
	private DataImpl _summaryDecoded;
	private PayloadAttribSummaryImpl _summaryData;
	
	VectorImpl() 
	{
		super(null);
	}
	
	VectorImpl(EmaObjectManager objManager)
	{
		super(objManager);
		if (objManager != null)
			_summaryDecoded = noDataInstance();
	} 

	@Override
	public int dataType()
	{
		return DataTypes.VECTOR;
	}

	@Override
	public boolean hasTotalCountHint()
	{
		return _rsslVector.checkHasTotalCountHint();
	}

	@Override
	public boolean sortable()
	{
		return _rsslVector.checkSupportsSorting();
	}

	@Override
	public int totalCountHint()
	{
		if (!hasTotalCountHint())
			throw ommIUExcept().message("Attempt to totalCountHint() while it is not set.");
		
		return _rsslVector.totalCountHint();
	}

	@Override
	public SummaryData summaryData()
	{
		if (_summaryData == null)
			_summaryData = new PayloadAttribSummaryImpl();
		
		if (_summaryDecoded == null)
			_summaryDecoded = (DataImpl)new NoDataImpl();
		
		_summaryData.data(_summaryDecoded);
		return (SummaryData)_summaryData;
	}
	
	@Override
	public String toString()
	{
		return toString(0);
	}
	
	@Override
	public void clear()
	{
		if (_rsslEncodeIter != null)
		{
			super.clear();
			
			_rsslVector.clear();
			_vectorCollection.clear();
		}
		else
			clearCollection();
	}

	@Override
	public Vector sortable(boolean sortable)
	{
		if (sortable)
			_rsslVector.applySupportsSorting();

		return this;
	}

	@Override
	public Vector totalCountHint(int totalCountHint)
	{
		if (totalCountHint < 0 || totalCountHint > 1073741823)
			throw ommOORExcept().message("totalCountHint is out of range [0 - 1073741823].");

		_rsslVector.applyHasTotalCountHint();
		_rsslVector.totalCountHint(totalCountHint);

		return this;
	}

	@Override
	public Vector summaryData(ComplexType summaryData)
	{
		if (summaryData == null)
			throw ommIUExcept().message("Passed in summaryData is null");

		_rsslVector.applyHasSummaryData();
		Utilities.copy(((DataImpl) summaryData).encodedData(), _rsslVector.encodedSummaryData());

		return this;
	}

	@Override
	public boolean isEmpty()
	{
		if (_fillCollection)
			fillCollection();
		return _vectorCollection.isEmpty();
	}

	@Override
	public Iterator<VectorEntry> iterator()
	{
		if (_fillCollection)
			fillCollection();
		
		return new EmaIterator<VectorEntry>(_vectorCollection.iterator());
	}

	@Override
	public int size()
	{
		if (_fillCollection)
			fillCollection();
		return _vectorCollection.size();
	}
	
	@Override
	public boolean add(VectorEntry vectorEntry)
	{
		if (vectorEntry == null)
			throw new NullPointerException("Passed in vectorEntry is null.");

		return _vectorCollection.add(vectorEntry);
	}

	@Override
	public boolean addAll(Collection<? extends VectorEntry> c)
	{
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Vector collection doesn't support this operation.");
	}

	@Override
	public boolean contains(Object o)
	{
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Vector collection doesn't support this operation.");
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Vector collection doesn't support this operation.");
	}

	@Override
	public boolean remove(Object o)
	{
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Vector collection doesn't support this operation.");
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Vector collection doesn't support this operation.");
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Vector collection doesn't support this operation.");
	}

	@Override
	public Object[] toArray()
	{
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Vector collection doesn't support this operation.");
	}

	@Override
	public <T> T[] toArray(T[] a)
	{
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Vector collection doesn't support this operation.");
	}
	
	String toString(int indent)
	{
		_toString.setLength(0);
		Utilities.addIndent(_toString, indent).append("Vector");
				
		_toString.append(" sortable=\"").append((sortable() ? "true" : "false")).append("\"");
		
		if (hasTotalCountHint())
			_toString.append(" totalCountHint=\"").append(totalCountHint()).append("\"");

		if (_rsslVector.checkHasSummaryData())
		{
			++indent;
			Utilities.addIndent(_toString.append("\n"), indent).append("SummaryData dataType=\"")
					 .append(DataType.asString(summaryData().dataType())).append("\"\n");
			
			++indent;
			_toString.append(_summaryDecoded.toString(indent));
			--indent;
			
			Utilities.addIndent(_toString, indent).append("SummaryDataEnd");
			--indent;
		}
		
		if (_fillCollection)
			fillCollection();
		
		if ( _vectorCollection.isEmpty() )
		{
			Utilities.addIndent(_toString.append("\n"), indent).append("VectorEnd\n");
			return _toString.toString();
		}
		
		++indent;
		
		DataImpl load;
		for (VectorEntry vectorEntry : _vectorCollection)
		{
			load = (DataImpl)vectorEntry.load();
			Utilities.addIndent(_toString.append("\n"), indent).append("VectorEntry action=\"")
					.append(vectorEntry.vectorActionAsString()).append("\" index=\"").append(vectorEntry.position());
			
			if (vectorEntry.hasPermissionData())
			{
				_toString.append(" permissionData=\"");
				Utilities.asHexString(_toString, vectorEntry.permissionData()).append("\"");
			}
				
			_toString.append(" dataType=\"").append(DataType.asString(load.dataType())).append("\"\n");
			
			++indent;
			_toString.append(load.toString(indent));
			--indent;
			
			Utilities.addIndent(_toString, indent).append("VectorEntryEnd");
		}

		--indent;

		Utilities.addIndent(_toString.append("\n"), indent).append("VectorEnd\n");

		return _toString.toString();
	}
	
	@Override
	void decode(Buffer rsslBuffer, int majVer, int minVer,
			DataDictionary rsslDictionary, Object obj)
	{
		_fillCollection = true;

		_rsslMajVer = majVer;

		_rsslMinVer = minVer;

		_rsslBuffer = rsslBuffer;

		_rsslDictionary = rsslDictionary;

		_rsslDecodeIter.clear();
		int retCode = _rsslDecodeIter.setBufferAndRWFVersion(rsslBuffer, _rsslMajVer, _rsslMinVer);
		if (com.thomsonreuters.upa.codec.CodecReturnCodes.SUCCESS != retCode)
		{
			_errorCode = ErrorCode.ITERATOR_SET_FAILURE;
			return;
		}
		
		retCode = _rsslVector.decode(_rsslDecodeIter);
		switch (retCode)
		{
			case com.thomsonreuters.upa.codec.CodecReturnCodes.NO_DATA :
				_errorCode = ErrorCode.NO_ERROR;
				_rsslVector.flags(0);
				_fillCollection = false;
				clearCollection();
				break;
			case com.thomsonreuters.upa.codec.CodecReturnCodes.SUCCESS :
				_errorCode = ErrorCode.NO_ERROR;
				break;
			case com.thomsonreuters.upa.codec.CodecReturnCodes.ITERATOR_OVERRUN :
				_errorCode = ErrorCode.ITERATOR_OVERRUN;
				break;
			case com.thomsonreuters.upa.codec.CodecReturnCodes.INCOMPLETE_DATA :
				_errorCode = ErrorCode.INCOMPLETE_DATA;
				break;
			default :
				_errorCode = ErrorCode.UNKNOWN_ERROR;
				break;
		}

		if (_errorCode == ErrorCode.NO_ERROR)
		{
			if (_rsslVector.checkHasSetDefs())
			{
				switch (_rsslVector.containerType())
				{
					case com.thomsonreuters.upa.codec.DataTypes.FIELD_LIST :
					{
						if (_rsslLocalFLSetDefDb != null)
							_rsslLocalFLSetDefDb.clear();
						else
								_rsslLocalFLSetDefDb = CodecFactory.createLocalFieldSetDefDb();
						
						_rsslLocalFLSetDefDb.decode(_rsslDecodeIter);
						_rsslLocalSetDefDb = _rsslLocalFLSetDefDb;
						break;
					}
					case com.thomsonreuters.upa.codec.DataTypes.ELEMENT_LIST :
					{
						if (_rsslLocalELSetDefDb != null)
							_rsslLocalELSetDefDb.clear();
						else
								_rsslLocalELSetDefDb = CodecFactory.createLocalElementSetDefDb();
						
						_rsslLocalELSetDefDb.decode(_rsslDecodeIter);
						_rsslLocalSetDefDb = _rsslLocalELSetDefDb;
						break;
					}
					default :
						_rsslLocalSetDefDb = null;
						_errorCode = ErrorCode.UNSUPPORTED_DATA_TYPE;
						return;
				}
			}
			else
				_rsslLocalSetDefDb = null;

			int rsslContainerType = _rsslVector.checkHasSummaryData() ? _rsslVector.containerType() : com.thomsonreuters.upa.codec.DataTypes.NO_DATA;
			int dType = dataType(rsslContainerType, _rsslMajVer, _rsslMinVer, _rsslVector.encodedSummaryData());
			_summaryDecoded = dataInstance(_summaryDecoded, dType);
			_summaryDecoded.decode(_rsslVector.encodedSummaryData(), _rsslMajVer, _rsslMinVer, _rsslDictionary, _rsslLocalSetDefDb);
		}
	}
	
	void fillCollection()
	{
		DataImpl load;
	
		clearCollection();
		
		VectorEntryImpl vectorEntry = vectorEntryInstance();
		
		if ( ErrorCode.NO_ERROR != _errorCode)
		{
			load =  dataInstance(vectorEntry._load, DataTypes.ERROR);
			load.decode(_rsslBuffer, _errorCode);
			vectorEntry._load = load;
			_vectorCollection.add(vectorEntry);
			_fillCollection = false;
			return;
		}

		int retCode;
		while ((retCode  = vectorEntry._rsslVectorEntry.decode(_rsslDecodeIter)) != com.thomsonreuters.upa.codec.CodecReturnCodes.END_OF_CONTAINER)
		{
			switch(retCode)
			{
			case com.thomsonreuters.upa.codec.CodecReturnCodes.SUCCESS :
				int rsslContainerType = (vectorEntry._rsslVectorEntry.action() != VectorEntryActions.DELETE && vectorEntry._rsslVectorEntry.action() != VectorEntryActions.CLEAR)?
														_rsslVector.containerType() : com.thomsonreuters.upa.codec.DataTypes.NO_DATA;
				int dType = dataType(rsslContainerType, _rsslMajVer, _rsslMinVer, vectorEntry._rsslVectorEntry.encodedData());
				load = dataInstance(vectorEntry._load, dType);
				load.decode(vectorEntry._rsslVectorEntry.encodedData(), _rsslMajVer, _rsslMinVer, _rsslDictionary, _rsslLocalSetDefDb);
				break;
			case com.thomsonreuters.upa.codec.CodecReturnCodes.INCOMPLETE_DATA :
				load = dataInstance(vectorEntry._load, DataTypes.ERROR);
				load.decode(vectorEntry._rsslVectorEntry.encodedData(),ErrorCode.INCOMPLETE_DATA);
				break;
			case com.thomsonreuters.upa.codec.CodecReturnCodes.UNSUPPORTED_DATA_TYPE :
				load = dataInstance(vectorEntry._load, DataTypes.ERROR);
				load.decode(vectorEntry._rsslVectorEntry.encodedData(),ErrorCode.UNSUPPORTED_DATA_TYPE);
				break;
			default :
				load = dataInstance(vectorEntry._load, DataTypes.ERROR);
				load.decode(vectorEntry._rsslVectorEntry.encodedData(),ErrorCode.UNKNOWN_ERROR);
				break;
			}
			
			vectorEntry._load = load;
			_vectorCollection.add(vectorEntry);
			
			vectorEntry = vectorEntryInstance();
		}
		
		vectorEntry.returnToPool();
		
		_fillCollection = false;
	}


	Buffer encodedData()
	{
		if (_encodeComplete || (_rsslEncodeIter == null))
			return _rsslBuffer; 
		
		if (_vectorCollection.isEmpty())
			throw ommIUExcept().message("Series to be encoded is empty.");
		
		int ret = _rsslEncodeIter.setBufferAndRWFVersion(_rsslBuffer, _rsslMajVer, _rsslMinVer);
	    if (ret != CodecReturnCodes.SUCCESS)
	    {
	    	String errText = errorString().append("Failed to setBufferAndRWFVersion on rssl encode iterator. Reason='")
	    								.append(CodecReturnCodes.toString(ret))
	    								.append("'").toString();
	    	throw ommIUExcept().message(errText);
	    }
	    
	    VectorEntryImpl firstEntry = (VectorEntryImpl)_vectorCollection.get(0);
	    int entryType = firstEntry._entryDataType;
	    
	    if ( entryType != com.thomsonreuters.upa.codec.DataTypes.UNKNOWN )
	    {
	    	_rsslVector.containerType(entryType);
	    }
	    else
	    {
	    	_rsslVector.containerType(Utilities.toRsslDataType(firstEntry.loadType()));
	    }

	    while ((ret = _rsslVector.encodeInit(_rsslEncodeIter, 0, 0)) == CodecReturnCodes.BUFFER_TOO_SMALL)
	    {
	    	_rsslBuffer = Utilities.realignBuffer(_rsslEncodeIter, _rsslBuffer.capacity() * 2);
	    }
	    
	    if (ret != CodecReturnCodes.SUCCESS)
	    {
	    	String errText = errorString().append("Failed to intialize encoding on rssl series. Reason='")
	    								.append(CodecReturnCodes.toString(ret))
	    								.append("'").toString();
	    	throw ommIUExcept().message(errText);
	    }
	    
	    VectorEntryImpl vectorEntry;
		for (com.thomsonreuters.ema.access.VectorEntry entry  : _vectorCollection)
		{
			vectorEntry = ((VectorEntryImpl)entry);
			if (entryType != vectorEntry._entryDataType)
			{
				String errText = errorString().append("Attempt to add entry of ")
						.append(com.thomsonreuters.upa.codec.DataTypes.toString(vectorEntry._entryDataType))
						.append("while Series contains=")
						.append(com.thomsonreuters.upa.codec.DataTypes.toString(entryType)).toString();
				throw ommIUExcept().message(errText);
			}
			
			while ((ret = vectorEntry._rsslVectorEntry.encode(_rsslEncodeIter)) == CodecReturnCodes.BUFFER_TOO_SMALL)
			{
				_rsslBuffer = Utilities.realignBuffer(_rsslEncodeIter, _rsslBuffer.capacity() * 2);
			}

			if (ret != CodecReturnCodes.SUCCESS)
		    {
				String errText = errorString().append("Failed to ")
						.append("rsslVectorEntry.encode()")
						.append(" while encoding rssl series. Reason='")
						.append(CodecReturnCodes.toString(ret))
						.append("'").toString();
				throw ommIUExcept().message(errText);
		    }
		 }
		 
		ret =  _rsslVector.encodeComplete(_rsslEncodeIter, true);
	    if (ret != CodecReturnCodes.SUCCESS)
	    {
	    	String errText = errorString().append("Failed to complete encoding on rssl series. Reason='")
	    								.append(CodecReturnCodes.toString(ret))
	    								.append("'").toString();
	        throw ommIUExcept().message(errText);
	    }
	     
	    _encodeComplete = true;
	    return _rsslBuffer;
	}
	
	private VectorEntryImpl vectorEntryInstance()
	{
		VectorEntryImpl retData = (VectorEntryImpl)_objManager._vectorEntryPool.poll();
        if(retData == null)
        {
        	retData = new VectorEntryImpl(com.thomsonreuters.upa.codec.CodecFactory.createVectorEntry(), noDataInstance(), _objManager);
        	_objManager._vectorEntryPool.updatePool(retData);
        }
        else
        	retData._rsslVectorEntry.clear();
        
        return retData;
	}
	
	private void clearCollection()
	{
		int collectionSize = _vectorCollection.size();
		if (collectionSize > 0)
		{
			for (int index = 0; index < collectionSize; ++index)
				((VectorEntryImpl)_vectorCollection.get(index)).returnToPool();
	
			_vectorCollection.clear();
		}
	}
}