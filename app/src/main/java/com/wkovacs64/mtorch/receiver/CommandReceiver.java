package com.wkovacs64.mtorch.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

/**
 * A simple forwarding BroadcastReceiver that receives filtered broadcasts from {@link
 * com.wkovacs64.mtorch.service.TorchService} (which runs in its own process) and sends them on to
 * {@link com.wkovacs64.mtorch.ui.activity.MainActivity} via the LocalBroadcastManager.
 */
public class CommandReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
