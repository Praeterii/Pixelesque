package praeterii.pixels.artlist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import praeterii.pixele.R;
import praeterii.pixels.artlist.ArtListFragment.ArtElement;
import praeterii.pixels.artlist.ArtListFragment.ArtItemSelectedListener;

public class ArtListActivity extends FragmentActivity implements ArtItemSelectedListener {
	public final static String PATH = "path";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.listactivity);
        
		ArtListFragment details = new ArtListFragment();
		details.setListener(this);
		details.setArguments(getIntent().getExtras());
		getSupportFragmentManager().beginTransaction().add(R.id.content, details).commit();
        
    }

	@Override
	public void onArtItemSelected(ArtElement element) {
		Intent resultIntent = new Intent();

		resultIntent.putExtra(PATH, element.image.getAbsolutePath());
		
		setResult(Activity.RESULT_OK, resultIntent);

		finish();
	}
    
}
