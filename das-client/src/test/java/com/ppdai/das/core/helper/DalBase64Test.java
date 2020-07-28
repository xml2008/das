package com.ppdai.das.core.helper;

import org.junit.Assert;
import org.junit.Test;

public class DalBase64Test {
    @Test
    public void test() {
        DalBase64 dalBase64 = new DalBase64(true);
        Assert.assertArrayEquals("A".getBytes(), dalBase64.decode(dalBase64.encode("A".getBytes())));
        Assert.assertArrayEquals("A".getBytes(), dalBase64.decodeBase64(dalBase64.encodeBase64("A".getBytes())));
        Assert.assertArrayEquals("A".getBytes(), dalBase64.decodeBase64(new String(dalBase64.encode("A".getBytes()))));
    }
}
