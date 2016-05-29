package com.andersoncarvalho.spherogo;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import java.util.ArrayList;

/**
 * Created by anderson on 29/05/16.
 */
public class MovimentoList extends ArrayAdapter<Integer> {

    private final Activity context;

    private final ArrayList<Integer> movimento;
    public MovimentoList(Activity context,
                         ArrayList<Integer> movimento) {
        super(context, R.layout.item_lista, movimento);
        this.context = context;
        this.movimento = movimento;

    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.item_lista, null, true);

        ImageView imageView = (ImageView) rowView.findViewById(R.id.img);

        imageView.setImageResource(movimento.get(position));
        return rowView;
    }
}
