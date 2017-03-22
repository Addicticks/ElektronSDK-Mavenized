///*|-----------------------------------------------------------------------------
// *|            This source code is provided under the Apache 2.0 license      --
// *|  and is provided AS IS with no warranty or guarantee of fit for purpose.  --
// *|                See the project's LICENSE.md for details.                  --
// *|           Copyright Thomson Reuters 2015. All rights reserved.            --
///*|-----------------------------------------------------------------------------

package com.thomsonreuters.ema.access;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import com.thomsonreuters.upa.codec.Buffer;
import com.thomsonreuters.upa.codec.CodecFactory;
import com.thomsonreuters.upa.codec.CodecReturnCodes;

class OmmArrayEntryImpl extends EntryImpl implements OmmArrayEntry
{
	protected com.thomsonreuters.upa.codec.ArrayEntry	_rsslArrayEntry;
	protected int _entryDataType;
	
	OmmArrayEntryImpl()
	{
		_rsslArrayEntry = CodecFactory.createArrayEntry();
	}
	
	OmmArrayEntryImpl(com.thomsonreuters.upa.codec.ArrayEntry rsslArrayEntry, DataImpl load)
	{
		super(load);
		_rsslArrayEntry = rsslArrayEntry;
	}
	
	@Override
	public String toString()
	{
		_toString.setLength(0);
		_toString.append("OmmArrayEntry ")
				 .append(" dataType=\"").append(DataType.asString(_load.dataType())).append("\"")
				 .append(" value=\"").append(_load.toString()).append("\"\n");

		return _toString.toString();
	}

	@Override
	public OmmArrayEntry intValue(long value)
	{
		if ( _previousEncodingType != com.thomsonreuters.upa.codec.DataTypes.INT )
		{
			GlobalPool.lock();
			GlobalPool.returnPool(_previousEncodingType, _entryData);
			_entryData = GlobalPool.getInt();
			GlobalPool.unlock();
			
			_previousEncodingType = com.thomsonreuters.upa.codec.DataTypes.INT;
		}
		
		((com.thomsonreuters.upa.codec.Int)_entryData).value(value);
		
		_entryDataType = com.thomsonreuters.upa.codec.DataTypes.INT;
		return this;
	}

	@Override
	public OmmArrayEntry uintValue(long value)
	{
		if ( _previousEncodingType != com.thomsonreuters.upa.codec.DataTypes.UINT )
		{
			GlobalPool.lock();
			GlobalPool.returnPool(_previousEncodingType, _entryData);
			_entryData = GlobalPool.getUInt();
			GlobalPool.unlock();
			
			_previousEncodingType = com.thomsonreuters.upa.codec.DataTypes.UINT;
		}
		
		((com.thomsonreuters.upa.codec.UInt)_entryData).value(value) ;
		
		_entryDataType = com.thomsonreuters.upa.codec.DataTypes.UINT;
		
		return this;
	}

	@Override
	public OmmArrayEntry uintValue(BigInteger value)
	{
		if ( _previousEncodingType != com.thomsonreuters.upa.codec.DataTypes.UINT )
		{
			GlobalPool.lock();
			GlobalPool.returnPool(_previousEncodingType, _entryData);
			_entryData = GlobalPool.getUInt();
			GlobalPool.unlock();
			
			_previousEncodingType = com.thomsonreuters.upa.codec.DataTypes.UINT;
		}
		
		((com.thomsonreuters.upa.codec.UInt)_entryData).value(value) ;
		
		_entryDataType = com.thomsonreuters.upa.codec.DataTypes.UINT;
		
		return this;
	}

	@Override
	public OmmArrayEntry real(long mantissa, int magnitudeType)
	{
		if ( _previousEncodingType != com.thomsonreuters.upa.codec.DataTypes.REAL )
		{
			GlobalPool.lock();
			GlobalPool.returnPool(_previousEncodingType, _entryData);
			_entryData = GlobalPool.getReal();
			GlobalPool.unlock();
			
			_previousEncodingType = com.thomsonreuters.upa.codec.DataTypes.REAL;
		}
		
		if (CodecReturnCodes.SUCCESS != ((com.thomsonreuters.upa.codec.Real)_entryData).value(mantissa, magnitudeType) )
		{
			String errText = errorString().append("Attempt to specify invalid real value. Passed mantissa, magnitudeType are='" )
										.append( mantissa ).append( " / " )
										.append( magnitudeType ).append( "'." ).toString();
			throw ommIUExcept().message(errText);
		}
		
		_entryDataType = com.thomsonreuters.upa.codec.DataTypes.REAL;
		
		return this;
	}

	@Override
	public OmmArrayEntry realFromDouble(double value)
	{
		return realFromDouble(value, OmmReal.MagnitudeType.EXPONENT_0);
	}

	@Override
	public OmmArrayEntry realFromDouble(double value, int magnitudeType)
	{
		if ( _previousEncodingType != com.thomsonreuters.upa.codec.DataTypes.REAL )
		{
			GlobalPool.lock();
			GlobalPool.returnPool(_previousEncodingType, _entryData);
			_entryData = GlobalPool.getReal();
			GlobalPool.unlock();
			
			_previousEncodingType = com.thomsonreuters.upa.codec.DataTypes.REAL;
		}
		
		if (CodecReturnCodes.SUCCESS != ((com.thomsonreuters.upa.codec.Real)_entryData).value(value, magnitudeType) )
		{
			String errText = errorString().append("Attempt to specify invalid real value. Passed in value,  magnitudeType are='" )
										.append( value ).append( " / " )
										.append( magnitudeType ).append( "'." ).toString();
			throw ommIUExcept().message(errText);
		}
		
		_entryDataType = com.thomsonreuters.upa.codec.DataTypes.REAL;
		
		return this;
	}

	@Override
	public OmmArrayEntry floatValue(float value)
	{
		if ( _previousEncodingType != com.thomsonreuters.upa.codec.DataTypes.FLOAT )
		{
			GlobalPool.lock();
			GlobalPool.returnPool(_previousEncodingType, _entryData);
			_entryData = GlobalPool.getFloat();
			GlobalPool.unlock();
			
			_previousEncodingType = com.thomsonreuters.upa.codec.DataTypes.FLOAT;
		}
		
		((com.thomsonreuters.upa.codec.Float)_entryData).value(value);
		
		_entryDataType = com.thomsonreuters.upa.codec.DataTypes.FLOAT;
		
		return this;
	}

	@Override
	public OmmArrayEntry doubleValue(double value)
	{
		if ( _previousEncodingType != com.thomsonreuters.upa.codec.DataTypes.DOUBLE )
		{
			GlobalPool.lock();
			GlobalPool.returnPool(_previousEncodingType, _entryData);
			_entryData = GlobalPool.getDouble();
			GlobalPool.unlock();
			
			_previousEncodingType = com.thomsonreuters.upa.codec.DataTypes.DOUBLE;
		}
		
		((com.thomsonreuters.upa.codec.Double)_entryData).value(value);
		
		_entryDataType = com.thomsonreuters.upa.codec.DataTypes.DOUBLE;
		
		return this;
	}

	@Override
	public OmmArrayEntry date(int year, int month, int day)
	{
		_entryData = dateValue(year, month, day);
		
		_entryDataType = com.thomsonreuters.upa.codec.DataTypes.DATE;
		
		return this;
	}

	@Override
	public OmmArrayEntry time(int hour, int minute)
	{
		return time(hour, minute, 0, 0, 0, 0);
	}

	@Override
	public OmmArrayEntry time(int hour, int minute, int second)
	{
		return time(hour, minute, second, 0, 0, 0);
	}

	@Override
	public OmmArrayEntry time(int hour, int minute, int second, int millisecond)
	{
		return time(hour, minute, second,  millisecond, 0, 0);
	}

	@Override
	public OmmArrayEntry time(int hour, int minute, int second, int millisecond, int microsecond)
	{
		return time(hour, minute, second,  millisecond, microsecond, 0);
	}

	@Override
	public OmmArrayEntry time(int hour, int minute, int second, int millisecond, int microsecond, int nanosecond)
	{
		_entryData = timeValue(hour, minute, second, millisecond, microsecond, nanosecond);
		
		_entryDataType = com.thomsonreuters.upa.codec.DataTypes.TIME;
		
		return this;
	}

	@Override
	public OmmArrayEntry dateTime(int year, int month, int day)
	{
		return dateTime(year, month, day, 0, 0, 0, 0, 0, 0);
	}

	@Override
	public OmmArrayEntry dateTime(int year, int month, int day, int hour)
	{
		return dateTime(year, month, day, hour, 0, 0, 0, 0, 0);
	}

	@Override
	public OmmArrayEntry dateTime(int year, int month, int day, int hour, int minute)
	{
		return dateTime(year, month, day, hour, minute, 0, 0, 0, 0);
	}

	@Override
	public OmmArrayEntry dateTime(int year, int month, int day, int hour, int minute, int second)
	{
		return dateTime(year, month, day, hour, minute, second, 0, 0, 0);
	}

	@Override
	public OmmArrayEntry dateTime(int year, int month, int day, int hour, int minute, int second, int millisecond)
	{
		return dateTime(year, month, day, hour, minute, second, millisecond, 0, 0);
	}

	@Override
	public OmmArrayEntry dateTime(int year, int month, int day, int hour, int minute, int second, int millisecond,
			int microsecond)
	{
		return dateTime(year, month, day, hour, minute, second, millisecond, microsecond, 0);
	}

	@Override
	public OmmArrayEntry dateTime(int year, int month, int day, int hour, int minute, int second, int millisecond,
			int microsecond, int nanosecond)
	{
		_entryData = dateTimeValue(year, month, day, hour, minute, second, millisecond, microsecond, nanosecond);
		
		_entryDataType = com.thomsonreuters.upa.codec.DataTypes.DATETIME;
		
		return this;
	}

	@Override
	public OmmArrayEntry qos(int timeliness)
	{
		return qos(timeliness, OmmQos.Rate.TICK_BY_TICK);
	}

	@Override
	public OmmArrayEntry qos(int timeliness, int rate)
	{
		if ( _previousEncodingType != com.thomsonreuters.upa.codec.DataTypes.QOS )
		{
			GlobalPool.lock();
			GlobalPool.returnPool(_previousEncodingType, _entryData);
			_entryData = GlobalPool.getQos();
			GlobalPool.unlock();
			
			_previousEncodingType = com.thomsonreuters.upa.codec.DataTypes.QOS;
		}
		
		Utilities.toRsslQos(rate, timeliness, (com.thomsonreuters.upa.codec.Qos)_entryData);
		
		_entryDataType = com.thomsonreuters.upa.codec.DataTypes.QOS;
		
		return this;
	}

	@Override
	public OmmArrayEntry state(int streamState)
	{
		return state(streamState, OmmState.DataState.OK, OmmState.StatusCode.NONE, DataImpl.EMPTY_STRING);
	}

	@Override
	public OmmArrayEntry state(int streamState, int dataState)
	{
		return state(streamState, dataState, OmmState.StatusCode.NONE, DataImpl.EMPTY_STRING);
	}

	@Override
	public OmmArrayEntry state(int streamState, int dataState, int statusCode)
	{
		return state(streamState, dataState, statusCode, DataImpl.EMPTY_STRING);
	}

	@Override
	public OmmArrayEntry state(int streamState, int dataState, int statusCode, String statusText)
	{
		_entryData = stateValue(streamState, dataState, statusCode, statusText);
		
		_entryDataType = com.thomsonreuters.upa.codec.DataTypes.STATE;
				
		return this;
	}

	@Override
	public OmmArrayEntry enumValue(int value)
	{
		if ( _previousEncodingType != com.thomsonreuters.upa.codec.DataTypes.ENUM )
		{
			GlobalPool.lock();
			GlobalPool.returnPool(_previousEncodingType, _entryData);
			_entryData = GlobalPool.getEnum();
			GlobalPool.unlock();
			
			_previousEncodingType = com.thomsonreuters.upa.codec.DataTypes.ENUM; 
		}
		
		if (CodecReturnCodes.SUCCESS != ((com.thomsonreuters.upa.codec.Enum)_entryData).value(value) )
		{
			String errText = errorString().append("Attempt to specify invalid enum. Passed in value is='" )
					.append( value ).append( "." ).toString();
				throw ommIUExcept().message(errText);
		}
		
		_entryDataType = com.thomsonreuters.upa.codec.DataTypes.ENUM;
		
		return this;
	}

	@Override
	public OmmArrayEntry buffer(ByteBuffer value)
	{
		if (value == null)
			throw ommIUExcept().message("Passed in value is null");
		
		if ( _previousEncodingType != com.thomsonreuters.upa.codec.DataTypes.BUFFER )
		{
			GlobalPool.lock();
			GlobalPool.returnPool(_previousEncodingType, _entryData);
			_entryData = GlobalPool.getBuffer();
			GlobalPool.unlock();
			
			_previousEncodingType = com.thomsonreuters.upa.codec.DataTypes.BUFFER;
		}
		
		Utilities.copy(value, (Buffer)_entryData);
		
		_entryDataType = com.thomsonreuters.upa.codec.DataTypes.BUFFER;
		
		return this;
	}

	@Override
	public OmmArrayEntry ascii(String value)
	{
		if (value == null)
			throw ommIUExcept().message("Passed in value is null");
		
		if ( _previousEncodingType != com.thomsonreuters.upa.codec.DataTypes.BUFFER )
		{
			GlobalPool.lock();
			GlobalPool.returnPool(_previousEncodingType, _entryData);
			_entryData = GlobalPool.getBuffer();
			GlobalPool.unlock();
			
			_previousEncodingType = com.thomsonreuters.upa.codec.DataTypes.BUFFER;
		}
		
		((Buffer)_entryData).data(value);
		
		_entryDataType = com.thomsonreuters.upa.codec.DataTypes.ASCII_STRING;
		
		return this;
	}

	@Override
	public OmmArrayEntry utf8(ByteBuffer value)
	{
		if (value == null)
			throw ommIUExcept().message("Passed in value is null");
		
		if ( _previousEncodingType != com.thomsonreuters.upa.codec.DataTypes.BUFFER )
		{
			GlobalPool.lock();
			GlobalPool.returnPool(_previousEncodingType, _entryData);
			_entryData = GlobalPool.getBuffer();
			GlobalPool.unlock();
			
			_previousEncodingType = com.thomsonreuters.upa.codec.DataTypes.BUFFER;
		}
		
		Utilities.copy(value, (Buffer)_entryData);
		
		_entryDataType = com.thomsonreuters.upa.codec.DataTypes.UTF8_STRING;
		
		return this;
	}

	@Override
	public OmmArrayEntry utf8(String value)
	{
		if (value == null)
			throw ommIUExcept().message("Passed in value is null");
		
		if ( _previousEncodingType != com.thomsonreuters.upa.codec.DataTypes.BUFFER )
		{
			GlobalPool.lock();
			GlobalPool.returnPool(_previousEncodingType, _entryData);
			_entryData = GlobalPool.getBuffer();
			GlobalPool.unlock();
			
			_previousEncodingType = com.thomsonreuters.upa.codec.DataTypes.BUFFER;
		}
		
		((Buffer)_entryData).data(value);
		
		_entryDataType = com.thomsonreuters.upa.codec.DataTypes.UTF8_STRING;
		
		return this;
	}

	@Override
	public OmmArrayEntry rmtes(ByteBuffer value)
	{
		if (value == null)
			throw ommIUExcept().message("Passed in value is null");
		
		if ( _previousEncodingType != com.thomsonreuters.upa.codec.DataTypes.BUFFER )
		{
			GlobalPool.lock();
			GlobalPool.returnPool(_previousEncodingType, _entryData);
			_entryData = GlobalPool.getBuffer();
			GlobalPool.unlock();
			
			_previousEncodingType = com.thomsonreuters.upa.codec.DataTypes.BUFFER;
		}
		
		Utilities.copy(value, (Buffer)_entryData);
		
		_entryDataType = com.thomsonreuters.upa.codec.DataTypes.RMTES_STRING;
		
		return this;
	}

	@Override
	public OmmArrayEntry codeInt()
	{
		_entryDataType = com.thomsonreuters.upa.codec.DataTypes.INT;
		
		return this;
	}

	@Override
	public OmmArrayEntry codeUInt()
	{
		_entryDataType = com.thomsonreuters.upa.codec.DataTypes.UINT;
		
		return this;
	}

	@Override
	public OmmArrayEntry codeReal()
	{
		_entryDataType = com.thomsonreuters.upa.codec.DataTypes.REAL;
		
		return this;
	}

	@Override
	public OmmArrayEntry codeFloat()
	{
		_entryDataType = com.thomsonreuters.upa.codec.DataTypes.FLOAT;
		
		return this;
	}

	@Override
	public OmmArrayEntry codeDouble()
	{
		_entryDataType = com.thomsonreuters.upa.codec.DataTypes.DOUBLE;
		
		return this;
	}

	@Override
	public OmmArrayEntry codeDate()
	{
		_entryDataType = com.thomsonreuters.upa.codec.DataTypes.DATE;
		
		return this;
	}

	@Override
	public OmmArrayEntry codeTime()
	{
		_entryDataType = com.thomsonreuters.upa.codec.DataTypes.TIME;
		
		return this;
	}

	@Override
	public OmmArrayEntry codeDateTime()
	{
		_entryDataType = com.thomsonreuters.upa.codec.DataTypes.DATETIME;
		
		return this;
	}

	@Override
	public OmmArrayEntry codeQos()
	{
		_entryDataType = com.thomsonreuters.upa.codec.DataTypes.QOS;
		
		return this;
	}

	@Override
	public OmmArrayEntry codeState()
	{
		_entryDataType = com.thomsonreuters.upa.codec.DataTypes.STATE;
		
		return this;
	}

	@Override
	public OmmArrayEntry codeEnum()
	{
		_entryDataType = com.thomsonreuters.upa.codec.DataTypes.ENUM;
		
		return this;
	}

	@Override
	public OmmArrayEntry codeBuffer()
	{
		_entryDataType = com.thomsonreuters.upa.codec.DataTypes.BUFFER;
		
		return this;
	}

	@Override
	public OmmArrayEntry codeAscii()
	{
		_entryDataType = com.thomsonreuters.upa.codec.DataTypes.ASCII_STRING;
		
		return this;
	}

	@Override
	public OmmArrayEntry codeUtf8()
	{
		_entryDataType = com.thomsonreuters.upa.codec.DataTypes.UTF8_STRING;
		
		return this;
	}

	@Override
	public OmmArrayEntry codeRmtes()
	{
		_entryDataType = com.thomsonreuters.upa.codec.DataTypes.RMTES_STRING;
		
		return this;
	}
}