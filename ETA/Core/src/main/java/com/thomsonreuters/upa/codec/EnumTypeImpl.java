package com.thomsonreuters.upa.codec;

import com.thomsonreuters.upa.codec.Buffer;
import com.thomsonreuters.upa.codec.CodecFactory;
import com.thomsonreuters.upa.codec.EnumType;

class EnumTypeImpl implements EnumType
{
    int             _value;
    final Buffer    _display = CodecFactory.createBuffer();
    final Buffer    _meaning = CodecFactory.createBuffer();

    @Override
    public int value()
    {
        return _value;
    }

    void value(int value)
    {
        assert (value >= 0 && value <= 65535) : "value is out of range (0-65535)"; // uint16

        _value = value;
    }

    @Override
    public Buffer display()
    {
        return _display;
    }

    void display(Buffer display)
    {
        assert (display != null) : "display must be non-null";

        ((BufferImpl)_display).copyReferences(display);
    }

    @Override
    public Buffer meaning()
    {
        return _meaning;
    }

    void meaning(Buffer meaning)
    {
        assert (meaning != null) : "meaning must be non-null";

        ((BufferImpl)_meaning).copyReferences(meaning);
    }
}
