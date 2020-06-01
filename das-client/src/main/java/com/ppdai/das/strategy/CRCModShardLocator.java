package com.ppdai.das.strategy;

import java.util.zip.CRC32;

public class CRCModShardLocator<CTX extends ConditionContext> extends ModShardLocator<CTX> {

    public CRCModShardLocator(Integer mod, String zeroPaddingFormat) {
        super(mod, zeroPaddingFormat);
    }

    @Override
    protected Long string2Long(String str) {
        CRC32 crc32 = new CRC32();
        crc32.update(str.getBytes());
        return crc32.getValue();
    }
}
