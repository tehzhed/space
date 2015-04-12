package com.spaceapps.meatanagram.spaceappsproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.spaceapps.meatanagram.spaceappsproject.utils.ImageDownloader;
import com.spaceapps.meatanagram.spaceappsproject.utils.MapNetworkTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class StaggeredAdapter extends ArrayAdapter<Post> {

    private ImageLoader mLoader;

    private enum ImageMode {PIC, MAP};

    private ImageMode currentIM = ImageMode.PIC;

    private HashMap<Integer, Bitmap> gMapHash;

    public StaggeredAdapter(Context context, int textViewResourceId,
                            Post[] objects) {
        super(context, textViewResourceId, objects);
        mLoader = new ImageLoader(context);
        gMapHash = new HashMap<>();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if(!gMapHash.containsKey(position)){
            try {
                gMapHash.put(position, new MapNetworkTask().execute(getItem(position).getGeoPoint().getLatitude(),
                        getItem(position).getGeoPoint().getLongitude()).get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater layoutInflator = LayoutInflater.from(getContext());
            convertView = layoutInflator.inflate(R.layout.row_staggered_demo, null);
            holder = new ViewHolder();
            holder.imageView = (ScaleImageView) convertView .findViewById(R.id.imageView1);
            convertView.setTag(holder);
        }

        holder = (ViewHolder) convertView.getTag();

        final ViewHolder finalHolder = holder;
        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                switch (currentIM) {
                    case PIC:
                        try {
                            if(gMapHash.containsKey(position)){
                                finalHolder.imageView.setImageBitmap(gMapHash.get(position));
                            }else {
                                Bitmap gMapPic = new MapNetworkTask().execute(getItem(position).getGeoPoint().getLatitude(),
                                        getItem(position).getGeoPoint().getLongitude()).get();
                                finalHolder.imageView.setImageBitmap(gMapPic);
                                gMapHash.put(position, gMapPic);
                            }
                            currentIM = ImageMode.MAP;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                        break;
                    case MAP:
                        try {
                            mLoader.DisplayImage(ImageDownloader.saveImageDefaultToday(getItem(position).getGeoPoint().getLatitude(),
                                    getItem(position).getGeoPoint().getLongitude()), finalHolder.imageView);
                            currentIM = ImageMode.PIC;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                }

                return true;
            }
        });

        try {
            mLoader.DisplayImage(ImageDownloader.saveImageDefaultInDate(getItem(position).getGeoPoint().getLatitude(),
                    getItem(position).getGeoPoint().getLongitude(), getItem(position).getDate()), finalHolder.imageView);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return convertView;
    }

    static class ViewHolder {
        ScaleImageView imageView;
    }
}