package android.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import com.meui.MIUISlidingButton.*;  // The R.java in the project. You can replace this with your own.

/**
 * MIUI Sliding Button Original Tutorial Link 
 * @see <a href="http://tieba.baidu.com/p/4924309975">Port Guide</a>
 * Patched by ME Failed Coder using JaDX
 * Thanks to Xiaomi Programmer, XDA Galaxy Y Duos Developer and chjj54.
 */

public class SlidingButton extends CheckBox {
	private static final int ANIMATION_FRAME_DURATION = 16;
	private static final float MAXIMUM_MINOR_VELOCITY = 150.0f;
	private static final int MSG_ANIMATE = 1000;
	private static final int TAP_THRESHOLD = 6;
	private static final String tag = "SlidingButton";
	private BitmapDrawable mActiveSlider;
	private int[] mAlphaPixels;
	private float mAnimatedVelocity;
	private boolean mAnimating;
	private long mAnimationLastTime;
	private float mAnimationPosition;
	private Bitmap mBarBitmap;
	private int[] mBarSlice;
	private long mCurrentAnimationTime;
	private BitmapDrawable mFrame;
	private final Handler mHandler;
	private int mHeight;
	private int mLastX;
	private BitmapDrawable mOffDisable;
	private OnCheckedChangedListener mOnCheckedChangedListener;
	private BitmapDrawable mOnDisable;
	private int mOriginalTouchPointX;
	private BitmapDrawable mPressedSlider;
	private BitmapDrawable mSlider;
	private boolean mSliderMoved;
	private int mSliderOffset;
	private int mSliderPosition;
	private int mSliderWidth;
	private int mTapThreshold;
	private boolean mTracking;
	private int mWidth;

	static /* synthetic */ class AnonymousClass_1 {
	}

	public static interface OnCheckedChangedListener {
		public void onCheckedChanged(boolean r1z);
	}

	private class SlidingHandler extends Handler {
		final /* synthetic */ SlidingButton this$0;

		private SlidingHandler(SlidingButton r1_SlidingButton) {
			super();
			this$0 = r1_SlidingButton;
		}

		/* synthetic */ SlidingHandler(SlidingButton x0, SlidingButton.AnonymousClass_1 x1) {
			this(x0);
		}

		public void handleMessage(Message msg) {
			switch(msg.what) {
			case MSG_ANIMATE:
				this$0.doAnimation();
			}
		}
	}


	public SlidingButton(Context context) {
		super(context);
		mAnimating = false;
		mHandler = new SlidingHandler(this, null);
		mAnimatedVelocity = 150.0f;
		mOnCheckedChangedListener = null;
		initialize();
	}

	public SlidingButton(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SlidingButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mAnimating = false;
		mHandler = new SlidingHandler(this, null);
		mAnimatedVelocity = 150.0f;
		mOnCheckedChangedListener = null;
		initialize();
	}

	private void animateOff() {
		Log.i(tag, "animateOff");
		performFling(-150.0f);
		invalidate();
	}

	private void animateOn() {
		Log.i(tag, "animateOn");
		performFling(MAXIMUM_MINOR_VELOCITY);
		invalidate();
	}

	private void animateToggle() {
		Log.i(tag, "animateToggle");
		if (isChecked()) {
			animateOff();
		} else {
			animateOn();
		}
	}

	private void cutEdge(int width, int height, int[] alphaPixels) {
		int i = (width * height) - 1;
		while (i >= 0) {
			alphaPixels[i] = alphaPixels[i] & (((((alphaPixels[i] >>> 24) * (mAlphaPixels[i] >>> 24)) / 255) << 24) + 16777215);
			i--;
		}
	}

	private void doAnimation() {
		String r3_String = tag;
		Log.i(r3_String, "doAnimation:  offset: " + mSliderOffset + "  position:  " + mSliderPosition);
		if (!mAnimating) {
		} else {
			incrementAnimation();
			moveSlider((int) mAnimationPosition);
			if (mSliderOffset <= 0 || mSliderOffset >= mSliderPosition) {
				mAnimating = false;
				mHandler.removeMessages(MSG_ANIMATE);
				boolean mOriginalChecked = isChecked();
				if (mSliderOffset >= mSliderPosition) {
					Log.i(r3_String, "doAnimation:  setChecked(true)");
					setChecked(true);
				} else {
					Log.i(r3_String, "doAnimation:  setChecked(false)");
					setChecked(false);
				}
				if (mOnCheckedChangedListener == null || mOriginalChecked == isChecked()) {
				} else {
					Log.i(r3_String, "doAnimation:  onCheckedChanged(isChecked())=" + isChecked());
					mOnCheckedChangedListener.onCheckedChanged(isChecked());
				}
			} else {
				mCurrentAnimationTime += 16;
				mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_ANIMATE), mCurrentAnimationTime);
			}
		}
	}

	private void drawSlidingBar(Canvas canvas) {
		mBarBitmap.getPixels(mBarSlice, 0, mWidth, mSliderPosition - mSliderOffset, 0, mWidth, mHeight);
		cutEdge(mWidth, mHeight, mBarSlice);
		canvas.drawBitmap(mBarSlice, 0, mWidth, 0, 0, mWidth, mHeight, true, null);
	}

	private void incrementAnimation() {
		long now = SystemClock.uptimeMillis();
		mAnimationPosition = ((mAnimatedVelocity * ((float) (now - mAnimationLastTime))) / 1000.0f) + mAnimationPosition;
		mAnimationLastTime = now;
	}

	private void initialize() {
		setDrawingCacheEnabled(false);
		mTapThreshold = (int) ((6.0f * getResources().getDisplayMetrics().density) + 0.5f);
		mFrame = (BitmapDrawable) getResources().getDrawable(R.drawable.sliding_btn_frame);
		mSlider = (BitmapDrawable) getResources().getDrawable(R.drawable.sliding_btn_slider);
		mPressedSlider = (BitmapDrawable) getResources().getDrawable(R.drawable.sliding_btn_slider_pressed);
		mOnDisable = (BitmapDrawable) getResources().getDrawable(R.drawable.sliding_btn_on_disable);
		mOffDisable = (BitmapDrawable) getResources().getDrawable(R.drawable.sliding_btn_off_disable);
		mWidth = mFrame.getIntrinsicWidth();
		mHeight = mFrame.getIntrinsicHeight();
		mActiveSlider = mSlider;
		mSliderWidth = Math.min(mWidth, mSlider.getIntrinsicWidth());
		mSliderPosition = mWidth - mSliderWidth;
		mFrame.setBounds(0, 0, mWidth, mHeight);
		mOnDisable.setBounds(0, 0, mWidth, mHeight);
		mOffDisable.setBounds(0, 0, mWidth, mHeight);
		mAlphaPixels = new int[(mWidth * mHeight)];
		Bitmap mMaskBitmap = Bitmap.createScaledBitmap(((BitmapDrawable) getResources().getDrawable(R.drawable.sliding_btn_mask)).getBitmap(), mWidth, mHeight, false);
		mMaskBitmap.getPixels(mAlphaPixels, 0, mWidth, 0, 0, mWidth, mHeight);
		mMaskBitmap.recycle();
		mBarSlice = new int[(mWidth * mHeight)];
		mBarBitmap = Bitmap.createScaledBitmap(((BitmapDrawable) getResources().getDrawable(R.drawable.sliding_btn_bar)).getBitmap(), (mWidth * 2) - mSliderWidth, mHeight, true);
	}

	private void moveSlider(int positon) {
		mSliderOffset += positon;
		if (mSliderOffset < 0) {
			mSliderOffset = 0;
		}
		if (mSliderOffset > mSliderPosition) {
			mSliderOffset = mSliderPosition;
		}
		invalidate();
	}

	private void performFling(float anmatedVelocity) {
		mAnimating = true;
		mAnimationPosition = 0.0f;
		mAnimatedVelocity = anmatedVelocity;
		mAnimationLastTime = SystemClock.uptimeMillis();
		mCurrentAnimationTime = 16 + mAnimationLastTime;
		mHandler.removeMessages(MSG_ANIMATE);
		mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_ANIMATE), mCurrentAnimationTime);
	}

	protected void onDraw(Canvas canvas) {
		String r1_String = tag;
		super.onDraw(canvas);
		if (!isEnabled()) {
			if (isChecked()) {
				Log.i(r1_String, "onDraw: mOnDisable.draw");
				mOnDisable.draw(canvas);
			} else {
				Log.i(r1_String, "onDraw: mOffDisable.draw");
				mOffDisable.draw(canvas);
			}
		} else {
			drawSlidingBar(canvas);
			mFrame.draw(canvas);
			mActiveSlider.setBounds(mSliderOffset, 0, mSliderOffset + mSliderWidth, mHeight);
			mActiveSlider.draw(canvas);
		}
	}

	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(mWidth, mHeight);
	}

	public boolean onTouchEvent(MotionEvent event) {
		if (!isEnabled()) {
			return false;
		} else {
			int x = (int) event.getX();
			int y = (int) event.getY();
			Rect localRect = new Rect(mSliderOffset, 0, mSliderOffset + mSliderWidth, mHeight);
			switch(event.getAction()) {
			case 0:
			case 2:
				if (localRect.contains(x, y)) {
					mTracking = true;
					mActiveSlider = mPressedSlider;
					mOriginalTouchPointX = x;
					invalidate();
				} else {
					mSliderMoved = false;
				}
				if (mTracking) {
					moveSlider(x - mLastX);
					if (Math.abs(x - mOriginalTouchPointX) >= mTapThreshold) {
						mSliderMoved = true;
						getParent().requestDisallowInterceptTouchEvent(true);
					}
				}
				mLastX = x;
				break;
			case 1:
				if (mTracking) {
					if (!mSliderMoved) {
						animateToggle();
					} else if (mSliderOffset >= 0) {
						if (mSliderOffset <= mSliderPosition / 2) {
							animateOff();
						} else {
							animateOn();
						}
					}
				} else {
					animateToggle();
				}
				mTracking = false;
				mSliderMoved = false;
				break;
			case 3:
				if (isChecked()) {
					animateOn();
				} else {
					mTracking = false;
					mSliderMoved = false;
					animateOff();
				}
				break;
			}
			return true;
		}
	}

	public void setButtonDrawable(Drawable paramDrawable) {
	}

	public void setChecked(boolean checked) {
		Log.i(tag, "setChecked " + checked);
		super.setChecked(checked);
		if (checked) {
			mSliderOffset = mWidth - mSliderWidth;
		} else {
			mSliderOffset = 0;
		}
		mActiveSlider = mSlider;
		invalidate();
	}

	public void setOnCheckedChangedListener(OnCheckedChangedListener listener) {
		mOnCheckedChangedListener = listener;
	}
}
