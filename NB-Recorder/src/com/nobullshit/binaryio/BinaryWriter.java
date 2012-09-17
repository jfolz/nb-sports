package com.nobullshit.binaryio;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.json.JSONException;
import org.json.JSONStringer;

public class BinaryWriter extends DataOutputStream {
	
	public static final int DEFAULT_BUFFERSIZE = 4*1024;
	public static final String HEADER_BEGIN = "stupid binary format";
	public static final int version = 2;
	public static final String KEY_VERSION = "version";
	public static final String KEY_SERIES = "series";
	public static final String KEY_NAME = "name";
	public static final String KEY_IDENTIFIER = "identifier";
	public static final String KEY_ATTRIBUTES = "attributes";
	public static final String KEY_TYPES = "types";
	
	private CharSequence[] names;

	/**
	 * Create a new BinaryWriter to write a number of data series to a File
	 * @param out the File to write to
	 * @param defs Several CharSequence[]. These define what will be written to this
	 * 		BinaryWriter. These will be
	 * 		<ol>
	 * 			<li>An array of names for every series.
	 * 			May be null.
	 * 			If not null array must have one entry for every series,
	 * 			but entries may be null.</li>
	 * 			<li>For every series, first an array of attribute names,
	 * 			then one of primitive attribute types.</li>
	 *		</ol>
	 * 		(ASCII only) 
	 * @throws IOException
	 */
	public BinaryWriter(File out, CharSequence[] ... defs) throws IOException {
		super(create(out,DEFAULT_BUFFERSIZE));
		setHeader(defs);
	}

	/**
	 * Create a new BinaryWriter to write a number of data series to a File
	 * @param out the File to write to
	 * @param defs Several CharSequence[].
	 * 		These define the series that this BinaryWriter will write.<br>
	 * 		They have to be the following:
	 * 		<ol>
	 * 			<li>An array of names for every series.
	 * 			May be null.
	 * 			If not null, must have one non-null entry for every series.</li>
	 * 			<li>For every series, first an array of attribute names,
	 * 			then one of primitive attribute types
	 * 			(supported: "byte","short","int","long","float","double")</li>
	 *		</ol>
	 * 		(ASCII only)
	 * @param buffersize The size of the internal {@link BufferedWriter}
	 * @throws IOException
	 */
	public BinaryWriter(File out, int buffersize, CharSequence[] ... defs)
			throws IOException {
		super(create(out, buffersize));
		setHeader(defs);
	}
	
	private void setHeader(CharSequence[] ... defs) throws IOException {
		if(defs == null || defs.length == 0) return;
		
		int numSeries = (defs.length - 1) / 2;
		
		if(defs[0] != null) {
			names = defs[0];
			if(numSeries != names.length) throw new IllegalArgumentException(
					String.format("given %d names, but only %d series defined",
							names.length,numSeries));
		}
		
		if((defs.length - 1) % 2 != 0) {
			 throw new IllegalArgumentException(
						"must define attribute names AND types for each series");
		}
		
		JSONStringer stringer = new JSONStringer();
		try {
			stringer.object();
			
			stringer.key(KEY_VERSION);
			stringer.value(version);
			
			stringer.key(KEY_SERIES);
			stringer.array();
			for(int series=0; series<numSeries; series++) {
				if(defs[2*series+1].length != defs[2*series+2].length) {
						throw new IllegalArgumentException(String.format(
	"series #%d has inequal number of attributes (%d) and types (%d)",
	series,defs[2*series+1].length,defs[2*series+2].length));
				}
				
				stringer.object();
				
				if(names != null && names[series] != null) {
					stringer.key(KEY_NAME);
					stringer.value(names[series]);
				}
				
				stringer.key(KEY_IDENTIFIER);
				stringer.value(series+1);
				
				stringer.key(KEY_ATTRIBUTES);
				stringer.array();
				for(CharSequence attr: defs[2*series+1]) stringer.value(attr);
				stringer.endArray();
				
				stringer.key(KEY_TYPES);
				stringer.array();
				for(CharSequence attr: defs[2*series+2]) stringer.value(attr);
				stringer.endArray();
				
				stringer.endObject();
			}
			stringer.endArray();
			stringer.endObject();
			
			String json = stringer.toString();
			StringBuilder b = new StringBuilder(HEADER_BEGIN.length() + json.length() + 1);
			b.append(HEADER_BEGIN);
			b.append(stringer.toString());
			b.append('\n');
			
			int n = b.length();
			for(int i=0; i<n; i++) writeByte(b.charAt(i));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private static OutputStream create(File out, int buffersize) throws IOException {
	    FileOutputStream fos = new FileOutputStream(out);
		BufferedOutputStream bos = new BufferedOutputStream(fos,buffersize);
		return bos;
	}
	
	/**
	 * For a given series name, get the identifying number used by this BinaryWriter.
	 * @param name The name of the series
	 * @return the identifier or 0 if no such series was defined
	 */
	public byte getIdentifier(CharSequence name) {
		for(byte i=0; i<names.length; i++) if(name.equals(names[i])) return i;
		return 0;
	}
	
	public void startEntry(byte identifier) throws IOException {
		writeByte(identifier);
	}
	
	public void endEntry() throws IOException {
		// TODO add checksum support in version 3
	}

}
