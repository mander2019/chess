package chess;

import java.util.Arrays;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard implements Cloneable {

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

    public void movePiece(ChessMove move) {
        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();
        ChessPiece.PieceType promotionType = move.getPromotionPiece();

        if (promotionType != null) {
            squares[end.getRow() - 1][end.getColumn() - 1] = new ChessPiece(getPiece(start).getTeamColor(), promotionType);
        } else {
            squares[end.getRow() - 1][end.getColumn() - 1] = getPiece(start);
        }
        squares[start.getRow() - 1][start.getColumn() - 1] = null;
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        squares = new ChessPiece[8][8];

        // Variables to simplify chessboard setup
        ChessGame.TeamColor black = ChessGame.TeamColor.BLACK;
        ChessGame.TeamColor white = ChessGame.TeamColor.WHITE;

        ChessPiece.PieceType pawn = ChessPiece.PieceType.PAWN;
        ChessPiece.PieceType rook = ChessPiece.PieceType.ROOK;
        ChessPiece.PieceType knight = ChessPiece.PieceType.KNIGHT;
        ChessPiece.PieceType bishop = ChessPiece.PieceType.BISHOP;
        ChessPiece.PieceType queen = ChessPiece.PieceType.QUEEN;
        ChessPiece.PieceType king = ChessPiece.PieceType.KING;


        // Add black pieces
        addPiece(new ChessPosition(8,1), new ChessPiece(black, rook));
        addPiece(new ChessPosition(8,2), new ChessPiece(black, knight));
        addPiece(new ChessPosition(8,3), new ChessPiece(black, bishop));
        addPiece(new ChessPosition(8,4), new ChessPiece(black, queen));
        addPiece(new ChessPosition(8,5), new ChessPiece(black, king));
        addPiece(new ChessPosition(8,6), new ChessPiece(black, bishop));
        addPiece(new ChessPosition(8,7), new ChessPiece(black, knight));
        addPiece(new ChessPosition(8,8), new ChessPiece(black, rook));

        for (int i = 1; i < 9; i++) {
            addPiece(new ChessPosition(7,i), new ChessPiece(black, pawn));
        }

        // Add white pieces
        addPiece(new ChessPosition(1,1), new ChessPiece(white, rook));
        addPiece(new ChessPosition(1,2), new ChessPiece(white, knight));
        addPiece(new ChessPosition(1,3), new ChessPiece(white, bishop));
        addPiece(new ChessPosition(1,4), new ChessPiece(white, queen));
        addPiece(new ChessPosition(1,5), new ChessPiece(white, king));
        addPiece(new ChessPosition(1,6), new ChessPiece(white, bishop));
        addPiece(new ChessPosition(1,7), new ChessPiece(white, knight));
        addPiece(new ChessPosition(1,8), new ChessPiece(white, rook));

        for (int i = 1; i < 9; i++) {
            addPiece(new ChessPosition(2,i), new ChessPiece(white, pawn));
        }
    }

    public String toString() {
        String string = "|";
        for (int i = 7; i > -1; i--) {
            for (int j = 0; j < 8; j++) {
                ChessPiece piece = squares[i][j];
                if (piece != null) {
                    if (piece.getPieceType() == ChessPiece.PieceType.ROOK && piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                        string = string + "R";
                    } else if (piece.getPieceType() == ChessPiece.PieceType.KNIGHT && piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                        string = string + "N";
                    } else if (piece.getPieceType() == ChessPiece.PieceType.BISHOP && piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                        string = string + "B";
                    } else if (piece.getPieceType() == ChessPiece.PieceType.QUEEN && piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                        string = string + "Q";
                    } else if (piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                        string = string + "K";
                    } else if (piece.getPieceType() == ChessPiece.PieceType.PAWN && piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                        string = string + "P";
                    }
                    if (piece.getPieceType() == ChessPiece.PieceType.ROOK && piece.getTeamColor() == ChessGame.TeamColor.BLACK) {
                        string = string + "r";
                    } else if (piece.getPieceType() == ChessPiece.PieceType.KNIGHT && piece.getTeamColor() == ChessGame.TeamColor.BLACK) {
                        string = string + "n";
                    } else if (piece.getPieceType() == ChessPiece.PieceType.BISHOP && piece.getTeamColor() == ChessGame.TeamColor.BLACK) {
                        string = string + "b";
                    } else if (piece.getPieceType() == ChessPiece.PieceType.QUEEN && piece.getTeamColor() == ChessGame.TeamColor.BLACK) {
                        string = string + "q";
                    } else if (piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == ChessGame.TeamColor.BLACK) {
                        string = string + "k";
                    } else if (piece.getPieceType() == ChessPiece.PieceType.PAWN && piece.getTeamColor() == ChessGame.TeamColor.BLACK) {
                        string = string + "p";
                    }
                } else {
                    string = string + " ";
                }

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

    public boolean kingExists() {
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                ChessPiece piece = this.getPiece(new ChessPosition(i,j));
                if (piece == null) {
                    continue;
                }
                if (piece.getPieceType() == ChessPiece.PieceType.KING) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public ChessBoard clone() {
        try {
            ChessBoard cloned = (ChessBoard) super.clone();
            cloned.squares = new ChessPiece[8][8];

            for (int i = 1; i < 9; i ++) {
                for (int j = 1; j < 9; j++) {
                    ChessPiece piece = this.squares[i-1][j-1];
                    if (piece != null) {
                        cloned.squares[i - 1][j - 1] = piece.clone();
                    }
                }
            }

            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("CHESSBOARD: Clone not supported", e);
        }
    }

    public ChessBoard(ChessBoard otherBoard) {
        this.squares = new ChessPiece[8][8];

        for (int i = 1; i < 9; i ++) {
            for (int j = 1; j < 9; j++) {
                ChessPiece piece = otherBoard.squares[i-1][j-1];
                if (piece != null) {
                    this.squares[i - 1][j - 1] = new ChessPiece(piece);
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(squares, that.squares);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(squares);
    }
}


