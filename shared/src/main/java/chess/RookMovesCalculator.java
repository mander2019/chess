package chess;

import java.util.ArrayList;
import java.util.Collection;

public class RookMovesCalculator extends PieceMovesCalculator {

    public RookMovesCalculator(ChessBoard board, ChessPosition position) {
        super(board, position);
    }

    @Override
    public Collection<ChessMove> calculateMoves() {
        Collection<ChessMove> moves = new ArrayList<>();

        // Positive and negative numbers indicate the direction of the rook's movement
        rookMovesHelper(moves, 1, 0);
        rookMovesHelper(moves, -1, 0);
        rookMovesHelper(moves, 0, 1);
        rookMovesHelper(moves, 0, -1);

        return moves;
    }

    private void rookMovesHelper(Collection<ChessMove> moves, int rowDirection, int colDirection) {
        ChessPosition possiblePosition;

        int row = chessPosition.getRow();
        int col = chessPosition.getColumn();

        for (int i = 1; i < 9; i++) { // Upward movement
            int tempRow = row + i * rowDirection;
            int tempCol = col + i * colDirection;

            possiblePosition = new ChessPosition(tempRow, tempCol);
            if (!withinChessboard(possiblePosition)) { // Is position within the board?
                break;
            }
            if (!isEmpty(possiblePosition) && isFriendlyPiece(possiblePosition)) { // Is it an ally?
                break;
            }
            if (isEmpty(possiblePosition) || isEnemyPiece(possiblePosition)) { // Is it empty or an enemy?
                moves.add(newMove(possiblePosition));
                if (!isEmpty(possiblePosition)) { // Is there a piece there?
                    break;
                }
            }
        }
    }
}
