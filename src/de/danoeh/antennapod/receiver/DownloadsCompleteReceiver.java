package de.danoeh.antennapod.receiver;

import de.danoeh.antennapod.AppConfig;
import de.danoeh.antennapod.feed.FeedManager;
import de.danoeh.antennapod.storage.AutoDownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DownloadsCompleteReceiver extends BroadcastReceiver {
	private static final String TAG = "DownloadsCompleteReceiver";
	public static final String ACTION_DOWNLOADS_COMPLETE = "de.danoeh.antennapod.receiver.DownloadsCompleteReceiver";
	
	@Override
	public void onReceive(final Context context, Intent intent) {
		if (AppConfig.DEBUG) 
			Log.d(TAG, "Received intent");
		
		// start the auto downloader
		(new Thread(new Runnable() {

			@Override
			public void run() {
				// make sure the db is loaded
				FeedManager manager=FeedManager.getInstance();

				// try and download some more podcasts!
				AutoDownloadManager download=new AutoDownloadManager(context);
				download.queue();
			}
			
		})).start();
	}

}

