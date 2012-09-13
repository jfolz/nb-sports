package com.nobullshit.binaryio;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.util.Log;

public class BinaryWriter extends DataOutputStream {
	
	public static final String HEADER_BEGIN = "stupid binary format";
	public static final int version = 1;
	private static final String SPACER = ", ";

	public BinaryWriter(File out, CharSequence[] ... defs) throws IOException {
		super(create(out));
		setHeader(defs);
	}
	
	private void setHeader(CharSequence[] ... defs) throws IOException {
		StringBuilder b = new StringBuilder(HEADER_BEGIN);
		b.append(" { version:"+version);
		b.append(SPACER);
		b.append("series:{ ");
		for(int i=0; i<defs.length-1; i+=2) {
			CharSequence[] names = defs[i];
			CharSequence[] types = defs[i+1];
			if(i > 0) b.append(SPACER);
			b.append((i/2+1)+":{ ");
			for(int j=0; j<names.length; j++) {
				if(j > 0) b.append(SPACER);
				b.append(names[j]);
				b.append(':');
				b.append("\""+types[j]+"\"");
			}
			b.append(" }");
		}
		b.append(" } }\n");
		
		int n = b.length();
		for(int i=0; i<n; i++) writeByte(b.charAt(i));
		Log.v("BinaryWriter", b.toString());
	}

	private static OutputStream create(File out) throws IOException {
	    FileOutputStream fos = new FileOutputStream(out);
		BufferedOutputStream bos = new BufferedOutputStream(fos,4*1024);
		return bos;
	}

}
