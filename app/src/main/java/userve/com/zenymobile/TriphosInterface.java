package userve.com.zenymobile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.webkit.WebView;


public class TriphosInterface  {
	Context mContext = null;
	WebView mWebView = null;
	MyProgressDialog dialog = null;
	
	public void TriphosInterface_init (WebView appView,Context context,MyProgressDialog dialog) {
		mWebView = appView;
		mContext = context;
		dialog = this.dialog;
	}

	Handler pageHandler = new Handler() 
	{
		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);
		}
	}; 
	

	public void LoadingStart()
	{
		System.out.println(">>>>>>>>>LoadingStart");
		new Thread(new Runnable() { 
			@Override
			public void run() {    
				((Activity) mContext).runOnUiThread(new Runnable(){
					@Override
					public void run() {
						//if(dialog != null)dialog.show();
						dialog = MyProgressDialog.show(mContext, "", "", true, true,
								null);
					}
				});
			}
		}).start();
	
	}
	public void LoadingStop()
	{
		System.out.println(">>>>>>>>>LoadingStop");
		new Thread(new Runnable() { 
			@Override
			public void run() {    
				((Activity) mContext).runOnUiThread(new Runnable(){
					@Override
					public void run() {
						if(dialog != null)dialog.dismiss();
					}
				});
			}
		}).start();
	
	}
	int backCnt = 0;
	public void finish()
	{
		((Activity)mContext).finish();
	}
	
	
}