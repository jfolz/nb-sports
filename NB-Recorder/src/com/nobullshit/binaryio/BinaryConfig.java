package com.nobullshit.binaryio;

public interface BinaryConfig<T> {
	
	public void write(T data, BinaryWriter writer);
	
	public T read(BinaryReader reader);

}
