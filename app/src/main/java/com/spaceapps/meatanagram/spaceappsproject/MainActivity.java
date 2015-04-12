package com.spaceapps.meatanagram.spaceappsproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import com.parse.ParseFacebookUtils;
import com.parse.ui.ParseLoginActivity;
import com.parse.ui.ParseLoginBuilder;
import android.app.Fragment;
import android.content.pm.ActivityInfo;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import com.nvanbenschoten.motion.ParallaxImageView;

public class MainActivity extends ActionBarActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(ParseFacebookUtils.getSession() == null)
        {
            ParseLoginBuilder builder = new ParseLoginBuilder(this);

            // builder.setAppLogo(R.drawable.app_logo);
            builder.setFacebookLoginEnabled(true);
            builder.setParseLoginEnabled(false);

            Intent loginIntent = builder.build();
            loginIntent.setClass(this, ParseLoginActivity.class);

            startActivityForResult(loginIntent, R.integer.login_request_code);
        }

        ParallaxFragment pf1 = new ParallaxFragment();
        pf1.mCurrentImage = 1;
        ParallaxFragment pf2 = new ParallaxFragment();
        pf2.mCurrentImage = 2;
        getFragmentManager().beginTransaction()
                .add(R.id.frag_holder, pf1)
                .commit();
        getFragmentManager().beginTransaction()
                .add(R.id.frag_holder, pf2)
                .commit();
        this.getSupportFragmentManager().beginTransaction().replace(R.id.frag_holder, new MainFragment()).commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_new_post)
        {
            addNewPost();
        }

        return super.onOptionsItemSelected(item);
    }

    protected void openPost(int position)
    {
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        PostFragment frag = new PostFragment();
        frag.setArguments(bundle);
        FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frag_holder, frag);
        ft.addToBackStack(null);
        ft.commit();
    }

    protected void showTiles()
    {
        this.getSupportFragmentManager().beginTransaction().replace(R.id.frag_holder, new MainFragment()).commit();
    }

    protected void addNewPost()
    {
        NewPostFragment frag = new NewPostFragment();
        FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frag_holder, frag);
        ft.addToBackStack(null);
        ft.commit();
    }

    public static class ParallaxFragment extends Fragment {

        ParallaxImageView mBackground;
        private ParallaxImageView mStars;

        private int mCurrentImage;
        private boolean mParallaxSet = true;
        private boolean mPortraitLock = true;

        public ParallaxFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_parallax, container, false);
            if (rootView == null) return null;

            mBackground = (ParallaxImageView) rootView.findViewById(android.R.id.background);

            setCurrentImage();

            return rootView;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            // Adjust the Parallax forward tilt adjustment
            mBackground.setForwardTiltOffset(.35f);
            if (mCurrentImage==1) mBackground.setParallaxIntensity(1.1f);
            if (mCurrentImage==2) mBackground.setParallaxIntensity(1.3f);
            if (mCurrentImage==3) mBackground.setParallaxIntensity(1.4f);


        }

        @Override
        public void onResume() {
            super.onResume();

            if (mParallaxSet)
                mBackground.registerSensorManager();
        }

        @Override
        public void onPause() {
            mBackground.unregisterSensorManager();
            super.onPause();
        }

        /*
        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            inflater.inflate(R.menu.parallax, menu);

            // Add parallax toggle
            final Switch mParallaxToggle = new Switch(getActivity());
            mParallaxToggle.setPadding(0, 0, (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics()), 0);
            mParallaxToggle.setChecked(mParallaxSet);
            mParallaxToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        mBackground.registerSensorManager();
                    } else {
                        mBackground.unregisterSensorManager();
                    }

                    mParallaxSet = isChecked;
                }
            });
            MenuItem switchItem = menu.findItem(R.id.action_parallax);
            if (switchItem != null)
                switchItem.setActionView(mParallaxToggle);

            // Set lock/ unlock orientation text
            if (mPortraitLock) {
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                MenuItem orientationItem = menu.findItem(R.id.action_portrait);
                if (orientationItem != null){}
                //orientationItem.setTitle(R.string.action_unlock_portrait);
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_switch:
                    mCurrentImage ++;
                    mCurrentImage %= 3;
                    setCurrentImage();
                    return true;

                case R.id.action_portrait:
                    if (mPortraitLock) {
                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                        //item.setTitle(getString(R.string.action_lock_portrait));
                    } else {
                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        //item.setTitle(getString(R.string.action_unlock_portrait));
                    }

                    mPortraitLock = !mPortraitLock;
                    return true;

                default:
                    return super.onOptionsItemSelected(item);
            }
        }
        */

        private void setCurrentImage() {

            if (mCurrentImage ==1) mBackground.setImageResource(R.drawable.back1);
            if (mCurrentImage ==2) mBackground.setImageResource(R.drawable.back2);
            //if (mCurrentImage ==3) mBackground.setImageResource(R.drawable.back3);

        }

    }
}
