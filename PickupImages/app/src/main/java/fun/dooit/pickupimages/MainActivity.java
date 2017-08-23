package fun.dooit.pickupimages;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private static final int RC_IMAGE_BROWSER = 123;
    private final int LOAD_COUNT = 20;
    final String[] projection = {
            MediaStore.Images.ImageColumns._ID,
            MediaStore.Images.ImageColumns.DATA,
            MediaStore.Images.ImageColumns.SIZE,
            MediaStore.Images.ImageColumns.DISPLAY_NAME,
            MediaStore.Images.ImageColumns.MIME_TYPE,
            MediaStore.Images.ImageColumns.TITLE
    };


    private ImageView mImgPreview;
    private Button mBtnLoadPix;
    private Button mBtnPicker;
    private Button mBtnClear;
    private RecyclerView mRecyclerView;
    private PhotoAdapter mAdapter;
    private GridLayoutManager mGridLayoutManager;
    //    private int mHeight;
//    private int mWidth;
    private ArrayList<PhotoBean> mImageList;
    private TextView mTextCounts;
    private RecyclerView.OnScrollListener mScrollListener;
    private Switch mSwitchMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageList = new ArrayList<>();
        mImgPreview = (ImageView) findViewById(R.id.img_preview);
        mBtnLoadPix = (Button) findViewById(R.id.btn_loadPix);
        mBtnPicker = (Button) findViewById(R.id.btn_picker);
        mBtnClear = (Button) findViewById(R.id.btn_clear);
        mTextCounts = (TextView) findViewById(R.id.text_counts);
        mSwitchMode = (Switch) findViewById(R.id.switch_list_mode);
        mBtnLoadPix.setOnClickListener(this);
        mBtnPicker.setOnClickListener(this);
        mSwitchMode.setOnClickListener(this);
        mBtnClear.setOnClickListener(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycle_list);
        mGridLayoutManager = new GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false);
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mScrollListener = new EndlessRecyclerViewScrollListener(mGridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                Log.d(TAG, "onLoadMore: ");
                loadExternalImages();
            }
        };
        switchListMode(mSwitchMode.isChecked());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult() called with: requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "]");
        Uri uri = data != null ? data.getData() : null;


        if (requestCode == RC_IMAGE_BROWSER) {

            if (resultCode == RESULT_OK) {
                if (uri != null) {
                    Log.d(TAG, "成功,uri: " + uri.toString());
                    mImgPreview.setImageURI(uri);
                }
                Toast.makeText(this, "成功", Toast.LENGTH_SHORT).show();

            } else if (resultCode == RESULT_CANCELED) {

                if (uri != null) {
                    Log.d(TAG, "取消,uri: " + uri.toString());
                }
                Toast.makeText(this, "取消", Toast.LENGTH_SHORT).show();
            }


        }

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.btn_loadPix:
                loadExternalImages();
                break;
            case R.id.btn_picker:
                popImgaePicker();
                break;

            case R.id.switch_list_mode:
                switchListMode(mSwitchMode.isChecked());
                break;

            case R.id.btn_clear:
                mAdapter.clearSelectItems();
                break;
        }
    }

    //切換清單UI狀態
    private void switchListMode(boolean isSelectedMode) {
        Log.d(TAG, "switchListMode() called with: isSelectedMode = [" + isSelectedMode + "]");

        //顯示清除按鈕
        if (isSelectedMode) {
            mBtnClear.setVisibility(View.VISIBLE);
        } else {
            mBtnClear.setVisibility(View.GONE);
        }


        if (this.mAdapter == null) {
            return;
        }


        this.mAdapter.switchListMode(isSelectedMode);


    }


    /**
     * 讀取系統照片
     */
    private void loadExternalImages() {


        int listCount = mImageList != null ? mImageList.size() : 0;
        ArrayList<PhotoBean> temp = new ArrayList<>();
        String orderby = "_id LIMIT ?2 OFFSET ?1";
        orderby = TextUtils.replace(orderby, new String[]{"?1", "?2"}, new String[]{String.valueOf(listCount), String.valueOf(LOAD_COUNT)}).toString();
        Log.d(TAG, orderby);
//        String selection = " LIMIT ? ";
//        String[] selectionArgs = {"7"};
        String selection = MediaStore.Images.ImageColumns.MIME_TYPE + " = ? ";
        String[] selectionArgs = {"image/jpeg"};

//        String sql="SELECT _id, _data, _size, _display_name, mime_type, title FROM images WHERE LIMIT 0 OFFSET 7";


        Cursor mCursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, orderby);

        Log.d(TAG, "onClick: " + mCursor.getCount());
        Log.d(TAG, "onClick: " + Arrays.toString(mCursor.getColumnNames()));
        if (mImageList != null && mImageList.size() > 0) {
            temp = getImageList(mCursor);
            mImageList.addAll(temp);
            mAdapter.notifyDataSetChanged();

        } else {
            temp = getImageList(mCursor);
            mImageList.addAll(temp);
            Log.d(TAG, "mImageList: " + mImageList.size());

            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
//            mWidth = metrics.widthPixels;
//            mHeight = ((metrics.widthPixels / 4) * 3);
//            mHeight = metrics.widthPixels;
            Log.i(TAG, "PhotoListAdapter: " + "手機銀幕大小為 " + metrics.widthPixels + " X " + metrics.heightPixels);
            mAdapter = new PhotoAdapter(this, mImageList, this.mSwitchMode.isChecked());
            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.addOnScrollListener(this.mScrollListener);
        }


        //更新狀態
        if (temp.size() > 0) {
            String textCounts = String.format(getString(R.string.data_counts), temp.size(), mImageList.size());
            mTextCounts.setText(textCounts);
        }

    }


    private ArrayList<PhotoBean> getImageList(Cursor cursor) {
        ArrayList<PhotoBean> list = new ArrayList<>();
        PhotoBean bean = null;
        if (cursor == null) {
            Log.d(TAG, "cursor is null");
            return list;
        }
        try {

            while (cursor.moveToNext()) {
                bean = new PhotoBean();
                bean.set_ID(cursor.getString(0));
                bean.setPath(cursor.getString(1));
                bean.setSize(cursor.getString(2));
                bean.setFileName(cursor.getString(3));
                bean.setMimeType(cursor.getString(4));
                bean.setTitle(cursor.getString(5));
                Log.d("bean", bean.toString());
                list.add(bean);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }


    /**
     * 選取圖片
     */
    private void popImgaePicker() {
        Intent picker = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        Intent picker = new Intent(Intent.ACTION_GET_CONTENT);
        picker.setType("image/jpg");
//        picker.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
//            picker.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
        Intent intent = Intent.createChooser(picker, "Pick Image");
        startActivityForResult(intent, RC_IMAGE_BROWSER);
    }

    /**
     * 照片清單Adapter
     */
    private class PhotoAdapter extends RecyclerView.Adapter<PhotoViewHolder> {

        private ArrayList<PhotoBean> list;
        private ArrayList<Integer> selectList;
        private Context context;
        private final int wVal;
        private final int hVal;
        private boolean isSelectedMode;


        public PhotoAdapter(Context context, ArrayList<PhotoBean> list, boolean isSelectedMode) {
            Log.d(TAG, "PhotoAdapter() called with:  list = [" + list.size() + "], isSelectedMode = [" + isSelectedMode + "]");
            this.context = context;
            this.list = list;
            this.isSelectedMode = isSelectedMode;
            wVal = mRecyclerView.getMeasuredWidth() / 3;
            hVal = mRecyclerView.getMeasuredWidth() / 3;
            selectList = new ArrayList<>();
            Log.d("recycler width", mRecyclerView.getWidth() + " / " + mRecyclerView.getMeasuredWidth());

        }

        //切換清單狀態
        public void switchListMode(boolean isSelectedMode) {
            Log.d(TAG, "switchListMode() called with: isSelectedMode = [" + isSelectedMode + "]");
            this.isSelectedMode = isSelectedMode;
            notifyDataSetChanged();
        }

        @Override
        public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Log.d(TAG, "onCreateViewHolder() called with: parent = [" + parent + "], viewType = [" + viewType + "]");
            View view = LayoutInflater.from(context).inflate(R.layout.photo_list_item, parent, false);
            PhotoViewHolder vh = new PhotoViewHolder(view);
            return vh;
        }

        @Override
        public void onBindViewHolder(final PhotoViewHolder holder, final int position) {
            Log.d(TAG, "onBindViewHolder() called with: holder = [" + holder + "], position = [" + position + "]");
            PhotoBean bean = getItemData(position);
            boolean isItemSelect = isItemSelected(position);
            holder.preview.setImageResource(0);
            Uri uri = Uri.fromFile(new File(bean.getPath()));
            if (uri == null) {
                Log.d(TAG, "file uri is null");
                return;
            }


            try {
                //設定選取UI大小
                holder.frameSelect.setMinimumWidth(wVal);
                holder.frameSelect.setMinimumHeight(hVal);

                Glide.with(context)
                        .load(uri)
                        .override(wVal, hVal)
                        .centerCrop()
                        .into(holder.preview);


                //選取模式切換
                if (isSelectedMode) {
                    holder.frameSelect.setSelected(isItemSelect);
                    holder.preview.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (!isSelectedMode) {
                                return;
                            }


//                            boolean isChecked = holder.frameSelect.isSelected();
//                            if (isChecked) {
                            if (isItemSelected(position)) {
                                selectList.remove((Object) position);
//                                Log.d(TAG, "remove from list: " + position);
                            } else {
                                selectList.add(position);
//                                Log.d(TAG, "added to list: " + position);
                            }
//                            getItemData(position).setChecked(!isChecked);
                            notifyItemChanged(position);
                            Log.d(TAG, Arrays.toString(selectList.toArray()));
                        }
                    });


                } else {
                    holder.frameSelect.setSelected(isSelectedMode);
                }


            } catch (Exception e) {
                Log.e(TAG, "照片寫入失敗");
                e.printStackTrace();
            }


        }

        private boolean isItemSelected(int position) {
            return selectList.contains(position);
        }

        /**
         * 取得item資料
         *
         * @param position
         * @return
         */
        private PhotoBean getItemData(int position) {
            return list == null || list.size() == 0 ? null : list.get(position);
        }

        @Override
        public int getItemCount() {
            return list != null ? list.size() : 0;
        }

        public void clearSelectItems() {
            if (selectList != null) {
                selectList.clear();
                notifyDataSetChanged();
            }
        }
    }

    /**
     *
     */
    private class PhotoViewHolder extends RecyclerView.ViewHolder {

        public View root;
        public ImageView preview;
        public FrameLayout frameSelect;

        public PhotoViewHolder(View itemView) {
            super(itemView);

            root = itemView.getRootView();
            preview = (ImageView) itemView.findViewById(R.id.img_preview);
            frameSelect = (FrameLayout) itemView.findViewById(R.id.frame_select);
        }
    }

}
