package ch.usi.inf.gabrialex.musicplayer2;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class LocalTimeIntervalTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testIntervalWithMidnight() throws  Exception {
        DateTime dt1 = DateTime.parse("2004-12-14T21:39:45.618-08:00");
        DateTime dt2 = DateTime.parse("2004-12-14T01:49:45.618-08:00");
        LocalTime lt1 = new LocalTime(dt1);
        LocalTime lt2 = new LocalTime(dt2);

        DateTime dtn = DateTime.parse("2004-12-14T23:49:45.618-08:00");
        assertTrue( insideInterval(lt1, lt2, new LocalTime(dtn)) );

        dtn = DateTime.parse("2004-12-14T23:59:59.999-08:00");
        assertTrue( insideInterval(lt1, lt2, new LocalTime(dtn)) );

        dtn = DateTime.parse("2004-12-14T00:00:00.000-08:00");
        assertTrue( insideInterval(lt1, lt2, new LocalTime(dtn)) );

        dtn = DateTime.parse("2004-12-14T01:30:00.000-08:00");
        assertTrue( insideInterval(lt1, lt2, new LocalTime(dtn)) );

        dtn = DateTime.parse("2004-12-14T01:49:45.618-08:00");
        assertTrue( insideInterval(lt1, lt2, new LocalTime(dtn)) );

        dtn = DateTime.parse("2004-12-14T01:49:45.619-08:00");
        assertFalse( insideInterval(lt1, lt2, new LocalTime(dtn)) );

        dtn = DateTime.parse("2004-12-14T01:49:45.618-12:00");
        assertTrue( insideInterval(lt1, lt2, new LocalTime(dtn)) );
    }

    @Test
    public void testIntervalWithoutMidnight() throws Exception {
        DateTime dt1 = DateTime.parse("2004-12-14T21:39:45.618-08:00");
        DateTime dt2 = DateTime.parse("2004-12-14T22:49:45.618-08:00");
        LocalTime lt1 = new LocalTime(dt1);
        LocalTime lt2 = new LocalTime(dt2);

        DateTime dtn = DateTime.parse("2004-12-14T21:50:45.618-08:00");
        assertTrue( insideInterval(lt1, lt2, new LocalTime(dtn)) );

        dtn = DateTime.parse("2004-12-14T21:39:45.618-08:00");
        assertTrue( insideInterval(lt1, lt2, new LocalTime(dtn)) );

        dtn = DateTime.parse("2005-12-14T22:49:45.618-08:00");
        assertTrue( insideInterval(lt1, lt2, new LocalTime(dtn)) );

        dtn = DateTime.parse("2005-12-14T00:00:45.618-08:00");
        assertFalse( insideInterval(lt1, lt2, new LocalTime(dtn)) );

        dtn = DateTime.parse("2005-12-14T21:38:45.618-08:00");
        assertFalse( insideInterval(lt1, lt2, new LocalTime(dtn)) );
    }


    /**
     * (1) if interval contains midnight, check sub-intervals [a, midnight] and [midnight, b]
     * (2) otherwise, check [a, b]
     * @param a start
     * @param b end
     * @param n current time
     * @return
     */
    public boolean insideInterval(LocalTime a, LocalTime b, LocalTime n) {
        if (a.isAfter(b)) {
            LocalTime midnight1 = new LocalTime(23, 59, 59, 999);
            LocalTime midnight2 = new LocalTime(0, 0, 0, 0);

            boolean nea = n.isEqual(a);
            boolean neb = n.isEqual(b);
            boolean nm1 = n.isEqual(midnight1);
            boolean nm2 = n.isEqual(midnight2);

            boolean i1 = (n.isAfter(a) || nea) && (n.isBefore(midnight1) || nm1);
            boolean i2 = (n.isAfter(midnight2) || nm2) && (n.isBefore(b) || neb);
            return i1 || i2;
        }

        return (n.isAfter(a) || n.isEqual(a)) && (n.isBefore(b) || n.isEqual(b));
    }
}