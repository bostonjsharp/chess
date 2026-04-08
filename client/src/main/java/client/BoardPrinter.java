package client;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static ui.EscapeSequences.*;

public class BoardPrinter {

    public void drawBaseBoard(ChessGame game, ChessGame.TeamColor view, Set<ChessPosition> highlights){
        ChessBoard board = game.getBoard();

        printColLabels(view);

        int startRow = (view == ChessGame.TeamColor.WHITE) ? 8 : 1;
        int endRow = (view == ChessGame.TeamColor.WHITE) ? 1 : 8;
        int rowStep = (view == ChessGame.TeamColor.WHITE) ? -1 : 1;
        int startCol = (view == ChessGame.TeamColor.WHITE) ? 1 : 8;
        int endCol = (view == ChessGame.TeamColor.WHITE) ? 8: 1;
        int colStep = (view == ChessGame.TeamColor.WHITE) ? 1: -1;

        for (int row = startRow; (rowStep > 0) ? row <= endRow : row>= endRow; row += rowStep) {
            System.out.print(SET_BG_COLOR_BLACK + SET_TEXT_COLOR_WHITE + " " + row + " ");
            for (int col = startCol;(colStep > 0) ? col <= endCol : col>= endCol; col += colStep){
                ChessPosition position = new ChessPosition(row, col);
                boolean lightSquare = (row + col) % 2 == 1;
                boolean highlighted = highlights.contains(position);
                ChessPiece piece = board.getPiece(position);
                printSquare(lightSquare, highlighted, piece);
                if (col == endCol){
                    break;
                }
            }
            System.out.print(SET_BG_COLOR_BLACK + SET_TEXT_COLOR_WHITE + " " + row + " ");
            System.out.println(RESET_BG_COLOR + RESET_TEXT_COLOR);
            if(row == endRow){
                break;
            }
        }
        printColLabels(view);
        System.out.println(RESET_BG_COLOR + RESET_TEXT_COLOR);
    }

    public void drawBoard(ChessGame game, ChessGame.TeamColor view) {
        drawBaseBoard(game, view, Collections.emptySet());
    }

    public void drawHighlights(ChessGame game, ChessGame.TeamColor view, ChessPosition position, Collection<ChessMove> legalMoves){
        Set<ChessPosition> highlights = new HashSet<>();
        highlights.add(position);
        if(legalMoves != null){
            for (ChessMove move : legalMoves){
                highlights.add(move.getEndPosition());
            }
        }
        drawBaseBoard(game, view, highlights);
    }

    private void printColLabels(ChessGame.TeamColor view){
        System.out.print(SET_BG_COLOR_BLACK + SET_TEXT_COLOR_WHITE + "   ");

        if(view == ChessGame.TeamColor.WHITE){
            for (char col ='a'; col <= 'h'; col++) {
                System.out.print(" " + col + " ");
            }
        } else {
            for (char col = 'h'; col >= 'a'; col--){
                System.out.print(" " + col + " ");
            }
        }
        System.out.println("   " + RESET_BG_COLOR + RESET_TEXT_COLOR);
    }

    private void printSquare(boolean lightSquare, boolean highlighted, ChessPiece piece) {
        String bg;
        if (highlighted) {
            bg = SET_BG_COLOR_GREEN;
        } else {
            bg = lightSquare ? LIGHT_SQUARE : DARK_SQUARE;
        }
        System.out.print(bg);

        if (piece == null) {
            System.out.print("   ");
            return;
        }
        String textColor = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? WHITE_PIECE_COLOR : BLACK_PIECE_COLOR;
        System.out.print(textColor);
        System.out.print(" " + getPieceSymbol(piece) + " ");
        System.out.print(RESET_TEXT_COLOR);
    }

    private String getPieceSymbol(ChessPiece piece) {
        return switch (piece.getPieceType()) {
            case KING -> "K";
            case QUEEN -> "Q";
            case BISHOP -> "B";
            case KNIGHT -> "N";
            case ROOK -> "R";
            case PAWN -> "P";
        };
    }
}
