package independent_study.ultimatetictactoe.gui;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

import independent_study.ultimatetictactoe.R;
import independent_study.ultimatetictactoe.game.UltimateTickTacToeBoard;
import independent_study.ultimatetictactoe.util.GameBackgroundService;

import static independent_study.ultimatetictactoe.gui.GameActivity.BOARD_TAG;

public class GameListActivity extends AppCompatActivity
{
    public static final String PREFERENCES_KEY = "gameStore";
    private static final int PERMISSIONS_KEY = 34809;
    private static final String BASE_STORAGE_STRING = "Game";

    private ListView listView;
    private FloatingActionButton fab;
    private ArrayAdapter<String> arrayAdapter;

    private ArrayList<UltimateTickTacToeBoard> boards;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_list);
        sharedPreferences = getSharedPreferences(PREFERENCES_KEY, MODE_PRIVATE);
        listView = findViewById(R.id.listViewList);
        fab = findViewById(R.id.floatingActionButtonList);
        arrayAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, android.R.id.text1);
        boards = new ArrayList<>();

        checkAndCallPermissions();
        loadSavedGames();

        Bundle gameSetup = getIntent().getExtras();
        if(gameSetup != null && !gameSetup.isEmpty())
        {
            String boardSerial = gameSetup.getString(BOARD_TAG);
            boards.add(UltimateTickTacToeBoard.fromString(boardSerial));
            saveLocalGames();
        }

        Intent serviceIntent = new Intent(getApplicationContext(), GameBackgroundService.class);
        getApplication().startService(serviceIntent);

        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                Intent gameIntent = new Intent(getApplicationContext(), GameActivity.class);
                gameIntent.putExtra(GameActivity.BOARD_TAG, boards.get(i).toString());
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

        sharedPreferences.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener()
        {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
            {
                loadSavedGames();
            }
        });
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
    }

    private void loadSavedGames()
    {
        int index = 0;
        boards.clear();

        try
        {
            while (sharedPreferences.contains(BASE_STORAGE_STRING + index))
            {
                String storedString = sharedPreferences.getString(BASE_STORAGE_STRING + index, "");
                if(!storedString.equals(""))
                    boards.add(UltimateTickTacToeBoard.fromString(storedString));
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        ArrayList<String> boardStrings = new ArrayList<>();
        for(int i = 0; i < boards.size(); i++)
        {
            boardStrings.add(String.format(Locale.US, "Game %d Against %d", i, boards.get(i).getPhoneNumber()));
        }
        arrayAdapter.clear();
        arrayAdapter.addAll(boardStrings);
    }

    private void saveLocalGames()
    {
        try
        {
            SharedPreferences.Editor edit = sharedPreferences.edit();
            for(int i = 0; i < boards.size(); i++)
            {
                edit.putString(BASE_STORAGE_STRING + i, boards.get(i).toString());
            }
            edit.commit();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private boolean checkAndCallPermissions()
    {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS )!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS,
                    Manifest.permission.NFC, Manifest.permission.READ_CONTACTS}, PERMISSIONS_KEY);
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
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
                    && (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS) ||
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
