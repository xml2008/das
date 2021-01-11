package com.ppdai.das.console.cloud.enums;


public enum AppAccessEnum {

    APP_NO_ACCESS(0, "未接入"),
    APP_YSE_ACCESS(1, "接入");

    private Integer type;
    private String description;

    AppAccessEnum(Integer type, String description) {
        this.type = type;
        this.description = description;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static AppAccessEnum getOfflineAppAccessEnumType(int type) {
        for (AppAccessEnum item : AppAccessEnum.values()) {
            if (type == item.type) {
                return item;
            }
        }
        throw new EnumConstantNotPresentException(AppAccessEnum.class, "type " + type + " is not exist!!");
    }
}
