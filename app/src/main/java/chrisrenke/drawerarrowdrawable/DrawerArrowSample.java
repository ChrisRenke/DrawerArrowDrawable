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
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.ImageView;

import static android.view.Gravity.START;

public class DrawerArrowSample extends Activity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.home_view);

    final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    ImageView imageView = (ImageView) findViewById(R.id.drawer_indicator);

    final DrawerArrowDrawable drawable = new DrawerArrowDrawable(getResources());
    imageView.setImageDrawable(drawable);

    drawer.setDrawerListener(new DrawerLayout.SimpleDrawerListener() {
      @Override public void onDrawerSlide(View drawerView, float slideOffset) {
        drawable.setParameter(slideOffset);
      }

      @Override public void onDrawerOpened(View drawerView) {
        drawable.setFlip(true);
      }

      @Override public void onDrawerClosed(View drawerView) {
        drawable.setFlip(false);
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
  }
}
