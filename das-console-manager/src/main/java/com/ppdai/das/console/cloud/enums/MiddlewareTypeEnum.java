package com.ppdai.das.console.cloud.enums;


public enum MiddlewareTypeEnum {

    MIDDLEWARE_APOLLO(1, "apollo", "apollo-admin"),
    MIDDLEWARE_CACHE(2, "cache", "cache-admin"),
    MIDDLEWARE_DAS(3, "das", "das-admin"),
    MIDDLEWARE_MQ(4, "mq", "mq-admin"),
    MIDDLEWARE_JOB(5, "job", "job-admin"),
    MIDDLEWARE_RADER(6, "rader", "reder-admin");

    private Integer type;
    private String name;
    private String admin;

    MiddlewareTypeEnum(Integer type, String name, String admin) {
        this.type = type;
        this.name = name;
        this.admin = admin;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    public static MiddlewareTypeEnum getMiddlewareTypeEnumByType(int type) {
        for (MiddlewareTypeEnum item : MiddlewareTypeEnum.values()) {
            if (type == item.type) {
                return item;
            }
        }
        throw new EnumConstantNotPresentException(MiddlewareTypeEnum.class, "type " + type + " is not exist!!");
    }
}
