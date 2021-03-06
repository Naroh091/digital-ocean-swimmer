package com.yassirh.digitalocean.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.yassirh.digitalocean.R;
import com.yassirh.digitalocean.data.DatabaseHelper;
import com.yassirh.digitalocean.data.SizeDao;
import com.yassirh.digitalocean.model.Account;
import com.yassirh.digitalocean.model.Size;
import com.yassirh.digitalocean.utils.ApiHelper;

public class SizeService {

	private Context mContext;
	private boolean mIsRefreshing;
	
	public SizeService(Context context) {
		mContext = context;
	}

	public void getAllSizesFromAPI(final boolean showProgress){
		Account currentAccount = ApiHelper.getCurrentAccount(mContext);
		if(currentAccount == null){
			return;
		}			
		mIsRefreshing = true;
		String url = "https://api.digitalocean.com/sizes/?client_id=" + currentAccount.getClientId() + "&api_key=" + currentAccount.getApiKey(); 
		AsyncHttpClient client = new AsyncHttpClient();
		client.get(url, new AsyncHttpResponseHandler() {
			NotificationManager mNotifyManager;
			NotificationCompat.Builder mBuilder;
			
			@Override
			public void onStart() {
				if(showProgress){
					mNotifyManager =
					        (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
					mBuilder = new NotificationCompat.Builder(mContext);
					mBuilder.setContentTitle(mContext.getResources().getString(R.string.synchronising))
					    .setContentText(mContext.getResources().getString(R.string.synchronising_sizes))
					    .setSmallIcon(R.drawable.ic_launcher);
					mBuilder.setContentIntent(PendingIntent.getActivity(mContext,0,new Intent(),PendingIntent.FLAG_UPDATE_CURRENT));
					mNotifyManager.notify(NotificationsIndexes.NOTIFICATION_GET_ALL_SIZES, mBuilder.build());
				}
			}
			
			@Override
			public void onFinish() {
				mIsRefreshing = false;
				if(showProgress){
					mNotifyManager.cancel(NotificationsIndexes.NOTIFICATION_GET_ALL_SIZES);
				}
			}
			
			@Override
			public void onProgress(int bytesWritten, int totalSize) {
				if(showProgress){
					mBuilder.setProgress(100, (int)100*bytesWritten/totalSize, false);
					mNotifyManager.notify(NotificationsIndexes.NOTIFICATION_GET_ALL_SIZES, mBuilder.build());
				}
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				if(statusCode == 401){
					Toast.makeText(mContext, R.string.access_denied_message, Toast.LENGTH_SHORT).show();
				}
			}
			
		    @Override
		    public void onSuccess(String response) {
		        try {
					JSONObject jsonObject = new JSONObject(response);
					String status = jsonObject.getString("status");
					List<Size> sizes = new ArrayList<Size>();
					if(ApiHelper.API_STATUS_OK.equals(status)){
						JSONArray sizeJSONArray = jsonObject.getJSONArray("sizes");
						for(int i = 0; i < sizeJSONArray.length(); i++){
							JSONObject regionJSONObject = sizeJSONArray.getJSONObject(i);
							Size size = new Size();
							size.setId(regionJSONObject.getLong("id"));
							size.setName(regionJSONObject.getString("name"));
							if(regionJSONObject.getString("slug").equals("null"))
								size.setSlug("");
							else
								size.setSlug(regionJSONObject.getString("slug"));
							size.setMemory(regionJSONObject.getInt("memory"));
							size.setCpu(regionJSONObject.getInt("cpu"));
							size.setDisk(regionJSONObject.getInt("disk"));
							size.setCostPerHour(regionJSONObject.getDouble("cost_per_hour"));
							size.setCostPerMonth(regionJSONObject.getDouble("cost_per_month"));
							sizes.add(size);
						}
						SizeService.this.deleteAll();
						SizeService.this.saveAll(sizes);
						SizeService.this.setRequiresRefresh(true);
					}
					else{
						// TODO handle error Access Denied/Not Found
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}  
		    }
		});
	}

	protected void saveAll(List<Size> sizes) {
		SizeDao sizeDao = new SizeDao(DatabaseHelper.getInstance(mContext));
		for (Size size : sizes) {
			sizeDao.create(size);
		}
	}
	
	public List<Size> getAllSizes(String orderBy){
		SizeDao sizeDao = new SizeDao(DatabaseHelper.getInstance(mContext));
		List<Size> sizes = sizeDao.getAll(orderBy);
		return sizes;
	}

	public void deleteAll() {
		SizeDao sizeDao = new SizeDao(DatabaseHelper.getInstance(mContext));
		sizeDao.deleteAll();
	}

	public void setRequiresRefresh(Boolean requireRefresh){
		SharedPreferences settings = mContext.getSharedPreferences("prefrences", 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("size_require_refresh", requireRefresh);
		editor.commit();
	}
	public Boolean requiresRefresh(){
		SharedPreferences settings = mContext.getSharedPreferences("prefrences", 0);
		return settings.getBoolean("size_require_refresh", true);
	}

	public boolean isRefreshing() {
		return mIsRefreshing;
	}
}
