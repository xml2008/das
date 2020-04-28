package com.ppdai.das.strategy;

import com.google.common.base.Preconditions;
import org.apache.commons.codec.digest.DigestUtils;

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;
import java.util.zip.CRC32;

public class HashStrategy extends ShardColModShardStrategy {
    public static final String HASH = "hash";
    private String hash = "md5";

    @Override
    public void initialize(Map<String, String> settings) {
        super.initialize(settings);

        if (settings.containsKey(HASH)) {
            hash = settings.get(HASH).toLowerCase();
            Preconditions.checkArgument(hash.equals("md5") || hash.equals("crc32"),
                    "Hash algorithm :" + hash +" is not allowed. Please input 'md5' or 'crc32'.");
        }
    }

    @Override
    public String calculateDbShard(Object value) {
        Preconditions.checkNotNull(value);
        long hashValue = hashValue(Objects.toString(value), getMod());
        return hashValue + "";
    }

    @Override
    public String calculateTableShard(String tableName, Object value) {
        Preconditions.checkNotNull(value);
        long hashValue = hashValue(Objects.toString(value), getTableMod());
        return hashValue + "";
    }

    long hashValue(String str, int mod) {
        if (hash.equals("md5")) {
            String md5 = DigestUtils.md5Hex(str);
            BigInteger bigInteger = new BigInteger(md5, 16);
            BigInteger result = bigInteger.mod(BigInteger.valueOf(mod));
            return result.longValue();
        } else {
            CRC32 crc32 = new CRC32();
            crc32.update(str.getBytes());
            return crc32.getValue() % mod;
        }
    }

}
