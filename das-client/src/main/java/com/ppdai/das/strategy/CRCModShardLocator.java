package com.ppdai.das.strategy;
import java.util.Set;
import java.util.zip.CRC32;

public class CRCModShardLocator<CTX extends ConditionContext> extends ModShardLocator<CTX> {

    public CRCModShardLocator(Integer mod, String zeroPaddingFormat) {
        super(mod, zeroPaddingFormat);
    }

    @Override
    protected String mod(int mod, Object value) {
        Long id = (Long)getNumberValue(value);
        return (id % mod ) + "";
    }

    @Override
    protected Number string2Long(String str) {
        CRC32 crc32 = new CRC32();
        crc32.update(str.getBytes());
        return crc32.getValue();
    }

    @Override
    public Set<String> locateForBetween(ConditionContext ctx) {
        return ctx.getAllShards();
    }

}
