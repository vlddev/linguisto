/*
#######################################################################
#
#  Linguisto Portal
#
#  Copyright (c) 2017 Volodymyr Vlad
#
#######################################################################
*/

package org.linguisto.utils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Owner
 *
 */
public class IOUtil {

    public static final String ln = System.getProperty("line.separator");

    public static String getFileContent(String file) throws IOException{
        String encoding = System.getProperty("file.encoding");
        return getFileContent(file, encoding);
        
    }

    public static String getFileContent(String file, String encoding) throws IOException{
        StringBuilder ret = new StringBuilder();
        String ln = System.getProperty("line.separator");
		BufferedReader fIn = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), encoding)
		);
		String str = fIn.readLine();
		while(str!=null){
		    ret.append(str);
		    ret.append(ln);
			str = fIn.readLine();
		}

		fIn.close();
		return ret.toString();
    }

	/**
	 * Load hashtable from file 
	 * <key>\t<val>
	 * 
	 * @param file
	 * @param encoding
	 * @param bCase true - case sencitive, false - case insencitive (load in lower case)
	 * @return
	 */
	public static Map<String, String> getFileContentAsMap(String file, String encoding, 
					boolean bCase, Map<String, String> initHm) throws Exception{
		return getFileContentAsMap(file, encoding, bCase, initHm, false);
	}
					
	/**
	 * Load hashtable from file 
	 * <key>\t<val>
	 * 
	 * @param file
	 * @param encoding
	 * @param bCase true - key is case sensitive, false - key is case insensitive (load in lower case), value will be read as is
	 * @param initHm - initial hash map (all new values will be added to this map) 
	 * @param bComments true - ignore lines starting from '#'
	 * @return
	 */
	public static Map<String, String> getFileContentAsMap(String file, String encoding, 
					boolean bCase, Map<String, String> initHm, boolean bComments) throws Exception{
		Map<String, String> hm = new HashMap<String, String>();
		if(initHm != null){
			hm = initHm; 
		}
		String str = null; 
		try{
			BufferedReader fIn = new BufferedReader(
				new InputStreamReader(new FileInputStream(file),encoding));
            
			str = fIn.readLine();
			int pos=0;
			String key = null;
			String val = null;
			while(str!=null){
				if(!str.startsWith("#")){ //ignore commented lines
					pos = str.indexOf('\t');
					if(pos>-1){
						if(str.length()>pos){
							key = str.substring(0, pos).trim();
							val = str.substring(pos+1).trim();
							if(!bCase){
								key = key.toLowerCase();
								//val = val.toLowerCase();
							}
							hm.put(key, val);
						}
					}
				}
				str = fIn.readLine();
			}
			fIn.close();
		}catch(Exception e){
            throw e;
		}
		return hm;
	}

    public static PrintWriter openFile(String file, String encoding) throws IOException{
        return new PrintWriter(
			new OutputStreamWriter(new FileOutputStream(file),encoding),true);

    }
    
    public static BufferedReader openFileToRead(String file, String encoding) throws IOException{
        return new BufferedReader(new InputStreamReader(
					new FileInputStream(file), encoding));


    }

	public static void storeString(String file, String encoding, String content) throws IOException{
		PrintWriter pw = openFile(file, encoding);
		pw.print(content);
		pw.flush();
		pw.close();
	}
}