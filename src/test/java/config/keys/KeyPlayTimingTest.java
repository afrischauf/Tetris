package config.keys;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KeyPlayTimingTest {

    @Test
    void convertsMillisecondsToInputTicksUsingCeiling() throws Exception {
        Method method = KeyPlay.class.getDeclaredMethod("millisToTicks", long.class);
        method.setAccessible(true);

        assertEquals(0, method.invoke(null, 0L));
        assertEquals(1, method.invoke(null, 4L));
        assertEquals(2, method.invoke(null, 5L));
        assertEquals(34, method.invoke(null, 133L));
    }

    @Test
    void convertsSoftDropRateToTickInterval() throws Exception {
        Method method = KeyPlay.class.getDeclaredMethod("dropsPerSecondToTicks", long.class);
        method.setAccessible(true);

        assertEquals(1, method.invoke(null, 0L));
        assertEquals(13, method.invoke(null, 20L));
        assertEquals(5, method.invoke(null, 60L));
    }
}
