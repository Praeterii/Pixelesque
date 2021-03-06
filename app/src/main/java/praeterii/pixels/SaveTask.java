package praeterii.pixels;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import praeterii.pixele.R;

public class SaveTask extends AsyncTask<Void, Void, Boolean> {
	private String name;
	private int width, height;
	private PixelArt data;
	private ProgressDialog dialog;
	private File location;
	private File file;
	private boolean export;
	private boolean share;
	PixelArtEditor context;
	
	public SaveTask(String name, int width, int height, PixelArt data, PixelArtEditor context) {
		this(name, width, height, data, context, null, false, false);			
	}
	public SaveTask(String name, int width, int height, PixelArt data, PixelArtEditor context, File location, boolean export, boolean share) {
		this.name = name; 
		this.width = width; 
		this.height = height; 
		this.data = data; 
		this.export = export;
		this.share = share;
		this.context = context;
		this.location = location;
		if (location == null) {
			this.location = StorageUtils.getSaveDirectory(context);
		}
		if (name == null) {
			String time = System.currentTimeMillis()+"";
			name = "Pixelesque-"+time.substring(time.length()-5);
		}
		this.location.mkdirs();
		file = new File(this.location, name+".png");
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		String title = context.getResources().getString(R.string.saving_title);
		String text = context.getResources().getString(R.string.saving_text);
		dialog = ProgressDialog.show(context, title, text);
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {
		try {
			Bitmap image;
			if (width < 0 && height < 0)
				image = data.render();
			else {
				if (width < 0)
					width = (height * data.width) / data.height;
				if (height < 0) 
					height = (width * data.height) / data.width;
				image = data.render(width, height);
			}
			
			StorageUtils.saveFile(name, file, image, context, !share && !export);
			ArtExtras.saveExtras(context, data, name);
			
			if (share || export) {
			    context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new MediaScanTask().execute();
                    }
                });
			}
			return true;
			

		} catch (Exception e) {
			e.printStackTrace();
		}				
		return false;

	}
	
	public class MediaScanTask extends AsyncTask<String, Void, Void> {
		@Override
		protected Void doInBackground(String... params) {
			// Tell the media scanner about the new file so that it is
	        // immediately available to the user.
	        MediaScannerConnection.scanFile(context,
	                new String[] { file.toString() }, null,
	                new MediaScannerConnection.OnScanCompletedListener() {
	            public void onScanCompleted(String path, Uri uri) {
	                Log.i("ExternalStorage", "Scanned " + path + ":");
	                Log.i("ExternalStorage", "-> uri=" + uri);
	            }
	        });
			return null;
		}
	}
	
	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		dialog.dismiss();
		if (result == false) {
			Toast.makeText(context, R.string.save_failed, Toast.LENGTH_SHORT).show();
		}
		if (export) {
			Log.d("SaveTask", "Exporting...");
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.parse("file://" + file.getAbsolutePath()), "image/*");

			// tells your intent to get the contents
			// opens the URI for your image directory on your sdcard
			context.startActivityForResult(intent, 1);
		} if (share) {
			Log.d("SaveTask", "Sharing...");
			Intent share = new Intent(Intent.ACTION_SEND);
			share.setType("image/jpeg");

			share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + file.getAbsolutePath()));

			String shareimage = context.getResources().getString(R.string.share_image);
			context.startActivity(Intent.createChooser(share, shareimage));

		} else {
			context.artChangedName();
		}
	}
	
}