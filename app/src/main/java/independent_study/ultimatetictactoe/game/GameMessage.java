package independent_study.ultimatetictactoe.game;

import android.util.Base64;
import android.util.Log;

import java.util.BitSet;

public class GameMessage
{
    private static final String MESSAGE_HEADER = "New TicTacToe Message: ";

    protected String message;
    protected boolean[][] isRed;
    protected boolean[][] isBlue;
    protected long phoneNumber;

    protected UltimateTickTacToeBoard board;

    public GameMessage(String message, long phoneNumber)
    {
        this.message = message;
        this.phoneNumber = phoneNumber;
        this.board = parseMessage();
        this.isRed = board.getRedBooleanArray();
        this.isBlue = board.getBlueBooleanArray();
    }

    public GameMessage(UltimateTickTacToeBoard board)
    {
        this.board = board;
        this.phoneNumber = board.getPhoneNumber();
        this.isBlue = board.getBlueBooleanArray();
        this.isRed = board.getRedBooleanArray();
        this.message = generateMessage();
    }

    private String generateMessage()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(MESSAGE_HEADER);
        BitSet bitSet = new BitSet(18 * 9 + 1);
        bitSet.set(18 * 9, true);
        for(int i = 0; i < bitSet.length(); i++)
        {
            if(i < 81)
                bitSet.set(i, isRed[i / 9][i % 9]);
            else if(i < 2 * 81)
                bitSet.set(i, isBlue[(i-81) / 9][i % 9]);
        }
        String base64 = Base64.encodeToString(bitSet.toByteArray(), Base64.NO_WRAP);
        builder.append(base64);
        return builder.toString();
    }

    private UltimateTickTacToeBoard parseMessage()
    {
        if(!message.contains(MESSAGE_HEADER))
            throw new IllegalArgumentException();

        String cutMessage = message.replace(MESSAGE_HEADER, "");
        byte[] rawBytes = Base64.decode(cutMessage, Base64.NO_WRAP);
        boolean[][] red = new boolean[9][9];
        boolean[][] blue = new boolean[9][9];

        /*
        for(int i = 0; i < 81 * 2; i++)
        {
            if(i < 81)
            {
                red[i / 9][i % 9] = getBooleanInByte(rawBytes[i / 8], i % 8);
            }
            else if(i < 81 * 2)
            {
                int tempI = i - 81;
                blue[tempI / 9][tempI % 9] = getBooleanInByte(rawBytes[i / 8], i % 8);
            }
        }
        */

        BitSet bools = BitSet.valueOf(rawBytes);
        for(int i = 0; i < bools.size(); i++)
        {
            if(i < 81)
            {
                red[i / 9][i % 9] = bools.get(i);
            }
            else if(i < 81 * 2)
            {
                int tempI = i - 81;
                blue[tempI / 9][tempI % 9] = bools.get(i);
            }
        }
        return new UltimateTickTacToeBoard(blue, red, phoneNumber); //TODO: Umm... is this cheating
    }

    /*
    private boolean getBooleanInByte(byte source, int position)
    {
        return ((source >> position) & 1) == 1;
    }

    private byte makeByteOfBoolean(boolean... booleans)
    {
        byte tempByte = 0;

    }
    */

    public UltimateTickTacToeBoard getBoard()
    {
        return board;
    }

    public String getMessage()
    {
        return message;
    }

    public long getPhoneNumber()
    {
        return phoneNumber;
    }

    public static boolean isGameMessage(String message)
    {
        try
        {
            new GameMessage(message, 0);
            return true;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return false;
        }
    }
}
