package com.example.aweather.activity;

import com.example.aweather.R;
import com.example.aweather.model.City;
import com.example.aweather.service.AutoUpdateService;
import com.example.aweather.util.HttpCallback;
import com.example.aweather.util.HttpUtil;
import com.example.aweather.util.Utility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;



/**
 * Created by Johnny on 2016/6/28.
 */
public class MaoWeatherActivity extends Activity {

    //�ҵĺͷ�����KEY���������˸��˵�KEY,����ʹ�ã�������ע����ѵĺͷ����������и��˵�KEY���������滻�ͺ��ˣ�
    //public static final String WEATHER_KEY = "################";
    public static final String WEATHER_KEY = "735a9b1324f04671a3e3f27c330dd9b4 ";

    private ProgressDialog mProgressDialog;//������
    private SharedPreferences mSharedPreferences;//���ݴ洢����
    private SharedPreferences.Editor mEditor;
    public static final int REQUEST_CODE = 1;

    private Button mChangeCityButton;//С���Ӱ�ť
    private TextView mTextView_cityName;//��������������
    private Button mRefreshButton;//ˢ�°�ť
    private TextView mTextView_updateTime;//����ʱ��
    private TextView mTextView_current_date;//��ǰ����
    private TextView mTextView_weather_desp;//������������
    private TextView mTextView_textView_temp1;//����¶�
    private TextView mTextView_textView_temp2;//����¶�

    private City mCity_current = new City();//��ǰ��ʾ�ĳ��ж���


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //���׹�������ʵ��
        //AdManager.getInstance(this).init("8c8f79aef6457ac0", "d71c14f920b0e968", false);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_maoweather);

        //ʵ�������ش洢
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mSharedPreferences.edit();

        //������У�С���Ӱ�ť��
        mChangeCityButton = (Button) findViewById(R.id.button_changeCity);
        mChangeCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //��������CityChooseActivity
                Intent intent = new Intent(MaoWeatherActivity.this, CityChooseActivity.class);
                //��Ҫ�󷵻ؽ���ķ�ʽ����
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        //ʵ���������齨
        mTextView_cityName = (TextView) findViewById(R.id.textView_city_name);
        mTextView_updateTime = (TextView) findViewById(R.id.textView_publishTime);
        mTextView_current_date = (TextView) findViewById(R.id.textView_current_date);
        mTextView_weather_desp = (TextView) findViewById(R.id.textView_weather_desp);
        mTextView_textView_temp1 = (TextView) findViewById(R.id.textView_temp1);
        mTextView_textView_temp2 = (TextView) findViewById(R.id.textView_temp2);

        //ˢ�°�ť
        mRefreshButton = (Button) findViewById(R.id.button_refresh);
        mRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //�ӷ���������
                updateWeatherFromServer();
            }
        });

        //�����Ϊ�����ڵ�һ�ΰ�װ��ʱ���жϱ��ش洢��û�����ݣ�����Ĭ�ϻ�ȡ����������
        //�����Ҫ�޸ģ����ԴӺͷ��������� http://www.heweather.com/documents/cn-city-list��ѯ����ID
        if (mSharedPreferences.getString("city_code", null) == null) {
            mCity_current.setCity_code("CN101010100");
            updateWeatherFromServer();
        } else {
            //�����ݣ���ӱ���ȡ������Ҳ�����ϴη��ʵĳ��У���ȷ�����
            loadWeatherData(mSharedPreferences.getString("city_code", null), mSharedPreferences.getString("city_name_ch", null), mSharedPreferences.getString("update_time", null), mSharedPreferences.getString("data_now", null), mSharedPreferences.getString("txt_d", null), mSharedPreferences.getString("txt_n", null), mSharedPreferences.getString("tmp_min", null), mSharedPreferences.getString("tmp_max", null));
            //Ȼ��ӷ���������һ��
            updateWeatherFromServer();//����ע�͵���ʹ�÷�������Զ�����
        }
        //�����Զ����·��񣨲���������û��ôʹ�õ��Զ����£������ﶼ�Ǵ򿪺�ʵʱ���µģ����Դ򿪺󲻴ӷ��������£�ֻ�ӱ��ػ�ȡ��
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);

        //���׹���������������ͣ���һ�д���������ͺ������Ҳ�ܼ�
        //AdView adView = new AdView(this, AdSize.FIT_SCREEN);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.adLayout);
        //linearLayout.addView(adView);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                loadWeatherData(mSharedPreferences.getString("city_code", null), mSharedPreferences.getString("city_name_ch", null), mSharedPreferences.getString("update_time", null), mSharedPreferences.getString("data_now", null), mSharedPreferences.getString("txt_d", null), mSharedPreferences.getString("txt_n", null), mSharedPreferences.getString("tmp_min", null), mSharedPreferences.getString("tmp_max", null));
            }
        }
    }

    //ˢ�¸�������ݵķ�װ
    private void loadWeatherData(String city_code, String city_name, String update_time, String current_data, String txt_d, String txt_n, String tmp_min, String tmp_max) {

        mTextView_cityName.setText(city_name);
        mTextView_updateTime.setText(update_time);
        mTextView_current_date.setText(current_data);

        if (txt_d.equals(txt_n)) {
            mTextView_weather_desp.setText(txt_d);
        } else {
            mTextView_weather_desp.setText(txt_d + "ת" + txt_n);
        }
        mTextView_textView_temp1.setText(tmp_min + "��");
        mTextView_textView_temp2.setText(tmp_max + "��");

        mCity_current.setCity_name_ch(city_name);
        mCity_current.setCity_code(city_code);

    }

    //�ӷ������������ݣ�CityChooseActivity�������Ʒ�����
    private void updateWeatherFromServer() {
        String address = "https://api.heweather.com/x3/weather?cityid=" + mCity_current.getCity_code() + "&key=" + MaoWeatherActivity.WEATHER_KEY;
        showProgressDialog();
        HttpUtil.sendHttpRequest(address, new HttpCallback() {
            @Override
            public void onFinish(final String response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (Utility.handleWeatherResponse(mEditor, response)) {
                            loadWeatherData(mSharedPreferences.getString("city_code", null), mSharedPreferences.getString("city_name_ch", null), mSharedPreferences.getString("update_time", null), mSharedPreferences.getString("data_now", null), mSharedPreferences.getString("txt_d", null), mSharedPreferences.getString("txt_n", null), mSharedPreferences.getString("tmp_min", null), mSharedPreferences.getString("tmp_max", null));
                            closeProgressDialog();
                        }
                    }
                });
            }

            @Override
            public void onError(final Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(MaoWeatherActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void showProgressDialog() {

        if (mProgressDialog == null) {

            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setMessage("����ͬ������...");
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }

    private void closeProgressDialog() {
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
    }
}
