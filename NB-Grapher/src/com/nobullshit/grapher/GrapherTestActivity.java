package com.nobullshit.grapher;

import com.nobullshit.text.DateFormatter;

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
        double[] Xs = new double[100];
        double[] Xs2 = new double[n];
    	for(int i=0; i<Xs2.length; i++) Xs2[i] = i;
        for(int i=0; i<Xs.length; i++) Xs[i] = Math.random()*10000 + System.currentTimeMillis();
        Graph g = (Graph) findViewById(R.id.graph1);
        addSeries(g,Xs2);
        Xs2[3] = 3.5;
        addSeries(g,Xs2);
        Xs2[4] = 4.5;
        addSeries(g,Xs2);
        g.refresh();
        ScatterGraph g2 = (ScatterGraph) findViewById(R.id.graph2);
        addSeriesRandom(g2, 20, Symbols.SYMBOL_TRIANGLE);
        addSeriesRandom(g2, 20, Symbols.SYMBOL_CROSS);
        addSeriesRandom(g2, 20, Symbols.SYMBOL_PLUS);
        g.refresh();
        g = (Graph) findViewById(R.id.graph3);
        addSeries(g,Xs);
        addSeries(g,Xs);
        g.setXTickFormatter(new DateFormatter("m:ss"));
        g.refresh();

        findViewById(R.id.graph4).setOnClickListener(this);
        findViewById(R.id.graph5).setOnClickListener(this);
    }
    
    private void addSeries(Graph g, double[] Xs) {
        double off = Math.random()/2;
        double[] Ys = new double[Xs.length];
        for(int i=0; i<Ys.length; i++) {
        	Ys[i] = Math.random()/2+off;
        }
        g.addSeries(Xs, Ys);
    }
    
    private void addSeriesRandom(ScatterGraph g, int n, int symbol) {
    	double[] Xs = new double[n];
        double[] Ys = new double[n];
        for(int i=0; i<Ys.length; i++) {
        	Xs[i] = Math.random();
        	Ys[i] = Math.random();
        }
        g.addSeries(Xs, Ys, 0, null, symbol);
    }

	public void onClick(View v) {
		Graph g;
		double[] Xs = new double[n];
		double[] Ys = new double[n];
        for(int i=0; i<Ys.length; i++) {
        	Ys[i] = Math.random()-.5;
        	Xs[i] = i-3;
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