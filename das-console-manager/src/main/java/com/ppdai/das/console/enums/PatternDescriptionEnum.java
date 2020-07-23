package com.ppdai.das.console.enums;


public enum PatternDescriptionEnum {

    NORMAL(0, "normal", "普通模式"),
    MGRREADWRITESPLITTING(1, "mgrReadWriteSplittingEnabled", "MGR读写分离");

    private Integer type;
    private String name;
    private String description;

    PatternDescriptionEnum(Integer type, String name, String description) {
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

    public static PatternDescriptionEnum getPatternDescriptionEnumByType(Integer type) {
        for (PatternDescriptionEnum item : PatternDescriptionEnum.values()) {
            if (type == item.type) {
                return item;
            }
        }
        throw new EnumConstantNotPresentException(PatternDescriptionEnum.class, "type " + type + " is not exist!!");
    }
}
