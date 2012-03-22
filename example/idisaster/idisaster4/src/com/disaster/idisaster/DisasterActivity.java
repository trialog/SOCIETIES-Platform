/**
 * Copyright (c) 2011, SOCIETIES Consortium (WATERFORD INSTITUTE OF TECHNOLOGY (TSSG), HERIOT-WATT UNIVERSITY (HWU), SOLUTA.NET 
 * (SN), GERMAN AEROSPACE CENTRE (Deutsches Zentrum fuer Luft- und Raumfahrt e.V.) (DLR), Zavod za varnostne tehnologije
 * informacijske družbe in elektronsko poslovanje (SETCCE), INSTITUTE OF COMMUNICATION AND COMPUTER SYSTEMS (ICCS), LAKE
 * COMMUNICATIONS (LAKE), INTEL PERFORMANCE LEARNING SOLUTIONS LTD (INTEL), PORTUGAL TELECOM INOVAÇÃO, SA (PTIN), IBM Corp., 
 * INSTITUT TELECOM (ITSUD), AMITEC DIACHYTI EFYIA PLIROFORIKI KAI EPIKINONIES ETERIA PERIORISMENIS EFTHINIS (AMITEC), TELECOM 
 * ITALIA S.p.a.(TI),  TRIALOG (TRIALOG), Stiftelsen SINTEF (SINTEF), NEC EUROPE LTD (NEC))
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
 * conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *    disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT 
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.disaster.idisaster;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.content.Intent;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

/**
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

 * 
 */
import android.widget.AdapterView.OnItemClickListener;


/**
 * This activity allows the users to manage the disasters they own and
 * or the disasters they subscribe to.
 * 
 * @author Jacqueline.Floch@sintef.no
 *
 */
public class DisasterActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	// TODO Auto-generated method stub

    	super.onCreate(savedInstanceState);
	// Going to ask CisManager for Cis List:
	ContentResolver cr = getContentResolver();
	
//	Uri uri;
//	Cursor cursor = managedQuery(uri, null, null, null, null);
	
    	
    	setContentView (R.layout.disaster_layout);
    	ListView listView = getListView();
    	
    	// Enable filtering for the contents of the list view.
    	// The filtering logic should be provided
    	// listView.setTextFilterEnabled(true);  
    	
    	
    	// TODO: Get the list from Societies API
    	final String[] CISLIST = new String[] { "Disaster 1", "Disaster 2", "Disaster 3", "Disaster 4",
    			"Disaster 5", "Disaster 6", "Disaster 7", "Disaster 8", "Disaster 9", "Disaster 10", "Disaster 11",
    			"Disaster 12", "Disaster 13", "Disaster 14", "Disaster 15", "Disaster 16", "Disaster 17", "Disaster 18",
    			"Disaster 19", "Disaster 20", "Disaster 21", "Disaster 22", "Disaster 23", "Disaster 24", "Disaster 25",
    			"Disaster 26", "Disaster 27", "Disaster 28", "Disaster 29" };

    	// The Adapter provides access to the data items.
    	// The Adapter is also responsible for making a View for each item in the data set.
    	//  Parameters: Context, Layout for the row, ID of the View to which the data is written, Array of data

    	ArrayAdapter<String> adapter = new ArrayAdapter<String> (this,
    			R.layout.disaster_list_item, R.id.disaster_item, CISLIST);
    	
    	// Assign adapter to ListView
    	listView.setAdapter(adapter);

    	// Add listener for short click.
    	// 
    	listView.setOnItemClickListener(new OnItemClickListener() {
    		public void onItemClick (AdapterView<?> parent, View view,
    			int position, long id) {
    			// Store the selected disaster in preferences
            	iDisasterApplication.getinstance().setDisasterName (CISLIST [position]);

// TODO: Remove code for testing the correct setting of preferences 
    			Toast.makeText(getApplicationContext(),
    				"Click ListItem Number " + (position+1) + " " + CISLIST [position], Toast.LENGTH_LONG)
    				.show();
    			// Start the Home Activity
    			startActivity(new Intent(DisasterActivity.this, HomeActivity.class));
    		}
    	});	

    	// Add listener for long click
    	// listView.setOnItemLongClickListener(new DrawPopup());

    }

/**
 * onCreateOptionsMenu creates the activity menu.
 */
 
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		menu.clear();
		getMenuInflater().inflate(R.menu.disaster_menu, menu);

//		It is possible to set up a variable menu		
//			menu.findItem (R.id....).setVisible(true);

		return true;
	}

/**
 * onOptionsItemSelected handles the selection of an item in the activity menu.
 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.disasterMenuAdd:
			startActivity(new Intent(DisasterActivity.this, NewDisasterActivity.class));
			break;
		default:
			break;
		}
		return true;
	}

/**
	@Override
	public void onListItemClick (ListView l, View v, int pos, long id) {
		if(l.getAdapter().getItemViewType(pos) == SeparatedListAdapter.TYPE_SECTION_HEADER)
		{
			//Pressing a header			
			return;
		}
		Poi p = (Poi) l.getAdapter().getItem(pos);

		if (requestCode == NewPoiActivity.CHOOSE_POI){
			Intent resultIntent = new Intent();
			resultIntent.putExtra(IntentPassable.POI, p);
			setResult( Activity.RESULT_OK, resultIntent );
			finish();
			return;
		}

		if (requestCode == PlanTripTab.ADD_TO_TRIP || requestCode == TripListActivity.ADD_TO_TRIP){

			if(selectedPois == null){				
				selectedPois = new ArrayList<Poi>();
			}
			if(!selectedPois.contains(p)){
				v.setBackgroundColor(0xff9ba7d5);
				selectedPois.add(p);
			}else {
				v.setBackgroundColor(Color.TRANSPARENT);
				selectedPois.remove(p);
			}
			return;
		}


		if (requestCode == SHARE_POI){
			if(sharePois == null){				
				sharePois = new ArrayList<Poi>();
			}
			if(!sharePois.contains(p)){
				v.setBackgroundColor(0xff9ba7d5);
				sharePois.add(p);
			}else {
				v.setBackgroundColor(Color.TRANSPARENT);
				sharePois.remove(p);
			}
			return;
		}

		if (requestCode == DOWNLOAD_POI){

			if(downloadedPois == null){				
				downloadedPois = new ArrayList<Poi>();
			}
			if(!downloadedPois.contains(p)){
				v.setBackgroundColor(0xff9ba7d5);
				downloadedPois.add(p);
			}else {
				v.setBackgroundColor(Color.TRANSPARENT);
				downloadedPois.remove(p);
			}
			return;
		}

		Intent details = new Intent(PlanPoiTab.this, PoiDetailsActivity.class);
		details.putExtra(IntentPassable.POI, p);

		startActivity(details);
	}//onListItemClick
	
*/

/**
 * Show quick actions when the user long-presses an item 
 */
	/**
	final private class DrawPopup implements AdapterView.OnItemLongClickListener {

		public boolean onItemLongClick(AdapterView<?> parent, View v, int pos, long id) {

			final String	s 			= (String) parent.getAdapter().getItem(pos);
			final AdapterView<?> par 	= parent;
			final int	idx				= pos;
			final int[] xy 				= new int[2];
			
			v.getLocationInWindow(xy);

			final Rect rect 		= new Rect(	xy[0], 
					xy[1], 
					xy[0]+v.getWidth(), 
					xy[1]+v.getHeight());

			final QuickActionPopup qa = new QuickActionPopup (DisasterActivity.this, v, rect);

			Drawable mapviewIcon	= res.getDrawable(android.R.drawable.ic_menu_mapmode);
			Drawable directIcon		= res.getDrawable(android.R.drawable.ic_menu_directions);
			Drawable deleteIcon		= res.getDrawable(android.R.drawable.ic_menu_delete);

			// declare quick actions 			
			qa.addItem(deleteIcon, "Delete from tour", new OnClickListener(){

				public void onClick(View view){
					db.deleteFromTrip(trip, trip.getPoiAt(idx));

					trip.removePoi(idx);

					//delete from list
					((PoiAdapter)par.getAdapter()).remove(p);	
					((PoiAdapter)par.getAdapter()).notifyDataSetChanged();
					qa.dismiss();
				}
			});

			qa.addItem(mapviewIcon,	"Show on map",		new OnClickListener(){

				public void onClick(View view){

					Intent showInMap = new Intent(TripListActivity.this, MapsActivity.class);
					ArrayList<Poi> selectedPois = new ArrayList<Poi>();
					selectedPois.add(p);
					showInMap.putParcelableArrayListExtra(IntentPassable.POILIST, selectedPois);

					startActivity(showInMap);
					qa.dismiss();
				}
			});

			qa.addItem(directIcon,	"Get directions",	new OnClickListener(){

				public void onClick(View view){

					//Latitude and longitude for current position
					double slon = userLocation.getLongitude();
					double slat = userLocation.getLatitude();
					//Latitude and longitude for selected poi
					double dlon = p.getGeoPoint().getLongitudeE6()/1E6;
					double dlat = p.getGeoPoint().getLatitudeE6()/1E6;

					Intent navigate = new Intent(TripListActivity.this, NavigateFrom.class);
					navigate.putExtra("slon", slon);
					navigate.putExtra("slat", slat);
					navigate.putExtra("dlon", dlon);
					navigate.putExtra("dlat", dlat);
					startActivity(navigate);

					qa.dismiss();

				}
			});

			qa.show();

			return true;
		}
	}
*/

/**
 * showDialog is used under testing
 */
	private void showDialog () {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(getString(R.string.disasterTestDialog))
				.setCancelable(false)
				.setPositiveButton (getString(R.string.dialogOK), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						return;
				    }
				});
		AlertDialog dialog = builder.create();
		dialog.show();
	}
}