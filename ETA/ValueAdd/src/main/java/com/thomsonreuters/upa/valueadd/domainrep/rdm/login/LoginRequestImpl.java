package com.thomsonreuters.upa.valueadd.domainrep.rdm.login;

import java.nio.ByteBuffer;

import com.thomsonreuters.upa.codec.Buffer;
import com.thomsonreuters.upa.codec.CodecFactory;
import com.thomsonreuters.upa.codec.CodecReturnCodes;
import com.thomsonreuters.upa.codec.DataTypes;
import com.thomsonreuters.upa.codec.DecodeIterator;
import com.thomsonreuters.upa.codec.ElementEntry;
import com.thomsonreuters.upa.codec.ElementList;
import com.thomsonreuters.upa.codec.EncodeIterator;
import com.thomsonreuters.upa.codec.Msg;
import com.thomsonreuters.upa.codec.MsgClasses;
import com.thomsonreuters.upa.codec.MsgKey;
import com.thomsonreuters.upa.codec.RequestMsg;
import com.thomsonreuters.upa.codec.RequestMsgFlags;
import com.thomsonreuters.upa.codec.UInt;
import com.thomsonreuters.upa.rdm.DomainTypes;
import com.thomsonreuters.upa.rdm.ElementNames;
import com.thomsonreuters.upa.rdm.Login;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.MsgBaseImpl;

class LoginRequestImpl extends MsgBaseImpl
{
    private Buffer userName;
    private int userNameType;
    private int flags;
    
    private LoginAttrib attrib;
    private long downloadConnectionConfig;
    private Buffer instanceId;
    private Buffer password;
    private long role;

    private static String defaultUsername;
    
    private ElementEntry elementEntry = CodecFactory.createElementEntry();
    private ElementList elementList = CodecFactory.createElementList();
    private UInt tmpUInt = CodecFactory.createUInt();
    private RequestMsg requestMsg = (RequestMsg)CodecFactory.createMsg();
    private final static String eol = System.getProperty("line.separator");
    private final static String tab = "\t";

    
    public void flags(int flags)
    {
        this.flags = flags;
    }

    public int flags()
    {
        return flags;
    }

    public int copy(LoginRequest destRequestMsg)
    {
        assert (destRequestMsg != null) : "destRequestMsg must be non-null";

        destRequestMsg.streamId(streamId());
        destRequestMsg.flags(flags());
        // username
        {
            ByteBuffer byteBuffer = ByteBuffer.allocate(this.userName.length());
            this.userName.copy(byteBuffer);
            destRequestMsg.userName().data(byteBuffer);
        }
        
        if (checkHasUserNameType())
        {
            destRequestMsg.applyHasUserNameType();
            destRequestMsg.userNameType(userNameType);
        }
        if (checkHasAttrib())
        {
            destRequestMsg.applyHasAttrib();
            attrib().copy(destRequestMsg.attrib());
        }
        if (checkHasDownloadConnectionConfig())
        {
            destRequestMsg.applyHasDownloadConnectionConfig();
            destRequestMsg.downloadConnectionConfig(downloadConnectionConfig);
        }
        if (checkHasInstanceId())
        {
            ByteBuffer byteBuffer = ByteBuffer.allocate(this.instanceId.length());
            this.instanceId.copy(byteBuffer);
            destRequestMsg.applyHasInstanceId();
            destRequestMsg.instanceId().data(byteBuffer);
        }
        if (checkHasPassword())
        {
            ByteBuffer byteBuffer = ByteBuffer.allocate(this.password.length());
            this.password.copy(byteBuffer);
            destRequestMsg.applyHasPassword();
            destRequestMsg.password().data(byteBuffer);
        }
        if (checkHasRole())
        {
            destRequestMsg.applyHasRole();
            destRequestMsg.role(role);
        }
        return CodecReturnCodes.SUCCESS;
    }

    LoginRequestImpl()
    {
        password = CodecFactory.createBuffer();
        instanceId = CodecFactory.createBuffer();
        attrib = new LoginAttribImpl();
        try
        {
            defaultUsername = System.getProperty("user.name");
        }
        catch (Exception e)
        {
            defaultUsername = "upa";
        }
        userName = CodecFactory.createBuffer();
      
        initDefaultRequest(1);
    }
    
   
    
    public void initDefaultRequest(int streamId)
    {
        clear();

        streamId(streamId);
        userName().data(defaultUsername);
        applyHasUserNameType(); 
        userNameType(Login.UserIdTypes.NAME);
        ((LoginAttribImpl)attrib).initDefaultAttrib();
    }

    public void clear()
    {
        super.clear();
        userName.clear();
        userNameType = Login.UserIdTypes.NAME;
        flags = 0;
        password.clear();
        instanceId.clear();
        role = Login.RoleTypes.CONS;
        downloadConnectionConfig = 0;
        attrib.clear();
    }
    
    public int decode(DecodeIterator dIter, Msg msg)
    {
        clear();
        if (msg.msgClass() != MsgClasses.REQUEST)
            return CodecReturnCodes.FAILURE;
        
        RequestMsg requestMsg = (RequestMsg)msg;
        
        //All login requests should be streaming
        if((requestMsg.flags() & RequestMsgFlags.STREAMING) == 0)
            return CodecReturnCodes.FAILURE;
        
        if((requestMsg.flags() & RequestMsgFlags.NO_REFRESH) != 0)
            applyNoRefresh();
        
        if((requestMsg.flags() & RequestMsgFlags.PAUSE) != 0)
            applyPause();
        streamId(msg.streamId());
        
        MsgKey msgKey = msg.msgKey();
        if (msgKey == null || !msgKey.checkHasName() || (msgKey.checkHasAttrib() && msgKey.attribContainerType() != DataTypes.ELEMENT_LIST))
            return CodecReturnCodes.FAILURE;

        Buffer userName = msgKey.name();
        userName().data(userName.data(), userName.position(), userName.length());
        if (msgKey.checkHasNameType())
        {
            applyHasUserNameType();
            userNameType(msgKey.nameType());
        }
  
        if (msgKey.checkHasAttrib())
        {
            int ret = msg.decodeKeyAttrib(dIter, msgKey);
            if (ret != CodecReturnCodes.SUCCESS)
                return ret;

            return decodeAttrib(dIter);
        }

        return CodecReturnCodes.SUCCESS;
    }

    private int decodeAttrib(DecodeIterator dIter)
    {
        elementList.clear();
        int ret = elementList.decode(dIter, null);
        if (ret != CodecReturnCodes.SUCCESS)
            return ret;

        elementEntry.clear();
        while ((ret = elementEntry.decode(dIter)) != CodecReturnCodes.END_OF_CONTAINER)
        {
            if (ret != CodecReturnCodes.SUCCESS)
                return ret;

            if (elementEntry.name().equals(ElementNames.ALLOW_SUSPECT_DATA))
            {
                if (elementEntry.dataType() != DataTypes.UINT)
                    return CodecReturnCodes.FAILURE;

                ret = tmpUInt.decode(dIter);
                if (ret != CodecReturnCodes.SUCCESS)
                    return ret;
                applyHasAttrib();
                attrib.applyHasAllowSuspectData();
                attrib.allowSuspectData(tmpUInt.toLong());
            }
            else if (elementEntry.name().equals(ElementNames.APPID))
            {
                if (elementEntry.dataType() != DataTypes.ASCII_STRING)
                    return CodecReturnCodes.FAILURE;
                applyHasAttrib();
                Buffer applicationId = elementEntry.encodedData();
                attrib.applyHasApplicationId();
                attrib.applicationId().data(applicationId.data(), applicationId.position(), applicationId.length());
            }
            else if (elementEntry.name().equals(ElementNames.APPNAME))
            {
                if (elementEntry.dataType() != DataTypes.ASCII_STRING)
                    return CodecReturnCodes.FAILURE;
                applyHasAttrib();
                Buffer applicationName = elementEntry.encodedData();
                attrib.applyHasApplicationName();
                attrib.applicationName().data(applicationName.data(), applicationName.position(), applicationName.length());
            }
            else if (elementEntry.name().equals(ElementNames.POSITION))
            {
                if (elementEntry.dataType() != DataTypes.ASCII_STRING)
                    return CodecReturnCodes.FAILURE;
                
                applyHasAttrib();
                Buffer position = elementEntry.encodedData();
                attrib.applyHasPosition();
                attrib.position().data(position.data(), position.position(), position.length());
            }
            else if (elementEntry.name().equals(ElementNames.PASSWORD))
            {
                if (elementEntry.dataType() != DataTypes.ASCII_STRING)
                    return CodecReturnCodes.FAILURE;
                Buffer password = elementEntry.encodedData();
                applyHasAttrib();
                applyHasPassword();
                password().data(password.data(), password.position(), password.length());
            }
            else if (elementEntry.name().equals(ElementNames.INST_ID))
            {
                if (elementEntry.dataType() != DataTypes.ASCII_STRING)
                    return CodecReturnCodes.FAILURE;
                Buffer instanceId = elementEntry.encodedData();
                applyHasInstanceId();
                instanceId().data(instanceId.data(), instanceId.position(), instanceId.length());
            }
            else if (elementEntry.name().equals(ElementNames.DOWNLOAD_CON_CONFIG))
            {
                if (elementEntry.dataType() != DataTypes.UINT)
                    return CodecReturnCodes.FAILURE;
                ret = tmpUInt.decode(dIter);
                if (ret != CodecReturnCodes.SUCCESS)
                    return ret;
                applyHasDownloadConnectionConfig();
                downloadConnectionConfig(tmpUInt.toLong());
            }
            else if (elementEntry.name().equals(ElementNames.PROV_PERM_EXP))
            {
                if (elementEntry.dataType() != DataTypes.UINT)
                    return CodecReturnCodes.FAILURE;
                ret = tmpUInt.decode(dIter);
                if (ret != CodecReturnCodes.SUCCESS)
                    return ret;
                applyHasAttrib();
                attrib.applyHasProvidePermissionExpressions();
                attrib.providePermissionExpressions(tmpUInt.toLong());
            }
            else if (elementEntry.name().equals(ElementNames.PROV_PERM_PROF))
            {
                if (elementEntry.dataType() != DataTypes.UINT)
                    return CodecReturnCodes.FAILURE;
                ret = tmpUInt.decode(dIter);
                if (ret != CodecReturnCodes.SUCCESS)
                    return ret;
                applyHasAttrib();
                attrib.applyHasProvidePermissionProfile();
                attrib.providePermissionProfile(tmpUInt.toLong());
            }
            else if (elementEntry.name().equals(ElementNames.SINGLE_OPEN))
            {
                if (elementEntry.dataType() != DataTypes.UINT)
                    return CodecReturnCodes.FAILURE;
                ret = tmpUInt.decode(dIter);
                if (ret != CodecReturnCodes.SUCCESS)
                    return ret;

                applyHasAttrib();
                attrib.applyHasSingleOpen();
                attrib.singleOpen(tmpUInt.toLong());
            }
            else if (elementEntry.name().equals(ElementNames.ROLE))
            {
                if (elementEntry.dataType() != DataTypes.UINT)
                    return CodecReturnCodes.FAILURE;
                ret = tmpUInt.decode(dIter);
                if (ret != CodecReturnCodes.SUCCESS)
                    return ret;
                applyHasRole();
                role(tmpUInt.toLong());
            }
            else if (elementEntry.name().equals(ElementNames.SUPPORT_PROVIDER_DICTIONARY_DOWNLOAD))
            {
                if (elementEntry.dataType() != DataTypes.UINT)
                    return CodecReturnCodes.FAILURE;
                ret = tmpUInt.decode(dIter);
                if (ret != CodecReturnCodes.SUCCESS)
                    return ret;
                
                applyHasAttrib();
                attrib.applyHasProviderSupportDictionaryDownload();
                attrib.supportProviderDictionaryDownload(tmpUInt.toLong());
            }
        }
        return CodecReturnCodes.SUCCESS;
    }
    public int encode(EncodeIterator encodeIter)
    {
        requestMsg.clear();

        requestMsg.msgClass(MsgClasses.REQUEST);
        requestMsg.streamId(streamId());
        requestMsg.domainType(DomainTypes.LOGIN);
        requestMsg.containerType(DataTypes.NO_DATA);

        requestMsg.applyStreaming();

        if (checkNoRefresh())
            requestMsg.applyNoRefresh();
        if (checkPause())
            requestMsg.applyPause();

        requestMsg.msgKey().applyHasName();
        requestMsg.msgKey().name(userName());
        if (checkHasUserNameType())
        {
            requestMsg.msgKey().applyHasNameType();
            requestMsg.msgKey().nameType(userNameType());
        }

       
        requestMsg.msgKey().applyHasAttrib();
        requestMsg.msgKey().attribContainerType(DataTypes.ELEMENT_LIST);
        int ret = requestMsg.encodeInit(encodeIter, 0);
        if (ret != CodecReturnCodes.ENCODE_MSG_KEY_ATTRIB)
            return ret;
        ret = encodeAttrib(encodeIter);
        if (ret != CodecReturnCodes.SUCCESS)
            return ret;
        if ((ret = requestMsg.encodeKeyAttribComplete(encodeIter, true)) < CodecReturnCodes.SUCCESS)
        {
            return ret;
        }
       
        if ((ret = requestMsg.encodeComplete(encodeIter, true)) < CodecReturnCodes.SUCCESS)
            return ret;

        return CodecReturnCodes.SUCCESS;
    }
    
    private int encodeAttrib(EncodeIterator encodeIter)
    {
        elementEntry.clear();
        elementList.clear();
        elementList.applyHasStandardData();
        int ret;
        if ((ret = elementList.encodeInit(encodeIter, null, 0)) != CodecReturnCodes.SUCCESS)
            return ret;

        if (checkHasAttrib() && attrib.checkHasApplicationId())
        {
            elementEntry.dataType(DataTypes.ASCII_STRING);
            elementEntry.name(ElementNames.APPID);
            if ((ret = elementEntry.encode(encodeIter, attrib.applicationId())) != CodecReturnCodes.SUCCESS)
                return ret;
        }

        if (checkHasAttrib() && attrib.checkHasApplicationName())
        {
            elementEntry.dataType(DataTypes.ASCII_STRING);
            elementEntry.name(ElementNames.APPNAME);
            if ((ret = elementEntry.encode(encodeIter, attrib.applicationName())) != CodecReturnCodes.SUCCESS)
                return ret;
        }

        if (checkHasAttrib() && attrib.checkHasPosition())
        {
            elementEntry.dataType(DataTypes.ASCII_STRING);
            elementEntry.name(ElementNames.POSITION);
            if ((ret = elementEntry.encode(encodeIter, attrib.position())) != CodecReturnCodes.SUCCESS)
                return ret;
        }

        if (checkHasPassword())
        {
            elementEntry.dataType(DataTypes.ASCII_STRING);
            elementEntry.name(ElementNames.PASSWORD);
            if ((ret = elementEntry.encode(encodeIter, password())) != CodecReturnCodes.SUCCESS)
                return ret;
        }

        if (checkHasAttrib() && attrib.checkHasProvidePermissionProfile())
        {
            elementEntry.dataType(DataTypes.UINT);
            elementEntry.name(ElementNames.PROV_PERM_PROF);
            tmpUInt.value(attrib.providePermissionProfile());
            ret = elementEntry.encode(encodeIter, tmpUInt);
            if (ret != CodecReturnCodes.SUCCESS)
                return ret;
        }

        if (checkHasAttrib() && attrib.checkHasProvidePermissionExpressions())
        {
            elementEntry.dataType(DataTypes.UINT);
            elementEntry.name(ElementNames.PROV_PERM_EXP);
            tmpUInt.value(attrib.providePermissionExpressions());
            ret = elementEntry.encode(encodeIter, tmpUInt);
            if (ret != CodecReturnCodes.SUCCESS)
                return ret;
        }

        if (checkHasAttrib() && attrib.checkHasSingleOpen())
        {
            elementEntry.dataType(DataTypes.UINT);
            elementEntry.name(ElementNames.SINGLE_OPEN);
            tmpUInt.value(attrib.singleOpen());
            ret = elementEntry.encode(encodeIter, tmpUInt);
            if (ret != CodecReturnCodes.SUCCESS)
                return ret;
        }

        if (checkHasAttrib() && attrib.checkHasAllowSuspectData())
        {
            elementEntry.dataType(DataTypes.UINT);
            elementEntry.name(ElementNames.ALLOW_SUSPECT_DATA);
            tmpUInt.value(attrib.allowSuspectData());
            ret = elementEntry.encode(encodeIter, tmpUInt);
            if (ret != CodecReturnCodes.SUCCESS)
                return ret;
        }
        
        if (checkHasAttrib() && attrib.checkHasProviderSupportDictionaryDownload())
        {
            elementEntry.dataType(DataTypes.UINT);
            elementEntry.name(ElementNames.SUPPORT_PROVIDER_DICTIONARY_DOWNLOAD);
            tmpUInt.value(attrib.supportProviderDictionaryDownload());
            ret = elementEntry.encode(encodeIter, tmpUInt);
            if (ret != CodecReturnCodes.SUCCESS)
                return ret;
        }
        
        if (checkHasInstanceId())
        {
            elementEntry.dataType(DataTypes.ASCII_STRING);
            elementEntry.name(ElementNames.INST_ID);
            ret = elementEntry.encode(encodeIter, instanceId());
            if (ret != CodecReturnCodes.SUCCESS)
                return ret;
        }

        if (checkHasRole())
        {
            elementEntry.dataType(DataTypes.UINT);
            elementEntry.name(ElementNames.ROLE);
            tmpUInt.value(role());
            ret = elementEntry.encode(encodeIter, tmpUInt);
            if (ret != CodecReturnCodes.SUCCESS)
                return ret;
        }

        if (checkHasDownloadConnectionConfig())
        {
            elementEntry.dataType(DataTypes.UINT);
            elementEntry.name(ElementNames.DOWNLOAD_CON_CONFIG);
            tmpUInt.value(downloadConnectionConfig());
            ret = elementEntry.encode(encodeIter, tmpUInt);
            if (ret != CodecReturnCodes.SUCCESS)
                return ret;
  
        }

        if ((ret = elementList.encodeComplete(encodeIter, true)) != CodecReturnCodes.SUCCESS)
            return ret;
        
        return CodecReturnCodes.SUCCESS;
    }
    
    public void applyPause()
    {
        flags |= LoginRequestFlags.PAUSE_ALL;
    }
    
    public boolean checkPause()
    {
        return (flags & LoginRequestFlags.PAUSE_ALL) != 0;
    }

    public void applyNoRefresh()
    {
        flags |= LoginRequestFlags.NO_REFRESH;
    }
    
    public boolean checkNoRefresh()
    {
        return (flags & LoginRequestFlags.NO_REFRESH) != 0;
    }
    
    public Buffer userName()
    {
        return userName;
    }
    
    public void userName(Buffer userName)
    {
        assert(userName != null) : "userName can not be null";
        userName().data(userName.data(), userName.position(), userName.length());
    }

    public int userNameType()
    {
        return userNameType;
    }
    
    public void userNameType(int userNameType)
    {
        this.userNameType = userNameType;
    }

    public boolean checkHasUserNameType()
    {
        return (flags & LoginRequestFlags.HAS_USERNAME_TYPE) != 0;
    }
    
    public void applyHasUserNameType()
    {
        flags |= LoginRequestFlags.HAS_USERNAME_TYPE;
    }
    
    
    public String toString()
    {
        StringBuilder stringBuf = super.buildStringBuffer();

        stringBuf.insert(0, "LoginRequest: \n");
        stringBuf.append(tab);
        stringBuf.append("userName: ");
        stringBuf.append(userName().toString());
        stringBuf.append(eol);
        stringBuf.append(tab);
        stringBuf.append("streaming: ");
        stringBuf.append("true");
        stringBuf.append(eol);

        if (checkHasUserNameType())
        {
            stringBuf.append(tab);
            stringBuf.append("nameType: ");
            stringBuf.append(userNameType());
            stringBuf.append(eol);
        }
        
        if (checkPause())
        {
            stringBuf.append(tab);
            stringBuf.append("pauseAll:");
            stringBuf.append("true");
            stringBuf.append(eol);
        }
        if (checkNoRefresh())
        {
            stringBuf.append(tab);
            stringBuf.append("noRefresh:");
            stringBuf.append("true");
            stringBuf.append(eol);
        }
        
        if(checkHasAttrib())
        {
            stringBuf.append(attrib().toString());
        }
        
        if (checkHasDownloadConnectionConfig())
        {
            stringBuf.append(tab);
            stringBuf.append("downloadConnectionConfig: ");
            stringBuf.append(downloadConnectionConfig());
            stringBuf.append(eol);
        }
        if (checkHasInstanceId())
        {
            stringBuf.append(tab);
            stringBuf.append("instanceId: ");
            stringBuf.append(instanceId());
            stringBuf.append(eol);
        }
        
        if (checkHasRole())
        {
            stringBuf.append(tab);
            stringBuf.append("role: ");
            stringBuf.append(role());
            stringBuf.append(eol);
        }
        
        return stringBuf.toString();
    }
    
    public long downloadConnectionConfig()
    {
        return downloadConnectionConfig;
    }

    public void downloadConnectionConfig(long downloadConnectionConfig)
    {
        assert(checkHasDownloadConnectionConfig());
        this.downloadConnectionConfig = downloadConnectionConfig;
    }
    
    public void applyHasDownloadConnectionConfig()
    {
         flags |= LoginRequestFlags.HAS_DOWNLOAD_CONN_CONFIG;
    }
    
    public boolean checkHasDownloadConnectionConfig()
    {
         return (flags & LoginRequestFlags.HAS_DOWNLOAD_CONN_CONFIG) != 0;
    }

    public Buffer instanceId()
    {
        return instanceId;
    }
    
    public void instanceId(Buffer instanceId)
    {
        assert(checkHasInstanceId()) : "instanceId flag should be set first";
        assert (instanceId != null) : "instanceId can not be null";

        instanceId().data(instanceId.data(), instanceId.position(), instanceId.length());
    }

    public void applyHasInstanceId()
    {
        flags |= LoginRequestFlags.HAS_INSTANCE_ID;
    }

    public boolean checkHasInstanceId()
    {
        return (flags & LoginRequestFlags.HAS_INSTANCE_ID) != 0;
    }
    
    public Buffer password()
    {
        return password;
    }

    public void applyHasPassword()
    {
        flags |= LoginRequestFlags.HAS_PASSWORD;
    }

    public boolean checkHasPassword()
    {
        return (flags & LoginRequestFlags.HAS_PASSWORD) != 0;
    }
    
    public long role()
    {
        return role;
    }

    public void role(long role)
    {
        assert(checkHasRole());
        this.role = role;
    }
    
    public void applyHasRole()
    {
        flags |= LoginRequestFlags.HAS_ROLE;
    }

    public boolean checkHasRole()
    {
        return (flags & LoginRequestFlags.HAS_ROLE) != 0;
    }
    
    public boolean checkHasAttrib()
    {
        return (flags & LoginRequestFlags.HAS_ATTRIB) != 0;
    }
    
    public void applyHasAttrib()
    {
        flags |= LoginRequestFlags.HAS_ATTRIB;
    }
    
    public LoginAttrib attrib()
    {
        return attrib;
    }
    
    public void attrib(LoginAttrib attrib)
    {
        assert(attrib != null) : "attrib can not be null";
        assert(checkHasAttrib());
        LoginAttribImpl loginAttribImpl = (LoginAttribImpl)attrib;
        loginAttribImpl.copyReferences(loginAttribImpl);
    }
    
    @Override
    public int domainType()
    {
        return DomainTypes.LOGIN;
    }
}