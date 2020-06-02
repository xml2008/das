package com.ppdai.das.strategy;

import org.apache.commons.codec.digest.DigestUtils;
import java.math.BigInteger;
import java.util.Set;

public class HashModShardLocator<CTX extends ConditionContext> extends ModShardLocator<CTX> {

    public HashModShardLocator(Integer mod, String zeroPaddingFormat) {
        super(mod, zeroPaddingFormat);
    }

    @Override
    public Set<String> locateForBetween(ConditionContext ctx) {
        return ctx.getAllShards();
    }

    @Override
    protected String mod(int mod, Object value) {
        BigInteger id = (BigInteger)getNumberValue(value);
        return id.mod(BigInteger.valueOf(mod)).intValue() + "";
    }

    @Override
    protected Number string2Long(String str) {
        String md5 = DigestUtils.md5Hex(str);
        return new BigInteger(md5, 16);
    }

}
