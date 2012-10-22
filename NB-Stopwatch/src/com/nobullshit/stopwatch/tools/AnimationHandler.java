package com.nobullshit.stopwatch.tools;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

public class AnimationHandler implements AnimationListener {
	
	public static int DEFAULT_ANIMATION_DURATION = 300;
	
	private Context context;
	private View animatedView;
	private ViewGroup from;
	private ViewGroup to;
	private int newpos;
	private int run = 0;
	
	private AnimationHandler() {}
	
	public static void animate(final Context c, final View view,
			ViewGroup from, ViewGroup to, int newpos) {
		final AnimationHandler handler = new AnimationHandler();
		handler.context = c;
		handler.animatedView = view;
		handler.from = from;
		handler.to = to;
		handler.newpos = newpos;
		handler.run = 0;
		
		if(from != null && newpos >= 0) {
			Animation animation = AnimationUtils.makeOutAnimation(c, true);
			animation.setDuration(DEFAULT_ANIMATION_DURATION);
			animation.setAnimationListener(handler);
			animation.setZAdjustment(Animation.ZORDER_BOTTOM);
			view.startAnimation(animation);
			//view.postInvalidateDelayed(20);
		}
		else {
			handler.run = 1;
			view.setVisibility(View.INVISIBLE);
			Animation animation = AnimationUtils.makeInAnimation(c, true);
			animation.setDuration(DEFAULT_ANIMATION_DURATION);
			animation.setAnimationListener(handler);
			animation.setZAdjustment(Animation.ZORDER_BOTTOM);
			view.startAnimation(animation);
			//view.postInvalidateDelayed(20);
		}
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		if(run == 0) {
			run++;
			//animatedView.clearAnimation();
			animatedView.setVisibility(View.INVISIBLE);
			
			animatedView.postDelayed(new Runnable() {
				public void run() {
					((ViewGroup) animatedView.getParent()).removeView(animatedView);
					from.removeView(animatedView);
					to.addView(animatedView, newpos);
					
					Animation inanimation = AnimationUtils.makeInAnimation(context, true);
					inanimation.setDuration(DEFAULT_ANIMATION_DURATION);
					inanimation.setAnimationListener(AnimationHandler.this);
					inanimation.setZAdjustment(Animation.ZORDER_BOTTOM);
					animatedView.startAnimation(inanimation);
					//animatedView.postInvalidateDelayed(20);
				}
			},100);
		}
		else if(run == 1) {
			//animatedView.clearAnimation();
		}
	}

	@Override
	public void onAnimationRepeat(Animation animation) {}

	@Override
	public void onAnimationStart(Animation animation) {
		animatedView.setVisibility(View.VISIBLE);
	}

}
