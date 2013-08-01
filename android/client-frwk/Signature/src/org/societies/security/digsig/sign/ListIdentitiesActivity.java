package org.societies.security.digsig.sign;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import org.societies.security.digsig.trust.AndroidSecureStorage;

import org.societies.security.digsig.R;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;

public class ListIdentitiesActivity extends ListActivity {
	private AndroidSecureStorage secureStorage;
	private CertificateFactory certFactory;
		
	private ArrayList<String> certNames;
	private ArrayList<Integer> certNumbers;
	
	private final static int UNLOCK_AND_LIST_IDENTITY = 3;	
	private static final String UNLOCK_ACTION = "android.credentials.UNLOCK";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		secureStorage = AndroidSecureStorage.getInstance();
		try {
			certFactory = CertificateFactory.getInstance("X.509");
		} catch (CertificateException e) {}
		        	
		
		if (secureStorage.test()!= AndroidSecureStorage.NO_ERROR) {
			Intent intent = new Intent(UNLOCK_ACTION);	
		    startActivityForResult(intent, UNLOCK_AND_LIST_IDENTITY);		    
			return;
		}
		
		setList();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == UNLOCK_AND_LIST_IDENTITY) {
			setList();
		}
	}
	
	
	private void setList() {
		if (secureStorage.test() != AndroidSecureStorage.NO_ERROR) return;
		
		certNames = new ArrayList<String>();
		certNumbers = new ArrayList<Integer>();
		
		int count = 0;
		while (true) {
			String certKey = String.format("CERT_%d", count);
			String keyKey = String.format("KEY_%d", count++);
			
			if (!secureStorage.contains(certKey) && !secureStorage.contains(keyKey)) break;
			
			// Add cert to list
			byte[] encodedCert = secureStorage.getWithStringKey(certKey);					
			
			if (encodedCert==null) continue;
			
			X509Certificate cert = null;
			try {
				cert = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(encodedCert));
			} catch (CertificateException e) {
				Log.e("miki",e.getMessage());
			}
			if (cert==null) continue;
			
			certNames.add(cert.getSubjectDN().toString());
			certNumbers.add(count-1);
		}
		
		setListAdapter(new ArrayAdapter<String>(this, R.layout.file_list, certNames));	
				
		getListView().setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long itemPos) {				
				Intent intent = getIntent();
				intent.putExtra("SELECTED", certNumbers.get((int)itemPos));				
				setResult(RESULT_OK,intent);
				finish();				
			}
		});
	}	
}
