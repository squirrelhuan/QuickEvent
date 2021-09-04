package cn.demomaster.quickevent;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import cn.demomaster.quickevent_library.core.QuickEvent;
import cn.demomaster.quickevent_library.core.Subscribe;
import cn.demomaster.quickevent_library.core.ThreadMode;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        QuickEvent.getDefault().register(this);
    }

    public void test(View view){
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i=0;i<10;i++){
                    Log.i("tag","---s---");
                    QuickEvent.getDefault().post(MainActivity.class,"无敌123");
                    try {
                        Thread.sleep(200);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void test(String msg){
        Log.i("tag",""+System.currentTimeMillis());
        Toast.makeText(this,"123"+msg,Toast.LENGTH_LONG).show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        QuickEvent.getDefault().unRegister(this);
    }
}