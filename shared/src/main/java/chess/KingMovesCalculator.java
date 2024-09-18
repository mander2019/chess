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

        int[] rowDistance = {1, 0, -1};
        int[] colDistance = {1, 0, -1};

        int row = chessPosition.getRow();
        int col = chessPosition.getColumn();

        for (int i : rowDistance) {
            for (int j : colDistance) {
                possiblePosition = new ChessPosition(row + i, col + j);
                if (WithinChessboard(possiblePosition)
                    && (IsEmpty(possiblePosition) || IsEnemyPiece(possiblePosition))) {
                    moves.add(NewMove(possiblePosition));
                }
            }
        }

        return moves;
    }
}
