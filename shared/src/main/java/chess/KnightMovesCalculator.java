package chess;

import java.util.ArrayList;
import java.util.Collection;

public class KnightMovesCalculator extends PieceMovesCalculator {

        public KnightMovesCalculator(ChessBoard board, ChessPosition position) {
            super(board, position);
        }

        @Override
        public Collection<ChessMove> calculateMoves() {
            Collection<ChessMove> moves = new ArrayList<>();

            ChessPosition possiblePosition;

            int[] possibleDistances = {2, 1, -1, -2};

            int row = chessPosition.getRow();
            int col = chessPosition.getColumn();

            for (int i : possibleDistances) {
                for (int j : possibleDistances) {
                    possiblePosition = new ChessPosition(row + i, col + j);
                    if (withinChessboard(possiblePosition) && (Math.abs(i) + Math.abs(j)) == 3
                        && (isEmpty(possiblePosition) || isEnemyPiece(possiblePosition))) {
                        moves.add(newMove(possiblePosition));
                    }
                }
            }

            return moves;
        }
}
