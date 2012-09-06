package com.nobullshit.grapher;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class GrapherTestActivity extends Activity implements OnClickListener {
	
	int n = 5;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        double off = Math.random();
        double[] Ys = new double[n];
        double[] Xs = new double[n];
        double[] Xs2 = new double[n];
        for(int i=0; i<Ys.length; i++) {
        	Ys[i] = Math.random()+off;
        	Xs[i] = Math.random()*10000 + System.currentTimeMillis();
        	Xs2[i] = i;
        }
        Graph g = (Graph) findViewById(R.id.graph1);
        g.addSeries(Xs2, Ys);
        Xs2[3] = 3.5;
        addSeries(g,n,Xs2);
        Xs2[4] = 4.5;
        addSeries(g,n,Xs2);
        g.refresh();
        g = (Graph) findViewById(R.id.graph2);
        g.addSeries(null, Ys);
        g.refresh();
        g = (Graph) findViewById(R.id.graph3);
        g.addSeries(null, Ys);
        g.refresh();

        findViewById(R.id.graph4).setOnClickListener(this);
        findViewById(R.id.graph5).setOnClickListener(this);
    }
    
    private void addSeries(Graph g, int n, double[] Xs) {
        double off = Math.random();
        double[] Ys = new double[n];
        for(int i=0; i<Ys.length; i++) {
        	Ys[i] = Math.random()+off;
        }
        g.addSeries(Xs, Ys);
    }

	public void onClick(View v) {
		Graph g;
		double[] Xs = new double[n];
		double[] Ys = new double[n];
		double off = Math.random();
        for(int i=0; i<Ys.length; i++) {
        	Ys[i] = Math.random()+off;
        	Xs[i] = i;
        }
		switch(v.getId()) {
			case R.id.graph4:
			case R.id.graph5:
				g = (Graph) v;
				g.addSeries(Xs,Ys);
				g.refresh();
		}
	}
}