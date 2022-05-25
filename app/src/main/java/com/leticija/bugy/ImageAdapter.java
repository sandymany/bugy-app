package com.leticija.bugy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.github.chrisbanes.photoview.PhotoView;
import com.leticija.bugy.concurrent.TaskQueue;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends PagerAdapter {
    Context context;
    JSONArray imagesJSONArray;
    List<Bitmap> imagesList = new ArrayList<>();
    TextView countTextView;
    private View mCurrentView;

    public ImageAdapter(Context context, JSONArray imagesJSONArray, TextView countTextView) throws JSONException, IOException {
        this.context = context;
        this.countTextView = countTextView;
        this.imagesJSONArray = imagesJSONArray;
        fillBitmapArray();
    }

    @Override
    public int getCount() {
        return imagesList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        PhotoView photoView = null;
        photoView = new PhotoView(context);
        photoView.setImageBitmap(imagesList.get(position));
        photoView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        container.addView(photoView);
        //photoView.setTag(position);
        return photoView;

    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((ImageView) object);
    }

    private void fillBitmapArray() throws JSONException, IOException {
        for (int i = 0; i < imagesJSONArray.length(); i++) {
            JSONObject image = (JSONObject) imagesJSONArray.get(i);
            String imageName = image.get("image_name").toString();

            URL url = new URL(context.getString(R.string.base_ip)+"/home/loadImage?imageName="+imageName);
            Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            imagesList.add(bmp);
        }
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        mCurrentView = (View)object;
    }

    public static void setPageCounter (ViewPager viewPager, TextView countTextView) {
        countTextView.setText("1/"+viewPager.getAdapter().getCount());
        PageListener pageListener = new PageListener(countTextView,viewPager);
        viewPager.setOnPageChangeListener(pageListener);
    }
}

class PageListener extends ViewPager.SimpleOnPageChangeListener {

    String TAG = "message";
    int currentPage;
    TextView countTextView;
    ViewPager viewPager;

    public PageListener(TextView countTextView, ViewPager viewPager) {
        this.countTextView = countTextView;
        this.viewPager = viewPager;
    }

    public void onPageSelected(final int position) {
        Log.i(TAG, "page selected " + position);
        System.out.println();
        currentPage = position;

        TaskQueue.prepare().guiTask(new Runnable() {
            @Override
            public void run() {
                countTextView.bringToFront();
                countTextView.setText((position+1)+"/"+viewPager.getAdapter().getCount());
            }
        }).subscribeMe();
    }
}
