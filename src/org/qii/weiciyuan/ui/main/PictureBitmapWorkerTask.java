package org.qii.weiciyuan.ui.main;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.view.Display;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.file.FileLocationMethod;
import org.qii.weiciyuan.support.imagetool.ImageTool;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.lib.PictureBitmapDrawable;

import java.lang.ref.WeakReference;
import java.util.Map;

/**
 * User: Jiang Qi
 * Date: 12-8-3
 */
public class PictureBitmapWorkerTask extends MyAsyncTask<String, Void, Bitmap> {


    private LruCache<String, Bitmap> lruCache;
    private String data = "";
    private final WeakReference<ImageView> view;

    private Map<String, PictureBitmapWorkerTask> taskMap;
    private int position;

    private Activity activity;

    private FileLocationMethod method;

    int reqWidth;
    int reqHeight;

    public String getUrl() {
        return data;
    }

    public PictureBitmapWorkerTask(LruCache<String, Bitmap> lruCache,
                                   Map<String, PictureBitmapWorkerTask> taskMap,
                                   ImageView view, String url, int position, Activity activity, FileLocationMethod method) {

        this.lruCache = lruCache;
        this.taskMap = taskMap;
        this.view = new WeakReference<ImageView>(view);
        this.data = url;
        this.position = position;
        this.activity = activity;
        this.method = method;

    }


    @Override
    protected Bitmap doInBackground(String... url) {


        if (!isCancelled()) {
            switch (method) {

                case picture_thumbnail:
                    return ImageTool.getThumbnailPictureWithRoundedCorner(data);

                case picture_bmiddle:
                    DisplayMetrics metrics = new DisplayMetrics();
                    Display display = activity.getWindowManager().getDefaultDisplay();
                    display.getMetrics(metrics);
                    float reSize = activity.getResources().getDisplayMetrics().density;
                    //because height is 80dp
                    int height = activity.getResources().getDimensionPixelSize(R.dimen.timeline_big_avatar_height);
                    //8 is  layout padding
                    int width = (int) (metrics.widthPixels - (8 + 8) * reSize);

                    return ImageTool.getMiddlePictureInTimeLine(data, width, height, null);

            }
        }
        return null;
    }

    @Override
    protected void onCancelled(Bitmap bitmap) {

        if (taskMap != null && taskMap.get(data) != null) {
            taskMap.remove(data);
        }

        super.onCancelled(bitmap);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {

        if (bitmap != null) {

            if (view != null && view.get() != null) {
                ImageView imageView = view.get();

                PictureBitmapWorkerTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
                if (this == bitmapDownloaderTask) {
                    switch (method) {
                        case picture_thumbnail:
                            playImageViewAnimation(imageView, bitmap);
                            break;
                        case picture_bmiddle:
                            playImageViewAnimation(imageView, bitmap);
                            break;
                    }

                    lruCache.put(data, bitmap);

                }
            }
        }

        if (taskMap.get(data) != null) {
            taskMap.remove(data);
        }
    }

    private static PictureBitmapWorkerTask getBitmapDownloaderTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof PictureBitmapDrawable) {
                PictureBitmapDrawable downloadedDrawable = (PictureBitmapDrawable) drawable;
                return downloadedDrawable.getBitmapDownloaderTask();
            }
        }
        return null;
    }

    private void playImageViewAnimation(final ImageView view, final Bitmap bitmap) {
        final Animation anim_out = AnimationUtils.loadAnimation(activity, R.anim.timeline_pic_fade_out);
        final Animation anim_in = AnimationUtils.loadAnimation(activity, R.anim.timeline_pic_fade_in);

        anim_out.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                view.setImageBitmap(bitmap);
                view.setTag(getUrl());
                anim_in.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                    }
                });

                view.startAnimation(anim_in);
            }
        });

        view.startAnimation(anim_out);
    }
}
