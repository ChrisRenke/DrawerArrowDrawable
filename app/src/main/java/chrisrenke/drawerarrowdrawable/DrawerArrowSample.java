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

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import static android.view.Gravity.START;

public class DrawerArrowSample extends Activity {

  private DrawerArrowDrawable drawerArrowDrawable;
  private float offset;
  private boolean flipped;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.home_view);

    final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    final ImageView imageView = (ImageView) findViewById(R.id.drawer_indicator);
    final Resources resources = getResources();

    drawerArrowDrawable = new DrawerArrowDrawable(resources);
    imageView.setImageDrawable(drawerArrowDrawable);

    drawer.setDrawerListener(new DrawerLayout.SimpleDrawerListener() {
      @Override public void onDrawerSlide(View drawerView, float slideOffset) {
        offset = slideOffset;

        // Sometimes slideOffset ends up so close to but not quite 1 or 0.
        if (slideOffset >= .995) {
          flipped = true;
          drawerArrowDrawable.setFlip(flipped);
        } else if (slideOffset <= .005) {
          flipped = false;
          drawerArrowDrawable.setFlip(flipped);
        }

        drawerArrowDrawable.setParameter(offset);
      }
    });

    imageView.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        if (drawer.isDrawerVisible(START)) {
          drawer.closeDrawer(START);
        } else {
          drawer.openDrawer(START);
        }
      }
    });

    final TextView styleButton = (TextView) findViewById(R.id.indicator_style);
    styleButton.setOnClickListener(new View.OnClickListener() {
      boolean rounded = false;

      @Override public void onClick(View v) {
        styleButton.setText(rounded //
            ? resources.getString(R.string.rounded) //
            : resources.getString(R.string.squared));

        rounded = !rounded;

        drawerArrowDrawable = new DrawerArrowDrawable(resources, rounded);
        drawerArrowDrawable.setParameter(offset);
        drawerArrowDrawable.setFlip(flipped);

        imageView.setImageDrawable(drawerArrowDrawable);

      }
    });
  }
}
