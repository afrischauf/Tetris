package logic;

import logic.pieces.Tetromino;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class RandomGeneratorTest {

    @Test
    void firstBagContainsAllSevenUniquePieces() {
        TetrisField field = mock(TetrisField.class);
        RandomGenerator generator = new RandomGenerator(field);

        Set<Integer> pieceIds = new HashSet<Integer>();
        for (int i = 0; i < 7; i++) {
            Tetromino piece = generator.getNext();
            pieceIds.add(piece.getPieceId());
        }

        assertEquals(7, pieceIds.size());
    }
}
