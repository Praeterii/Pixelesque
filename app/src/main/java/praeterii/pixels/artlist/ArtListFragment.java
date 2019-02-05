package praeterii.pixels.artlist;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import praeterii.pixele.R;
import praeterii.pixels.StorageUtils;
import processing.core.PApplet;

public class ArtListFragment extends Fragment {

	public static class ArtElement implements Comparable<ArtElement> {
		File image;
		String name;

		public ArtElement(File image, String name) {
			this.image = image;
			this.name = name;
		}

		@Override
		public int compareTo(ArtElement o) {
			return name.compareToIgnoreCase(o.name);
		}
	}

	public interface ArtItemSelectedListener {
		public void onArtItemSelected(ArtElement element);
	}

	GridView gridview;
	ArtAdapter adapter;
	ArtItemSelectedListener listener;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.artlist, null);
	}

	@Override
	public void onStart() {
		super.onStart();

		gridview = (GridView) getView().findViewById(R.id.list);
		gridview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (listener != null) {
					listener.onArtItemSelected(adapter.getItem(arg2));
				}
			}
		});
		adapter = new ArtAdapter(getActivity(), 0);
		gridview.setAdapter(adapter);
		gridview.setFastScrollAlwaysVisible(true);
		gridview.setFastScrollEnabled(true);
		new ImageListLoader().execute();
	}

	public void setListener(ArtItemSelectedListener listen) {
		this.listener = listen;
	}

	public class ArtAdapter extends ArrayAdapter<ArtElement> {

		@Override
		public void addAll(Collection<? extends ArtElement> collection) {
			for (ArtElement e : collection)
				add(e);
		}

		public ArtAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getLayoutInflater(getArguments()).inflate(R.layout.artlistitem, null);
			}
			ArtElement element = getItem(position);
			ImageView image = (ImageView) convertView.findViewById(R.id.thumb);
			TextView title = (TextView) convertView.findViewById(R.id.title);

			Bitmap bitmz = BitmapFactory.decodeFile(element.image.getAbsolutePath());
			int width = bitmz.getWidth();
			int height = bitmz.getHeight();
			int max = PApplet.max(image.getWidth(), image.getHeight());
			if (max <= 0)
				max = 150; // just a good number off the top of my head;
			if (width > height) {
				height = (height * max) / width;
				width = max;
			} else {
				width = (width * max) / height;
				height = max;
			}
			Bitmap scaled = Bitmap.createScaledBitmap(bitmz, width, height, false);
			image.setImageBitmap(scaled);
			title.setText(element.name);

			return convertView;
		}

	}

	public class ImageListLoader extends AsyncTask<String, Void, ArrayList<ArtElement>> {

		@Override
		protected ArrayList<ArtElement> doInBackground(String... params) {
			try {
				ArrayList<ArtElement> list = StorageUtils.getSavedFiles(getActivity());
				Collections.sort(list);
				return list;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(ArrayList<ArtElement> elements) {
			if (elements != null) {
				adapter.addAll(elements);
				adapter.notifyDataSetChanged();
			}
		}

	}

}
