package singh.saurbh.godogs;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class InteractiveArrayAdapter extends ArrayAdapter<HashMap<String, String>> {

    private final ArrayList<HashMap<String, String>> list;
    private final Activity context;
    private final Boolean flag;
    private static ViewHolder holder ;
    public static Boolean[] checkList;
    int j = 0;

    public InteractiveArrayAdapter(Activity context,
                                   ArrayList<HashMap<String, String>> list,
                                   Boolean flag) {
        super(context, R.layout.single_post, list);
        this.context = context;
        this.list = list;
        this.flag = flag;
    }

    static class ViewHolder {
        protected TextView first_name, title, published_date;
        protected CheckBox checkbox;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View single_post_for_list_view ;

        if (convertView == null) {
            LayoutInflater inflator = context.getLayoutInflater();
            single_post_for_list_view = inflator.inflate(R.layout.single_post, null);
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.first_name = (TextView) single_post_for_list_view.findViewById(R.id.first_name_for_discussion_forum);
            viewHolder.title = (TextView) single_post_for_list_view.findViewById(R.id.title_for_discussion_forum);
            viewHolder.published_date = (TextView) single_post_for_list_view.findViewById(R.id.date_time_single_post_for_discussion_forum);
            viewHolder.checkbox = (CheckBox) single_post_for_list_view.findViewById(R.id.checkBox_comment);

            checkList = new Boolean[list.size()];
            for(int i = 0; i < list.size(); i++)
                checkList[i] = false;

            if (flag) {
                j++;
                viewHolder.checkbox.setVisibility(View.VISIBLE);
                viewHolder.checkbox
                        .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                checkList[position] = isChecked;
                            }
                        });

            } else {
                viewHolder.checkbox.setVisibility(View.INVISIBLE);
            }
            single_post_for_list_view.setTag(viewHolder);
            viewHolder.checkbox.setTag(list.get(position));
        } else {
            single_post_for_list_view = convertView;
            ((ViewHolder) single_post_for_list_view.getTag()).checkbox.setTag(list.get(position));
        }

        holder = (ViewHolder) single_post_for_list_view.getTag();
        holder.title.setText(list.get(position).get("title"));
        holder.first_name.setText(list.get(position).get("firstName"));
        holder.published_date.setText(list.get(position).get("createdAt"));

        return single_post_for_list_view;
    }
}