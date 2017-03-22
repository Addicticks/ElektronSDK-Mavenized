package com.thomsonreuters.upa.valueadd.domainrep.rdm.login;

import com.thomsonreuters.upa.codec.Buffer;
import com.thomsonreuters.upa.codec.CodecFactory;
import com.thomsonreuters.upa.codec.CodecReturnCodes;
import com.thomsonreuters.upa.codec.DataTypes;
import com.thomsonreuters.upa.codec.DecodeIterator;
import com.thomsonreuters.upa.codec.EncodeIterator;
import com.thomsonreuters.upa.codec.GenericMsg;
import com.thomsonreuters.upa.codec.GenericMsgFlags;
import com.thomsonreuters.upa.codec.Map;
import com.thomsonreuters.upa.codec.MapEntry;
import com.thomsonreuters.upa.codec.MapEntryActions;
import com.thomsonreuters.upa.codec.MapEntryFlags;
import com.thomsonreuters.upa.codec.MapFlags;
import com.thomsonreuters.upa.codec.Msg;
import com.thomsonreuters.upa.codec.MsgClasses;
import com.thomsonreuters.upa.codec.MsgKey;
import com.thomsonreuters.upa.codec.MsgKeyFlags;
import com.thomsonreuters.upa.rdm.DomainTypes;
import com.thomsonreuters.upa.rdm.ElementNames;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.MsgBaseImpl;

class LoginConsumerConnectionStatusImpl extends MsgBaseImpl
{
    private LoginWarmStandbyInfo warmStandbyInfo;
    private int flags;
    private GenericMsg genericMsg;
    private Map map;
    private MapEntry mapEntry;
    private Buffer tempBuffer;
    private final static String tab = "\t";
    
    public LoginConsumerConnectionStatusImpl()
    {
        warmStandbyInfo = new LoginWarmStandbyInfoImpl();
        genericMsg = (GenericMsg)CodecFactory.createMsg();
        genericMsg.msgClass(MsgClasses.GENERIC);
        genericMsg.domainType(DomainTypes.LOGIN);
        map = CodecFactory.createMap();
        mapEntry = CodecFactory.createMapEntry();
        tempBuffer = CodecFactory.createBuffer();
    }

    public int copy(LoginConsumerConnectionStatus destConnStatusMsg)
    {
        assert (destConnStatusMsg != null) : "destConnStatusMsg must be non-null";
        destConnStatusMsg.streamId(streamId());
        if(checkHasWarmStandbyInfo())
        {
            destConnStatusMsg.applyHasWarmStandbyInfo();
            destConnStatusMsg.warmStandbyInfo().warmStandbyMode(warmStandbyInfo().warmStandbyMode());
            destConnStatusMsg.warmStandbyInfo().action(warmStandbyInfo().action());
        }
        return CodecReturnCodes.SUCCESS;
    }
    
    public int flags()
    {
        return flags;
    }
    
    public void flags(int flags)
    {
        this.flags = flags; 
    }
    
    public void clear()
    {
        super.clear();
        warmStandbyInfo.clear();
        flags = 0;
    }

    
    public int encode(EncodeIterator encodeIter)
    {
        // Encode ConsumerConnectionStatus Generic message.
        // Used to send the WarmStandbyMode.

        genericMsg.clear();
        genericMsg.msgClass(MsgClasses.GENERIC);
        genericMsg.domainType(DomainTypes.LOGIN);
        genericMsg.streamId(streamId());
        genericMsg.containerType(DataTypes.MAP);
        genericMsg.flags(GenericMsgFlags.HAS_MSG_KEY);
        genericMsg.msgKey().flags(MsgKeyFlags.HAS_NAME);
        genericMsg.msgKey().name(ElementNames.CONS_CONN_STATUS);
        int ret = genericMsg.encodeInit(encodeIter, 0);
        if (ret != CodecReturnCodes.ENCODE_CONTAINER)
            return CodecReturnCodes.FAILURE;

        map.clear();
        map.flags(MapFlags.NONE);
        map.keyPrimitiveType(DataTypes.ASCII_STRING);
        map.containerType(DataTypes.ELEMENT_LIST);
        ret = map.encodeInit(encodeIter, 0, 0);
        if (ret != CodecReturnCodes.SUCCESS)
            return CodecReturnCodes.FAILURE;

        if (checkHasWarmStandbyInfo())
        {
            mapEntry.clear();
            mapEntry.flags(MapEntryFlags.NONE);
            mapEntry.action(warmStandbyInfo().action());
            if (mapEntry.action() != MapEntryActions.DELETE)
            {
                ret = mapEntry.encodeInit(encodeIter, ElementNames.WARMSTANDBY_INFO, 0);
                
                ret = warmStandbyInfo().encode(encodeIter);
                if (ret != CodecReturnCodes.SUCCESS)
                    return CodecReturnCodes.FAILURE;

                ret = mapEntry.encodeComplete(encodeIter, true);
                if (ret != CodecReturnCodes.SUCCESS)
                    return CodecReturnCodes.FAILURE;
            }
            else
            {
                ret = mapEntry.encode(encodeIter, ElementNames.WARMSTANDBY_INFO);
                if (ret != CodecReturnCodes.SUCCESS)
                    return CodecReturnCodes.FAILURE;
            }

        }

        ret = map.encodeComplete(encodeIter, true);
        if (ret != CodecReturnCodes.SUCCESS)
            return CodecReturnCodes.FAILURE;

        ret = genericMsg.encodeComplete(encodeIter, true);
        if (ret != CodecReturnCodes.SUCCESS)
            return CodecReturnCodes.FAILURE;

        return CodecReturnCodes.SUCCESS;
    }

    public int decode(DecodeIterator dIter, Msg msg)
    {
        if (msg.msgClass() != MsgClasses.GENERIC || msg.domainType() != DomainTypes.LOGIN)
            return CodecReturnCodes.FAILURE;

        MsgKey key = msg.msgKey();

        if (key == null || !key.checkHasName())
            return CodecReturnCodes.FAILURE;

        if (!key.name().equals(ElementNames.CONS_CONN_STATUS))
        {
            // Unknown generic msg name
            return CodecReturnCodes.FAILURE;
        }

        if (msg.containerType() != DataTypes.MAP)
            return CodecReturnCodes.FAILURE;
        
        clear();
        streamId(msg.streamId());

        map.clear();

        int ret = map.decode(dIter);
        if (ret != CodecReturnCodes.SUCCESS)
            return CodecReturnCodes.FAILURE;

        if (!(map.keyPrimitiveType() == DataTypes.ASCII_STRING || map.keyPrimitiveType() == DataTypes.BUFFER))
            return CodecReturnCodes.FAILURE;

        if (map.containerType() != DataTypes.ELEMENT_LIST)
            return CodecReturnCodes.FAILURE;

        mapEntry.clear();
        tempBuffer.clear();
        while ((ret = mapEntry.decode(dIter, tempBuffer)) != CodecReturnCodes.END_OF_CONTAINER)
        {
            if (ret < CodecReturnCodes.SUCCESS)
                return CodecReturnCodes.FAILURE;

            if (tempBuffer.equals(ElementNames.WARMSTANDBY_INFO))
            {
                flags(flags | LoginConsumerConnectionStatusFlags.HAS_WARM_STANDBY_INFO);
                if (mapEntry.action() != MapEntryActions.DELETE)
                {
                    ret = warmStandbyInfo().decode(dIter, msg);
                    if (ret != CodecReturnCodes.SUCCESS)
                        return CodecReturnCodes.FAILURE;
                }
                warmStandbyInfo().action(mapEntry.action());
            }
        }
        
        return CodecReturnCodes.SUCCESS;
    }

    public LoginWarmStandbyInfo warmStandbyInfo()
    {
        return warmStandbyInfo;
    }
    
    public void warmStandbyInfo(LoginWarmStandbyInfo warmStandbyInfo)
    {
        ((LoginWarmStandbyInfoImpl)warmStandbyInfo()).copyReferences(warmStandbyInfo);
    }

    public boolean checkHasWarmStandbyInfo()
    {
       return (flags & LoginConsumerConnectionStatusFlags.HAS_WARM_STANDBY_INFO) != 0;
    }
    
    public void applyHasWarmStandbyInfo()
    {
       flags |= LoginConsumerConnectionStatusFlags.HAS_WARM_STANDBY_INFO;
    }
    
    public String toString()
    {
        StringBuilder toStringBuilder = super.buildStringBuffer();
        toStringBuilder.insert(0, "LoginConsumerConnectionStatus: \n");

        if (checkHasWarmStandbyInfo())
        {
            toStringBuilder.append(tab);
            toStringBuilder.append("warmStandbyInfo: ");
            toStringBuilder.append(warmStandbyInfo());
        }

        return toStringBuilder.toString();
    }
    
    @Override
    public int domainType()
    {
        return DomainTypes.LOGIN;
    }
}