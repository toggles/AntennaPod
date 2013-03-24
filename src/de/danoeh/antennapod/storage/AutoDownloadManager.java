/*
package de.danoeh.antennapod.storage;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import de.danoeh.antennapod.AppConfig;
import de.danoeh.antennapod.feed.FeedItem;
import de.danoeh.antennapod.feed.FeedManager;

import android.content.Context;
import android.util.Log;

public class AutoDownloadManager {
	private static final String TAG = "AutoDownloadManager";
	
	public enum Mode {
		FileCount,
		TotalSize,
	}

	private String path = null;
	private long downloadsCount;
	private long downloadsSize;
	private long maxCount = 40;
	private Mode mode = Mode.FileCount;
	private Context application;
	private final int maxDownloads = 5;
	
	public AutoDownloadManager(Context context) {
		application=context.getApplicationContext();
		File file=context.getExternalFilesDir(DownloadRequester.MEDIA_DOWNLOADPATH);
		this.path=file.getAbsolutePath();
		if(AppConfig.DEBUG)
			Log.i(TAG,"Created with path "+this.path);
	}
	
	public void queue() {
		// scan the already downloaded files, for count && size
		scan();
		switch(mode) {
		case TotalSize:
			// not implemented...
			//break;
		case FileCount:
			int count=(int)(maxCount-downloadsCount);
			if(count>0) {
                // only download a few at a time
                if(count>maxDownloads) {
                    count=maxDownloads;
                }
                // enque the files for download
				enqueFiles(count);
			}
			break;
		}
	}
	
	public String getPath() {
		return(path);
	}
	
	public void setPath(String path) {
		this.path=path;
	}
	
	public long getDownloadsCount() {
		return(downloadsCount);
	}
	
	public long getDownloadsSize() {
		return(downloadsSize);
	}
	
	private void count(String path) {
		File file=new File(path);
		
		if(file.isDirectory()) {
			String files[]=file.list();
			for(String check:files) {
				count(path+"/"+check);
			}
		}
		else if(file.isFile()) {
			++downloadsCount;
			downloadsSize+=file.length();
		}
	}
	
	public void scan() {
		Date start,end;
		downloadsCount=downloadsSize=0;
		start=new Date();
		count(this.path);
		end=new Date();
		if(AppConfig.DEBUG)
			Log.i(TAG,"Found "+downloadsCount+" files, using "+(downloadsSize/1024/1024)+ "MB. "
					 +"Scan took "+(end.getTime()-start.getTime())+"ms");
	}
	
	public List<FeedItem> copyAndSort(List<FeedItem> src) {
		List<FeedItem> list=new ArrayList<FeedItem>();
		
		for(FeedItem feed:src) {
			list.add(feed);
		}
		// sort list by priority
        Collections.sort(list,new FeedManager.QueuePrioritySort());
		return(list);
	}
	
	public void enqueFiles(int n) {
		final List<FeedItem> enqueued=new ArrayList<FeedItem>();
        // clone the list as we will be passing it to another thread
		List<FeedItem> unread=copyAndSort(FeedManager.getInstance().getUnreadItems());
		Iterator<FeedItem> i=unread.iterator();
		
		for(int count=0;i.hasNext()&&count<n;++count) {
			FeedItem item=i.next();
			enqueued.add(item);
		}
		// on the UI thread, try and enqueue the new feeds
		FeedManager.postToUI(new Runnable() {
				@Override
				public void run() {
					FeedManager manager=FeedManager.getInstance();
					for(FeedItem item:enqueued) {
						try {
							manager.downloadFeedItem(application,item);
						} catch (DownloadRequestException e) {
							Log.e(TAG,"Error downloading feed item "+item.getTitle());
						}
					}
				}
			});
	}
	
}
*/

package de.danoeh.antennapod.storage;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import de.danoeh.antennapod.AppConfig;
import de.danoeh.antennapod.feed.Feed;
import de.danoeh.antennapod.feed.FeedItem;
import de.danoeh.antennapod.feed.FeedManager;
import de.danoeh.antennapod.feed.FeedMedia;
import de.danoeh.antennapod.feed.FeedManager.QueuePrioritySort;

import android.content.Context;
import android.util.Log;

public class AutoDownloadManager {
	private static final String TAG = "AutoDownloadManager";
	
	public enum Mode {
		FileCount,
		TotalSize,
	}

	private String path = null;
	private long downloadsCount;
	private long downloadsSize;
	private long maxCount = 40;
	private Mode mode = Mode.FileCount;
	private Context application;
	private final int maxQueuedDownloads = 5;
	
	public AutoDownloadManager(Context context) {
		application=context.getApplicationContext();
		File file=context.getExternalFilesDir(DownloadRequester.MEDIA_DOWNLOADPATH);
		this.path=file.getAbsolutePath();
		if(AppConfig.DEBUG)
			Log.i(TAG,"Created with path "+this.path);
	}
	
	public void queue() {
		// scan the already downloaded files, for count && size
		scan();
		// queue the files
		switch(mode) {
		case TotalSize:
			// not implemented...
			break;
		case FileCount:
			int count=(int)(maxCount-downloadsCount);
			if(count>0) {
				enqueFiles(count);
			}
			break;
		}
	}
	
	public String getPath() {
		return(path);
	}
	
	public void setPath(String path) {
		this.path=path;
	}
	
	public long getDownloadsCount() {
		return(downloadsCount);
	}
	
	public long getDownloadsSize() {
		return(downloadsSize);
	}
	
	private void count(String path) {
		File file=new File(path);
		
		if(file.isDirectory()) {
			String files[]=file.list();
			for(String check:files) {
				count(path+"/"+check);
			}
		}
		else if(file.isFile()) {
			++downloadsCount;
			downloadsSize+=file.length();
		}
	}
	
	public void scan() {
		Date start,end;
		downloadsCount=downloadsSize=0;
		start=new Date();
		count(this.path);
		end=new Date();
		if(AppConfig.DEBUG)
			Log.i(TAG,"Found "+downloadsCount+" files, using "+(downloadsSize/1024/1024)+ "MB. "
					 +"Scan took "+(end.getTime()-start.getTime())+"ms");
	}
	
	public List<FeedItem> copyAndSort() {
		FeedManager manager=FeedManager.getInstance();
		List<FeedItem> list=new ArrayList<FeedItem>();
		
		// in queue but not downloaded
		for(FeedItem feed:manager.getQueue()) {
			FeedMedia media=feed.getMedia();
			if(media!=null) {
				if(!media.isDownloaded()) {
					media=null;  // force download
				}
			}
			// no media, then download
			if(media==null) {
				list.add(feed);
			}
		}
		// unread
		for(FeedItem feed:manager.getUnreadItems()) {
			list.add(feed);
		}
		// sort by priority
		Collections.sort(list,new QueuePrioritySort());
		return(list);
	}
	
	public void enqueFiles(int n) {
		final List<FeedItem> enqueued=new ArrayList<FeedItem>();
		List<FeedItem> unread=copyAndSort();
		
		Log.i(TAG,"Requested "+n+" downloads.");
		// limit max downloads at a time
		if(n>maxQueuedDownloads) {
			n=maxQueuedDownloads;
		}
		for(Iterator<FeedItem> i=unread.iterator();
				i.hasNext()&&enqueued.size()<n;) {
			FeedItem item=i.next();
			Feed feed=item.getFeed();
			// only download feeds with priorities
			if(feed.getPriority()>0) {
				// if we haven't already tried to download this item this session
				if(!item.getDownloadAttempted()) {
					enqueued.add(item);
				}
				else {
					Log.i(TAG,"Already attempted download of "+item.getId()+":"
				            +item.getFeed().getTitle()+":"+item.getTitle());
				}
			}
		}
		// on the UI thread, try and enqueue the new feeds
		FeedManager.postToUI(new Runnable() {
				@Override
				public void run() {
					FeedManager manager=FeedManager.getInstance();
					for(FeedItem item:enqueued) {
						try {
							Log.i(TAG,"Requesting download of "+item.getId()+":"
						            +item.getFeed().getTitle()+":"+item.getTitle());
							manager.downloadFeedItem(application,item);
							manager.markItemRead(application, item, true, false);
							// to stop failed downloads being attempted again
							item.setDownloadAttempted();
						} catch (DownloadRequestException e) {
							Log.e(TAG,"Error downloading feed item "+item.getTitle());
						}
					}
				}
			});
	}
	
}