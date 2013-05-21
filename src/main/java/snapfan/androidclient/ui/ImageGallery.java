/*
 Copyright (c) 2012 Roman Truba

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial
 portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH
 THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package snapfan.androidclient.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;
import ru.truba.touchgallery.GalleryWidget.BasePagerAdapter.OnItemChangeListener;
import ru.truba.touchgallery.GalleryWidget.GalleryViewPager;
import ru.truba.touchgallery.GalleryWidget.UrlPagerAdapter;
import ru.truba.touchgallery.R;

import java.util.Arrays;

public class ImageGallery extends Activity {
    private GalleryViewPager viewPager;
    private String[] imageUrls;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.image_gallery);

        imageUrls = getIntent().getStringArrayExtra("image_urls");

        UrlPagerAdapter pagerAdapter = new UrlPagerAdapter(this, Arrays.asList(imageUrls));
        pagerAdapter.setOnItemChangeListener(new OnItemChangeListener() {
            @Override
            public void onItemChange(int currentPosition) {
                Toast.makeText(ImageGallery.this, (currentPosition + 1) + "/" + imageUrls.length, Toast.LENGTH_SHORT).show();
            }
        });

        viewPager = (GalleryViewPager) findViewById(R.id.viewer);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(pagerAdapter);
    }
}