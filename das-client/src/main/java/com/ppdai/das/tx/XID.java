package com.ppdai.das.tx;

public class XID {
    String ip;
    String number;

    public String getIp() {
        return ip;
    }

    public XID setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public String getNumber() {
        return number;
    }

    public XID setNumber(String number) {
        this.number = number;
        return this;
    }

    @Override
    public String toString() {
        return ip + ":" + number;
    }
}
