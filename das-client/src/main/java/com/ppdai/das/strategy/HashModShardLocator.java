package com.ppdai.das.strategy;

import org.apache.commons.codec.digest.DigestUtils;
import java.math.BigInteger;

public class HashModShardLocator<CTX extends ConditionContext> extends ModShardLocator<CTX> {

    public HashModShardLocator(Integer mod, String zeroPaddingFormat) {
        super(mod, zeroPaddingFormat);
    }

    @Override
    protected Long string2Long(String str) {
        String md5 = DigestUtils.md5Hex(str);
        BigInteger bigInteger = new BigInteger(md5, 16);
        return bigInteger.longValue();
    }

}
