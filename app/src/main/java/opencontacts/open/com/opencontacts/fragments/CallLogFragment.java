package opencontacts.open.com.opencontacts.fragments;

import static opencontacts.open.com.opencontacts.utils.AndroidUtils.getBoolean;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.processAsync;
import static opencontacts.open.com.opencontacts.utils.DomainUtils.removeAnyMissedCallNotifications;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import opencontacts.open.com.opencontacts.CallLogListView;
import opencontacts.open.com.opencontacts.activities.MainActivity;
import opencontacts.open.com.opencontacts.interfaces.EditNumberBeforeCallHandler;
import opencontacts.open.com.opencontacts.interfaces.SelectableTab;
import opencontacts.open.com.opencontacts.utils.SharedPreferencesUtils;

public class CallLogFragment extends AppBaseFragment implements SelectableTab {
    private CallLogListView callLogListView;
    private EditNumberBeforeCallHandler editNumberBeforeCallHandler;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("G&S","Modificato-setOnEnteringMultiSelectMode");
        Log.i("G&S","Modificato-setOnExitingMultiSelectMode");
        callLogListView = new CallLogListView(getContext(), editNumberBeforeCallHandler);
        callLogListView.onEnteringMultiSelectMode =  () -> ((MainActivity) getActivity()).hideBottomMenu();
        callLogListView.onExitingMultiSelectMode = () -> ((MainActivity) getActivity()).showBottomMenu();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final Context context = getContext();
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.addView(callLogListView);
        return linearLayout;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i("G&S","Modificato-shouldAutoCancelMissedCallNotification");
        if( getBoolean(SharedPreferencesUtils.SHOULD_AUTO_CANCEL_MISSED_CALL_NOTIF_SHARED_PREF_KEY, false, getContext())) processAsync(() -> removeAnyMissedCallNotifications(getContext()));
    }

    @Override
    public void onDestroy() {
        callLogListView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onSelect() {
    }

    @Override
    public void onUnSelect() {
        if (callLogListView == null) return;
        callLogListView.exitSelectionMode();
    }

    @Override
    public boolean handleBackPress() {
        Log.i("G&S","Modificato-isInSelectionMode");
        if (!callLogListView.inSelectionMode) return false;
        callLogListView.exitSelectionMode();
        return true;
    }

    public void setEditNumberBeforeCallHandler(EditNumberBeforeCallHandler editNumberBeforeCallHandler) {
        this.editNumberBeforeCallHandler = editNumberBeforeCallHandler;
        if (callLogListView != null){
            Log.i("G&S","Modificato-setEditNumberBeforeCallHandler");
            callLogListView.editNumberBeforeCallHandler = editNumberBeforeCallHandler;

        }
    }

}
