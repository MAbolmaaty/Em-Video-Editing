package com.em.emvideoediting.ui;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.storage.StorageManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.loader.content.CursorLoader;

import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.em.emvideoediting.R;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TrimFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TrimFragment extends Fragment {

    private static final String TAG = TrimFragment.class.getSimpleName();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ImageView mIcVideoAdd;
    private VideoView mVideoView;
    private ImageView mIcVideoControl;
    private ImageView mIcRemove;
    private TextView mUpload;
    private Button mFFmpeg;

    private String mVideoPath;
    private String mVideoName;
    private String mDirectory;

    private ActivityResultLauncher<String> mGetVideo = registerForActivityResult(
            new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri result) {
                    Log.d(TAG, "result : " + result.toString());
                    mVideoPath = getPath(getActivity(), result);
                    Log.d(TAG, "mVideoPath : " + mVideoPath);
                    mVideoView.setVideoURI(result);
                    String[] pathSegments = mVideoPath.split("/");
                    mVideoName = pathSegments[pathSegments.length - 1];
                    mVideoView.start();
                    mVideoView.setVisibility(View.VISIBLE);
                    mIcVideoAdd.setVisibility(View.INVISIBLE);
                    mUpload.setVisibility(View.INVISIBLE);
                    mIcRemove.setVisibility(View.VISIBLE);
                    mFFmpeg.setVisibility(View.VISIBLE);
                    mVideoView.requestFocus();
                }
            });

    public TrimFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TrimFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TrimFragment newInstance(String param1, String param2) {
        TrimFragment fragment = new TrimFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_trim, container, false);
        mVideoView = view.findViewById(R.id.video_view);
        mIcVideoAdd = view.findViewById(R.id.ic_video_add);
        mIcVideoControl = view.findViewById(R.id.ic_video_control);
        mIcRemove = view.findViewById(R.id.ic_remove);
        mUpload = view.findViewById(R.id.text_upload);
        mFFmpeg = view.findViewById(R.id.ffmpeg);

        // Creating MediaController
        MediaController mediaController = new MediaController(getContext());
        mediaController.setAnchorView(mVideoView);

        mIcVideoAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGetVideo.launch("video/*");
            }
        });

        mVideoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mVideoView.isPlaying()) {
                    mIcVideoControl.setImageResource(R.drawable.ic_pause);
                    mIcVideoControl.setVisibility(View.VISIBLE);
                    mVideoView.pause();
                } else {
                    mIcVideoControl.setImageResource(R.drawable.ic_play);
                    mIcVideoControl.setVisibility(View.VISIBLE);
                    mVideoView.setClickable(false);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mIcVideoControl.setVisibility(View.INVISIBLE);
                            mVideoView.setClickable(true);
                        }
                    }, 1200);
                    mVideoView.start();
                }
            }
        });

        mIcRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVideoView.setVisibility(View.INVISIBLE);
                mIcVideoControl.setVisibility(View.INVISIBLE);
                mIcVideoAdd.setVisibility(View.VISIBLE);
                mUpload.setVisibility(View.VISIBLE);
                mIcRemove.setVisibility(View.INVISIBLE);
                mFFmpeg.setVisibility(View.INVISIBLE);
                mVideoView.stopPlayback();
            }
        });

        mFFmpeg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDirectory = createStorageDirectory(mVideoName);
                trim(50, 438, 0,1);
//                String file =
//                        new File(mDirectory, "trim" + "-" + "%02d" + ".mp4").getAbsolutePath();
//
//                final String[] emTrim = {
//                        "-i", mVideoPath,
//                        "-c:v", "libx264",
//                        "-crf", "22",
//                        "-map", "0",
//                        "-segment_time", "50",
//                        "-g", "50",
//                        "-sc_threshold", "0",
//                        "-force_key_frames", "expr:gte(t,n_forced*50)",
//                        "-f", "segment",
//                        file};
//
//                FFmpeg.executeAsync(emTrim, new ExecuteCallback() {
//                    @Override
//                    public void apply(long executionId, int returnCode) {
//                        Log.d(TAG, "start");
//                        if (returnCode == RETURN_CODE_SUCCESS) {
//                            Log.d(TAG, "success");
//                            File directory = new File(mDirectory);
//                            File[] files = directory.listFiles();
//                            if (files != null && files.length > 1) {
//                                for (int i = 0 ; i < files.length ; i++){
//                                    String copyFile =
//                                            new File(mDirectory, "trim" + "-" + "copy - " + i
//                                                    + ".mp4").getAbsolutePath();
//                                    final String[] seek = {
//                                            "-ss", "0",
//                                            "-i", files[i].getAbsolutePath(),
//                                            "-c:v", "copy",
//                                            "-c:a", "copy",
//                                            copyFile};
//                                    FFmpeg.executeAsync(seek, new ExecuteCallback() {
//                                        @Override
//                                        public void apply(long executionId, int returnCode) {
//                                            Log.d(TAG, "start");
//                                            if (returnCode == RETURN_CODE_SUCCESS) {
//                                                Log.d(TAG, "success");
//
//                                            } else {
//                                                Log.d(TAG, "fail : " + returnCode);
//                                            }
//                                        }
//                                    });
//
//                                }
//                            }
//                        } else {
//                            Log.d(TAG, "fail : " + returnCode);
//                        }
//                    }
//                });
            }
        });

        return view;
    }

    private void trim(int secs, int totalSecs, int start, int counter) {
        if (start >= totalSecs) {
            File directory = new File(mDirectory);
            File[] files = directory.listFiles();
            if (files != null && files.length > 1) {
                Arrays.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(File o1, File o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                });
            }
            return;
        }

        mDirectory = createStorageDirectory(mVideoName);
        String name = String.format(Locale.ENGLISH, "trim-%02d", counter);
        String file =
                new File(mDirectory, name + ".mp4").getAbsolutePath();

        final String[] trim = {
                "-ss", String.valueOf(start),
                "-i", mVideoPath,
                "-t", String.valueOf(secs),
                "-c:v", "libx264",
                "-crf", "23",
                file};

        FFmpeg.executeAsync(trim, new ExecuteCallback() {
            @Override
            public void apply(long executionId, int returnCode) {
                Log.d(TAG, "start");
                if (returnCode == RETURN_CODE_SUCCESS) {
                    Log.d(TAG, "success");
                    trim(secs, totalSecs, start + secs, counter + 1);
                } else {
                    Log.d(TAG, "fail : " + returnCode);
                }
            }
        });
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(getActivity(), contentUri, proj,
                null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    private String getVideoPath(Uri uri) {
        String documentId = DocumentsContract.getDocumentId(uri);
        Log.d(TAG, "documentId : " + documentId);
        String videoId = documentId.split(":")[1];

        Uri collection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        }

        final String column = "_data";
        final String[] projection = {column};

        final String selection = "_id=?";
        final String[] selectionArgs = new String[]{videoId};

        Cursor cursor = getActivity().getContentResolver().query(collection,
                projection, selection, selectionArgs, null);

        String videoPath = "No Video";

        if (cursor != null) {
            int columnIndex = cursor.getColumnIndex(column);

            if (cursor.moveToFirst()) {
                videoPath = cursor.getString(columnIndex);
            }
            cursor.close();
        }
        return videoPath;
    }

    private String createStorageDirectory(String name) {
        File file = new File(Environment.getExternalStorageDirectory(),
                "EmVideoEditing/" + name + "/");
        if (file.mkdirs()) {
            Log.d(TAG, "File created");
        } else {
            Log.d(TAG, "File not created");
        }
        return file.getAbsolutePath();
    }

    private String getPath(final Context context, final Uri uri) {

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
                } else {
                    // Below logic is how External Storage provider build URI for documents
                    // Based on http://stackoverflow.com/questions/28605278/android-5-sd-card-label and https://gist.github.com/prasad321/9852037
                    StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);

                    try {
                        Class<?> storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
                        Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
                        Method getUuid = storageVolumeClazz.getMethod("getUuid");
                        Method getState = storageVolumeClazz.getMethod("getState");
                        Method getPath = storageVolumeClazz.getMethod("getPath");
                        Method isPrimary = storageVolumeClazz.getMethod("isPrimary");
                        Method isEmulated = storageVolumeClazz.getMethod("isEmulated");

                        Object result = getVolumeList.invoke(mStorageManager);

                        final int length = Array.getLength(result);
                        for (int i = 0; i < length; i++) {
                            Object storageVolumeElement = Array.get(result, i);
                            //String uuid = (String) getUuid.invoke(storageVolumeElement);

                            final boolean mounted = Environment.MEDIA_MOUNTED.equals(getState.invoke(storageVolumeElement))
                                    || Environment.MEDIA_MOUNTED_READ_ONLY.equals(getState.invoke(storageVolumeElement));

                            //if the media is not mounted, we need not get the volume details
                            if (!mounted) continue;

                            //Primary storage is already handled.
                            if ((Boolean) isPrimary.invoke(storageVolumeElement) && (Boolean) isEmulated.invoke(storageVolumeElement))
                                continue;

                            String uuid = (String) getUuid.invoke(storageVolumeElement);

                            if (uuid != null && uuid.equals(type)) {
                                String res = getPath.invoke(storageVolumeElement) + "/" + split[1];
                                return res;
                            }
                        }
                    } catch (Exception ex) {
                    }
                }
            }


            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                if (!TextUtils.isEmpty(id)) {
                    if (id.startsWith("raw:")) {
                        return id.replaceFirst("raw:", "");
                    }
                }
                String[] contentUriPrefixesToTry = new String[]{
                        "content://downloads/public_downloads",
                        "content://downloads/my_downloads",
                        "content://downloads/all_downloads"
                };

                for (String contentUriPrefix : contentUriPrefixesToTry) {
                    Uri contentUri = ContentUris.withAppendedId(Uri.parse(contentUriPrefix), Long.valueOf(id));
                    try {
                        String path = getDataColumn(context, contentUri, null, null);
                        if (path != null) {
                            return path;
                        }
                    } catch (Exception e) {
                    }
                }

                return null;
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

//            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    /**
     * Get the value of the data column for this Uri.
     */
    private String getDataColumn(Context context, Uri uri, String selection,
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
    private boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}