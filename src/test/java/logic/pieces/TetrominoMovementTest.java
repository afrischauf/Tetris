package logic.pieces;

import logic.TetrisField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TetrominoMovementTest {

    @Test
    void pieceMovesHorizontallyWhenSpaceIsFree() {
        TetrisField field = openField();
        TPiece piece = new TPiece(field);

        piece.movePieceLeft();
        assertEquals(2, piece.getX());

        piece.movePieceRight();
        assertEquals(3, piece.getX());
    }

    @Test
    void instantSoftDropMovesPieceToTheFloor() {
        TetrisField field = openField();
        TPiece piece = new TPiece(field);

        int droppedRows = piece.instantSDF();

        assertTrue(droppedRows > 0);
        assertEquals(48, piece.getY());
    }

    @Test
    void clockwiseRotationChangesRotationInOpenSpace() {
        TetrisField field = openField();
        TPiece piece = new TPiece(field);

        piece.rotateClockwise();

        assertEquals(1, piece.getCurrentRotation());
    }

    private TetrisField openField() {
        TetrisField field = mock(TetrisField.class);
        when(field.isFreePixel(anyInt(), anyInt())).thenAnswer(invocation -> {
            int x = invocation.getArgument(0);
            int y = invocation.getArgument(1);
            return x >= 0 && x < 10 && y >= 0 && y < 50;
        });
        return field;
    }
}
