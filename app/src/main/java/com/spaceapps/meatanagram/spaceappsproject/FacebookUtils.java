package com.spaceapps.meatanagram.spaceappsproject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public final class FacebookUtils {

    protected static final int LARGE_PIC_SIZE = 200;
    protected static final int SMALL_PIC_SIZE = 120;

    private static final String TAG = "FacebookUtils";
    private static final FacebookUtils shared_instance = new FacebookUtils();
    private final HashMap<String, HashSet<ParseUtilCallback>> scheduledOperationsQueue = new HashMap<>();

    //attempt to solve slow download of profile picture
    public static final String DEFAULT_AVATAR_URL = "http://1.gravatar.com/avatar/ad516503a11cd5ca435acc9bb6523536?s=256";

    private FacebookUtils() {
    }

    /**
     * This is a singleton class. This method returns the ONLY instance
     *
     * @return Singleton instance
     */
    public static FacebookUtils getInstance() {
        return FacebookUtils.shared_instance;
    }

    /**
     * @return true if the current user is ready
     */
    public static boolean isFacebookSessionOpened() {
        return ParseFacebookUtils.getSession() != null && ParseFacebookUtils.getSession().isOpened();
    }

    public void downloadFacebookInfo(Context ctx) {
        final ProgressDialog progress = new ProgressDialog(ctx);
        progress.setTitle("Loading");
        progress.setMessage("Wait while loading...");
        progress.setCancelable(false);
        progress.show();
        FacebookUtils.getInstance().makeMeRequest(new ParseUtilCallback() {
            @Override
            public void onResult(String result, Exception e) {
                if (e != null) {
                    // FIXME result not being used?

                    Log.d(TAG, e.getMessage());
                    progress.setMessage(e.getMessage());
                } else {
                    progress.dismiss();
                    Log.d(TAG, result);
                }
            }
        });
    }

    /**
     * Configures current user
     *
     * @param cbk callback
     */
    public void makeMeRequest(final ParseUtilCallback cbk) {

        final Session session = ParseFacebookUtils.getSession();
        if (session == null) {
            cbk.onResult(null, new RuntimeException("Session not valid"));
            return;
        }

        Request request = Request.newMeRequest(session,
                new Request.GraphUserCallback() {
                    @Override
                    public void onCompleted(GraphUser user, Response response) {
                        if (user != null) {
                            FacebookUtils.setCurrentUserId(user.getId());
                        }
                    }
                });
        request.executeAsync();
    }

    public static void setCurrentUserId(String id){}
    public static String getCurrentUserId(){ return ParseUser.getCurrentUser() + ""; }

    /**
     * Asynchronously retrieves the User's username. If the username is cached callback will be called immediately.
     * Otherwise a request to fb APIs will be issued
     *
     * @param fb_id FB user id
     * @param cbk   callback parameter. MUST not be null. User Username will be given as a parameter of onResult method
     */
    public void getFacebookUsernameFromID(final String fb_id, final ParseUtilCallback cbk) {
        Log.d(TAG, "In getFacebookUsernameFromID");

        String username = ParseUser.getCurrentUser().getUsername();
        if (username != null) {
            if (cbk != null) {
                cbk.onResult(username, null);
                return;
            }
        }

        final String current_key = "NAME " + fb_id;
        synchronized (this.scheduledOperationsQueue) {
            if (this.scheduledOperationsQueue.containsKey(current_key)) {
                Set<ParseUtilCallback> cbks = this.scheduledOperationsQueue.get(current_key);
                cbks.add(cbk);
                //Log.d(TAG, "Enqueued"+current_key);
                return;
            } else {
                HashSet<ParseUtilCallback> newSet = new HashSet<>();
                newSet.add(cbk);
                this.scheduledOperationsQueue.put(current_key, newSet);
                //Log.d(TAG, "Scheduled"+current_key);
            }
        }

        Bundle bundle = new Bundle();
        bundle.putString("fields", "name");
        Request req = new Request(ParseFacebookUtils.getSession(), fb_id, bundle, HttpMethod.GET,
                new Request.Callback() {
                    @Override
                    public void onCompleted(Response response) {
                        try {
                            GraphObject go = response.getGraphObject();

                            Log.d(TAG, "Response in getFacebookUsernameFromID: " + go.toString());

                            JSONObject obj = go.getInnerJSONObject();
                            String name = obj.getString("name");

                            Set<ParseUtilCallback> cbks;
                            synchronized (FacebookUtils.this.scheduledOperationsQueue) {
                                cbks = FacebookUtils.this.scheduledOperationsQueue.remove(current_key);
                            }


                            if (cbks != null) {
                                for (ParseUtilCallback c : cbks) {
                                    c.onResult(name, null);
                                }
                            }
                        } catch (JSONException | NullPointerException e) {
                            Log.v(TAG, "Couldn't resolve facebook user's name.  Error: " + e.toString());
                            e.printStackTrace();
                            Set<ParseUtilCallback> cbks;
                            synchronized (FacebookUtils.this.scheduledOperationsQueue) {
                                cbks = FacebookUtils.this.scheduledOperationsQueue.remove(current_key);
                            }
                            if (cbks != null) {
                                for (ParseUtilCallback c : cbks) {
                                    c.onResult(null, e);
                                }
                            }
                        }
                    }
                });

        req.executeAsync();
    }

    protected void loadUsernameIntoTextView(final String userId, final TextView tv) {
        // if(PlacesLoginUtils.loginType == PlacesLoginUtils.LoginType.FACEBOOK) {
        getFacebookUsernameFromID(userId, new ParseUtilCallback() {
            @Override
            public void onResult(String result, Exception e) {
                Log.d(TAG, "Result in loadUsernameIntoTextView: " + result);
                tv.setText(result);
            }
        });
    }

    public enum PicSize {
        SMALL, LARGE;

        public String toString() {
            if (this == SMALL) {
                return SMALL_PIC_SIZE + "";
            }
            return LARGE_PIC_SIZE + "";
        }
    }

    /**
     * Asynchronously loads a profile pictures into an image view
     *
     * @param user_id   facebook user id
     * @param imageView ImageView where to load picture
     */
    public void loadProfilePicIntoImageView(final String user_id, final ImageView imageView, final PicSize size) {

        this.getFbProfilePictureURL(user_id, size, new ParseUtilCallback() {
            @Override
            public void onResult(String result, Exception e) {
                if (e == null) {
                    if (result.trim().length() == 0) {
                        Picasso.with(SpaceAppsApplication.getAppContext()).load(DEFAULT_AVATAR_URL).into(imageView);
                    } else {
                        Picasso.with(SpaceAppsApplication.getAppContext()).load(result).into(imageView);
                    }
                } else {
                    Log.d(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * Asynchronously computes the url of a FB profile picture
     *
     * @param user_id FB user id
     * @param size    size of the profile picture
     * @param cbk     callback parameter. MUST not be null. Picture URL will be given as a parameter of onResult method
     */
    public void getFbProfilePictureURL(final String user_id, final PicSize size, final ParseUtilCallback cbk)
    {
        final String current_key = "PIC_" + size + '_' + user_id;
        synchronized (this.scheduledOperationsQueue) {

            if (this.scheduledOperationsQueue.containsKey(current_key)) {
                Set<ParseUtilCallback> cbksSet = this.scheduledOperationsQueue.get(current_key);
                cbksSet.add(cbk);
                //Log.d(TAG, "Enqueued: " + user_id);
                return;
            } else {
                HashSet<ParseUtilCallback> cbksSet = new HashSet<>();
                cbksSet.add(cbk);
                //Log.d(TAG, "Scheduled: " + user_id);
                this.scheduledOperationsQueue.put(current_key, cbksSet);
            }
        }


        Bundle bundle = new Bundle();
        bundle.putBoolean("redirect", false);
        bundle.putString("height", size.toString());
        bundle.putString("type", "normal");
        bundle.putString("width", size.toString());

        Request req = new Request(ParseFacebookUtils.getSession(), '/' + user_id + "/picture", bundle, HttpMethod.GET,
                new Request.Callback() {
                    @Override
                    public void onCompleted(Response response) {
                        try {
                            GraphObject go = response.getGraphObject();

                            JSONObject obj = go.getInnerJSONObject();
                            final String url = obj.getJSONObject("data").getString("url");

                            Set<ParseUtilCallback> cbks;
                            synchronized (FacebookUtils.this.scheduledOperationsQueue) {
                                cbks = FacebookUtils.this.scheduledOperationsQueue.remove(current_key);
                            }

                            if (cbks != null) {
                                for (ParseUtilCallback c : cbks) {
                                    c.onResult(url, null);
                                }
                            }

                        } catch (JSONException e) {
                            Log.v(TAG, "Couldn't retrieve facebook user data.  Error: " + e.toString());
                            e.printStackTrace();
                            Set<ParseUtilCallback> cbks;
                            synchronized (FacebookUtils.this.scheduledOperationsQueue) {
                                cbks = FacebookUtils.this.scheduledOperationsQueue.remove(current_key);
                            }
                            if (cbks != null) {
                                for (ParseUtilCallback c : cbks) {
                                    c.onResult(null, e);
                                }
                            }

                        } catch (NullPointerException npe) {
                            Log.e(TAG, "GraphObject is null!");
                            npe.printStackTrace();
                        }
                    }
                }
        );

        req.executeAsync();
    }
}