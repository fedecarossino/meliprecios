package com.calcular;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.imageloader.ImageLoader;
import com.imageloader.LazyAdapter;
import com.meliprecios.R;

public class calcularActivity extends Activity{

	private AdView adView;
	HashMap<String, String> mapCategory = new HashMap<String, String>();
	private String MELI_URL = "http://wabila.com/getMeliPrecios";
	Button calcular;
	TextView itemInput, medianaCalculadaImput, itemCalculadoInput, textPrecioMedio;
	String mediana;
	String item;
	ListView list;
	private ProgressDialog pDialog;
	private boolean haveCategory = false;
	String category = "";
	JSONParser jsonParser = new JSONParser();
	JSONObject json = new JSONObject();
	JSONArray jsonCategories = new JSONArray();
	String condition = "";
	LazyAdapter adapter;
	String siteId = "";
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		calcular=(Button)findViewById(R.id.button1);
		list=(ListView)findViewById(R.id.list);
		
		adView = ( AdView) findViewById(R.id.adView);
		adView.loadAd(new AdRequest());
		
		medianaCalculadaImput = (TextView) findViewById(R.id.precioMedianaCalculado);
		itemCalculadoInput = (TextView) findViewById(R.id.itemTextSelect);
		textPrecioMedio= (TextView) findViewById(R.id.precioMediana);
		
		setSiteId();
		
        calcular.setOnClickListener(new OnClickListener() {

    	   @Override
    	    public void onClick(View view) {
    		   	itemInput = (TextView) findViewById(R.id.itemInput);
    		   	if(!itemInput.getText().toString().equals("")){
    		   		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    		   		imm.hideSoftInputFromWindow(itemInput.getApplicationWindowToken(), 0);
    		   		
    		   		itemCalculadoInput.setText(itemInput.getText().toString());
    		   		textPrecioMedio.setText("Precio Promedio");
    		   		
    		   		final AlertDialog.Builder menuAleart = new  AlertDialog.Builder(calcularActivity.this);
    		   		final String[] menuList = { "Nuevo", "Usado" };
    		   		menuAleart.setTitle("Articulo");
    		   		menuAleart.setItems(menuList,new DialogInterface.OnClickListener() {
    		   		 public void onClick(DialogInterface dialog, int item) {
    		   		  switch (item) {
    		   		  case 0:
    		   			condition = "new";
    		   		  break;
    		   		  case 1:
    		   			condition = "used";
    		   		  break;
    		   		  }
    		   		  new LoadPrice().execute();
    		   		 }
    		   		});
    		   		AlertDialog menuDrop = menuAleart.create();
    		   		menuDrop.show();
    		   	}
    		    adView = ( AdView) findViewById(R.id.adView);
    		    adView.loadAd(new AdRequest());
    	    }
	    });
        
        list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				JSONArray auxItems;
				try {
					auxItems = json.getJSONArray("items");
					JSONObject urlAuxJson = (JSONObject) auxItems.get(position);
					Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse(urlAuxJson.getString("permalink")));
					startActivity(viewIntent);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});	
	}
	
    private void setSiteId() {
    	TelephonyManager tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String networkOperator = tel.getNetworkOperator();
		int mcc = Integer.parseInt(networkOperator.substring(0, 3));
		
		if(mcc == 722){
			siteId = "MLA";//Argentina
		}else if(mcc == 724){
			siteId = "MLB";//Brasil
		}else if(mcc == 734){
			siteId = "MLV";//Venezuela
		}else if(mcc == 730){
			siteId = "MLC";//Chile
		}else if(mcc == 748){
			siteId = "MLU";//Uruguay
		}else if(mcc == 334){
			siteId = "MLM";//Mexico
		}else if(mcc == 732){
			siteId = "MCO";//Colombia
		}else if(mcc == 268){
			siteId = "MPT";//Portugal
		}else if(mcc == 716){
			siteId = "MPE";//Peru
		}else if(mcc == 714){
			siteId = "MPA";//Panama
		}else if(mcc == 740){
			siteId = "MEC";//Ecuador
		}else if(mcc == 712){
			siteId = "MCR";//Costa Rica
		}else if(mcc == 370 || mcc == 366){
			siteId = "MRD";//Republica Dominicana
		}
	}

	public void onDestroy() {
        // Destroy the AdView.
        adView.destroy();
        super.onDestroy();
    }
    
    class LoadPrice extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(calcularActivity.this);
			pDialog.setMessage("Loading ...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		/**
		 * getting Inbox JSON
		 * */
		protected String doInBackground(String... args) {
			try {
				getPriceMedium();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				pDialog.dismiss();
				e.printStackTrace();
			}
			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String file_url) {
			if(haveCategory){
				pDialog.dismiss();
				ArrayList aux = new ArrayList();
				
				for(int i=0; i < jsonCategories.length(); i++){
					try {
						JSONObject auxJson = (JSONObject) jsonCategories.get(i);
						aux.add(auxJson.getString("name"));
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				String[] menuList = (String[]) aux.toArray(new String[aux.size()]);
				final AlertDialog.Builder menuAleart = new AlertDialog.Builder(calcularActivity.this);
				menuAleart.setTitle("Categoria");
				menuAleart.setItems(menuList,new DialogInterface.OnClickListener() {
				 public void onClick(DialogInterface dialog, int item) {
					 try {
						JSONObject auxJson = (JSONObject) jsonCategories.get(item);
						category = auxJson.getString("id");
						
						new LoadPrice().execute();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				 }
				});
				AlertDialog menuDrop = menuAleart.create();
				menuDrop.show();
			}else{
				try {
					setLazyAdapter();
					list.setAdapter(adapter);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			haveCategory=false;
			pDialog.dismiss();
		}

	}
    
    private void setLazyAdapter() throws JSONException{
    	itemInput.setText("");
    	if(json.getJSONArray("items").length() > 0){
    		medianaCalculadaImput.setText(mediana);
    		Random r = new Random();
    		ImageView imagePrincipal=(ImageView) findViewById(R.id.imageView1);
    		ImageLoader imageLoader=new ImageLoader(getApplicationContext());
    		JSONArray aux = json.getJSONArray("items");
    		int jsonLenght = aux.length()-1;
    		JSONObject auxJson = (JSONObject) aux.get(r.nextInt(jsonLenght));
    		imageLoader.DisplayImage(auxJson.getString("thumbnail"), imagePrincipal);
    		adapter=new LazyAdapter(this, json.getJSONArray("items"));
    	}else{
    		ImageView imagePrincipal=(ImageView) findViewById(R.id.imageView1);
    		imagePrincipal.setImageDrawable(null);
    		medianaCalculadaImput.setText(json.getString("message"));
    		textPrecioMedio.setText("");
    	}
    }
    
    private void getPriceMedium() throws UnsupportedEncodingException{
    	itemInput = (TextView) findViewById(R.id.itemInput);
    	String URL = MELI_URL+"?item="+URLEncoder.encode(itemInput.getText().toString(), "utf-8")+"&site="+siteId+"&condition="+condition+"&category="+category;
    	json = jsonParser.makeHttpRequest(URL, "GET", null);
    	category = "";
    	try {
    		json = json.getJSONObject("data");
			if(json.getJSONArray("category") != null){
				haveCategory = true;
				jsonCategories = json.getJSONArray("category");
			}
		} catch (JSONException e) {
			try {
				mediana = json.getString("mediana");
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	
    	
    }
}
