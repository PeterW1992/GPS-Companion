package dissertation.GPSCompanionApp;

import org.junit.Test;

import dissertation.GPSCompanionApp.helpers.Utils;

import static org.junit.Assert.assertEquals;

/**
 * Created by Peter on 18/04/2017.
 */
public class UtilsTest {

        @Test
        public void durationFormat() throws Exception {
            assertEquals("0 mins",Utils.getDurationFormat(59000));
            assertEquals("1 mins",Utils.getDurationFormat(60000));
            assertEquals("1 hrs",Utils.getDurationFormat(3600000));

            assertEquals("0 mins",Utils.getDurationFormat(-59000));
            assertEquals("-1 mins",Utils.getDurationFormat(-60000));
            assertEquals("-1 hrs",Utils.getDurationFormat(-3600000));

            assertEquals("0 mins",Utils.getDurationFormat(0));
            assertEquals("0 mins",Utils.getDurationFormat(-0));
        }
}

