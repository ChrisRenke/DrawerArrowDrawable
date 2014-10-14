/*
 * Copyright (C) 2014 Chris Renke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package chrisrenke.drawerarrowdrawable;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import static android.graphics.Color.BLACK;
import static android.graphics.Paint.ANTI_ALIAS_FLAG;
import static android.graphics.Paint.Cap;
import static android.graphics.Paint.Cap.BUTT;
import static android.graphics.Paint.Cap.ROUND;
import static android.graphics.Paint.SUBPIXEL_TEXT_FLAG;
import static android.graphics.Paint.Style.STROKE;
import static android.graphics.PixelFormat.TRANSLUCENT;
import static android.support.v4.widget.DrawerLayout.DrawerListener;
import static java.lang.Math.sqrt;

/** A drawable that rotates between a drawer icon and a back arrow based on parameter. */
public class DrawerArrowDrawable extends Drawable {

  /**
   * Joins two {@link Path}s as if they were one where the first 50% of the path is {@code
   * PathFirst} and the second 50% of the path is {@code pathSecond}.
   */
  private static class JoinedPath {

    private final PathMeasure measureFirst;
    private final PathMeasure measureSecond;
    private final float lengthFirst;
    private final float lengthSecond;

    private JoinedPath(Path pathFirst, Path pathSecond) {
      measureFirst = new PathMeasure(pathFirst, false);
      measureSecond = new PathMeasure(pathSecond, false);
      lengthFirst = measureFirst.getLength();
      lengthSecond = measureSecond.getLength();
    }

    /**
     * Returns a point on this curve at the given {@code parameter}.
     * For {@code parameter} values less than .5f, the first path will drive the point.
     * For {@code parameter} values greater than .5f, the second path will drive the point.
     * For {@code parameter} equal to .5f, the point will be the point where the two
     * internal paths connect.
     */
    private void getPointOnLine(float parameter, float[] coords) {
      if (parameter <= .5f) {
        parameter *= 2;
        measureFirst.getPosTan(lengthFirst * parameter, coords, null);
      } else {
        parameter -= .5f;
        parameter *= 2;
        measureSecond.getPosTan(lengthSecond * parameter, coords, null);
      }
    }
  }

  /** Draws a line between two {@link JoinedPath}s at distance {@code parameter} along each path. */
  private class BridgingLine {

    private final JoinedPath pathA;
    private final JoinedPath pathB;

    private BridgingLine(JoinedPath pathA, JoinedPath pathB) {
      this.pathA = pathA;
      this.pathB = pathB;
    }

    /**
     * Draw a line between the points defined on the paths backing {@code measureA} and
     * {@code measureB} at the current parameter.
     */
    private void draw(Canvas canvas) {
      pathA.getPointOnLine(parameter, coordsA);
      pathB.getPointOnLine(parameter, coordsB);
      if (rounded) insetPointsForRoundCaps();
      canvas.drawLine(coordsA[0], coordsA[1], coordsB[0], coordsB[1], linePaint);
    }

    /**
     * Insets the end points of the current line to account for the protruding
     * ends drawn for {@link Cap#ROUND} style lines.
     */
    private void insetPointsForRoundCaps() {
      vX = coordsB[0] - coordsA[0];
      vY = coordsB[1] - coordsA[1];

      magnitude = (float) sqrt((vX * vX + vY * vY));
      paramA = (magnitude - halfStrokeWidthPixel) / magnitude;
      paramB = halfStrokeWidthPixel / magnitude;

      coordsA[0] = coordsB[0] - (vX * paramA);
      coordsA[1] = coordsB[1] - (vY * paramA);
      coordsB[0] = coordsB[0] - (vX * paramB);
      coordsB[1] = coordsB[1] - (vY * paramB);
    }
  }

  /** Paths were generated at a 3px/dp density; this is the scale factor for different densities. */
  private final static float PATH_GEN_DENSITY = 3;

  /** Paths were generated with at this size for {@link DrawerArrowDrawable#PATH_GEN_DENSITY}. */
  private final static float DIMEN_DP = 23.5f;

  /**
   * Paths were generated targeting this stroke width to form the arrowhead properly, modification
   * may cause the arrow to not for nicely.
   */
  private final static float STROKE_WIDTH_DP = 2;

  private BridgingLine topLine;
  private BridgingLine middleLine;
  private BridgingLine bottomLine;

  private final Rect bounds;
  private final float halfStrokeWidthPixel;
  private final Paint linePaint;
  private final boolean rounded;

  private boolean flip;
  private float parameter;

  // Helper fields during drawing calculations.
  private float vX, vY, magnitude, paramA, paramB;
  private final float coordsA[] = { 0f, 0f };
  private final float coordsB[] = { 0f, 0f };

  public DrawerArrowDrawable(Resources resources) {
    this(resources, false);
  }

  public DrawerArrowDrawable(Resources resources, boolean rounded) {
    this.rounded = rounded;
    float density = resources.getDisplayMetrics().density;
    float strokeWidthPixel = STROKE_WIDTH_DP * density;
    halfStrokeWidthPixel = strokeWidthPixel / 2;

    linePaint = new Paint(SUBPIXEL_TEXT_FLAG | ANTI_ALIAS_FLAG);
    linePaint.setStrokeCap(rounded ? ROUND : BUTT);
    linePaint.setColor(BLACK);
    linePaint.setStyle(STROKE);
    linePaint.setStrokeWidth(strokeWidthPixel);

    int dimen = (int) (DIMEN_DP * density);
    bounds = new Rect(0, 0, dimen, dimen);

    Path first, second;
    JoinedPath joinedA, joinedB;

    // Top
    first = new Path();
    first.moveTo(5.042f, 20f);
    first.rCubicTo(8.125f, -16.317f, 39.753f, -27.851f, 55.49f, -2.765f);
    second = new Path();
    second.moveTo(60.531f, 17.235f);
    second.rCubicTo(11.301f, 18.015f, -3.699f, 46.083f, -23.725f, 43.456f);
    scalePath(first, density);
    scalePath(second, density);
    joinedA = new JoinedPath(first, second);

    first = new Path();
    first.moveTo(64.959f, 20f);
    first.rCubicTo(4.457f, 16.75f, 1.512f, 37.982f, -22.557f, 42.699f);
    second = new Path();
    second.moveTo(42.402f, 62.699f);
    second.cubicTo(18.333f, 67.418f, 8.807f, 45.646f, 8.807f, 32.823f);
    scalePath(first, density);
    scalePath(second, density);
    joinedB = new JoinedPath(first, second);
    topLine = new BridgingLine(joinedA, joinedB);

    // Middle
    first = new Path();
    first.moveTo(5.042f, 35f);
    first.cubicTo(5.042f, 20.333f, 18.625f, 6.791f, 35f, 6.791f);
    second = new Path();
    second.moveTo(35f, 6.791f);
    second.rCubicTo(16.083f, 0f, 26.853f, 16.702f, 26.853f, 28.209f);
    scalePath(first, density);
    scalePath(second, density);
    joinedA = new JoinedPath(first, second);

    first = new Path();
    first.moveTo(64.959f, 35f);
    first.rCubicTo(0f, 10.926f, -8.709f, 26.416f, -29.958f, 26.416f);
    second = new Path();
    second.moveTo(35f, 61.416f);
    second.rCubicTo(-7.5f, 0f, -23.946f, -8.211f, -23.946f, -26.416f);
    scalePath(first, density);
    scalePath(second, density);
    joinedB = new JoinedPath(first, second);
    middleLine = new BridgingLine(joinedA, joinedB);

    // Bottom
    first = new Path();
    first.moveTo(5.042f, 50f);
    first.cubicTo(2.5f, 43.312f, 0.013f, 26.546f, 9.475f, 17.346f);
    second = new Path();
    second.moveTo(9.475f, 17.346f);
    second.rCubicTo(9.462f, -9.2f, 24.188f, -10.353f, 27.326f, -8.245f);
    scalePath(first, density);
    scalePath(second, density);
    joinedA = new JoinedPath(first, second);

    first = new Path();
    first.moveTo(64.959f, 50f);
    first.rCubicTo(-7.021f, 10.08f, -20.584f, 19.699f, -37.361f, 12.74f);
    second = new Path();
    second.moveTo(27.598f, 62.699f);
    second.rCubicTo(-15.723f, -6.521f, -18.8f, -23.543f, -18.8f, -25.642f);
    scalePath(first, density);
    scalePath(second, density);
    joinedB = new JoinedPath(first, second);
    bottomLine = new BridgingLine(joinedA, joinedB);
  }

  @Override public int getIntrinsicHeight() {
    return bounds.height();
  }

  @Override public int getIntrinsicWidth() {
    return bounds.width();
  }

  @Override public void draw(Canvas canvas) {
    if (flip) {
      canvas.save();
      canvas.scale(1f, -1f, getIntrinsicWidth() / 2, getIntrinsicHeight() / 2);
    }

    topLine.draw(canvas);
    middleLine.draw(canvas);
    bottomLine.draw(canvas);

    if (flip) canvas.restore();
  }

  @Override public void setAlpha(int alpha) {
    linePaint.setAlpha(alpha);
    invalidateSelf();
  }

  @Override public void setColorFilter(ColorFilter cf) {
    linePaint.setColorFilter(cf);
    invalidateSelf();
  }

  @Override public int getOpacity() {
    return TRANSLUCENT;
  }

  public void setStrokeColor(int color) {
    linePaint.setColor(color);
    invalidateSelf();
  }

  /**
   * Sets the rotation of this drawable based on {@code parameter} between 0 and 1. Usually driven
   * via {@link DrawerListener#onDrawerSlide(View, float)}'s {@code slideOffset} parameter.
   */
  public void setParameter(float parameter) {
    if (parameter > 1 || parameter < 0) {
      throw new IllegalArgumentException("Value must be between 1 and zero inclusive!");
    }
    this.parameter = parameter;
    invalidateSelf();
  }

  /**
   * When false, rotates from 3 o'clock to 9 o'clock between a drawer icon and a back arrow.
   * When true, rotates from 9 o'clock to 3 o'clock between a back arrow and a drawer icon.
   */
  public void setFlip(boolean flip) {
    this.flip = flip;
    invalidateSelf();
  }

  /**
   * Scales the paths to the given screen density. If the density matches the
   * {@link DrawerArrowDrawable#PATH_GEN_DENSITY}, no scaling needs to be done.
   */
  private static void scalePath(Path path, float density) {
    if (density == PATH_GEN_DENSITY) return;
    Matrix scaleMatrix = new Matrix();
    scaleMatrix.setScale(density / PATH_GEN_DENSITY, density / PATH_GEN_DENSITY, 0, 0);
    path.transform(scaleMatrix);
  }
}