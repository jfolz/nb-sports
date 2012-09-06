package com.nobullshit.grapher;

import com.nobullshit.text.DateFormatter;

import android.app.Activity;
import android.os.Bundle;

public class GrapherTestActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        int n = 5;
        double off = Math.random();
        double[] Ys = new double[n];
        double[] Xs = new double[n];
        for(int i=0; i<Ys.length; i++) {
        	Ys[i] = Math.random()+off;
        	Xs[i] = Math.random()*10000 + System.currentTimeMillis();
        }
        Graph g = (Graph) findViewById(R.id.graph1);
        g.addSeries(Xs, Ys);
        addSeries(g,Xs);
        addSeries(g,Xs);
        g.setXTickFormatter(new DateFormatter("mm:ss"));
        g.refresh();
        g = (Graph) findViewById(R.id.graph2);
        g.addSeries(null, Ys);
        g = (Graph) findViewById(R.id.graph3);
        g.addSeries(null, Ys);
    }
    
    private void addSeries(Graph g, double[] Xs) {
        int n = 10;
        double off = Math.random();
        double[] Ys = new double[n];
        for(int i=0; i<Ys.length; i++) {
        	Ys[i] = Math.random()+off;
        }
        g.addSeries(Xs, Ys);
    }
}