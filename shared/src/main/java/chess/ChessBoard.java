package chess;

import java.util.Arrays;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    private ChessPiece[][] squares = new ChessPiece[8][8];

    public ChessBoard() {
        
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        squares[position.getRow() - 1][position.getColumn() - 1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return squares[position.getRow() - 1][position.getColumn() - 1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        squares = new ChessPiece[8][8];

//        Add black pieces
        addPiece(new ChessPosition(8,1), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK));
        addPiece(new ChessPosition(8,2), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(8,3), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(8,4), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.QUEEN));
        addPiece(new ChessPosition(8,5), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KING));
        addPiece(new ChessPosition(8,6), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(8,7), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(8,8), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK));

        for (int i = 1; i < 9; i++) {
            addPiece(new ChessPosition(7,i), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN));
        }

//          Add white pieces
        addPiece(new ChessPosition(1,1), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK));
        addPiece(new ChessPosition(1,2), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(1,3), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(1,4), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN));
        addPiece(new ChessPosition(1,5), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING));
        addPiece(new ChessPosition(1,6), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(1,7), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(1,8), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK));

        for (int i = 1; i < 9; i++) {
            addPiece(new ChessPosition(2,i), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN));
        }
    }

    public String toString() {
        String string = "|";
        for (int i = 7; i > -1; i--) {
            for (int j = 0; j < 8; j++) {
                ChessPiece piece = squares[i][j];
                string = toStringHelper(string, piece);

                string = string + "|";

                if (j == 7) {
                    string = string + "\n";
                    if (i != 0) {
                        string = string + "|";
                    }
                }
            }
        }

        return string;
    }

    private String toStringHelper(String string, ChessPiece piece) {
        if (piece != null) {
            if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                if (piece.getPieceType() == ChessPiece.PieceType.ROOK) {
                    string = string + "R";
                } else if (piece.getPieceType() == ChessPiece.PieceType.KNIGHT) {
                    string = string + "N";
                } else if (piece.getPieceType() == ChessPiece.PieceType.BISHOP) {
                    string = string + "B";
                } else if (piece.getPieceType() == ChessPiece.PieceType.QUEEN) {
                    string = string + "Q";
                } else if (piece.getPieceType() == ChessPiece.PieceType.KING) {
                    string = string + "K";
                } else if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
                    string = string + "P";
                } else {
                    string = string + " ";
                }
            }
            else if (piece.getTeamColor() == ChessGame.TeamColor.BLACK) {
                if (piece.getPieceType() == ChessPiece.PieceType.ROOK) {
                    string = string + "r";
                } else if (piece.getPieceType() == ChessPiece.PieceType.KNIGHT) {
                    string = string + "n";
                } else if (piece.getPieceType() == ChessPiece.PieceType.BISHOP) {
                    string = string + "b";
                } else if (piece.getPieceType() == ChessPiece.PieceType.QUEEN) {
                    string = string + "q";
                } else if (piece.getPieceType() == ChessPiece.PieceType.KING) {
                    string = string + "k";
                } else if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
                    string = string + "p";
                } else {
                    string = string + " ";
                }
            }
        }

        else {
            string = string + " ";
        }

        return string;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(squares, that.squares);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(squares);
    }
}


