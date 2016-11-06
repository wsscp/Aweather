package com.example.aweather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;



import java.util.ArrayList;
import java.util.List;

import com.example.aweather.R;
import com.example.aweather.db.MaoWeatherDB;
import com.example.aweather.model.City;
import com.example.aweather.util.HttpCallback;
import com.example.aweather.util.HttpUtil;
import com.example.aweather.util.Utility;

/**
 * Created by Johnny on 2016/6/28.
 */
public class CityChooseActivity extends Activity {

    private MaoWeatherDB mMaoWeatherDB;//���ݿ��������
    private ProgressDialog mProgressDialog;//�������Ի���
    private EditText editText;//�����༭��
    private ArrayAdapter<String> mAdapter;//ListView������
    private ListView mListView;//����ListView
    private List<String> cityNames = new ArrayList<>();//���ڴ���������������ƥ��ĳ��������ַ���
    private City mCity_selected;//ѡ�еĳ���
    private List<City> mCities;//���ڴ���������������ƥ��ĳ������ƶ���

    private static final int NONE_DATA = 0;//��ʶ�Ƿ��г�ʼ����������

    private SharedPreferences mSharedPreferences;//���ش洢
    private SharedPreferences.Editor mEditor;//���ش洢

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_citychoose);

        mMaoWeatherDB = MaoWeatherDB.getInstance(this);//��ȡ���ݿ⴦�����
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);//��ȡ���ش洢����
        mEditor = mSharedPreferences.edit();//��ȡ���ش洢����

        //�ȼ�鱾���Ƿ���ͬ�����������ݣ����û�У���ӷ�����ͬ��
        if (mMaoWeatherDB.checkDataState() == NONE_DATA) {
            queryCitiesFromServer();
        }

        mCities = queryCitiesFromLocal("");//��ȡ���ش洢�����еĳ���

        //�����������ı��仯������
        editText = (EditText) findViewById(R.id.edit_city);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                mCities = queryCitiesFromLocal(s.toString());//ÿ���ı��仯��ȥ�������ݿ��ѯƥ��ĳ���
                mAdapter.notifyDataSetChanged();//֪ͨ����
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, cityNames);//��������ʼ��
        mListView = (ListView) findViewById(R.id.list_view_cities);
        mListView.setAdapter(mAdapter);

        //ListView��Item����¼�
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCity_selected = mCities.get(position);//���ݵ����λ�û�ȡ��Ӧ��City����
                queryWeatherFromServer();//���ݵ���ĳ��дӷ�������ȡ��������
            }
        });
    }

    //�ӷ�����ȡ�����еĳ�����Ϣ
    private void queryCitiesFromServer() {
        String address = " https://api.heweather.com/x3/citylist?search=allchina&key=" + MaoWeatherActivity.WEATHER_KEY;
        showProgressDialog();

        HttpUtil.sendHttpRequest(address, new HttpCallback() {
            @Override
            public void onFinish(String response) {
                if (Utility.handleCityResponse(mMaoWeatherDB, response)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            mMaoWeatherDB.updateDataState();
                        }
                    });
                }
            }

            @Override
            public void onError(final Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(CityChooseActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    //�ӱ������ݿ�ȡ�����Ƶĳ�������
    private List<City> queryCitiesFromLocal(String name) {
        List<City> cities = mMaoWeatherDB.loadCitiesByName(name);
        cityNames.clear();
        for (City city : cities) {
            cityNames.add(city.getCity_name_ch());
        }
        return cities;
    }

    //�ӷ�������ȡ��������
    private void queryWeatherFromServer() {

        String address = "https://api.heweather.com/x3/weather?cityid=" + mCity_selected.getCity_code() + "&key=" + MaoWeatherActivity.WEATHER_KEY;
        showProgressDialog();

        HttpUtil.sendHttpRequest(address, new HttpCallback() {
            @Override
            public void onFinish(String response) {
                //���ӷ�������ȡ��JSON���ݽ��н���
                if (Utility.handleWeatherResponse(mEditor, response)) {
                    //ע��������̵߳Ĵ���
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            //�������������ݣ�˵���Ѿ����浽���أ����ǲ����ٰ����ݷ�װ��Intent���淵�ظ�MaoWeatherActivity
                            //������onActivityResult����ӱ��ش洢�л�ȡ
                            setResult(RESULT_OK);
                            finish();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                //ע��������̵߳Ĵ���
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(CityChooseActivity.this, "����ͬ��ʧ��", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    //��ʾ������
    private void showProgressDialog() {

        if (mProgressDialog == null) {

            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setMessage("����ͬ������...");
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }

    //�رս�����
    private void closeProgressDialog() {
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
    }
}
