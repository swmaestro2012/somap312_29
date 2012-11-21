package com.soma.chronos.gallery;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import com.soma.chronos.root.RootActivity;

public class GalleryActivity extends RootActivity {

	public static final String ACTION_INTENT = "chronos.intent.action.GALLERY";
	private static final String TYPE = "image/*";
	private static int TAKE_GALLERY = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType(TYPE);
		startActivityForResult(intent, TAKE_GALLERY);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == RESULT_OK && requestCode == TAKE_GALLERY) {
			Uri currImageURI = data.getData();
			Intent intent = new Intent(SelectPreviewActivity.ACTION_INTENT);
			intent.putExtra(SelectPreviewActivity.KEY_SELECT,
					getRealPathFromURI(currImageURI));
			startActivity(intent);
			finish();
		} else {
			finish();
		}
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
}
