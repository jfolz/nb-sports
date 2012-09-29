package com.nobullshit.recorder;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

import com.nobullshit.binaryio.BinaryReader;
import com.nobullshit.binaryio.BinaryWriter;

public class TestbedActivity extends Activity {
	
	private TextView output;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testbed);
        
        output = (TextView) findViewById(R.id.output);
        File dir = getExternalFilesDir(null);
        File out = new File(dir,"testbed.out");
        try {
        	byte idummy = 1;
			BinaryWriter writer = new BinaryWriter(out,
					new CharSequence[] {"test","dummy"},
					new CharSequence[] {"value"},
					new CharSequence[] {"integer"},
					new CharSequence[] {"dummy1","dummy2","dummy3"},
					new CharSequence[] {"integer","double","short"});
			int i = 0;
			for(; i<20; i++) {
				writer.startEntry(idummy);
				writer.writeInt(i);
				writer.endEntry();
			}
			writer.close();
			
			writer = new BinaryWriter(out);
			for(; i<50; i++) {
				writer.startEntry(idummy);
				writer.writeInt(i);
				writer.endEntry();
			}
			writer.close();
			
			BinaryReader reader = new BinaryReader(out);
			output.append("Series: ");
			appendArray(reader.getNames());
			output.append("\n");
			for(CharSequence name: reader.getNames()) {
				output.append("Attributes " + name + ": ");
				appendArray(reader.getAttributes(name));
				output.append("\n");
				output.append("Types " + name + ": ");
				appendArray(reader.getTypes(name));
				output.append("\n");
			}
			output.append("Data:\n");
			while(reader.available() > 0) {
				reader.readByte();
				output.append(reader.readInt() + "\n");
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_testbed, menu);
        return true;
    }
    
    private void appendArray(CharSequence[] arr) {
		for(int i=0; i<arr.length; i++) {
			output.append(arr[i]);
			if(i < arr.length - 1) output.append(", ");
		}
    }
}
