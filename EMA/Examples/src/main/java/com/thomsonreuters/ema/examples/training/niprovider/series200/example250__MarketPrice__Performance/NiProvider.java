///*|-----------------------------------------------------------------------------
// *|            This source code is provided under the Apache 2.0 license      --
// *|  and is provided AS IS with no warranty or guarantee of fit for purpose.  --
// *|                See the project's LICENSE.md for details.                  --
// *|           Copyright Thomson Reuters 2016. All rights reserved.            --
///*|-----------------------------------------------------------------------------

package com.thomsonreuters.ema.examples.training.niprovider.series200.example250__MarketPrice__Performance;

import java.nio.ByteBuffer;

import com.thomsonreuters.ema.access.EmaFactory;
import com.thomsonreuters.ema.access.FieldList;
import com.thomsonreuters.ema.access.OmmException;
import com.thomsonreuters.ema.access.OmmNiProviderConfig;
import com.thomsonreuters.ema.access.OmmProvider;
import com.thomsonreuters.ema.access.OmmReal;
import com.thomsonreuters.ema.access.OmmState;

public class NiProvider {

	public static void main(String[] args)
	{
		OmmProvider provider = null;
		try
		{
			OmmNiProviderConfig config = EmaFactory.createOmmNiProviderConfig();
			
			provider = EmaFactory.createOmmProvider(config.username("user"));
			
			int itemNumber = 1000;
			int sleepTime = 1000;
			
			FieldList fieldList = EmaFactory.createFieldList();
			
			fieldList.add(EmaFactory.createFieldEntry().intValue(1, 6560));
			fieldList.add(EmaFactory.createFieldEntry().intValue(2, 66));
			fieldList.add(EmaFactory.createFieldEntry().intValue(3855, 52832001));
			fieldList.add(EmaFactory.createFieldEntry().rmtes(296, ByteBuffer.wrap("BOS".getBytes())));
			fieldList.add(EmaFactory.createFieldEntry().time(375, 21, 0));
			fieldList.add(EmaFactory.createFieldEntry().time(1025, 14, 40, 32));
			fieldList.add(EmaFactory.createFieldEntry().real(22, 14400, OmmReal.MagnitudeType.EXPONENT_NEG_2));
			fieldList.add(EmaFactory.createFieldEntry().real(25, 14700, OmmReal.MagnitudeType.EXPONENT_NEG_2));
			fieldList.add(EmaFactory.createFieldEntry().real(30, 9, OmmReal.MagnitudeType.EXPONENT_0));
			fieldList.add(EmaFactory.createFieldEntry().real(31, 19, OmmReal.MagnitudeType.EXPONENT_0));
			
			long start = System.currentTimeMillis();
			
			for (int handle = 0; handle < itemNumber; ++handle)
			{
				provider.submit( EmaFactory.createRefreshMsg().serviceName("TEST_NI_PUB").name("RTR" + handle +".N")
						.state(OmmState.StreamState.OPEN, OmmState.DataState.OK, OmmState.StatusCode.NONE, "UnSolicited Refresh Completed")
						.payload(fieldList).complete(true), handle);
			}

			long end = System.currentTimeMillis();
			
			float timeSpent = (float)(end - start) / (float) 1000;
			
			System.out.println("total refresh count = " + itemNumber + 
					"\ntotal time = " + timeSpent + " sec" +
					"\nrefresh rate = " + (float)itemNumber / timeSpent + " refresh per sec");
			
			long midpoint = end = start = System.currentTimeMillis();
			int updateCount = 0;
			
			while (start + 300000 > end)
			{
				for (int handle = 0; handle < itemNumber; ++handle)
				{
					fieldList.clear();
					fieldList.add(EmaFactory.createFieldEntry().time(1025, 14, 40, 32));
					fieldList.add(EmaFactory.createFieldEntry().intValue(3855, 52832001));
					fieldList.add(EmaFactory.createFieldEntry().real(22, 14400 + (((handle & 0x1) == 1) ? 1 : 10), OmmReal.MagnitudeType.EXPONENT_NEG_2));
					fieldList.add(EmaFactory.createFieldEntry().real(30, 10 + (((handle & 0x1) == 1) ? 10 : 20), OmmReal.MagnitudeType.EXPONENT_0));
					fieldList.add(EmaFactory.createFieldEntry().rmtes(296, ByteBuffer.wrap("NAS".getBytes())));
					
					provider.submit( EmaFactory.createUpdateMsg().serviceName("TEST_NI_PUB").name("RTR" + handle + ".N").payload( fieldList ), handle );
					++updateCount;
				}
				
				Thread.sleep(sleepTime);
				
				end = System.currentTimeMillis();
				
				if (end >= midpoint + 1000)
				{
					timeSpent = (float)(end-midpoint) / (float)1000;
					
					System.out.println("update count = " + updateCount +
							"\nupdate rate = " + (float)updateCount / timeSpent + " update per sec");
					
					updateCount = 0;
					midpoint = end;
				}
			}

		} 
		catch (InterruptedException | OmmException excp)
		{
			System.out.println(excp.getMessage());
		}
		finally 
		{
			if (provider != null) provider.uninitialize();
		}
	}
}
