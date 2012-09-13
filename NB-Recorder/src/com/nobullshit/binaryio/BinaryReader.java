package com.nobullshit.binaryio;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.util.Log;

public class BinaryReader extends DataInputStream {
	
	private String header;

	public BinaryReader(File in) throws IOException {
		super(create(in));
		StringBuilder b = new StringBuilder();
		char c = (char) readByte();
		while(c != '\n') {
			b.append(c);
			c = (char) readByte();
		}
		header = b.toString();
		Log.v("BinaryReader", header);
	}
	
	private static InputStream create(File in) throws IOException {
	    FileInputStream fis = new FileInputStream(in);
	    BufferedInputStream bis = new BufferedInputStream(fis,4*1024);
		return bis;
	}
	
	public String getHeader() {
		return header;
	}
	
}
