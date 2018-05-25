package independent_study.ultimatetictactoe.game;

public class UltimateTickTacToeBoard
{
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

    public UltimateTickTacToeBoard()
    {
        boardStates = new BOARD_STATE[9][9];
        for(int i = 0; i < boardStates.length; i++)
        {
            for(int j = 0; j < boardStates[0].length; j++)
            {
                boardStates[i][j] = BOARD_STATE.NONE;
            }
        }
    }

    public UltimateTickTacToeBoard(boolean[][] isRed, boolean[][] isBlue)
    {
        this();

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
}
