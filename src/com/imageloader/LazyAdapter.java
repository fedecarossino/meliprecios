package com.imageloader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.meliprecios.R;

public class LazyAdapter extends BaseAdapter {
    
    private Activity activity;
    private JSONArray data;
    private static LayoutInflater inflater=null;
    public ImageLoader imageLoader; 
    
    public LazyAdapter(Activity a, JSONArray d) {
        activity = a;
        data=d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader=new ImageLoader(activity.getApplicationContext());
    }

    public int getCount() {
        return data.length();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.item_row, null);

        try {
	        JSONObject jsonData = (JSONObject) data.get(position);
//	        ImageView imagePrincipal=(ImageView)vi.findViewById(R.id.imageView1);
	        
	        TextView title=(TextView)vi.findViewById(R.id.row_item_title);
	        ImageView image=(ImageView)vi.findViewById(R.id.row_item_image);
	        TextView subtitle = (TextView) vi.findViewById(R.id.row_item_subtitle);
	        TextView price = (TextView) vi.findViewById(R.id.row_item_price);

	        imageLoader.DisplayImage(jsonData.getString("thumbnail"), image);
	        title.setText(jsonData.getString("title"));
	        subtitle.setText(jsonData.getString("sold_quantity"));
	        price.setText(jsonData.getString("price"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return vi;
    }
}