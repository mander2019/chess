package chess;

import java.util.ArrayList;
import java.util.Collection;

public class KingMovesCalculator extends PieceMovesCalculator {

    public KingMovesCalculator(ChessBoard board, ChessPosition position) {
        super(board, position);
    }

    @Override
    public Collection<ChessMove> calculateMoves() {
        Collection<ChessMove> moves = new ArrayList<>();

        ChessPosition possiblePosition;

        int[] possibleDistance = {1, 0, -1};

        int row = chessPosition.getRow();
        int col = chessPosition.getColumn();

        for (int i : possibleDistance) {
            for (int j : possibleDistance) {
                possiblePosition = new ChessPosition(row + i, col + j);
                if (withinChessboard(possiblePosition) && (isEmpty(possiblePosition) || isEnemyPiece(possiblePosition))) {
                    moves.add(newMove(possiblePosition));
                }
            }
        }

        return moves;
    }

}
