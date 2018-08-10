package com.sky.techflakepractical.ui.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.giphy.sdk.core.models.Media;
import com.giphy.sdk.core.models.enums.MediaType;
import com.giphy.sdk.core.network.api.CompletionHandler;
import com.giphy.sdk.core.network.api.GPHApi;
import com.giphy.sdk.core.network.api.GPHApiClient;
import com.giphy.sdk.core.network.response.ListMediaResponse;
import com.sky.techflakepractical.App;
import com.sky.techflakepractical.R;
import com.sky.techflakepractical.models.Likes;
import com.sky.techflakepractical.models.Likes_;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @BindView(R.id.edtSearch)
    EditText edtSearch;
    @BindView(R.id.imgClear)
    ImageView imgClear;
    @BindView(R.id.gridView)
    GridView gridView;

    GPHApi gphApi;
    Context mContext;

    @Inject
    BoxStore boxStore;

    Box<Likes> likesBox;

    List<Media> arrMedia;
    MyGiphyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mContext = MainActivity.this;

        ((App) getApplication()).getNetComponent().inject(this);
        likesBox = boxStore.boxFor(Likes.class);

        gphApi = new GPHApiClient("aKSlEDzfBjMGmAoxQfEZb3zqSyq296LO");

        edtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (edtSearch.getText().length() > 0) {
                        hideSoftKeyboard();
                        loadGIFVideos(edtSearch.getText().toString().trim());
                        return true;
                    } else {
                        Toast.makeText(mContext, "Please insert some text to search", Toast.LENGTH_SHORT).show();
                    }
                    return false;
                }
                return false;
            }
        });

        imgClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtSearch.getText().clear();
            }
        });

    }

    public void hideSoftKeyboard() {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(((Activity) mContext).getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadGIFVideos(String key) {

        final ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage("Loading");
        progressDialog.setCancelable(false);
        progressDialog.show();

        gphApi.search(key, MediaType.gif, 50, null, null, null, new CompletionHandler<ListMediaResponse>() {
            @Override
            public void onComplete(ListMediaResponse result, Throwable e) {
                progressDialog.dismiss();
                arrMedia = new ArrayList<>();
                if (result == null) {
                    Toast.makeText(mContext, "Error for fetching data", Toast.LENGTH_SHORT).show();
                } else {
                    if (result.getData() != null) {
                        for (Media gif : result.getData()) {
                            Log.i("giphy", gif.getId());
                            arrMedia.add(gif);
                        }
                        creatingObservable(arrMedia);
                    } else {
                        Log.e("giphy error", "No results found");
                    }
                }
            }
        });
    }

    private void creatingObservable(List<Media> mediaList) {
        final Observable<List<Media>> listObserable = Observable.just(mediaList);
        listObserable.subscribe(new Observer<List<Media>>() {

            @Override
            public void onError(Throwable e) {
                Log.i(TAG, "onError()", e);
            }

            @Override
            public void onComplete() {
                Log.i(TAG, "onComplete: ");
            }

            @Override
            public void onSubscribe(Disposable d) {
                Log.i(TAG, "onSubscribe: ");
            }

            @Override
            public void onNext(List<Media> data) {
                Log.i(TAG, "onNext: " + data.size());
                adapter = null;
                adapter = new MyGiphyAdapter(data);
                gridView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        });

    }

    class MyGiphyAdapter extends BaseAdapter {

        List<Media> arrModels;

        MyGiphyAdapter(List<Media> arrModels) {
            this.arrModels = arrModels;
        }

        @Override
        public int getCount() {
            return arrModels.size();
        }

        @Override
        public Object getItem(int position) {
            return arrModels.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return getCount();
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final Media media = arrModels.get(position);
            final MyViewHolder holder;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
                assert inflater != null;
                convertView = inflater.inflate(R.layout.row_giphy_view, parent, false);
                holder = new MyViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (MyViewHolder) convertView.getTag();
            }

            Picasso.get().load(media.getImages().getFixedHeightStill().getGifUrl()).placeholder(R.drawable.load_image).into(holder.img);

            Likes sr_like = likesBox.query().equal(Likes_.gifId, media.getId()).build().findFirst();
            if (sr_like != null) {
                holder.txtThumbsUp.setText(sr_like.getUpVote());
                holder.txtThumbsDown.setText(sr_like.getDownVote());
            }

            holder.img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, ExoplayerActivity.class);
                    intent.putExtra("media", media);
                    startActivity(intent);
                }
            });
            holder.imgThumpsUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int up = Integer.parseInt(holder.txtThumbsUp.getText().toString());
                    holder.txtThumbsUp.setText(String.valueOf(up + 1));

                    Likes arr = likesBox.query().equal(Likes_.gifId, media.getId()).build().findFirst();

                    if (up == 0 && arr == null) {
                        Likes like = new Likes();
                        like.setGifId(media.getId());
                        like.setUpVote(String.valueOf(up + 1));
                        like.setDownVote(holder.txtThumbsDown.getText().toString());
                        likesBox.put(like);
                        Log.d(App.TAG, "Inserted new note, ID: " + like.getId());
                    } else {
                        assert arr != null;
                        arr.setUpVote(String.valueOf(up + 1));
                        likesBox.put(arr);
                        Log.d(App.TAG, "updated ID: " + arr.getId());
                    }
                }
            });

            holder.imgThumpsDown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int down = Integer.parseInt(holder.txtThumbsDown.getText().toString());
                    holder.txtThumbsDown.setText(String.valueOf(down + 1));

                    Likes arr = likesBox.query().equal(Likes_.gifId, media.getId()).build().findFirst();

                    if (down == 0 && arr == null) {
                        Likes like = new Likes();
                        like.setGifId(media.getId());
                        like.setUpVote(holder.txtThumbsDown.getText().toString());
                        like.setDownVote(String.valueOf(down + 1));
                        likesBox.put(like);
                        Log.d(App.TAG, "Inserted new note, ID: " + like.getId());
                    } else {
                        assert arr != null;
                        arr.setDownVote(String.valueOf(down + 1));
                        likesBox.put(arr);
                        Log.d(App.TAG, "updated ID: " + arr.getId());
                    }
                }
            });

            return convertView;
        }

        class MyViewHolder {
            @BindView(R.id.imgIcon)
            ImageView img;
            @BindView(R.id.imgThumpsUp)
            ImageView imgThumpsUp;
            @BindView(R.id.imgThumpsDown)
            ImageView imgThumpsDown;
            @BindView(R.id.txtThumbsUp)
            TextView txtThumbsUp;
            @BindView(R.id.txtThumbsDown)
            TextView txtThumbsDown;

            MyViewHolder(View view) {
                ButterKnife.bind(this, view);
            }
        }
    }
}
