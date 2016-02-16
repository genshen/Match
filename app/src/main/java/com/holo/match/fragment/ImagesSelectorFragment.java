package com.holo.match.fragment;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageView;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;

import com.holo.m.files.BasicFileInformation;
import com.holo.match.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 根深 on 2016/2/1.
 */
public class ImagesSelectorFragment extends Fragment implements AdapterView.OnItemClickListener, AbsListView.MultiChoiceModeListener {
    ImageLoader imageLoader;
    GridView image_grid_view;
    ImagesAdapter adapter;
    List<Map<String, Object>> images;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Initialize ImageLoader with configuration.
        ImageLoaderConfiguration configuration = ImageLoaderConfiguration.createDefault(getContext());
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(configuration);
    }

    @Override
    public void onStop() {
        imageLoader.stop();
        super.onStop();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_images_selector, container, false);
        ContentResolver contentResolver = getActivity().getContentResolver();
        images = getImagesList(contentResolver);
        adapter = new ImagesAdapter(getContext(), contentResolver, images);
        image_grid_view = (GridView) rootView.findViewById(R.id.image_selector_gridview);
        image_grid_view.setAdapter(adapter);
        image_grid_view.setOnScrollListener(new PauseOnScrollListener(imageLoader, false, true));
        image_grid_view.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
        image_grid_view.setMultiChoiceModeListener(this);
        image_grid_view.setOnItemClickListener(this);
        return rootView;
    }

    final String CHECKED = "checked";

    private List<Map<String, Object>> getImagesList(ContentResolver contentResolver) {
        List<Map<String, Object>> image_list = new ArrayList<>();
        Cursor cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, null, null, MediaStore.Images.Media.DATA);
        if (cursor != null) {
            cursor.moveToFirst();
//            ImageView image = (ImageView) view.findViewById(R.id.thumbnail);
//            image.setImageBitmap(MediaStore.Images.Thumbnails.getThumbnail(contentResolver, (long) id, MediaStore.Images.Thumbnails.MICRO_KIND, null));
            while (!cursor.isAfterLast()) {
                HashMap<String, Object> list_item = new HashMap<>();
                list_item.put(MediaStore.Images.Media._ID, cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID)));
//                list_item.put(MediaStore.Images.Media.TITLE, cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.TITLE)));
                list_item.put(MediaStore.Images.Media.DATA, cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)));
                list_item.put(MediaStore.Images.Media.DISPLAY_NAME, cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)));
                list_item.put(MediaStore.Images.Media.SIZE, cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.SIZE)));
                list_item.put(CHECKED, false);
                image_list.add(list_item);
                cursor.moveToNext();
            }
            cursor.close();
        }
        return image_list;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Map<String, Object> m = images.get(position);
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.dialog_file_selector_send_ack)
                .setMessage(R.string.dialog_file_selector_send_ack_message)
                .setPositiveButton(R.string.dialog_file_selector_send_ack_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int witch) {
                        List<BasicFileInformation> music = new ArrayList<>();
                        music.add(new BasicFileInformation(m.get(MediaStore.Images.Media.DATA).toString(),
                                m.get(MediaStore.Images.Media.DISPLAY_NAME).toString(), (long) m.get(MediaStore.Images.Media.SIZE)));
                        startResult(music);
                    }
                })
                .setNegativeButton(R.string.dialog_file_selector_send_ack_cancel, null).show();
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        if (checked) {
            images.get(position).put(CHECKED, true);
        } else {
            images.get(position).put(CHECKED, false);
        }
        adapter.notifyDataSetChanged();
        mode.setTitle(getString(R.string.have_selected_count, image_grid_view.getCheckedItemCount()));
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.file_selector_app, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        mode.setTitle(getString(R.string.have_selected_count, image_grid_view.getCheckedItemCount()));
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_send:
                List<BasicFileInformation> musics = new ArrayList<>();
                for (int i = adapter.getCount() - 1; i >= 0; i--) {
                    if (image_grid_view.isItemChecked(i)) {
                        Map<String, Object> m = images.get(i);
                        musics.add(new BasicFileInformation(m.get(MediaStore.Images.Media.DATA).toString(),
                                m.get(MediaStore.Images.Media.DISPLAY_NAME).toString(), (long) m.get(MediaStore.Images.Media.SIZE)));
                    }
                }
                startResult(musics);
                break;
        }
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        for (int i = image_grid_view.getCount() - 1; i >= 0; i--) {
            image_grid_view.setItemChecked(i, false);
            images.get(i).put(CHECKED, false);
        }
        adapter.notifyDataSetChanged();
    }

    private void startResult(List<BasicFileInformation> musics) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("files", (Serializable) musics);
        intent.putExtras(bundle);
        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
    }

    private class ImagesAdapter extends BaseAdapter {
        List<Map<String, Object>> images;
        Context context;
        ContentResolver contentResolver;
        DisplayImageOptions options;

        public ImagesAdapter(Context context, ContentResolver contentResolver, List<Map<String, Object>> images) {
            this.images = images;
            this.context = context;
            this.contentResolver = contentResolver;
            options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.filesystem_icon_photo)
                    .cacheInMemory(true)
                    .cacheOnDisk(false)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build();
        }

        @Override
        public int getCount() {
            return images.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            long id = (long) images.get(position).get(MediaStore.Images.Media._ID);
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = getLayoutInflater(null).inflate(R.layout.gridview_item_images, null);
                holder.imageView = (AppCompatImageView) convertView.findViewById(R.id.gridview_item_image_view);
                holder.checkbox = (CheckBox) convertView.findViewById(R.id.image_select_box);
//                holder.imageView.setImageBitmap(MediaStore.Images.Thumbnails.getThumbnail(contentResolver, id, MediaStore.Images.Thumbnails.MICRO_KIND, null));
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            imageLoader.displayImage("content://media/external/images/media/" + id, holder.imageView, options);
            holder.checkbox.setVisibility((boolean) images.get(position).get(CHECKED) ? View.VISIBLE : View.INVISIBLE);
//            MediaStore.Images.Thumbnails.getThumbnail(contentResolver, id, MediaStore.Images.Thumbnails.MICRO_KIND, null);
            return convertView;
        }

        private class ViewHolder {
            public AppCompatImageView imageView;
            CheckBox checkbox;
        }
    }
}
