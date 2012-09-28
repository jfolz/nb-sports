package com.nobullshit.binaryio;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class BinaryReader extends DataInputStream {
	
	private String header;
	private CharSequence[][] headerChar;
		
	public BinaryReader(File in) throws IOException {
		super(create(in));
		StringBuilder b = new StringBuilder();
		char c = (char) readByte();
		while(c != '\n') {
			b.append(c);
			c = (char) readByte();
		}
		header = b.toString();
		try {
			this.parseHeader();
		} catch (JSONException e) {
			throw new IOException(e);
		}
	}
	
	private static InputStream create(File in) throws IOException {
	    FileInputStream fis = new FileInputStream(in);
	    BufferedInputStream bis = new BufferedInputStream(fis,4*1024);
		return bis;
	}
	
	public String getHeader() {
		return header;
	}
	
	public CharSequence[] getNames(){
		return headerChar[0];
	}
	
	public CharSequence[] getAttributes(CharSequence name)
	{
		int j = -1;
		while(headerChar[0][++j] != name);
		return headerChar[2*j+1];
	}
	
	public CharSequence[] getTypes(CharSequence name)
	{
		int j = -1;
		while(headerChar[0][++j] != name);
		return headerChar[2*j+2];
	}
	
	private void parseHeader() throws JSONException{
		String JSONString = header.substring(BinaryWriter.HEADER_BEGIN.length());
		JSONObject object = (JSONObject) new JSONTokener(JSONString).nextValue();
		int version = object.getInt(BinaryWriter.KEY_VERSION);
		if(version == 2)
		{
			JSONArray series = object.getJSONArray(BinaryWriter.KEY_SERIES);
			int n = series.length();
			int[] identifiers = new int[n];
			String[] name = new String[n];
			CharSequence[][] attributes = new CharSequence[n][];
			CharSequence[][] types = new CharSequence[n][];
			for(int i = 0; i< n; i++)
			{
				JSONObject innerObject = (JSONObject) series.get(i);
				identifiers[i] = innerObject.getInt(BinaryWriter.KEY_IDENTIFIER);
				name[i] = innerObject.getString(BinaryWriter.KEY_NAME);
				attributes[i] = JSONtoArray(innerObject.getJSONArray(BinaryWriter.KEY_ATTRIBUTES));
				types[i] = JSONtoArray(innerObject.getJSONArray(BinaryWriter.KEY_TYPES));
			}
			Argsort as = new Argsort(identifiers);
			int[] indices = as.argsort();
			headerChar = new CharSequence[2*n+1][];
			headerChar[0] = new CharSequence[n];
			for(int j = 0; j< n; j++)
			{
				headerChar[0][j] = name[indices[j]];
				headerChar[2*j+1] = attributes[indices[j]];
				headerChar[2*j+2] = types[indices[j]];
			}
		}
	}
	private CharSequence[] JSONtoArray(JSONArray j) throws JSONException
	{
		CharSequence[] result = new CharSequence[j.length()];
		for(int i = 0; i<j.length(); i++){
			result[i] = j.getString(i);
		}
		return result;
	}
	private class Argsort implements Comparator<Integer> {
		private int[] toSort;
		
		public Argsort(int[] toSort) {
			this.toSort = toSort;
		}

		public int compare(Integer lhs, Integer rhs) {
			if(toSort[lhs] < toSort[rhs]) return -1;
			else if(toSort[lhs] > toSort[rhs]) return 1;
			else return 0;
		}
		
		public int[] argsort() {
			Integer[] indices = new Integer[toSort.length];
			for(int i=0; i<indices.length; i++) indices[i] = i;
			Arrays.sort(indices, this);
			int[] out = new int[indices.length];
			for(int i=0; i<indices.length; i++) out[i] = indices[i].intValue();
			return out;
		}
	}
	
}
