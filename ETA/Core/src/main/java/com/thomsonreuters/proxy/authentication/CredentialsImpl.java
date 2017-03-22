package com.thomsonreuters.proxy.authentication;

import java.util.Map;
import java.util.HashMap;

class CredentialsImpl implements ICredentials, Cloneable
{
    private final Map<String, String> _credentials = new HashMap<String, String>();

    protected CredentialsImpl()
    {
    }

    @Override
    public void set(String name, String value)
    {
        if (name != null)
        {
            _credentials.put(name, value);
        }
    }

    @Override
    public String get(String name)
    {
        String value;

        if (name != null)
        {
            value = _credentials.get(name);
        }
        else
        {
            value = null;
        }

        return value;
    }

    @Override
    public boolean isSet(String name)
    {
        return name != null && _credentials.containsKey(name);
    }
	
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        CredentialsImpl result = (CredentialsImpl)super.clone();

        for (String name : _credentials.keySet())
        {
            result._credentials.put(name, _credentials.get(name));
        }

        return result;
    }
}
