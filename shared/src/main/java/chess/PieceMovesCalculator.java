package chess;

public class PieceMovesCalculator {

    private ChessGame.TeamColor teamColor;
    private ChessPiece.PieceType pieceType;

    public PieceMovesCalculator(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.teamColor = pieceColor;
        this.pieceType = type;
    };



}
