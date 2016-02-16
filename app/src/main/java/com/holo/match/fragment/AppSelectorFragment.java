package com.holo.match.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.holo.m.files.BasicFileInformation;
import com.holo.match.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by 根深 on 2016/1/26.
 */
public class AppSelectorFragment extends Fragment implements AdapterView.OnItemClickListener, AbsListView.MultiChoiceModeListener {
    // TODO: Rename parameter arguments, choose names that match
    private GridView mAppGridView;
    private ArrayList<ResolveInfo> mApps;
    private PackageManager pm;
    private MyAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pm = getActivity().getPackageManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_app_selector, container, false);
        mAppGridView = (GridView) rootView.findViewById(R.id.appGridView);

        initApp();
        adapter = new MyAdapter(getContext(), mApps);
        mAppGridView.setAdapter(adapter);
        mAppGridView.setOnItemClickListener(this);
        mAppGridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
//        mAppGridView.setOnItemSelectedListener();
        mAppGridView.setMultiChoiceModeListener(this);

        return rootView;
    }

    /**
     * 初始化app列表
     */
    private void initApp() {
        // 获取android设备的应用列表
        Intent intent = new Intent(Intent.ACTION_MAIN); // 动作匹配
        intent.addCategory(Intent.CATEGORY_LAUNCHER); // 类别匹配
        mApps = (ArrayList<ResolveInfo>) pm.queryIntentActivities(intent, 0);
        Collections.sort(mApps, new Comparator<ResolveInfo>() {
            @Override
            public int compare(ResolveInfo a, ResolveInfo b) {
                return String.CASE_INSENSITIVE_ORDER.compare(a.loadLabel(pm)
                        .toString(), b.loadLabel(pm).toString());
            }
        });
    }

    String application, app_label;

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ResolveInfo app = mApps.get(position);
        try {
            application = pm.getApplicationInfo(app.activityInfo.packageName, 0).sourceDir;
            app_label = app.loadLabel(pm).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        // show dialog
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.dialog_file_selector_send_ack)
                .setMessage(R.string.dialog_file_selector_send_ack_message)
                .setPositiveButton(R.string.dialog_file_selector_send_ack_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int witch) {
                        List<BasicFileInformation> apps = new ArrayList<>();
                        apps.add(new BasicFileInformation(application, app_label + ".apk"));
                        startResult(apps);
                    }
                })
                .setNegativeButton(R.string.dialog_file_selector_send_ack_cancel, null).show();
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        mode.setTitle(getString(R.string.have_selected_count, mAppGridView.getCheckedItemCount()));
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.file_selector_app, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        mode.setTitle(getString(R.string.have_selected_count, mAppGridView.getCheckedItemCount()));
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_send:
                List<BasicFileInformation> apps = new ArrayList<>();
                for (int i = adapter.getCount() - 1; i >= 0; i--) {
                    if (mAppGridView.isItemChecked(i)) {
                        ResolveInfo app = mApps.get(i);
                        try {
                            String appDir = pm.getApplicationInfo(app.activityInfo.packageName, 0).sourceDir;
                            String appLabel = app.loadLabel(pm).toString();
                            apps.add(new BasicFileInformation(appDir, appLabel + ".apk"));
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
                startResult(apps);
                return true;

            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        for (int i = adapter.getCount() - 1; i >= 0; i--) {
            mAppGridView.setItemChecked(i, false);
        }
    }

    private void startResult(List<BasicFileInformation> apps) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("files", (Serializable) apps);
        intent.putExtras(bundle);
        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
    }

    private class MyAdapter extends ArrayAdapter<ResolveInfo> {

        public MyAdapter(Context context, ArrayList<ResolveInfo> apps) {
            super(context, 0, apps);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = getLayoutInflater(null).inflate(R.layout.gridview_item_app, null);
                holder = new ViewHolder();
                holder.appImageView = (ImageView) convertView.findViewById(R.id.appImageView);
                holder.appNameTextView = (TextView) convertView.findViewById(R.id.appNameTextView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            ResolveInfo app = mApps.get(position);
            CharSequence appName = app.loadLabel(pm);
            holder.appNameTextView.setText(appName);
            Drawable appIcon = app.loadIcon(pm);
            holder.appImageView.setImageDrawable(appIcon);
            return convertView;
        }

        private class ViewHolder {
            public ImageView appImageView;
            public TextView appNameTextView;
        }
    }
}