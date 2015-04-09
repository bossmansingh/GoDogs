package singh.saurbh.godogs;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONException;

/**
 * Created by ${SAURBAH} on ${10/29/14}.
 */
public class RoadMapCreation {

    private Activity mContext;
    private View view;
    private String[] arr_of_interest_list = {
            "Art",
            "Science",
            "Agriculture",
            "Business"
    };

    private String[] arr_of_majors = {
            "Art",
            "Biology",
            "Business",
            "Computer Science",
            "Engineering",
            "French",
            "Geology",
            "Mathematics",
            "Physics"
    };

    private ListView lv;
    private TextView emptyTextView;

    public RoadMapCreation(Activity mContext, View view) {
        this.mContext = mContext;
        this.view = view;
    }

    public void createRoadMap () {
        Spinner spinner = (Spinner) view.findViewById(R.id.spinner_for_roadmap);

        lv = (ListView)view.findViewById(R.id.course_interest_listView);
        emptyTextView = (TextView) view.findViewById(R.id.heading_text_for_interest_list);

        String defaultTextForSpinner = "Select your major";
        spinner.setAdapter(new CustomSpinnerAdapter(mContext, R.layout.spinner_row, arr_of_majors, defaultTextForSpinner));

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Courses");
        query.whereEqualTo("areas", "GE Area A2-Written Communication");
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e == null) {
                    try {
                        Log.d("Obj", parseObject.getJSONArray("courses").getString(0));
                    } catch (JSONException e1) {
                        Log.d("Exception", e1.getMessage());

                    }
                }
            }
        });
    }

    public class CustomSpinnerAdapter extends ArrayAdapter<String>{

        Activity context;
        String[] objects;
        String firstElement;
        boolean isFirstTime;

        public CustomSpinnerAdapter(Activity context, int textViewResourceId, String[] objects, String defaultText) {
            super(context, textViewResourceId, objects);
            this.context = context;
            this.objects = objects;
            this.isFirstTime = true;
            setDefaultText(defaultText);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if(isFirstTime) {
                objects[0] = firstElement;
                isFirstTime = false;
            }
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            notifyDataSetChanged();
            if (objects[0].compareTo(firstElement) != 0) {
                emptyTextView.setVisibility(View.INVISIBLE);
                lv.setVisibility(View.INVISIBLE);
            } else {
                ArrayAdapter<String> listAdapter =
                        new ArrayAdapter<>(mContext,
                                R.layout.single_item_for_roadmap_interest_list,
                                R.id.interest_textView,
                                arr_of_interest_list);

                emptyTextView.setVisibility(View.VISIBLE);
                lv.setVisibility(View.VISIBLE);
                lv.setAdapter(listAdapter);
            }
            return getCustomView(position, convertView, parent);
        }

        public void setDefaultText(String defaultText) {
            this.firstElement = objects[0];
            objects[0] = defaultText;
        }

        public View getCustomView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = inflater.inflate(R.layout.spinner_row, parent, false);
            TextView label = (TextView) row.findViewById(R.id.spinner_label_text);
            label.setText(objects[position]);

            return row;
        }

    }
}
