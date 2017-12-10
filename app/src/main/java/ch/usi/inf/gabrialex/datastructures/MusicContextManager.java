package ch.usi.inf.gabrialex.datastructures;

/**
 * Created by alex on 27.11.17.
 */

import android.location.Location;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Date;

import ch.usi.inf.gabrialex.service.Audio;
import ch.usi.inf.gabrialex.service.InsertRankableEntryTask;

/**
 * Class where we store location, track playtime, and all other useful things we need
 * for ranking.
 */
public class MusicContextManager {

    private static MusicContextManager instance;

    /**
     * Get music context instance.
     * @return
     */
    public static MusicContextManager getInstance() {
        if (instance == null) {
            instance = new MusicContextManager();
        }
        return instance;
    }

    private MusicContext musicContext = null;


    public void trackChanged(Audio audio) {
        if (this.musicContext != null && this.musicContext.hasAudio()) {
            if (!this.musicContext.hasData()) {
                this.musicContext.initializeWithEnvironment();
            }
            InsertRankableEntryTask task = new InsertRankableEntryTask(this.musicContext);
            Thread t = new Thread(task);
            t.run();
        }

        // reset context
        this.musicContext = new MusicContext();
        this.musicContext.setActiveMedia(audio);
    }


    public void timestamp() {
        DateTime dateTime = new DateTime(new Date());
        EnvironmentContext context = EnvironmentContext.getInstance();
        this.musicContext.addDate(dateTime);
        this.musicContext.addMood(context.getMood());
        this.musicContext.addLocation(context.getLastLocation());
    }
}
