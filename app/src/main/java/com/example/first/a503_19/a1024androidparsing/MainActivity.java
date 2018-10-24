package com.example.first.a503_19.a1024androidparsing;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    EditText id, passwd;
    LinearLayout linearLayout;
    Button button;
    ProgressDialog progressDialog;

    //스레드로 작업한 후 화면 갱신을 위한 객체
    //1개만 있으면 Message의 what을 구분해서 사용할 수 있기 때문에 바로 인스턴스 생성
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            progressDialog.dismiss();
            if(msg.what == 1){
                linearLayout.setBackgroundColor(Color.RED);
            }else if(msg.what ==2){
                linearLayout.setBackgroundColor(Color.GREEN);
            }

        }
    };

    //비동기적으로 작업을 수행하기 위한 스레드 클래스
    //스레드는 재사용이 안되기 때문에 필요할 때마다 인스턴스를 만들어서 사용하므로
    //클래스를 만들어서 사용합니다.
    class ThreadEx extends Thread{
        @Override
        public void run() {
            try{
                String addr = "http://192.168.0.249:8015/android/login?id=";
                String logid = id.getText().toString();
                String logpw = passwd.getText().toString();
                addr = addr + logid + "&pw=" +logpw;

                //문자열 주소를 URL로 변경
                URL url = new URL(addr);
                //연결 객체 생성
                HttpURLConnection con = (HttpURLConnection)url.openConnection();
                //옵션 설정
                con.setUseCaches(false);//캐시(로컬에 저장해두고 사용) 사용여부
                con.setConnectTimeout(30000);//접속을 시도하는 최대 시간

                //문자열을 다운로드 받을 스트림 생성
                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                //문자열 다운로드
                StringBuilder sb = new StringBuilder();
                while (true){
                    String line = br.readLine();
                    if(line == null)break;
                    sb.append(line + "\n");
                }
                br.close();
                con.disconnect();
                //Log.e("다운로드 받은 데이터",sb.toString());
                //JSON 파싱
                JSONObject result = new JSONObject(sb.toString());
                String x = result.getString("id");
                //파싱한 결과를 가지고 Message의 what을 달리해서 핸들러에게 전송
                Message msg = new Message();
                if(x.equals("null")){
                    //Log.e("로그인 여부","실패");
                    msg.what = 1;
                }else {
                    //Log.e("로그인 여부","성공");
                    msg.what = 2;
                }
            }catch(Exception e){
                Log.e("다운로드실패",e.getMessage());
            }

        }
    }

    //Activity가 만들어 질 때 호출되는 메소드
    //Activity가 실행될 때 무엇인가를 하고자 하는 경우는 onResume
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //layout 파일을 읽어서 메모리에 로드 한 후 화면출력을 준비하는 메소드 호출
        setContentView(R.layout.activity_main);

        id = (EditText)findViewById(R.id.id);
        passwd = (EditText)findViewById(R.id.passwd);
        linearLayout = (LinearLayout)findViewById(R.id.layout01);
        button = (Button)findViewById(R.id.loginButton);

        //버튼을 누르면 수행할 내용
        button.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                //진행 대화상자를 출력
                progressDialog = ProgressDialog.show(MainActivity.this,"로그인","로그인처리중");

                //스레드를 만들어서 실행
                ThreadEx th = new ThreadEx();
                th.start();
            }
        });



    }
}
