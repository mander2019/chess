package chess;

import javax.swing.text.Position;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece implements Cloneable {

    private PieceType pieceType;
    private ChessGame.TeamColor teamColor;
    protected boolean hasMoved;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceType = type;
        this.teamColor = pieceColor;
        this.hasMoved = false;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return teamColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return pieceType;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new ArrayList<>();

        if (pieceType == PieceType.PAWN) {
            moves =  new PawnMovesCalculator(board, myPosition).calculateMoves();
        } else if (pieceType == PieceType.KNIGHT) {
            moves = new KnightMovesCalculator(board, myPosition).calculateMoves();
        } else if (pieceType == PieceType.KING) {
            moves = new KingMovesCalculator(board, myPosition).calculateMoves();
        } else if (pieceType == PieceType.BISHOP) {
            moves =  new BishopMovesCalculator(board, myPosition).calculateMoves();
        } else if (pieceType == PieceType.ROOK) {
            moves =  new RookMovesCalculator(board, myPosition).calculateMoves();
        } else if (pieceType == PieceType.QUEEN) {
            Collection<ChessMove> rookMoves = new RookMovesCalculator(board, myPosition).calculateMoves();
            Collection<ChessMove> bishopMoves = new BishopMovesCalculator(board, myPosition).calculateMoves();

            for (ChessMove move : rookMoves) {
                moves.add(move);
            } for (ChessMove move : bishopMoves) {
                moves.add(move);
            }
        }

        return moves;
    }

    public void hasPawnMoved(ChessPosition chessPosition) {
        // Mark whether the pawn is in its start position
        if (teamColor == ChessGame.TeamColor.WHITE && chessPosition.getRow() == 2
                || teamColor == ChessGame.TeamColor.BLACK && chessPosition.getRow() == 7) {
            this.hasMoved = false;
        } else {
            this.hasMoved = true;
        }
    }

    @Override
    public ChessPiece clone() {
        try {
            ChessPiece cloned = (ChessPiece) super.clone();

            cloned.pieceType = this.pieceType;
            cloned.teamColor = this.teamColor;
            cloned.hasMoved = this.hasMoved;

            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("CHESSPIECE: Clone not supported", e);
        }
    }

    @Override
    public String toString() {
        return teamColor + " " + pieceType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return hasMoved == that.hasMoved && pieceType == that.pieceType && teamColor == that.teamColor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceType, teamColor, hasMoved);
    }
}
