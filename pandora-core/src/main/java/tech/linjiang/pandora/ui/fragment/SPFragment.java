package tech.linjiang.pandora.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import tech.linjiang.pandora.Pandora;
import tech.linjiang.pandora.core.R;
import tech.linjiang.pandora.ui.connector.EditCallback;
import tech.linjiang.pandora.ui.item.KeyValueItem;
import tech.linjiang.pandora.ui.item.TitleItem;
import tech.linjiang.pandora.ui.recyclerview.BaseItem;
import tech.linjiang.pandora.ui.recyclerview.UniversalAdapter;
import tech.linjiang.pandora.util.SimpleTask;
import tech.linjiang.pandora.util.Utils;

/**
 * Created by linjiang on 05/06/2018.
 */

public class SPFragment extends BaseListFragment {

    private File descriptor;
    private String clickKey;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        descriptor = (File) getArguments().getSerializable(PARAM1);
        getToolbar().setTitle(descriptor.getName());

        getAdapter().setListener(new UniversalAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, BaseItem item) {
                if (item instanceof KeyValueItem) {
                    if (((KeyValueItem) item).isTitle) {
                        return;
                    }
                    clickKey = ((KeyValueItem) item).data[0];
                    Bundle bundle = new Bundle();
                    bundle.putString(PARAM1, ((KeyValueItem) item).data[1]);
                    bundle.putSerializable(PARAM2, callback);
                    launch(EditFragment.class, bundle);
                }
            }
        });

        loadData();
    }

    private void loadData() {
        Map<String, String> contents = Pandora.get().getSharedPref().getSharedPrefContent(descriptor);
        if (contents != null && !contents.isEmpty()) {
            List<BaseItem> data = new ArrayList<>();
            data.add(new TitleItem(String.format(Locale.getDefault(), "%d ITEMS", contents.size())));
            data.add(new KeyValueItem(new String[]{"KEY", "VALUE"}, true));
            for (Map.Entry<String, String> entry : contents.entrySet()) {
                data.add(new KeyValueItem(new String[]{entry.getKey(), entry.getValue()}, false, true));
            }
            getAdapter().setItems(data);

        } else {
            showError(null);
        }
    }

    private EditCallback callback = new EditCallback() {
        @Override
        public void onValueChanged(final String value) {
            if (!TextUtils.isEmpty(clickKey)) {

                new SimpleTask<>(new SimpleTask.Callback<Void, String>() {
                    @Override
                    public String doInBackground(Void[] params) {
                        return Pandora.get().getSharedPref().updateSharedPref(descriptor, clickKey, value);
                    }

                    @Override
                    public void onPostExecute(String result) {
                        hideLoading();
                        if (TextUtils.isEmpty(result)) {
                            Utils.toast(R.string.pd_success);
                        } else {
                            Utils.toast(result);
                        }
                        loadData();
                    }
                }).execute();
                showLoading();
            }
        }
    };
}