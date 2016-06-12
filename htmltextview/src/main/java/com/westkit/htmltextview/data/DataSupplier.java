package com.westkit.htmltextview.data;

import org.xml.sax.Attributes;

public interface DataSupplier {
    ImgData getImgData(String src, Attributes attributes);
}
