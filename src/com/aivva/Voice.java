package com.aivva;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;
import android.annotation.TargetApi;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Instrumentation;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.CameraInfo;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.AlarmClock;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences.Editor;

public class Voice extends Activity implements OnClickListener {

	ImageButton bCom, ibMenu;
	Button b, btorch;
    boolean noSpeechFlag = false;
	EditText etCom;
	static final int check = 111, PICK_CONTACT = 123, BT_REQ_CODE = 12;
	TextView res, res2;
	ListView lv;
	int nApps;
	float brightness, BackLightValue = 0.5f;;
	String results;
	ArrayList<PInfo> alApp = new ArrayList<PInfo>();
	PInfo appTemp;
	private boolean hasFlash;
    Camera camera;
	Parameters params;
	AudioManager am;
	String[] appNameList = new String[300];
	String[] appPackageList = new String[300];
	Pattern addPT, alarmPT, subtractPT, multiplyPT, divisionPT;

	//@TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @Override
	protected void onCreate(Bundle savedInstanceState) throws NullPointerException{
		super.onCreate(savedInstanceState);
        //ibMenu = (ImageButton) findViewById(R.id.ibMenu);
        //ibMenu.setOnClickListener(this);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences getPrefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        boolean fs = getPrefs.getBoolean("checkboxFS", true);
        getPrefs.edit().putBoolean("fUse",true);
        if (fs) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if(Build.VERSION.SDK_INT <= 10 || (Build.VERSION.SDK_INT >= 14 && ViewConfiguration.get(this).hasPermanentMenuKey())){
                Log.d("Has a key","menu");
                setContentView(R.layout.voice);
            }else{
                setContentView(R.layout.voice);
                ibMenu = (ImageButton) findViewById(R.id.ibMenu);
                ibMenu.setOnClickListener(this);
                ibMenu.setVisibility(View.VISIBLE);
            }
        }
        //setContentView(R.layout.voice);
        initValues();
        initPatterns();
        //appData();
        Boolean boolFirstUse = getPrefs.getBoolean("fUse", true);
        if(boolFirstUse){
            prefs.edit().putBoolean("fUse", false).commit();
            AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
            dlgAlert.setMessage("Hey there user ! Thanks for Beta testing my App. For knowing the existing functions, open the menu and select Help. It will redirect you to the web page where all functions are written.");
            dlgAlert.setTitle("Thanks for Beta Testing");
            dlgAlert.setPositiveButton("Ok", null);
            dlgAlert.setCancelable(true);
            dlgAlert.create().show();
        }else{}
            //putVoice();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		headFloat(false);
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
        super.onStop();
        SharedPreferences getPrefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        boolean fb = getPrefs.getBoolean("checkboxB", true);
        if (fb) {
            headFloat(true);
        }
        if (camera != null) {
            camera.release();
        }
		//finish();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
		
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
	}

	public void initPatterns() {
		// TODO Auto-generated method stub
		addPT = Pattern.compile("add|\\+|plus|sum|total");
		alarmPT = Pattern.compile("alarm|wake");
		subtractPT = Pattern.compile("subtract|\\-|minus|difference");
		multiplyPT = Pattern
				.compile("multiply|\\*|times|into|product|multiplication");
		divisionPT = Pattern.compile("divide|\\/|by|division|divided|quotient");
	}

	public void appData() {
		// TODO Auto-generated method stub
		alApp = getPackages();
		appTemp = new PInfo();
		nApps = alApp.size();
		for (int i = 0; i < nApps; i++) {
			appTemp = alApp.get(i);
			appNameList[i] = appTemp.appname;
			appPackageList[i] = appTemp.pname;
		}

	}

	private void initValues() {
		// TODO Auto-generated method stub

		// Initiate the values

            b = (Button) findViewById(R.id.bVoice);
            btorch = (Button) findViewById(R.id.bTorch);
            res = (TextView) findViewById(R.id.tvRes);
            res2 = (TextView) findViewById(R.id.tvRes2);
            bCom = (ImageButton) findViewById(R.id.bCom);
            etCom = (EditText) findViewById(R.id.etCom);
            // lv = (ListView) findViewById(R.id.lvRes);
			String nan = android.os.Build.MODEL;
            boolean backCam = true;
            for(int i=0;i<Camera.getNumberOfCameras();i++){
                CameraInfo cameraInfo = new CameraInfo();
                Camera.getCameraInfo(i,cameraInfo);
                if(cameraInfo.facing==CameraInfo.CAMERA_FACING_BACK) {
                    backCam = true;
                }else backCam = false;
            }
            if(backCam==false) camera = Camera.open(0);
		    else camera = Camera.open();
        	try{
				params = camera.getParameters();
        	}catch(Exception e){
				e.printStackTrace();
				Log.i("Params not working", "Device - " + e);

				//email in catch
				Intent i = new Intent(Intent.ACTION_SEND);
				i.setType("message/rfc822");
				i.putExtra(Intent.EXTRA_EMAIL, new String[]{"sanved77@gmail.com"});
				i.putExtra(Intent.EXTRA_SUBJECT, "AIVVA: Reported Bugs");
				i.putExtra(Intent.EXTRA_TEXT, "Exception Occurred - " + e +"\n"+ nan);
				try {
					startActivity(Intent.createChooser(i, "Send mail..."));
				} catch (android.content.ActivityNotFoundException ex) {
					Toast.makeText(Voice.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
				}
				//email in catch
       		}
            am = (AudioManager) getBaseContext().getSystemService(
                    Context.AUDIO_SERVICE);
            // Listeners
            b.setOnClickListener(this);
            btorch.setOnClickListener(this);
            bCom.setOnClickListener(this);
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		switch (requestCode) {
            case PICK_CONTACT:
                try {
                    if (resultCode == PICK_CONTACT) {
                        Uri contactData = data.getData();
                        Cursor cur = managedQuery(contactData, null, null, null,
                                null);
                        ContentResolver contect_resolver = getContentResolver();

                        if (cur.moveToFirst()) {
                            String id = cur
                                    .getString(cur
                                            .getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                            String name = "";
                            String no = "";

                            Cursor phoneCur = contect_resolver
                                    .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                            null,
                                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                                                    + " = ?", new String[]{id},
                                            null);

                            if (phoneCur.moveToFirst()) {
                                name = phoneCur
                                        .getString(phoneCur
                                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                                no = phoneCur
                                        .getString(phoneCur
                                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            }

                            Log.e("Phone no & name :***: ", name + " : " + no);
                            res.append(name + " : " + no + "\n");

                            id = null;
                            name = null;
                            no = null;
                            phoneCur = null;
                        }
                        contect_resolver = null;
                        cur = null;
                        // populateContacts();
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("Error :: ", e.toString());
                }
                break;

            case check:
                if (requestCode == check && resultCode == RESULT_OK) {
                    ArrayList<String> resultsAL;
                    resultsAL = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    results = resultsAL.get(0).replace("'", "");
                    res.setText(results);
                    voiceAnalyze(results);
                    super.onActivityResult(requestCode, resultCode, data);
                } else if (noSpeechFlag) {
                    res.setText("You don't have Google Speech Installed. Use Keyboard instead.");
                } else {
                    res.setText("Press the voice button");
                }
                break;
		case BT_REQ_CODE:
			if (requestCode == BT_REQ_CODE) {

				if (resultCode == 120) {
					Toast.makeText(
							getApplicationContext(),
							"Your device is now discoverable by other devices for 120 seconds",
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getApplicationContext(),
							"Fail to enable discoverability on your device.",
							Toast.LENGTH_SHORT).show();
				}
			}
			break;
		}
	}

	private void voiceAnalyze(String results) {
		// TODO Auto-generated method stub
		/*************** SECURE ******************/
		results = results.toLowerCase();
		results = results.replace("whats app", "whatsapp");
		if (results.contains("open")
        || results.contains("launch")
        || results.contains("start") || results.contains("initiate")){
			openOptions(results);}
        else if(results.contains("fullscreen on")){

            }
		 else if (results.contains("silent mode on")
				|| results.contains("ringer mode off")) {

			switch (am.getRingerMode()) {
			case AudioManager.RINGER_MODE_SILENT:
				Toast.makeText(getApplicationContext(),
						"Phone already in Silent Mode", Toast.LENGTH_LONG)
						.show();
				break;
			case AudioManager.RINGER_MODE_NORMAL:
				am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
				Toast.makeText(getApplicationContext(),
						"Silent Mode activated", Toast.LENGTH_LONG).show();
				break;
			case AudioManager.RINGER_MODE_VIBRATE:
				Toast.makeText(getApplicationContext(),
						"Phone already in Silent Mode with Vibration",
						Toast.LENGTH_LONG).show();
				break;
			}
		} else if (results.contains("silent mode off")
				|| results.contains("ringer mode on")) {
			am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
			Toast.makeText(getApplicationContext(), "Silent Mode de-activated",
                    Toast.LENGTH_LONG).show();
		} else if (results.contains("square of")
				|| results.contains("square root of")) {
			squareRT(results);
		} else if (results.contains("turn on bluetooth")
				|| results.contains("turn on blue tooth")
				|| results.contains("bluetooth on")
				|| results.contains("blue tooth on")) {
			BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
					.getDefaultAdapter();
			mBluetoothAdapter.enable();
			makeDiscoverable();
		} else if (results.contains("turn off bluetooth")
				|| results.contains("turn off blue tooth")
				|| results.contains("bluetooth off")
				|| results.contains("blue tooth off")) {
			BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
					.getDefaultAdapter();
			if (mBluetoothAdapter.isEnabled()) {
				mBluetoothAdapter.disable();
			} else {
				Toast.makeText(getApplicationContext(),
						"Bluetooth is already off", Toast.LENGTH_LONG).show();
			}
		} else if (results.contains("turn on wifi")
				|| results.contains("wifi on")) {
			toggleWiFi(true);
		} else if (results.contains("turn off wifi")
				|| results.contains("wifi off")) {
			toggleWiFi(false);
		} else if (results.contains("send whatsapp message")
				|| results.contains("send whatsapp")) {
			results = results.replace("send", "");
			results = results.replace("whatsapp", "");
			res.setText("The message " + results + " is being sent on WhatsApp");
			sendWhatsAppMsg(results);
		} else if (results.contains("send hike message")
				|| results.contains("send hike")) {
			results = results.replace("send", "");
			results = results.replace("hike", "");
			res.setText("The message" + results + " is being sent on Hike");
			sendHikeMsg(results);
		} else if (results.contains("facebook status")) {
			results = results.replace("facebook", "");
			results = results.replace("status", "");
			res.setText("The message" + results
					+ " is being posted on Facebook");
			postOnFB(results);
		} else if (results.contains("send message")) {
			results = results.replace("send", "");
			results = results.replace("message", "");
			res.setText("The message" + results + " is being sent using SMS");
			sendTextMessage(results);
		} else if (results.contains("search google")
                || results.contains("search")
				|| results.contains("google")) {
			results = results.replace("search", "");
			results = results.replace("google", "");
			searchOnGoogle(results);
		} else if (results.contains("search maps") || results.contains("maps")) {
			results = results.replace("search", "");
			results = results.replace("maps", "");
			searchOnMaps(results);
		} else if (results.contains("temperature")
				|| (results.contains("weather") || (results.contains("climate")))) {
			tellTemperature();
		} else if (results.contains("songs")
				|| (results.contains("music") || (results.contains("tunes")))) {
			randomMusic();
		}
		else if (results.contains("reboot")){
            try {
                Process process = Runtime.getRuntime().exec(new String[] { "su", "-c", "reboot" });
                process.waitFor();
            } catch (IOException e) {
                res.setText("You don't have root");
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
		} else if (results.contains("search yahoo")
				|| results.contains("yahoo"))

    {
        results = results.replace("search", "");
			results = results.replace("yahoo", "");
			searchOnYahoo(results);
		} else if (results.contains("search bing") || results.contains("bing"))

    {
        results = results.replace("search", "");
			results = results.replace("bing", "");
			searchOnBing(results);
		} else if (addPT.matcher(results).find()) {
			addNums(results);
		} else if (subtractPT.matcher(results).

    find()) {
			subtractNums(results);
		} else if (multiplyPT.matcher(results).

    find()) {
			multiplyNums(results);
		} else if (divisionPT.matcher(results).find()) {
			divideNums(results);
		} else if (results.contains("call")) {
			placeCall(results);
		} else if (results.contains("turn off head")) {
			headFloat(false);
		} else if (results.contains("turn on data")
				|| results.contains("data on")) {
			enableData();
		} else if (results.contains("turn off data")
				|| results.contains("data off")) {
			enableData();
		} else if (results.contains("turn on torch")
				|| results.contains("torch on")) {
			toggleTorch(true);
		} else if (results.contains("turn off torch")
				|| results.contains("close torch")
				|| results.contains("torch of")
				|| results.contains("torch off")) {
			toggleTorch(false);
		} else if (results.contains("brightness settings")
				|| results.contains("display settings")) {
			brightness();
		} else if (results.contains("take a pic")
				|| results.contains("take a picture")) {
		} else if (results.contains("alarm")|| results.contains("wake")) {
			alarm(results);
		} else if (results.contains("volume up")) {
			volumeUp();
		} else if (results.contains("volume down")) {
			volumeDown();
		}
		/*************** SECURE ******************/
	}

	private void volumeDown() {
		// TODO Auto-generated method stub
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Instrumentation inst = new Instrumentation();
					for (int i = 0; i < 7; i++) {
						inst.sendKeyDownUpSync(KeyEvent.KEYCODE_VOLUME_DOWN);
						Thread.sleep(200);
					}
				} catch (InterruptedException e) {
				}
			}
		}).start();
	}

	private void volumeUp() {
		// TODO Auto-generated method stub
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Instrumentation inst = new Instrumentation();
					for (int i = 0; i < 7; i++) {
						inst.sendKeyDownUpSync(KeyEvent.KEYCODE_VOLUME_UP);
						Thread.sleep(200);
					}
				} catch (InterruptedException e) {
				}
			}
		}).start();
	}

	public void alarm(String results2) {
		// TODO Auto-generated method stub
		Pattern pattern = Pattern.compile("alarm|wake");
		String num1, num2;
		int[] val = new int[5];
		String str=results2;
		str.replace(":"," ");
		if (pattern.matcher(str).find()) {

			Pattern p = Pattern.compile("-?\\d+|am|pm|hours|minutes");
			Matcher m = p.matcher(str);
			List<String> list = new ArrayList<String>();
			while (m.find()) {
				list.add(m.group());
			}
			int lsize = list.size();
			for (int i = 0; i < lsize; i++) {
				if (list.get(i).matches("-?\\d+(\\.\\d+)?")) {
					val[i] = Integer.parseInt(list.get(i));

				}
			}
            res.setText("V = " + val[0] + ":" + val[1]);
			if (lsize == 2) {
				if (list.get(0).contains("minutes") || list.get(0).contains("minutes")) {

				} else {
					if (list.get(0).contains("am") || list.get(0).contains("pm")) {

					} else if (list.get(1).contains("am") || list.get(1).contains("pm")) {
                        if (list.get(0).contains("am")) {

                        } else if (list.get(0).contains("pm")) {

                        }
					}
				}
			} else if (lsize == 3) {
				if (list.get(0).contains("am") || list.get(1).contains("pm")) {

				} else if (list.get(1).contains("am") || list.get(1).contains("pm")) {

				} else if (list.get(2).contains("am") || list.get(2).contains("pm")) {

				}
			}
		}
	}

	private void toggleTorch(boolean state) {
		// TODO Auto-generated method stub
		try {
            hasFlash = getApplicationContext().getPackageManager()
                    .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
            if (!hasFlash) {
                // device doesn't support flash
                // Show alert message and close the application
                res.setText("No LED flash on your device");
            } else {
                if (state) {
                    params.setFlashMode(Parameters.FLASH_MODE_TORCH);
                    camera.setParameters(params);
                    camera.startPreview();
                    btorch.setVisibility(View.VISIBLE);
                } else if (!state) {
                    params.setFlashMode(Parameters.FLASH_MODE_OFF);
                    camera.setParameters(params);
                    camera.stopPreview();
                    btorch.setVisibility(View.INVISIBLE);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            String nan = android.os.Build.MODEL;
            //Toast.makeText(this, "Cant torch it baby", Toast.LENGTH_SHORT).show();
            //email in catch
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_EMAIL, new String[]{"sanved77@gmail.com"});
            i.putExtra(Intent.EXTRA_SUBJECT, "AIVVA: Reported Bugs");
            i.putExtra(Intent.EXTRA_TEXT, "Exception Occurred - " + e +"\n"+ nan);
            try {
                startActivity(Intent.createChooser(i, "Send mail..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(Voice.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
            }
            //email in catch
        }
	}

	public void headFloat(boolean c) {
		// TODO Auto-generated method stub
		Intent i2 = new Intent(this, ChatHeadService.class);
		if (c) {
			startService(i2);
		} else if (!c) {
			stopService(i2);
			//stopService(new Intent(this, ChatHeadService.class));
		}
	}

	private void divideNums(String results2) {
		// TODO Auto-generated method stub
		String num1, num2;
		String str = results2;
		Pattern p = Pattern.compile("-?\\d+|-");
		Matcher m = p.matcher(str);
		List<String> list = new ArrayList<String>();
		while (m.find()) {
			list.add(m.group());
		}
		num1 = list.get(0);
		num2 = list.get(1);
		if (num1.length() != 0 && num2.length() != 0) {
			double n1 = Integer.parseInt(num1);
			double n2 = Integer.parseInt(num2);
			double sum = n1 / n2;
			res.setText("The quotient is " + sum);
		} else {
			res.setText("Cannot perform addition with insifficient data");
		}
	}

	public boolean isAlpha(String results2) {

		if (results.matches("([a-zA-Z]+\\s+)*[a-zA-Z]+"))
			return true;
		else
			return false;
	}

	private boolean isTelephonyEnabled() {
		TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		return tm != null
				&& tm.getSimState() == TelephonyManager.SIM_STATE_READY;
	}

	private void placeCall(String results2) {
		// TODO Auto-generated method stub
		if (isTelephonyEnabled()) {
			if (isAlpha(results2)) {
				String url = results2.replaceAll("call", "");
				url = url.substring(1);
				String url2 = url.substring(0, 1);
				url2 = url2.toUpperCase();
				String url_noFirst;
				url_noFirst = url.substring(1);
				url = url2 + url_noFirst;
				String phoneNum = getPhoneNumber(url, this);
				phoneNum = phoneNum.replaceAll("-", "");
				Intent intent = new Intent(Intent.ACTION_CALL);
				intent.setData(Uri.parse("tel:" + phoneNum));
				startActivity(intent);
			} else {
				String url = results2.replaceAll("call", "");
				url = url.replaceAll(" ", "");
				Intent intent = new Intent(Intent.ACTION_DIAL);
				intent.setData(Uri.parse("tel:" + url));
				startActivity(intent);
			}
		} else {
			res.setText("Your device doesn't support calling.");
		}
	}

	public String getPhoneNumber(String name, Context context) {
		String ret = null;
		String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
				+ " like '" + name + "' COLLATE NOCASE";
		String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER };
		Cursor c = context.getContentResolver().query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection,
				selection, null, null);
		if (c.moveToFirst()) {
			ret = c.getString(0);
		}
		c.close();
		if (ret == null)
			ret = "Unsaved";
		return ret;
	}

	private void squareRT(String results2) {
		// TODO Auto-generated method stub
		String num1, num2;
		String str = results2;
		Pattern p = Pattern.compile("-?\\d+|-");
		Matcher m = p.matcher(str);
		List<String> list = new ArrayList<String>();
		while (m.find()) {
			list.add(m.group());
		}
		num1 = list.get(0);
		if (str.contains("root")) {
			if (num1.length() != 0) {
				int n1 = Integer.parseInt(num1);
				double sum = Math.sqrt(n1);
				res.setText("The square root of " + num1 + " is " + sum);
			} else {
				res.setText("Cannot perform calculation with insifficient data");
			}
		} else {
			if (num1.length() != 0) {
				int n1 = Integer.parseInt(num1);
				int sum = n1 * n1;
				res.setText("The square of " + num1 + " is " + sum);
			} else {
				res.setText("Cannot perform calculation with insifficient data");
			}
		}
	}

	private void addNums(String results2) {
		// TODO Auto-generated method stub
		try {
            String num1, num2;
            String str = results2;
            Pattern p = Pattern.compile("-?\\d+|-");
            Matcher m = p.matcher(str);
            List<String> list = new ArrayList<String>();
            while (m.find()) {
                list.add(m.group());
            }
            num1 = list.get(0);
            num2 = list.get(1);
            if (num1.length() != 0 && num2.length() != 0) {
                int n1 = Integer.parseInt(num1);
                int n2 = Integer.parseInt(num2);
                int sum = n1 + n2;
                res.setText("The addition is " + sum);
            } else {
                res.setText("Cannot perform addition with insifficient data");
            }
        }catch(Exception e){
            e.printStackTrace();
            res.setText("That can't be added mate !");
        }
	}

	private void subtractNums(String results2) {
		// TODO Auto-generated method stub
		try {
            String num1, num2;
            String str = results2;
            Pattern p = Pattern.compile("-?\\d+");
            Matcher m = p.matcher(str);
            List<String> list = new ArrayList<String>();
            while (m.find()) {
                list.add(m.group());
            }
            num1 = list.get(0);
            num2 = list.get(1);
            if (num1.length() != 0 && num2.length() != 0) {
                int n2 = Integer.parseInt(num1);
                int n1 = Integer.parseInt(num2);
                int sum = n2 - n1;
                sum = -sum;
                res.setText("The difference is " + sum);
            } else {
                res.setText("Cannot perform subtraction with insifficient data");
            }
        }catch(Exception e){
            e.printStackTrace();
            res.setText("Can't find the difference of that buddy!");
        }
	}

	private void multiplyNums(String results2) {
		// TODO Auto-generated method stub
		try {
            String num1, num2;
            String str = results2;
            Pattern p = Pattern.compile("-?\\d+|-");
            Matcher m = p.matcher(str);
            List<String> list = new ArrayList<String>();
            while (m.find()) {
                list.add(m.group());
            }
            num1 = list.get(0);
            num2 = list.get(1);
            if (num1.length() != 0 && num2.length() != 0) {
                int n1 = Integer.parseInt(num1);
                int n2 = Integer.parseInt(num2);
                int sum = n1 * n2;
                res.setText("The product is " + sum);
            } else {
                res.setText("Cannot perform multiplication with insufficient data");
            }
        }catch(Exception e){
            e.printStackTrace();
            res.setText("That's not how multiplication works buddy");
        }
	}

	public void enableData() {
		Intent intent = new Intent();
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS);
		startActivity(intent);
	}

	public void brightness() {
		Intent intent = new Intent();
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.provider.Settings.ACTION_DISPLAY_SETTINGS);
		startActivity(intent);
	}

	private void randomMusic() {
		// TODO Auto-generated method stub
		Intent browserIntent = new Intent(Intent.ACTION_VIEW,
				Uri.parse("http://gaana.com/mostpopular/"));
		startActivity(browserIntent);
	}

	private void tellTemperature() {
		// TODO Auto-generated method stub
		Intent browserIntent = new Intent(Intent.ACTION_VIEW,
				Uri.parse("http://m.accuweather.com/"));
		startActivity(browserIntent);
	}

	public void searchOnMaps(String results) {
		// TODO Auto-generated method stub
		boolean flag = false;
		do {
			if (results.startsWith(" ")) {
				results = results.substring(1);
			} else if (!results.startsWith(" ")) {
				flag = true;
			}
		} while (flag = false);
		Intent browserIntent = new Intent(Intent.ACTION_VIEW,
				Uri.parse("http://maps.google.com/#q=" + results));
		startActivity(browserIntent);
	}

	public void searchOnGoogle(String results) {
		// TODO Auto-generated method stub
		boolean flag = false;
		do {
			if (results.startsWith(" ")) {
				results = results.substring(1);
			} else if (!results.startsWith(" ")) {
				flag = true;
			}
		} while (flag = false);
		Intent browserIntent = new Intent(Intent.ACTION_VIEW,
				Uri.parse("http://www.google.com/#q=" + results));
		startActivity(browserIntent);
	}

	public void searchOnYahoo(String results) {
		// TODO Auto-generated method stub
		boolean flag = false;
		do {
			if (results.startsWith(" ")) {
				results = results.substring(1);
			} else if (!results.startsWith(" ")) {
				flag = true;
			}
		} while (flag = false);
		Intent browserIntent = new Intent(Intent.ACTION_VIEW,
				Uri.parse("http://search.yahoo.com/search?p=" + results));
		startActivity(browserIntent);
	}

	public void searchOnBing(String results) {
		// TODO Auto-generated method stub
		boolean flag = false;
		do {
			if (results.startsWith(" ")) {
				results = results.substring(1);
			} else if (!results.startsWith(" ")) {
				flag = true;
			}
		} while (flag = false);
		Intent browserIntent = new Intent(Intent.ACTION_VIEW,
				Uri.parse("http://www.bing.com/search?q=" + results));
		startActivity(browserIntent);
	}

	public void sendTextMessage(String results2) {
		// TODO Auto-generated method stub
		Intent sendIntent = new Intent(Intent.ACTION_VIEW);
		if (sendIntent != null) {
			sendIntent.putExtra("sms_body", results2);
			sendIntent.setType("vnd.android-dir/mms-sms");
			startActivityForResult(
					Intent.createChooser(sendIntent, "Sending SMS..."), 1);
		}
	}

	public void toggleWiFi(boolean status) {
		WifiManager wifiManager = (WifiManager) this
				.getSystemService(Context.WIFI_SERVICE);
		if (status == true && !wifiManager.isWifiEnabled()) {
			wifiManager.setWifiEnabled(true);
		} else if (status == false && wifiManager.isWifiEnabled()) {
			wifiManager.setWifiEnabled(false);
		}
	}

	private void openOptions(String results) {
		// TODO Auto-generated method stub

		if (results.contains("open")) {
			alApp = getPackages();
			int a = alApp.size();
			PInfo abc = new PInfo();
			// String text = " calendar";
			for (int i = 0; i < a; i++) {
				abc = alApp.get(i);
				String sTemp2 = abc.appname;
				if (results.contains(sTemp2.toLowerCase())) {
					res.setText(abc.appname + " is being opened");
					Intent LaunchIntent = getPackageManager()
							.getLaunchIntentForPackage(abc.pname);
					startActivity(LaunchIntent);
					break;
				}
			}

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.voice, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()){
		case R.id.prefs:
			headFloat(false);
            Intent t = new Intent("com.aivva.PREFS");
			startActivity(t);
			break;
		case R.id.exit:
			finish();
			break;
            case R.id.about:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://www.techwindow.org/2015/09/aivva-android-app-beta-testing.html"));
                startActivity(browserIntent);
                break;
            case R.id.source:
                Intent email = new Intent(Voice.this, Email.class);
                startActivity(email);
                break;
            case R.id.bug:
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL, new String[]{"sanved77@gmail.com"});
                i.putExtra(Intent.EXTRA_SUBJECT, "AIVVA: Reported Bugs");
                i.putExtra(Intent.EXTRA_TEXT   , "Bug/Malfunction - ");
                try {
                    startActivity(Intent.createChooser(i, "Send mail..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(Voice.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }
                break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.bVoice:
			putVoice();
			break;
		case R.id.bCom:
			if (etCom.getText().toString().matches("")) {
				Toast.makeText(getApplicationContext(),
						"You did not enter anything", Toast.LENGTH_LONG).show();
				headFloat(true);

			} else {
				results = etCom.getText().toString();
				res.setText(results);
				voiceAnalyze(results);
			}
			break;
		case R.id.bTorch:
			toggleTorch(false);
			res.setText("Press the voice button");
			btorch.setVisibility(View.INVISIBLE);
			break;
            case R.id.ibMenu:
                openOptionsMenu();
                break;
        }


	}

    public void putVoice() {
		// TODO Auto-generated method stub
		Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak");
		i.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
		try{
		startActivityForResult(i, check);}
		catch(Exception e){
			e.printStackTrace();
            noSpeechFlag=true;
		}
	}

    public void sendWhatsAppMsg(String results) {

		Intent waIntent = new Intent(Intent.ACTION_SEND);
		waIntent.setType("text/plain");
		String text = results;
		waIntent.setPackage("com.whatsapp");
		if (waIntent != null) {
			waIntent.putExtra(Intent.EXTRA_TEXT, text);//
			startActivity(Intent.createChooser(waIntent, text));
		} else {
			Toast.makeText(this, "WhatsApp not found", Toast.LENGTH_SHORT)
					.show();
		}

	}

	public void sendHikeMsg(String results) {

		Intent waIntent = new Intent(Intent.ACTION_SEND);
		waIntent.setType("text/plain");
		String text = results;
		waIntent.setPackage("com.bsb.hike");
		if (waIntent != null) {
			waIntent.putExtra(Intent.EXTRA_TEXT, text);//
			startActivity(Intent.createChooser(waIntent, text));
		} else {
			Toast.makeText(this, "Hike not found", Toast.LENGTH_SHORT).show();
		}

	}

	protected void makeDiscoverable() {
		// Make local device discoverable
		Intent discoverableIntent = new Intent(
				BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(
				BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
		startActivityForResult(discoverableIntent, BT_REQ_CODE);
	}

	public void postOnFB(String results) {

		Intent waIntent = new Intent(Intent.ACTION_SEND);
		waIntent.setType("text/plain");
		String text = results;
		waIntent.setPackage("com.facebook.katana");
		if (waIntent != null) {
			waIntent.putExtra(Intent.EXTRA_TEXT, text);//
			startActivity(Intent.createChooser(waIntent, text));
		} else {
			Toast.makeText(this, "Facebook not found", Toast.LENGTH_SHORT)
					.show();
		}

	}

	class PInfo {
		private String appname = "";
		private String pname = "";
		private String versionName = "";
		private int versionCode = 0;

		private void prettyPrint() {
			System.out.println(appname + "\t" + pname + "\t" + versionName
					+ "\t" + versionCode);
		}
	}

	private ArrayList<PInfo> getPackages() {
		ArrayList<PInfo> apps = getInstalledApps(true); /*
														 * false = no system
														 * packages
														 */
		final int max = apps.size();
		for (int i = 0; i < max; i++) {
			apps.get(i).prettyPrint();
		}
		return apps;
	}

	private ArrayList<PInfo> getInstalledApps(boolean getSysPackages) {

		List<PackageInfo> packs = getPackageManager().getInstalledPackages(0);
		ArrayList<PInfo> resAL = new ArrayList<PInfo>();
		for (int i = 0; i < packs.size(); i++) {
			PackageInfo p = packs.get(i);
			if ((!getSysPackages) && (p.versionName == null)) {
				continue;
			}
			PInfo newInfo = new PInfo();
			newInfo.appname = p.applicationInfo.loadLabel(getPackageManager())
					.toString();
			newInfo.pname = p.packageName;
			newInfo.versionName = p.versionName;
			newInfo.versionCode = p.versionCode;
			resAL.add(newInfo);
		}
		return resAL;
	}

}
