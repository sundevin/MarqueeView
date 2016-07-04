# MarqueeView
支持各向循环切换且可点击的自定义控件

本自定义控件是在别人的基础上完善的，在此表示感谢
原作者：https://github.com/oubowu/MarqueeLayoutLibrary

效果就不展示了，和原作者的是一样的，如下

![cmd-markdown-logo](https://github.com/oubowu/MarqueeLayoutLibrary/blob/master/pic/demo.gif)

###改动方面：
1,对设置adapter的方法做了调整，个人认为结构上更合理,舍弃了原有的setCustomView方法的调用，并且在setAdapter后自动开启切换

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
        

2,对item的增加了点击事件，

        marqueeView.setOnItemClickListener(new MarqueeView.OnItemClickListener() {
            @Override
            public void onItemClick(MarqueeView parent, View view, int position) {

                Log.d("----------", "----" + list.size() + "---" + position + "--" + list.get(position));

            }
        });

3,可以使用notify动态的加载数据

        Button button = (Button) findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (list.size() > 0) {
                    list.remove(0);
                }
                marqueeViewAdapter.notifyDataSetChanged();

            }
        });


自定义属性如下

| intervalTime     | 多久滚动一次的时间间隔 | 
| --------   | -----:  |
| orientation        |  切换的方向，有bottomToTop，topToBottom，rightToLeft，leftToRight四种方向   | 
| enableAlphaAnim        |    是否开启子View的透明渐变   |
| enableScaleAnim        |    是否开启子View的缩放渐变   |



xml如下

    <com.devin.marqueeview.MarqueeView
        android:padding="10dp"
        android:id="@+id/marqueeView"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="#00f"
        app:intervalTime="3000"
        app:orientation="bottomToTop" />

