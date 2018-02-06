package com.kang.Dlife.tb_page1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.kang.Dlife.Common;
import com.kang.Dlife.R;
import com.kang.Dlife.data_base.DiaryDetailWeb;
import com.kang.Dlife.sever.LocationDao;
import com.kang.Dlife.sever.MyTask;
import com.kang.Dlife.tb_page2.diary_view.PhotoSpot;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import static android.content.ContentValues.TAG;

public class Page1_AlreadyUpload extends Fragment {
    private LocationDao locationDao;
    private RecyclerView rvDiary;
    private MyTask getAllDiaryTask;
    private List<DiaryDetailWeb> allDiaryList;
    private SpotGetImageTask spotGetImageTask;
    private MyTask newsGetAllTask;
    private ImageView ivNoUploadDiary;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {
        //因為有宣告view, 所以之後可以在這頁裡面找下面之後要用到的id
        View view = inflater.inflate(R.layout.page1_already_upload, container, false);
        rvDiary = (RecyclerView) view.findViewById(R.id.diarylist);
        ivNoUploadDiary = view.findViewById(R.id.ivNoUploadDiary);
        return view;
    }
    public void onResume() {
        super.onResume();
        showAllDiarys();
        rvDiary.setLayoutManager(
                new StaggeredGridLayoutManager(
                        // spanCount(列數 or 行數), HORIZONTAL -> 水平, VERTICAL -> 垂直
                        1, StaggeredGridLayoutManager.VERTICAL));
        if (allDiaryList != null) {
            Collections.reverse(allDiaryList);
            ivNoUploadDiary.setVisibility(View.GONE);
            rvDiary.setAdapter(new DiaryAdapter(getActivity(), allDiaryList));
        } else {
            ivNoUploadDiary.setVisibility(View.VISIBLE);
        }
    }

    private class DiaryAdapter extends
            RecyclerView.Adapter<DiaryAdapter.MyViewHolder> {
        private Context context;
        private List<DiaryDetailWeb> allDiary;


        DiaryAdapter(Context context, List<DiaryDetailWeb> allDiary) {
            this.context = context;
            this.allDiary = allDiary;
        }


        class MyViewHolder extends RecyclerView.ViewHolder {
            private ImageView imageView;
            private ImageView icWeather;
            private ImageView icNew;
            private TextView tvDate;
            private TextView tvTimeStart;
            private TextView tvTimeEnd;
            private TextView tvPlace;
            private TextView tvDiary;
            private RecyclerView rvPhoto;


            MyViewHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imageView);
                icWeather = itemView.findViewById(R.id.weather);
                icNew = itemView.findViewById(R.id.icnew);
                tvDate = itemView.findViewById(R.id.tvDate);
                tvTimeStart = itemView.findViewById(R.id.tvTimeStart);
                tvTimeEnd = itemView.findViewById(R.id.tvTimeEnd);
                tvPlace = itemView.findViewById(R.id.tvPlace);
                tvDiary = itemView.findViewById(R.id.tvDiary);
                rvPhoto = itemView.findViewById(R.id.rvImagePhoto);
            }
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View itemView = layoutInflater.inflate(R.layout.page1_recycleview, viewGroup, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder viewHolder, final int position) {

            final DiaryDetailWeb diaryDetailWeb = allDiary.get(position);
            // 抓sever的資料

                viewHolder.tvDiary.setText(diaryDetailWeb.getNote());
                viewHolder.icWeather.setImageResource(R.drawable.ic_sun);
                viewHolder.icNew.setImageResource(0);
                viewHolder.tvDate.setText(Common.dateStringToDay(diaryDetailWeb.getEnd_date()));
                viewHolder.tvTimeStart.setText(Common.dateStringToHM(diaryDetailWeb.getStart_date()));
                viewHolder.tvTimeEnd.setText(Common.dateStringToHM(diaryDetailWeb.getEnd_date()));
                Geocoder geocoder = new Geocoder(getActivity());
                //測試時初始化防呆
                if (diaryDetailWeb.getLatitude() != 0.0) {
                    try {
                        List<Address> addressList =
                                geocoder.getFromLocation(diaryDetailWeb.getLatitude(), diaryDetailWeb.getLongitude(), 1);
                        if (addressList.size() > 0) {
                            Address address = addressList.get(0);
                            String addrStr = "";
                            for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                                addrStr = address.getLocality();
                            }
                            viewHolder.tvPlace.setText(addrStr);
                        } else {
                            viewHolder.tvPlace.setText("Place");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                // 照片的recyclerView
                    viewHolder.rvPhoto.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL));
                    // 禁止recyclerView滑動
                    CustomLinearLayoutManager linearLayoutManager = new CustomLinearLayoutManager(getActivity());
                    linearLayoutManager.setScrollEnabled(false);
                    viewHolder.rvPhoto.setLayoutManager(linearLayoutManager);
                    //先放這就不會滑到底
                    viewHolder.rvPhoto.setOnFlingListener(null);
                    PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
                    pagerSnapHelper.attachToRecyclerView(viewHolder.rvPhoto);

                    List<PhotoSpot> photoSpotList = null;

                    if (Common.checkNetConnected(getActivity())) {
                        String url = Common.URL + Common.WEBPHOTO;

                        try {


                            JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("action", "getDiaryPhotoSKList");
                        jsonObject.addProperty("account", Common.getAccount(getActivity()));
                        jsonObject.addProperty("password", Common.getPWD(getActivity()));
                        jsonObject.addProperty("diarySK", allDiary.get(position).getSk());
                        String jsonOut = jsonObject.toString();

                        MyTask getDiaryPhotoSKTask = new MyTask(url, jsonOut);

                        String getDiaryPhotoSKjsonIn = getDiaryPhotoSKTask.execute().get();
                        Type ltWeb = new TypeToken<List<PhotoSpot>>() {
                        }.getType();
                        photoSpotList = new Gson().fromJson(getDiaryPhotoSKjsonIn, ltWeb);


                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }

                }
                    viewHolder.rvPhoto.setAdapter(new photoAdapter(context, photoSpotList));
                }
        }

        @Override
        public int getItemCount() {
            return allDiary.size();
        }
    }


    // 取得上傳的日記
    private void showAllDiarys() {
        if (Common.checkNetConnected(getActivity())) {
            String url = "";
            List<DiaryDetailWeb> diaryList = null;
            try {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action", "getRecyclerViewDiary");
                jsonObject.addProperty("account", Common.getAccount(getActivity()));
                jsonObject.addProperty("password", Common.getPWD(getActivity()));
                String jsonOut = jsonObject.toString();

                url = Common.URL + Common.WEBDIARY;
                getAllDiaryTask = new MyTask(url, jsonOut);
                String getDiaryJsonIn = getAllDiaryTask.execute().get();

                Gson gson = new Gson();
                JsonObject diaryInJsonObject = gson.fromJson(getDiaryJsonIn, JsonObject.class);
                String ltDiaryDetailString = diaryInJsonObject.get("getRecyclerViewDiary").getAsString();

                Type tySum = new TypeToken<List<DiaryDetailWeb>>() { }.getType();
                allDiaryList = new Gson().fromJson(ltDiaryDetailString, tySum);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            if (allDiaryList == null || allDiaryList.isEmpty()) {
                Common.showToast(getActivity(), R.string.msg_NoNewsFound);
            } else {

            }
        } else {
            Common.showToast(getActivity(), R.string.msg_NoNetwork);
        }
    }

    private class photoAdapter extends
            RecyclerView.Adapter<photoAdapter.MyViewHolder> {
        private Context context;
        private List<PhotoSpot> photoSpotList;
        private int imageSize;

        photoAdapter(Context context, List<PhotoSpot> photoSpotList) {
            this.context = context;
            this.photoSpotList = photoSpotList;
            imageSize = getResources().getDisplayMetrics().widthPixels;
        }

        //相當於一個資料夾hold住這三個的捷徑
        class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView ivRecyclerImage;


            MyViewHolder(View itemView) {
                super(itemView);
                ivRecyclerImage = (ImageView) itemView.findViewById(R.id.ivPhotoImage);


            }
        }

        //建立個替身 因為常用  建立完捷徑再給他整理起來
        @Override
        public int getItemCount() {
            return photoSpotList.size();
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View itemView = layoutInflater.inflate(R.layout.page1_recycleview_photo, viewGroup, false);
            return new MyViewHolder(itemView);
        }

        //position 就是當初listview的 index
        @Override
        public void onBindViewHolder(final MyViewHolder viewHolder, int position) {

            final PhotoSpot photoSpot = photoSpotList.get(position);

            String url = Common.URL + Common.WEBPHOTO;

            int id = photoSpot.getSk();
            spotGetImageTask = new SpotGetImageTask(url, id, imageSize, viewHolder.ivRecyclerImage);
            spotGetImageTask.execute();   //只要沒寫get 就是一直讓他抓 不等圖 不然會卡著等圖
        }
    }

    //新開 SpotGetImageTask 寫入/java 下
    public class SpotGetImageTask extends AsyncTask<Object, Integer, Bitmap> {
        private final static String TAG = "SpotGetImageTask";
        private String url;
        private int id, imageSize;

        // WeakReference物件不會阻止參照到的實體被回收
        private WeakReference<ImageView> imageViewWeakReference;

        SpotGetImageTask(String url, int id, int imageSize) {
            this(url, id, imageSize, null);
        }

        public SpotGetImageTask(String url, int id, int imageSize, ImageView imageView) {
            this.url = url;
            this.id = id;
            this.imageSize = imageSize;
            this.imageViewWeakReference = new WeakReference<>(imageView);
        }

        @Override
        protected Bitmap doInBackground(Object... params) {

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getImage");
            jsonObject.addProperty("account", Common.getAccount(getActivity()));
            jsonObject.addProperty("password", Common.getPWD(getActivity()));
            jsonObject.addProperty("id", id);
            jsonObject.addProperty("imageSize", imageSize);
            return getRemoteImage(url, jsonObject.toString());
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            ImageView imageView = imageViewWeakReference.get();
            if (isCancelled() || imageView == null) {
                return;
            }
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
//            imageView.setImageResource(R.drawable.default_image);
            }
        }

        private Bitmap getRemoteImage(String url, String jsonOut) {
            HttpURLConnection connection = null;
            Bitmap bitmap = null;
            try {
                connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setDoInput(true); // allow inputs
                connection.setDoOutput(true); // allow outputs
                connection.setUseCaches(false); // do not use a cached copy
                connection.setRequestMethod("POST");
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
                bw.write(jsonOut);
                Log.d(TAG, "output: " + jsonOut);
                bw.close();

                int responseCode = connection.getResponseCode();

                if (responseCode == 200) {
                    bitmap = BitmapFactory.decodeStream(
                            new BufferedInputStream(connection.getInputStream()));
                } else {
                    Log.d(TAG, "response code: " + responseCode);
                }
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return bitmap;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (newsGetAllTask != null) {
            newsGetAllTask.cancel(true);
        }

    }

    // 鎖定recyclerView
    public class CustomLinearLayoutManager extends LinearLayoutManager {
        private boolean isScrollEnabled = true;

        public CustomLinearLayoutManager(Context context) {
            super(context);
        }

        public void setScrollEnabled(boolean flag) {
            this.isScrollEnabled = flag;
        }

        @Override
        public boolean canScrollVertically() {
            //Similarly you can customize "canScrollHorizontally()" for managing horizontal scroll
            return isScrollEnabled && super.canScrollVertically();
        }
    }
}