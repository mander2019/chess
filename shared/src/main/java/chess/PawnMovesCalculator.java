package chess;

import java.util.ArrayList;
import java.util.Collection;

public class PawnMovesCalculator extends PieceMovesCalculator {

    private int moveForward = 0; // Variables to specify direction of pawn
    private int moveForward2 = 0;

    public PawnMovesCalculator(ChessBoard board, ChessPosition position) {
        super(board, position);

        // Assign directionality of pawn
        if (pieceColor == ChessGame.TeamColor.WHITE) {
            this.moveForward = 1;
            this.moveForward2 = 2;
        } else {
            this.moveForward = -1;
            this.moveForward2 = -2;
        }
    }

    @Override
    public Collection<ChessMove> calculateMoves() {
        Collection<ChessMove> moves = new ArrayList<>();

        ChessPiece pieceInTheWay;
        ChessPosition possiblePosition;

        chessPiece.hasPawnMoved(chessPosition);

        // Move forward 1 space
        possiblePosition = new ChessPosition(chessPosition.getRow() + moveForward, chessPosition.getColumn());

        if (isEmpty(possiblePosition)) { // Add move to list of possibilities
                     // Promotion
            if (possiblePosition.getRow() == 8 && pieceColor == ChessGame.TeamColor.WHITE ||
                possiblePosition.getRow() == 1 && pieceColor == ChessGame.TeamColor.BLACK) {
                addPromotionMoves(moves, chessPosition, possiblePosition);
            } else { // No promotion
                moves.add(newMove(possiblePosition));
            }
        }

        if (!chessPiece.hasMoved) { // Move forward 2 spaces
            possiblePosition = new ChessPosition(chessPosition.getRow() + moveForward2, chessPosition.getColumn());
            ChessPiece adjacentPiece = chessBoard.getPiece(new ChessPosition(chessPosition.getRow() + moveForward, chessPosition.getColumn()));
            pieceInTheWay = chessBoard.getPiece(possiblePosition);
            if (pieceInTheWay == null && adjacentPiece == null) { // Add move to list of possibilities
                moves.add(newMove(possiblePosition));
            }
        }

        // Capture pieces
        pawnAttackHelper(moves, 1);
        pawnAttackHelper(moves, -1);

        return moves;
    }

    private void addPromotionMoves(Collection<ChessMove> moves, ChessPosition start, ChessPosition end) {
        moves.add(new ChessMove(start, end, ChessPiece.PieceType.QUEEN));
        moves.add(new ChessMove(start, end, ChessPiece.PieceType.ROOK));
        moves.add(new ChessMove(start, end, ChessPiece.PieceType.BISHOP));
        moves.add(new ChessMove(start, end, ChessPiece.PieceType.KNIGHT));
    }

    private void pawnAttackHelper(Collection<ChessMove> moves, int attackSide) {
        ChessPosition possiblePosition;
        int edgeColumn;

        if (attackSide == 1) { // Right side attack
            edgeColumn = 1;
        } else if (attackSide == -1) { // Left side attack
            edgeColumn = 8;
        } else { // Error
            edgeColumn = -1;
        }

        if (chessPosition.getColumn() != edgeColumn) {
            possiblePosition = new ChessPosition(chessPosition.getRow() + moveForward, chessPosition.getColumn() + attackSide);
            if (!withinChessboard(possiblePosition)) {
                return;
            }
            if (!isEmpty(possiblePosition) && isEnemyPiece(possiblePosition)) {
                if (possiblePosition.getRow() == 1 && pieceColor == ChessGame.TeamColor.BLACK || // Promotion
                    possiblePosition.getRow() == 8 && pieceColor == ChessGame.TeamColor.WHITE) {
                    addPromotionMoves(moves, chessPosition, possiblePosition);
                } else { // No promotion
                    moves.add(newMove(possiblePosition));
                }
            }
        }
    }

}
