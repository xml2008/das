package com.ppdai.das.client.sqlbuilder;

import com.google.gson.JsonObject;
import com.ppdai.das.client.Segment;


public class PageSerializer implements Serializer {
    @Override
    public Segment deserialize(JsonObject jsonObject) {
        int pageNo = jsonObject.get("pageNo").getAsInt();
        int pageSize = jsonObject.get("pageSize").getAsInt();
        return new Page(pageNo, pageSize);
    }

    @Override
    public JsonObject serialize(Segment segment) {
        JsonObject element = new JsonObject();
        Page page = (Page)segment;
        element.addProperty("pageNo", (int)readField(page, "pageNo"));
        element.addProperty("pageSize", (int)readField(page, "pageSize"));
        return addBuildType(element);
    }

    @Override
    public Class getBuildType() {
        return Page.class;
    }
}
