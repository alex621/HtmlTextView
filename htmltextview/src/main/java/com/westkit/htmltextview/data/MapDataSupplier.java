package com.westkit.htmltextview.data;

import java.util.HashMap;

public class MapDataSupplier implements DataSupplier{
    private HashMap<String, ImgData> map;

    public MapDataSupplier(){
        this(null);
    }

    public MapDataSupplier(HashMap<String, ImgData> map) {
        this.map = map;

        if (this.map == null){
            this.map = new HashMap<>();
        }
    }

    public MapDataSupplier put(String src, ImgData data){
        map.put(src, data);
        return this;
    }

    @Override
    public ImgData getImgData(String src) {
        return map.get(src);
    }
}
