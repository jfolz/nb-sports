package com.nobullshit.binaryio;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

public interface Header extends Serializable {
	
	public void read(DataInputStream in);

	public void write(DataOutputStream out, Object o) throws IOException;
	
}