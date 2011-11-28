package org.societies.clientframework.devicestatus;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

/**
 * @author olivierm
 * @date 28 nov. 2011
 */
public class DeviceSatusActivity extends Activity {
	private String TAG = "DeviceSatusActivity";

	private TextView txtConnectivity;
	private TextView txtBattery;
	private TextView txtLocation;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		txtConnectivity = (TextView) findViewById(R.id.txtConnectivity);
		txtBattery = (TextView) findViewById(R.id.txtBattery);
		txtLocation = (TextView) findViewById(R.id.txtLocation);

		updateConnectivity();
		updateBattery();
		updateLocation();
	}

	public void updateConnectivity() {
		StringBuffer sb = new StringBuffer();
		ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		// -- Internet enabled?
		sb.append("** Internet is enabled? "+(isInternetEnabled(connectivity) ? "yes" : "no")+"\n");

		// --- Mobile, Wifi, Wimax, ...
		NetworkInfo[] networkInfos = connectivity.getAllNetworkInfo();
		int length = networkInfos.length;
		for (int i=0; i<length; i++) {
			NetworkInfo networkInfo = networkInfos[i];
			sb.append("** "+networkInfo.getTypeName()+" ["+networkInfo.getSubtypeName()+"]\n");
			if (networkInfo.getState().equals(NetworkInfo.State.UNKNOWN)) {
				sb.append("Not available\n");
			}
			else {
				sb.append("State: "+networkInfo.getState()+" ["+networkInfo.getDetailedState()+"]\n");
				sb.append("Reason: "+networkInfo.getReason()+"\n");
				sb.append("Extra info: "+networkInfo.getExtraInfo()+"\n");
			}
		}

		// --- BLUETOUTH
		sb.append("** Blutouth\n");
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null) {
			sb.append("Not available\n");
		}
		else if(!bluetoothAdapter.isEnabled()) {
			sb.append("Disabled\n");
		}
		else {
			sb.append("Enabled\n");
			Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
			// If there are paired devices
			if (pairedDevices.size() > 0) {
				sb.append("Device available: "+pairedDevices.size()+"\n");
				// Loop through paired devices
				for (BluetoothDevice device : pairedDevices) {
					// Add the name and address to an array adapter to show in a ListView
					sb.append(device.getName()+": "+device.getAddress()+"\n");
				}
			}
			else {
				sb.append("0 device available\n");
			}
		}

		// -- Add these data to the text
		txtConnectivity.setText(sb.toString());
	}

	public boolean isInternetEnabled(ConnectivityManager connectivity) {
		return (connectivity.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED ||
				connectivity.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTING ||
				connectivity.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTING ||
				connectivity.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED);
	}


	public void updateBattery() {
		// launch a broadcast receiver to be inform of battery status
		BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver() {
			double level = -1;
			int scale = -1;
			double voltage = -1;
			double temperature = -1;
			int status = -1;
			int plugged = -1;
			public void onReceive(Context context, Intent intent) {
				context.unregisterReceiver(this);
				int rawLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
				scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
				int rawTemperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
				int rawVoltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
				status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
				plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

				if (rawLevel >= 0 && scale > 0) {
					level = (rawLevel * 100) / scale;
				}
				if (rawTemperature >= 0) {
					temperature = rawTemperature/10;
				}
				if (rawVoltage >= 0) {
					voltage = rawVoltage/1000;
				}
				Log.e(TAG, "Battery status > level: "+level+"% (="+rawLevel+"/"+scale+"), temperature: "+temperature+"°C (="+rawTemperature+"), voltage: "+voltage+"V (="+rawVoltage+"mV)");
				StringBuffer sb = new StringBuffer();
				sb.append("Remaining level: "+level+"%\n" +
						"Temperature: "+temperature+"°C\n" +
						"Voltage: "+voltage+"V\n"+
						"Status: ");
				
				switch(status) {
				case BatteryManager.BATTERY_STATUS_CHARGING:
					sb.append("charging");
					break;
				case BatteryManager.BATTERY_STATUS_DISCHARGING:
					sb.append("discharging");
					break;
				case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
					sb.append("not charging");
					break;
				case BatteryManager.BATTERY_STATUS_FULL:
					sb.append("full");
					break;
				default: sb.append("unknown");
				}
				sb.append(pluggedOn(plugged));
				sb.append("\n");
				
				txtBattery.setText(sb.toString());
			}
		};
		IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(batteryLevelReceiver, batteryLevelFilter);
	}

	public String pluggedOn(int plugged) {
		switch(plugged) {
		case BatteryManager.BATTERY_PLUGGED_AC:
			return ", plugged on AC";
		case BatteryManager.BATTERY_PLUGGED_USB:
			return ", plugged on USB";
		default:
			return ", not plugged";
		}
	}
	public void updateLocation() {
		StringBuffer sb = new StringBuffer();

		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		List<String> providers = locationManager.getAllProviders();
		sb.append("Providers:\n");
		for (String provider : providers) {
			sb.append("* "+provider+" ["+(locationManager.isProviderEnabled(provider) ? "enabled" : "disabled")+"]\n");
		}
		// -- Add these data to the text
		txtLocation.setText(sb.toString());
	}
}
