package com.ppdai.das.strategy;

import org.apache.commons.codec.digest.DigestUtils;
import java.math.BigInteger;

public class HashModShardLocator extends ModShardLocator {

    public HashModShardLocator(Integer mod) {
        super(mod);
    }

    @Override
    protected Long string2Long(String str) {
        String md5 = DigestUtils.md5Hex(str);
        BigInteger bigInteger = new BigInteger(md5, 16);
        return bigInteger.longValue();
    }

}
