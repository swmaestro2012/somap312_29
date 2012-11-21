package com.soma.chronos.contacts;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.soma.chronos.MainActivity;
import com.soma.chronos.R;
import com.soma.chronos.root.RootActivity;

public class ContactsListActivity extends RootActivity {
	public static final String ACTION_INTENT = "chronos.intent.action.CONTACTS";
	public static final String KEY_RECT = "rect";

	public static final int HANDLER_CHANGE = 0x100000;
	public static final int HANDLER_BACK = 0x200000;
	public static final int HANDLER_MAIN = 0x300000;

	private static final String[] ID_PROJECTION = new String[] { Contacts._ID,
			Contacts.DISPLAY_NAME };

	private ListView listView = null;
	private Bitmap bitmap = null;

	private List<ContactsValue> list = null;
	private ContactsAdapter adapter = null;
	private ListEventListener listener = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		Bundle bundle = getIntent().getExtras();
		bitmap = bundle.getParcelable(KEY_RECT);

		list = new ArrayList<ContactsValue>();
		adapter = new ContactsAdapter(getApplicationContext(), 0, list);
		listener = new ListEventListener();
		listView = initListView(getApplicationContext());

		LinearLayout layout = new LinearLayout(getApplicationContext());
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.addView(topLayout(getApplicationContext()),
				linearParams(MATCH_PARENT, MATCH_PARENT, 9));
		layout.addView(listView, linearParams(MATCH_PARENT, MATCH_PARENT, 1));
		setContentView(layout);
		setItem();
	}

	private ViewGroup topLayout(Context context) {
		int maxLength = 10;

		EditText editText = new EditText(getApplicationContext());
		editText.setHint("Search");
		editText.addTextChangedListener(listener);
		editText.setLines(1);
		editText.setInputType(0);
		editText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
				maxLength) });
		editText.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				EditText text = (EditText) view;
				text.setInputType(1);
			}
		});

		ImageView close = new ImageView(context);
		close.setImageResource(R.drawable.close);
		close.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				setResult(RESULT_OK);
				finish();
			}
		});

		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.HORIZONTAL);
		layout.addView(editText, linearParams(MATCH_PARENT, MATCH_PARENT, 2));
		layout.addView(close, linearParams(MATCH_PARENT, MATCH_PARENT, 8));

		return layout;
	}

	private ListView initListView(Context context) {
		ListView listView = new ListView(getApplicationContext());
		listView.setBackgroundResource(R.drawable.bg);
		listView.setOnItemClickListener(listener);
		listView.setAdapter(adapter);
		listView.setTextFilterEnabled(true);
		listView.setFastScrollEnabled(true);
		return listView;
	}

	private ProgressDialog getProgressBar(String message, boolean cancelable) {
		ProgressDialog dialog = new ProgressDialog(this);
		dialog.setMessage(message);
		dialog.setIndeterminate(true);
		dialog.setCancelable(cancelable);
		return dialog;
	}

	private void setItem() {
		new AsyncTask<Void, ContactsValue, Void>() {
			private ProgressDialog dialog;
			Cursor cursor;

			@Override
			public Void doInBackground(Void... params) {
				while (cursor.moveToNext()) {
					String id = cursor.getString(cursor
							.getColumnIndex(Contacts._ID));
					String name = cursor.getString(cursor
							.getColumnIndex(Contacts.DISPLAY_NAME));

					Bitmap bitmap = resizedContactPhoto(Long.parseLong(id));
					ContactsValue contactsValue = new ContactsValue();
					contactsValue.setPersonId(Long.parseLong(id));
					contactsValue.setName(name);
					contactsValue.setBitmap(bitmap);
					publishProgress(contactsValue);
				}
				return null;
			}

			@Override
			protected void onProgressUpdate(ContactsValue... values) {
				// ListView Adapter Update
				list.add(values[0]);
				listView.post(new Runnable() {
					@Override
					public void run() {
						adapter.sort();
						adapter.notifyDataSetChanged();

					}
				});
			}

			@Override
			protected void onPreExecute() {
				dialog = getProgressBar("Waiting...", false);
				dialog.show();
				cursor = getContentResolver().query(Contacts.CONTENT_URI,
						ID_PROJECTION, null, null, null);
			};

			@Override
			protected void onPostExecute(Void result) {

				try {
					cursor.close();
					dialog.dismiss();
					for (ContactsValue contactsValue : list) {
						adapter.addOriginal(contactsValue);
					}

					Toast.makeText(getApplicationContext(),
							"사용자를 선택 하시면 자동으로 사진이 변경 됩니다.", Toast.LENGTH_SHORT)
							.show();
				} catch (Exception e) {
					Log.e(MainActivity.TAG,
							"ContactsListAcitivity " + e.getMessage());
				}

			};

		}.execute();

	}

	private LinearLayout.LayoutParams linearParams(int w, int h, float weight) {
		return new LinearLayout.LayoutParams(w, h, weight);
	}

	private void notifyDataSetChanged(final ProgressDialog dialog) {
		new AsyncTask<Void, Void, Void>() {

			@Override
			public Void doInBackground(Void... params) {
				listView.post(new Runnable() {

					@Override
					public void run() {
						adapter.notifyDataSetChanged();

					}
				});
				return null;
			}

			@Override
			protected void onPreExecute() {

			};

			@Override
			protected void onPostExecute(Void result) {
				dialog.dismiss();

			};

		}.execute();
	}

	private void onContactPhoto(int postion) {
		boolean result = false;
		ProgressDialog dialog = getProgressBar("Waiting...", false);
		dialog.show();

		ContactsValue contactsValue = list.get(postion);
		final long personId = contactsValue.getPersonId();

		result = updateContactPhoto(personId);

		if (result) {
			Bitmap bitmap = resizedContactPhoto(personId);
			contactsValue.setBitmap(bitmap);
		} else {
			Bitmap bitmap = resizedBitmap(getResources(), R.drawable.contact,
					IMAGE_DEFAULT_WIDTH, IMAGE_DEFAULT_WIDTH);
			contactsValue.setBitmap(bitmap);
		}

		list.set(postion, contactsValue);

		notifyDataSetChanged(dialog);
	}

	private boolean updateContactPhoto(long id) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);

		int result = setContactPhoto(getContentResolver(), baos.toByteArray(),
				id);

		if (result > 0)
			return true;
		else
			return false;
	}

	public String getRealPathFromURI(Uri contentUri) {
		// can post image
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = getContentResolver().query(contentUri, projection,
				null, null, null);
		int column = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();

		String string = cursor.getString(column);
		cursor.close();
		return string;
	}

	private int setContactPhoto(ContentResolver c, byte[] bytes, long personId) {
		ContentValues values = new ContentValues();
		int result = -1;

		long rawId = getRawId(c, personId);

		values.put(ContactsContract.Data.RAW_CONTACT_ID, rawId);
		values.put(ContactsContract.Data.IS_SUPER_PRIMARY, 1);
		values.put(ContactsContract.CommonDataKinds.Photo.PHOTO, bytes);
		values.put(ContactsContract.Data.MIMETYPE,
				ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);

		result = c
				.update(ContactsContract.Data.CONTENT_URI,
						values,
						ContactsContract.Data._ID + " = "
								+ String.valueOf(rawId), null);

		return result;
	}

	private long getRawId(ContentResolver c, long personId) {
		long result = -1;

		StringBuffer buffer = new StringBuffer();
		buffer.append(ContactsContract.Data.CONTACT_ID);
		buffer.append(" = ");
		buffer.append(String.valueOf(personId));
		buffer.append(" AND ");
		buffer.append(ContactsContract.Data.MIMETYPE);
		buffer.append("=='");
		buffer.append(ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);
		buffer.append("'");
		Cursor cursor = c.query(ContactsContract.Data.CONTENT_URI,
				new String[] { ContactsContract.Data._ID }, buffer.toString(),
				null, null);

		if (cursor.moveToFirst()) {
			result = cursor.getLong(cursor
					.getColumnIndex(ContactsContract.Data._ID));
			Log.v(MainActivity.TAG,
					"Account Name : "
							+ cursor.getString(cursor
									.getColumnIndex(ContactsContract.Data._ID)));

		}
		cursor.close();

		return result;

	}

	private Bitmap resizedContactPhoto(long id) {
		Bitmap bitmap = loadContactPhoto(getContentResolver(), id);
		if (bitmap == null) {
			return resizedBitmap(getResources(), R.drawable.contact,
					IMAGE_DEFAULT_WIDTH, IMAGE_DEFAULT_HEIGHT);
		} else {
			return resizedBitmap(bitmap, IMAGE_DEFAULT_WIDTH,
					IMAGE_DEFAULT_HEIGHT);
		}
	}

	public Bitmap loadContactPhoto(ContentResolver cr, long id) {
		Uri uri = ContentUris.withAppendedId(Contacts.CONTENT_URI, id);
		InputStream input = ContactsContract.Contacts
				.openContactPhotoInputStream(cr, uri);
		if (input == null) {
			return null;
		}
		return BitmapFactory.decodeStream(input);
	}

	private class ListEventListener implements OnItemClickListener, TextWatcher {
		@Override
		public void onItemClick(AdapterView<?> viewGroup, View view,
				int postion, long id) {

			onContactPhoto(postion);

			/*
			 * LinearLayout layout = (LinearLayout) view; LinearLayout
			 * rightLayout = (LinearLayout) layout.getChildAt(1);
			 * rightLayout.setVisibility(View.VISIBLE);
			 * 
			 * Button b0 = (Button) rightLayout.getChildAt(0); Button b1 =
			 * (Button) rightLayout.getChildAt(1); Button b2 = (Button)
			 * rightLayout.getChildAt(2);
			 * 
			 * b0.setOnClickListener(new ButtonListener(postion));
			 * b1.setOnClickListener(new ButtonListener(postion));
			 * b2.setOnClickListener(new ButtonListener(postion));
			 */
		}

		@Override
		public void afterTextChanged(Editable s) {
			adapter.getFilter().filter(s);
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// TODO Auto-generated method stub

		}
	}

	@SuppressWarnings("unused")
	private class ButtonListener implements OnClickListener {
		private int postion;

		public ButtonListener(int postion) {
			this.postion = postion;
		}

		@Override
		public void onClick(View v) {

			switch (v.getId()) {
			case HANDLER_CHANGE:
				onContactPhoto(postion);
				break;
			case HANDLER_BACK:
				setResult(RESULT_CANCELED);
				finish();
				break;
			case HANDLER_MAIN:
				setResult(RESULT_OK);
				finish();
				break;
			}

		}
	}

}
