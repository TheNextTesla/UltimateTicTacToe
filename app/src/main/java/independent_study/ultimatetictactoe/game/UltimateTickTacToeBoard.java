package independent_study.ultimatetictactoe.game;

import org.json.JSONException;
import org.json.JSONObject;

public class UltimateTickTacToeBoard implements Cloneable
{
    private static final String BASE_BOOL_STRING_BLUE = "bB";
    private static final String BASE_BOOL_STRING_RED = "bR";;
    private static final String BASE_PHONE_NUMBER = "p";

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
        BR(8);

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

    public UltimateTickTacToeBoard(long phoneNumber)
    {
        this.phoneNumber = phoneNumber;
        boardStates = new BOARD_STATE[9][9];
        for(int i = 0; i < boardStates.length; i++)
        {
            for(int j = 0; j < boardStates[0].length; j++)
            {
                boardStates[i][j] = BOARD_STATE.NONE;
            }
        }
    }

    public UltimateTickTacToeBoard(boolean[][] isRed, boolean[][] isBlue, long phoneNumber)
    {
        this(phoneNumber);

        for(int i = 0 ; i < isRed.length && i < boardStates.length; i++)
        {
            for(int j = 0; j < isRed[0].length && i < boardStates[0].length; j++)
            {
                if(isRed[i][j])
                    boardStates[i][j] = BOARD_STATE.RED;
            }
        }

        for(int i = 0 ; i < isBlue.length && i < boardStates.length; i++)
        {
            for(int j = 0; j < isBlue[0].length && i < boardStates[0].length; j++)
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

            return new UltimateTickTacToeBoard(redArray, blueArray, phone);
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
            return board;
        }
        catch (CloneNotSupportedException cnse)
        {
            cnse.printStackTrace();
            return null;
        }
    }
}
