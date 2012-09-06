package com.nobullshit.grapher;

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
    }
    
    private void addSeries(Graph g, int n, double[] Xs) {
        double off = Math.random();
        double[] Ys = new double[n];
        for(int i=0; i<Ys.length; i++) {
        	Ys[i] = Math.random()+off;
        }
        g.addSeries(Xs, Ys);
    }
}