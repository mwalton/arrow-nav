package com.hci.cyclenav.util;

import com.hci.cyclenav.R;
import com.hci.cyclenav.guidance.GuidanceNode;
import com.hci.cyclenav.guidance.GuidanceNode.maneuver;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;

public class ArrowAnimation extends View {

	Paint black = new Paint();
	// float used to indicate how far the arrow is. Default set to 0;
	float percent = 0;

	// rectangle used as a mask for our animation 
	Rect mask = new Rect();

	int counter = 0;

	/*
	 * an int that is used to indicate which arrow has been passed in 1:
	 * straight 2: left 3: right 4: uturn
	 */
	GuidanceNode.maneuver arrowType;

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		black.setColor(Color.BLACK);
		black.setStyle(Paint.Style.FILL);

		// This draws the arrow with the appropriate mask on it
		if (arrowType == maneuver.STRAIGHT || arrowType == maneuver.BECOMES
				|| arrowType == maneuver.STAY_STRAIGHT || arrowType == maneuver.MERGE_STRAIGHT) {
			drawStraightArrow(canvas, Paint.Style.FILL);
			canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight()
					* (1 - percent), black);
			drawStraightArrow(canvas, Paint.Style.STROKE);
		}

		if (arrowType == maneuver.SHARP_LEFT || arrowType == maneuver.STAY_LEFT
				|| arrowType == maneuver.LEFT || arrowType == maneuver.SLIGHT_LEFT) {
			int rectBottom = canvas.getHeight();
			int rectRight = canvas.getWidth();

			if (percent <= (float) 0.384615385) {
				rectRight = canvas.getWidth() * 4 / 5;

				rectBottom = (int) Math.round((canvas.getHeight() * 9 / 10)
						- (canvas.getHeight() / 2) * percent * 13 / 5);

			}
			if (percent > (float) 0.384615385 && percent <= (float) 0.538461538) {
				rectBottom = canvas.getHeight() * 2 / 5;
				rectRight = canvas.getWidth()
						* 4
						/ 5
						- Math.round(canvas.getWidth() / 5
								* (percent - (float) 0.384615385) * 13 / 2);
			}

			if (percent > (float) 0.538461538) {
				rectBottom = canvas.getHeight() / 2;
				rectRight = canvas.getWidth()
						* 3
						/ 5
						- Math.round((canvas.getWidth() * 3 / 5)
								* (percent - (float) 0.538461538) * 13 / 6);
			}
			drawLeftArrow(canvas, Paint.Style.FILL);
			canvas.drawRect(0, 0, rectRight, rectBottom, black);
			drawLeftArrow(canvas, Paint.Style.STROKE);
		}
		if (arrowType == maneuver.SHARP_RIGHT || arrowType == maneuver.STAY_RIGHT
				|| arrowType == maneuver.RIGHT || arrowType == maneuver.SLIGHT_RIGHT) {
			int rectBottom = canvas.getHeight();
			int rectLeft = 0;

			if (percent <= (float) 0.384615385) {
				rectLeft = canvas.getWidth() / 5;

				rectBottom = (int) Math.round((canvas.getHeight() * 9 / 10)
						- (canvas.getHeight() / 2) * percent * 13 / 5);

			}

			if (percent > (float) 0.384615385 && percent <= (float) 0.538461538) {
				rectBottom = canvas.getHeight() * 2 / 5;
				rectLeft = canvas.getWidth()
						/ 5
						+ Math.round(canvas.getWidth() / 5
								* (percent - (float) 0.384615385) * 13 / 2);
			}

			if (percent > (float) 0.538461538) {
				rectBottom = canvas.getHeight() / 2;
				rectLeft = canvas.getWidth()
						* 2
						/ 5
						+ Math.round((canvas.getWidth() * 3 / 5)
								* (percent - (float) 0.538461538) * 13 / 6);
			}

			drawRightArrow(canvas, Paint.Style.FILL);
			canvas.drawRect(rectLeft, 0, canvas.getWidth(), rectBottom, black);
			drawRightArrow(canvas, Paint.Style.STROKE);
		}

		if (arrowType == maneuver.UTURN || arrowType == maneuver.UTURN_LEFT ||
				arrowType == maneuver.UTURN_RIGHT) {
			drawUturn(canvas, Paint.Style.FILL);
			if (counter < 50)
				canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(),
						black);
			drawUturn(canvas, Paint.Style.STROKE);
			counter++;
			counter = counter % 100;
		}
		// ATTN MYKE the next TWO lines should be deleted to stop constant
		// animation
		//percent += .01;
		//percent = percent % 1;
		invalidate();
	}

	/*
	 * each of these methods draw the appropriate arrow in with the style given.
	 * Each one should be used twice, once for outline, once for real arrow
	 */
	private void drawStraightArrow(Canvas canvas, Paint.Style style) {
		// getting a paint to fill the path (style set by method)
		Paint paint = new Paint();
		paint.setStrokeWidth(10);
		paint.setColor(android.graphics.Color.GREEN);
		paint.setStyle(style);
		paint.setAntiAlias(true);

		// Path to draw along
		Path path = new Path();
		path.setFillType(Path.FillType.EVEN_ODD);

		// for easy referencing
		float width = canvas.getWidth();
		float height = canvas.getHeight();

		// starts at tip of arrow
		path.moveTo(width / 2, 0);
		path.lineTo(width * 3 / 10, height * 3 / 10);
		path.lineTo(width * 2 / 5, height * 3 / 10);
		path.lineTo(width * 2 / 5, height);
		path.lineTo(width * 3 / 5, height);
		path.lineTo(width * 3 / 5, height * 3 / 10);
		path.lineTo(width * 7 / 10, height * 3 / 10);
		path.lineTo(width / 2, 0);
		path.close();

		canvas.drawPath(path, paint);

	}

	private void drawLeftArrow(Canvas canvas, Paint.Style style) {

		Paint paint = new Paint();
		paint.setStrokeWidth(10);
		paint.setColor(android.graphics.Color.GREEN);
		paint.setStyle(style);
		paint.setAntiAlias(true);

		// Path to draw along
		Path path = new Path();
		path.setFillType(Path.FillType.EVEN_ODD);

		// for easy referencing
		float width = canvas.getWidth();
		float height = canvas.getHeight();

		// starts at tip of arrow
		path.moveTo(0, height * 3 / 10);
		path.lineTo(width * 3 / 10, height / 10);
		path.lineTo(width * 3 / 10, height / 5);
		path.lineTo(width * 4 / 5, height / 5);
		path.lineTo(width * 4 / 5, height * 9 / 10);
		path.lineTo(width * 3 / 5, height * 9 / 10);
		path.lineTo(width * 3 / 5, height * 2 / 5);
		path.lineTo(width * 3 / 10, height * 2 / 5);
		path.lineTo(width * 3 / 10, height / 2);
		path.lineTo(0, height * 3 / 10);
		path.close();

		canvas.drawPath(path, paint);

	}

	private void drawRightArrow(Canvas canvas, Paint.Style style) {
		Paint paint = new Paint();
		paint.setStrokeWidth(10);
		paint.setColor(android.graphics.Color.GREEN);
		paint.setStyle(style);
		paint.setAntiAlias(true);

		// Path to draw along
		Path path = new Path();
		path.setFillType(Path.FillType.EVEN_ODD);

		// for easy referencing
		float width = canvas.getWidth();
		float height = canvas.getHeight();

		// starts at tip of arrow

		path.moveTo(width, height * 3 / 10);
		path.lineTo(width * 7 / 10, height / 10);
		path.lineTo(width * 7 / 10, height / 5);
		path.lineTo(width / 5, height / 5);
		path.lineTo(width / 5, height * 9 / 10);
		path.lineTo(width * 2 / 5, height * 9 / 10);
		path.lineTo(width * 2 / 5, height * 2 / 5);
		path.lineTo(width * 7 / 10, height * 2 / 5);
		path.lineTo(width * 7 / 10, height / 2);
		path.lineTo(width, height * 3 / 10);
		path.close();

		canvas.drawPath(path, paint);

	}

	private void drawUturn(Canvas canvas, Paint.Style style) {
		Paint paint = new Paint();
		paint.setStrokeWidth(10);
		paint.setColor(android.graphics.Color.GREEN);
		paint.setStyle(style);
		paint.setAntiAlias(true);

		// Path to draw along
		Path path = new Path();
		path.setFillType(Path.FillType.EVEN_ODD);

		// for easy referencing
		float width = canvas.getWidth();
		float height = canvas.getHeight();

		// starts at tip of arrow

		path.moveTo(width / 5, height);
		path.lineTo(0, height * 7 / 10);
		path.lineTo(width / 10, height * 7 / 10);
		path.lineTo(width / 10, height / 5);
		path.lineTo(width * 4 / 5, height / 5);
		path.lineTo(width * 4 / 5, height * 4 / 5);
		path.lineTo(width * 3 / 5, height * 4 / 5);
		path.lineTo(width * 3 / 5, height * 2 / 5);
		path.lineTo(width * 3 / 10, height * 2 / 5);
		path.lineTo(width * 3 / 10, height * 7 / 10);
		path.lineTo(width * 2 / 5, height * 7 / 10);
		path.lineTo(width / 5, height);
		path.close();

		canvas.drawPath(path, paint);

	}

	// Draws a triangle between these three points with the gven paint style

	public ArrowAnimation(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		TypedArray a = context.getTheme().obtainStyledAttributes(
		        attrs,
		        R.styleable.ArrowAnimation,
		        0, 0);
		try {
			this.percent = a.getFloat(R.styleable.ArrowAnimation_percentFilled, 0);
			int arrowNum = a.getInteger(R.styleable.ArrowAnimation_arrowType, 0);
			
			switch (arrowNum) {
				case 0: arrowType = maneuver.STRAIGHT; break;
				case 1: arrowType = maneuver.LEFT; break;
				case 2: arrowType = maneuver.RIGHT; break;
				case 3: arrowType = maneuver.UTURN; break;
				default: arrowType = maneuver.STRAIGHT; break;
			}
			
			} finally {
				a.recycle();
				}
	}

	public void setArrowType(GuidanceNode.maneuver arrow) {
		arrowType = arrow;
	}

	// set current percentage to arrow as you can here
	public void setFill(float percentage) {
		percent = percentage;

	}
}