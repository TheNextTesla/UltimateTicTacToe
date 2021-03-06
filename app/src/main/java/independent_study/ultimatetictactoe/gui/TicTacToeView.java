package independent_study.ultimatetictactoe.gui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import independent_study.ultimatetictactoe.R;
import independent_study.ultimatetictactoe.game.UltimateTickTacToeBoard;

public class TicTacToeView extends View
{
    private static final PointF LINE_1_1 = new PointF((float)0.333, (float)0);
    private static final PointF LINE_1_2 = new PointF((float)0.333, (float)1);

    private static final PointF LINE_2_1 = new PointF((float)0.666, (float)0);
    private static final PointF LINE_2_2 = new PointF((float)0.666, (float)1);

    private static final PointF LINE_3_1 = new PointF((float)0, (float)0.333);
    private static final PointF LINE_3_2 = new PointF((float)1, (float)0.333);

    private static final PointF LINE_4_1 = new PointF((float)0, (float)0.666);
    private static final PointF LINE_4_2 = new PointF((float)1, (float)0.666);

    private Paint linePaint;
    private Paint redBoxPaint;
    private Paint blueBoxPaint;
    private Paint greenGlowPaint;

    private RectF[][] locationRects;
    private RectF[] magnifiedLocationRects;

    private UltimateTickTacToeBoard board;
    private UltimateTickTacToeBoard.BOARD_LOCATION magnifiedLocation;
    private UltimateTickTacToeBoard.BOARD_LOCATION subMagnifiedLocation;
    private UltimateTickTacToeBoard.BOARD_STATE color;

    private boolean isMagnified;
    private boolean hasBeenSelected;

    public TicTacToeView(Context context)
    {
        super(context);
        linePaint = new Paint();
        linePaint.setColor(Color.BLACK);
        linePaint.setStrokeWidth(5);
        redBoxPaint = new Paint();
        redBoxPaint.setColor(Color.RED);
        redBoxPaint.setStrokeWidth(20);
        blueBoxPaint = new Paint();
        blueBoxPaint.setColor(Color.BLUE);
        greenGlowPaint = new Paint();
        int aGreen = 100;
        int rGreen = 0;
        int gGreen = 100;
        int bGreen = 0;
        int greenColor = (aGreen & 0xff) << 24 | (rGreen & 0xff) << 16 | (gGreen & 0xff) << 8 | (bGreen & 0xff);
        greenGlowPaint.setColor(greenColor);
        greenGlowPaint.setStrokeWidth(20);

        color = UltimateTickTacToeBoard.BOARD_STATE.NONE;
        isMagnified = false;
        hasBeenSelected = false;
    }

    public TicTacToeView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        linePaint = new Paint();
        linePaint.setColor(Color.BLACK);
        linePaint.setStrokeWidth(5);
        redBoxPaint = new Paint();
        redBoxPaint.setColor(Color.RED);
        redBoxPaint.setStrokeWidth(20);
        blueBoxPaint = new Paint();
        blueBoxPaint.setColor(Color.BLUE);
        greenGlowPaint = new Paint();
        int aGreen = 100;
        int rGreen = 0;
        int gGreen = 100;
        int bGreen = 0;
        int greenColor = (aGreen & 0xff) << 24 | (rGreen & 0xff) << 16 | (gGreen & 0xff) << 8 | (bGreen & 0xff);
        greenGlowPaint.setColor(greenColor);
        greenGlowPaint.setStrokeWidth(20);

        color = UltimateTickTacToeBoard.BOARD_STATE.NONE;
        isMagnified = false;
        hasBeenSelected = false;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;

        if(locationRects == null)
            generateRects();

        canvas.drawLine(LINE_1_1.x * contentWidth, LINE_1_1.y * contentHeight,
                LINE_1_2.x * contentWidth, LINE_1_2.y * contentHeight, linePaint);
        canvas.drawLine(LINE_2_1.x * contentWidth, LINE_2_1.y * contentHeight,
                LINE_2_2.x * contentWidth, LINE_2_2.y * contentHeight, linePaint);
        canvas.drawLine(LINE_3_1.x * contentWidth, LINE_3_1.y * contentHeight,
                LINE_3_2.x * contentWidth, LINE_3_2.y * contentHeight, linePaint);
        canvas.drawLine(LINE_4_1.x * contentWidth, LINE_4_1.y * contentHeight,
                LINE_4_2.x * contentWidth, LINE_4_2.y * contentHeight, linePaint);

        if(board == null)
            return;

        if(isMagnified)
        {
            for(int i = 0; i < magnifiedLocationRects.length; i++)
            {
                if(board.getBoardStates()[magnifiedLocation.getNum()][i] == UltimateTickTacToeBoard.BOARD_STATE.BLUE)
                {
                    canvas.drawRect(magnifiedLocationRects[i], blueBoxPaint);
                }
                else if(board.getBoardStates()[magnifiedLocation.getNum()][i] == UltimateTickTacToeBoard.BOARD_STATE.RED)
                {
                    canvas.drawRect(magnifiedLocationRects[i], redBoxPaint);
                }
            }
        }
        else
        {
            for(int i = 0; i < locationRects.length; i++)
            {
                RectF[] rects = locationRects[i];

                for(int j = 0; j < rects.length; j++)
                {
                    if(board.getBoardStates()[i][j] == UltimateTickTacToeBoard.BOARD_STATE.BLUE)
                    {
                        canvas.drawRect(rects[j], blueBoxPaint);
                    }
                    else if(board.getBoardStates()[i][j] == UltimateTickTacToeBoard.BOARD_STATE.RED)
                    {
                        canvas.drawRect(rects[j], redBoxPaint);
                    }
                }
            }

            if(board.getLastChangedLocation().second.getNum() != 9)
                canvas.drawRect(magnifiedLocationRects[board.getLastChangedLocation().second.getNum()], greenGlowPaint);
        }
        //canvas.drawRect(magnifiedLocationRects[1], greenGlowPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent)
    {
        if(motionEvent.getAction() == MotionEvent.ACTION_DOWN)
        {
            UltimateTickTacToeBoard.BOARD_LOCATION touch_type;

            if(motionEvent.getX() < 0.333 * getWidth())
            {
                if(motionEvent.getY() < 0.333 * getHeight())
                    touch_type = UltimateTickTacToeBoard.BOARD_LOCATION.TL;
                else if(motionEvent.getY() < 0.666 * getHeight())
                    touch_type = UltimateTickTacToeBoard.BOARD_LOCATION.ML;
                else
                    touch_type = UltimateTickTacToeBoard.BOARD_LOCATION.BL;

            }
            else if(motionEvent.getX() < 0.666 * getWidth())
            {
                if(motionEvent.getY() < 0.333 * getHeight())
                    touch_type = UltimateTickTacToeBoard.BOARD_LOCATION.TM;
                else if(motionEvent.getY() < 0.666 * getHeight())
                    touch_type = UltimateTickTacToeBoard.BOARD_LOCATION.MM;
                else
                    touch_type = UltimateTickTacToeBoard.BOARD_LOCATION.BM;
            }
            else
            {
                if(motionEvent.getY() < 0.333 * getHeight())
                    touch_type = UltimateTickTacToeBoard.BOARD_LOCATION.TR;
                else if(motionEvent.getY() < 0.666 * getHeight())
                    touch_type = UltimateTickTacToeBoard.BOARD_LOCATION.MR;
                else
                    touch_type = UltimateTickTacToeBoard.BOARD_LOCATION.BR;
            }

            if(isMagnified)
            {
                selectBoardPieces(touch_type);
            }
            else
            {
                selectBoardRegion(touch_type);
            }
            invalidate();
            return true;
        }
        return false;
    }

    private void generateRects()
    {
        locationRects = new RectF[9][9];
        double blockMultiplier = 1.0 / 3.0;
        double multiplier = 1.0 / 9.0;

        for(int i = 0; i < locationRects.length; i++)
        {
            for(int j = 0; j < 3; j++)
            {
                for(int k = 0; k < 3; k++)
                {
                    float leftLocation = f((i % 3) * blockMultiplier + k * multiplier);
                    float topLocation = f((i / 3) * blockMultiplier + j * multiplier);
                    float rightLocation = f((i % 3) * blockMultiplier + (k + 1) * multiplier);
                    float bottomLocation = f((i / 3) * blockMultiplier + (j + 1) * multiplier);
                    locationRects[i][j * 3 + k] = new RectF(leftLocation * getWidth(), topLocation * getHeight(),
                            rightLocation * getWidth(), bottomLocation * getHeight());
                }
            }
        }

        magnifiedLocationRects = new RectF[9];

        for(int i = 0; i < 3; i++)
        {
            for(int j = 0; j < 3; j++)
            {
                magnifiedLocationRects[i * 3 + j] = new RectF(f(j * blockMultiplier) * getWidth(), f(i * blockMultiplier) * getHeight(),
                        f((j + 1) * blockMultiplier) * getWidth(), f((i + 1) * blockMultiplier) * getHeight());
            }
        }
    }

    public void resetBoardToOriginal(UltimateTickTacToeBoard originalBoard)
    {
        setBoard(originalBoard);
        isMagnified = false;
        hasBeenSelected = false;
    }

    private void selectBoardRegion(UltimateTickTacToeBoard.BOARD_LOCATION location)
    {
        if(!hasBeenSelected)
        {
            magnifiedLocation = location;
            isMagnified = true;
        }
    }

    private void selectBoardPieces(UltimateTickTacToeBoard.BOARD_LOCATION location)
    {
        if(!hasBeenSelected)
        {
            Pair<UltimateTickTacToeBoard.BOARD_LOCATION,UltimateTickTacToeBoard.BOARD_LOCATION> pair =
                    new Pair<>(magnifiedLocation, location);
            if(board.evaluateMoveValidity(pair))
            {
                hasBeenSelected = true;
                isMagnified = false;
                subMagnifiedLocation = location;
                board.getBoardStates()[magnifiedLocation.getNum()][location.getNum()] = color;

                if(board.evaluateIsGameWon() == UltimateTickTacToeBoard.BOARD_STATE.RED)
                    Toast.makeText(this.getContext(), "You Have Won!", Toast.LENGTH_LONG).show();
                else if(board.evaluateIsGameWon() == UltimateTickTacToeBoard.BOARD_STATE.BLUE)
                    Toast.makeText(this.getContext(), "You Have Lost...", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Toast.makeText(this.getContext(), "Improper Selection", Toast.LENGTH_SHORT).show();
    }

    public boolean isValidPieceChoosen()
    {
        return !isMagnified && hasBeenSelected;
    }

    public UltimateTickTacToeBoard.BOARD_LOCATION getMagnifiedLocation()
    {
        return magnifiedLocation;
    }

    public UltimateTickTacToeBoard.BOARD_LOCATION getSubMagnifiedLocation()
    {
        return subMagnifiedLocation;
    }

    public void setColor(UltimateTickTacToeBoard.BOARD_STATE color)
    {
        this.color = color;
    }

    public void setBoard(UltimateTickTacToeBoard board)
    {
        this.board = board;
    }

    private float f(double d)
    {
        return (float) d;
    }
}
