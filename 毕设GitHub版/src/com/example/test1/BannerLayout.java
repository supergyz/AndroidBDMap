package com.example.test1;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;


public class BannerLayout extends ViewGroup {

	private Scroller scroller;
	private float mLastMotionX;
//	private int mTouchSlop;
	
	private OnItemClickListener onItemClickListener;
	
	private int currentScreenIndex=0;
	
	private boolean moving=false;
	
	private boolean autoScroll=false;
	
	private int scrollTime=1*1000;//滚动时间间隔
	
	private int currentWhat=0;
	
	@SuppressLint("HandlerLeak")
	private Handler handler=new Handler()
	{
		@Override
		public void handleMessage(Message msg) {
			if(autoScroll && currentWhat==msg.what)
			{
				currentScreenIndex=(currentScreenIndex+1)%getChildCount();
				scrollToScreen(currentScreenIndex);
				
				//Log.i("TAG","handleMessage scrollToScreen:"+currentScreenIndex);
				
				if(autoScroll)
					handler.sendEmptyMessageDelayed(currentWhat, scrollTime);
			}
		}
	};
	
	public BannerLayout(Context context) {
		super(context);

		initView(context);
		// TODO Auto-generated constructor stub
	}

	public BannerLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		initView(context);
	}

	public BannerLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
		// TODO Auto-generated constructor stub
	}

	private void initView(final Context context) {
		this.scroller = new Scroller(context, new DecelerateInterpolator(4));//OvershootInterpolator(1.1f)

		
//		handler.sendEmptyMessageDelayed(currentWhat, scrollTime);
		
//		final ViewConfiguration configuration = ViewConfiguration
//				.get(getContext());
//		mTouchSlop = configuration.getScaledTouchSlop();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		int maxHeight=-1;
		
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
			
			maxHeight=Math.max(maxHeight, getChildAt(i).getMeasuredHeight());
			
		}
		maxHeight=Math.min(maxHeight, MeasureSpec.getSize(heightMeasureSpec));
		
		//Log.e("TAG","onMeasure Height:"+maxHeight);
		
		setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),maxHeight);
	}
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {

		final int count = getChildCount();

		int cLeft = 0;
		
		
		for (int i = 0; i < count; i++) {
			View child = getChildAt(i);
			if (child.getVisibility() == View.GONE)
				continue;
			
//			child.setVisibility(View.VISIBLE);
			final int childWidth = child.getMeasuredWidth();
			child.layout(cLeft, 0, cLeft +childWidth, child.getMeasuredHeight());

			cLeft += childWidth;
		}
	}

	@Override
	public void computeScroll() {
		if (scroller.computeScrollOffset()) {
			scrollTo(scroller.getCurrX(), 0);
			postInvalidate();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (getChildCount() == 0)
			return false;
		final int action = ev.getAction();
		final float x = ev.getX();
		switch (action) {
		case MotionEvent.ACTION_DOWN:

			autoScroll=false;
			
			currentWhat++;
			
			mLastMotionX = x;
			if (!scroller.isFinished()) {
				scroller.abortAnimation();
			}
			
			moving=false;
//			Log.i("TAG","ACTION_DOWN");
			
			return true;

		case MotionEvent.ACTION_MOVE:
			final int deltaX = (int) (mLastMotionX - x);
			boolean xMoved = Math.abs(deltaX) > 4;
			if(!moving && !xMoved)
				break;
			mLastMotionX = x;
			
			if((0==currentScreenIndex && deltaX<0) || (getChildCount()-1==currentScreenIndex && deltaX>0))
				scrollBy(deltaX/4, 0);
			else
				scrollBy(deltaX, 0);
			
			moving=true;
			
			{
				final int screenWidth = getWidth();
				currentScreenIndex=(getScrollX() + (screenWidth / 2))/ screenWidth;
			}
			
			return true;
		case MotionEvent.ACTION_UP:
			snapToDestination();
			
			if(!autoScroll)
			{
				startScroll();
			}
			if(!moving && null!=onItemClickListener)
			{
				final int screenWidth = getWidth();
				int index=(int) ((getScrollX()+x)/ screenWidth);
				onItemClickListener.onClick(index,getChildAt(index));
			}
				
			break;
		case MotionEvent.ACTION_CANCEL:
			snapToDestination();
			if(!autoScroll)
			{
				startScroll();
			}
			
		}
		return false;
	}
	private void scrollToScreen(int whichScreen)
	{
//		if (!scroller.isFinished())
//			return;
//		Log.e("TAG","scrollToScreen:"+whichScreen);
		if(whichScreen>=getChildCount())
			whichScreen=getChildCount()-1;
		
		int delta = 0;
		
		delta = whichScreen * getWidth() - getScrollX();
		
//		scroller.startScroll(getScrollX(), 0, delta, 0, Math.abs(delta) * 2);
		scroller.startScroll(getScrollX(), 0, delta, 0, 1500);
		invalidate();
		
		currentScreenIndex=whichScreen;
	}
	private void snapToDestination()
	{
		final int x=getScrollX();
		final int screenWidth = getWidth();
		
		scrollToScreen((x + (screenWidth / 2))/ screenWidth);
	}
	
	public int getCurrentScreenIndex() {
		return currentScreenIndex;
	}
	public void startScroll()
	{
		
		autoScroll=true;
		handler.sendEmptyMessageDelayed(currentWhat, scrollTime);
	}
	public boolean isScrolling()
	{
		return autoScroll;
	}
	public void stopScroll()
	{
		autoScroll=false;
		currentWhat++;
	}
	@Override
	protected void finalize() throws Throwable {

		//Log.e("TAG","finalize===");

		super.finalize();
	}
	
	public OnItemClickListener getOnItemClickListener() {
		return onItemClickListener;
	}

	public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
		this.onItemClickListener = onItemClickListener;
	}

	public interface OnItemClickListener
	{
		public void onClick(int index,View childview);
	}
//	OnClickListener onclick;
}