package com.applico.example.appviewpager;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FragmentExample extends Fragment {
	private static final String TAG = "FragmentExample";
	private final int background;
	private final int index;
	
	public FragmentExample(int bgResource, int position) {
		background = bgResource;
		index = position;
	}
	/**
	 * This class is inflated and attached/detached to the container view group in the Slider class
	 * Fragments are inflated only once when added, then are hidden and shown 
	 * This is simply a view in the activity, the FragmentManager holds on to these views to make it fast
	 */

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
		Log.i(TAG, "create a new view");
        // Inflate the layout for this fragment
        final View fragmentView = inflater.inflate(R.layout.fragment_layout, container, false);
        fragmentView.findViewById(R.id.root).setBackgroundResource(background);
        TextView tv = (TextView) fragmentView.findViewById(R.id.tv);
    	if(tv != null) tv.setText(tv.getText() + " Fragment Id: " + (index+1));

        return fragmentView;
    }
}
