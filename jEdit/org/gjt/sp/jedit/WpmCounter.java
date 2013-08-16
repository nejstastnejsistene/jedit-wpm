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

        long now = new Date().getTime();

        // If going from inactive to active, save the start time
        // and reset the counts.
        if (!active)
        {
            startTime = now;
            wordsCounted = 0;
            charactersCounted = 0;
        }

        active = true;
        lastActivity = now;
    }

    public void run()
    {
        while (true)
        {
            if (active) {
                long now = new Date().getTime();

                // Clear the status because we haven't been typing in a while.
                if (now - lastActivity > stopDelay)
                {
                    status.updateWpmStatus();
                    active = false;
                }
                else
                {
                    // Calculate the wpm and cpm.
                    double factor = (now - startTime) / 60000.0;
                    int wpm = (int)(wordsCounted / factor);
                    int cpm = (int)(charactersCounted / factor);

                    // Update the status bar and clear the count.
                    status.updateWpmStatus(wpm, cpm);
                }
            }

            try
            {
                Thread.sleep(refreshRate);
            } catch (InterruptedException e) {}
        }        
    }

    // Private instance fields.
    private StatusBar status;
    private volatile long startTime;
    private volatile long lastActivity;
    private volatile boolean active;
    private volatile int wordsCounted;
    private volatile int charactersCounted;
    private boolean lastCharWasSpace;
    private int stopDelay;
    private int refreshRate;

}
