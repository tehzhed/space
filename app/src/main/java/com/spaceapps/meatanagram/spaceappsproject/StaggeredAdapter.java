package com.spaceapps.meatanagram.spaceappsproject;

import android.app.FragmentTransaction;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.spaceapps.meatanagram.spaceappsproject.utils.ImageDownloader;
import com.spaceapps.meatanagram.spaceappsproject.utils.MapNetworkTask;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class StaggeredAdapter extends ArrayAdapter<String> {

    private ImageLoader mLoader;
    protected GoogleMap gMap;

    private enum ImageMode {PIC, MAP};

    private ImageMode currentIM = ImageMode.PIC;

    public StaggeredAdapter(Context context, int textViewResourceId,
                            String[] objects) {
        super(context, textViewResourceId, objects);
        mLoader = new ImageLoader(context);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

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
                            finalHolder.imageView.setImageBitmap(new MapNetworkTask().execute(150, 400).get());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                        break;
                    case MAP:
                        mLoader.DisplayImage(getItem(position), finalHolder.imageView);
                        break;
                    default:
                }

                return true;
            }
        });

        mLoader.DisplayImage(getItem(position), holder.imageView);

        return convertView;
    }

    static class ViewHolder {
        ScaleImageView imageView;
    }
}