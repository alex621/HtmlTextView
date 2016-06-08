package com.lolapplication;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String html = "<b>test</b>";
        html = "<figure class=\"tmblr-full\" height=\"581\" width=\"816\">lol<img src=\"https://67.media.tumblr.com/0b8143e6e8ce6316203a7f1b371e3f55/tumblr_inline_o7qyz2wJqY1qj4elg_540.png\" height=\"381\" width=\"416\" /></figure><p>As an education Volunteer in Tanzania (2012-15), I grew very close to some of the girls at my school. <img src=\"http://rethinkingtruth.com/wp-content/uploads/2013/11/sleep-like-a-baby.jpg?w=500\" width=\"400\" height=\"300\" />I held after-school activities and girls clubs, and simply enjoyed spending time with “my girls.” We talked about everything, and the subject of menstruation first came up when my girls asked me to help them prepare hot water bottles to relieve their menstrual cramps.</p><p>As conversations around this topic continued, I quickly discovered that menstruation for a young girl in the developing world isn’t just a monthly annoyance but a major problem. Without money to buy sanitary pads, girls use unhealthy substitutes like old clothes, mattress pieces and tissue. When these substitutes don’t work, they stain their skirts and suffer embarrassment and shame in public places; many decide to skip school on these days altogether. Even with proper materials, many girls don’t have access to suitable facilities at school or at home to discretely and hygienically manage their periods. And on top of everything, girls and boys lack the education they need about puberty, so girls feel confused and ashamed about their periods.</p><p>Menstrual Hygiene Management (MHM) is a cross-cutting issue that affects girls in all corners of the world. Since 2013, the momentum surrounding MHM has materialized through increased global research and programming efforts, as well as a worldwide recognition of May 28 as Menstrual Hygiene Day.</p><figure class=\"tmblr-full\" data-orig-height=\"640\" data-orig-width=\"960\"><img src=\"https://66.media.tumblr.com/bd04bcc59a1531c33879523eb5275e88/tumblr_inline_o7qyzhxoe91qj4elg_540.jpg\" height=\"640\" width=\"1960\"/></figure><p>The momentum surrounding MHM has also materialized in the work of Peace Corps Volunteers all around the world:</p><ul><li>Many Volunteers have begun teaching young women in their communities how to make their own cloth sanitary pads. In Uganda, <a href=\"https://passport.peacecorps.gov/2016/01/15/creating-change-with-reusable-menstrual-pads/\" target=\"_blank\">“RUMPS (Re-Usable Menstrual Pads)”</a> is a make-your-own-pads program that has seen great success.</li></ul><ul><li>In many countries, Let Girls Learn and Water Charity have teamed up to support Volunteers in building girl-friendly WASH facilities. For example, in Cambodia the Svey Leu High School Latrine Project aims to give girls better facilities and eventually improve their attendance rates, especially during menstruation.</li></ul><ul><li>In Tanzania, Volunteers and their counterparts are working with an organization called Huru International, using kits with reusable sanitary pads as an entry point for educating girls and their communities about menstruation, sexual health and the importance of girls’ education.</li></ul><ul><li>The very first Let Girls Learn-funded <a href=\"https://passport.peacecorps.gov/2015/11/12/generation-glow-emancipation-is-not-an-import/\" target=\"_blank\">GLOW (Girls Leading our World) Camp</a> happened in Albania last year and several more have followed. Volunteers use these camps to empower young women and teach them about their bodies, puberty and menstruation.</li></ul><p>This year, supported by Let Girls Learn and the Office of Global Health and HIV, Peace Corps Volunteers from all around the world will be celebrating Menstrual Hygiene Day with education and awareness activities throughout the month of May. You can follow their stories and learn about why Menstruation Matters to Everyone, Everywhere! on <a href=\"https://twitter.com/peacecorps\" target=\"_blank\">Twitter</a>, <a href=\"https://www.facebook.com/peacecorps\" target=\"_blank\">Facebook</a>, <a href=\"https://www.instagram.com/peacecorps/\" target=\"_blank\">Instagram </a>and <a href=\"http://peacecorps.tumblr.com/\" target=\"_blank\">Tumblr</a>. </p>";

        final HtmlTextView tv = (HtmlTextView) findViewById(R.id.lol);
        tv.setRenderer(new MyAdapter());
        tv.setHtml(html);

        ObservableScrollView scroller = (ObservableScrollView) findViewById(R.id.scroller);
        scroller.setScrollViewListener(new ObservableScrollView.ScrollViewListener() {
            @Override
            public void onScrollChanged(ObservableScrollView scrollView, int x, int y, int oldx, int oldy) {
                tv.recycleCheck();
            }
        });
    }

    public static class MyAdapter extends HtmlTextView.HtmlTextViewAdapter{
        @Override
        public HtmlTextView.ViewHolder renderImg(Context context, String src, HtmlTextView.ViewHolder oldViewHolder) {
            if (oldViewHolder == null){
                View v = new View(context);
                v.setBackgroundColor(0x330000FF);
                oldViewHolder = new HtmlTextView.ViewHolder(v){};
            }


            return oldViewHolder;
        }
    }
}
