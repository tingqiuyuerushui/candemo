package com.uaes.candemo.util;

import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPOutputStream;

public class CompressUtils {
    public static byte[] compress(byte []unzipData) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(baos);
            gzip.write(unzipData);
            gzip.close();
            byte[] encode = baos.toByteArray();
            baos.flush();
            baos.close();
            return encode;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
