package com.spaceapps.meatanagram.spaceappsproject;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Simone on 4/11/2015.
 */
public class PostFragment extends Fragment implements View.OnKeyListener {

    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        view = inflater.inflate(R.layout.post_layout, container, false);
        TextView tv = (TextView)view.findViewById(R.id.textView1);
        int position = getArguments().getInt("position");
        tv.setText("Post at position " + position + ".");

        return view;
    }

    @Override
    public boolean onKey( View v, int keyCode, KeyEvent event )
    {
        if( keyCode == KeyEvent.KEYCODE_BACK )
        {
            ((MainActivity)getActivity()).showTiles();

            return true;
        }

        return false;
    }
}
