package chess;

import java.util.ArrayList;
import java.util.Collection;

public class PieceMovesCalculator {

    protected ChessBoard chessBoard;
    protected ChessPosition chessPosition;
    protected ChessPiece chessPiece;
    protected ChessGame.TeamColor pieceColor;

    public PieceMovesCalculator(ChessBoard board, ChessPosition position) {
        this.chessBoard = board;
        this.chessPosition = position;
        this.chessPiece = board.getPiece(position);
        this.pieceColor = chessPiece.getTeamColor();
    };

    public Collection<ChessMove> calculateMoves() {
        return new ArrayList<>();
    }

    public Boolean IsEnemyPiece(ChessPosition position) { return chessBoard.getPiece(position).getTeamColor() != pieceColor; }

    public Boolean IsFriendlyPiece(ChessPosition position) { return chessBoard.getPiece(position).getTeamColor() == pieceColor; }

    public Boolean IsEmpty(ChessPosition position) { return chessBoard.getPiece(position) == null; }

    public ChessMove NewMove(ChessPosition end) { return new ChessMove(chessPosition, end, null); }

    public Boolean WithinChessboard(ChessPosition position) {
        int row = position.getRow();
        int col = position.getColumn();

        if (row > 8 || col > 8 || row < 1 || col < 1) {
            return false;
        } else {
            return true;
        }
    }
}
