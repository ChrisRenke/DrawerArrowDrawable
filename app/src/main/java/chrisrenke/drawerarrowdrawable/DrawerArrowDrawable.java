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
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import static android.graphics.Color.BLACK;
import static android.graphics.Paint.ANTI_ALIAS_FLAG;
import static android.graphics.Paint.Cap.BUTT;
import static android.graphics.Paint.SUBPIXEL_TEXT_FLAG;
import static android.graphics.Paint.Style.STROKE;
import static android.graphics.PixelFormat.TRANSLUCENT;
import static android.support.v4.widget.DrawerLayout.*;

/** A drawable that rotates between a drawer icon and a back arrow based on parameter. */
public class DrawerArrowDrawable extends Drawable {

  private final static float STROKE_WIDTH_DP = 2;
  private final static float DIMEN_DP = 23.5f;

  private final static Path topA = new Path();
  private final static Path topB = new Path();
  private final static Path middleA = new Path();
  private final static Path middleB = new Path();
  private final static Path bottomA = new Path();
  private final static Path bottomB = new Path();

  static {
    topA.moveTo(36.807f, 60.69f);
    topA.rCubicTo(20.184f, 1.858f, 34.812f, -20.915f, 23.086f, -43.732f);
    topA.cubicTo(48.648f, -4.917f, 15.125f, -0.167f, 5.041f, 20f);
    topB.moveTo(8.92f, 32.936f);
    topB.rCubicTo(0f, 17.54f, 13.887f, 30.381f, 24.716f, 30.381f);
    topB.rCubicTo(35.824f, 0f, 35.238f, -32.419f, 31.322f, -43.317f);

    middleA.moveTo(62f, 35f);
    middleA.rCubicTo(0f, -7.833f, -8.25f, -28.209f, -27f, -28.209f);
    middleA.cubicTo(16.25f, 6.791f, 5.041f, 23.25f, 5.041f, 35f);
    middleB.moveTo(11.054f, 35f);
    middleB.rCubicTo(0f, 5f, 3.113f, 26.416f, 23.946f, 26.416f);
    middleB.rCubicTo(20.833f, 0f, 29.959f, -14.583f, 29.959f, -26.416f);

    bottomA.moveTo(8.906f, 37.059f);
    bottomA.rCubicTo(0f, 14.085f, 15.535f, 27.915f, 31.061f, 27.915f);
    bottomA.rCubicTo(15.523f, 0f, 25.1f, -15.015f, 25.1f, -15.015f);
    bottomB.moveTo(36.801f, 9.101f);
    bottomB.cubicTo(8.083f, 4.5f, -1.584f, 29.583f, 5.041f, 50.212f);
  }

  private final float coordsA[] = { 0f, 0f };
  private final float coordsB[] = { 0f, 0f };
  private final Rect dimens;
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

  private boolean flip;
  private float parameter;

  public DrawerArrowDrawable(Resources resources) {
    float density = resources.getDisplayMetrics().density;

    this.linePaint = new Paint(SUBPIXEL_TEXT_FLAG | ANTI_ALIAS_FLAG);
    linePaint.setStrokeCap(BUTT);
    linePaint.setColor(BLACK);
    linePaint.setStyle(STROKE);
    linePaint.setStrokeWidth(STROKE_WIDTH_DP * density);

    int dimen = (int) (DIMEN_DP * density);
    dimens = new Rect(0, 0, dimen, dimen);

    // Top Line - 9 to 3 rotation
    measureTopA = new PathMeasure(topA, false);
    measureTopB = new PathMeasure(topB, false);
    lengthTopA = measureTopA.getLength();
    lengthTopB = measureTopB.getLength();

    // Middle Line - 9 to 3 rotation
    measureMiddleA = new PathMeasure(middleA, false);
    measureMiddleB = new PathMeasure(middleB, false);
    lengthMiddleA = measureMiddleA.getLength();
    lengthMiddleB = measureMiddleB.getLength();

    // Bottom Line - 9 to 3 rotation
    measureBottomA = new PathMeasure(bottomA, false);
    measureBottomB = new PathMeasure(bottomB, false);
    lengthBottomA = measureBottomA.getLength();
    lengthBottomB = measureBottomB.getLength();
  }

  @Override public int getIntrinsicHeight() {
    return dimens.height();
  }

  @Override public int getIntrinsicWidth() {
    return dimens.width();
  }

  @Override public void draw(Canvas canvas) {
    if (flip) {
      canvas.save();
      canvas.scale(1f, -1f, getIntrinsicWidth() / 2, getIntrinsicHeight() / 2);
    }

    measureMiddleA.getPosTan(lengthMiddleA * (1 - parameter), coordsA, null);
    measureMiddleB.getPosTan(lengthMiddleB * (1 - parameter), coordsB, null);
    canvas.drawLine(coordsA[0], coordsA[1], coordsB[0], coordsB[1], linePaint);

    measureTopA.getPosTan(lengthTopA * (1 - parameter), coordsA, null);
    measureTopB.getPosTan(lengthTopB * (1 - parameter), coordsB, null);
    canvas.drawLine(coordsA[0], coordsA[1], coordsB[0], coordsB[1], linePaint);

    measureBottomA.getPosTan(lengthBottomA * (1 - parameter), coordsA, null);
    measureBottomB.getPosTan(lengthBottomB * (1 - parameter), coordsB, null);
    canvas.drawLine(coordsA[0], coordsA[1], coordsB[0], coordsB[1], linePaint);

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

  /**
   * Sets the rotation of this drawable based on {@code parameter} between 0 and 1. Usually driven
   * via {@link DrawerListener#onDrawerSlide(View, float)}'s {@code slideOffset} parameter.
   */
  public void setParameter(float parameter) {
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
}