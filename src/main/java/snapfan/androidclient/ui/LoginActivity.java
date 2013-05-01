//package snapfan.androidclient.ui;
//
//import android.app.Activity;
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.TextView;
//import snapfan.androidclient.R;
//import snapfan.androidclient.api.ApiHelper;
//
//public class LoginActivity extends Activity {
//    private TextView textViewResponse;
//    private Button buttonLogin;
//    private EditText editTextUserId;
//    private EditText editTextPassword;
//
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        setContentView(R.layout.login);
//
//        textViewResponse = (TextView) findViewById(R.id.textViewResponse);
//        buttonLogin = (Button) findViewById(R.id.buttonLogin);
//        editTextUserId = (EditText) findViewById(R.id.editTextUserId);
//        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
//
//        buttonLogin.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String response = ApiHelper.login(LoginActivity.this.editTextUserId.getText().toString(), LoginActivity.this.editTextPassword.getText().toString());
//                if(response != null) {
//                    textViewResponse.setText(response);
//                    startActivity(new Intent(LoginActivity.this, MainListActivity.class));
//                }
//            }
//        });
//    }
//}