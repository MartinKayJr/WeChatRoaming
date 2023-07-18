package cn.martinkay.wechatroaming.settings.fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import cn.martinkay.wechatroaming.settings.activity.SettingsUiFragmentHostActivity;

public abstract class BaseSettingFragment extends Fragment {

    private SettingsUiFragmentHostActivity mSettingsHostActivity = null;
    @Nullable
    private String mTitle = null;

    @Nullable
    private String mSubtitle = null;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mSettingsHostActivity = (SettingsUiFragmentHostActivity) requireActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mSettingsHostActivity = null;
    }

    @Nullable
    protected SettingsUiFragmentHostActivity getSettingsHostActivity() {
        return mSettingsHostActivity;
    }

    public void finishFragment() {
        if (mSettingsHostActivity == null) {
            throw new IllegalStateException("mSettingsHostActivity is null, is current fragment attached?");
        }
        mSettingsHostActivity.finishFragment(this);
    }

    @Nullable
    public String getTitle() {
        return mTitle;
    }

    protected void setTitle(@Nullable String title) {
        mTitle = title;
        if (mSettingsHostActivity != null) {
            mSettingsHostActivity.requestInvalidateActionBar();
        }
    }

    @Nullable
    public String getSubtitle() {
        return mSubtitle;
    }

    protected void setSubtitle(@Nullable String title) {
        mSubtitle = title;
        if (mSettingsHostActivity != null) {
            mSettingsHostActivity.requestInvalidateActionBar();
        }
    }

    public boolean doOnBackPressed() {
        return false;
    }

    public void notifyLayoutPaddingsChanged() {
        onLayoutPaddingsChanged();
    }

    /**
     * @deprecated use {@link #doOnCreateView(LayoutInflater, ViewGroup, Bundle)} instead
     */
    @Nullable
    @Deprecated
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return doOnCreateView(inflater, container, savedInstanceState);
    }

    @Nullable
    public View doOnCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                               @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    protected void onLayoutPaddingsChanged() {
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        onLayoutPaddingsChanged();
    }

    public int getLayoutPaddingTop() {
        return mSettingsHostActivity.getLayoutPaddingTop();
    }

    public int getLayoutPaddingBottom() {
        return mSettingsHostActivity.getLayoutPaddingBottom();
    }

    public boolean isWrapContent() {
        return true;
    }


}
