package independent_study.ultimatetictactoe.gui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import independent_study.ultimatetictactoe.R;
import independent_study.ultimatetictactoe.game.UltimateTickTacToeBoard;

public class GameActivity extends AppCompatActivity
{
    public static final String BOARD_TAG = "Board";

    private TicTacToeView ticTacToeView;
    private UltimateTickTacToeBoard board;
    private long number;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Bundle gameSetup = getIntent().getExtras();
        String boardSerial = gameSetup.getString(BOARD_TAG);
        board = UltimateTickTacToeBoard.fromString(boardSerial);
        number = board.getPhoneNumber();

        ticTacToeView = findViewById(R.id.ticTacToeView);
        //TODO: Replace Next Line When Color is Cleared Up
        ticTacToeView.setColor(UltimateTickTacToeBoard.BOARD_STATE.RED);
        ticTacToeView.setBoard(board);
        ticTacToeView.invalidate();
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
