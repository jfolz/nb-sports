package com.nobullshit.binaryio;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

public class BinaryWriter extends DataOutputStream {
	
	public static final String HEADER_BEGIN_V1 = "stupid big endian binary format for fools v1";

	public BinaryWriter(File out, String[] names, Object[] types) throws IOException {
		super(create(out));
		setHeader(names,types);
	}
	
	private void setHeader(String[] names, Object[] types) throws IOException {
		StringBuilder b = new StringBuilder(HEADER_BEGIN_V1);
		for(int i=0; i<names.length; i++) {
			String name = names != null ? names[i] : ""+i;
			Object o = types[i];
			
			b.append(' ');
			b.append(name);
			b.append(':');
			b.append(o.getClass().getSimpleName().toLowerCase());
		}
		
		writeUTF(b.toString());
	}

	private static OutputStream create(File out) throws IOException {
	    FileOutputStream fos = new FileOutputStream(out);
	    BufferedOutputStream bos = new BufferedOutputStream(fos, 256*1024);
		GZIPOutputStream zos = new GZIPOutputStream(bos);
		return zos;
	}

}
