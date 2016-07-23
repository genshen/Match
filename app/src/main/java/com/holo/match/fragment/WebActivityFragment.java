package com.holo.match.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.holo.m.tools.Tools;
import com.holo.match.R;
import com.holo.web.tools.AndroidAPI;

public class WebActivityFragment extends Fragment {
    AppCompatTextView address;

    public WebActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_web, container, false);
        address = (AppCompatTextView) rootView.findViewById(R.id.http_address);
        AppCompatTextView code = (AppCompatTextView) rootView.findViewById(R.id.code);
        code.setText(getString(R.string.auth_code, AndroidAPI.newCode()));
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        address.setText(getString(R.string.http_address, Tools.getLocalHostIp()));
    }
}
