package chess;

import java.util.ArrayList;
import java.util.Collection;

public class PawnMovesCalculator extends PieceMovesCalculator {

    public PawnMovesCalculator(ChessBoard board, ChessPosition position) {
        super(board, position);
    }

    @Override
    public Collection<ChessMove> CalculateMoves() {
        Collection<ChessMove> moves = new ArrayList<>();

        int moveForward = 0; // Variables to specify direction of pawn
        int moveForward2 = 0;

        ChessPiece pieceInTheWay;
        ChessPosition possiblePosition;

        // Assign directionality of pawn
        if (pieceColor == ChessGame.TeamColor.WHITE) {
            moveForward = 1;
            moveForward2 = 2;
        } else {
            moveForward = -1;
            moveForward2 = -2;
        }

        // Mark whether the pawn is in its start position
        if (pieceColor == ChessGame.TeamColor.WHITE && chessPosition.getRow() == 2
        || pieceColor == ChessGame.TeamColor.BLACK && chessPosition.getRow() == 7) {
            chessPiece.HasMoved = false;
        } else {
            chessPiece.HasMoved = true;
        }

        // Move forward 1 space
        possiblePosition = new ChessPosition(chessPosition.getRow() + moveForward, chessPosition.getColumn());
        pieceInTheWay = chessBoard.getPiece(possiblePosition);

        if (pieceInTheWay == null) { // Add move to list of possibilities
                     // Promotion
            if (possiblePosition.getRow() == 1 && pieceColor == ChessGame.TeamColor.BLACK ||
                possiblePosition.getRow() == 8 && pieceColor == ChessGame.TeamColor.WHITE) {
                moves.add(new ChessMove(chessPosition, possiblePosition, ChessPiece.PieceType.QUEEN));
                moves.add(new ChessMove(chessPosition, possiblePosition, ChessPiece.PieceType.ROOK));
                moves.add(new ChessMove(chessPosition, possiblePosition, ChessPiece.PieceType.BISHOP));
                moves.add(new ChessMove(chessPosition, possiblePosition, ChessPiece.PieceType.KNIGHT));
            } else { // No promotion
                moves.add(new ChessMove(chessPosition, possiblePosition, null));
            }
        }

        if (!chessPiece.HasMoved) { // Move forward 2 spaces
            possiblePosition = new ChessPosition(chessPosition.getRow() + moveForward2, chessPosition.getColumn());
            ChessPiece adjacentPiece = chessBoard.getPiece(new ChessPosition(chessPosition.getRow() + moveForward, chessPosition.getColumn()));
            pieceInTheWay = chessBoard.getPiece(possiblePosition);
            if (pieceInTheWay == null && adjacentPiece == null) { // Add move to list of possibilities
                moves.add(new ChessMove(chessPosition, possiblePosition, null));
            }
        }

        // Capture pieces
        if (chessPosition.getColumn() != 1) { // Left side attack
            possiblePosition = new ChessPosition(chessPosition.getRow() + moveForward, chessPosition.getColumn() - 1);
            pieceInTheWay = chessBoard.getPiece(possiblePosition);
            if (pieceInTheWay != null && pieceInTheWay.getTeamColor() != pieceColor) {
                if (possiblePosition.getRow() == 1 && pieceColor == ChessGame.TeamColor.BLACK || // Promotion
                    possiblePosition.getRow() == 8 && pieceColor == ChessGame.TeamColor.WHITE) {
                    moves.add(new ChessMove(chessPosition, possiblePosition, ChessPiece.PieceType.QUEEN));
                    moves.add(new ChessMove(chessPosition, possiblePosition, ChessPiece.PieceType.ROOK));
                    moves.add(new ChessMove(chessPosition, possiblePosition, ChessPiece.PieceType.BISHOP));
                    moves.add(new ChessMove(chessPosition, possiblePosition, ChessPiece.PieceType.KNIGHT));
                } else { // No promotion
                    moves.add(new ChessMove(chessPosition, possiblePosition, null));
                }
            }
        }
        if (chessPosition.getColumn() != 8) { // Right side attack
            possiblePosition = new ChessPosition(chessPosition.getRow() + moveForward, chessPosition.getColumn() + 1);
            pieceInTheWay = chessBoard.getPiece(possiblePosition);
            if (pieceInTheWay != null && pieceInTheWay.getTeamColor() != pieceColor) {
                if (possiblePosition.getRow() == 1 && pieceColor == ChessGame.TeamColor.BLACK || // Promotion
                    possiblePosition.getRow() == 8 && pieceColor == ChessGame.TeamColor.WHITE) {
                    moves.add(new ChessMove(chessPosition, possiblePosition, ChessPiece.PieceType.QUEEN));
                    moves.add(new ChessMove(chessPosition, possiblePosition, ChessPiece.PieceType.ROOK));
                    moves.add(new ChessMove(chessPosition, possiblePosition, ChessPiece.PieceType.BISHOP));
                    moves.add(new ChessMove(chessPosition, possiblePosition, ChessPiece.PieceType.KNIGHT));
                } else { // No promotion
                    moves.add(new ChessMove(chessPosition, possiblePosition, null));
                }
            }
        }


        return moves;
    }

}
