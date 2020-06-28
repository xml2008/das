package com.ppdai.das.client.sqlbuilder;

import com.google.gson.JsonObject;
import com.ppdai.das.client.Segment;


public class PageSerializer implements Serializer {
    @Override
    public Segment deserialize(JsonObject jsonObject) {
        int pageNo = Integer.parseInt(jsonObject.get("pageNo").getAsString());
        int pageSize = Integer.parseInt(jsonObject.get("pageSize").getAsString());
        return new Page(pageNo, pageSize);
    }

    @Override
    public JsonObject serialize(Segment segment) {
        JsonObject element = new JsonObject();
        element.addProperty("pageNo", readField(segment, "pageNo").toString());
        element.addProperty("pageSize", readField(segment, "pageSize").toString());
        return addBuildType(element);
    }

    @Override
    public Class getBuildType() {
        return Page.class;
    }
}
