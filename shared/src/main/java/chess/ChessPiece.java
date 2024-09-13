package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private PieceType pieceType;
    private ChessGame.TeamColor teamColor;
    protected boolean HasMoved;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceType = type;
        this.teamColor = pieceColor;
        this.HasMoved = false;
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
        if (pieceType == PieceType.PAWN) {
            return new PawnMovesCalculator(board, myPosition).CalculateMoves();
        } else if (pieceType == PieceType.KNIGHT) {
            return new KnightMovesCalculator(board, myPosition).CalculateMoves();
        } else if (pieceType == PieceType.KING) {
            return new KingMovesCalculator(board, myPosition).CalculateMoves();
        } else if (pieceType == PieceType.BISHOP) {
            return new BishopMovesCalculator(board, myPosition).CalculateMoves();
        } else if (pieceType == PieceType.ROOK) {
            return new RookMovesCalculator(board, myPosition).CalculateMoves();
        } else if (pieceType == PieceType.QUEEN) {
            Collection<ChessMove> moves = new RookMovesCalculator(board, myPosition).CalculateMoves();
            Collection<ChessMove> moves2 = new BishopMovesCalculator(board, myPosition).CalculateMoves();

            for (ChessMove move : moves2) {
                moves.add(move);
            }

            return moves;
        }

        return new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessPiece that = (ChessPiece) o;
        return pieceType == that.pieceType && teamColor == that.teamColor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceType, teamColor);
    }
}
