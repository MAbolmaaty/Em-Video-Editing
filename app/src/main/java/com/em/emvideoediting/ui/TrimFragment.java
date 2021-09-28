package com.em.emvideoediting.ui;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
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
import java.net.URI;

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

    private ActivityResultLauncher<String> mGetVideo = registerForActivityResult(
            new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri result) {
                    Log.d(TAG, "result : " + result.toString());
                    mVideoPath = getRealPathFromURI(result);
                    Log.d(TAG, "mVideoPath : " + mVideoPath);
                    mVideoView.setVideoURI(result);
                    String[] pathSegments = mVideoPath.split("/");
                    mVideoName = pathSegments[pathSegments.length-1];
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
                String directory = createStorageDirectory(mVideoName);

//                String file =
//                        new File(directory,"trim" + "%02d" + ".mp4").getAbsolutePath();
//                final String[] trim = {"-i", mVideoPath,
//                        "-codec:a", "copy", "-f", "segment", "-segment_time",
//                        "13", "-codec:v", "copy",
//                        "-map", "0", file};

                int start = 0;
                for(int i = 0 ; i < 2 ; i++){
                    String file =
                            new File(directory,"trim" + "-" + i + ".mp4").getAbsolutePath();

                    final String[] emTrim = {
                            "-i", mVideoPath,
                            "-ss", String.valueOf(start),
                            "-t", "60",
                            "-c:v", "libx264",
                            "-crf", "30",
                              file};

                    FFmpeg.executeAsync(emTrim, new ExecuteCallback() {
                        @Override
                        public void apply(long executionId, int returnCode) {
                            Log.d(TAG, "start");
                            if (returnCode == RETURN_CODE_SUCCESS) {
                                Log.d(TAG, "success");
                            } else {
                                Log.d(TAG, "fail : " + returnCode);
                            }
                        }
                    });
                    start = start + 60;
                }


//                FFmpeg.executeAsync(trim, new ExecuteCallback() {
//                    @Override
//                    public void apply(long executionId, int returnCode) {
//                        Log.d(TAG, "start");
//                        if (returnCode == RETURN_CODE_SUCCESS) {
//                            Log.d(TAG, "success");
//                        } else {
//                            Log.d(TAG, "fail : " + returnCode);
//                        }
//                    }
//                });
            }
        });

        return view;
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
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

    private String createStorageDirectory(String name){
        File file = new File(Environment.getExternalStorageDirectory(),
                "EmVideoEditing/" + name + "/");
        if (file.mkdirs()){
            Log.d(TAG, "File created");
        } else {
            Log.d(TAG, "File not created");
        }
        return file.getAbsolutePath();
    }
}