package independent_study.ultimatetictactoe.game;

import android.util.Log;
import android.util.Pair;

import org.json.JSONException;
import org.json.JSONObject;

public class UltimateTickTacToeBoard implements Cloneable
{
    private static final String BASE_BOOL_STRING_BLUE = "bB";
    private static final String BASE_BOOL_STRING_RED = "bR";
    private static final String BASE_PHONE_NUMBER = "p";
    private static final String BASE_OUTER_LOCATION = "oL";
    private static final String BASE_INNER_LOCATION = "iL";
    private static final String LOG_TAG = "Board";

    public enum BOARD_STATE {RED, BLUE, NONE}
    public enum BOARD_LOCATION
    {
        TL(0),
        TM(1),
        TR(2),
        ML(3),
        MM(4),
        MR(5),
        BL(6),
        BM(7),
        BR(8),
        NONE(9);

        private int num;

        BOARD_LOCATION(int num)
        {
            this.num = num;
        }

        public int getNum()
        {
            return num;
        }
    }

    protected BOARD_STATE[][] boardStates;
    protected long phoneNumber;

    protected Pair<BOARD_LOCATION, BOARD_LOCATION> lastChangedLocation;

    public UltimateTickTacToeBoard(long phoneNumber)
    {
        this.phoneNumber = phoneNumber;
        boardStates = new BOARD_STATE[9][9];
        for(int i = 0; i < boardStates.length; i++)
        {
            for (int j = 0; j < boardStates[0].length; j++)
            {
                boardStates[i][j] = BOARD_STATE.NONE;
            }
        }
        this.lastChangedLocation = new Pair<>(BOARD_LOCATION.NONE, BOARD_LOCATION.NONE);
    }

    public UltimateTickTacToeBoard(boolean[][] isRed, boolean[][] isBlue,
                                   Pair<BOARD_LOCATION, BOARD_LOCATION> lastChangedLocation, long phoneNumber)
    {
        this(phoneNumber);
        this.lastChangedLocation = lastChangedLocation;

        for(int i = 0; i < isRed.length && i < boardStates.length; i++)
        {
            for(int j = 0; j < isRed[0].length && j < boardStates[0].length; j++)
            {
                if(isRed[i][j])
                    boardStates[i][j] = BOARD_STATE.RED;
            }
        }

        for(int i = 0; i < isBlue.length && i < boardStates.length; i++)
        {
            for(int j = 0; j < isBlue[0].length && j < boardStates[0].length; j++)
            {
                if(isBlue[i][j])
                    boardStates[i][j] = BOARD_STATE.BLUE;
            }
        }
    }

    public BOARD_STATE[][] getBoardStates()
    {
        return boardStates;
    }

    public long getPhoneNumber()
    {
        return phoneNumber;
    }

    public Pair<BOARD_LOCATION, BOARD_LOCATION> getLastChangedLocation()
    {
        return lastChangedLocation;
    }

    public void setLastChangedLocation(Pair<BOARD_LOCATION,BOARD_LOCATION> locations)
    {
        lastChangedLocation = locations;
    }

    public boolean evaluateMoveValidity(Pair<BOARD_LOCATION,BOARD_LOCATION> locations)
    {
        //If this is the first round, always true
        if(lastChangedLocation.second == BOARD_LOCATION.NONE)
            return true;

        BOARD_STATE[] regionStates = boardStates[lastChangedLocation.second.getNum()];

        //Has Selected in the Correct Region
        if(locations.first == lastChangedLocation.second
                || evaluateBoardComponentForWinner(regionStates) != BOARD_STATE.NONE)
        {
            //Has Selected a Location that Is Not Already Taken
            if(boardStates[locations.first.getNum()][locations.second.getNum()] == BOARD_STATE.NONE)
            {
                //Is Placing In An Incomplete Section
                BOARD_STATE[] selectedStates = boardStates[lastChangedLocation.first.getNum()];
                if(evaluateBoardComponentForWinner(selectedStates) == BOARD_STATE.NONE)
                {
                    return true;
                }
                else
                    Log.d(LOG_TAG, "In a Region Already Won");
            }
            else
                Log.d(LOG_TAG, "In a Location Already Taken");
        }
        else
            Log.d(LOG_TAG, "Not in a Valid Region");
        return false;
    }

    public BOARD_STATE evaluateBoardComponentForWinner(BOARD_STATE[] states)
    {
        //Horizontal
        for(int i = 0; i < 3; i++)
        {
            if(states[i] == states[i + 1] && states[i + 1] == states[i + 2])
                return states[i];
        }

        //Vertical
        for(int j = 0; j < 3; j++)
        {
            if(states[j] == states[j + 3] && states[j + 3] == states[j + 6])
                return states[j];
        }

        //Diagonal
        if(states[0] == states[4] && states[4] == states[8])
            return states[0];
        if(states[2] == states[4] && states[4] == states[6])
            return states[0];

        return BOARD_STATE.NONE;
    }

    public BOARD_STATE evaluateIsGameWon()
    {
        BOARD_STATE[] regionalBoardStates = new BOARD_STATE[9];
        for(int i = 0; i < regionalBoardStates.length; i++)
        {
            regionalBoardStates[i] = evaluateBoardComponentForWinner(boardStates[i]);
        }
        return evaluateBoardComponentForWinner(regionalBoardStates);
    }

    public boolean[][] getRedBooleanArray()
    {
        boolean[][] redArray = new boolean[9][9];
        for(int i = 0; i < boardStates.length; i++)
        {
            for(int j = 0; j < boardStates[0].length; j++)
            {
                redArray[i][j] = (boardStates[i][j] == BOARD_STATE.RED);
            }
        }
        return redArray;
    }

    public boolean[][] getBlueBooleanArray()
    {
        boolean[][] blueArray = new boolean[9][9];
        for(int i = 0; i < boardStates.length; i++)
        {
            for(int j = 0; j < boardStates[0].length; j++)
            {
                blueArray[i][j] = (boardStates[i][j] == BOARD_STATE.BLUE);
            }
        }
        return blueArray;
    }

    public static UltimateTickTacToeBoard fromString(String string)
    {
        try
        {
            JSONObject json = new JSONObject(string);
            boolean[][] redArray = new boolean[9][9];
            boolean[][] blueArray = new boolean[9][9];

            for(int i = 0; i < 9; i++)
            {
                for(int j = 0; j < 9; j++)
                {
                    redArray[i][j] = json.getBoolean(BASE_BOOL_STRING_RED + (i * redArray.length + j));
                    blueArray[i][j] = json.getBoolean(BASE_BOOL_STRING_BLUE + (i * redArray.length + j));
                }
            }

            long phone = json.getLong(BASE_PHONE_NUMBER);
            BOARD_LOCATION outer = convertToBoardLocation(json.getInt(BASE_OUTER_LOCATION));
            BOARD_LOCATION inner = convertToBoardLocation(json.getInt(BASE_INNER_LOCATION));
            Pair<BOARD_LOCATION, BOARD_LOCATION> locations = new Pair<>(outer, inner);

            return new UltimateTickTacToeBoard(redArray, blueArray, locations, phone);
        }
        catch (JSONException jsonex)
        {
            jsonex.printStackTrace();
            return null;
        }
    }

    public String toString()
    {
        JSONObject json = new JSONObject();

        try
        {
            boolean[][] redArray = getRedBooleanArray();
            boolean[][] blueArray = getBlueBooleanArray();
            for (int i = 0; i < redArray.length; i++)
            {
                for (int j = 0; j < redArray[0].length; j++)
                {
                    json.put(BASE_BOOL_STRING_RED + (i * redArray.length + j), redArray[i][j]);
                    json.put(BASE_BOOL_STRING_BLUE + (i * redArray.length + j), blueArray[i][j]);
                }
            }
            json.put(BASE_PHONE_NUMBER, phoneNumber);
            json.put(BASE_OUTER_LOCATION, lastChangedLocation.first.getNum());
            json.put(BASE_INNER_LOCATION, lastChangedLocation.second.getNum());
        }
        catch (JSONException jsonex)
        {
            jsonex.printStackTrace();
        }

        return json.toString();
    }

    @Override
    public Object clone()
    {
        try
        {
            UltimateTickTacToeBoard board = (UltimateTickTacToeBoard) super.clone();
            board.phoneNumber = this.phoneNumber;
            board.boardStates = new BOARD_STATE[9][9];
            for(int i = 0; i < board.boardStates.length; i++)
            {
                for(int j = 0; j < board.boardStates[0].length; j++)
                {
                    board.boardStates[i][j] = this.boardStates[i][j];
                }
            }
            board.lastChangedLocation = this.lastChangedLocation;
            return board;
        }
        catch (CloneNotSupportedException cnse)
        {
            cnse.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean equals(Object board)
    {
        if(!(board instanceof UltimateTickTacToeBoard))
            return false;

        UltimateTickTacToeBoard tickTacToeBoard = (UltimateTickTacToeBoard) board;

        BOARD_STATE[][] otherStates = tickTacToeBoard.getBoardStates();
        BOARD_STATE[][] thisStates = this.getBoardStates();

        for(int i = 0; i < otherStates.length; i++)
        {
            for(int j = 0; j < otherStates[i].length; j++)
            {
                if(otherStates[i][j] != thisStates[i][j])
                {
                    return false;
                }
            }
        }

        if(this.getPhoneNumber() != tickTacToeBoard.getPhoneNumber())
            return false;

        if(this.getLastChangedLocation().first != tickTacToeBoard.getLastChangedLocation().first)
            return false;

        if(this.getLastChangedLocation().second != tickTacToeBoard.getLastChangedLocation().second)
            return false;

        return true;
    }

    public static BOARD_LOCATION convertToBoardLocation(int integer)
    {
        if(BOARD_LOCATION.TL.getNum() == integer)
            return BOARD_LOCATION.TL;
        if(BOARD_LOCATION.TM.getNum() == integer)
            return BOARD_LOCATION.TM;
        if(BOARD_LOCATION.TR.getNum() == integer)
            return BOARD_LOCATION.TR;
        if(BOARD_LOCATION.ML.getNum() == integer)
            return BOARD_LOCATION.ML;
        if(BOARD_LOCATION.MM.getNum() == integer)
            return BOARD_LOCATION.MM;
        if(BOARD_LOCATION.MR.getNum() == integer)
            return BOARD_LOCATION.MR;
        if(BOARD_LOCATION.BL.getNum() == integer)
            return BOARD_LOCATION.BL;
        if(BOARD_LOCATION.BM.getNum() == integer)
            return BOARD_LOCATION.BM;
        if(BOARD_LOCATION.BR.getNum() == integer)
            return BOARD_LOCATION.BR;
        return BOARD_LOCATION.NONE;
    }
}
