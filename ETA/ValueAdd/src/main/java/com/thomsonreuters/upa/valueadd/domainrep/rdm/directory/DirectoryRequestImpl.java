package com.thomsonreuters.upa.valueadd.domainrep.rdm.directory;

import com.thomsonreuters.upa.codec.CodecFactory;
import com.thomsonreuters.upa.codec.CodecReturnCodes;
import com.thomsonreuters.upa.codec.DataTypes;
import com.thomsonreuters.upa.codec.DecodeIterator;
import com.thomsonreuters.upa.codec.EncodeIterator;
import com.thomsonreuters.upa.codec.Msg;
import com.thomsonreuters.upa.codec.MsgClasses;
import com.thomsonreuters.upa.codec.MsgKey;
import com.thomsonreuters.upa.codec.RequestMsg;
import com.thomsonreuters.upa.rdm.Directory;
import com.thomsonreuters.upa.rdm.DomainTypes;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.MsgBaseImpl;

class DirectoryRequestImpl extends MsgBaseImpl
{
    private long filter;
    private int serviceId;
    private int flags;

    private final static String eol = System.getProperty("line.separator");
    private final static String tab = "\t";
    private RequestMsg requestMsg = (RequestMsg)CodecFactory.createMsg();

    DirectoryRequestImpl()
    {
    }

    public void flags(int flags)
    {
        this.flags = flags;
    }

    public int flags()
    {
        return flags;
    }

    public void clear()
    {
        super.clear();
        flags = 0;
        filter = 0;
        serviceId = 0;
    }

    public long filter()
    {
        return filter;
    }

    public void filter(long filter)
    {
        this.filter = filter;
    }

    public int serviceId()
    {
        return serviceId;
    }

    public void applyStreaming()
    {
        flags |= DirectoryRequestFlags.STREAMING;
    }

    public boolean checkStreaming()
    {
        return (flags & DirectoryRequestFlags.STREAMING) != 0;
    }

    public void applyHasServiceId()
    {
        flags |= DirectoryRequestFlags.HAS_SERVICE_ID;
    }

    public boolean checkHasServiceId()
    {
        return (flags & DirectoryRequestFlags.HAS_SERVICE_ID) != 0;
    }

    public void serviceId(int serviceId)
    {
        assert(checkHasServiceId());
        this.serviceId = serviceId;
    }

    public int encode(EncodeIterator encodeIter)
    {
        requestMsg.clear();

        requestMsg.msgClass(MsgClasses.REQUEST);
        requestMsg.streamId(streamId());
        requestMsg.domainType(DomainTypes.SOURCE);
        requestMsg.containerType(DataTypes.NO_DATA);

        if (checkStreaming())
            requestMsg.applyStreaming();

        requestMsg.msgKey().applyHasFilter();
        requestMsg.msgKey().filter(filter());

        if (checkHasServiceId())
        {
            requestMsg.msgKey().applyHasServiceId();
            requestMsg.msgKey().serviceId(serviceId());
        }

        return requestMsg.encode(encodeIter);
    }

    public int decode(DecodeIterator dIter, Msg msg)
    {
        clear();
        if (msg.msgClass() != MsgClasses.REQUEST)
            return CodecReturnCodes.FAILURE;

        RequestMsg requestMsg = (RequestMsg)msg;

        if (requestMsg.checkStreaming())
            applyStreaming();

        MsgKey msgKey = msg.msgKey();
        if (msgKey == null || !msgKey.checkHasFilter())
            return CodecReturnCodes.FAILURE;

        if (msgKey.checkHasFilter())
            filter(requestMsg.msgKey().filter());
        streamId(msg.streamId());
        if (msgKey.checkHasServiceId())
        {
            applyHasServiceId();
            serviceId(msgKey.serviceId());
        }
        return CodecReturnCodes.SUCCESS;
    }

    public int copy(DirectoryRequest destRequestMsg)
    {
        assert (destRequestMsg != null) : "destRequestMsg must be non-null";
        destRequestMsg.streamId(streamId());

        destRequestMsg.filter(filter());

        if (checkHasServiceId())
        {
            destRequestMsg.applyHasServiceId();
            destRequestMsg.serviceId(serviceId());
        }

        if (checkStreaming())
        {
            destRequestMsg.applyStreaming();
        }

        return CodecReturnCodes.SUCCESS;
    }

    public String toString()
    {
        StringBuilder stringBuf = super.buildStringBuffer();
        stringBuf.insert(0, "DirectoryRequest: \n");

        if (checkHasServiceId())
        {
            stringBuf.append(tab);
            stringBuf.append("serviceId: ");
            stringBuf.append(serviceId());
            stringBuf.append(eol);
        }

        stringBuf.append(tab);
        stringBuf.append("streaming: ");
        stringBuf.append(checkStreaming());
        stringBuf.append(eol);

        stringBuf.append(tab);
        stringBuf.append("filter: ");
        long filter = filter();
        boolean addOr = false;
        if ((filter & Directory.ServiceFilterFlags.INFO) != 0)
        {
            stringBuf.append("INFO");
            addOr = true;
        }
        if ((filter & Directory.ServiceFilterFlags.DATA) != 0)
        {
            if (addOr)
                stringBuf.append(" | ");
            stringBuf.append("DATA");
            addOr = true;
        }
        if ((filter & Directory.ServiceFilterFlags.GROUP) != 0)
        {
            if (addOr)
                stringBuf.append(" | ");
            stringBuf.append("GROUP");
            addOr = true;
        }
        if ((filter & Directory.ServiceFilterFlags.LINK) != 0)
        {
            if (addOr)
                stringBuf.append(" | ");
            stringBuf.append("LINK");
            addOr = true;
        }
        if ((filter & Directory.ServiceFilterFlags.LOAD) != 0)
        {
            if (addOr)
                stringBuf.append(" | ");
            stringBuf.append("LOAD");
            addOr = true;
        }
        if ((filter & Directory.ServiceFilterFlags.STATE) != 0)
        {
            if (addOr)
                stringBuf.append(" | ");
            stringBuf.append("STATE");
        }
        stringBuf.append(eol);

        return stringBuf.toString();
    }
    
    @Override
    public int domainType()
    {
        return DomainTypes.SOURCE;
    }
}