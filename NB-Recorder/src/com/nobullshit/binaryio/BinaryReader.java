package com.nobullshit.binaryio;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public class BinaryReader extends DataInputStream {
	
	private String header;

	public BinaryReader(File in) throws IOException {
		super(create(in));
		header = readUTF();
	}
	
	private static InputStream create(File in) throws IOException {
	    FileInputStream fis = new FileInputStream(in);
	    BufferedInputStream bis = new BufferedInputStream(fis, 256*1024);
		GZIPInputStream zis = new GZIPInputStream(bis);
		return zis;
	}
	
	public String getHeader() {
		return header;
	}
	
}
