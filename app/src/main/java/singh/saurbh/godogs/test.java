package singh.saurbh.godogs;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

/**
 * Created by ${SAURBAH} on ${10/29/14}.
 */
public class test extends ParseQueryAdapter<ParseObject> {

    private Context mContext;
    private String className;

    public test(Context context, String className) {
        super(context, className);
        this.mContext = context;
        this.className = className;
    }

    @Override
    public View getItemView(ParseObject object, View v, ViewGroup parent) {
        return super.getItemView(object, v, parent);
    }

    @Override
    public void loadObjects() {
        super.loadObjects();
    }

    @Override
    protected void setPageOnQuery(int page, ParseQuery<ParseObject> query) {
        super.setPageOnQuery(page, query);
    }
}
