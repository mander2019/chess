package chess;

import java.util.ArrayList;
import java.util.Collection;

public class PawnMovesCalculator extends PieceMovesCalculator {

    public PawnMovesCalculator(ChessBoard board, ChessPosition position) {
        super(board, position);
    };

    @Override
    public Collection<ChessMove> CalculateMoves() {
        Collection<ChessMove> moves = new ArrayList<>();
        boolean hasMoved;

        // Determines whether the piece has moved from its starting position
        if (pieceColor == ChessGame.TeamColor.WHITE && chessPosition.getRow() == 2) {
            hasMoved = false;
        } else if (pieceColor == ChessGame.TeamColor.BLACK && chessPosition.getRow() == 7) {
            hasMoved = false;
        } else {
            hasMoved = true;
        }

        ChessPiece pieceInTheWay;
        ChessPosition possiblePosition;

        // Move forward
        possiblePosition = new ChessPosition(chessPosition.getRow(), chessPosition.getColumn());
        pieceInTheWay = chessBoard.getPiece(possiblePosition);

//        if (pieceInTheWay != null) {
//            moves.add(ChessMove(chessPosition, possiblePosition, null));
//        }


        return moves;
    }

}
