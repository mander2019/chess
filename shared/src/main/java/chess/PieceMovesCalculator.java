package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

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

    public Collection<ChessMove> CalculateMoves() {
        return new ArrayList<>();
    }

}

