package kz.develop.lexicon;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Rav_4 on 09.06.2014.
 */
public class ImageListAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final LayoutInflater inflater;
    private final String[] items;
    private final int[] images;

    public ImageListAdapter(Context context, LayoutInflater inflater, String[] items, int[] images) {
        super(context, R.layout.simple_item, items);
        this.items = items;
        this.images = images;
        this.context = context;
        this.inflater = inflater;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = inflater.inflate(R.layout.image_list_item, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.txt);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
        txtTitle.setText(items[position]);
        imageView.setImageResource(images[position]);
        return rowView;
    }
}
