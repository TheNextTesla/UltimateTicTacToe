package independent_study.ultimatetictactoe.util;

import java.util.ArrayList;

import independent_study.ultimatetictactoe.game.UltimateTickTacToeBoard;

public interface ListenerGameUpdate
{
    void onGameUpdate(ArrayList<UltimateTickTacToeBoard> boards);
}
