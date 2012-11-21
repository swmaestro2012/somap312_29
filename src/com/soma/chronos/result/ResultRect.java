package com.soma.chronos.result;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;

public class ResultRect implements Parcelable {

	public static byte TRUE = 1;
	public static byte FALSE = 0;

	private int drawNum;
	private int selectFaceNum;

	private int selectFrame;

	private byte isFaceDectect;
	private byte isTouchMode;

	private float rx, ry;

	private List<Rect> rects = null;

	public ResultRect() {
		this.rects = new ArrayList<Rect>();
		this.selectFaceNum = -1;
		this.selectFrame = -1;
		this.drawNum = 0;
		this.isTouchMode = FALSE;
		this.isFaceDectect = FALSE;
	}

	public ResultRect(Parcel parcel) {
		readFromParcel(parcel);
	}

	public void set(Rect rect) {
		rects.add(rect);
	}

	public void set(int left, int top, int right, int bottom) {
		rects.add(new Rect(left, top, right, bottom));
	}

	public int getDrawNum() {
		return drawNum;
	}

	public void setDrawNum(int drawNum) {
		this.drawNum = drawNum;
	}

	public int getSelectFaceNum() {
		return selectFaceNum;
	}

	public void setSelectFaceNum(int selectFaceNum) {
		this.selectFaceNum = selectFaceNum;
	}

	public int getSelectFrame() {
		return selectFrame;
	}

	public void setSelectFrame(int selectFrame) {
		this.selectFrame = selectFrame;
	}

	public boolean isFaceDectect() {

		if (isFaceDectect == TRUE)
			return true;
		else
			return false;
	}

	public void setFaceDectect(boolean isFaceDectect) {
		if (isFaceDectect)
			this.isFaceDectect = TRUE;
		else
			this.isFaceDectect = FALSE;
	}

	public boolean isTouchMode() {
		if (isTouchMode == TRUE)
			return true;
		else
			return false;
	}

	public void setTouchMode(boolean isTouch, float rx, float ry) {
		if (isTouch)
			this.isTouchMode = TRUE;
		else
			this.isTouchMode = FALSE;
		this.rx = rx;
		this.ry = ry;
	}

	public float getRx() {
		return rx;
	}

	public float getRy() {
		return ry;
	}

	public List<Rect> getRects() {
		return rects;
	}

	public Rect get(int index) {
		return rects.get(index);
	}

	public int size() {
		return rects.size();
	}

	@SuppressWarnings("unchecked")
	private void readFromParcel(Parcel parcel) {
		this.drawNum = parcel.readInt();
		this.selectFaceNum = parcel.readInt();
		this.selectFrame = parcel.readInt();
		this.isFaceDectect = parcel.readByte();
		this.isTouchMode = parcel.readByte();
		this.rx = parcel.readFloat();
		this.ry = parcel.readFloat();
		this.rects = parcel.readArrayList(Rect.class.getClassLoader());
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(drawNum);
		dest.writeInt(selectFaceNum);
		dest.writeInt(selectFrame);
		dest.writeByte(isFaceDectect);
		dest.writeByte(isTouchMode);
		dest.writeFloat(rx);
		dest.writeFloat(ry);
		dest.writeList(rects);
	}

	public static final Parcelable.Creator<ResultRect> CREATOR = new Parcelable.Creator<ResultRect>() {
		public ResultRect createFromParcel(Parcel parcel) {
			return new ResultRect(parcel);
		}

		public ResultRect[] newArray(int size) {
			return new ResultRect[size];
		}
	};

}
