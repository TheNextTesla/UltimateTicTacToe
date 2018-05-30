package independent_study.ultimatetictactoe.gui;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Locale;

import independent_study.ultimatetictactoe.R;
import independent_study.ultimatetictactoe.game.UltimateTickTacToeBoard;
import independent_study.ultimatetictactoe.sms.BroadcastReceiverSMS;
import independent_study.ultimatetictactoe.sms.ListenerSMS;
import independent_study.ultimatetictactoe.util.GameBackgroundService;
import independent_study.ultimatetictactoe.util.ListenerGameUpdate;

import static independent_study.ultimatetictactoe.gui.GameActivity.BOARD_TAG;

public class GameListActivity extends AppCompatActivity implements ListenerGameUpdate
{
    public static final String REMOVE_FROM_LIST_KEY = "rem";
    private static final int PERMISSIONS_KEY = 34809;
    private static final String LOG_TAG = "GameListActivity";

    private ListView listView;
    private FloatingActionButton fab;
    private ArrayAdapter<String> arrayAdapter;

    private GameListActivity gameListActivity;
    private GameBackgroundService gameService;
    private ServiceConnection serviceConnection;
    private boolean isBound;

    private String toBeDeleted;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_list);

        gameListActivity = this;
        listView = findViewById(R.id.listViewList);
        fab = findViewById(R.id.floatingActionButtonList);
        arrayAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, android.R.id.text1);
        isBound = false;

        checkAndCallPermissions();

        if (!GameBackgroundService.serviceStarted)
        {
            Intent serviceIntent = new Intent(getApplicationContext(), GameBackgroundService.class);
            getApplication().startService(serviceIntent);
        }

        Bundle gameSetup = getIntent().getExtras();
        try
        {
            if(gameSetup != null && !gameSetup.isEmpty())
            {
                toBeDeleted = gameSetup.getString(REMOVE_FROM_LIST_KEY);
            }
            else
            {
                toBeDeleted = null;
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                Intent gameIntent = new Intent(getApplicationContext(), GameActivity.class);
                gameIntent.putExtra(GameActivity.BOARD_TAG, gameService.getBoards().get(i).toString());
                startActivity(gameIntent);
            }
        });

        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent createGameIntent = new Intent(getApplicationContext(), GameCreateActivity.class);
                startActivity(createGameIntent);
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if(serviceConnection == null)
        {
            serviceConnection = new ServiceConnection()
            {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder iBinder)
                {
                    GameBackgroundService.GameBackgroundBinder binder = (GameBackgroundService.GameBackgroundBinder) iBinder;
                    gameService = binder.getService();
                    gameService.addListener(gameListActivity);
                    isBound = true;
                    if(toBeDeleted != null);

                    onGameUpdate(gameService.getBoardsCopy());
                    Log.d(LOG_TAG, "Service Bound!");
                }

                @Override
                public void onServiceDisconnected(ComponentName componentName)
                {
                    isBound = false;
                }
            };
        }
        Intent intent = new Intent(this, GameBackgroundService.class);
        bindService(intent, serviceConnection, Context.BIND_ABOVE_CLIENT);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        checkAndCallPermissions();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if(isBound)
        {
            gameService.removeListener(this);
            unbindService(serviceConnection);
        }
    }

    @Override
    public void onGameUpdate(ArrayList<UltimateTickTacToeBoard> boards)
    {
        ArrayList<String> listOutput = new ArrayList<>();
        for(int i = 0; i < boards.size(); i++)
        {
            listOutput.add(String.format(Locale.US, "Game %d Against %d", i, boards.get(i).getPhoneNumber()));
        }
        arrayAdapter.clear();
        arrayAdapter.addAll(listOutput);
    }

    private boolean checkAndCallPermissions()
    {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS )!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS,
                    Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS}, PERMISSIONS_KEY);
            return false;
        }
        else
        {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        if (requestCode == PERMISSIONS_KEY)
        {
            if((ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
                    && (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS) ||
                    !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE) ||
                    !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)))
            {
                Toast.makeText(this, "Please Authorize Permissions", Toast.LENGTH_SHORT).show();
            }
            else
            {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
        else
        {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
