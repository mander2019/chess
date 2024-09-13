package chess;

import java.util.ArrayList;
import java.util.Collection;

public class RookMovesCalculator extends PieceMovesCalculator {

    public RookMovesCalculator(ChessBoard board, ChessPosition position) {
        super(board, position);
    }

    @Override
    public Collection<ChessMove> CalculateMoves() {
        Collection<ChessMove> moves = new ArrayList<>();

        ChessPiece pieceInTheWay;
        ChessPosition possiblePosition;

        int row = chessPosition.getRow();
        int col = chessPosition.getColumn();

        int tempRow = row;
        int tempCol = col;

        for (int i = 1; i < 9; i++) { // Upward movement
            tempRow = row + i;
            tempCol = col;
            if (tempRow > 8 || tempCol > 8 || tempRow < 1 || tempCol < 1) {
                break;
            }
            possiblePosition = new ChessPosition(tempRow, tempCol);
            pieceInTheWay = chessBoard.getPiece(possiblePosition);
            if (pieceInTheWay != null && pieceInTheWay.getTeamColor() == pieceColor) {
                break;
            }
            if (pieceInTheWay == null || pieceInTheWay.getTeamColor() != pieceColor) {
                moves.add(new ChessMove(chessPosition, possiblePosition, null));
                if (pieceInTheWay != null) {
                    break;
                }
            }
        }

        for (int i = 1; i < 9; i++) { // Downward movement
            tempRow = row - i;
            tempCol = col;
            if (tempRow > 8 || tempCol > 8 || tempRow < 1 || tempCol < 1) {
                break;
            }
            possiblePosition = new ChessPosition(tempRow, tempCol);
            pieceInTheWay = chessBoard.getPiece(possiblePosition);
            if (pieceInTheWay != null && pieceInTheWay.getTeamColor() == pieceColor) {
                break;
            }
            if (pieceInTheWay == null || pieceInTheWay.getTeamColor() != pieceColor) {
                moves.add(new ChessMove(chessPosition, possiblePosition, null));
                if (pieceInTheWay != null) {
                    break;
                }
            }
        }

        for (int i = 1; i < 9; i++) { // Rightward movement
            tempRow = row;
            tempCol = col + i;
            if (tempRow > 8 || tempCol > 8 || tempRow < 1 || tempCol < 1) {
                break;
            }
            possiblePosition = new ChessPosition(tempRow, tempCol);
            pieceInTheWay = chessBoard.getPiece(possiblePosition);
            if (pieceInTheWay != null && pieceInTheWay.getTeamColor() == pieceColor) {
                break;
            }
            if (pieceInTheWay == null || pieceInTheWay.getTeamColor() != pieceColor) {
                moves.add(new ChessMove(chessPosition, possiblePosition, null));
                if (pieceInTheWay != null) {
                    break;
                }
            }
        }

        for (int i = 1; i < 9; i++) { // Leftward movement
            tempRow = row;
            tempCol = col - i;
            if (tempRow > 8 || tempCol > 8 || tempRow < 1 || tempCol < 1) {
                break;
            }
            possiblePosition = new ChessPosition(tempRow, tempCol);
            pieceInTheWay = chessBoard.getPiece(possiblePosition);
            if (pieceInTheWay != null && pieceInTheWay.getTeamColor() == pieceColor) {
                break;
            }
            if (pieceInTheWay == null || pieceInTheWay.getTeamColor() != pieceColor) {
                moves.add(new ChessMove(chessPosition, possiblePosition, null));
                if (pieceInTheWay != null) {
                    break;
                }
            }
        }

        return moves;
    }
}
