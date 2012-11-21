package com.soma.chronos.contacts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.soma.chronos.root.RootInterface;
import com.soma.chronos.util.DeviceMetrics;

public class ContactsAdapter extends ArrayAdapter<ContactsValue> implements
		RootInterface, SectionIndexer {

	private final int INDEX_IMAGE = 0;
	private final int INDEX_TEXT = 1;

	private List<ContactsValue> items = null;
	private List<ContactsValue> originalItems = null;
	private Map<String, Integer> indexer = null;
	private String[] sections = null;
	private Filter filter;

	private Context context = null;

	public ContactsAdapter(Context context, int id, List<ContactsValue> items) {
		super(context, id, items);
		this.context = context;
		this.items = items;
		indexer = new HashMap<String, Integer>();
		originalItems = new ArrayList<ContactsValue>();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = null;
		try {
			row = convertView;

			if (row == null) {

				ImageView imageView = new ImageView(context);
				imageView.setScaleType(ScaleType.MATRIX);

				TextView textView = new TextView(context);
				textView.setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL);
				textView.setTextColor(Color.BLACK);
				textView.setTextSize(20.0F);

				LinearLayout left = new LinearLayout(context);
				left.setOrientation(LinearLayout.HORIZONTAL);
				left.setGravity(Gravity.LEFT);
				left.addView(imageView, INDEX_IMAGE);
				left.addView(textView, INDEX_TEXT);

				Button b1 = new Button(context);
				b1.setId(ContactsListActivity.HANDLER_CHANGE);
				b1.setText("변경");

				Button b2 = new Button(context);
				b2.setId(ContactsListActivity.HANDLER_BACK);
				b2.setText("이전");

				Button b3 = new Button(context);
				b3.setId(ContactsListActivity.HANDLER_MAIN);
				b3.setText("처음");

				LinearLayout right = new LinearLayout(context);
				right.setVisibility(View.GONE);
				right.setPadding(0, 0, DeviceMetrics.getWidth() / 50, 0);
				right.setOrientation(LinearLayout.HORIZONTAL);
				right.setGravity(Gravity.RIGHT);
				right.addView(b1);
				right.addView(b2);
				right.addView(b3);

				LinearLayout layout = new LinearLayout(context);
				layout.setOrientation(LinearLayout.HORIZONTAL);
				layout.setGravity(Gravity.CENTER_VERTICAL);
				layout.addView(left, new LinearLayout.LayoutParams(
						WRAP_CONTENT, WRAP_CONTENT, 1));
				layout.addView(right, new LinearLayout.LayoutParams(
						WRAP_CONTENT, WRAP_CONTENT, 1));

				row = layout;
			}

			ContactsValue contacts = items.get(position);
			if (contacts != null) {
				LinearLayout layout = (LinearLayout) row;
				LinearLayout left = (LinearLayout) layout.getChildAt(0);

				ImageView imageView = (ImageView) left.getChildAt(INDEX_IMAGE);
				imageView.setImageBitmap(contacts.getBitmap());

				TextView textView = (TextView) left.getChildAt(INDEX_TEXT);
				textView.setText(contacts.getName());

			}

		} catch (ClassCastException e) {
		}

		return row;
	}

	@Override
	public Filter getFilter() {
		if (filter == null)
			filter = new ContactsFilter();
		return filter;
	}

	@Override
	public void notifyDataSetInvalidated() {

		int size = items.size();
		for (int i = size - 1; i >= 0; i--) {
			ContactsValue contactsValue = items.get(i);
			String first = contactsValue.getName().substring(0, 1)
					.toUpperCase();

			indexer.put(first, i);
		}

		Set<String> keys = indexer.keySet();
		Iterator<String> it = keys.iterator();
		ArrayList<String> keyList = new ArrayList<String>();
		while (it.hasNext())
			keyList.add(it.next());

		Collections.sort(keyList);
		sections = new String[keyList.size()];
		keyList.toArray(sections);

		sort();

		super.notifyDataSetInvalidated();
	}

	public void addOriginal(ContactsValue object) {
		originalItems.add(object);
	}

	public void sort() {
		sort(comparator);

	}

	@Override
	public int getPositionForSection(int section) {
		String letter = sections[section];
		return indexer.get(letter);
	}

	@Override
	public int getSectionForPosition(int position) {
		int prevIndex = 0;
		for (int i = 0; i < sections.length; i++) {
			if (getPositionForSection(i) > position && prevIndex <= position) {
				prevIndex = i;
				break;
			}
			prevIndex = i;
		}
		return prevIndex;
	}

	@Override
	public Object[] getSections() {
		return sections;
	}

	private Comparator<ContactsValue> comparator = new Comparator<ContactsValue>() {

		@Override
		public int compare(ContactsValue lhs, ContactsValue rhs) {
			String a = lhs.getName();
			String b = rhs.getName();

			return a.compareTo(b);
		}
	};

	private class ContactsFilter extends Filter {

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			// Initiate our results object
			FilterResults results = new FilterResults();

			// No prefix is sent to filter by so we're going to send back the
			// original array
			if (constraint == null || constraint.length() == 0) {
				synchronized (this) {
					results.values = originalItems;
					results.count = originalItems.size();
				}
			} else {
				synchronized (this) {
					// Compare lower case strings
					String prefixString = constraint.toString().toLowerCase();
					final ArrayList<ContactsValue> filteredItems = new ArrayList<ContactsValue>();
					// Local to here so we're not changing actual array
					final ArrayList<ContactsValue> localItems = new ArrayList<ContactsValue>();
					localItems.addAll(originalItems);
					final int count = localItems.size();

					for (int i = 0; i < count; i++) {
						final ContactsValue item = localItems.get(i);
						final String itemName = item.getName().toString()
								.toLowerCase();

						// First match against the whole, non-splitted value
						if (itemName.contains(prefixString)) {
							filteredItems.add(item);
						} else {
						} /*
						 * This is option and taken from the source of
						 * ArrayAdapter final String[] words =
						 * itemName.split(" "); final int wordCount =
						 * words.length;
						 * 
						 * for (int k = 0; k < wordCount; k++) { if
						 * (words[k].startsWith(prefixString)) {
						 * newItems.add(item); break; } } }
						 */
					}

					// Set and return
					results.values = filteredItems;
					results.count = filteredItems.size();
				}// end synchronized
			}

			return results;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint,
				FilterResults results) {
			synchronized (this) {
				final ArrayList<ContactsValue> localItems = (ArrayList<ContactsValue>) results.values;
				notifyDataSetChanged();
				clear();
				// Add the items back in
				for (ContactsValue contactsValue : localItems) {
					add(contactsValue);
				}
			}// end synchronized

			notifyDataSetInvalidated();
		}

	}

}
