package swar8080.collaborativedrawing;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class LobbyActivity extends Activity {

    private Button discoverButton, advertiseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        discoverButton = (Button)findViewById(R.id.startDiscoverButton);
        advertiseButton = (Button)findViewById(R.id.startAdvertiseButton);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    public void onClickDiscoverOrAdvertise(View view){
        if (R.id.startDiscoverButton == view.getId()){
            startDiscovering();
        }
        else if (R.id.startAdvertiseButton == view.getId()){
            startAdvertising();
        }
    }

    private void startDiscovering() {
        Intent intent = new Intent();
        intent.setClass(this, ClientDrawingActivity.class);
        startActivity(intent);
    }

    private void startAdvertising() {
        Intent intent = new Intent();
        intent.setClass(this, HostDrawingActivity.class);
        startActivity(intent);
    }
}


