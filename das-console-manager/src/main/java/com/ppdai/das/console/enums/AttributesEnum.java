package com.ppdai.das.console.enums;


public enum AttributesEnum {

    AUTO_RELOAD_DISENABLED(0, "false", "项目自动不reload"),
    AUTO_RELOAD_ENABLED(1, "true", "项目自动reload");

    private Integer type;
    private String name;
    private String description;

    AttributesEnum(Integer type, String name, String description) {
        this.type = type;
        this.name = name;
        this.description = description;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static AttributesEnum getPatternTypeEnumByType(int type) {
        for (AttributesEnum item : AttributesEnum.values()) {
            if (type == item.type) {
                return item;
            }
        }
        throw new EnumConstantNotPresentException(AttributesEnum.class, "type " + type + " is not exist!!");
    }
}
