package com.spaceapps.meatanagram.spaceappsproject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import java.io.File;
import java.io.IOException;

/**
 * Created by Simone on 4/11/2015.
 */
public class NewPostFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener, View.OnCreateContextMenuListener {

    private static final String TAG = "NewPostFragment";

    public static final String PICTURE_FORMAT = ".jpg";
    public static final String AUDIO_FORMAT = ".3gp";
    public static final String VIDEO_FORMAT = ".mp4";

    private static final int PIC_CODE = 0;
    private static final int VIDEO_CODE = 1;
    private static final int PHONE_MEDIA_CODE = 2;
    private static final int PHONE_IMAGE_REQUEST_CODE = 3;
    private static final int PHONE_VIDEO_REQUEST_CODE = 4;
    private static final int PHONE_MEDIA_GROUP = 5;
    private static final int PHONE_IMAGE = 6;
    private static final int PHONE_VIDEO = 7;
    private static final int PIC_CAPTURE_REQUEST_CODE = 8;
    private static final int VID_SHOOT_REQUEST_CODE = 9;
    private static final int LOGIN_REQUEST_CODE = 10;

    private int requestedPhoneMediaType;

    private View view;

    private ImageButton picButton;
    private ImageButton vidButton;
    private ImageButton phoneButton;

    private boolean isPicTaken = false;
    private boolean isVideoShoot = false;
    private boolean isPhoneMediaSelected = false;

    private File pic;
    private File video;
    private File phoneMedia;

    private TextView textView;
    private MenuItem confirmButton;
    private MenuItem addButton;

    private File imageFile;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        addButton = menu.findItem(R.id.action_new_post);
        inflater.inflate(R.menu.menu_share, menu);
        confirmButton = menu.findItem(R.id.action_confirm_post);

        addButton.setVisible(false);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_confirm_post) share();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause()
    {
        super.onPause();

        addButton.setVisible(true);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if(addButton != null) addButton.setVisible(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        view = inflater.inflate(R.layout.share_layout, container, false);

        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    return true;
                }
                else
                {
                    return false;
                }
            }
        });



        this.picButton = (ImageButton) view.findViewById(R.id.pic_button);
        this.vidButton = (ImageButton) view.findViewById(R.id.vid_button);
        this.phoneButton = (ImageButton) view.findViewById(R.id.phone_button);

        this.setPicture(this.getPicPath());
        this.setVideo(this.getVideoPath());
        this.setPhoneMedia(this.getPhoneMediaPath());

        this.picButton.setClickable(true);
        this.vidButton.setClickable(true);
        this.phoneButton.setClickable(true);

        this.picButton.setLongClickable(true);
        this.vidButton.setLongClickable(true);
        this.phoneButton.setLongClickable(true);

        this.picButton.setOnLongClickListener(this);
        this.vidButton.setOnLongClickListener(this);
        this.phoneButton.setOnLongClickListener(this);

        this.phoneButton.setOnClickListener(this);
        this.picButton.setOnClickListener(this);
        this.vidButton.setOnClickListener(this);

        this.textView = (TextView) view.findViewById(R.id.share_text_field);
        this.textView.setGravity(Gravity.CENTER);

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    private void handleShareImage(Intent intent) {
        Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            File imageFile = new File(Utils.getImageRealPathFromURI(getActivity(), imageUri));
            if (imageFile.length() >= Post.MAX_FILE_SIZE_BYTES) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Cannot share this picture :(\nPlease, choose a smaller one").setNegativeButton("No", null).show();
            } else {
                this.setPhoneMedia(imageFile.getAbsolutePath());
                this.requestedPhoneMediaType = PHONE_IMAGE_REQUEST_CODE;
                this.changeAlphaBasedOnSelection(PHONE_MEDIA_CODE);
            }

        } else {
            Toast.makeText(getActivity(), "Cannot find the requested file", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleShareVideo(Intent intent) {
        Uri videoUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (videoUri != null) {
            File videoFile = new File(Utils.getVideoRealPathFromURI(getActivity(), videoUri));
            if (videoFile.length() >= Post.MAX_FILE_SIZE_BYTES) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Cannot share this video :(\nPlease, choose a smaller one").setNegativeButton("No", null).show();
            } else {
                this.setPhoneMedia(videoFile.getAbsolutePath());
                this.requestedPhoneMediaType = PHONE_VIDEO_REQUEST_CODE;
                this.changeAlphaBasedOnSelection(PHONE_MEDIA_CODE);
            }
        } else {
            Toast.makeText(getActivity(), "Cannot find the requested file", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleShareText(Intent intent) {
        String text = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (text != null) {
            this.textView.setText(text);

        }

    }

    public void setVideo(String video)
    {
        this.video = null;
        if (video != null)
        {
            File f = new File(video);
            this.video = f.canRead() ? f : null;
        }

        this.isVideoShoot = this.video != null;

        if (this.vidButton != null)
        {
            int res = R.mipmap.videocam;
            if (this.isVideoShoot)
            {
                res = R.mipmap.tick;
            }
            this.vidButton.setImageDrawable(getResources().getDrawable(res));
        }
    }

    public void setPicture(String pic)
    {
        this.pic = null;
        if (pic != null)
        {
            File f = new File(pic);
            this.pic = f.canRead() ? f : null;
        }
        this.isPicTaken = this.pic != null;

        if (this.picButton != null)
        {
            int res = R.mipmap.camera;
            if (this.isPicTaken)
            {
                res = R.mipmap.tick;
            }

            this.picButton.setImageDrawable(getResources().getDrawable(res));
        }
    }

    private void setPhoneMedia(String phoneMediaPath) {
        this.phoneMedia = null;
        if (phoneMediaPath != null) {
            File f = new File(phoneMediaPath);
            this.phoneMedia = f.canRead() ? f : null;
        }
        this.isPhoneMediaSelected = this.phoneMedia != null;

        if (this.phoneButton != null) {
            int res = R.mipmap.attachment;
            if (this.isPhoneMediaSelected) {
                res = R.mipmap.tick;
            }

            this.phoneButton.setImageDrawable(getResources().getDrawable(res));
        }
    }

    public String getPicPath() {
        return this.pic == null ? null : this.pic.getAbsolutePath();
    }

    public String getVideoPath() { return this.video == null ? null : this.video.getAbsolutePath(); }

    public String getPhoneMediaPath() { return this.phoneMedia == null ? null : this.phoneMedia.getAbsolutePath(); }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.vid_button) shootVid();
        else if (v.getId() == R.id.pic_button) takePic();
        else if (v.getId() == R.id.phone_button)
        {
            registerForContextMenu(v);
            getActivity().openContextMenu(v);
            unregisterForContextMenu(v);
        }
    }

    @Override
    public boolean onLongClick(final View v) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        if (v.getId() == picButton.getId()) {
                            setPicture(null);
                        } else if (v.getId() == vidButton.getId()) {
                            setVideo(null);
                        } else if (v.getId() == phoneButton.getId()) {
                            setPhoneMedia(null);
                        }

                        restoreAlpha(-1);

                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                }
            }
        };

        AlertDialog.Builder builder;
        AlertDialog dialog = null;

        if (v.getId() == picButton.getId()) {
            if (this.pic == null) return true;

            builder = new AlertDialog.Builder(getActivity());
            dialog = builder.setMessage("Discard picture?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        }
        else if (v.getId() == vidButton.getId())
        {
            if (this.video == null) return true;

            builder = new AlertDialog.Builder(getActivity());
            dialog = builder.setMessage("Discard video?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        }

        if (dialog == null) return false;

        TextView dialogText = (TextView) dialog.findViewById(android.R.id.message);
        dialogText.setGravity(Gravity.CENTER);
        dialog.show();

        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        menu.setHeaderTitle("File Type");

        menu.add(PHONE_MEDIA_GROUP, PHONE_IMAGE, 0, "Image");
        menu.add(PHONE_MEDIA_GROUP, PHONE_VIDEO, 0, "Video");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        getMedia(item.getItemId());
        return true;
    }

    private boolean canShare(Location current_location) {
        //if there is no content
        if (this.textView.getText().toString().length() == 0 && !isPicTaken
                && !isVideoShoot  && !isPhoneMediaSelected) {
            this.onShareFailed("Something went wrong while uploading post.");
            return false;
        }
        else if (current_location == null)
        {
            Log.d(TAG, "No GPS data");
            this.onShareFailed("Connection failed.");
            return false;
        } else if (ParseFacebookUtils.getSession() == null)
        {
            this.onShareFailed("Please log in.");
            return false;
        }

        return true;
    }

    protected void onShareFailed(String toastText) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("result", toastText);
        getActivity().setResult(Activity.RESULT_OK, returnIntent);
        ((MainActivity)getActivity()).showTiles();
    }

    private void share() {
        Location current_location = LocationService.getLocation();

        if (!this.canShare(current_location))
        {
            return;
        }

        ParseGeoPoint p = new ParseGeoPoint(current_location.getLatitude(), current_location.getLongitude());

        Post f = new Post();

        f.setFbId(ParseUser.getCurrentUser() + "");
        f.setLocation(p);
        f.setText(this.textView.getText().toString());


        try {
            if (isPicTaken && this.pic != null) {
                Log.v(TAG, "Successfully retrieved pic.");
                ParseFile parse_pic = new ParseFile(this.pic.getName(), Utils.convertFileToByteArray(this.pic));
                f.setPictureFile(parse_pic);
            } else if (isPicTaken) { // equals isPicTaken && pic == null)
                Toast.makeText(getActivity(), "Pic not found.", Toast.LENGTH_LONG).show();
                return;
            }
            if (isVideoShoot && video != null) {
                Log.v(TAG, "Successfully retrieved video.");
                ParseFile parse_video = new ParseFile(this.video.getName(), Utils.convertFileToByteArray(this.video));
                f.setVideoFile(parse_video);
            } else if (isVideoShoot) {
                Toast.makeText(getActivity(), "Video not found.", Toast.LENGTH_LONG).show();
                return;
            }
            if (isPhoneMediaSelected && this.phoneMedia != null) {
                Log.v(TAG, "Successfully retrieved media.");
                // uploader.setPhoneMediaFile(this.phoneMedia);
                switch (this.requestedPhoneMediaType)
                {
                    case PHONE_IMAGE_REQUEST_CODE:
                        ParseFile parse_pic = new ParseFile(this.phoneMedia.getName(), Utils.convertFileToByteArray(this.pic));
                        f.setPictureFile(parse_pic);
                        break;
                    case PHONE_VIDEO_REQUEST_CODE:
                        ParseFile parse_video = new ParseFile(this.phoneMedia.getName(), Utils.convertFileToByteArray(this.video));
                        f.setVideoFile(parse_video);
                        break;
                    default:
                        throw new UnsupportedOperationException("Invalid media type");
                }
            } else if (isPhoneMediaSelected) { // equals isPicTaken && pic == null)
                Toast.makeText(getActivity(), "Media not found.", Toast.LENGTH_LONG).show();
                return;
            }
        } catch (Exception e) {
            Log.d(TAG, "Error", e);
            return;
        }

        confirmButton.setVisible(false);

        this.resetMedia();
    }

    protected void resetMedia()
    {
        this.setPicture(null);
        this.setVideo(null);

        Log.v(TAG, "Media has been cleared!");
    }

    public void takePic()
    {
        restoreAlpha(PIC_CODE);

        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePicture.resolveActivity(getActivity().getPackageManager()) != null)
        {
            this.imageFile = null;
            this.imageFile = Utils.createImageFile(PICTURE_FORMAT);

            if (this.imageFile != null) {
                takePicture.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(this.imageFile));
                this.startActivityForResult(takePicture, PIC_CAPTURE_REQUEST_CODE);
            }
        }
    }

    public void shootVid()
    {
        restoreAlpha(VIDEO_CODE);

        Intent videoIntent = new Intent(getActivity(), VideoCaptureActivity.class);

        if (videoIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(videoIntent, VID_SHOOT_REQUEST_CODE);
        }
    }

    private void getMedia(int mediaType)
    {
        restoreAlpha(PHONE_MEDIA_CODE);

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try
        {
            switch (mediaType)
            {
                case PHONE_IMAGE:
                    intent.setType("image/*");
                    startActivityForResult(
                            Intent.createChooser(intent, "Select a File to Upload"),
                            PHONE_IMAGE_REQUEST_CODE);
                    break;
                case PHONE_VIDEO:
                    intent.setType("video/*");
                    startActivityForResult(
                            Intent.createChooser(intent, "Select a File to Upload"),
                            PHONE_VIDEO_REQUEST_CODE);
            }
        } catch (android.content.ActivityNotFoundException ex) {
            // no file manager installed
            Log.e(TAG, ex.getMessage());
            Toast.makeText(getActivity(), "Please install a File Manager", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOGIN_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            FacebookUtils.getInstance().downloadFacebookInfo(getActivity());
        } else if (requestCode == PIC_CAPTURE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            if (this.imageFile == null || !this.imageFile.canRead()) {
                Toast.makeText(getActivity(), "Error encountered while taking picture", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error encountered while taking picture");
                this.imageFile = null;
                return;
            }

            this.setPicture(this.imageFile.getAbsolutePath());
            this.imageFile = null;

            changeAlphaBasedOnSelection(PIC_CODE);
        } else if (requestCode == PIC_CAPTURE_REQUEST_CODE && resultCode == Activity.RESULT_CANCELED) {
            Log.v(TAG, "Camera Intent canceled");
        } else if (requestCode == VID_SHOOT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            String videoPath = data.getExtras().getString("result");
            this.setVideo(videoPath);

            changeAlphaBasedOnSelection(VIDEO_CODE);
        } else if (requestCode == VID_SHOOT_REQUEST_CODE && resultCode == Activity.RESULT_CANCELED) {
            Log.v(TAG, "Video Intent canceled");
        } else if ((requestCode == PHONE_IMAGE_REQUEST_CODE ||
                requestCode == PHONE_VIDEO_REQUEST_CODE)
                && resultCode == Activity.RESULT_OK) {

            Uri mediaUri = data.getData();
            String mediaPath = getPath(getActivity(), mediaUri);
            File mediaFile = new File(mediaPath);
            if (mediaFile.exists()) {

                this.setPhoneMedia(mediaPath);
                this.requestedPhoneMediaType = requestCode;
                changeAlphaBasedOnSelection(PHONE_MEDIA_CODE);

                Log.d(TAG, "Media Path selected: " + mediaPath);
                Log.d(TAG, "Media Path selected(Uri): " + mediaUri.getPath());

            } else {
                Toast.makeText(getActivity(), "Invalid Media Selected", Toast.LENGTH_SHORT).show();
            }

        } else if ((requestCode == PHONE_IMAGE_REQUEST_CODE ||
                requestCode == PHONE_VIDEO_REQUEST_CODE)
                && resultCode == Activity.RESULT_CANCELED) {
            Log.v(TAG, "Phone Media Intent canceled");
        }
    }

    private void changeAlphaBasedOnSelection(int media_code) {
        switch (media_code) {
            case PIC_CODE:
                this.setVideo(null);
                this.setPhoneMedia(null);

                this.vidButton.setAlpha(0.5f);
                this.phoneButton.setAlpha(0.5f);

                this.phoneButton.setEnabled(false);
                this.vidButton.setEnabled(false);

                break;

            case VIDEO_CODE:
                this.setPicture(null);
                this.setPhoneMedia(null);

                this.picButton.setAlpha(0.5f);
                this.phoneButton.setAlpha(0.5f);

                this.phoneButton.setEnabled(false);
                this.picButton.setEnabled(false);

                break;

            case PHONE_MEDIA_CODE:
                this.setPicture(null);
                this.setVideo(null);

                this.picButton.setAlpha(0.5f);
                this.vidButton.setAlpha(0.5f);

                this.picButton.setEnabled(false);
                this.vidButton.setEnabled(false);

                break;
        }
    }

    private void restoreAlpha(int media_code) {
        if (media_code == -1 || media_code == PIC_CODE) {
            this.picButton.setAlpha(1f);
            this.picButton.setEnabled(true);
        }
        if (media_code == -1 || media_code == VIDEO_CODE) {
            this.vidButton.setAlpha(1f);
            this.vidButton.setEnabled(true);
        }
        if (media_code == -1 || media_code == PHONE_MEDIA_CODE) {
            this.phoneButton.setAlpha(1f);
            this.phoneButton.setEnabled(true);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
