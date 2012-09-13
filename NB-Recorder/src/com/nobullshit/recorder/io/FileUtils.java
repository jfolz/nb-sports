package com.nobullshit.recorder.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileUtils {
	
	public static void ZipFiles(File[] files, File dest) throws IOException {
		dest.createNewFile();
		FileOutputStream fos = new FileOutputStream(dest);
		ZipOutputStream zos = new ZipOutputStream(fos);
		
		byte[] buffer = new byte[4*1024];
		for(File in: files) {
			zos.putNextEntry(new ZipEntry(in.getName()));
			FileInputStream fis = new FileInputStream(in);
			int l = fis.read(buffer);
			while(l > 0) {
				zos.write(buffer, 0, l);
				l = fis.read(buffer);
			}
			fis.close();
			zos.closeEntry();
		}
		
		zos.close();
	}

}
