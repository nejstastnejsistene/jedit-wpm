/* pajohnson@email.wm.edu
 * This file is to encapsulate all the wpm counting functionality.
 */
package org.gjt.sp.jedit;

import java.util.Date;
import org.gjt.sp.jedit.gui.StatusBar;

public class WpmCounter implements Runnable
{

    public WpmCounter(StatusBar status)
    {
        this.status = status;
        lastActivity = 0;
        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    public void setStopDelay(int stopDelay)
    {
        this.stopDelay = stopDelay;
    }

    public void setRefreshRate(int refreshRate)
    {
        this.refreshRate = refreshRate;
    }

    // Tell the counter to count this char.
    public void countChar(char ch)
    {
        charactersCounted += 1;

        boolean isSpace = Character.isWhitespace(ch);
        // A space usually indicates a new word was finished being typed,
        // but not if several spaces come in a row.
        if (isSpace && !lastCharWasSpace)
            wordsCounted += 1;

        lastCharWasSpace = isSpace;
        lastActivity = new Date().getTime();
    }

    public void run()
    {
        wordsCounted = 0;
        charactersCounted = 0;

        while (true)
        {
            // Clear the status because we haven't been typing in a while.
            if (new Date().getTime() - lastActivity > stopDelay)
                status.updateWpmStatus();
            else
            {
                int wpm = wordsCounted * 60000 / refreshRate;
                int cpm = charactersCounted * 60000 / refreshRate;

                // Update the status bar and clear the count.
                status.updateWpmStatus(wpm, cpm);
                wordsCounted = 0;
                charactersCounted = 0;
            }

            try
            {
                Thread.sleep(refreshRate);
            } catch (InterruptedException e) {}
        }        
    }

    // Private instance fields.
    private StatusBar status;
    private long lastActivity;
    private volatile int wordsCounted;
    private volatile int charactersCounted;
    private int stopDelay;
    private int refreshRate;
    private boolean lastCharWasSpace;

}
