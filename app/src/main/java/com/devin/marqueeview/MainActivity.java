package com.devin.marqueeview;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final List<String> list = new ArrayList<>();

        list.add("1");
        list.add("2");
        list.add("3");
        list.add("4");
        list.add("5");

        final MarqueeView marqueeView = (MarqueeView) findViewById(R.id.marqueeView);

        final MarqueeViewAdapter marqueeViewAdapter = new MarqueeViewAdapter<String>(list) {
            @Override
            public View getView(MarqueeView parent, final int position, String o) {

                final View view = LayoutInflater.from(getApplication()).inflate(R.layout.item, marqueeView, false);

                TextView textView = (TextView) view.findViewById(R.id.textView);

                textView.setText(list.get(position));

                return view;
            }
        };

        marqueeView.setAdapter(marqueeViewAdapter);

        marqueeView.setOnItemClickListener(new MarqueeView.OnItemClickListener() {
            @Override
            public void onItemClick(MarqueeView parent, View view, int position) {

                Log.d("----------", "----" + list.size() + "---" + position + "--" + list.get(position));

            }
        });


        Button button = (Button) findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (list.size() > 0) {
                    list.remove(0);
                    Log.d("---------","------list.size() =="+list.size() );
                }

                marqueeViewAdapter.notifyDataSetChanged();

            }
        });


    }
}
