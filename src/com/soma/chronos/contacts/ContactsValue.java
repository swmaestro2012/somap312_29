package com.soma.chronos.contacts;

import android.graphics.Bitmap;

public class ContactsValue {

	private long personId;
	private String name;
	private Bitmap bitmap;
	private Bitmap bakBitmap;

	public long getPersonId() {
		return personId;
	}

	public void setPersonId(long personId) {
		this.personId = personId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	public Bitmap getBakBitmap() {
		return bakBitmap;
	}

	public void setBakBitmap(Bitmap bakBitmap) {
		this.bakBitmap = bakBitmap;
	}

	@Override
	public String toString() {
		return this.getName() == null ? "" : this.getName().toLowerCase();
	}
}
