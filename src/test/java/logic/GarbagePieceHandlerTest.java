package logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GarbagePieceHandlerTest {

    @Test
    void removeGarbageLinesConsumesAcrossMultipleGarbagePieces() {
        GarbagePieceHandler handler = new GarbagePieceHandler();
        handler.addGarbage(2);
        handler.addGarbage(4);

        int remaining = handler.removeGarbageLines(1);

        assertEquals(0, remaining);
        assertEquals(2, handler.tetrisGarbageCollector.size());
        assertEquals(1, handler.tetrisGarbageCollector.get(0).getLines());
        assertEquals(4, handler.tetrisGarbageCollector.get(1).getLines());
    }

    @Test
    void removeGarbageLinesReturnsLeftoverWhenNotEnoughGarbageExists() {
        GarbagePieceHandler handler = new GarbagePieceHandler();
        handler.addGarbage(2);

        int remaining = handler.removeGarbageLines(5);

        assertEquals(3, remaining);
        assertTrue(handler.tetrisGarbageCollector.isEmpty());
    }
}
