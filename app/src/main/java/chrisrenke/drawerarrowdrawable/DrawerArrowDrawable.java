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

  /** Paths were generated at a 3px/dp density; this is the scale factor for different densities. */
  private final static float PATH_GEN_DENSITY = 3;


  /** Paths were generated with at this size for {@link DrawerArrowDrawable#PATH_GEN_DENSITY}. */
  private final static float DIMEN_DP = 23.5f;

  /**
   * Paths were generated targeting this stroke width to form the arrowhead properly, modification
   * may cause the arrow to not for nicely.
   */
  private final static float STROKE_WIDTH_DP = 2;

  private final Rect bounds;
  private final float halfStrokeWidthPixel;
  private final float lengthBottomB;
  private final float lengthBottomA;
  private final float lengthMiddleA;
  private final float lengthMiddleB;
  private final float lengthTopA;
  private final float lengthTopB;
  private final Paint linePaint;
  private final PathMeasure measureBottomA;
  private final PathMeasure measureBottomB;
  private final PathMeasure measureMiddleA;
  private final PathMeasure measureMiddleB;
  private final PathMeasure measureTopA;
  private final PathMeasure measureTopB;
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

    // Top Line - 9 to 3 rotation
    Path topA = new Path();
    topA.moveTo(36.807f, 60.69f);
    topA.rCubicTo(20.184f, 1.858f, 34.812f, -20.915f, 23.086f, -43.732f);
    topA.cubicTo(48.648f, -4.917f, 15.125f, -0.167f, 5.041f, 20f);
    Path topB = new Path();
    topB.moveTo(8.92f, 32.936f);
    topB.rCubicTo(0f, 17.54f, 13.887f, 30.381f, 24.716f, 30.381f);
    topB.rCubicTo(35.824f, 0f, 35.238f, -32.419f, 31.322f, -43.317f);
    scalePath(topA, density);
    scalePath(topB, density);
    measureTopA = new PathMeasure(topA, false);
    measureTopB = new PathMeasure(topB, false);
    lengthTopA = measureTopA.getLength();
    lengthTopB = measureTopB.getLength();

    // Middle Line - 9 to 3 rotation
    Path middleA = new Path();
    middleA.moveTo(62f, 35f);
    middleA.rCubicTo(0f, -7.833f, -8.25f, -28.209f, -27f, -28.209f);
    middleA.cubicTo(16.25f, 6.791f, 5.041f, 23.25f, 5.041f, 35f);
    Path middleB = new Path();
    middleB.moveTo(11.054f, 35f);
    middleB.rCubicTo(0f, 5f, 3.113f, 26.416f, 23.946f, 26.416f);
    middleB.rCubicTo(20.833f, 0f, 29.959f, -14.583f, 29.959f, -26.416f);
    scalePath(middleA, density);
    scalePath(middleB, density);
    measureMiddleA = new PathMeasure(middleA, false);
    measureMiddleB = new PathMeasure(middleB, false);
    lengthMiddleA = measureMiddleA.getLength();
    lengthMiddleB = measureMiddleB.getLength();

    // Bottom Line - 9 to 3 rotation
    Path bottomA = new Path();
    bottomA.moveTo(36.801f, 9.101f);
    bottomA.cubicTo(8.083f, 4.5f, -1.584f, 29.583f, 5.041f, 50.212f);
    Path bottomB = new Path();
    bottomB.moveTo(8.906f, 37.059f);
    bottomB.rCubicTo(0f, 14.085f, 15.535f, 27.915f, 31.061f, 27.915f);
    bottomB.rCubicTo(15.523f, 0f, 25.1f, -15.015f, 25.1f, -15.015f);
    scalePath(bottomA, density);
    scalePath(bottomB, density);
    measureBottomA = new PathMeasure(bottomA, false);
    measureBottomB = new PathMeasure(bottomB, false);
    lengthBottomA = measureBottomA.getLength();
    lengthBottomB = measureBottomB.getLength();
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

    drawPathSet(measureTopA, measureTopB, lengthTopA, lengthTopB, canvas);
    drawPathSet(measureMiddleA, measureMiddleB, lengthMiddleA, lengthMiddleB, canvas);
    drawPathSet(measureBottomA, measureBottomB, lengthBottomA, lengthBottomB, canvas);

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
   * Draw a line between the points defined on the paths backing {@code measureA} and
   * {@code measureB} at the current parameter.
   */
  private void drawPathSet(PathMeasure measureA, PathMeasure measureB, float lengthA, float lengthB,
      Canvas canvas) {
    measureA.getPosTan(lengthA * (1 - parameter), coordsA, null);
    measureB.getPosTan(lengthB * (1 - parameter), coordsB, null);
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

  /**
   * Scales the paths to the given screen density. If the density matches the
   * {@link DrawerArrowDrawable#PATH_GEN_DENSITY}, no scaling needs to be done.
   */
  private void scalePath(Path path, float density) {
    if (density == PATH_GEN_DENSITY) return;
    Matrix scaleMatrix = new Matrix();
    scaleMatrix.setScale(density / PATH_GEN_DENSITY, density / PATH_GEN_DENSITY, 0, 0);
    path.transform(scaleMatrix);
  }
}