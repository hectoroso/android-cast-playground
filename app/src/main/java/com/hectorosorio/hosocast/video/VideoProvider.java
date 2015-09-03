package com.hectorosorio.hosocast.video;

import android.net.Uri;
import android.util.Log;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaTrack;
import com.google.android.gms.common.images.WebImage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hector on 9/2/15.
 */
public class VideoProvider {
    private static final String TAG = VideoProvider.class.getSimpleName();

    private static final String TAG_VIDEO = "video";
    private static final String TAG_VIDEO_URL = "url";
    private static final String TAG_VIDEO_MIME = "mime";

    private static final String TAG_STUDIO = "studio";
    private static final String TAG_SUBTITLE = "subtitle";
    private static final String TAG_DURATION = "duration";
    private static final String TAG_THUMB = "thumb"; // "image-480x270";
    private static final String TAG_IMG_BIG = "image"; // image-780x1200
    private static final String TAG_TITLE = "title";

    private static final String TAG_TRACKS = "tracks";
    private static final String TAG_TRACK_ID = "id";
    private static final String TAG_TRACK_TYPE = "type";
    private static final String TAG_TRACK_SUBTYPE = "subtype";
    private static final String TAG_TRACK_CONTENT_ID = "contentId";
    private static final String TAG_TRACK_NAME = "name";
    private static final String TAG_TRACK_LANGUAGE = "language";

    public static final String KEY_DESCRIPTION = "description";

    private static List<MediaInfo> mediaList;

    public static List<MediaInfo> buildMedia(JSONObject jsonObj) throws JSONException {

        Map<String, String> urlPrefixMap = new HashMap<>();
        mediaList = new ArrayList<>();
        if (null != jsonObj && jsonObj.has(TAG_VIDEO)) {
            JSONObject video = jsonObj.getJSONObject(TAG_VIDEO);
            if (null != video) {
                String title = video.getString(TAG_TITLE);
                String studio = video.getString(TAG_STUDIO);
                String subTitle = video.getString(TAG_SUBTITLE);
                int duration = video.getInt(TAG_DURATION);
                String videoUrl = video.getString(TAG_VIDEO_URL);
                String mimeType = video.getString(TAG_VIDEO_MIME);
                String imageUrl = video.getString(TAG_THUMB);
                String bigImageUrl = video.getString(TAG_IMG_BIG);
                List<MediaTrack> tracks = null;
                if (video.has(TAG_TRACKS)) {
                    JSONArray tracksArray = video.getJSONArray(TAG_TRACKS);
                    if (tracksArray != null) {
                        tracks = new ArrayList<>();
                        for (int k = 0; k < tracksArray.length(); k++) {
                            JSONObject track = tracksArray.getJSONObject(k);
                            tracks.add(buildTrack(track.getLong(TAG_TRACK_ID),
                                    track.getString(TAG_TRACK_TYPE),
                                    track.getString(TAG_TRACK_SUBTYPE),
                                    urlPrefixMap.get(TAG_TRACKS) + track
                                            .getString(TAG_TRACK_CONTENT_ID),
                                    track.getString(TAG_TRACK_NAME),
                                    track.getString(TAG_TRACK_LANGUAGE)
                            ));
                        }
                    }
                }
                mediaList.add(buildMediaInfo(title, studio, subTitle, duration, videoUrl,
                        mimeType, imageUrl, bigImageUrl, tracks));
            }
        }
        return mediaList;
    }

    public static JSONObject buildTestJSON() {
        JSONObject data = new JSONObject();
        try {
            JSONObject video = new JSONObject();
            video.put(VideoProvider.TAG_TITLE, "Big Buck Bunny");
            video.put(VideoProvider.TAG_STUDIO, "Blender Foundation");
            video.put(VideoProvider.TAG_SUBTITLE, "Fusce id nisi turpis.");
            video.put(VideoProvider.TAG_DURATION, "596");
            video.put(VideoProvider.TAG_VIDEO_URL, "https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/mp4/BigBuckBunny.mp4");
            video.put(VideoProvider.TAG_VIDEO_MIME, "videos/mp4");
            video.put(VideoProvider.TAG_THUMB, "https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/images/480x270/BigBuckBunny.jpg");
            video.put(VideoProvider.TAG_IMG_BIG, "https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/images/780x1200/BigBuckBunny-780x1200.jpg");

            data.put(VideoProvider.TAG_VIDEO, video);
        }
        catch(JSONException ex) {
            ex.printStackTrace();
        }
        return data;
    }

    private static MediaInfo buildMediaInfo(String title, String studio, String subTitle,
                                            int duration, String url, String mimeType, String imgUrl, String bigImageUrl,
                                            List<MediaTrack> tracks) {
        MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);

        movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, studio);
        movieMetadata.putString(MediaMetadata.KEY_TITLE, title);
        movieMetadata.addImage(new WebImage(Uri.parse(imgUrl)));
        movieMetadata.addImage(new WebImage(Uri.parse(bigImageUrl)));
        JSONObject jsonObj = null;
        try {
            jsonObj = new JSONObject();
            jsonObj.put(KEY_DESCRIPTION, subTitle);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to add description to the json object", e);
        }

        return new MediaInfo.Builder(url)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType(mimeType)
                .setMetadata(movieMetadata)
                .setMediaTracks(tracks)
                .setStreamDuration(duration * 1000)
                .setCustomData(jsonObj)
                .build();
    }

    private static MediaTrack buildTrack(long id, String type, String subType, String contentId,
                                         String name, String language) {
        int trackType = MediaTrack.TYPE_UNKNOWN;
        if ("text".equals(type)) {
            trackType = MediaTrack.TYPE_TEXT;
        } else if ("video".equals(type)) {
            trackType = MediaTrack.TYPE_VIDEO;
        } else if ("audio".equals(type)) {
            trackType = MediaTrack.TYPE_AUDIO;
        }

        int trackSubType = MediaTrack.SUBTYPE_NONE;
        if (subType != null) {
            if ("captions".equals(type)) {
                trackSubType = MediaTrack.SUBTYPE_CAPTIONS;
            } else if ("subtitle".equals(type)) {
                trackSubType = MediaTrack.SUBTYPE_SUBTITLES;
            }
        }

        return new MediaTrack.Builder(id, trackType)
                .setName(name)
                .setSubtype(trackSubType)
                .setContentId(contentId)
                .setLanguage(language).build();
    }
}
