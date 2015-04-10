package singh.saurbh.godogs;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by ${SAURBAH} on ${10/29/14}.
 */
public class RoadMapCreation {

    private Activity mContext;
    private View view;
    private String[] arr_of_interest_list = {
            "Programming",
            "English",
            "Mathematics",
            "Quantitative Reasoning",
            "General Area"
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

    private int[] index_arr = new int[4];
    private String[] arr_to_store_result_of_course_list = new String[4];
    private String[] arr_to_search = new String[arr_of_interest_list.length];
    private ListView lv;
    private TextView headingTextView;
    private Button generate_roadmap_button;

    public RoadMapCreation(Activity mContext, View view) {
        this.mContext = mContext;
        this.view = view;
    }

    public void createRoadMap () {
        Spinner spinner = (Spinner) view.findViewById(R.id.spinner_for_roadmap);

        lv = (ListView)view.findViewById(android.R.id.list);
        headingTextView = (TextView) view.findViewById(R.id.heading_text_for_interest_list);
        generate_roadmap_button = (Button) view.findViewById(R.id.generate_roadmap_button);

        String defaultTextForSpinner = "Select your major";
        spinner.setAdapter(new CustomSpinnerAdapter(mContext, R.layout.spinner_row, arr_of_majors, defaultTextForSpinner));

        generate_roadmap_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateRoadMap();
            }
        });
    }

    public void generateRoadMap() {
        final ProgressDialog dialog = new ProgressDialog(mContext);
        dialog.setTitle("Generating Roadmap");
        dialog.setMessage("Please wait...");
        dialog.setIndeterminate(false);
        dialog.setCancelable(true);
        dialog.show();
        final Random random = new Random();

        List<ParseQuery<ParseObject>> queries = new ArrayList<>();
        for (int i = 0; i < arr_of_interest_list.length; i++) {

            if (arr_to_search[i] != null) {
                Log.d("TAG", "Checked --> "+arr_to_search[i]);
                ParseQuery<ParseObject> query1 = ParseQuery.getQuery("Courses");
                query1.whereEqualTo("tags", arr_to_search[i].toLowerCase());

                queries.add(query1);
            }
        }

        if (queries.size() > 0) {
            ParseQuery<ParseObject> mainQuery = ParseQuery.or(queries);
            mainQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> parseObjects, com.parse.ParseException e) {
//                    dialog.dismiss();
                    if (e == null) {
                        int index = 0;
                        for (ParseObject parseObject: parseObjects) {
                            int x = random.nextInt(parseObject.getJSONArray("courses").length());
                            try {
                                Log.d("TAG", "Retrieved --> " + parseObject.getJSONArray("courses").getString(x));
                                if (index < 4) {
                                    arr_to_store_result_of_course_list[index] = parseObject.getJSONArray("courses").getString(x);
                                    index++;
                                }
                            } catch (JSONException e1) {
                                Log.d("TAG", "Main query JSON Exception: " + e1.getMessage());
                            }
                        }

                        for(int i = 0; i < 4; i++) {
                            if (arr_to_store_result_of_course_list[i] == null)
                                index_arr[i] = i;
                            else
                                index_arr[i] = 404;
                        }

                        List<ParseQuery<ParseObject>> queries1 = new ArrayList<>();
                        for (int i = 0; i < 4; i++) {

                            if (arr_to_store_result_of_course_list[i] != null) {
                                ParseQuery<ParseObject> query1 = ParseQuery.getQuery("Courses");
                                query1.whereNotEqualTo("courses", arr_to_store_result_of_course_list[i]);

                                queries1.add(query1);
                            }
                            ParseQuery<ParseObject> mainQuery1 = ParseQuery.or(queries1);
                            mainQuery1.findInBackground(new FindCallback<ParseObject>() {
                                @Override
                                public void done(List<ParseObject> list, ParseException e) {
                                    dialog.dismiss();
                                    if (e == null) {
                                        int index = 0;
                                        for (ParseObject parseObject: list) {
                                            int x = random.nextInt(parseObject.getJSONArray("courses").length());
                                            try {
                                                if (index < 4) {
                                                    if (index_arr[index] != 404)
                                                        arr_to_store_result_of_course_list[index_arr[index]] = parseObject.getJSONArray("courses").getString(x);
                                                    index++;
                                                }
                                            } catch (JSONException e1) {
                                                Log.d("TAG", "Main query JSON Exception: " + e1.getMessage());
                                            }
                                        }
                                    } else {
                                        Log.d("TAG", "Main Query1 Exception: "+e.getMessage());
                                    }
                                }
                            });
                        }
                        for (int i = 0; i < 4; i++)
                            Log.d("TAG", "Course "+i+": "+arr_to_store_result_of_course_list[i]);
                    } else {
                        Log.d("TAG", "Main Query Exception: "+e.getMessage());
                    }
                }
            });
        } else {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Courses");
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> parseObjects, com.parse.ParseException e) {
                    dialog.dismiss();
                    if (e == null) {
                        int index = 0;
                        for (ParseObject parseObject: parseObjects) {
                            //TODO: implement random object picking
                            int x = random.nextInt(parseObject.getJSONArray("courses").length());
                            try {
                                Log.d("TAG", "(No Selection) Retrieved --> " + parseObject.getJSONArray("courses").getString(x));
                                if (index < 4) {
                                    arr_to_store_result_of_course_list[index] = parseObject.getJSONArray("courses").getString(x);
                                    index++;
                                }
                            } catch (JSONException e1) {
                                Log.d("TAG", "(No Selection) Main query JSON Exception: " + e1.getMessage());
                            }
                        }
                    } else {
                        Log.d("TAG", "(No Selection) Main Query Exception: " + e.getMessage());
                    }
                }
            });
        }



//        ParseQuery<ParseObject> query = ParseQuery.getQuery("Courses");
//        query.whereEqualTo("tags", arr_to_search[0]);
//        query.getFirstInBackground(new GetCallback<ParseObject>() {
//            @Override
//            public void done(ParseObject parseObject, ParseException e) {
//                if (e == null) {
//                    try {
//                        Log.d("TAG", "Obj: "+parseObject.getJSONArray("courses").getString(0));
//                    } catch (JSONException e1) {
//                        Log.d("TAG", "Test query JSON Exception: "+e1.getMessage());
//
//                    }
//                } else {
//                    Log.d("TAG", "Test query Exception: "+e.getMessage());
//                }
//            }
//        });
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
                headingTextView.setVisibility(View.INVISIBLE);
                lv.setVisibility(View.INVISIBLE);
                generate_roadmap_button.setVisibility(View.INVISIBLE);
            } else {
                ArrayList<String> arrayList = new ArrayList<>(arr_of_interest_list.length);
                Collections.addAll(arrayList, arr_of_interest_list);

                ArrayAdapter<String> listAdapter = new InteractiveArrayAdapterForInterestList(mContext, arrayList);

                headingTextView.setVisibility(View.VISIBLE);
                lv.setVisibility(View.VISIBLE);
                generate_roadmap_button.setVisibility(View.VISIBLE);
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

    public class InteractiveArrayAdapterForInterestList extends ArrayAdapter<String> {

        private final ArrayList<String> list;
        private final Activity context;
        private ViewHolder holder;

        public InteractiveArrayAdapterForInterestList(Activity context,
                                                      ArrayList<String> list) {
            super(context, R.layout.single_item_for_roadmap_interest_list, list);
            this.context = context;
            this.list = list;
        }

        class ViewHolder {
            protected TextView interest_textView;
            protected CheckBox checkbox;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View single_reply_for_list_view;
            for (int i = 0; i < arr_to_search.length; i++)
                arr_to_search[i] = null;

            if (convertView == null) {
                LayoutInflater inflator = context.getLayoutInflater();
                single_reply_for_list_view = inflator.inflate(R.layout.single_item_for_roadmap_interest_list, null);
                final ViewHolder viewHolder = new ViewHolder();
                viewHolder.interest_textView = (TextView) single_reply_for_list_view.findViewById(R.id.interest_textView);
                viewHolder.checkbox = (CheckBox) single_reply_for_list_view.findViewById(android.R.id.checkbox);

                viewHolder.checkbox
                        .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if (isChecked)
                                    arr_to_search[position] = list.get(position);
                                else
                                    arr_to_search[position] = null;
                            }
                        });
                single_reply_for_list_view.setTag(viewHolder);
                viewHolder.checkbox.setTag(list.get(position));
            } else {
                single_reply_for_list_view = convertView;
                ((ViewHolder) single_reply_for_list_view.getTag()).checkbox.setTag(list.get(position));
            }
            holder = (ViewHolder) single_reply_for_list_view.getTag();
            holder.interest_textView.setText(list.get(position));

            return single_reply_for_list_view;
        }
    }
}
