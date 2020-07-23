package com.ppdai.das.console.enums;


public enum PatternTypeEnum {

    NORMAL(0, "normal", "普通模式"),
    MGR(1, "mgrEnabled", "MGR模式");

    private Integer type;
    private String name;
    private String description;

    PatternTypeEnum(Integer type, String name, String description) {
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

    public static PatternTypeEnum getPatternTypeEnumByType(int type) {
        for (PatternTypeEnum item : PatternTypeEnum.values()) {
            if (type == item.type) {
                return item;
            }
        }
        throw new EnumConstantNotPresentException(PatternTypeEnum.class, "type " + type + " is not exist!!");
    }
}
