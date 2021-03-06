package com.yassirh.digitalocean.ui;

import java.util.Formatter;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.yassirh.digitalocean.R;
import com.yassirh.digitalocean.model.Size;


public class SizeAdapter extends BaseAdapter {
    
    private List<Size> data;
    private static LayoutInflater inflater=null;
    private boolean mShowPrice;
    public SizeAdapter(Activity activity, List<Size> data,boolean showPrice) {
        this.data=data;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mShowPrice = showPrice;
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return data.get(position);
    }

    public long getItemId(int position) {
    	return data.get(position).getId();
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.size_list_row, null);

        final Size size = data.get(position);
        
        TextView ramcpuTextView = (TextView)vi.findViewById(R.id.ramcpuTextView);
        TextView diskTextView = (TextView)vi.findViewById(R.id.diskTextView);
        TextView pricingTextView = (TextView)vi.findViewById(R.id.pricingTextView);
        
        ramcpuTextView.setText(size.getName() + "/" + size.getCpu() + " CPU");
        diskTextView.setText(size.getDisk() +"GB SSD");
        Formatter formatter = new Formatter();

        pricingTextView.setText(vi.getResources().getString(R.string.monthly) + ": $" + size.getCostPerMonth() +", " + vi.getResources().getString(R.string.hourly) + ": $" + formatter.format("%1.5f", size.getCostPerHour()));
        formatter.close();
        if(!mShowPrice)
        	pricingTextView.setVisibility(View.GONE);
        return vi;
    }
}