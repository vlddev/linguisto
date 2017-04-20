package org.linguisto.utils;

import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPOutputStream;

public class Archiver {

	public static byte[] compress(String str) throws Exception {
        if (str == null || str.length() == 0) {
            return null;
        }
        //System.out.println("String length : " + str.length());
        ByteArrayOutputStream obj=new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(obj);
        gzip.write(str.getBytes("UTF-8"));
        gzip.close();
        return obj.toByteArray();
     }
}
