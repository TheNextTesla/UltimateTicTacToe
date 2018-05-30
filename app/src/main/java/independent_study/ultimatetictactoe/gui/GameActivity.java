package independent_study.ultimatetictactoe.gui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import independent_study.ultimatetictactoe.R;
import independent_study.ultimatetictactoe.game.GameMessage;
import independent_study.ultimatetictactoe.game.UltimateTickTacToeBoard;
import independent_study.ultimatetictactoe.sms.TransmitterSMS;

public class GameActivity extends AppCompatActivity
{
    public static final String BOARD_TAG = "Board";
    private static final String LOG_TAG = "GameActivity";

    private GameActivity gameActivity;
    private TicTacToeView ticTacToeView;
    private FloatingActionButton fabGo;
    private FloatingActionButton fabReset;
    private UltimateTickTacToeBoard board;
    private UltimateTickTacToeBoard boardCopy;
    private long number;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        gameActivity = this;

        Bundle gameSetup = getIntent().getExtras();
        final String boardSerial = gameSetup.getString(BOARD_TAG);
        board = UltimateTickTacToeBoard.fromString(boardSerial);
        boardCopy = (UltimateTickTacToeBoard) board.clone();
        number = board.getPhoneNumber();

        ticTacToeView = findViewById(R.id.ticTacToeView);
        fabGo = findViewById(R.id.floatingActionButtonGameSend);
        fabReset = findViewById(R.id.floatingActionButtonGameClear);
        ticTacToeView.setColor(UltimateTickTacToeBoard.BOARD_STATE.RED);
        ticTacToeView.setBoard(board);
        ticTacToeView.invalidate();

        fabGo.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(ticTacToeView.isValidPieceChoosen())
                {
                    Pair<UltimateTickTacToeBoard.BOARD_LOCATION, UltimateTickTacToeBoard.BOARD_LOCATION> locations =
                             new Pair<>(ticTacToeView.getMagnifiedLocation(), ticTacToeView.getSubMagnifiedLocation());
                    board.setLastChangedLocation(locations);

                    Log.d(LOG_TAG, board.toString());
                    GameMessage gameMessage = new GameMessage(board);
                    Log.d(LOG_TAG, gameMessage.getMessage());
                    TransmitterSMS.getInstance().sendSMS(gameMessage.getPhoneNumber(), gameMessage.getMessage(), gameActivity);
                    AlertDialog.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    {
                        builder = new AlertDialog.Builder(gameActivity, android.R.style.Theme_Material_Dialog_Alert);
                    }
                    else
                    {
                        builder = new AlertDialog.Builder(gameActivity);
                    }
                    builder.setTitle("Game Update Sent");
                    builder.setIcon(android.R.drawable.ic_dialog_alert);
                    builder.setNeutralButton("OK", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            Intent returnIntent = new Intent(getApplicationContext(), GameListActivity.class);
                            returnIntent.putExtra(GameListActivity.REMOVE_FROM_LIST_KEY, boardCopy.toString());
                            startActivity(returnIntent);
                        }
                    });
                    builder.show();
                }
                else
                {
                    AlertDialog.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    {
                        builder = new AlertDialog.Builder(gameActivity, android.R.style.Theme_Material_Dialog_Alert);
                    }
                    else
                    {
                        builder = new AlertDialog.Builder(gameActivity);
                    }
                    builder.setTitle("Select a Valid Location");
                    builder.setIcon(android.R.drawable.ic_dialog_alert);
                    builder.setNeutralButton("OK", null);
                    builder.show();

                    ticTacToeView.resetBoardToOriginal(boardCopy);
                    board = boardCopy;
                    boardCopy = (UltimateTickTacToeBoard) board.clone();
                    ticTacToeView.invalidate();
                }
            }
        });

        fabReset.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                ticTacToeView.resetBoardToOriginal(boardCopy);
                board = boardCopy;
                boardCopy = (UltimateTickTacToeBoard) board.clone();
                ticTacToeView.invalidate();
                Log.d("GameActivity", "Resetting Board");
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        ticTacToeView.setColor(UltimateTickTacToeBoard.BOARD_STATE.RED);
        ticTacToeView.setBoard(board);
        ticTacToeView.invalidate();
    }
}
