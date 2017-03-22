package com.thomsonreuters.upa.transport;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.nio.ByteOrder;

enum LZ4Utils
{
    ;

    static final int MEMORY_USAGE = 14;
    static final int NOT_COMPRESSIBLE_DETECTION_LEVEL = 6;

    static final int MIN_MATCH = 4;

    static final int HASH_LOG = MEMORY_USAGE - 2;
    static final int HASH_TABLE_SIZE = 1 << HASH_LOG;

    static final int SKIP_STRENGTH = Math.max(NOT_COMPRESSIBLE_DETECTION_LEVEL, 2);
    static final int COPY_LENGTH = 8;
    static final int LAST_LITERALS = 5;
    static final int MF_LIMIT = COPY_LENGTH + MIN_MATCH;
    static final int MIN_LENGTH = MF_LIMIT + 1;

    static final int MAX_DISTANCE = 1 << 16;

    static final int ML_BITS = 4;
    static final int ML_MASK = (1 << ML_BITS) - 1;
    static final int RUN_BITS = 8 - ML_BITS;
    static final int RUN_MASK = (1 << RUN_BITS) - 1;

    static final int LZ4_64K_LIMIT = (1 << 16) + (MF_LIMIT - 1);
    static final int HASH_LOG_64K = HASH_LOG + 1;
    static final int HASH_TABLE_SIZE_64K = 1 << HASH_LOG_64K;

    static final int HASH_LOG_HC = 15;
    static final int HASH_TABLE_SIZE_HC = 1 << HASH_LOG_HC;
    static final int OPTIMAL_ML = ML_MASK - 1 + MIN_MATCH;

    static final int maxCompressedLength(int length)
    {
        if (length < 0)
        {
            throw new IllegalArgumentException("length must be >= 0, got " + length);
        }
        return length + length / 255 + 16;
    }

    static int hash(int i)
    {
        return (i * -1640531535) >>> ((MIN_MATCH * 8) - HASH_LOG);
    }

    static int hash64k(int i)
    {
        return (i * -1640531535) >>> ((MIN_MATCH * 8) - HASH_LOG_64K);
    }

    static int hashHC(int i)
    {
        return (i * -1640531535) >>> ((MIN_MATCH * 8) - HASH_LOG_HC);
    }

    static int readShortLittleEndian(byte[] buf, int i)
    {
        return (buf[i] & 0xFF) | ((buf[i + 1] & 0xFF) << 8);
    }

    static int hash(byte[] buf, int i)
    {
        return hash(readInt(buf, i));
    }

    static int hash64k(byte[] buf, int i)
    {
        return hash64k(readInt(buf, i));
    }

    static boolean readIntEquals(byte[] buf, int i, int j)
    {
        return buf[i] == buf[j] && buf[i + 1] == buf[j + 1] && buf[i + 2] == buf[j + 2] && buf[i + 3] == buf[j + 3];
    }

    static void naiveIncrementalCopy(byte[] dest, int matchOff, int dOff, int matchLen)
    {
        for (int i = 0; i < matchLen; ++i)
        {
            dest[dOff++] = dest[matchOff++];
        }
    }

    static void safeIncrementalCopy(byte[] dest, int matchOff, int dOff, int matchLen)
    {
        assert matchLen >= 4;
        if (dOff - matchOff >= matchLen)
        {
            safeArraycopy(dest, matchOff, dest, dOff, matchLen);
        }
        else
        {
            naiveIncrementalCopy(dest, matchOff, dOff, matchLen);
        }
    }

    static void wildIncrementalCopy(byte[] dest, int matchOff, int dOff, int matchLen)
    {
        assert matchLen >= 4;
        switch (matchLen)
        {
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
                for (int i = 0; i < 8; ++i)
                {
                    dest[dOff++] = dest[matchOff++];
                }
                break;
            default:
                if (dOff - matchOff >= matchLen)
                {
                    wildArraycopy(dest, matchOff, dest, dOff, matchLen);
                }
                else
                {
                    naiveIncrementalCopy(dest, matchOff, dOff, matchLen);
                }
                break;
        }
    }

    static int commonBytes(byte[] b, int o1, int o2, int limit)
    {
        int count = 0;
        while (o2 < limit && b[o1++] == b[o2++])
        {
            ++count;
        }
        return count;
    }

    static int commonBytesBackward(byte[] b, int o1, int o2, int l1, int l2)
    {
        int count = 0;
        while (o1 > l1 && o2 > l2 && b[--o1] == b[--o2])
        {
            ++count;
        }
        return count;
    }

    static void safeArraycopy(byte[] src, int sOff, byte[] dest, int dOff, int len)
    {
        System.arraycopy(src, sOff, dest, dOff, len);
    }

    static void wildArraycopy(byte[] src, int sOff, byte[] dest, int dOff, int len)
    {
        // can make decompression 10% faster
        final int fastLen = ((len - 1) & 0xFFFFFFF8) + COPY_LENGTH;
        try
        {
            System.arraycopy(src, sOff, dest, dOff, fastLen);
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            throw new CompressorException("Malformed input at " + sOff);
        }
    }

    static int encodeSequence(byte[] src, int anchor, int matchOff, int matchRef, int matchLen, byte[] dest, int dOff, int destEnd)
    {
        final int runLen = matchOff - anchor;
        final int tokenOff = dOff++;

        if (dOff + runLen + (2 + 1 + LAST_LITERALS) + (runLen >>> 8) > destEnd)
        {
            throw new CompressorException("maxDestLen is too small");
        }

        int token;
        if (runLen >= RUN_MASK)
        {
            token = (byte)(RUN_MASK << ML_BITS);
            dOff = writeLen(runLen - RUN_MASK, dest, dOff);
        }
        else
        {
            token = runLen << ML_BITS;
        }

        // copy literals
        wildArraycopy(src, anchor, dest, dOff, runLen);
        dOff += runLen;

        // encode offset
        final int matchDec = matchOff - matchRef;
        dest[dOff++] = (byte)matchDec;
        dest[dOff++] = (byte)(matchDec >>> 8);

        // encode match len
        matchLen -= 4;
        if (dOff + (1 + LAST_LITERALS) + (matchLen >>> 8) > destEnd)
        {
            throw new CompressorException("maxDestLen is too small");
        }
        if (matchLen >= ML_MASK)
        {
            token |= ML_MASK;
            dOff = writeLen(matchLen - RUN_MASK, dest, dOff);
        }
        else
        {
            token |= matchLen;
        }

        dest[tokenOff] = (byte)token;

        return dOff;
    }

    static int lastLiterals(byte[] src, int sOff, int srcLen, byte[] dest, int dOff, int destEnd)
    {
        final int runLen = srcLen;

        if (dOff + runLen + 1 + (runLen + 255 - RUN_MASK) / 255 > destEnd)
        {
            throw new CompressorException("maxDestLen is too small");
        }

        if (runLen >= RUN_MASK)
        {
            dest[dOff++] = (byte)(RUN_MASK << ML_BITS);
            dOff = writeLen(runLen - RUN_MASK, dest, dOff);
        }
        else
        {
            dest[dOff++] = (byte)(runLen << ML_BITS);
        }
        // copy literals
        System.arraycopy(src, sOff, dest, dOff, runLen);
        dOff += runLen;

        return dOff;
    }

    static int writeLen(int len, byte[] dest, int dOff)
    {
        while (len >= 0xFF)
        {
            dest[dOff++] = (byte)0xFF;
            len -= 0xFF;
        }
        dest[dOff++] = (byte)len;

        return dOff;
    }

    static class Match
    {
        int start, ref, len;

        void fix(int correction)
        {
            start += correction;
            ref += correction;
            len -= correction;
        }

        int end()
        {
            return start + len;
        }
    }

    static void copyTo(Match m1, Match m2)
    {
        m2.len = m1.len;
        m2.start = m1.start;
        m2.ref = m1.ref;
    }

    static final ByteOrder NATIVE_BYTE_ORDER = ByteOrder.nativeOrder();

    static void checkRange(byte[] buf, int off)
    {
        if (off < 0 || off >= buf.length)
        {
            throw new ArrayIndexOutOfBoundsException(off);
        }
    }

    static void checkRange(byte[] buf, int off, int len)
    {
        checkLength(len);
        if (len > 0)
        {
            checkRange(buf, off);
            checkRange(buf, off + len - 1);
        }
    }

    static void checkLength(int len)
    {
        if (len < 0)
        {
            throw new IllegalArgumentException("lengths must be >= 0");
        }
    }

    static int readIntBE(byte[] buf, int i)
    {
        return ((buf[i] & 0xFF) << 24) | ((buf[i + 1] & 0xFF) << 16) | ((buf[i + 2] & 0xFF) << 8) | (buf[i + 3] & 0xFF);
    }

    static int readIntLE(byte[] buf, int i)
    {
        return (buf[i] & 0xFF) | ((buf[i + 1] & 0xFF) << 8) | ((buf[i + 2] & 0xFF) << 16) | ((buf[i + 3] & 0xFF) << 24);
    }

    static int readInt(byte[] buf, int i)
    {
        if (NATIVE_BYTE_ORDER == ByteOrder.BIG_ENDIAN)
        {
            return readIntBE(buf, i);
        }
        else
        {
            return readIntLE(buf, i);
        }
    }

}
