package com.bt.chat.v25;
import android.app.Activity;
import android.bluetooth.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import java.io.*;

public class MainActivity extends Activity {
    private final UUID UUID_SERIAL = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter bAdapter;
    private BluetoothSocket bSocket;
    private TextView logView;
    private EditText input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40,40,40,40);
        logView = new TextView(this);
        logView.setText("V25 NATIVE - Prêt\n");
        layout.addView(logView);
        Button btn = new Button(this);
        btn.setText("SCAN & CONNECTER");
        btn.setOnClickListener(v -> connectBonded());
        layout.addView(btn);
        input = new EditText(this);
        layout.addView(input);
        Button s = new Button(this);
        s.setText("ENVOYER");
        s.setOnClickListener(v -> send());
        layout.addView(s);
        setContentView(layout);
        bAdapter = BluetoothAdapter.getDefaultAdapter();
        if(android.os.Build.VERSION.SDK_INT >= 31) {
            requestPermissions(new String[]{"android.permission.BLUETOOTH_SCAN","android.permission.BLUETOOTH_CONNECT","android.permission.ACCESS_FINE_LOCATION"}, 1);
        }
        startServer();
    }

    private void startServer() {
        new Thread(() -> {
            try {
                BluetoothServerSocket server = bAdapter.listenUsingInsecureRfcommWithServiceRecord("BTCHAT", UUID_SERIAL);
                bSocket = server.accept();
                loop();
            } catch (Exception e) {}
        }).start();
    }

    private void connectBonded() {
        Set<BluetoothDevice> paired = bAdapter.getBondedDevices();
        for(BluetoothDevice d : paired) {
            new Thread(() -> {
                try {
                    bSocket = d.createInsecureRfcommSocketToServiceRecord(UUID_SERIAL);
                    bSocket.connect();
                    loop();
                } catch (Exception e) {}
            }).start();
        }
    }

    private void loop() {
        runOnUiThread(() -> logView.append("CONNECTÉ\n"));
        try {
            InputStream is = bSocket.getInputStream();
            byte[] buf = new byte[1024];
            while(true) {
                int len = is.read(buf);
                if(len > 0) {
                    String m = new String(buf, 0, len);
                    runOnUiThread(() -> logView.append("Ami: " + m + "\n"));
                }
            }
        } catch (Exception e) {}
    }

    private void send() {
        try {
            String m = input.getText().toString();
            bSocket.getOutputStream().write(m.getBytes());
            logView.append("Moi: " + m + "\n");
            input.setText("");
        } catch (Exception e) {}
    }
}