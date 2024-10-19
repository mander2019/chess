package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private TeamColor teamTurn;
    private ChessBoard chessBoard;

    public ChessGame() {
        this.chessBoard = new ChessBoard();
        chessBoard.resetBoard();
        this.teamTurn = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = chessBoard.getPiece(startPosition);
        Collection<ChessMove> moves;

        if (piece == null) {
            return null;
        } else {
            moves = piece.pieceMoves(chessBoard, startPosition);
        }

        ChessBoard dreamBoard;
        Collection<ChessMove> approvedMoves = new ArrayList<>();

        for (ChessMove move : moves) { // Only allows legitimate moves
            dreamBoard = new ChessBoard(chessBoard);
            System.out.println("Before: \n" + dreamBoard);
            dreamBoard.movePiece(move);
            System.out.println("After: \n" + dreamBoard);
            System.out.println(("Is in check: " + isInCheckHelper(piece.getTeamColor(), dreamBoard)));
//            System.out.println(dreamBoard.getPiece(new ChessPosition(4, 6)).pieceMoves(dreamBoard, new ChessPosition(4, 6)));
//            System.out.println(dreamBoard.getPiece(new ChessPosition(3, 7)));
            if (!isInCheckHelper(piece.getTeamColor(), dreamBoard)) {
                approvedMoves.add(move);
            }
        }

        return approvedMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();
        ChessPiece piece = chessBoard.getPiece(start);

        if (piece == null) {
            throw new InvalidMoveException("ERROR: Piece does not exist");
        }

        piece.hasPawnMoved(start);

        ChessBoard dreamBoard = new ChessBoard(chessBoard);

        if (!piece.pieceMoves(dreamBoard, start).contains(move)) {
            throw new InvalidMoveException("ERROR: Invalid move");
        } else if (piece.getPieceType() == ChessPiece.PieceType.PAWN & Math.abs(end.getRow() - start.getRow()) == 2 & piece.hasMoved) {
            throw new InvalidMoveException("ERROR: Pawn double move");
        } else if (teamTurn != piece.getTeamColor()) {
            System.out.println(chessBoard);
            throw new InvalidMoveException("ERROR: Wrong player's turn");
        } else if (chessBoard.getPiece(end) != null) {
            if (chessBoard.getPiece(end).getTeamColor() == teamTurn) {
                throw new InvalidMoveException("ERROR: Capture of own piece");
            }
        } else if (chessBoard.kingExists()) {
            if (isInCheckHelper(piece.getTeamColor(), dreamBoard)) {
                throw new InvalidMoveException("ERROR: Cannot put own king in check");
            }
        }

        // Marks a piece as moved if this is its first move
        if (!piece.hasMoved) {
            piece.hasMoved = true;
        }

        chessBoard.movePiece(move);

        // Next player's turn
        if (teamTurn == TeamColor.BLACK) {
            setTeamTurn(TeamColor.WHITE);
        } else {
            setTeamTurn(TeamColor.BLACK);
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return isInCheckHelper(teamColor, chessBoard);
    }

    private boolean isInCheckHelper(TeamColor teamColor, ChessBoard board) {
        ChessPosition kingPosition = getKingPosition(teamColor, board);

        Collection<Collection<ChessMove>> enemyMoves = new ArrayList<>();
        ChessPosition p;
        ChessPiece piece;

        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                p = new ChessPosition(i, j);
                if (board.getPiece(p) != null) {
                    piece = board.getPiece(p);
                    if (piece.getTeamColor() != teamColor) {
                        enemyMoves.add(piece.pieceMoves(board, p));
                    }
                }
            }
        }


//        System.out.println(board);


        for (Collection<ChessMove> moves : enemyMoves) {
            for (ChessMove move : moves) {
                if (move.getEndPosition().equals(kingPosition)) {
                    return true;
                }
            }
        }

        return false;
    }

    private ChessPosition getKingPosition(TeamColor color) {
        return getKingPosition(color, chessBoard);
    }

    private ChessPosition getKingPosition(TeamColor color, ChessBoard board) {
        ChessPosition p;
        ChessPiece piece;
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                p = new ChessPosition(i, j);
                if (board.getPiece(p) != null) {
                    piece = board.getPiece(p);
                    if (piece.getPieceType() == ChessPiece.PieceType.KING & piece.getTeamColor() == color) {
                        return p;
                    }
                }
            }
        }

        throw new RuntimeException("ERROR: King not found");
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {

        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                ChessPosition p = new ChessPosition(i,j);
                ChessPiece piece = chessBoard.getPiece(p);
                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> pieceMoves = piece.pieceMoves(chessBoard, p);
                    if (isInCheckmateHelper(teamColor, pieceMoves)) return false;
                }
            }
        }

        if (isInCheck(teamColor)) {
            return true;
        }

        return false;
    }

    private boolean isInCheckmateHelper(TeamColor teamColor, Collection<ChessMove> pieceMoves) {
        for (ChessMove move : pieceMoves) {
            ChessBoard dreamBoard = new ChessBoard(chessBoard);
            dreamBoard.movePiece(move);

            if (!isInCheckHelper(teamColor, dreamBoard)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheckmate(teamColor)) {
            return false;
        }

        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                ChessPosition p = new ChessPosition(i,j);
                ChessPiece piece = chessBoard.getPiece(p);
                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> pieceMoves = piece.pieceMoves(chessBoard, p);
                    if (isInStalemateHelper(teamColor, pieceMoves)) return false;
                }
            }
        }

        return true;
}

    private boolean isInStalemateHelper(TeamColor teamColor, Collection<ChessMove> pieceMoves) {
        for (ChessMove move : pieceMoves) {
            ChessBoard dreamBoard = new ChessBoard(chessBoard);

            System.out.println("Before: \n" + dreamBoard);

            dreamBoard.movePiece(move);

            if (!isInCheckHelper(teamColor, dreamBoard)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        chessBoard = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return chessBoard;
    }

    @Override
    public String toString() {
        return "ChessGame{" +
                "teamTurn=" + teamTurn +
                ", chessBoard=" + chessBoard +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return teamTurn == chessGame.teamTurn && Objects.equals(chessBoard, chessGame.chessBoard);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamTurn, chessBoard);
    }
}

